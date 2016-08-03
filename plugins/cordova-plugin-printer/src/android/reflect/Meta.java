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

package de.appplant.cordova.plugin.printer.reflect;

import android.content.Context;
import android.content.res.Resources;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This is an activity for selecting a printer.
 */
public abstract class Meta  {

    /**
     * Tries to find the class for the given name.
     *
     * @param fullName
     *      The full class name including the package scope.
     * @return
     *      The found class or null.
     */
    public static Class<?> getClass (String fullName) {
        try {
            return Class.forName(fullName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Finds the method with given name and set of arguments.
     *
     * @param cls
     *      The class in where to look for the method declaration.
     * @param name
     *      The name of the method.
     * @param params
     *      The arguments of the method.
     * @return
     *      The found method or null.
     */
    public static Method getMethod (Class<?> cls, String name, Class<?>... params) {
        try {
            return cls.getDeclaredMethod(name, params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Invokes the method on the given object with the specified arguments.
     *
     * @param obj
     *      An object which class defines the method.
     * @param method
     *      The method to invoke.
     * @param args
     *      Set of arguments.
     * @return
     *      The returned object or null.
     */
    public static Object invokeMethod (Object obj, Method method, Object... args) {
        try {
            return method.invoke(obj, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Invokes the method on the given object.
     *
     * @param obj
     *      An object which class defines the method.
     * @param methodName
     *      The name of method to invoke.
     * @return
     *      The returned object or null.
     */
    public static Object invokeMethod (Object obj, String methodName) {
        Method method = getMethod(obj.getClass(), methodName);

        if (method == null)
            return null;

        try {
            return method.invoke(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Return a resource identifier for the given resource name.
     *
     * @param context The applications context.
     * @param type Resource type to find (id or layout or string ...)
     * @param name The name of the desired resource.
     *
     * @return The associated resource identifier or 0 if not found.
     */
    public static int getResId (Context context, String type, String name) {
        Resources res   = context.getResources();
        String pkgName  = context.getPackageName();

        int resId;
        resId = res.getIdentifier(name, type, pkgName);

        return resId;
    }
}
