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
import android.content.Context;
import android.os.Build;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Printer extends CordovaPlugin {

    private WebView view;

    private CallbackContext command;

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

        if (action.equalsIgnoreCase("isAvailable")) {
            isAvailable();
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
    private void isAvailable () {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                List<String> ids  = getEnabledPrintServiceIds();
                Boolean available = ids.size() > 1;

                PluginResult res1 = new PluginResult(
                        PluginResult.Status.OK, available);
                PluginResult res2 = new PluginResult(
                        PluginResult.Status.OK, new JSONArray(ids));
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
            Method setMixedContentModeMethod = getMethod(settings.getClass(),
                    "setMixedContentMode", int.class);

            invokeMethod(settings, setMixedContentModeMethod, 2);
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
                PrintManager printManager       = getPrintMgr();
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
                    Method setDuplexModeMethod = getMethod(builder.getClass(),
                            "setDuplexMode", int.class);

                    invokeMethod(builder, setDuplexModeMethod,
                            longEdge ? 2 : 4);
                }

                PrintJob job = printManager.print(
                        docName, adapter, builder.build());

                invokeCallbackOnceCompletedOrCanceled(job);

                view = null;
            }
        });
    }

    /**
     * Invokes the callback once the print job is complete or was canceled.
     *
     * @param job
     *      The reference to the print job
     */
    private void invokeCallbackOnceCompletedOrCanceled (final PrintJob job) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                for (;;) {
                    if (job.isCancelled() || job.isCompleted() || job.isFailed()) {
                        PluginResult res = new PluginResult(
                                PluginResult.Status.OK, job.isCompleted());

                        command.sendPluginResult(res);
                        break;
                    }
                }
            }
        });
    }

    /**
     * Get a PrintManager instance.
     *
     * @return A PrintManager instance.
     */
    private PrintManager getPrintMgr () {
        return (PrintManager) cordova.getActivity()
                .getSystemService(Context.PRINT_SERVICE);
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
            Method createPrintDocumentAdapterMethod = getMethod(
                    WebView.class, "createPrintDocumentAdapter", String.class);

            return (PrintDocumentAdapter) invokeMethod(
                    webView, createPrintDocumentAdapterMethod, docName);
        } else {
            Method createPrintDocumentAdapterMethod = getMethod(
                    WebView.class, "createPrintDocumentAdapter");

            return (PrintDocumentAdapter) invokeMethod(
                    webView, createPrintDocumentAdapterMethod);
        }
    }

    /**
     * Get a list of ids of all installed and enabled print services. For
     * that it uses reflections to call public but hidden methods from the
     * PrintManager.
     *
     * @return A list of found print service ids.
     */
    private List<String> getEnabledPrintServiceIds () {
        try {
            PrintManager printMgr = getPrintMgr();
            Class<?> printerCls = Class.forName(
                    "android.printservice.PrintServiceInfo");
            Method getPrinterMethod = getMethod(printMgr.getClass(),
                    "getEnabledPrintServices");
            Method getIdMethod = getMethod(printerCls,
                    "getId");

            List printers = (List) invokeMethod(printMgr, getPrinterMethod);
            ArrayList<String> printerIds = new ArrayList<String>();

            printerIds.add("android.print.pdf");

            if (printers == null)
                return printerIds;

            for (Object printer : printers) {
                String printerId = (String) invokeMethod(printer, getIdMethod);
                printerIds.add(printerId);
            }

            return printerIds;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    /**
     * Finds the method with given name and set of arguments.
     *
     * @param cls
     *      The class in where to look for the method declaration.
     * @param name
     *      The name of the method.
     * @param params
     *      The arguments of the method.
     * @return
     *      The found method or null.
     */
    private Method getMethod (Class<?> cls, String name, Class<?>... params) {
        try {
            return cls.getDeclaredMethod(name, params);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Invokes the method on the given object with the specified arguments.
     *
     * @param obj
     *      An object which class defines the method.
     * @param method
     *      The method to invoke.
     * @param args
     *      Set of arguments.
     * @return
     *      The returned object or null.
     */
    private Object invokeMethod (Object obj, Method method, Object... args) {
        try {
            return method.invoke(obj, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }
}
