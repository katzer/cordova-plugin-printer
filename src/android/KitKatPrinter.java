/*
    Copyright 2013 appPlant UG

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

public class KitKatPrinter extends CordovaPlugin {

    @Override
    public boolean execute (String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // Es soll überprüft werden, ob ein Dienst zum Ausdrucken von Inhalten zur Verfügung steht
        if ("isServiceAvailable".equals(action)) {
            isServiceAvailable(callbackContext);

            return true;
        }

        // Etwas soll ausgedruckt werden
        if ("print".equals(action)) {
            print(args, callbackContext);

            return true;
        }

        // Returning false results in a "MethodNotFound" error.
        return false;
    }

    /**
     * Überprüft, ob ein Drucker zur Verfügung steht.
     */
    private void isServiceAvailable (CallbackContext ctx) {
        Boolean supported   = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;;
        PluginResult result = new PluginResult(PluginResult.Status.OK, supported);

        ctx.sendPluginResult(result);
    }

    /**
     * Druckt den HTML Content aus.
     */
    private void print (final JSONArray args, CallbackContext ctx) {

    }
}
