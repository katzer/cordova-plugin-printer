/*
 Copyright 2013 appPlant UG

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

// Erstellt den PrintController
- (UIPrintInteractionController*) getPrintController;
// Stellt die Eigenschaften des Druckers ein.
- (UIPrintInteractionController*) adjustSettingsForPrintController:(UIPrintInteractionController*)controller;
// Lädt den zu druckenden Content in ein WebView, welcher vom Drucker ausgedruckt werden soll.
- (void) loadContent:(NSString*)content intoPrintController:(UIPrintInteractionController*)controller;
// Ruft den Callback auf und informiert diesen über den das Ergebnis des Druckvorgangs.
- (void) informAboutResult:(int)code callbackId:(NSString*)callbackId;
// Überprüft, ob der Drucker-Dienst verfügbar ist
- (BOOL) isPrintServiceAvailable;

@end


@implementation APPPrinter

/*
 * Is printing available.
 */
- (void) isServiceAvailable:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult;

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                       messageAsBool:[self isPrintServiceAvailable]];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

/**
 * Öffnet den Drucker-Kontroller zur Auswahl des Druckers.
 * Callback gibt Meta-Informationen an.
 */
- (void) print:(CDVInvokedUrlCommand*)command
{
    if (![self isPrintServiceAvailable])
    {
        return;
    }

    NSArray*  arguments  = [command arguments];
    NSString* content    = [arguments objectAtIndex:0];

    UIPrintInteractionController* controller = [self getPrintController];

    [self adjustSettingsForPrintController:controller];
    [self loadContent:content intoPrintController:controller];

    [self openPrintController:controller];

    [self commandDelegate];
}

/**
 * Erstellt den PrintController.
 */
- (UIPrintInteractionController*) getPrintController
{
    return [UIPrintInteractionController sharedPrintController];
}

/**
 * Stellt die Eigenschaften des Druckers ein.
 */
- (UIPrintInteractionController*) adjustSettingsForPrintController:(UIPrintInteractionController*)controller
{
    UIPrintInfo* printInfo    = [UIPrintInfo printInfo];
    printInfo.outputType      = UIPrintInfoOutputGeneral;
    controller.printInfo      = printInfo;
    controller.showsPageRange = YES;

    return controller;
}

/**
 * Lädt den zu druckenden Content in ein WebView, welcher vom Drucker ausgedruckt werden soll.
 */
- (void) loadContent:(NSString*)content intoPrintController:(UIPrintInteractionController*)controller
{
    // Set the base URL to be the www directory.
    NSString* wwwFilePath = [[NSBundle mainBundle] pathForResource:@"www" ofType:nil];
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
 * Zeigt den PrintController an.
 */
- (void) openPrintController:(UIPrintInteractionController*)controller
{
    //[self.commandDelegate runInBackground:^{
        [controller presentAnimated:YES completionHandler:NULL];
    //}];
}

/**
 * Überprüft, ob der Drucker-Dienst verfügbar ist.
 */
- (BOOL) isPrintServiceAvailable
{
    Class printController = NSClassFromString(@"UIPrintInteractionController");

    if (printController)
    {
        UIPrintInteractionController* controller = [UIPrintInteractionController sharedPrintController];

        return (controller != nil) && [UIPrintInteractionController isPrintingAvailable];
    }

    return NO;
}

@end
