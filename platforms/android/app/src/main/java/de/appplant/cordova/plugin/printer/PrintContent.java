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
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

class PrintContent {

    // List of supported content types
    enum ContentType { PLAIN, HTML, IMAGE, PDF, UNSUPPORTED }

    // Application context
    private final @NonNull Context context;

    /**
     * Initializes the asset utils.
     *
     * @param ctx The application context.
     */
    private PrintContent (@NonNull Context ctx) {
        context = ctx;
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

            if (path.startsWith("base64:")) {
                try {
                    mime = URLConnection.guessContentTypeFromStream(openBase64(path));
                } catch (IOException e) {
                    return ContentType.UNSUPPORTED;
                }
            } else {
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
    static InputStream open (@NonNull String path, @NonNull Context context)
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
    private InputStream open (@NonNull String path)
    {
        InputStream stream = null;

        if (path.startsWith("res:"))
        {
            stream = openResource(path);
        }
        else if (path.startsWith("file:///"))
        {
            stream = openFile(path);
        }
        else if (path.startsWith("file://"))
        {
            stream = openAsset(path);
        }
        else if (path.startsWith("base64:"))
        {
            stream = openBase64(path);
        }

        return stream;
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
            bitmap = decodeResource(path);
        }
        else if (path.startsWith("file:///"))
        {
            bitmap = decodeFile(path);
        }
        else if (path.startsWith("file://"))
        {
            bitmap = decodeAsset(path);
        }
        else if (path.startsWith("base64:"))
        {
            bitmap = decodeBase64(path);
        }
        else {
            bitmap = BitmapFactory.decodeFile(path);
        }

        return bitmap;
    }

    /**
     * Copies content of input stream to output stream.
     *
     * @param input  The readable input stream.
     * @param output The writable output stream.
     *
     * @throws IOException If the input stream is not readable,
     *                     or the output stream is not writable.
     */
    static void copy (@NonNull InputStream input,
                      @NonNull OutputStream output) throws IOException
    {
        byte[] buf = new byte[1024];
        int bytesRead;

        while ((bytesRead = input.read(buf)) > 0) {
            output.write(buf, 0, bytesRead);
        }

        input.reset();
        close(output);
    }

    /**
     * Closes the stream.
     *
     * @param stream The stream to close.
     */
    static void close (@NonNull Closeable stream)
    {
        try {
            stream.close();
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Opens an file given as a file:/// path.
     *
     * @param path The path to the file.
     *
     * @return An open IO stream or null if the file does not exist.
     */
    @Nullable
    private InputStream openFile (@NonNull String path)
    {
        String absPath = path.substring(7);

        try {
            return new FileInputStream(absPath);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Decodes an file given as a file:/// path to a bitmap.
     *
     * @param path The path to the file.
     *
     * @return A bitmap or null if the path is not valid
     */
    @Nullable
    private Bitmap decodeFile (@NonNull String path)
    {
        String absPath = path.substring(7);

        return BitmapFactory.decodeFile(absPath);
    }

    /**
     * Opens an asset file given as a file:// path.
     *
     * @param path The path to the asset.
     *
     * @return An open IO stream or null if the file does not exist.
     */
    @Nullable
    private InputStream openAsset (@NonNull String path)
    {
        String resPath = path.replaceFirst("file:/", "www");

        try {
            return getAssets().open(resPath);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Decodes an asset file given as a file:// path to a bitmap.
     *
     * @param path The path to the asset.
     *
     * @return A bitmap or null if the path is not valid
     */
    @Nullable
    private Bitmap decodeAsset (@NonNull String path)
    {
        InputStream stream  = openAsset(path);
        Bitmap bitmap;

        if (stream == null)
            return null;

        bitmap = BitmapFactory.decodeStream(stream);

        close(stream);

        return bitmap;
    }

    /**
     * Opens a resource file given as a res:// path.
     *
     * @param path The path to the resource.
     *
     * @return An open IO stream or null if the file does not exist.
     */
    @NonNull
    private InputStream openResource (@NonNull String path)
    {
        String resPath = path.substring(6);
        int resId      = getResId(resPath);

        return getResources().openRawResource(resId);
    }

    /**
     * Decodes a resource given as a res:// path to a bitmap.
     *
     * @param path The path to the resource.
     *
     * @return A bitmap or null if the path is not valid
     */
    @Nullable
    private Bitmap decodeResource (@NonNull String path)
    {
        String data  = path.substring(9);
        byte[] bytes = Base64.decode(data, 0);

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Opens a resource file given as a res:// path.
     *
     * @param path The path to the resource.
     *
     * @return An open IO stream or null if the file does not exist.
     */
    @NonNull
    private InputStream openBase64 (@NonNull String path)
    {
        String data  = path.substring(9);
        byte[] bytes = Base64.decode(data, 0);

        return new ByteArrayInputStream(bytes);
    }

    /**
     * Decodes a resource given as a base64:// string to a bitmap.
     *
     * @param path The given relative path.
     *
     * @return A bitmap or null if the path is not valid
     */
    @Nullable
    private Bitmap decodeBase64 (@NonNull String path)
    {
        String data  = path.substring(9);
        byte[] bytes = Base64.decode(data, 0);

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Returns the resource ID for the given resource path.
     *
     * @return The resource ID for the given resource.
     */
    private int getResId (@NonNull String resPath)
    {
        Resources res   = getResources();
        String pkgName  = context.getPackageName();
        String dirName  = "drawable";
        String fileName = resPath;

        if (resPath.contains("/")) {
            dirName  = resPath.substring(0, resPath.lastIndexOf('/'));
            fileName = resPath.substring(resPath.lastIndexOf('/') + 1);
        }

        String resName = fileName.substring(0, fileName.lastIndexOf('.'));
        int resId      = res.getIdentifier(resName, dirName, pkgName);

        if (resId == 0) {
            resId = res.getIdentifier(resName, "mipmap", pkgName);
        }

        if (resId == 0) {
            resId = res.getIdentifier(resName, "drawable", pkgName);
        }

        return resId;
    }

    /**
     * Returns the asset manager for the app.
     */
    private AssetManager getAssets() {
        return context.getAssets();
    }

    /**
     * Returns the resource bundle for the app.
     */
    private Resources getResources() {
        return context.getResources();
    }
}
