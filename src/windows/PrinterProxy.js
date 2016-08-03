/* globals Windows: true */

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

var Printing     = Windows.Graphics.Printing,
    PrintManager = Windows.Graphics.Printing.PrintManager;

/**
 * Verifies if printing is supported on the device.
 *
 * @param {Function} success
 *      Success callback function
 * @param {Function} error
 *      Error callback function
 * @param {Array} args
 *      Interface arguments
 */
exports.check = function (success, fail, args) {
    success(MSApp.hasOwnProperty('getHtmlPrintDocumentSourceAsync'), 0);
};

/**
 * Displays system interface for selecting a printer.
 *
 * @param {Function} success
 *      Success callback function
 * @param {Function} error
 *      Error callback function
 * @param {Array} args
 *      Interface arguments
 */
exports.pick = function (success, fail, args) {
    success(); // Not supported :(
};

/**
 * Sends the content to the Printing Framework.
 *
 * @param {Function} success
 *      Success callback function
 * @param {Function} error
 *      Error callback function
 * @param {Array} args
 *      Interface arguments
 */
exports.print = function (success, fail, args) {
    var page    = document.createDocumentFragment(),
        content = document.createElement('html');

    content.innerHTML = args[0];
    page.appendChild(content);

    exports._func = success;
    exports._args = args[1];

    MSApp.getHtmlPrintDocumentSourceAsync(page).then(function (source) {
        exports._page = source;
        PrintManager.showPrintUIAsync();
    });
};

/**
 * Raised when a request to print has occurred.
 * Create, configure and schedule the print task.
 *
 * @param {PrintTaskRequestedEventArgs} event
 *      Event arguments associated with the request.
 */
exports.onPrintTaskRequested = function (event) {
    var config = exports._args,
        task;

    task = event.request.createPrintTask(config.name, function (args) {
        args.setSource(exports._page);
    });

    if (config.graystyle) {
        task.options.colorMode = Printing.PrintColorMode.grayscale;
    } else {
        task.options.colorMode = Printing.PrintColorMode.color;
    }

    if (config.landscape) {
        task.options.orientation = Printing.PrintOrientation.landscape;
    } else {
        task.options.orientation = Printing.PrintOrientation.portrait;
    }

    if (config.duplex == 'long') {
        task.options.duplex = Printing.PrintDuplex.twoSidedLongEdge;
    } else
    if (config.duplex == 'short') {
        task.options.duplex = Printing.PrintDuplex.twoSidedShortEdge;
    } else {
        task.options.duplex = Printing.PrintDuplex.oneSided;
    }

    task.options.numberOfCopies = config.copies || 1;

    task.oncompleted = function (e) {
        exports._func(e.detail[0].completion == 3);
    };
};

PrintManager.getForCurrentView()
    .onprinttaskrequested = exports.onPrintTaskRequested;

require('cordova/exec/proxy').add('Printer', exports);
