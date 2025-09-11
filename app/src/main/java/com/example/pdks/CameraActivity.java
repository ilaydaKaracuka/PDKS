package com.example.pdks;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int TIMEOUT_MS = 30000;
    private static final int DETECTION_INTERVAL_MS = 2000;

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private Handler timeoutHandler;
    private boolean faceDetected = false;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.viewCameraPreview);
        cameraExecutor = Executors.newSingleThreadExecutor();
        timeoutHandler = new Handler();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST
            );
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        imageCapture = new ImageCapture.Builder().build();

        androidx.camera.core.Preview preview = new androidx.camera.core.Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture
        );

        startTime = System.currentTimeMillis();

        previewView.postDelayed(this::detectHumanLoop, 3000);
    }

    private void detectHumanLoop() {
        if (faceDetected) return;
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > TIMEOUT_MS) {
            Toast.makeText(CameraActivity.this, "İnsan algılanamadı!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (imageCapture != null) {
            imageCapture.takePicture(ContextCompat.getMainExecutor(this),
                    new ImageCapture.OnImageCapturedCallback() {
                        @Override
                        public void onCaptureSuccess(@NonNull androidx.camera.core.ImageProxy imageProxy) {
                            Bitmap bitmap = previewView.getBitmap();
                            if (bitmap != null) {
                                InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
                                FaceDetectorOptions options =
                                        new FaceDetectorOptions.Builder()
                                                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                                                .build();
                                FaceDetector detector = FaceDetection.getClient(options);

                                detector.process(inputImage)
                                        .addOnSuccessListener(faces -> {
                                            if (faces.size() > 0) {
                                                faceDetected = true;
                                                goBackToMain(bitmap);
                                            } else {
                                                previewView.postDelayed(CameraActivity.this::detectHumanLoop, DETECTION_INTERVAL_MS);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            previewView.postDelayed(CameraActivity.this::detectHumanLoop, DETECTION_INTERVAL_MS);
                                        });
                            }
                            imageProxy.close();
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            previewView.postDelayed(CameraActivity.this::detectHumanLoop, DETECTION_INTERVAL_MS);
                        }
                    });
        }
    }

    private void goBackToMain(Bitmap bitmap) {
        ImageStorage.setBitmap(bitmap);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera Permission Denied!", Toast.LENGTH_SHORT).show();
        }
    }
}
