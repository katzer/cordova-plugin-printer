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

// Bereitet den Drucker-Kontroller vor
- (UIPrintInteractionController *) prepareController:(NSString *)content;
// Überprüft, ob der Drucker-Dienst verfügbar ist
- (BOOL) isPrintServiceAvailable;

@end


@implementation APPPrinter

/*
 * Is printing available.
 * Callback returns true/false if printing is available/unavailable.
 */
- (void) isServiceAvailable:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult* pluginResult = nil;

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
    NSArray*         arguments    = [command arguments];
    CDVPluginResult* pluginResult = nil;

    if (![self isPrintServiceAvailable])
    {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                        messageAsString:@"{success: false, available: false}"];

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

        return;
    }

    if ([arguments count] == 0)
    {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                        messageAsString:@"{success: false, available: true}"];

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

        return;
    }

    NSString                     *content    = [arguments objectAtIndex:0];
    UIPrintInteractionController *controller = [self prepareController:content];

    void (^completionHandler)(UIPrintInteractionController *, BOOL, NSError *) =
    ^(UIPrintInteractionController *printController, BOOL completed, NSError *error) {
        CDVPluginResult *pluginResult = nil;

        if (!completed || error)
        {
            NSString *result = [NSString stringWithFormat:@"{success: false, available: true, error: \"%@\"}", error.localizedDescription];

            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                            messageAsString:result];

        }
        else
        {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                            messageAsString:@"{success: true, available: true}"];
        }

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    };

    [controller presentAnimated:YES completionHandler:completionHandler];
}

/**
 * Bereitet den Drucker-Kontroller vor.
 *
 * @param {NSString} content Der zu druckende Inhalt
 */
- (UIPrintInteractionController *) prepareController:(NSString *)content
{
    UIPrintInteractionController *controller = [UIPrintInteractionController sharedPrintController];

    //Set the priner settings
    UIPrintInfo *printInfo    = [UIPrintInfo printInfo];
    printInfo.outputType      = UIPrintInfoOutputGeneral;
    controller.printInfo      = printInfo;
    controller.showsPageRange = YES;

    //Set the base URL to be the www directory.
    NSString *wwwFilePath     = [[NSBundle mainBundle] pathForResource:@"www" ofType:nil ];
    NSURL    *baseURL         = [NSURL fileURLWithPath:wwwFilePath];

    //Load page into a webview and use its formatter to print the page
    UIWebView *webViewPrint = [[UIWebView alloc] init];
    [webViewPrint loadHTMLString:content baseURL:baseURL];

    //Get formatter for web (note: margin not required - done in web page)
    UIViewPrintFormatter *viewFormatter = [webViewPrint viewPrintFormatter];
    controller.printFormatter           = viewFormatter;
    controller.showsPageRange           = YES;

    return controller;
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
