package com.appzonegroup.app.fasttrack.utility.online;

/**
 * Created by fdamilola on 9/11/15.
 * Contact fdamilola@gmail.com or fdamilola@hextremelabs.com or fdamilola@echurch.ng
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * @author ADIO Kingsley O.
 * @since 19 Apr, 2015
 */
public class ImageUtils {

    private static final String TAG = ImageUtils.class.getSimpleName();
    private static final Matrix mMatrix = new Matrix();
    public static final int IMAGE_SIZE = 640;

    public static Bitmap getProcessedBitmap(String filePath) {
        // Calculate options for decoding bitmap
        final BitmapFactory.Options options = getBitmapDecodeOptions(filePath);
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        // Rotate bitmap, maybe
        int rotationAngle = getRotationAngle(filePath);
        if (rotationAngle != 0) {
            bitmap = rotateBitmap(bitmap, rotationAngle);
        }
        return bitmap;
    }

    public static BitmapFactory.Options getBitmapDecodeOptions(String imagePath) {
        return getBitmapDecodeOptions(imagePath, IMAGE_SIZE, IMAGE_SIZE);
    }

    public static BitmapFactory.Options getBitmapDecodeOptions(String imagePath, int outWidth, int outHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        options.inSampleSize = calculateSampleSize(options, outWidth, outHeight);
        options.inJustDecodeBounds = false;
        return options;
    }

    public static int getRotationAngle(String imagePath) {
        try {
            ExifInterface ei = new ExifInterface(imagePath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not detect image rotation angle", e);
        }
        return 0;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int rotationAngle) {
        mMatrix.reset();
        mMatrix.postRotate(rotationAngle);
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), mMatrix, false);
    }

    private static int calculateSampleSize(BitmapFactory.Options options, int dstWidth, int dstHeight) {
        // Calculate inSampleSize
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > dstHeight || width > dstWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > dstHeight
                    && (halfWidth / inSampleSize) > dstWidth) {
                inSampleSize <<= 1;
            }
        }
        return inSampleSize;
    }

    public static Bitmap getResizedBitmap(Bitmap image, int bitmapWidth,
                                   int bitmapHeight) {
        return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight,
                true);
    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static String encodeToBase64(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
