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

package de.appplant.cordova.plugin.printer.ext;

import android.content.Context;
import android.print.PrintJob;
import android.print.PrintJobId;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.appplant.cordova.plugin.printer.reflect.Meta;

public final class PrintManager {

    public interface OnPrintJobStateChangeListener {
        /**
         * Callback notifying that a print job state changed.
         *
         * @param job The print job.
         */
        void onPrintJobStateChanged(PrintJob job);
    }

    /**
     * Required to be able to register a listener of hidden interface.
     */
    private class OnPrintJobStateChangeProxy implements InvocationHandler {
        @Override
        public Object invoke (Object o, Method method, Object[] objects)
                throws Throwable {

            if (method.getName().equals("hashCode")) {
                return listener.get().hashCode();
            }

            if (method.getName().equals("onPrintJobStateChanged")) {
                notifyOnPrintJobStateChanged((PrintJobId) objects[0]);
                return null;
            }

            throw new Exception();
        }
    }

    /**
     * The application context.
     */
    private WeakReference<Context> ctx;

    /**
     * The registered listener for the state change event.
     */
    private WeakReference<OnPrintJobStateChangeListener> listener;

    /**
     * The proxy wrapper of the listener.
     */
    private Object proxy;

    /**
     * Constructor
     *
     * @param context The context where to look for.
     */
    public PrintManager (Context context) {
        this.ctx = new WeakReference<Context>(context);
    }

    /**
     * Get an instance from PrintManager service.
     *
     * @return A PrintManager instance.
     */
    public final android.print.PrintManager getInstance () {
        return (android.print.PrintManager)
                ctx.get().getSystemService(Context.PRINT_SERVICE);
    }

    /**
     * Gets the list of installed print services.
     *
     * @return The found service list or an empty list.
     */
    public final List<PrintServiceInfo> getInstalledPrintServices () {
        List printers = (List) Meta.invokeMethod(getInstance(),
                "getInstalledPrintServices");

        ArrayList<PrintServiceInfo> services =
                new ArrayList<PrintServiceInfo>();

        if (printers == null)
            return Collections.emptyList();

        for (Object printer : printers) {
            services.add(new PrintServiceInfo(printer));
        }

        return services;
    }

    /**
     * Gets the list of enabled print services.
     *
     * @return The found service list or an empty list.
     */
    public final List<PrintServiceInfo> getEnabledPrintServices () {
        List printers = (List) Meta.invokeMethod(getInstance(),
                "getEnabledPrintServices");

        ArrayList<PrintServiceInfo> services =
                new ArrayList<PrintServiceInfo>();

        if (printers == null)
            return Collections.emptyList();

        for (Object printer : printers) {
            services.add(new PrintServiceInfo(printer));
        }

        return services;
    }

    /**
     * Creates an session object to discover all printer services. To do so
     * you need to register a listener object and start the discovery process.
     *
     * @return An instance of class PrinterDiscoverySession.
     */
    public final PrinterDiscoverySession createPrinterDiscoverySession () {
        Object session = Meta.invokeMethod(getInstance(),
                "createPrinterDiscoverySession");

        return new PrinterDiscoverySession(session);
    }

    /**
     * Adds a listener for observing the state of print jobs.
     *
     * @param listener The listener to add.
     */
    public void setOnPrintJobStateChangeListener(
            OnPrintJobStateChangeListener listener) {

        if (this.listener == listener)
            return;

        if (listener == null) {
            unsetOnPrintJobStateChangeListener();
            return;
        }

        Class<?> interfaceCls = Meta.getClass(
                "android.print.PrintManager$PrintJobStateChangeListener");

        if (interfaceCls == null)
            return;

        this.listener =
                new WeakReference<OnPrintJobStateChangeListener>(listener);

        Method method = Meta.getMethod(getInstance().getClass(),
                "addPrintJobStateChangeListener", interfaceCls);

        Class<?>[] interfaces = {interfaceCls};

        proxy = Proxy.newProxyInstance(
                interfaceCls.getClassLoader(),
                interfaces,
                new OnPrintJobStateChangeProxy()
        );

        Meta.invokeMethod(getInstance(), method, proxy);
    }

    /**
     * Removes the listener from the observing the state of print jobs.
     */
    public void unsetOnPrintJobStateChangeListener() {
        Class<?> interfaceCls = Meta.getClass(
                "android.print.PrintManager$PrintJobStateChangeListener");

        if (interfaceCls == null || proxy == null)
            return;

        Method method = Meta.getMethod(getInstance().getClass(),
                "removePrintJobStateChangeListener", interfaceCls);

        Meta.invokeMethod(getInstance(), method, proxy);

        proxy    = null;
        listener = null;
    }

    /**
     * Callback notifying that a print job state changed.
     *
     * @param printJobId The print job id.
     */
    private void notifyOnPrintJobStateChanged(PrintJobId printJobId) {
        if (listener != null && listener.get() != null) {
            Method method = Meta.getMethod(getInstance().getClass(),
                    "getPrintJob", PrintJobId.class);

            PrintJob job  = (PrintJob) Meta.invokeMethod(getInstance(),
                    method, printJobId);

            listener.get().onPrintJobStateChanged(job);
        }
    }

}