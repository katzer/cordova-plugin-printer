/*
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
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.support.annotation.NonNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT;

/**
 * Document adapter to render and print PDF files.
 */
class PrintPdfAdapter extends PrintDocumentAdapter {

    // The application context
    private @NonNull Context context;

    // The path to the PDF file
    private @NonNull String path;

    /**
     * Constructor
     *
     * @param context The context where to look for.
     */
    PrintPdfAdapter (@NonNull String path, @NonNull Context context)
    {
        this.path    = path;
        this.context = context;
    }

    @Override
    public void onLayout (PrintAttributes oldAttributes,
                          PrintAttributes newAttributes,
                          CancellationSignal cancellationSignal,
                          LayoutResultCallback callback,
                          Bundle bundle)
    {
        PrintDocumentInfo pdi;

        if (cancellationSignal.isCanceled())
            return;

        pdi = new PrintDocumentInfo.Builder("test")
                .setContentType(CONTENT_TYPE_DOCUMENT)
                .build();

        callback.onLayoutFinished(pdi, true);
    }

    @Override
    public void onWrite (PageRange[] range,
                         ParcelFileDescriptor dest,
                         CancellationSignal cancellationSignal,
                         WriteResultCallback callback)
    {
        if (cancellationSignal.isCanceled())
            return;

        AssetUtil io   = new AssetUtil(context);
        InputStream in = io.open(path);

        if (in == null) {
            callback.onWriteFailed("File not found: " + path);
            return;
        }

        OutputStream out = new FileOutputStream(dest.getFileDescriptor());

        try {
            AssetUtil.copy(in, out);
        } catch (IOException e) {
            callback.onWriteFailed(e.getMessage());
            return;
        }

        callback.onWriteFinished(new PageRange[]{ PageRange.ALL_PAGES });
    }
}
