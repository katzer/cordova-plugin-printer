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

+ (double) convert:(nullable NSString *)unit
{
    if (!unit || [unit isEqual:[NSNull null]])
        return 1.0;

    if ([unit isEqualToString:@"in"])
        return 72.0;

    if ([unit isEqualToString:@"mm"])
        return 72.0 / 25.4;

    if ([unit isEqualToString:@"cm"])
        return 72.0 / 2.54;

    if (![unit isEqualToString:@"pp"])
    {
        NSLog(@"[cordova-plugin-printer] unit not recognized: %@", unit);
    }

    return 1.0;
}

@end
