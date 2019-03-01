/*
 Copyright 2013 Sebastián Katzer

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

import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.support.annotation.NonNull;
import android.support.v4.print.PrintHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT;

/**
 * Document adapter to render and print PDF files.
 */
class PrintAdapter extends PrintDocumentAdapter
{
    // The name of the print job
    private final @NonNull String jobName;

    // Max page count
    private final int pageCount;

    // The input stream to render
    private final @NonNull InputStream input;

    // The callback to inform once the job is done
    private final @NonNull PrintHelper.OnPrintFinishCallback callback;

    /**
     * Constructor
     *
     * @param jobName   The name of the print job.
     * @param pageCount The max page count.
     * @param input     The input stream to render.
     * @param callback  The callback to inform once the job is done.
     */
    PrintAdapter (@NonNull String jobName, int pageCount,
                  @NonNull InputStream input,
                  @NonNull PrintHelper.OnPrintFinishCallback callback)
    {
        this.jobName   = jobName;
        this.pageCount = pageCount;
        this.input     = input;
        this.callback  = callback;
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

        pdi = new PrintDocumentInfo.Builder(jobName)
                .setContentType(CONTENT_TYPE_DOCUMENT)
                .setPageCount(pageCount)
                .build();

        boolean changed = !newAttributes.equals(oldAttributes);

        callback.onLayoutFinished(pdi, changed);
    }

    @Override
    public void onWrite (PageRange[] range,
                         ParcelFileDescriptor dest,
                         CancellationSignal cancellationSignal,
                         WriteResultCallback callback)
    {
        if (cancellationSignal.isCanceled())
            return;

        OutputStream output = new FileOutputStream(dest.getFileDescriptor());

        try {
            PrintIO.copy(input, output);
        } catch (IOException e) {
            callback.onWriteFailed(e.getMessage());
            return;
        }

        callback.onWriteFinished(new PageRange[]{ PageRange.ALL_PAGES });
    }

    /**
     * Closes the input stream and invokes the callback.
     */
    @Override
    public void onFinish ()
    {
        super.onFinish();

        PrintIO.close(input);

        callback.onFinish();
    }
}
