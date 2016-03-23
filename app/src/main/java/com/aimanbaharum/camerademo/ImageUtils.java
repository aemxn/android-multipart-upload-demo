package com.aimanbaharum.camerademo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by cliqers on 23/3/2016.
 */
public class ImageUtils {

    private static final String TAG = ImageUtils.class.getSimpleName();

    public static Bitmap getScaledImage(Uri uri, Context context) {

        InputStream in = null;

        try {
            final int IMAGE_MAX_SIZE = 900000; // 0.9mp
            in = context.getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            if (in != null) {
                in.close();
            }

            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d(TAG, "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-heigth: " + o.outHeight);

            Bitmap b = null;
            in = context.getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;

                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                int height = b.getHeight();
                int width = b.getWidth();
                Log.d(TAG, "1th scale operation dimension - w: " + width + ", h: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x, (int) y, true);
                b.recycle();
                b = scaledBitmap;
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            if (in != null) {
                in.close();
            }
            Log.d(TAG, "bitmap size - width: " + b.getWidth() + ", height: " + b.getHeight());
            return b;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            return null;
        }
    }
}
