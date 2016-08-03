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

import android.content.pm.ResolveInfo;

import de.appplant.cordova.plugin.printer.reflect.Meta;

public final class PrintServiceInfo {

    /**
     * The wrapped object of type android.printservice.PrintServiceInfo
     */
    private Object obj;

    /**
     * Wraps the object of the hidden class
     * android.printservice.PrintServiceInfo.
     *
     * @param wrappedObj The object to wrap.
     */
    PrintServiceInfo (Object wrappedObj) {
        obj = wrappedObj;
    }

    /**
     * The accessibility service id.
     *
     * @return @return The id.
     */
    public final String getId() {
        return (String) Meta.invokeMethod(obj, "getId");
    }

    /**
     * The add printers activity name.
     *
     * @return The add printers activity name.
     */
    public final String getAddPrintersActivityName() {
        return (String) Meta.invokeMethod(obj, "getAddPrintersActivityName");
    }
    /**
     * The service {@link ResolveInfo}.
     *
     * @return The info.
     */
    public final ResolveInfo getResolveInfo() {
        return (ResolveInfo) Meta.invokeMethod(obj, "getResolveInfo");
    }

}