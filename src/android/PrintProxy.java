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
import android.support.annotation.NonNull;
import android.support.v4.print.PrintHelper;

/**
 * Simple delegate class to have access to the onFinish method.
 */
class PrintProxy extends PrintDocumentAdapter
{
    // Holds the delegate object
    private final @NonNull PrintDocumentAdapter delegate;

    // The callback to inform once the job is done
    private final @NonNull PrintHelper.OnPrintFinishCallback callback;

    /**
     * Constructor
     *
     * @param adapter  The real adapter.
     * @param callback The callback to invoke once the printing is done.
     */
    PrintProxy (@NonNull PrintDocumentAdapter adapter,
                @NonNull PrintHelper.OnPrintFinishCallback callback)
    {
        this.delegate = adapter;
        this.callback = callback;
    }

    @Override
    public void onLayout (PrintAttributes oldAttributes,
                          PrintAttributes newAttributes,
                          CancellationSignal cancellationSignal,
                          LayoutResultCallback callback,
                          Bundle bundle)
    {
        delegate.onLayout(oldAttributes, newAttributes, cancellationSignal, callback, bundle);
    }

    @Override
    public void onWrite (PageRange[] range,
                         ParcelFileDescriptor dest,
                         CancellationSignal cancellationSignal,
                         WriteResultCallback callback)
    {
        delegate.onWrite(range, dest, cancellationSignal, callback);
    }

    /**
     * Invokes the callback.
     */
    @Override
    public void onFinish () {
        super.onFinish();
        callback.onFinish();
    }
}
