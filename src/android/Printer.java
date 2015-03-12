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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * This plug in brings up a native overlay to print HTML documents using
 * AirPrint for iOS and Google Cloud Print for Android.
 */
public class Printer extends CordovaPlugin {
    private WebView view;

    private CallbackContext command;

    private static final String DEFAULT_DOC_NAME = "unknown";

    /**
     * Executes the request.
     *
     * This method is called from the WebView thread.
     * To do a non-trivial amount of work, use:
     *     cordova.getThreadPool().execute(runnable);hh
     *
     * To run on the UI thread, use:
     *     cordova.getActivity().runOnUiThread(runnable);
     *
     * @param action   The action to execute.
     * @param args     The exec() arguments in JSON form.
     * @param callback The callback context used when calling
     *                 back into JavaScript.
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
        if(Build.VERSION.SDK_INT >= 19){
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    Boolean supported   = true;
                    PluginResult result = new PluginResult(PluginResult.Status.OK, supported);

                    command.sendPluginResult(result);
                }
            });
        } else {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    Boolean supported = isOnline();
                    PluginResult result;

                    if (!supported) {
                        supported = hasGoogleCloudPrintApp();
                    }

                    result = new PluginResult(PluginResult.Status.OK, supported);

                    command.sendPluginResult(result);
                }
            });
        }
    }

    /** Call SDK-Version specific Print-function
     *
     * @param args
     *          The exec arguments as JSON
     */
    private void print (JSONArray args){
        if(Build.VERSION.SDK_INT >= 19){
            printAndroid(args);
        } else {
            printCloud(args);
        }
    }


//Android-Print functions---------------------------------------------------------------------------

    /**
     * Loads the HTML content into the web view and invokes the print manager.
     *
     * @param args
     *      The exec arguments as JSON
     */
    private void printAndroid (final JSONArray args) {
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
                try {
                    // Get a PrintManager instance
                    String PRINT_SERVICE = (String) Context.class.getDeclaredField("PRINT_SERVICE").get(null);
                    Object printManager = cordova.getActivity().getSystemService(PRINT_SERVICE);

                    // Get a print adapter instance
                    Class<?> printDocumentAdapterClass = Class.forName("android.print.PrintDocumentAdapter");
                    Method createPrintDocumentAdapterMethod = webView.getClass().getMethod("createPrintDocumentAdapter");
                    Object printAdapter = createPrintDocumentAdapterMethod.invoke(webView);

                    // Get a print builder instance
                    Class<?> printAttributesBuilderClass = Class.forName("android.print.PrintAttributes$Builder");
                    Constructor<?> ctor = printAttributesBuilderClass.getConstructor();
                    Object printAttributes = ctor.newInstance(new Object[]{});


                    // The page does itself set its own margins
                    Class<?> Margins = Class.forName("android.print.PrintAttributes$Margins");
                    Object noMargins = Margins.getDeclaredField("NO_MARGINS").get(null);
                    Method setMinMargins = printAttributesBuilderClass.getMethod("setMinMargins",Margins);
                    setMinMargins.invoke(printAttributes,noMargins);

                    //Set Colormode
                    Method setColorMode = printAttributesBuilderClass.getMethod("setColorMode", int.class);
                    if(graystyle){
                        // 1 is the colormode_mode_monochrome int
                        setColorMode.invoke(printAttributes,1);
                    }else {
                        // 2 is the colormode_mode_color int
                        setColorMode.invoke(printAttributes,2);
                    }

                    // Set landscape-mode
                    Class<?> MediaSize = Class.forName("android.print.PrintAttributes$MediaSize");
                    Object landscapeMode = MediaSize.getDeclaredField("UNKNOWN_LANDSCAPE").get(null);
                    Object portraitMode = MediaSize.getDeclaredField("UNKNOWN_PORTRAIT").get(null);
                    Method setMediaSize = printAttributesBuilderClass.getMethod("setMediaSize",MediaSize);
                    if (landscape){
                        setMediaSize.invoke(printAttributes,landscapeMode);
                    } else {
                        setMediaSize.invoke(printAttributes,portraitMode);
                    }


                    // Call build method of attributeBuilder
                    Method buildMethod = printAttributes.getClass().getMethod("build");
                    Object printAttributesBuild = buildMethod.invoke(printAttributes);

                    // Create a print job with name and adapter instance
                    Method printMethod = printManager.getClass().getMethod("print", String.class, printDocumentAdapterClass, printAttributesBuild.getClass());
                    Object printJob = printMethod.invoke(printManager, docName, printAdapter, printAttributesBuild);

                    invokeCallbackOnceCompletedOrCanceled(printJob);
                } catch (Exception e){
                    e.printStackTrace();
                }
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
    private void invokeCallbackOnceCompletedOrCanceled (final Object job) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                for (;;) {
                    try {
                        Method isCancelled = job.getClass().getMethod("isCancelled");
                        Method isCompleted = job.getClass().getMethod("isCompleted");
                        Method isFailed = job.getClass().getMethod("isFailed");

                        Boolean cancelled = (Boolean) isCancelled.invoke(null);
                        Boolean completed = (Boolean) isCompleted.invoke(null);
                        Boolean failed = (Boolean) isFailed.invoke(null);

                        if (cancelled || completed || failed ) {
                            command.success();
                            break;
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

//Cloud-Print functions-----------------------------------------------------------------------------

    /**
     * Create an intent with the content to print out
     * and sends that to the cloud print activity.
     *
     * @param args
     *      The exec arguments as JSON
     */
    private void printCloud (JSONArray args) {
        final String content = args.optString(0, "<html></html>");
        final String title = args.optJSONObject(1)
                .optString("name", DEFAULT_DOC_NAME);

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebView browser = new WebView(cordova.getActivity());

                new ContentClient(content, browser) {
                    @Override
                    void onContentReady(String content) {
                        if (hasGoogleCloudPrintApp()) {
                            printViaGoogleCloudPrintApp(
                                    content, title);
                        } else {
                            printViaGoogleCloudPrintDialog(
                                    content, title);
                        }
                    }
                };
            }
        });
    }

    /**
     * Checks if the device is connected
     * to the Internet.
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
     * Ask the package manager if the google cloud print app
     * is installed on the device.
     *
     * @return
     *      true if yes otherwise false
     */
    private boolean hasGoogleCloudPrintApp() {
        PackageManager pm = cordova.getActivity().getPackageManager();

        try {
            pm.getPackageInfo("com.google.android.apps.cloudprint", 0);
            return true;
        } catch(PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Uses the native cloud print app to print the content.
     *
     * @param content
     *      The HTML encoded content
     * @param title
     *      The title for the print job
     */
    private void printViaGoogleCloudPrintApp(String content, String title) {
        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.setPackage("com.google.android.apps.cloudprint");

        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_TEXT, content);

        cordova.startActivityForResult(this, intent, 0);
    }

    /**
     * Uses the cloud print web dialog to print the content.
     *
     * @param content
     *      The HTML encoded content
     * @param title
     *      The title for the print job
     */
    private void printViaGoogleCloudPrintDialog(String content, String title) {
        Intent intent = new Intent(
                cordova.getActivity(), CloudPrintDialog.class);

        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_TEXT, content);

        cordova.startActivityForResult(null, intent, 0);
        cordova.setActivityResultCallback(this);
    }

    /**
     * Called when an activity you launched exits, giving you the reqCode you
     * started it with, the resCode it returned, and any additional data from it.
     *
     * @param reqCode     The request code originally supplied to startActivityForResult(),
     *                    allowing you to identify who this result came from.
     * @param resCode     The integer result code returned by the child activity
     *                    through its setResult().
     * @param intent      An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int reqCode, int resCode, Intent intent) {
        super.onActivityResult(reqCode, resCode, intent);
        command.success();
        command = null;
    }

    /**
     * Holds HTML content passed from WebView.
     */
    private class ContentHolder {

        String htmlContent;

        /**
         * Setter for the HTML content.
         */
        @JavascriptInterface
        @SuppressWarnings("UnusedDeclaration")
        public void setContent(String htmlContent) {
            this.htmlContent = htmlContent;
        }

        /**
         * @return
         *      HTML encoded string
         */
        public String getContent() {
            return htmlContent;
        }

        /**
         * @return
         *      If the content is available or not.
         */
        public boolean isContentReady() {
            return htmlContent != null;
        }
    }

    /**
     * Custom web browser client to easily get
     * the HTML content as an URI.
     */
    abstract class ContentClient extends WebViewClient {

        private WebView browser;

        private final ContentHolder holder =
                new ContentHolder();

        ContentClient(String content, WebView webView) {
            this.browser = webView;

            initWebView();
            loadContent(content);
        }

        /**
         * Configures the WebView components which
         * will hold the print content.
         */
        @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
        private void initWebView () {
            WebSettings settings = browser.getSettings();

            settings.setLoadWithOverviewMode(true);
            settings.setUseWideViewPort(true);
            settings.setJavaScriptEnabled(true);

            browser.addJavascriptInterface(
                    holder, "ContentHolder");

            browser.setWebViewClient(this);
        }

        /**
         * Loads the content into the web view.
         *
         * @param content
         *      Either an HTML string or URI
         */
        private void loadContent(String content) {
            if (content.startsWith("http") || content.startsWith("file:")) {
                browser.loadUrl(content);
            } else {
                //Set base URI to the assets/www folder
                String baseURL = webView.getUrl();
                baseURL        = baseURL.substring(0, baseURL.lastIndexOf('/') + 1);

                browser.loadDataWithBaseURL(
                        baseURL, content, "text/html", "UTF-8", null);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            view.loadUrl("javascript:window.ContentHolder.setContent(" +
                    "new XMLSerializer().serializeToString(document));");

            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    for (;;)
                        if (holder.isContentReady()) {
                            onContentReady(holder.getContent());
                            break;
                        }
                }
            });
        }

        /**
         * Called after onPageFinished when the content
         * has been set through the client.
         *
         * @param content
         *      The HTML encoded content
         *      from the web view
         */
        abstract void onContentReady(String content);
    }
}
