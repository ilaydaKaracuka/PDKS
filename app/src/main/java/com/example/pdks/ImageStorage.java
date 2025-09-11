package com.example.pdks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageStorage {
    private static Bitmap bitmap;

    public static void setBitmap(Bitmap bmp) {
        bitmap = bmp;
    }

    public static Bitmap getBitmap() {
        return bitmap;
    }

    public static void clear() {
        bitmap = null;
    }

    public static Bitmap getBitmap(byte[] imageBytes) {
        if (imageBytes == null) return null;
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}
