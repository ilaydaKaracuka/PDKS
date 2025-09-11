package com.example.pdks.camera;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.pdks.graphic.GraphicOverlay;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;

import java.util.List;

public abstract class BaseCameraAnalyzer<T extends List<Face>> implements ImageAnalysis.Analyzer {

    public abstract GraphicOverlay getGraphicOverlay();

    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        try {
            if (imageProxy.getImage() == null) {
                imageProxy.close();
                return;
            }

            InputImage image = InputImage.fromMediaImage(
                    imageProxy.getImage(),
                    imageProxy.getImageInfo().getRotationDegrees()
            );

            detectInImage(image)
                    .addOnSuccessListener(results -> {
                        onSuccess(results, getGraphicOverlay(), imageProxy.getCropRect());
                        imageProxy.close();
                    })
                    .addOnFailureListener(e -> {
                        onFailure(e);
                        imageProxy.close();
                    });

        } catch (Exception e) {
            Log.e("BaseCameraAnalyzer", "Analyze error: " + e.getMessage());
            imageProxy.close();
        }
    }

    protected abstract Task<T> detectInImage(InputImage image);

    public abstract void stop();

    protected abstract void onSuccess(T results, GraphicOverlay graphicOverlay, Rect rect);

    protected abstract void onFailure(Exception e);
}
