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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.WebView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Plugin to print HTML documents. Therefore it creates an invisible web view
 * that loads the markup data. Once the page has been fully rendered it takes
 * the print adapter of that web view and initializes a print job.
 */
public final class Printer extends CordovaPlugin
{
    /**
     * Executes the request.
     *
     * This method is called from the WebView thread.
     * To do a non-trivial amount of work, use:
     *     cordova.getThreadPool().execute(runnable);
     *
     * To run on the UI thread, use:
     *     cordova.getActivity().runOnUiThread(runnable);
     *
     * @param action   The action to execute.
     * @param args     The exec() arguments in JSON form.
     * @param callback The callback context used when calling back into JavaScript.
     *
     * @return         Whether the action was valid.
     */
    @Override
    public boolean execute (String action, JSONArray args,
                            CallbackContext callback)
    {
        boolean valid = true;

        if (action.equalsIgnoreCase("check"))
        {
            check(args.optString(0), callback);
        }
        else if (action.equalsIgnoreCase("types"))
        {
            types(callback);
        }
        else if (action.equalsIgnoreCase("print"))
        {
            print(args.optString(0), args.optJSONObject(1), callback);
        }
        else {
            valid = false;
        }

        return valid;
    }

    /**
     * If the print framework is able to render the referenced file.
     *
     * @param item     Any kind of URL like file://, file:///, res:// or base64://
     * @param callback The plugin function to invoke with the result.
     */
    private void check (@Nullable String item, CallbackContext callback)
    {
        cordova.getThreadPool().execute(() -> {
            PrintManager pm   = new PrintManager(cordova.getContext());
            boolean printable = pm.canPrintItem(item);

            sendPluginResult(callback, printable);
        });
    }

    /**
     * List of all printable document types (utis).
     *
     * @param callback The plugin function to invoke with the result.
     */
    private void types (CallbackContext callback)
    {
        cordova.getThreadPool().execute(() -> {
            JSONArray utis = PrintManager.getPrintableTypes();

            PluginResult res = new PluginResult(
                    Status.OK, utis);

            callback.sendPluginResult(res);
        });
    }

    /**
     * Sends the provided content to the printing controller and opens
     * them.
     *
     * @param content  The content or file to print.
     * @param settings Additional settings how to render the content.
     * @param callback The plugin function to invoke with the result.
     */
    private void print (@Nullable String content, JSONObject settings,
                        CallbackContext callback)
    {
        cordova.getThreadPool().execute(() -> {
            PrintManager pm = new PrintManager(cordova.getContext());
            WebView view    = (WebView) webView.getView();

            pm.print(content, settings, view, (boolean completed) -> sendPluginResult(callback, completed));
        });
    }

    /**
     * Sends the result back to the client.
     *
     * @param callback The callback to invoke.
     * @param value    The argument to pass with.
     */
    private void sendPluginResult (@NonNull CallbackContext callback,
                                   boolean value)
    {
        PluginResult result = new PluginResult(Status.OK, value);

        callback.sendPluginResult(result);
    }
}
