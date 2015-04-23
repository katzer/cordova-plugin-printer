/*
    Copyright 2013-2014 appPlant UG

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

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@TargetApi(19)
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

        // Returning false results in a "MethodNotFound" error.
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
                Boolean supported   = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
                PluginResult result = new PluginResult(PluginResult.Status.OK, supported);

                command.sendPluginResult(result);
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
            //Set base URI to the assets/www folder
            String baseURL = webView.getUrl();
            baseURL        = baseURL.substring(0, baseURL.lastIndexOf('/') + 1);

            view.loadDataWithBaseURL(baseURL, content, "text/html", "UTF-8", null);
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
        Activity ctx = cordova.getActivity();
        view         = new WebView(ctx);

        view.getSettings().setDatabaseEnabled(true);

        setWebViewClient(props);
    }

    /**
     * Creates the web view client which sets the print document.
     *
     * @param props
     *      The JSON object with the containing page properties
     */
    private void setWebViewClient (JSONObject props) {
        final String docName = props.optString("name", DEFAULT_DOC_NAME);
        final boolean landscape = props.optBoolean("landscape", false);
        final boolean graystyle = props.optBoolean("graystyle", false);

        view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading (WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished (WebView webView, String url) {
                // Get a PrintManager instance
                PrintManager printManager = (PrintManager) cordova.getActivity()
                        .getSystemService(Context.PRINT_SERVICE);

                // Get a print adapter instance
                PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

                // Get a print builder instance
                PrintAttributes.Builder builder = new PrintAttributes.Builder();

                // The page does itself set its own margins
                builder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);

                builder.setColorMode(graystyle ? PrintAttributes.COLOR_MODE_MONOCHROME
                        : PrintAttributes.COLOR_MODE_COLOR);

                builder.setMediaSize(landscape ? PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE
                        : PrintAttributes.MediaSize.UNKNOWN_PORTRAIT);

                // Create a print job with name and adapter instance
                PrintJob job = printManager.print(docName, printAdapter, builder.build());

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
                        command.success();
                        break;
                    }
                }
            }
        });
    }
}
