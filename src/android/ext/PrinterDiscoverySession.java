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

import android.print.PrinterInfo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

import de.appplant.cordova.plugin.printer.reflect.Meta;

public final class PrinterDiscoverySession {

    /**
     * Required interface of the listener.
     */
    public interface OnPrintersChangeListener {
        void onPrintersChanged(List<PrinterInfo> printerInfos);
    }

    /**
     * Required to be able to register a listener of the expected type.
     */
    private class OnPrintersChangeProxy implements InvocationHandler {
        @Override
        public Object invoke (Object o, Method method, Object[] objects)
                throws Throwable {

            if (method.getName().equals("onPrintersChanged")) {
                notifyOnPrintersChanged();
                return null;
            } else throw new Exception();
        }
    }

    /**
     * The wrapped session of type
     * android.print.PrinterDiscoverySession
     */
    private Object session;

    /**
     * Registered listener object.
     */
    private OnPrintersChangeListener listener = null;

    /**
     * Constructor
     *
     * @param session An instance of type
     *                android.print.PrinterDiscoverySession
     */
    public PrinterDiscoverySession (Object session) {
        this.session = session;
    }

    /**
     * Start discovering available printers.
     */
    public final void startPrinterDiscovery () {
        Method method = Meta.getMethod(session.getClass(),
                "startPrinterDiscovery", List.class);

        Meta.invokeMethod(session, method, Collections.emptyList());
    }

    /**
     * Stop discovering printers.
     */
    public final void stopPrinterDiscovery() {
        Meta.invokeMethod(session, "stopPrinterDiscovery");
    }

    /**
     * If session is discovering printers.
     */
    @SuppressWarnings("ConstantConditions")
    public final boolean isPrinterDiscoveryStarted() {
        return (Boolean) Meta.invokeMethod(session,
                "isPrinterDiscoveryStarted");
    }

    /**
     * Register the listener.
     *
     * @param listener Use null to unregister the listener.
     */
    public final void setOnPrintersChangeListener(
            OnPrintersChangeListener listener) {

        Object proxy          = null;
        Class<?> interfaceCls = Meta.getClass(
                "android.print.PrinterDiscoverySession$OnPrintersChangeListener");

        if (interfaceCls == null)
            return;

        this.listener = listener;

        Method method = Meta.getMethod(session.getClass(),
                "setOnPrintersChangeListener", interfaceCls);

        if (listener != null) {
            Class<?>[] interfaces = {interfaceCls};

            proxy = Proxy.newProxyInstance(
                    interfaceCls.getClassLoader(),
                    interfaces,
                    new OnPrintersChangeProxy()
            );
        }

        Meta.invokeMethod(session, method, proxy);
    }

    /**
     * Destroy the session if not already done.
     */
    public final void destroy() {
        stopPrinterDiscovery();
        setOnPrintersChangeListener(null);
        Meta.invokeMethod(session, "destroy");
        session = null;
    }

    /**
     * Get a list of all yet discovered printers.
     *
     * @return List of their basic infos
     */
    @SuppressWarnings("unchecked")
    public final List<PrinterInfo> getPrinters() {
        Method method = Meta.getMethod(session.getClass(), "getPrinters");

        return (List<PrinterInfo>) Meta.invokeMethod(session, method);
    }

    /**
     * Notifies the listener about the occurred event.
     */
    private void notifyOnPrintersChanged() {
        if (listener != null) {
            listener.onPrintersChanged(getPrinters());
        }
    }
}