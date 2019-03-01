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

#include "APPPrinterItem.h"

@implementation APPPrinterItem

#pragma mark -
#pragma mark Public

/**
 * Returns the printing item refered by url either as NSURL or NSData.
 *
 * @param url Any kind of URL like file://, file:///, res:// or base64://
 *
 * @return [ id ] NSURL or NSData
 */
+ (id) ItemFromURL:(NSString *)url
{
    return [[[APPPrinterItem alloc] init] itemFromURL:url];
}

/**
 * Returns if the print framework is able to render the referenced file.
 *
 * @param url Any kind of URL like file://, file:///, res:// or base64://
 *
 * @return true if its able to render the content of the file.
 */
+ (BOOL) canPrintURL:(nullable NSString *)url
{
    if ([url isEqual:[NSNull null]] || ![NSURL URLWithString:url].scheme)
    {
        return UIPrintInteractionController.isPrintingAvailable;
    }

    id item = [self ItemFromURL:url];

    if ([item isKindOfClass:NSData.class])
    {
        return [UIPrintInteractionController canPrintData:item];
    }
    else
    {
        return [UIPrintInteractionController canPrintURL:item];
    }
}

#pragma mark -
#pragma mark Private

/**
 * Returns the printing item refered by url either as NSURL or NSData.
 *
 * @param url Any kind of URL like file://, file:///, res:// or base64://
 *
 * @return [ id ] NSURL or NSData
 */
- (id) itemFromURL:(NSString *)path
{
    if ([path hasPrefix:@"file:///"])
    {
        return [self urlForFile:path];
    }
    else if ([path hasPrefix:@"res:"])
    {
        return [self urlForResource:path];
    }
    else if ([path hasPrefix:@"file://"])
    {
        return [self urlForAsset:path];
    }
    else if ([path hasPrefix:@"base64:"])
    {
        return [self dataFromBase64:path];
    }

    NSFileManager* fm = [NSFileManager defaultManager];

    if (![fm fileExistsAtPath:path]){
        NSLog(@"File not found: %@", path);
    }

    return [NSURL fileURLWithPath:path];
}

/**
 * URL to an absolute file path.
 *
 * @param [ NSString* ] path An absolute file path.
 *
 * @return [ NSURL* ]
 */
- (NSURL *) urlForFile:(NSString *)path
{
    NSFileManager* fm = [NSFileManager defaultManager];

    NSString* absPath;
    absPath = [path stringByReplacingOccurrencesOfString:@"file://"
                                              withString:@""];

    if (![fm fileExistsAtPath:absPath]) {
        NSLog(@"File not found: %@", absPath);
    }

    return [NSURL fileURLWithPath:absPath];
}

/**
 * URL to a resource file.
 *
 * @param [ NSString* ] path A relative file path.
 *
 * @return [ NSURL* ]
 */
- (NSURL *) urlForResource:(NSString *)path
{
    NSFileManager* fm    = [NSFileManager defaultManager];
    NSBundle* mainBundle = [NSBundle mainBundle];
    NSString* bundlePath = [mainBundle resourcePath];

    if ([path isEqualToString:@"res://icon"])
    {
        if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
            path = @"res://AppIcon76x76@2x~ipad.png";
        } else {
            path = @"res://AppIcon60x60@2x.png";
        }
    }

    NSString* absPath;
    absPath = [path stringByReplacingOccurrencesOfString:@"res:/"
                                              withString:@""];

    absPath = [bundlePath stringByAppendingString:absPath];

    if (![fm fileExistsAtPath:absPath]) {
        NSLog(@"File not found: %@", absPath);
    }

    return [NSURL fileURLWithPath:absPath];
}

/**
 * Returns the file from the www folder as an NSURL object.
 *
 * @param url A relative www file path.
 *
 * @return [ NSURL ]
 */
- (NSURL *) urlForAsset:(NSString *)path
{
    NSFileManager* fm    = [NSFileManager defaultManager];
    NSBundle* mainBundle = [NSBundle mainBundle];
    NSString* bundlePath = [mainBundle bundlePath];

    NSString* absPath;
    absPath = [path stringByReplacingOccurrencesOfString:@"file:/"
                                              withString:@"/www"];

    absPath = [bundlePath stringByAppendingString:absPath];

    if (![fm fileExistsAtPath:absPath]) {
        NSLog(@"File not found: %@", absPath);
    }

    return [NSURL fileURLWithPath:absPath];
}

/**
 * Returns the Base64 string as an NSData object.
 *
 * @param url A valid Base64 string prefixed with base64://
 *
 * @return [ NSData ]
 */
- (NSData *) dataFromBase64:(NSString *)url
{
    NSString* base64String = [url substringFromIndex:9];

    return [[NSData alloc] initWithBase64EncodedString:base64String
                                               options:NSDataBase64DecodingIgnoreUnknownCharacters];
}

@end
