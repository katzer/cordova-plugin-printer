/**
 *  Printer.m
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
     * @return {Boolean}
     */
    isServiceAvailable: function (callback) {
        cordova.exec(callback, null, 'Printer', 'isServiceAvailable', []);
    },

    /**
     * Übergibt den HTML-Content an den Drucker-Dienst.
     *
     * @param {String} content html string or DOM node (if latter, innerHTML is used to get the contents)
     * @param {Function?} success callback function called if print successful. {success: true}
     * @param {Function?} failure callback function called if print unsuccessful. If print fails, {error: reason}. If printing not available: {available: false}
     */
    print: function (content, success, failure) {
        content = content.innerHTML || content;

        if (typeof content != 'string') {
            console.log('Print function requires an HTML string. Not an object');
            return;
        }

        cordova.exec(success, failure, 'Printer', 'print', [content]);
    }
};

var printer = new Printer();

module.exports = printer;