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

#include "APPPrinterInfo.h"

#import <objc/message.h>

@implementation APPPrinterInfo

#pragma mark -
#pragma mark Init

+ (UIPrintInfo *) printInfoWithDictionary:(NSDictionary *)spec
{
    UIPrintInfo* info = [APPPrinterInfo printInfo];
    NSString* duplex  = spec[@"duplex"];
    NSString* jobName = spec[@"name"];
    long copies       = MAX([spec[@"copies"] longValue], 1);

    if ([spec[@"orientation"] isEqualToString:@"landscape"])
    {
        info.orientation = UIPrintInfoOrientationLandscape;
    }
    else if ([spec[@"orientation"] isEqualToString:@"portrait"])
    {
        info.orientation = UIPrintInfoOrientationPortrait;
    }

    if ([spec[@"monochrome"] boolValue])
    {
        if ([spec[@"photo"] boolValue])
        {
            info.outputType = UIPrintInfoOutputPhotoGrayscale;
        }
        else
        {
            info.outputType = UIPrintInfoOutputGrayscale;
        }
    }
    else if ([spec[@"photo"] boolValue])
    {
        info.outputType = UIPrintInfoOutputPhoto;
    }

    if ([duplex isEqualToString:@"long"])
    {
        info.duplex = UIPrintInfoDuplexLongEdge;
    }
    else if ([duplex isEqualToString:@"short"])
    {
        info.duplex = UIPrintInfoDuplexShortEdge;
    }
    else if ([duplex isEqualToString:@"none"])
    {
        info.duplex = UIPrintInfoDuplexNone;
    }

    if (class_getProperty(info.class, "copies"))
    {
        [info setValue:[NSNumber numberWithLong:copies] forKey:@"_copies"];
    }

    if (jobName)
    {
        info.jobName = jobName;
    }

    return info;
}

@end
