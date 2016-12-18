/*
    Copyright 2013-2016 appPlant GmbH

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */

package de.appplant.cordova.plugin.printer;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrinterId;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import de.appplant.cordova.plugin.printer.ext.PrintManager;
import de.appplant.cordova.plugin.printer.ext.PrintManager.OnPrintJobStateChangeListener;
import de.appplant.cordova.plugin.printer.ext.PrintServiceInfo;
import de.appplant.cordova.plugin.printer.reflect.Meta;
import de.appplant.cordova.plugin.printer.ui.SelectPrinterActivity;

import static android.print.PrintJobInfo.STATE_STARTED;
import static de.appplant.cordova.plugin.printer.ui.SelectPrinterActivity.ACTION_SELECT_PRINTER;
import static de.appplant.cordova.plugin.printer.ui.SelectPrinterActivity.EXTRA_PRINTER_ID;

/**
 * Plugin to print HTML documents. Therefore it creates an invisible web view
 * that loads the markup data. Once the page has been fully rendered it takes
 * the print adapter of that web view and initializes a print job.
 */
public class Printer extends CordovaPlugin {

    /**
     * The web view that loads all the content.
     */
    private WebView view;

    /**
     * Reference is necessary to invoke the callback in the onresume event.
     */
    private CallbackContext command;

    /**
     * Instance of the print manager to listen for job status changes.
     */
    private PrintManager pm;

    /**
     * Invokes the callback once the job has reached a final state.
     */
    OnPrintJobStateChangeListener listener = new OnPrintJobStateChangeListener() {
        /**
         * Callback notifying that a print job state changed.
         *
         * @param job The print job.
         */
        @Override
        public void onPrintJobStateChanged(PrintJob job) {
            notifyAboutPrintJobResult(job);
        }
    };

    /**
     * Default name of the printed document (PDF-Printer).
     */
    private static final String DEFAULT_DOC_NAME = "unknown";

    /**
     * Executes the request.
     *
     * This method is called from the WebView thread.
     * To do a non-trivial amount of work, use:
     *     cordova.getThreadPool().execute(runnable);
     *
     * To run on the UI thread, use:
     *     cordova.getActivity().runOnUiThread(runnable);
     *
     * @param action   The action to execute.
     * @param args     The exec() arguments in JSON form.
     * @param callback The callback context used when calling back into JavaScript.
     * @return         Whether the action was valid.
     */
    @Override
    public boolean execute (String action, JSONArray args,
                            CallbackContext callback) throws JSONException {

        command = callback;

        if (action.equalsIgnoreCase("check")) {
            check();
            return true;
        }

        if (action.equalsIgnoreCase("pick")) {
            pick();
            return true;
        }

        if (action.equalsIgnoreCase("print")) {
            print(args);
            return true;
        }

        return false;
    }

    /**
     * Informs if the device is able to print documents.
     * A Internet connection is required to load the cloud print dialog.
     */
    private void check () {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                List<PrintServiceInfo> services  = pm.getEnabledPrintServices();
                Boolean available = services.size() > 0;

                PluginResult res1 = new PluginResult(
                        PluginResult.Status.OK, available);
                PluginResult res2 = new PluginResult(
                        PluginResult.Status.OK, services.size());
                PluginResult res  = new PluginResult(
                        PluginResult.Status.OK, Arrays.asList(res1, res2));

                command.sendPluginResult(res);
            }
        });
    }

    /**
     * Loads the HTML content into the web view and invokes the print manager.
     *
     * @param args
     *      The exec arguments as JSON
     */
    private void print (final JSONArray args) {
        final String content   = args.optString(0, "<html></html>");
        final JSONObject props = args.optJSONObject(1);

        cordova.getActivity().runOnUiThread( new Runnable() {
            @Override
            public void run() {
                initWebView(props);
                loadContent(content);
            }
        });
    }

    /**
     * Presents a list with all enabled print services and invokes the
     * callback with the selected one.
     */
    private void pick () {
        Intent intent = new Intent(
            cordova.getActivity(), SelectPrinterActivity.class);

        cordova.startActivityForResult(this, intent, 0);
    }

    /**
     * Loads the content into the web view.
     *
     * @param content
     *      Either an HTML string or URI
     */
    private void loadContent(String content) {
        if (content.startsWith("http") || content.startsWith("file:")) {
            view.loadUrl(content);
        } else {
            String baseURL = webView.getUrl();
            baseURL        = baseURL.substring(0, baseURL.lastIndexOf('/') + 1);

            // Set base URI to the assets/www folder
            view.loadDataWithBaseURL(
                    baseURL, content, "text/html", "UTF-8", null);
        }
    }

    /**
     * Configures the WebView components which will call the Google Cloud Print
     * Service.
     *
     * @param props
     *      The JSON object with the containing page properties
     */
    private void initWebView (JSONObject props) {
        Activity ctx         = cordova.getActivity();
        view                 = new WebView(ctx);
        WebSettings settings = view.getSettings();

        settings.setDatabaseEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setSaveFormData(true);
        settings.setUseWideViewPort(true);
        view.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        if (Build.VERSION.SDK_INT >= 21) {
            Method setMixedContentModeMethod = Meta.getMethod(
                    settings.getClass(), "setMixedContentMode", int.class);

            Meta.invokeMethod(settings, setMixedContentModeMethod, 2);
        }

        setWebViewClient(props);
    }

    /**
     * Creates the web view client which sets the print document.
     *
     * @param props
     *      The JSON object with the containing page properties
     */
    private void setWebViewClient (JSONObject props) {
        final String docName    = props.optString("name", DEFAULT_DOC_NAME);
        final boolean landscape = props.optBoolean("landscape", false);
        final boolean graystyle = props.optBoolean("graystyle", false);
        final String  duplex    = props.optString("duplex", "none");

        view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading (WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished (WebView webView, String url) {
                PrintAttributes.Builder builder = new PrintAttributes.Builder();
                PrintDocumentAdapter adapter    = getAdapter(webView, docName);

                builder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);

                builder.setColorMode(graystyle
                        ? PrintAttributes.COLOR_MODE_MONOCHROME
                        : PrintAttributes.COLOR_MODE_COLOR);

                builder.setMediaSize(landscape
                        ? PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE
                        : PrintAttributes.MediaSize.UNKNOWN_PORTRAIT);

                if (!duplex.equals("none") && Build.VERSION.SDK_INT >= 23) {
                    boolean longEdge = duplex.equals("long");
                    Method setDuplexModeMethod = Meta.getMethod(
                            builder.getClass(), "setDuplexMode", int.class);

                    Meta.invokeMethod(builder, setDuplexModeMethod,
                            longEdge ? 2 : 4);
                }

                pm.getInstance().print(docName, adapter, builder.build());
            }
        });
    }

    /**
     * Invoke the callback send with `print` to inform about the result.
     *
     * @param job The print job.
     */
    @SuppressWarnings("ConstantConditions")
    private void notifyAboutPrintJobResult(PrintJob job) {

        if (job == null || command == null ||
                job.getInfo().getState() <= STATE_STARTED) {
            return;
        }

        PluginResult res = new PluginResult(
                PluginResult.Status.OK, job.isCompleted());

        command.sendPluginResult(res);

        view = null;
    }

    /**
     * Create the print document adapter for the web view component. On
     * devices older then SDK 21 it will use the deprecated method
     * `createPrintDocumentAdapter` without arguments and on newer devices
     * the recommended way.
     *
     * @param webView
     *      The web view which content to print out.
     * @param docName
     *      The name of the printed document.
     * @return
     *      The created adapter.
     */
    private PrintDocumentAdapter getAdapter (WebView webView, String docName) {
        if (Build.VERSION.SDK_INT >= 21) {
            Method createPrintDocumentAdapterMethod = Meta.getMethod(
                    WebView.class, "createPrintDocumentAdapter", String.class);

            return (PrintDocumentAdapter) Meta.invokeMethod(
                    webView, createPrintDocumentAdapterMethod, docName);
        } else {
            return (PrintDocumentAdapter) Meta.invokeMethod(webView,
                    "createPrintDocumentAdapter");
        }
    }

    /**
     * Called after plugin construction and fields have been initialized.
     */
    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        pm = new PrintManager(cordova.getActivity());
        pm.setOnPrintJobStateChangeListener(listener);
    }

    /**
     * The final call you receive before your activity is destroyed.
     */
    @Override
    public void onDestroy() {
        pm.unsetOnPrintJobStateChangeListener();

        pm       = null;
        listener = null;
        command  = null;
        view     = null;

        super.onDestroy();
    }

    /**
     * Invoke the callback from `pick` method.
     *
     * @param requestCode   The request code originally supplied to
     *                      startActivityForResult(), allowing you to
     *                      identify who this result came from.
     * @param resultCode    The integer result code returned by the child
     *                      activity through its setResult().
     * @param intent        An Intent, which can return result data to the
     *                      caller (various data can be
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (command == null || intent == null) {
            return;
        }

        if (!intent.getAction().equals(ACTION_SELECT_PRINTER)) {
            return;
        }

        PrinterId printer = intent.getParcelableExtra(EXTRA_PRINTER_ID);

        PluginResult res  = new PluginResult(PluginResult.Status.OK,
                printer != null ? printer.getLocalId() : null);

        command.sendPluginResult(res);
    }
}
