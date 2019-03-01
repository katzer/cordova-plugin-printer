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
#include "APPPrinterFont.h"
#include "APPPrinterUnit.h"

@implementation APPPrinterLayout

#pragma mark -
#pragma mark Init

- (id) initWithDictionary:(nullable NSDictionary *)spec
{
    self = [self init];

    if (!spec) return self;

    NSDictionary *margin = spec[@"margin"];

    if ([margin isKindOfClass:NSDictionary.class])
    {
        _contentInsets = UIEdgeInsetsMake([APPPrinterUnit convert:margin[@"top"]],
                                          [APPPrinterUnit convert:margin[@"left"]],
                                          [APPPrinterUnit convert:margin[@"bottom"]],
                                          [APPPrinterUnit convert:margin[@"right"]]);
    }

    _maximumContentWidth  = [APPPrinterUnit convert:spec[@"maxWidth"]];

    _maximumContentHeight = [APPPrinterUnit convert:spec[@"maxHeight"]];

    return self;
}

#pragma mark -
#pragma mark Public

+ (UIPrintFormatter *) configureFormatter:(UIPrintFormatter *)formatter
                             withSettings:(NSDictionary *)settings
{
    id layout   = [[self alloc] initWithDictionary:settings];
    SEL setFont = NSSelectorFromString(@"font");

    [layout configureFormatter:formatter];

    if (settings && [formatter respondsToSelector:setFont])
    {
        [layout configureTextFormatter:(UISimpleTextPrintFormatter *)formatter
                          withSettings:settings];
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
                   withSettings:(NSDictionary *)settings
{
    APPPrinterFont *font = [[APPPrinterFont alloc]
                            initWithDictionary:settings[@"font"]];

    formatter.font          = font.font;
    formatter.color         = font.color;
    formatter.textAlignment = font.alignment;
}

@end
