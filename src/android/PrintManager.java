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
import android.graphics.Bitmap;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.print.PrintHelper;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;

import static android.content.Context.PRINT_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static de.appplant.cordova.plugin.printer.PrintContent.ContentType.UNSUPPORTED;

class PrintManager {

    // The application context
    private final @NonNull Context context;

    // Reference required as long as the page does load the HTML markup
    private @Nullable WebView view;

    /**
     * Constructor
     *
     * @param context The context where to look for.
     */
    PrintManager (@NonNull Context context)
    {
        this.context = context;
    }

    /**
     * If the print framework is able to render the referenced file.
     *
     * @param item Any kind of URL like file://, file:///, res:// or base64://
     *
     * @return true if its able to render the content of the file.
     */
    boolean canPrintItem (@Nullable String item)
    {
        boolean supported = PrintHelper.systemSupportsPrint();

        if (item != null)
        {
            supported = PrintContent.getContentType(item, context) != UNSUPPORTED;
        }

        return supported;
    }

    /**
     * List of all printable document types (utis).
     */
    @NonNull
    static JSONArray getPrintableUTIs()
    {
        JSONArray utis = new JSONArray();

        utis.put("com.adobe.pdf");
        utis.put("com.microsoft.bmp");
        utis.put("public.jpeg");
        utis.put("public.jpeg-2000");
        utis.put("public.png");
        utis.put("public.heif");
        utis.put("com.compuserve.gif");
        utis.put("com.microsoft.ico");

        return utis;
    }

    /**
     * Sends the provided content to the printing controller and opens
     * them.
     *
     * @param content  The content or file to print.
     * @param settings Additional settings how to render the content.
     * @param callback The function to invoke once the job is done.
     */
    @SuppressWarnings("ConstantConditions")
    void print (@Nullable String content, @NonNull JSONObject settings,
                @NonNull WebView view,
                @Nullable PrintHelper.OnPrintFinishCallback callback)
    {
        switch (PrintContent.getContentType(content, context))
        {
            case IMAGE:
                printImage(content, settings, callback);
                break;
            case PDF:
                printPdf(content, settings, callback);
                break;
            case HTML:
                if (content == null || content.isEmpty()) {
                    printWebView(view, settings);
                } else {
                    printText(content, "text/html", settings);
                }
                break;
            case UNSUPPORTED:
                // TODO unsupported content
            case PLAIN:
                printText(content, "text/plain", settings);
        }
    }

    /**
     * Prints the markup content.
     *
     * @param content  The HTML markup to print.
     * @param settings Additional settings how to render the content.
     */
    private void printText (@Nullable String content, @NonNull String mimeType,
                            @NonNull JSONObject settings)
    {
        ((Activity) context).runOnUiThread(() -> {
            view = this.createWebView(settings);

            view.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading (WebView view, String url) {
                    return false;
                }

                @Override
                public void onPageFinished (WebView view, String url) {
                    printWebView(PrintManager.this.view, settings);
                    PrintManager.this.view = null;
                }
            });

            view.loadDataWithBaseURL("file:///android_asset/www/", content, mimeType, "UTF-8",null);
        });
    }

    /**
     * Prints the content of the specified view.
     *
     * @param view     The web view instance to print.
     * @param settings Additional settings how to render the content.
     */
    private void printWebView (@NonNull WebView view,
                               @NonNull JSONObject settings)
    {
        PrintOptions options = new PrintOptions(settings);
        String jobName       = options.getJobName();

        ((Activity) context).runOnUiThread(() -> {
            PrintDocumentAdapter adapter;

            if (SDK_INT >= 21) {
                adapter = view.createPrintDocumentAdapter(jobName);
            } else {
                adapter = view.createPrintDocumentAdapter();
            }

            printAdapter(adapter, options);
        });
    }

    /**
     * Prints the provided PDF document.
     *
     * @param path     The path to the file to print.
     * @param settings Additional settings how to render the content.
     * @param callback The function to invoke once the job is done.
     */
    private void printPdf (@NonNull String path, @NonNull JSONObject settings,
                           @Nullable PrintHelper.OnPrintFinishCallback callback)
    {
        InputStream stream    = PrintContent.open(path, context);

        if (stream == null) return;

        PrintOptions options  = new PrintOptions(settings);
        String jobName        = options.getJobName();
        PrintAdapter adapter  = new PrintAdapter(jobName, stream, callback);

        printAdapter(adapter, options);
    }

    /**
     * Prints the content provided by the print adapter.
     *
     * @param adapter The adapter that holds the content.
     * @param options Additional settings how to render the content.
     */
    private void printAdapter (@NonNull PrintDocumentAdapter adapter,
                               @NonNull PrintOptions options)
    {
        String jobName        = options.getJobName();
        PrintAttributes attrs = options.toPrintAttributes();

        getPrintService().print(jobName, adapter, attrs);
    }

    /**
     * Prints the specified image by file uri.
     *
     * @param path     The path to the file to print.
     * @param settings Additional settings how to render the content.
     * @param callback The function to invoke once the job is done.
     */
    private void printImage (@NonNull String path, @NonNull JSONObject settings,
                             @Nullable PrintHelper.OnPrintFinishCallback callback)
    {
        Bitmap bitmap        = PrintContent.decode(path, context);

        if (bitmap == null) return;

        PrintOptions options = new PrintOptions(settings);
        PrintHelper printer  = new PrintHelper(context);
        String jobName       = options.getJobName();

        options.decoratePrintHelper(printer);

        printer.printBitmap(jobName, bitmap, callback);
    }

    /**
     * Creates a new web view instance that can be used for printing.
     *
     * @param settings Additional settings about the print job.
     *
     * @return A web view instance.
     */
    @NonNull
    private WebView createWebView (@NonNull JSONObject settings)
    {
        boolean jsEnabled = settings.optBoolean("javascript", false);
        WebView      view = new WebView(context);
        WebSettings  spec = view.getSettings();
        JSONObject  style = settings.optJSONObject("style");

        spec.setDatabaseEnabled(true);
        spec.setGeolocationEnabled(true);
        spec.setSaveFormData(true);
        spec.setUseWideViewPort(true);
        spec.setJavaScriptEnabled(jsEnabled);

        if (style != null && style.has("size"))
        {
            spec.setDefaultFixedFontSize(style.optInt("size", 16));
        }

        if (SDK_INT >= 21)
        {
            spec.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(view, true);
        }

        return view;
    }

    /**
     * Returns the print service of the app.
     */
    @NonNull
    private android.print.PrintManager getPrintService()
    {
        return (android.print.PrintManager) context.getSystemService(PRINT_SERVICE);
    }
}
