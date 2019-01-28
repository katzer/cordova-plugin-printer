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

import android.content.Context;
import android.graphics.Bitmap;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.print.PrintHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import static android.content.Context.PRINT_SERVICE;

class PrintManager {

    // The application context
    private @NonNull Context context;

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
            supported = new AssetUtil(context).open(item) != null;
        }

        return supported;
    }

    /**
     * @return List of all printable document types (utis).
     */
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
        utis.put("com.microsoft.bmp");
        utis.put("com.microsoft.bmp");

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
    void print (@Nullable String content, JSONObject settings,
                @Nullable PrintHelper.OnPrintFinishCallback callback)
    {
        switch (AssetUtil.getContentType(content))
        {
            case IMAGE:
                printImage(content, settings, callback);
                break;
            case PDF:
                printPdf(content, settings, callback);
                break;
            case HTML:
            case SELF:
            case PLAIN:
                // TODO
        }
    }

    /**
     * Prints the provided PDF document.
     *
     * @param path     The path to the file to print.
     * @param settings Additional settings how to render the content.
     * @param callback The function to invoke once the job is done.
     */
    private void printPdf (String path, JSONObject settings,
                           @Nullable PrintHelper.OnPrintFinishCallback callback)
    {
        Options options              = new Options(settings);
        String jobName               = options.getJobName();
        PrintDocumentAdapter adapter = new PdfAdapter(path, context);
        PrintAttributes attributes   = options.toPrintAttributes();

        getPrintService().print(jobName, adapter, attributes);
    }

    /**
     * Prints the specified image by file uri.
     *
     * @param path     The path to the file to print.
     * @param settings Additional settings how to render the content.
     * @param callback The function to invoke once the job is done.
     */
    private void printImage (String path, JSONObject settings,
                             @Nullable PrintHelper.OnPrintFinishCallback callback)
    {
        AssetUtil decoder   = new AssetUtil(context);
        Bitmap bitmap       = decoder.decode(path);

        if (bitmap == null) return;

        Options options     = new Options(settings);
        PrintHelper printer = new PrintHelper(context);
        String jobName      = options.getJobName();

        options.decoratePrintHelper(printer);

        printer.printBitmap(jobName, bitmap, callback);
    }

    /**
     * Returns the print service of the app.
     */
    private @NonNull android.print.PrintManager getPrintService()
    {
        return (android.print.PrintManager) context.getSystemService(PRINT_SERVICE);
    }
}
