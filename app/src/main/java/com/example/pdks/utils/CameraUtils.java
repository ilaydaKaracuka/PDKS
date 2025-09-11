package com.example.pdks.utils;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.camera.core.CameraSelector;

import com.example.pdks.graphic.GraphicOverlay;

import static java.lang.Math.ceil;
import static java.lang.Math.max;

public final class CameraUtils {

    private static Float mScale = null;
    private static Float mOffsetX = null;
    private static Float mOffsetY = null;
    private static int cameraSelector = CameraSelector.LENS_FACING_FRONT;

    private CameraUtils() {
    }

    public static RectF calculateRect(GraphicOverlay overlay, float height, float width, Rect boundingBoxT) {
        boolean isLandscapeMode = overlay.getContext().getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;

        float whenLandscapeModeWidth = isLandscapeMode ? width : height;
        float whenLandscapeModeHeight = isLandscapeMode ? height : width;

        float scaleX = overlay.getWidth() / whenLandscapeModeWidth;
        float scaleY = overlay.getHeight() / whenLandscapeModeHeight;
        float scale = max(scaleX, scaleY);
        mScale = scale;

        float offsetX = (overlay.getWidth() - (float) ceil(whenLandscapeModeWidth * scale)) / 2.0f;
        float offsetY = (overlay.getHeight() - (float) ceil(whenLandscapeModeHeight * scale)) / 2.0f;
        mOffsetX = offsetX;
        mOffsetY = offsetY;

        RectF mappedBox = new RectF();
        mappedBox.left = boundingBoxT.right * scale + offsetX;
        mappedBox.top = boundingBoxT.top * scale + offsetY;
        mappedBox.right = boundingBoxT.left * scale + offsetX;
        mappedBox.bottom = boundingBoxT.bottom * scale + offsetY;

        if (isFrontMode()) {
            float centerX = overlay.getWidth() / 2.0f;
            float left = mappedBox.left;
            float right = mappedBox.right;

            mappedBox.left = centerX + (centerX - left);
            mappedBox.right = centerX - (right - centerX);
        }

        return mappedBox;
    }

    private static boolean isFrontMode() {
        return cameraSelector == CameraSelector.LENS_FACING_FRONT;
    }

    public static void toggleSelector() {
        cameraSelector = (cameraSelector == CameraSelector.LENS_FACING_BACK)
                ? CameraSelector.LENS_FACING_FRONT
                : CameraSelector.LENS_FACING_BACK;
    }

    public static Float getScale() {
        return mScale;
    }

    public static Float getOffsetX() {
        return mOffsetX;
    }

    public static Float getOffsetY() {
        return mOffsetY;
    }
}
