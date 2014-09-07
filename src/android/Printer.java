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

package de.appplant.cordova.plugin.cloudprint;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * This plug in brings up a native overlay to print HTML documents using
 * AirPrint for iOS and Google Cloud Print for Android.
 */
public class Printer extends CordovaPlugin {

    private WebView view;
    private CallbackContext command;

    private static final String PRINT_DIALOG_URL =
            "https://www.google.com/cloudprint/dialog.html";

    private static final String JS_INTERFACE = "Printer";

    private static final String DEFAULT_DOC_NAME = "unknown";

    /**
     * Post message that is sent by Print Dialog web
     * page when the printing dialog needs to be closed.
     */
    static final
    private String CLOSE_POST_MESSAGE_NAME = "cp-dialog-on-close";

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
     * @param action          The action to execute.
     * @param rawArgs         The exec() arguments in JSON form.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return                Whether the action was valid.
     */
    @Override
    public boolean execute (String action, JSONArray args,
            CallbackContext callbackContext) throws JSONException {

        command = callbackContext;

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
        Boolean supported   = isOnline();
        PluginResult result = new PluginResult(PluginResult.Status.OK, supported);

        command.sendPluginResult(result);
    }

    /**
     * Configures the WebView components which will call the Google Cloud Print
     * Service.
     */
    private void setup () {
        Activity ctx = cordova.getActivity();
        view         = new WebView(ctx);
        WebSettings settings = view.getSettings();

        view.setVisibility(View.INVISIBLE);
        view.setVerticalScrollBarEnabled(false);
        view.setHorizontalScrollBarEnabled(false);
        view.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        view.setScrollbarFadingEnabled(false);

        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setJavaScriptEnabled(true);

        ctx.addContentView(view, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
                );
    }

    /**
     * Creates the web view client which sets the print document.
     *
     * @param content
     *      HTML encoded string
     * @param docName
     *      The name for the document
     */
    private void setupWebViewClient (final String content, final String docName) {
        view.setWebViewClient( new WebViewClient() {
            @Override
            public void onPageFinished(final WebView view, String url) {
                StringBuffer js = new StringBuffer();

                if (!url.equals(PRINT_DIALOG_URL)) {
                    return;
                }

                js.append("javascript:printDialog.setPrintDocument(");
                js.append("printDialog.createPrintDocument(");
                js.append("'text/html',");
                js.append("'" + docName + "',");
                js.append("'" + content + "'");
                js.append("))");

                // Submit print document
                view.loadUrl(js.toString());

                js.delete(0,  js.length());

                js.append("javascript:window.addEventListener('message',");
                js.append("function(evt){");
                js.append(JS_INTERFACE);
                js.append(".onPostMessage(evt.data)}, false)");

                // Add post messages listener
                view.loadUrl(js.toString());
            }
        });
    }

    /**
     * JS interface to get informed when the job is finished and the view should
     * be closed.
     */
    private void setupJavascriptInterface () {
        view.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void onPostMessage (String message) {
                if (message.startsWith(CLOSE_POST_MESSAGE_NAME)) {
                    removeView();
                    command.success();
                }
            }
        }, JS_INTERFACE);
    }

    /**
     * Loads the Google Cloud Print Dialog page and opens them with the called
     * content.
     *
     * @param args
     *      The exec arguments as JSON
     */
    private void print (JSONArray args) {
        final String content = args.optString(0, "<html></html>");
        final String docName = args.optString(1, DEFAULT_DOC_NAME);

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setup();
                setupWebViewClient(content, docName);
                setupJavascriptInterface();

                view.loadUrl(PRINT_DIALOG_URL);
                view.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Checks if the device is connected to the Internet.
     *
     * @return
     *      true if online otherwise false
     */
    private Boolean isOnline () {
        Activity activity = cordova.getActivity();
        ConnectivityManager conMGr =
                (ConnectivityManager) activity.getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = conMGr.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnected();
    }

    /**
     * Removes the view from the layout.
     */
    public void removeView () {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup vg = (ViewGroup)view.getParent();

                view.stopLoading();
                vg.removeView(view);
            }
        });
    }
}