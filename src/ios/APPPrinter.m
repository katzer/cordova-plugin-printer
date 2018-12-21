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

#import "APPPrinter.h"
#import <Cordova/CDVAvailability.h>

@interface APPPrinter ()

@property (retain) NSString* callbackId;
@property (retain) NSMutableDictionary* settings;
// this is used to cache the uiprinter making repeated prints faster
@property (nonatomic) UIPrinter *previousPrinter;

@end


@implementation APPPrinter

#pragma mark -
#pragma mark Interface

/*
 * Checks if the printing service is available.
 */
- (void) check:(CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
        CDVPluginResult* pluginResult;
        BOOL isAvailable   = [self isPrintingAvailable];
        NSArray *multipart = @[[NSNumber numberWithBool:isAvailable],
                               [NSNumber numberWithInt:-1]];

        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                      messageAsMultipart:multipart];

        [self.commandDelegate sendPluginResult:pluginResult
                                    callbackId:command.callbackId];
    }];
}

/**
 * Sends the printing content to the printer controller and opens them.
 */
- (void) print:(CDVInvokedUrlCommand*)command
{
    if (!self.isPrintingAvailable) return;

    UIPrintInteractionController* controller = [self printController];
    NSString* content                        = command.arguments[0];

    _settings                                = command.arguments[1];
    _callbackId                              = command.callbackId;

    [self adjustPrintController:controller
                   withSettings:_settings];

    [self loadContent:content intoPrintController:controller];
}

/**
 * Displays system interface for selecting a printer.
 */
- (void) pick:(CDVInvokedUrlCommand*)command
{
    if (!self.isPrintingAvailable) return;

    NSMutableDictionary* settings = command.arguments[0];
    NSArray* bounds               = settings[@"bounds"];
    _callbackId                   = command.callbackId;
    CGRect rect;

    if (bounds) {
        rect = [self convertIntoRect:bounds];
    } else {
        rect = CGRectMake(40, 30, 0, 0);
    }

    [self presentPrinterPicker:rect];
}

#pragma mark -
#pragma mark UIWebViewDelegate

/**
 * Sent after a web view finishes loading a frame.
 *
 * @param webView The web view has finished loading.
 *
 * @return [ Void ]
 */
- (void) webViewDidFinishLoad:(UIWebView *)webView
{
    UIPrintInteractionController* controller = [self printController];
    NSString* printerID = _settings[@"printerId"];

    if (![printerID isEqual:[NSNull null]] && printerID.length > 0)
    {
        [self sendToPrinter:controller printer:printerID];
        return;
    }

    NSArray* bounds = self.settings[@"bounds"];
    CGRect rect     = [self convertIntoRect:bounds];

    [self presentPrintController:controller fromRect:rect];
}

#pragma mark -
#pragma mark UIPrintInteractionControllerDelegate

/**
 * Asks the delegate for an object encapsulating the paper size and printing
 * area to use for the print job. If Paper-Size is given it selects the best
 * fitting papersize
 */
- (UIPrintPaper *) printInteractionController:(UIPrintInteractionController *)printInteractionController
                                  choosePaper:(NSArray *)paperList
{
    double height = [self.settings[@"paperHeight"] doubleValue];
    double width  = [self.settings[@"paperWidth"] doubleValue];
    UIPrintPaper* paper;

    if (height && width)
    {
        double dotsHeigth = 72 * height / 25.4; //convert milimeters to dots
        double dotsWidth  = 72 * width / 25.4;  //convert milimeters to dots
        CGSize pageSize   = CGSizeMake(dotsHeigth, dotsWidth);

        // get best fitting paper size
        paper = [UIPrintPaper bestPaperForPageSize:pageSize
                               withPapersFromArray:paperList];
    }

    return paper;
}

/**
 * Asks the delegate for a length to use when cutting the page. If using roll
 * printers like Label-Printer (brother QL-710W) you can cut paper after given
 * length.
 */
- (CGFloat) printInteractionController:(UIPrintInteractionController *)printInteractionController
                     cutLengthForPaper:(UIPrintPaper *)paper
{
    double length  = [self.settings[@"paperCutLength"] doubleValue];
    CGFloat height = paper.paperSize.height;

    if (length)
    {
        height = 72 * length / 25.4; //convert milimeters to dots
    }

    return height;
}

#pragma mark -
#pragma mark Core

/**
 * Checks either the printing service is avaible or not.
 */
- (BOOL) isPrintingAvailable
{
    Class controllerCls = NSClassFromString(@"UIPrintInteractionController");

    if (!controllerCls) {
        return NO;
    }

    return [self printController] && [UIPrintInteractionController
                                      isPrintingAvailable];
}

/**
 * Sends the content directly to the specified or previously selected printer.
 *
 * @param controller The prepared print controller with the content.
 * @param printer    The printer specified by its URL.
 *
 * @return [ Void ]
 */
- (void) sendToPrinter:(UIPrintInteractionController*)controller
               printer:(NSString*)printerID
{
    NSURL* printerURL = [NSURL URLWithString:printerID];

    // check to see if we have previously created this printer to reduce printing/"contacting" time
    if (!_previousPrinter || ![_previousPrinter.URL.absoluteString isEqualToString:printerID])
    {
        _previousPrinter = [UIPrinter printerWithURL:printerURL];
    }

    [controller printToPrinter:_previousPrinter completionHandler:
     ^(UIPrintInteractionController *ctrl, BOOL ok, NSError *e) {
         CDVPluginResult* result =
         [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                             messageAsBool:ok];

         [self.commandDelegate sendPluginResult:result
                                     callbackId:self->_callbackId];
     }];
}

/**
 * Opens the print controller so that the user can choose between
 * available iPrinters.
 *
 * @param controller The prepared print preview controller.
 * @param rect       The coordinates where to present the preview.
 *
 * @return [ Void ]
 */
- (void) presentPrintController:(UIPrintInteractionController*)controller
                       fromRect:(CGRect)rect
{
    UIPrintInteractionCompletionHandler handler =
    ^(UIPrintInteractionController *ctrl, BOOL ok, NSError *e) {
        CDVPluginResult* result =
        [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                            messageAsBool:ok];

        [self.commandDelegate sendPluginResult:result
                                    callbackId:self.callbackId];
    };

    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        [controller presentFromRect:rect
                             inView:self.webView
                           animated:YES
                  completionHandler:handler];
    } else {
        [controller presentAnimated:YES
                  completionHandler:handler];
    }
}

/**
 * Displays system interface for selecting a printer.
 *
 * @param rect Defines where to display the interface on the screen.
 *
 * @return [ Void ]
 */
- (void) presentPrinterPicker:(CGRect)rect
{
    UIPrinterPickerController* controller =
    [UIPrinterPickerController printerPickerControllerWithInitiallySelectedPrinter:nil];

    UIPrinterPickerCompletionHandler handler =
    ^(UIPrinterPickerController *ctrl, BOOL selected, NSError *e) {
        [self returnPrinterPickerResult:ctrl];
    };

    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        [controller presentFromRect:rect
                             inView:self.webView
                           animated:YES
                  completionHandler:handler];
    } else {
        [controller presentAnimated:YES
                  completionHandler:handler];
    }
}

/**
 * Calls the callback funtion with the result of the selected printer.
 *
 * @param controller The UIPrinterPickerController used to display
 *                   the printer selector interface.
 *
 * @return [ Void ]
 */
- (void) returnPrinterPickerResult:(UIPrinterPickerController*)controller
{
    CDVPluginResult* result;
    UIPrinter* printer = controller.selectedPrinter;

    [self rememberPrinter:printer];

    if (printer) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                   messageAsString:printer.URL.absoluteString];
    } else {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    }

    [self.commandDelegate sendPluginResult:result
                                callbackId:_callbackId];
}

#pragma mark -
#pragma mark Helper

/**
 * Returns the shared instance of the printer controller.
 */
- (UIPrintInteractionController*) printController
{
    UIPrintInteractionController* controller = [UIPrintInteractionController
                                                sharedPrintController];

    controller.delegate = self;

    return controller;
}

/**
 * Adjusts the settings for the print controller.
 *
 * @param controller The print controller instance.
 * @param settings   The print job specs.
 *
 * @return The modified print controller instance
 */
- (UIPrintInteractionController*) adjustPrintController:(UIPrintInteractionController*)controller
                                           withSettings:(NSMutableDictionary*)settings
{
    UIPrintInfo* printInfo             = [UIPrintInfo printInfo];
    UIPrintInfoOrientation orientation = UIPrintInfoOrientationPortrait;
    UIPrintInfoOutputType outputType   = UIPrintInfoOutputGeneral;
    UIPrintInfoDuplex duplexMode       = UIPrintInfoDuplexNone;

    if ([settings[@"landscape"] boolValue]) {
        orientation = UIPrintInfoOrientationLandscape;
    }

    if ([settings[@"graystyle"] boolValue]) {
        outputType = UIPrintInfoOutputGrayscale;
    }

    outputType += [settings[@"border"] boolValue] ? 0 : 1;

    if ([settings[@"duplex"] isEqualToString:@"long"]) {
        duplexMode = UIPrintInfoDuplexLongEdge;
    } else
    if ([settings[@"duplex"] isEqualToString:@"short"]) {
        duplexMode = UIPrintInfoDuplexShortEdge;
    }

    printInfo.outputType  = outputType;
    printInfo.orientation = orientation;
    printInfo.duplex      = duplexMode;
    printInfo.jobName     = settings[@"name"];

    controller.printInfo  = printInfo;

    controller.showsNumberOfCopies = ![settings[@"hideNumberOfCopies"] boolValue];
    controller.showsPaperSelectionForLoadedPapers = ![settings[@"hidePaperFormat"] boolValue];

    return controller;
}

/**
 * Loads the content into the print controller.
 *
 * @param content    The (HTML encoded) content
 * @param controller The print controller instance
 *
 * @return [ Void ]
 */
- (void) loadContent:(NSString*)content intoPrintController:(UIPrintInteractionController*)controller
{
    UIPrintPageRenderer* renderer   = [[UIPrintPageRenderer alloc] init];
    UIViewPrintFormatter* formatter = self.webView.viewPrintFormatter;
    bool printSelf                  = ![content isEqual:[NSNull null]] && content.length == 0;

    if (!printSelf)
    {
        UIWebView* page = [[UIWebView alloc] init];
        formatter       = page.viewPrintFormatter;

        page.delegate = self;

        if ([NSURL URLWithString:content])
        {
            NSURL *url = [NSURL URLWithString:content];

            [page loadRequest:[NSURLRequest requestWithURL:url]];
        }
        else
        {
            NSString* path = [NSBundle.mainBundle pathForResource:@"www" ofType:nil];
            NSURL *url     = [NSURL fileURLWithPath:path];

            [page loadHTMLString:content baseURL:url];
        }
    }

    [renderer addPrintFormatter:formatter startingAtPageAtIndex:0];
    [controller setPrintPageRenderer:renderer];

    if (printSelf) {
        // just trigger the finish load fn straight off if using current webView
        [self webViewDidFinishLoad:(UIWebView *)self.webView];
    }
}

/**
 * Tells the system to pre-select the given printer next time.
 *
 * @param printer The printer to remeber.
 *
 * @return [ Void ]
 */
- (void) rememberPrinter:(UIPrinter*)printer
{
    if (!printer) return;
    [UIPrinterPickerController printerPickerControllerWithInitiallySelectedPrinter:printer];
}

/**
 * Convert Array into Rect object.
 *
 * @param bounds The bounds
 *
 * @return A converted Rect object
 */
- (CGRect) convertIntoRect:(NSArray*)bounds
{
    return CGRectMake([bounds[0] floatValue],
                      [bounds[1] floatValue],
                      [bounds[2] floatValue],
                      [bounds[3] floatValue]);
}

@end

