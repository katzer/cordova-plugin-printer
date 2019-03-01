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

#include "APPPrinterUnit.h"

@implementation APPPrinterUnit

/**
 * Converts any unit value to poings.
 *
 * @param [ id ] unit A number, a number as string or a string with a unit.
 *
 * @return [ double ] The converted unit into points.
 */
+ (double) convert:(nullable id)unit
{
    double value = 0;

    @try
    {
        if (!unit || [unit isEqual:[NSNull null]])
        {
            value = 0;
        }
        else if ([unit isKindOfClass:NSNumber.class])
        {
            value = [unit longValue];
        }
        else if ([unit hasSuffix:@"pt"])
        {
            value = [[self stringWithoutUnit:unit] doubleValue];
        }
        else if ([unit hasSuffix:@"in"])
        {
            value = [[self stringWithoutUnit:unit] doubleValue] * 72.0;
        }
        else if ([unit hasSuffix:@"mm"])
        {
            value = [[self stringWithoutUnit:unit] doubleValue] * 72.0 / 25.4;
        }
        else if ([unit hasSuffix:@"cm"]) {
            value = [[self stringWithoutUnit:unit] doubleValue] * 72.0 / 2.54;
        }
        else
        {
            value = [unit doubleValue];
        }
    }
    @catch (NSException *e)
    {
        NSLog(@"[cordova-plugin-printer] unit not recognized: %@", unit);
    }

    return value;
}

/**
 * Cuts the last 2 characters from the string.
 *
 * @param str A string like @"2cm"
 *
 * @return [ NSString ]
 */
+ (NSString *) stringWithoutUnit:(NSString *)str
{
    return [str substringToIndex:[str length] - 2];
}

@end
