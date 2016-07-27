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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.appplant.cordova.plugin.printer.reflect.Meta;

public final class PrintManager {

    /**
     * The application context.
     */
    private Context ctx;

    /**
     * Constructor
     *
     * @param context The context where to look for.
     */
    public PrintManager (Context context) {
        this.ctx = context;
    }

    /**
     * Get an instance from PrintManager service.
     *
     * @return A PrintManager instance.
     */
    public final android.print.PrintManager getInstance () {
        return (android.print.PrintManager)
                ctx.getSystemService(Context.PRINT_SERVICE);
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

}