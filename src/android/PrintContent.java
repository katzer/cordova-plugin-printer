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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

/**
 * Knows how to convert a resource URL into an io stream.
 */
class PrintContent
{
    // List of supported content types
    enum ContentType { PLAIN, HTML, IMAGE, PDF, UNSUPPORTED }

    // Helper class to deal with io operations
    private final @NonNull PrintIO io;

    /**
     * Initializes the asset utils.
     *
     * @param ctx The application context.
     */
    private PrintContent (@NonNull Context ctx) {
        io = new PrintIO(ctx);
    }

    /**
     * Returns the content type for the file referenced by its uri.
     *
     * @param path The path to check.
     *
     * @return The content type even the file does not exist.
     */
    @NonNull
    static ContentType getContentType (@Nullable String path,
                                       @NonNull Context context)
    {
        return new PrintContent(context).getContentType(path);
    }

    /**
     * Returns the content type for the file referenced by its uri.
     *
     * @param path The path to check.
     *
     * @return The content type even the file does not exist.
     */
    @NonNull
    private ContentType getContentType (@Nullable String path)
    {
        ContentType type = ContentType.PLAIN;

        if (path == null || path.isEmpty() || path.charAt(0) == '<')
        {
            type = ContentType.HTML;
        }
        else if (path.matches("^[a-z0-9]+://.+"))
        {
            String mime;

            if (path.startsWith("base64:"))
            {
                try
                {
                    mime = URLConnection.guessContentTypeFromStream(io.openBase64(path));
                }
                catch (IOException e)
                {
                    return ContentType.UNSUPPORTED;
                }
            }
            else
            {
                mime = URLConnection.guessContentTypeFromName(path);
            }

            switch (mime)
            {
                case "image/bmp":
                case "image/png":
                case "image/jpeg":
                case "image/jpeg2000":
                case "image/jp2":
                case "image/gif":
                case "image/x-icon":
                case "image/vnd.microsoft.icon":
                case "image/heif":
                    return ContentType.IMAGE;
                case "application/pdf":
                    return ContentType.PDF;
                default:
                    return ContentType.UNSUPPORTED;
            }
        }

        return type;
    }

    /**
     * Opens a file://, res:// or base64:// Uri as a stream.
     *
     * @param path The file path to decode.
     * @param context The application context.
     *
     * @return An open IO stream or null if the file does not exist.
     */
    @Nullable
    static BufferedInputStream open (@NonNull String path,
                                     @NonNull Context context)
    {
        return new PrintContent(context).open(path);
    }

    /**
     * Opens a file://, res:// or base64:// Uri as a stream.
     *
     * @param path The file path to decode.
     *
     * @return An open IO stream or null if the file does not exist.
     */
    @Nullable
    private BufferedInputStream open (@NonNull String path)
    {
        InputStream stream = null;

        if (path.startsWith("res:"))
        {
            stream = io.openResource(path);
        }
        else if (path.startsWith("file:///"))
        {
            stream = io.openFile(path);
        }
        else if (path.startsWith("file://"))
        {
            stream = io.openAsset(path);
        }
        else if (path.startsWith("base64:"))
        {
            stream = io.openBase64(path);
        }

        return stream != null ? new BufferedInputStream(stream) : null;
    }

    /**
     * Decodes a file://, res:// or base64:// Uri to bitmap.
     *
     * @param path    The file path to decode.
     * @param context The application context.
     *
     * @return A bitmap or null if the path is not valid
     */
    @Nullable
    static Bitmap decode (@NonNull String path, @NonNull Context context)
    {
        return new PrintContent(context).decode(path);
    }

    /**
     * Decodes a file://, res:// or base64:// Uri to bitmap.
     *
     * @param path The file path to decode.
     *
     * @return A bitmap or null if the path is not valid
     */
    @Nullable
    private Bitmap decode (@NonNull String path)
    {
        Bitmap bitmap;

        if (path.startsWith("res:"))
        {
            bitmap = io.decodeResource(path);
        }
        else if (path.startsWith("file:///"))
        {
            bitmap = io.decodeFile(path);
        }
        else if (path.startsWith("file://"))
        {
            bitmap = io.decodeAsset(path);
        }
        else if (path.startsWith("base64:"))
        {
            bitmap = io.decodeBase64(path);
        }
        else {
            bitmap = BitmapFactory.decodeFile(path);
        }

        return bitmap;
    }
}
