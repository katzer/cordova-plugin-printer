/**
 *  printer.js
 *  Cordova Printer Plugin
 *
 *  Created by Sebastian Katzer (github.com/katzer) on 10/08/2013.
 *  Copyright 2013 Sebastian Katzer. All rights reserved.
 *  GPL v2 licensed
 */

var Printer = function () {

};

Printer.prototype = {
    /**
     * Überprüft, ob der Drucker-Dienst verfügbar ist.
     *
     * @param {Function} callback
     * @param {Object?}  scope    callback scope (default: window)
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
     * Übergibt den HTML-Content an den Drucker-Dienst.
     *
     * @param {String}    content  HTML string or DOM node (if latter, innerHTML is used to get the contents)
     * @param {Function?} callback callback function called if print is completed. {success: bool, available: bool, error: reason}
     * @param {Object?}   scope    callback scope (default: window)
     * @param {Object?}   options  platform specific options
     */
    print: function (content, callback, scope, options) {
        var page    = content.innerHTML || content,
            options = options || {},
            callbackFn;

        if (typeof page != 'string') {
            console.log('Print function requires an HTML string. Not an object');
            return;
        }

        if (typeof callback == 'function'){
            callbackFn = function () {
                callback.apply(scope || window, arguments);
            }
        }

        cordova.exec(callbackFn, null, 'Printer', 'print', [page, options]);
    }
};

var plugin = new Printer();

module.exports = plugin;