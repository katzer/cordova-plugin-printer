/**
 *  APPPrinter.h
 *  Cordova Printer Plugin
 *
 *  Created by Sebastian Katzer (github.com/katzer) on 10/08/2013.
 *  Copyright 2013 Sebastian Katzer. All rights reserved.
 *  GPL v2 licensed
 */

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>


@interface APPPrinter : CDVPlugin {

}

// Prints the content
- (void) print:(CDVInvokedUrlCommand*)command;
// Find out whether printing is supported on this platform
- (void) isServiceAvailable:(CDVInvokedUrlCommand*)command;

@end
