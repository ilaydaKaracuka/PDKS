package com.example.pdks.camera;

import android.graphics.Rect;
import android.util.Log;

import com.example.pdks.graphic.GraphicOverlay;
import com.example.pdks.graphic.RectangleOverlay;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

public class CameraAnalyzer extends BaseCameraAnalyzer<List<Face>> {

    private final GraphicOverlay overlay;
    private final FaceDetector detector;

    private static final String TAG = "CameraAnalyzer";

    public CameraAnalyzer(GraphicOverlay overlay) {
        this.overlay = overlay;

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.15f)
                .enableTracking()
                .build();

        detector = FaceDetection.getClient(options);
    }

    @Override
    public GraphicOverlay getGraphicOverlay() {
        return overlay;
    }

    @Override
    protected Task<List<Face>> detectInImage(InputImage image) {
        return detector.process(image);
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (Exception e) {
            Log.e(TAG, "stop: " + e.getMessage());
        }
    }

    @Override
    protected void onSuccess(List<Face> results, GraphicOverlay graphicOverlay, Rect rect) {
        graphicOverlay.clear();
        for (Face face : results) {
            RectangleOverlay faceGraphic = new RectangleOverlay(graphicOverlay, face, rect);
            graphicOverlay.add(faceGraphic);
        }
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(Exception e) {
        Log.e(TAG, "onFailure: " + e.getMessage());
    }
}
