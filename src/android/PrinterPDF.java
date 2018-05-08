package de.appplant.cordova.plugin.printer;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PrinterPDF {


    /**
     * Convert the content into an input stream
     *
     * @param content
     *      The encoded data string or file uri string
     *
     * @return
     *      content as InputStream
     */
    private InputStream convertContentToInputStream(final String content) throws FileNotFoundException {
        InputStream input = null;

        File file = new File(content.replaceAll("file:///", "/"));
        if (!file.exists()) {
            handlePrintError(new Exception("File does not exist"));
        } else {
            input = new FileInputStream(file);
        }

        return input;
    }

    private void writeInputStreamToOutput (InputStream input, FileOutputStream output) throws IOException {
        byte[] buf = new byte[1024];
        int bytesRead;

        while ((bytesRead = input.read(buf)) > 0) {
            output.write(buf, 0, bytesRead);
        }
        output.close();
    }

    private void handlePrintError (Exception e) {
        e.printStackTrace();
    }


    /**
     * Print the document using the native print api
     *
     * @param content
     *      The encoded data string or file uri string
     *
     * @param title
     *      The document title as a string
     *
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public PrintDocumentAdapter CreatePrintAdapter(final String content,  final String title) {
        return new PrintDocumentAdapter() {
            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {

                if (cancellationSignal.isCanceled()) {
                    return;
                }

                InputStream input = null;
                try {
                    input = convertContentToInputStream(content);
                } catch (FileNotFoundException e) {
                    handlePrintError(e);
                }
                FileOutputStream output = new FileOutputStream(destination.getFileDescriptor());

                try {
                    writeInputStreamToOutput(input, output);
                } catch (IOException e) {
                    handlePrintError(e);
                }

                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            }

            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {

                if (cancellationSignal.isCanceled()) {
                    return;
                }

                PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(title).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();

                callback.onLayoutFinished(pdi, true);
            }

            @Override
            public void onFinish() {

            }
        };
    }

}