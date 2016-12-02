package com.simplecamera.application.simplecamera;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.support.v4.graphics.ColorUtils;

public class BitmapManager
{
    private Bitmap bitmap;

    public BitmapManager() { bitmap = null; }
    public BitmapManager(Bitmap refMap) { bitmap = refMap; }

    public void setBitmap(Bitmap newMap)
    {
        bitmap.recycle();
        bitmap = newMap;
    }

    public Bitmap getBitmap() { return bitmap; }

    /**
     *   Manipulates a copy of the member Bitmap, bitmap, so
     *   that the copy is an grayscale (or intensity) image
     *   of the original, assuming that the original is an
     *   RGB (or truecolor) image.
     *
     *   @return
     *       A Bitmap object which is an grayscale image of
     *       the the truecolor member Bitmap, bitmap.
     */

    public Bitmap turnGray()
    {
        Bitmap outMap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas grayCanvas = new Canvas(outMap);

        Paint grayPaint = new Paint();
        ColorMatrix colormap = new ColorMatrix();
        colormap.setSaturation(0);

        grayPaint.setColorFilter(new ColorMatrixColorFilter(colormap));
        grayCanvas.drawBitmap(bitmap, 0, 0, grayPaint);

        return outMap;
    }

    /**
     *   Manipulates a copy of the member Bitmap, bitmap, so
     *   that the copy is a binary image of the original whose
     *   white pixels had an original intensity greater than or
     *   equal to the int parameter, threshold, and whose black
     *   pixels had an original intensity less than the threshold.
     *
     *   @param threshold
     *      The gray value threshold which defines which pixels
     *      in the binary transformation should be white and which
     *      should be black.
     *
     *   @return
     *       A binary image of the orginal member Bitmap, bitmap.
     */

    public Bitmap turnBinary(int threshold)
    {
        Bitmap outMap = turnGray();
        int width = outMap.getWidth(), height = outMap.getHeight();

        int [] pixArray = new int[width * height];
        outMap.getPixels(pixArray, 0, width, 0, 0, width, height);

        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
            {
                if (Color.red(pixArray[i + j*width]) < threshold)
                    outMap.setPixel(i, j, Color.BLACK);
                else
                    outMap.setPixel(i, j, Color.WHITE);
            }

        return outMap;
    }

    /**
     *   Manipulates a copy of the member Bitmap, bitmap, so
     *   that the the copy becomes a transformation of the original
     *   image from the RGB colorspace to the CIE L*a*b colorspace,
     *   assuming that bitmap is using the RGB image representation.
     *
     *   @return
     *       A copy of the member Bitmap, bitmap, which has
     *       been transformed into the CIE L*a*b representation.
     *
     *       The values previously stored in the red, green, and blue
     *       channels now represent the luminosity, a, and b channels,
     *       respectively.
     */

    public Bitmap convertToLAB()
    {
        Bitmap outMap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        int width = outMap.getWidth(), height = outMap.getHeight();

        double [] outLAB = {0, 0, 0};

        int [] pixArray = new int[width * height];
        bitmap.getPixels(pixArray, 0, width, 0, 0, width, height);

        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
            {
                ColorUtils.colorToLAB(pixArray[i + j * width], outLAB);

                /*

                    Convert all L*a*b* values to conform to the range
                    specifications required by bitmap images.

                */

                outMap.setPixel(i, j, Color.argb(Color.alpha(pixArray[i + j * width]), (int)(outLAB[0] * 255 / 100.0),
                        (int)(outLAB[1] + 128), (int)(outLAB[2] + 128)));
            }

        return outMap;
    }

    /**
     *   Manipulates a copy of the member Bitmap, bitmap, so
     *   that the the copy becomes a the a (or Red-Green) channel
     *   of the CIE L*a*b colorspace representation of the original.
     *
     *   @return
     *       A copy of the member Bitmap, bitmap, which
     *       contains the Red-Green channel of the CIE L*a*b
     *       colorspace representation of bitmap.
     *
     *       This image is an intensity image.
     */

    public Bitmap extractRG()
    {
        Bitmap outMap = convertToLAB();

        ColorMatrix colormap = new ColorMatrix(new float[]
                {
                        0, 1, 0, 0, 0,
                        0, 1, 0, 0, 0,
                        0, 1, 0, 0, 0,
                        0, 0, 0, 1, 0
                });

        Canvas redCanvas = new Canvas(outMap);
        Paint redPaint = new Paint();

        redPaint.setColorFilter(new ColorMatrixColorFilter(colormap));
        redCanvas.drawBitmap(outMap, 0, 0, redPaint);

        return outMap;
    }

    /**
     *   Manipulates a copy of the member Bitmap, bitmap, so
     *   that the the copy becomes a the b (or Blue-Yellow) channel
     *   of the CIE L*a*b colorspace representation of the original.
     *
     *   @return
     *       A copy of the member Bitmap, bitmap, which
     *       contains the Blue-Yellow channel of the CIE L*a*b
     *       colorspace representation of bitmap.
     *
     *       This image is an intensity image.
     */

    public Bitmap extractBY()
    {
        Bitmap outMap = convertToLAB();

        ColorMatrix colormap = new ColorMatrix(new float[]
                {
                        0, 0, 1, 0, 0,
                        0, 0, 1, 0, 0,
                        0, 0, 1, 0, 0,
                        0, 0, 0, 1, 0
                });

        Canvas blueCanvas = new Canvas(outMap);
        Paint bluePaint = new Paint();

        bluePaint.setColorFilter(new ColorMatrixColorFilter(colormap));
        blueCanvas.drawBitmap(outMap, 0, 0, bluePaint);


        return outMap;
    }
}