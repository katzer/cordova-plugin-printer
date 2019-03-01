
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

#include "APPPrinterFont.h"

@interface APPPrinterFont ()

@property (nonatomic, retain) NSDictionary *settings;

@end

@implementation APPPrinterFont

#pragma mark -
#pragma mark Public

- (instancetype) initWithDictionary:(NSDictionary *)settings
{
    self = [self init];

    _settings = settings;

    return self;
}

- (NSDictionary<NSAttributedStringKey, id> *)attributes
{
    NSMutableParagraphStyle *style = [[NSParagraphStyle defaultParagraphStyle]
                                      mutableCopy];

    style.alignment = self.alignment;

    return @{ NSFontAttributeName: self.font,
              NSParagraphStyleAttributeName: style,
              NSForegroundColorAttributeName: self.color };
}

- (UIFont *)font
{
    double size = [_settings[@"size"] doubleValue];

    if (size <= 0.0)
    {
        size = UIFont.smallSystemFontSize;
    }

    UIFont *font = [UIFont fontWithName:_settings[@"name"] size:size];

    if (!font)
    {
        font = [UIFont systemFontOfSize:size];
    }

    UIFontDescriptorSymbolicTraits traits = 0;

    if ([_settings[@"bold"] boolValue])
    {
        traits = traits | UIFontDescriptorTraitBold;
    }

    if ([_settings[@"italic"] boolValue])
    {
        traits = traits | UIFontDescriptorTraitItalic;
    }

    UIFontDescriptor *descriptor = [font.fontDescriptor
                                    fontDescriptorWithSymbolicTraits:traits];

    return [UIFont fontWithDescriptor:descriptor size:size];
}

- (UIColor *)color
{
    NSString *hex  = _settings[@"color"];
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

    return color;
}

- (NSTextAlignment) alignment
{
    NSString *align = _settings[@"align"];

    if ([align isEqualToString:@"left"])
    {
        return NSTextAlignmentLeft;
    }

    if ([align isEqualToString:@"right"])
    {
        return NSTextAlignmentRight;
    }

    if ([align isEqualToString:@"center"])
    {
        return NSTextAlignmentCenter;
    }

    if ([align isEqualToString:@"justified"])
    {
        return NSTextAlignmentJustified;
    }

    return NSTextAlignmentNatural;
}

#pragma mark -
#pragma mark Private

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
