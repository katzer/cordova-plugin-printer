/**
 *  Printer.m
 *  Cordova Printer Plugin
 *
 *  Created by Sebastian Katzer (github.com/katzer) on 10/08/2013.
 *  Copyright 2013 Sebastian Katzer. All rights reserved.
 *  GPL v2 licensed
 */

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>


@interface CDVPrinter : CDVPlugin {

}

// Prints the content
- (void) print:(CDVInvokedUrlCommand*)command;
// Find out whether printing is supported on this platform
- (void) isServiceAvailable:(CDVInvokedUrlCommand*)command;

@end


@interface CDVPrinter (Private)

// Bereitet den Drucker-Kontroller vor
- (UIPrintInteractionController *) prepareController:(NSString*)content;
// Überprüft, ob der Drucker-Dienst verfügbar ist
- (BOOL) isPrintServiceAvailable;

@end