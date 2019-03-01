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

var exec  = require('cordova/exec'),
    ua    = navigator.userAgent.toLowerCase(),
    isIOS = ua.indexOf('ipad') > -1 || ua.indexOf('iphone') > -1;

// Defaults
exports._defaults = {
    // name:       'unknown',

    // duplex: 'none',
    // orientation: 'landscape',
    // monochrome: false,
    // photo: false,

    // copies: 1,
    // pageCount: 1,

    // maxHeight: '10cm',
    // maxWidth: '10cm',

    // font: {
    //     name: 'Helvetica',
    //     align: 'left',
    //     italic: false,
    //     bold: false,
    //     color: '#FF0000'
    // },

    // margin: {
    //     top: 0,
    //     left: 0,
    //     bottom: 0,
    //     right: 0
    // },

    // ui: {
    //     hideNumberOfCopies: false,
    //     hidePaperFormat:    false,
    //     top: 30,
    //     left: 40,
    //     height: 0,
    //     width: 0
    // },

    // paper: {
    //     height: 0,
    //     width: 0,
    //     length: 0,
    //     name: 'A4'
    // },

    // header: {
    //     height: '1cm',

    //     labels: [{
    //         text: 'Awesome Printer Plug-in',
    //         font: {
    //             align: 'center',
    //             italic: true,
    //             color: '#FF0000'
    //         }
    //     },{
    //         showPageIndex: true,
    //         font: {
    //             align: 'right',
    //             bold: true
    //         }
    //     }]
    // },

    // footer: {
    //     height: '3mm',

    //     label: {
    //         text: 'Copyright (c) 2013-2019 Sebastián Katzer',
    //         font: { size: 9 },
    //         top: '1.5mm',
    //         right: '5mm'
    //     }
    // }
};

/**
 * Test if the printer service is able to print
 * the ressource specified by URL.
 *
 * @param [ String ]   uri      A file URI.
 * @param [ Function ] callback The callback function.
 * @param [ Object ]   scope    The scope for the function.
 *
 * @return [ Void ]
 */
exports.canPrintItem = function (uri, callback, scope)
{
    if (typeof uri == 'function')
    {
        scope    = callback;
        callback = uri;
        uri      = null;
    }

    var fn = this._createCallbackFn(callback, scope);

    exec(fn, null, 'Printer', 'check', [uri]);
};

/**
 * Returns a list of all printable document types.
 *
 * @param [ Function ] callback The callback function.
 * @param [ Object ]   scope    The scope for the function.
 *
 * @return [ Void ]
 */
exports.getPrintableTypes = function (callback, scope)
{
    var fn = this._createCallbackFn(callback, scope);

    exec(fn, null, 'Printer', 'types', []);
};

/**
 * Displays system interface for selecting a printer.
 *
 * @param [ Object ]   options  Optional ui options.
 * @param [ Function ] callback The callback function.
 * @param [ Object ]   scope    The scope for the function.
 *
 * @return [ Void ]
 */
exports.pick = function (options, callback, scope)
{
    if (typeof options == 'function')
    {
        scope    = callback;
        callback = options;
        options  = {};
    }

    var fn     = this._createCallbackFn(callback, scope),
        params = this._mergeWithDefaults({ ui: options });

    if (isIOS)
    {
        exec(fn, null, 'Printer', 'pick', [params]);
    }
    else if (fn)
    {
        fn(null);
    }
};

/**
 * Sends the content to the printer.
 *
 * @param [ String ]   content  The plain/html text or a file URI.
 * @param [ Object ]   options  Options for the print job.
 * @param [ Function ] callback The callback function.
 * @param [ Object ]   scope    The scope for the function.
 */
exports.print = function (content, options, callback, scope)
{
    if (typeof content == 'function')
    {
        scope    = options;
        callback = content;
        options  = {};
        content  = null;
    }

    if (typeof options == 'function')
    {
        scope    = callback;
        callback = options;
        options  = typeof content != 'string' ? content : {};
        content  = typeof content == 'string' ? content : null;
    }

    var fn     = this._createCallbackFn(callback, scope),
        params = this._mergeWithDefaults(options || {});

    exec(fn, null, 'Printer', 'print', [content || '', params]);
};

/**
 * The (platform specific) default settings.
 *
 * @return [ Object ]
 */
exports.getDefaults = function ()
{
    var map = Object.assign({}, this._defaults);

    for (var key in map)
    {
        if (Array.isArray(map[key]))
        {
            map[key] = Array.from(map[key]);
        }
        else if (Object.prototype.isPrototypeOf(map[key]))
        {
            map[key] = Object.assign({}, map[key]);
        }
    }

    return map;
};

/**
 * Overwrite default settings.
 *
 * @param [ Object ] newDefaults New default values.
 *
 * @return [ Void ]
 */
exports.setDefaults = function (newDefaults)
{
    Object.assign(this._defaults, newDefaults);
};

/**
 * Merge custom properties with the default values.
 *
 * @param [ Object ] options Set of custom values.
 *
 * @retrun [ Object ]
 */
exports._mergeWithDefaults = function (options)
{
    var defaults = this.getDefaults();

    if (options.duplex && typeof options.duplex == 'boolean')
    {
        options.duplex = options.duplex ? 'long' : 'none';
    }

    Object.assign(defaults, options);

    for (var key in defaults)
    {
        if (defaults[key] !== null)
        {
            options[key] = defaults[key];
        }
        else
        {
            delete options[key];
        }
    }

    return options;
};

/**
 * @private
 *
 * Creates a callback, which will be executed
 * within a specific scope.
 *
 * @param [ Function ] callback The callback function.
 * @param [ Object ]   scope    The scope for the function.
 *
 * @return [ Function ] The new callback function
 */
exports._createCallbackFn = function (callback, scope)
{
    if (typeof callback !== 'function')
        return;

    return function () {
        callback.apply(scope || this, arguments);
    };
};

// Polyfill for Object.assign
if (typeof Object.assign != 'function') {
  Object.assign = function(target) {
    'use strict';
    if (target == null) {
      throw new TypeError('Cannot convert undefined or null to object');
    }

    target = Object(target);
    for (var index = 1; index < arguments.length; index++) {
      var source = arguments[index];
      if (source != null) {
        for (var key in source) {
          if (Object.prototype.hasOwnProperty.call(source, key)) {
            target[key] = source[key];
          }
        }
      }
    }
    return target;
  };
}

// Polyfill for Array.from
// Production steps of ECMA-262, Edition 6, 22.1.2.1
// Reference: https://people.mozilla.org/~jorendorff/es6-draft.html#sec-array.from
if (!Array.from) {
  Array.from = (function () {
    var toStr = Object.prototype.toString;
    var isCallable = function (fn) {
      return typeof fn === 'function' || toStr.call(fn) === '[object Function]';
    };
    var toInteger = function (value) {
      var number = Number(value);
      if (isNaN(number)) { return 0; }
      if (number === 0 || !isFinite(number)) { return number; }
      return (number > 0 ? 1 : -1) * Math.floor(Math.abs(number));
    };
    var maxSafeInteger = Math.pow(2, 53) - 1;
    var toLength = function (value) {
      var len = toInteger(value);
      return Math.min(Math.max(len, 0), maxSafeInteger);
    };

    // The length property of the from method is 1.
    return function from(arrayLike/*, mapFn, thisArg */) {
      // 1. Let C be the this value.
      var C = this;

      // 2. Let items be ToObject(arrayLike).
      var items = Object(arrayLike);

      // 3. ReturnIfAbrupt(items).
      if (arrayLike == null) {
        throw new TypeError("Array.from requires an array-like object - not null or undefined");
      }

      // 4. If mapfn is undefined, then let mapping be false.
      var mapFn = arguments.length > 1 ? arguments[1] : void undefined;
      var T;
      if (typeof mapFn !== 'undefined') {
        // 5. else
        // 5. a If IsCallable(mapfn) is false, throw a TypeError exception.
        if (!isCallable(mapFn)) {
          throw new TypeError('Array.from: when provided, the second argument must be a function');
        }

        // 5. b. If thisArg was supplied, let T be thisArg; else let T be undefined.
        if (arguments.length > 2) {
          T = arguments[2];
        }
      }

      // 10. Let lenValue be Get(items, "length").
      // 11. Let len be ToLength(lenValue).
      var len = toLength(items.length);

      // 13. If IsConstructor(C) is true, then
      // 13. a. Let A be the result of calling the [[Construct]] internal method of C with an argument list containing the single item len.
      // 14. a. Else, Let A be ArrayCreate(len).
      var A = isCallable(C) ? Object(new C(len)) : new Array(len);

      // 16. Let k be 0.
      var k = 0;
      // 17. Repeat, while k < len… (also steps a - h)
      var kValue;
      while (k < len) {
        kValue = items[k];
        if (mapFn) {
          A[k] = typeof T === 'undefined' ? mapFn(kValue, k) : mapFn.call(T, kValue, k);
        } else {
          A[k] = kValue;
        }
        k += 1;
      }
      // 18. Let putStatus be Put(A, "length", len, true).
      A.length = len;
      // 20. Return A.
      return A;
    };
  }());
}
