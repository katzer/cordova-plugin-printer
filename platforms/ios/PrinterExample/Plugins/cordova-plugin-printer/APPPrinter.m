/*
 Copyright 2013 appPlant GmbH

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

#include "APPPrinter.h"
#include "APPPrinterItem.h"
#include "APPPrinterPaper.h"
#include "APPPrinterRenderer.h"
#include "UIPrintInteractionController+APPPrinter.h"

@interface APPPrinter ()

@property (nonatomic) UIPrinter *previousPrinter;

@end

@implementation APPPrinter

#pragma mark -
#pragma mark Interface

/*
 * Checks if the printing service is available.
 */
- (void) check:(CDVInvokedUrlCommand *)command
{
    [self.commandDelegate runInBackground:^{
        BOOL res = [APPPrinterItem canPrintURL:command.arguments[0]];

        [self sendResultWithMessageAsBool:res
                               callbackId:command.callbackId];
    }];
}

/*
 * List all printable document types (utis).
 */
- (void) types:(CDVInvokedUrlCommand *)command
{
    [self.commandDelegate runInBackground:^{
        NSSet *utis = UIPrintInteractionController.printableUTIs;

        CDVPluginResult* result =
        [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                            messageAsArray:utis.allObjects];

        [self.commandDelegate sendPluginResult:result
                                    callbackId:command.callbackId];
    }];
}

/**
 * Displays system interface for selecting a printer.
 */
- (void) pick:(CDVInvokedUrlCommand *)command
{
    [self.commandDelegate runInBackground:^{
        NSMutableDictionary* settings = command.arguments[0];
        settings[@"callbackId"]       = command.callbackId;

        [self presentPickerWithSettings:settings];
    }];
}

/**
 * Sends the printing content to the printer controller and opens them.
 */
- (void) print:(CDVInvokedUrlCommand *)command
{
    [self.commandDelegate runInBackground:^{
        NSString* content             = command.arguments[0];
        NSMutableDictionary* settings = command.arguments[1];
        settings[@"callbackId"]       = command.callbackId;

        [self printContent:content withSettings:settings];
    }];
}

#pragma mark -
#pragma mark UIPrintInteractionControllerDelegate

/**
 * Asks the delegate for an object encapsulating the paper size and printing
 * area to use for the print job. If Paper-Size is given it selects the best
 * fitting papersize
 */
- (UIPrintPaper *) printInteractionController:(UIPrintInteractionController *)ctrl
                                  choosePaper:(NSArray *)paperList
{
    APPPrinterPaper* paperSpec = [[APPPrinterPaper alloc]
                                  initWithDictionary:ctrl.settings[@"paper"]];

    return [paperSpec bestPaperFromArray:paperList];
}

/**
 * Asks the delegate for a length to use when cutting the page. If using roll
 * printers like Label-Printer (brother QL-710W) you can cut paper after given
 * length.
 */
- (CGFloat) printInteractionController:(UIPrintInteractionController *)ctrl
                     cutLengthForPaper:(UIPrintPaper *)paper
{
    APPPrinterPaper* paperSpec = [[APPPrinterPaper alloc]
                                  initWithDictionary:ctrl.settings[@"paper"]];

    return paperSpec.length || paper.paperSize.height;
}

#pragma mark -
#pragma mark Core

/**
 * Displays system interface for selecting a printer.
 *
 * @param settings Describes additional settings like from where to present the
 *                 picker for iPad.
 *
 * @return [ Void ]
 */
- (void) presentPickerWithSettings:(NSDictionary *)settings
{
    UIPrinterPickerController* controller =
    [UIPrinterPickerController printerPickerControllerWithInitiallySelectedPrinter:nil];

    UIPrinterPickerCompletionHandler handler =
    ^(UIPrinterPickerController *ctrl, BOOL selected, NSError *e) {
        [self returnPickerResultForController:ctrl
                                 callbackId:settings[@"callbackId"]];
    };

    dispatch_async(dispatch_get_main_queue(), ^{
        if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
            CGRect rect = [self rectFromDictionary:settings[@"ui"]];

            [controller presentFromRect:rect
                                 inView:self.webView
                               animated:YES
                      completionHandler:handler];
        } else {
            [controller presentAnimated:YES
                      completionHandler:handler];
        }
    });
}

/**
 * Loads the content into the print controller.
 *
 * @param content  The HTML content or remote web page URI to print.
 * @param settings The print job specs.
 *
 * @return [ Void ]
 */
- (void) printContent:(NSString *)content
         withSettings:(NSDictionary *)settings
{
    __block id item;

    UIPrintInteractionController* ctrl =
    [UIPrintInteractionController sharedPrintControllerWithSettings:settings];

    ctrl.delegate = self;

    if ([self strIsNullOrEmpty:content])
    {
        dispatch_sync(dispatch_get_main_queue(), ^{
            item = self.webView.viewPrintFormatter;
        });
    }
    else if ([content characterAtIndex:0] == '<')
    {
        dispatch_sync(dispatch_get_main_queue(), ^{
            item = [[UIMarkupTextPrintFormatter alloc]
                    initWithMarkupText:content];
        });
    }
    else if ([NSURL URLWithString:content].scheme)
    {
        item = [APPPrinterItem ItemFromURL:content];
    }
    else
    {
        dispatch_sync(dispatch_get_main_queue(), ^{
            item = [[UISimpleTextPrintFormatter alloc]
                    initWithText:content];
        });
    }

    [self useController:ctrl toPrintItem:item withSettings:settings];
}

/**
 * Print the rendered content of the given view.
 *
 * @param ctrl     The interactive printer controller.
 * @param item     Either the item to print or the formatted content.
 * @param settings The print job specs.
 *
 * @return [ Void ]
 */
- (void) useController:(UIPrintInteractionController *)ctrl
           toPrintItem:(id)item
          withSettings:(NSDictionary *)settings
{
    NSString* printer = settings[@"printer"];

    if ([item isKindOfClass:UIPrintFormatter.class])
    {
        ctrl.printPageRenderer =
        [[APPPrinterRenderer alloc] initWithDictionary:settings formatter:item];
    }
    else
    {
        ctrl.printingItem = item;
    }

    if ([self strIsNullOrEmpty:printer])
    {
        [self presentController:ctrl withSettings:settings];
    }
    else
    {
        [self printToPrinter:ctrl withSettings:settings];
    }
}

/**
 * Sends the content directly to the specified or previously selected printer.
 *
 * @param ctrl       The interactive printer controller.
 * @param printerURL The printer specified by its URL.
 * @param settings   The print job specs.
 *
 * @return [ Void ]
 */
- (void) printToPrinter:(UIPrintInteractionController *)ctrl
          withSettings:(NSDictionary *)settings
{
    NSString* callbackId = settings[@"callbackId"];
    NSString* printerURL = settings[@"printer"];
    UIPrinter* printer   = [self printerWithURL:printerURL];

    dispatch_async(dispatch_get_main_queue(), ^{
        [ctrl printToPrinter:printer completionHandler:
         ^(UIPrintInteractionController *ctrl, BOOL ok, NSError *e) {
             [self rememberPrinter:(ok ? printer : NULL)];
             [self sendResultWithMessageAsBool:ok callbackId:callbackId];
         }];
    });
}

/**
 * Opens the print controller so that the user can choose between
 * available iPrinters.
 *
 * @param ctrl     The interactive printer controller.
 * @param settings The print job specs.
 *
 * @return [ Void ]
 */
- (void) presentController:(UIPrintInteractionController *)ctrl
              withSettings:(NSDictionary *)settings
{
    NSString* callbackId = settings[@"callbackId"];
    CGRect rect          = [self rectFromDictionary:settings[@"ui"]];

    UIPrintInteractionCompletionHandler handler =
    ^(UIPrintInteractionController *ctrl, BOOL ok, NSError *e) {
        [self sendResultWithMessageAsBool:ok callbackId:callbackId];
    };

    dispatch_async(dispatch_get_main_queue(), ^{
        if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
        {
            [ctrl presentFromRect:rect
                           inView:self.webView
                         animated:YES
                completionHandler:handler];
        }
        else
        {
            [ctrl presentAnimated:YES
                completionHandler:handler];
        }
    });
}

#pragma mark -
#pragma mark Helper

/**
 * Tells the system to pre-select the given printer next time.
 *
 * @param printer The printer to remeber.
 *
 * @return [ Void ]
 */
- (void) rememberPrinter:(UIPrinter*)printer
{
    [UIPrinterPickerController
     printerPickerControllerWithInitiallySelectedPrinter:(_previousPrinter = printer)];
}

/**
 * Returns an object that can be used to connect to the network printer.
 *
 * @param url The network URL as a string.
 *
 * @return A printer even if the URL is not a valid printer.
 */
- (UIPrinter *)printerWithURL:(NSString *)urlAsString
{
    NSURL* url = [NSURL URLWithString:urlAsString];
    UIPrinter* printer;

    if (_previousPrinter && [_previousPrinter.URL.absoluteString isEqualToString:urlAsString])
    {
        printer = _previousPrinter;
    }
    else
    {
        printer = [UIPrinter printerWithURL:url];
    }

    return printer;
}

/**
 * Convert Array into Rect object.
 *
 * @param bounds The bounds
 *
 * @return A converted Rect object
 */
- (CGRect) rectFromDictionary:(NSDictionary *)pos
{
    CGFloat left = 40, top = 30, width = 0, height = 0;

    if (pos)
    {
        top    = [pos[@"top"] floatValue];
        left   = [pos[@"left"] floatValue];
        width  = [pos[@"width"] floatValue];
        height = [pos[@"height"] floatValue];
    }

    return CGRectMake(left, top, width, height);
}

/**
 * Test if the given string is null or empty.
 *
 * @param string The string to test.
 *
 * @return true or false
 */
- (BOOL) strIsNullOrEmpty:(NSString *)string
{
    return [string isEqual:[NSNull null]] || string.length == 0;
}

/**
 * Calls the callback funtion with the result of the selected printer.
 *
 * @param ctrl The controller used to display the printer selector interface.
 * @param callbackId The ID of the callback that shall receive the info.
 *
 * @return [ Void ]
 */
- (void) returnPickerResultForController:(UIPrinterPickerController *)ctrl
                              callbackId:(NSString *)callbackId
{
    UIPrinter* printer = ctrl.selectedPrinter;
    CDVPluginResult* result;

    [self rememberPrinter:printer];

    if (printer) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                   messageAsString:printer.URL.absoluteString];
    } else {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    }

    [self.commandDelegate sendPluginResult:result
                                callbackId:callbackId];
}

/**
 * Sends the plugin result by invoking the callback function with a boolean arg.
 *
 * @param msg The boolean message value.
 *
 * @return [ Void ]
 */
- (void) sendResultWithMessageAsBool:(BOOL)msg
                          callbackId:(NSString *)callbackId
{
    CDVPluginResult* result =
    [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                        messageAsBool:msg];

    [self.commandDelegate sendPluginResult:result
                                callbackId:callbackId];
}

@end
