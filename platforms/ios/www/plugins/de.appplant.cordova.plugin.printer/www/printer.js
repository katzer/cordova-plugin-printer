cordova.define("de.appplant.cordova.plugin.printer.Printer", function(require, exports, module) { /*
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

var exec = require('cordova/exec');

/**
 * The default document/job name.
 */
exports.DEFAULT_DOC_NAME = 'unknown';

/**
 * Checks if the printer service is avaible (iOS)
 * or if connected to the Internet (Android).
 *
 * @param {Function} callback
 *      A callback function
 * @param {Object?} scope
 *      The scope of the callback (default: window)
 *
 * @return {Boolean}
 */
exports.isAvailable = function (callback, scope) {
    var fn = this._createCallbackFn(callback);

    exec(fn, null, 'Printer', 'isAvailable', []);
};

/**
 * Sends the content to the Google Cloud Print service.
 *
 * @param {String} content
 *      HTML string or DOM node
 *      if latter, innerHTML is used to get the content
 * @param {Object} options
 *       Options for the print job
 * @param {Function?} callback
 *      A callback function
 * @param {Object?} scope
 *      The scope of the callback (default: window)
 */
exports.print = function (content, options, callback, scope) {
    var page   = content.innerHTML || content,
        params = options || {},
        fn     = this._createCallbackFn(callback);

    if (typeof page != 'string') {
        console.log('Print function requires an HTML string. Not an object');
        return;
    }

    if (typeof params == 'string')
        params = { name: params };

    if ([null, undefined, ''].indexOf(params.name) > -1)
        params.name = this.DEFAULT_DOC_NAME;

    exec(fn, null, 'Printer', 'print', [page, params]);
};

/**
 * @private
 *
 * Creates a callback, which will be executed within a specific scope.
 *
 * @param {Function} callbackFn
 *      The callback function
 * @param {Object} scope
 *      The scope for the function
 *
 * @return {Function}
 *      The new callback function
 */
exports._createCallbackFn = function (callbackFn, scope) {
    if (typeof callbackFn != 'function')
        return;

    return function () {
        callbackFn.apply(scope || this, arguments);
    };
};

});
