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
#include "APPPrinterUnit.h"

@interface APPPrinterRenderer ()

@property (nonatomic, retain) NSDictionary *settings;

@end

@implementation APPPrinterRenderer

#pragma mark -
#pragma mark Public

- (instancetype) initWithDictionary:(NSDictionary *)spec formatter:(UIPrintFormatter *)formatter
{
    NSDictionary* layout = spec[@"layout"];
    NSDictionary* header = spec[@"header"];
    NSDictionary* footer = spec[@"footer"];
    double dots          = 0;

    self = [self init];

    if (layout)
    {
        [APPPrinterLayout configureFormatter:formatter
                    withLayoutFromDictionary:layout];
    }

    [self addPrintFormatter:formatter startingAtPageAtIndex:0];

    if (header)
    {
        dots = [APPPrinterUnit convert:header[@"unit"]];
        self.headerHeight = dots * [header[@"height"] floatValue];
    }

    if (footer)
    {
        dots = [APPPrinterUnit convert:footer[@"unit"]];
        self.footerHeight = dots * [footer[@"height"] floatValue];
    }

    _settings = spec;

    return self;
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
    NSString *align = spec[@"align"];
    BOOL showIndex  = [spec[@"showPageIndex"] boolValue];

    if (showIndex)
    {
        label = [self strIsNullOrEmpty:label] ? @"%ld" : label;
        label = [NSString stringWithFormat:label, index + 1];
    }

    if ([self strIsNullOrEmpty:label])
        return;

    NSMutableParagraphStyle *style = [[NSParagraphStyle defaultParagraphStyle]
                                      mutableCopy];

    if ([align isEqualToString:@"left"])
    {
        style.alignment = NSTextAlignmentLeft;
    }
    else if ([align isEqualToString:@"right"])
    {
        style.alignment = NSTextAlignmentRight;
    }
    else if ([align isEqualToString:@"center"])
    {
        style.alignment = NSTextAlignmentCenter;
    }
    else if ([align isEqualToString:@"justified"])
    {
        style.alignment = NSTextAlignmentJustified;
    }

    NSString *hex  = spec[@"color"];
    UIColor *color;

    if ([self strIsNullOrEmpty:hex])
    {
        color = UIColor.darkTextColor;
    }
    else
    {
        if ([hex characterAtIndex:0] == '#')
        {
            hex = [hex substringFromIndex:1];
        }

        color = [self colorFromHexString:hex];
    }

    if (!color)
    {
        color = UIColor.darkTextColor;
    }

    double size = [spec[@"size"] doubleValue];

    if (size <= 0.0)
    {
        size = UIFont.smallSystemFontSize;
    }

    UIFont *font = [UIFont fontWithName:spec[@"font"] size:size];

    if (!font)
    {
        font = [UIFont systemFontOfSize:size];
    }

    UIFontDescriptorSymbolicTraits traits = 0;

    if ([spec[@"bold"] boolValue])
    {
        traits = traits | UIFontDescriptorTraitBold;
    }

    if ([spec[@"italic"] boolValue])
    {
        traits = traits | UIFontDescriptorTraitItalic;
    }

    font = [UIFont fontWithDescriptor:[font.fontDescriptor fontDescriptorWithSymbolicTraits:traits]
                                 size:size];

    NSDictionary *attrs = @{ NSFontAttributeName: font,
                             NSParagraphStyleAttributeName: style,
                             NSForegroundColorAttributeName: color };

    [label drawInRect:rect withAttributes:attrs];
}

- (BOOL) strIsNullOrEmpty:(NSString *)string
{
    return [string isEqual:[NSNull null]] || string.length == 0;
}

- (UIColor *)colorFromHexString:(NSString *)hex
{
    unsigned rgb = 0;

    NSScanner *scanner = [NSScanner scannerWithString:hex];

    [scanner setCharactersToBeSkipped:
     [NSCharacterSet characterSetWithCharactersInString:@"#"]];

    [scanner scanHexInt:&rgb];

    return [UIColor colorWithRed:((rgb & 0xFF0000) >> 16)/255.0
                           green:((rgb & 0xFF00) >> 8)/255.0
                            blue:(rgb & 0xFF)/255.0 alpha:1.0];
}

@end
