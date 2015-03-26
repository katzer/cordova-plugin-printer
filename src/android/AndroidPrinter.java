/*
 * Copyright (c) 2013-2015 by appPlant UG. All rights reserved.
 *
 * @APPPLANT_LICENSE_HEADER_START@
 *
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apache License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://opensource.org/licenses/Apache-2.0/ and read it before using this
 * file.
 *
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 *
 * @APPPLANT_LICENSE_HEADER_END@
 */

package de.appplant.cordova.plugin.printer;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.cordova.CallbackContext;


public class AndroidPrinter {
    private WebView view;

    private CallbackContext command;

    private Printer printer;

    /**
     * Constructor of AndroidPrinter
     */
    AndroidPrinter(CallbackContext command,Printer printer) {
        this.command = command;
        this.printer = printer;
    }

    /**
     * Loads the HTML content into the web view and invokes the print manager.
     *
     * @param args
     *      The exec arguments as JSON
     */
    void printAndroid (final JSONArray args) {
        final String content   = args.optString(0, "<html></html>");
        final JSONObject props = args.optJSONObject(1);

        printer.cordova.getActivity().runOnUiThread(new Runnable() {
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
            String baseURL = printer.webView.getUrl();
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
        Activity ctx = printer.cordova.getActivity();
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
        final String docName = props.optString("name", Printer.DEFAULT_DOC_NAME);
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
                    @SuppressWarnings("all")
                    Object printManager = printer.cordova.getActivity().getSystemService(PRINT_SERVICE);

                    // Get a print adapter instance
                    Class<?> printDocumentAdapterClass = Class.forName("android.print.PrintDocumentAdapter");
                    Method createPrintDocumentAdapterMethod = webView.getClass().getMethod("createPrintDocumentAdapter");
                    Object printAdapter = createPrintDocumentAdapterMethod.invoke(webView);

                    // Get a print builder instance
                    Class<?> printAttributesBuilderClass = Class.forName("android.print.PrintAttributes$Builder");
                    Constructor<?> ctor = printAttributesBuilderClass.getConstructor();
                    Object printAttributes = ctor.newInstance();


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
        printer.cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    try {
                        Method isCancelled = job.getClass().getMethod("isCancelled");
                        Method isCompleted = job.getClass().getMethod("isCompleted");
                        Method isFailed = job.getClass().getMethod("isFailed");

                        Boolean cancelled = (Boolean) isCancelled.invoke(job);
                        Boolean completed = (Boolean) isCompleted.invoke(job);
                        Boolean failed = (Boolean) isFailed.invoke(job);

                        if (cancelled || completed || failed) {
                            command.success();
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
