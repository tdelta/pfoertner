package de.tu_darmstadt.epool.pfoertnerpanel.qrcode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Android drawable for easy rendering of QR codes, given a message string.
 * (Internally it uses ZXings QRCodeWriter and renders it to an Android Bitmap)
 *
 * You can use an ImageView to display it.
 */
public class QRCode extends Drawable {
    private final QRCodeWriter writer = new QRCodeWriter();
    private final String message;

    // Bitmaps are displayed using a transformation matrix.
    // For now I am using an identity matrix, since there is no reason to transform it.
    private final static Matrix transformationMatrix = new Matrix(); //identity Matrix for now

    /**
     * Create the drawable.
     *
     * @param message which should be encoded within the displayed QR code
     */
    public QRCode(final String message) {
        this.message = message;
    }

    /**
     * This method is called when the drawable shall be drawn.
     *
     * @param canvas the drawable shall be drawn on
     */
    @Override
    public void draw(@NonNull Canvas canvas) {
        final int foregroundColor = Color.WHITE;
        final int backgroundColor = Color.BLACK;

        try {
            final Bitmap bitmap;

            {
                final BitMatrix m = writer.encode(  // using zxing to generate an qr code
                        message, // message to encode within the qr code
                        BarcodeFormat.QR_CODE, // we want a QR code
                        getBounds().width(), // width and height of the code shall match the width and height of this drawable
                        getBounds().height()
                );

                // ZXing returns just a boolean matrix, where each entry indicates, whether the corresponding pixel
                // should be black or white when drawing the QR code.

                // To render it within an android app, we therefore must convert it to an Bitmap
                // This is done by flat mapping the matrix into an 1-dimensional array of color values,
                // which can be used to initialize and draw a Bitmap.

                final int matrixWidth = m.getWidth();
                final int matrixHeight = m.getHeight();

                // Array of colors we will built the bitmap from
                final int[] pixels = new int[matrixWidth * matrixHeight];

                // for each row r within the qr code matrix...
                for (int y = 0; y < matrixHeight; ++y) {
                    final int linearOffset = y * matrixWidth;

                    // for each column c within that row...
                    for (int x = 0; x < matrixWidth; ++x) {
                        // check whether the element at row r and column c is a background pixel (white)
                        final boolean isBackground = m.get(x, y);

                        // if so, paint it in background color (white), otherwise in foreground color (black)
                        // by setting the corresponding bitmap pixel to the color value
                        pixels[linearOffset + x] = isBackground ? backgroundColor : foregroundColor;
                    }
                }

                // create the bitmap from the color pixels
                bitmap = Bitmap.createBitmap(
                        pixels,
                        matrixWidth,
                        matrixHeight,
                        Bitmap.Config.ARGB_8888 //colors are stored as 4 Bytes in pixels array
                );
            }

            // draw it
            canvas.drawBitmap(
                    bitmap,
                    transformationMatrix,
                    null // no special paint
            );
        } catch (final WriterException e) {
            e.printStackTrace();

            // TODO, proper error handling
        }
    }

    @Override
    public void setAlpha(int alpha) {
        //TODO
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        //TODO
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
