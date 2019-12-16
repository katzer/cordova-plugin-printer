/* globals Windows: true */

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

var Printing     = Windows.Graphics.Printing,
    PrintManager = Windows.Graphics.Printing.PrintManager;

/**
 * Verifies if printing is supported on the device.
 *
 * @param [ Function ] success Success callback function.
 * @param [ Function ] error   Error callback function.
 * @param [ Array ]    args    Interface arguments.
 *
 * @return [ Void ]
 */
exports.check = function (success, fail, args)
{
    var item      = args[0],
        supported = PrintManager.isSupported();

    if (!item || !supported)
    {
        success(supported);
        return;
    }

    if (item[0] === '<')
    {
        supported = true;
    }
    else
    {
        supported = item.match(/[a-z0-9]:\/\//) === null;
    }

    success(supported);
};

/**
 * List of printable document types.
 *
 * @param [ Function ] success Success callback function.
 * @param [ Function ] error   Error callback function.
 * @param [ Array ]    args    Interface arguments.
 *
 * @return [ Void ]
 */
exports.types = function (success, fail, args)
{
    success([]);
};

/**
 * Sends the content to the Printing Framework.
 *
 * @param [ Function ] success Success callback function.
 * @param [ Function ] error   Error callback function.
 * @param [ Array ]    args    Interface arguments.
 *
 * @return [ Void ]
 */
exports.print = function (success, fail, args)
{
    var content = args[0],
        page    = document, body;

    if (content && content.length > 0)
    {
        page = document.createDocumentFragment();
        body = document.createElement('html');

        body.innerHTML = content;
        page.appendChild(body);
    }

    exports._func = success;
    exports._args = args[1];

    MSApp.getHtmlPrintDocumentSourceAsync(page).then(function (source) {
        configureHtmlPrintDocumentSource(source, args[1]);
        exports._page = source;
        PrintManager.showPrintUIAsync();
    });
};

/**
 * Raised when a request to print has occurred.
 * Create, configure and schedule the print task.
 *
 * @param [ PrintTaskRequestedEventArgs ] event Event arguments.
 *
 * @return [ Void ]
 */
exports.onPrintTaskRequested = function (event)
{
    var config = exports._args,
        task, spec;

    task = event.request.createPrintTask(config.name, function (args) {
        args.setSource(exports._page);
    });

    spec = task.options;

    if (config.monochrome)
    {
        spec.colorMode = Printing.PrintColorMode.grayscale;
    }

    if (config.orientation == 'landscape')
    {
        spec.orientation = Printing.PrintOrientation.landscape;
    }
    else if (config.orientation == 'portrait')
    {
        spec.orientation = Printing.PrintOrientation.portrait;
    }

    if (config.duplex == 'long')
    {
        spec.duplex = Printing.PrintDuplex.twoSidedLongEdge;
    }
    else if (config.duplex == 'short')
    {
        spec.duplex = Printing.PrintDuplex.twoSidedShortEdge;
    }
    else
    {
        spec.duplex = Printing.PrintDuplex.oneSided;
    }

    if (config.photo)
    {
        spec.printQuality = Printing.PrintQuality.photographic;
        spec.mediaType    = Printing.PrintMediaType.photographic;
    }

    if (config.paper && config.paper.name)
    {
        spec.mediaSize = Printing.PrintMediaSize[config.paper.name] || Printing.PrintMediaSize.default;
    }

    try {
        spec.numberOfCopies = config.copies || 1;
    } catch (e) {}

    task.oncompleted = function (e) {
        exports._func(e.detail[0].completion === 3);
    };
};

function configureHtmlPrintDocumentSource(source, config) {
    var margin = config.margin === false ? { top: 0, left: 0, bottom: 0, right: 0 } : config.margin;

    if (margin) {
        // By observation, the units expected for these margin properties are hundredths of an
        // inch. Unfortunately, the docs for HtmlPrintDocumentSource don't state that explicitly.
        // See e.g. https://docs.microsoft.com/en-us/uwp/api/windows.ui.webui.htmlprintdocumentsource.bottommargin
        // (and ditto for top, left, and right margins). Note that the docs for a separate class
        // System.Drawing.Printing.Margins used elsewhere in the UWP printing API DO state that
        // the expected units are hundredths of an inch. See e.g.
        // https://docs.microsoft.com/en-us/dotnet/api/system.drawing.printing.margins.bottom
        // (and ditto for top, left, and right margins).
        source.topMargin = convertToInches(margin.top) * 100;
        source.bottomMargin = convertToInches(margin.bottom) * 100;
        source.leftMargin = convertToInches(margin.left) * 100;
        source.rightMargin = convertToInches(margin.right) * 100;
    }

    if (typeof config.percentScale === 'number') {
        source.percentScale = config.percentScale;
    }
}

var PT = /^(.*)pt$/i,
    IN = /^(.*)in$/i,
    CM = /^(.*)cm$/i,
    MM = /^(.*)mm$/i;

var IN_PER_PT = 1 / 72,
    IN_PER_CM = 1 / 2.54,
    IN_PER_MM = 1 / 25.4;

function convertToInches(unit) {
    // Cf src/ios/APPPrinterUnit.m, but w/ inches rather than points as the target, for easier
    // interop w/ UWP printing APIs.

    if (unit === undefined || unit === null) {
        return 0;
    } else if (typeof unit === 'number') {
        // If not otherwise specified, assume pt, as stated in our README
        return unit * IN_PER_PT;
    } else {
        unit = '' + unit;
        var match;

        match = unit.match(PT);
        if (match) {
            return numberify(match[1], unit) * IN_PER_PT;
        }

        match = unit.match(IN);
        if (match) {
            return numberify(match[1], unit);
        }

        match = unit.match(CM);
        if (match) {
            return numberify(match[1], unit) * IN_PER_CM;
        }

        match = unit.match(MM);
        if (match) {
            return numberify(match[1], unit) * IN_PER_MM;
        }

        // If not otherwise specified, assume pt, as stated in our README
        return numberify(unit, unit) * IN_PER_PT;
    }
}

function numberify(unitless, inContext) {
    var num = +unitless;
    if (isNaN(num)) {
        console.error('[cordova-plugin-printer] unparseable Unit string: ' + inContext);
        num = 0;
    }
    return num;
}

PrintManager.getForCurrentView().onprinttaskrequested = exports.onPrintTaskRequested;

require('cordova/exec/proxy').add('Printer', exports);
