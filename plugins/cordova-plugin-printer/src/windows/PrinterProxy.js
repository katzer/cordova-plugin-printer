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

    if (config.margin === false)
    {
        spec.bordering = Printing.PrintBordering.borderless;
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

PrintManager.getForCurrentView().onprinttaskrequested = exports.onPrintTaskRequested;

require('cordova/exec/proxy').add('Printer', exports);
