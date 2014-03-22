/*
    Copyright 2013-2014 appPlant UG

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

var Printer = function () {

};

Printer.prototype = {
    /**
     * Checks if the printer service is avaible (iOS)
     * or if a printing app is installed on the device (Android).
     *
     * @param {Function} callback
     *      A callback function
     * @param {Object?} scope
     *      The scope of the callback (default: window)
     *
     * @return {Boolean}
     */
    isServiceAvailable: function (callback, scope) {
        var callbackFn = function () {
            var args = typeof arguments[0] == 'boolean' ? arguments : arguments[0];

            callback.apply(scope || window, args);
        };

        cordova.exec(callbackFn, null, 'Printer', 'isServiceAvailable', []);
    },

    /**
     * Sends the content to the printer app or service.
     *
     * @param {String} content
     *      HTML string or DOM node
     *      if latter, innerHTML is used to get the contents
     * @param {Object?} options
     *      Platform specific options
     */
    print: function (content, options) {
        var page    = content.innerHTML || content,
            options = options || {};

        if (typeof page != 'string') {
            console.log('Print function requires an HTML string. Not an object');
            return;
        }

        cordova.exec(null, null, 'Printer', 'print', [page, options]);
    }
};

var plugin = new Printer();

module.exports = plugin;