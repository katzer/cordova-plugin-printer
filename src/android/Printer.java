/*
    Copyright 2013 appPlant UG

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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@TargetApi(19)
public class Printer extends CordovaPlugin {

    @Override
    public boolean execute (String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // Es soll überprüft werden, ob ein Dienst zum Ausdrucken von Inhalten zur Verfügung steht
        if (action.equalsIgnoreCase("isAvailable")) {
            isAvailable(callbackContext);

            return true;
        }

        // Etwas soll ausgedruckt werden
        if (action.equalsIgnoreCase("print")) {
            print(args, callbackContext);

            return true;
        }

        // Returning false results in a "MethodNotFound" error.
        return false;
    }

    /**
     * Überprüft, ob ein Drucker zur Verfügung steht.
     */
    private void isAvailable (CallbackContext ctx) {
        Boolean supported   = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        PluginResult result = new PluginResult(PluginResult.Status.OK, supported);

        ctx.sendPluginResult(result);
    }

    /**
     * Druckt den HTML Content aus.
     */
    private void print (final JSONArray args, CallbackContext ctx) {
        final Printer self = this;

        cordova.getActivity().runOnUiThread( new Runnable() {
            public void run() {
                String content     = args.optString(0, "<html></html>");
                WebView controller = self.getPrintController();

                self.loadContentIntoPrintController(content, controller);

                self.startPrinterApp(controller);
            }
        });
    }

    /**
     * Erstellt den Print-View.
     */
    private WebView getPrintController () {
        WebView webview = new WebView(cordova.getActivity());

        webview.setVisibility(View.INVISIBLE);
        webview.getSettings().setJavaScriptEnabled(false);

        return webview;
    }

    /**
     * Lädt den zu druckenden Content in ein WebView, welcher vom Drucker ausgedruckt werden soll.
     */
    private void loadContentIntoPrintController (String content, WebView webview) {
        //Set base URI to the assets/www folder
        String baseURL = webView.getUrl();
        baseURL        = baseURL.substring(0, baseURL.lastIndexOf('/') + 1);

        webview.loadDataWithBaseURL(baseURL, content, "text/html", "UTF-8", null);
    }

    /**
     * Öffnet die Printer App, damit der Content ausgedruckt werden kann.
     */
    private void startPrinterApp (WebView webview) {
        webview.setWebViewClient (new WebViewClient() {
            public boolean shouldOverrideUrlLoading (WebView view, String url) {
                return false;
            }

            public void onPageFinished (WebView webview, String url) {
                // Get a PrintManager instance
                PrintManager printManager = (PrintManager) cordova.getActivity()
                    .getSystemService(Context.PRINT_SERVICE);

                // Get a print adapter instance
                PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

                // Get a print builder instance
                PrintAttributes.Builder builder = new PrintAttributes.Builder();

                builder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);

                // Create a print job with name and adapter instance
                printManager.print("Print Document", printAdapter, builder.build());
            }
        });
    }
}
