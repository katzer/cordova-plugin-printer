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
#include "APPPrinterRenderer.h"
#include "APPPrinterFont.h"
#include "APPPrinterUnit.h"

@interface APPPrinterRenderer ()

@property (nonatomic, retain) NSDictionary *settings;

@end

@implementation APPPrinterRenderer

#pragma mark -
#pragma mark Public

- (instancetype) initWithDictionary:(NSDictionary *)spec formatter:(UIPrintFormatter *)formatter
{
    NSDictionary* header = spec[@"header"];
    NSDictionary* footer = spec[@"footer"];

    self = [self init];

    [APPPrinterLayout configureFormatter:formatter
                            withSettings:spec];

    [self addPrintFormatter:formatter startingAtPageAtIndex:0];

    if (header)
    {
        self.headerHeight = [APPPrinterUnit convert:header[@"height"]];
    }

    if (footer)
    {
        self.footerHeight = [APPPrinterUnit convert:footer[@"height"]];
    }

    _settings = spec;

    return self;
}

- (NSInteger) numberOfPages
{
    NSInteger num = [super numberOfPages];
    id maxPages   = _settings[@"pageCount"];

    if (maxPages < 0)
    {
        return MAX(1, num + [maxPages longValue]);
    }

    return maxPages ? MAX(1, MIN(num, [maxPages longValue])) : num;
}

#pragma mark -
#pragma mark Callbacks

- (void) drawHeaderForPageAtIndex:(NSInteger)index inRect:(CGRect)rect
{
    [self drawLabelsFromDictionary:_settings[@"header"]
                    forPageAtIndex:index
                            inRect:rect];
}

- (void) drawFooterForPageAtIndex:(NSInteger)index inRect:(CGRect)rect
{
    [self drawLabelsFromDictionary:_settings[@"footer"]
                    forPageAtIndex:index
                            inRect:rect];
}

#pragma mark -
#pragma mark Private

- (void) drawLabelsFromDictionary:(NSDictionary *)spec
                   forPageAtIndex:(NSInteger)index
                           inRect:(CGRect)rect
{
    NSDictionary* label  = spec[@"label"];
    NSArray *labels = label ? @[label] : spec[@"labels"];

    if (!labels)
    {
        NSString *text = spec[@"text"];

        if (![self strIsNullOrEmpty:text])
        {
            labels = @[@{ @"text":text }];
        }
    }

    if (!labels) return;

    for (label in labels) {
        [self drawLabelFromDictionary:label forPageAtIndex:index inRect:rect];
    }
}

- (void) drawLabelFromDictionary:(NSDictionary *)spec
                  forPageAtIndex:(NSInteger)index
                          inRect:(CGRect)rect
{
    NSString *label = spec[@"text"];
    BOOL showIndex  = [spec[@"showPageIndex"] boolValue];

    if (showIndex)
    {
        label = [self strIsNullOrEmpty:label] ? @"%ld" : label;
        label = [NSString stringWithFormat:label, index + 1];
    }

    if ([self strIsNullOrEmpty:label])
        return;

    APPPrinterFont *font = [[APPPrinterFont alloc]
                            initWithDictionary:spec[@"font"]];

    NSDictionary *attributes = font.attributes;

    if (spec[@"top"] || spec[@"left"] || spec[@"right"] || spec[@"bottom"])
    {
        [label drawAtPoint:[self pointFromPositionAsDictionary:spec
                                                      forLabel:label
                                                withAttributes:attributes
                                                        inRect:rect]
            withAttributes:attributes];
    }
    else
    {
        [label drawInRect:rect withAttributes:attributes];
    }
}

- (CGPoint) pointFromPositionAsDictionary:(NSDictionary *)spec
                                 forLabel:(NSString *)label
                           withAttributes:(NSDictionary *)attributes
                                   inRect:(CGRect)rect
{
    id top    = spec[@"top"];
    id left   = spec[@"left"];
    id right  = spec[@"right"];
    id bottom = spec[@"bottom"];

    double x = rect.origin.x, y = rect.origin.y;
    CGSize size;

    if (bottom || right)
    {
        size =  [label sizeWithAttributes:attributes];
    }

    if (top)
    {
        y = rect.origin.y + [APPPrinterUnit convert:top];
    }
    else if (bottom)
    {
        y = rect.origin.y - size.height - [APPPrinterUnit convert:bottom];
    }

    if (left)
    {
        x = rect.origin.x + [APPPrinterUnit convert:left];
    }
    else if (right)
    {
        x = rect.size.width - size.width - [APPPrinterUnit convert:right];
    }

    return CGPointMake(x, y);
}

- (BOOL) strIsNullOrEmpty:(NSString *)string
{
    return [string isEqual:[NSNull null]] || string.length == 0;
}

@end
