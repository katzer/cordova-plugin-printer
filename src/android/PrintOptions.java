/*
 Copyright 2013 Sebastián Katzer

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

package de.appplant.cordova.plugin.printer;

import android.print.PrintAttributes;
import android.support.annotation.NonNull;
import android.support.v4.print.PrintHelper;

import org.json.JSONObject;

import static android.os.Build.VERSION.SDK_INT;
import static android.print.PrintAttributes.DUPLEX_MODE_LONG_EDGE;
import static android.print.PrintAttributes.DUPLEX_MODE_NONE;
import static android.print.PrintAttributes.DUPLEX_MODE_SHORT_EDGE;
import static android.print.PrintAttributes.Margins.NO_MARGINS;
import static android.print.PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE;
import static android.print.PrintAttributes.MediaSize.UNKNOWN_PORTRAIT;
import static android.print.PrintDocumentInfo.PAGE_COUNT_UNKNOWN;
import static android.support.v4.print.PrintHelper.ORIENTATION_LANDSCAPE;
import static android.support.v4.print.PrintHelper.ORIENTATION_PORTRAIT;
import static android.support.v4.print.PrintHelper.SCALE_MODE_FILL;
import static android.support.v4.print.PrintHelper.SCALE_MODE_FIT;

/**
 * Wrapper for the print job settings.
 */
class PrintOptions
{
    // The print job settings
    private final @NonNull JSONObject spec;

    /**
     * Constructor
     *
     * @param spec The print job settings.
     */
    PrintOptions (@NonNull JSONObject spec)
    {
        this.spec = spec;
    }

    /**
     * Returns the name for the print job.
     */
    @NonNull String getJobName()
    {
        String jobName = spec.optString("name");

        if (jobName == null || jobName.isEmpty())
        {
            jobName = "Printer Plugin Job #" + System.currentTimeMillis();
        }

        return jobName;
    }

    /**
     * Returns the max page count.
     */
    int getPageCount()
    {
        int count = spec.optInt("pageCount", PAGE_COUNT_UNKNOWN);

        return count <= 0 ? PAGE_COUNT_UNKNOWN : count;
    }

    /**
     * Converts the options into a PrintAttributes object.
     */
    @NonNull PrintAttributes toPrintAttributes()
    {
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        Object margin                   = spec.opt("margin");

        switch (spec.optString("orientation"))
        {
            case "landscape":
                builder.setMediaSize(UNKNOWN_LANDSCAPE);
                break;
            case "portrait":
                builder.setMediaSize(UNKNOWN_PORTRAIT);
                break;
        }

        if (spec.has("monochrome"))
        {
            if (spec.optBoolean("monochrome"))
            {
                builder.setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME);
            }
            else
            {
                builder.setColorMode(PrintAttributes.COLOR_MODE_COLOR);
            }
        }

        if (margin instanceof Boolean && !((Boolean) margin))
        {
            builder.setMinMargins(NO_MARGINS);
        }

        if (SDK_INT >= 23)
        {
            switch (spec.optString("duplex"))
            {
                case "long":
                    builder.setDuplexMode(DUPLEX_MODE_LONG_EDGE);
                    break;
                case "short":
                    builder.setDuplexMode(DUPLEX_MODE_SHORT_EDGE);
                    break;
                case "none":
                    builder.setDuplexMode(DUPLEX_MODE_NONE);
                    break;
            }
        }

        return builder.build();
    }

    /**
     * Tweaks the printer helper depending on the job spec.
     *
     * @param printer The printer to decorate.
     */
    void decoratePrintHelper (@NonNull PrintHelper printer)
    {
        switch (spec.optString("orientation"))
        {
            case "landscape":
                printer.setOrientation(ORIENTATION_LANDSCAPE);
                break;
            case "portrait":
                printer.setOrientation(ORIENTATION_PORTRAIT);
                break;
        }

        if (spec.has("monochrome"))
        {
            if (spec.optBoolean("monochrome"))
            {
                printer.setColorMode(PrintHelper.COLOR_MODE_MONOCHROME);
            }
            else
            {
                printer.setColorMode(PrintHelper.COLOR_MODE_COLOR);
            }
        }

        if (spec.optBoolean("autoFit", true))
        {
            printer.setScaleMode(SCALE_MODE_FIT);
        }
        else
        {
            printer.setScaleMode(SCALE_MODE_FILL);
        }
    }
}
