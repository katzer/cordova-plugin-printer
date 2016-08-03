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

var exec = require('cordova/exec');

/**
 * List of all available options with their default value.
 *
 * @return {Object}
 */
exports.getDefaults = function () {
    return {
        // Platform independent
        name:      'unknown',
        duplex:    'none',
        landscape: false,
        graystyle: false,
        // iOS specific
        border:    true,
        hidePageRange:      false,
        hideNumberOfCopies: false,
        hidePaperFormat:    false,
        // iPad specific
        bounds:    [40, 30, 0, 0]
    };
};

/**
 * Checks if the printer service is avaible (iOS)
 * or if services are available (Android).
 *
 * @param {Function} callback
 *      A callback function
 * @param {Object?} scope
 *      The scope of the callback (default: window)
 *
 * @return {Boolean}
 */
exports.check = function (callback, scope) {
    var fn = this._createCallbackFn(callback);

    exec(fn, null, 'Printer', 'check', []);
};

/**
 * @deprecated API call. Use `check` instead!
 */
exports.isAvailable = function () {
    exports.check.apply(exports, arguments);
};

/**
 * Displays system interface for selecting a printer.
 *
 * @param {Function} callback
 *      A callback function
 * @param {Object} options
 *       Options for the printer picker
 */
exports.pick = function (callback, options) {
    var fn     = this._createCallbackFn(callback);
    var params = options || {};

    params = this.mergeWithDefaults(params);

    exec(fn, null, 'Printer', 'pick', [params]);
};

/**
 * Sends the content to the Printing Framework.
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

    if (typeof params == 'string') {
        params = { name: params };
    }

    params = this.mergeWithDefaults(params);

    if ([null, undefined, ''].indexOf(params.name) > -1) {
        params.name = this.getDefaults().name;
    }

    exec(fn, null, 'Printer', 'print', [page, params]);
};

/**
 * @private
 *
 * Merge settings with default values.
 *
 * @param {Object} options
 *      The custom options
 *
 * @retrun {Object}
 *      Default values merged
 *      with custom values
 */
exports.mergeWithDefaults = function (options) {
    var defaults = this.getDefaults();

    if (options.bounds && !options.bounds.length) {
        options.bounds = [
            options.bounds.left   || defaults.bounds[0],
            options.bounds.top    || defaults.bounds[1],
            options.bounds.width  || defaults.bounds[2],
            options.bounds.height || defaults.bounds[3],
        ];
    }

    if (options.duplex && typeof options.duplex == 'boolean') {
        options.duplex = options.duplex ? 'long' : 'none';
    }

    for (var key in defaults) {
        if (!options.hasOwnProperty(key)) {
            options[key] = defaults[key];
            continue;
        }

        if (typeof options[key] != typeof defaults[key]) {
            delete options[key];
        }
    }

    return options;
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
