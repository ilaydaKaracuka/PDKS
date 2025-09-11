package com.example.pdks.camera;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.pdks.graphic.GraphicOverlay;
import com.example.pdks.utils.CameraUtils;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraManager {

    private final Context context;
    private final PreviewView previewView;
    private final GraphicOverlay graphicOverlay;
    private final LifecycleOwner lifecycleOwner;

    private Preview preview;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    private ImageAnalysis imageAnalysis;

    private static final String TAG = "CameraManager";
    public static int cameraOption = CameraSelector.LENS_FACING_FRONT;

    public CameraManager(Context context,
                         PreviewView previewView,
                         GraphicOverlay graphicOverlay,
                         LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.previewView = previewView;
        this.graphicOverlay = graphicOverlay;
        this.lifecycleOwner = lifecycleOwner;
    }

    public void cameraStart() {
        ListenableFuture<ProcessCameraProvider> cameraProcessProvider =
                ProcessCameraProvider.getInstance(context);

        cameraProcessProvider.addListener(() -> {
            try {
                cameraProvider = cameraProcessProvider.get();

                preview = new Preview.Builder().build();

                imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, new CameraAnalyzer(graphicOverlay));

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraOption)
                        .build();

                setCameraConfig(cameraProvider, cameraSelector);

            } catch (Exception e) {
                Log.e(TAG, "Error starting camera: " + e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void setCameraConfig(@NonNull ProcessCameraProvider cameraProvider,
                                 @NonNull CameraSelector cameraSelector) {
        try {
            cameraProvider.unbindAll();

            camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
            );

            preview.setSurfaceProvider(previewView.getSurfaceProvider());

        } catch (Exception e) {
            Log.e(TAG, "setCameraConfig: " + e);
        }
    }

    public void changeCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraOption = (cameraOption == CameraSelector.LENS_FACING_BACK)
                    ? CameraSelector.LENS_FACING_FRONT
                    : CameraSelector.LENS_FACING_BACK;
            CameraUtils.toggleSelector();
            cameraStart();
        }
    }

    public void cameraStop() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}
