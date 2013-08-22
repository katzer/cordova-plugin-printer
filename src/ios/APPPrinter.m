/**
 *  APPPrinter.m
 *  Cordova Printer Plugin
 *
 *  Created by Sebastian Katzer (github.com/katzer) on 10/08/2013.
 *  Copyright 2013 Sebastian Katzer. All rights reserved.
 *  GPL v2 licensed
 */

#import "APPPrinter.h"


@interface APPPrinter (Private)

// Erstellt den PrintController
- (UIPrintInteractionController *) getPrintController;
// Stellt die Eigenschaften des Druckers ein.
- (UIPrintInteractionController *) adjustSettingsForPrintController:(UIPrintInteractionController *)controller;
// Lädt den zu druckenden Content in ein WebView, welcher vom Drucker ausgedruckt werden soll.
- (void) loadContent:(NSString *)content intoPrintController:(UIPrintInteractionController *)controller;
// Ruft den Callback auf und informiert diesen über den das Ergebnis des Druckvorgangs.
- (void) informAboutResult:(BOOL)success serviceAvailable:(BOOL)available error:(NSString *)error callbackId:(NSString *)callbackId;
// Überprüft, ob der Drucker-Dienst verfügbar ist
- (BOOL) isPrintServiceAvailable;

@end


@implementation APPPrinter

/*
 * Is printing available.
 */
- (void) isServiceAvailable:(CDVInvokedUrlCommand *)command
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
- (void) print:(CDVInvokedUrlCommand *)command
{
    if (![self isPrintServiceAvailable])
    {
        [self informAboutResult:FALSE serviceAvailable:FALSE error:NULL callbackId:command.callbackId];
        return;
    }

    NSArray*  arguments  = [command arguments];
    NSString* content    = [arguments objectAtIndex:0];

    UIPrintInteractionController* controller = [self getPrintController];

    [self adjustSettingsForPrintController:controller];
    [self loadContent:content intoPrintController:controller];

    [controller presentAnimated:YES completionHandler:^(UIPrintInteractionController* printController, BOOL completed, NSError* error) {
        [self informAboutResult:completed serviceAvailable:TRUE error:(error ? error.localizedDescription : @"null") callbackId:command.callbackId];
    }];
}

/**
 * Erstellt den PrintController.
 *
 * @return {UIPrintInteractionController *}
 */
- (UIPrintInteractionController *) getPrintController
{
    return [UIPrintInteractionController sharedPrintController];
}

/**
 * Stellt die Eigenschaften des Druckers ein.
 *
 * @param {UIPrintInteractionController *} controller
 */
- (UIPrintInteractionController *) adjustSettingsForPrintController:(UIPrintInteractionController *)controller
{
    UIPrintInfo* printInfo    = [UIPrintInfo printInfo];
    printInfo.outputType      = UIPrintInfoOutputGeneral;
    controller.printInfo      = printInfo;
    controller.showsPageRange = YES;

    return controller;
}

/**
 * Lädt den zu druckenden Content in ein WebView, welcher vom Drucker ausgedruckt werden soll.
 *
 * @param {NSString}                       content
 * @param {UIPrintInteractionController *} controller
 */
- (void) loadContent:(NSString *)content intoPrintController:(UIPrintInteractionController *)controller
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
 * Ruft den Callback auf und informiert diesen über den das Ergebnis des Druckvorgangs.
 *
 * @param {NSString *} callbackId
 * @param {Boolean}    success
 * @param {Boolean}    available
 * @param {NSString *} error
 */
- (void) informAboutResult:(BOOL)success serviceAvailable:(BOOL)available error:(NSString *)error callbackId:(NSString *)callbackId
{
    NSString* wasSuccess   = success ? @"true" : @"false";
    NSString* wasAvailable = available ? @"true" : @"false";
    NSString* result       = [NSString stringWithFormat:@"%@, %@, %@", wasSuccess, wasAvailable, error];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                     messageAsString:result];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
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
