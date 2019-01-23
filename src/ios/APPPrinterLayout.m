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

#include "APPPrinterLayout.h"
#include "APPPrinterStyle.h"
#include "APPPrinterUnit.h"

@implementation APPPrinterLayout

#pragma mark -
#pragma mark Init

- (id) initWithDictionary:(nullable NSDictionary *)spec
{
    self = [self init];

    if (!spec) return self;

    NSDictionary* insests = spec[@"padding"];
    double maxWidth       = [spec[@"maxWidth"] doubleValue];
    double maxHeight      = [spec[@"maxHeight"] doubleValue];
    double dots           = [APPPrinterUnit convert:spec[@"unit"]];

    _contentInsets = UIEdgeInsetsMake(dots * [insests[@"top"] doubleValue],
                                      dots * [insests[@"left"] doubleValue],
                                      dots * [insests[@"bottom"] doubleValue],
                                      dots * [insests[@"right"] doubleValue]);

    _maximumContentWidth  = dots * maxWidth;

    _maximumContentHeight = dots * maxHeight;

    return self;
}

#pragma mark -
#pragma mark Public

+ (UIPrintFormatter *) configureFormatter:(UIPrintFormatter *)formatter
                 withLayoutFromDictionary:(NSDictionary *)layoutSpec
                  withStyleFromDictionary:(nullable NSDictionary *)styleSpec
{
    id layout = [[self alloc] initWithDictionary:layoutSpec];

    [layout configureFormatter:formatter];

    if (styleSpec && ![formatter isKindOfClass:UIMarkupTextPrintFormatter.class])
    {
        [layout configureTextFormatter:(UISimpleTextPrintFormatter *)formatter
               withStyleFromDictionary:styleSpec];
    }

    return formatter;
}

- (void) configureFormatter:(UIPrintFormatter *)formatter
{
    if (_maximumContentHeight)
    {
        formatter.maximumContentHeight = _maximumContentHeight;
    }

    if (_maximumContentWidth)
    {
        formatter.maximumContentWidth = _maximumContentWidth;
    }

    formatter.perPageContentInsets = _contentInsets;
}

- (void) configureTextFormatter:(UISimpleTextPrintFormatter *)formatter
        withStyleFromDictionary:(NSDictionary *)spec
{
    APPPrinterStyle *style = [[APPPrinterStyle alloc]
                              initWithDictionary:spec];

    formatter.font          = style.font;
    formatter.color         = style.color;
    formatter.textAlignment = style.textAlignment;
}

@end
