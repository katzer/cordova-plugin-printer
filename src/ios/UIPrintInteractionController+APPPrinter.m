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
#include "UIPrintInteractionController+APPPrinter.h"

#include <objc/runtime.h>

@implementation UIPrintInteractionController (APPPrinter)

static char UIP_SETTINGS_KEY;

@dynamic settings;

#pragma mark -
#pragma mark Init

+ (instancetype) sharedPrintControllerWithSettings:(NSDictionary *)settings
{
    NSDictionary* ui = settings[@"ui"];

    UIPrintInteractionController* ctrl =
    UIPrintInteractionController.sharedPrintController;

    ctrl.printInfo = [APPPrinterInfo
                      printInfoWithDictionary:settings];

    ctrl.settings  = settings;

    if (ui)
    {
        ctrl.showsNumberOfCopies = ![ui[@"hideNumberOfCopies"] boolValue];
        ctrl.showsPaperSelectionForLoadedPapers = ![ui[@"hidePaperFormat"] boolValue];
    }

    return ctrl;
}

#pragma mark -
#pragma mark Private

- (void) setSettings:(NSDictionary *)settings
{
    objc_setAssociatedObject(self, &UIP_SETTINGS_KEY, settings, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (NSDictionary *)settings
{
    return objc_getAssociatedObject(self, &UIP_SETTINGS_KEY);
}

@end
