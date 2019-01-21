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
        dots = [self pointsPerUnit:header[@"unit"]];
        self.headerHeight = dots * [header[@"height"] doubleValue];
    }

    if (footer)
    {
        dots = [self pointsPerUnit:header[@"unit"]];
        self.footerHeight = dots * [footer[@"height"] doubleValue];
    }

    return self;
}

#pragma mark -
#pragma mark Private

- (void) drawHeaderForPageAtIndex:(NSInteger)index inRect:(CGRect)rect
{
    NSString *header = [NSString stringWithFormat:@"Sample header at page %ld", index + 1];
    NSMutableParagraphStyle *style = [[NSParagraphStyle defaultParagraphStyle] mutableCopy];

    style.alignment = NSTextAlignmentRight;

    UIFont *font = [UIFont fontWithName:@"Courier" size:UIFont.labelFontSize];

    NSDictionary *attrs = @{ NSFontAttributeName: font,
                                  NSParagraphStyleAttributeName: style,
                                  NSForegroundColorAttributeName: [UIColor redColor]};

    [header drawInRect:rect withAttributes:attrs];
}

- (void) drawFooterForPageAtIndex:(NSInteger)index inRect:(CGRect)rect
{
    NSString *footer = @"Sample Footer";
    NSMutableParagraphStyle *style = [[NSParagraphStyle defaultParagraphStyle] mutableCopy];

    style.alignment = NSTextAlignmentCenter;

    UIFont *font = [UIFont fontWithName:@"Helvetica" size:UIFont.buttonFontSize];

    NSDictionary *attrs = @{ NSFontAttributeName: font,
                             NSParagraphStyleAttributeName: style,
                             NSForegroundColorAttributeName: [UIColor blueColor]};

    [footer drawInRect:rect withAttributes:attrs];
}

- (double) pointsPerUnit:(NSString*)unit
{
    if ([unit isEqualToString:@"in"])
        return 72.0;

    if ([unit isEqualToString:@"mm"])
        return 72.0 / 25.4;

    if ([unit isEqualToString:@"cm"])
        return 72.0 / 2.54;

    if (![unit isEqualToString:@"pp"])
    {
        NSLog(@"[cordova-plugin-printer] unit not recogniced: %@", unit);
    }

    return 1.0;
}

@end
