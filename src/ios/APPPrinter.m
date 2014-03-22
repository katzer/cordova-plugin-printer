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

#import "APPPrinter.h"

@interface APPPrinter (Private)

// Retrieves an instance of shared print controller
- (UIPrintInteractionController*) printController;
// Adjusts the settings for the print controller
- (UIPrintInteractionController*) adjustSettingsForPrintController:(UIPrintInteractionController*)controller;
// Loads the content into the print controller
- (void) loadContent:(NSString*)content intoPrintController:(UIPrintInteractionController*)controller;
// Opens the print controller so that the user can choose between available iPrinters
- (void) informAboutResult:(int)code callbackId:(NSString*)callbackId;
// Checks either the printing service is avaible or not
- (BOOL) isPrintingAvailable;

@end

@interface APPPrinter ()

// Getter property for the `isPrintingAvailable` method
@property (readonly, getter=isPrintingAvailable) BOOL isPrintingAvailable;
// Getter property for the `printController` method
@property (readonly, getter=printController) UIPrintInteractionController* printController;

@end


@implementation APPPrinter

@synthesize isPrintingAvailable, printController;

/*
 * Checks if the printing service is available.
 *
 * @param {Function} callback
 *      A callback function to be called with the result
 */
- (void) isServiceAvailable:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult;

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                       messageAsBool:isPrintingAvailable];

    [self.commandDelegate sendPluginResult:pluginResult
                                callbackId:command.callbackId];
}

/**
 * Sends the printing content to the printer controller and opens them.
 *
 * @param {NSString} content
 *      The (HTML encoded) content
 */
- (void) print:(CDVInvokedUrlCommand*)command
{
    if (!self.isPrintingAvailable)
    {
        return;
    }

    NSArray*  arguments  = [command arguments];
    NSString* content    = [arguments objectAtIndex:0];

    UIPrintInteractionController* controller = printController;

    [self adjustSettingsForPrintController:controller];
    [self loadContent:content intoPrintController:controller];

    [self openPrintController:controller];

    [self commandDelegate];
}

/**
 * Retrieves an instance of shared print controller.
 *
 * @return {UIPrintInteractionController*}
 */
- (UIPrintInteractionController*) printController
{
    return [UIPrintInteractionController sharedPrintController];
}

/**
 * Adjusts the settings for the print controller.
 *
 * @param {UIPrintInteractionController} controller
 *      The print controller instance
 *
 * @return {UIPrintInteractionController} controller
 *      The modified print controller instance
 */
- (UIPrintInteractionController*) adjustSettingsForPrintController:(UIPrintInteractionController*)controller
{
    UIPrintInfo* printInfo = [UIPrintInfo printInfo];

    printInfo.outputType = UIPrintInfoOutputGeneral;

    controller.printInfo      = printInfo;
    controller.showsPageRange = YES;

    return controller;
}

/**
 * Loads the content into the print controller.
 *
 * @param {NSString} content
 *      The (HTML encoded) content
 * @param {UIPrintInteractionController} controller
 *      The print controller instance
 */
- (void) loadContent:(NSString*)content intoPrintController:(UIPrintInteractionController*)controller
{
    // Set the base URL to be the www directory.
    NSString* wwwFilePath = [[NSBundle mainBundle] pathForResource:@"www"
                                                            ofType:nil];

    NSURL*    baseURL     = [NSURL fileURLWithPath:wwwFilePath];
    // Load page into a webview and use its formatter to print the page
    UIWebView* webPage    = [[UIWebView alloc] init];

    [webPage loadHTMLString:content baseURL:baseURL];

    // Get formatter for web (note: margin not required - done in web page)
    UIViewPrintFormatter* formatter = [webPage viewPrintFormatter];

    controller.printFormatter = formatter;
    controller.showsPageRange = YES;
}

/**
 * Opens the print controller so that the user can choose between
 * available iPrinters.
 *
 * @param {UIPrintInteractionController} controller
 *      The prepared print controller with a content
 */
- (void) openPrintController:(UIPrintInteractionController*)controller
{
    //[self.commandDelegate runInBackground:^{
        [controller presentAnimated:YES completionHandler:NULL];
    //}];
}

/**
 * Checks either the printing service is avaible or not.
 *
 * @return {BOOL}
 */
- (BOOL) isPrintingAvailable
{
    Class controllerCls = NSClassFromString(@"UIPrintInteractionController");

    if (!controllerCls){
        return NO;
    }

    return printController && [UIPrintInteractionController isPrintingAvailable];
}

@end
