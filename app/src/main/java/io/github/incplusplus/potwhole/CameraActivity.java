package io.github.incplusplus.potwhole;

import static io.github.incplusplus.potwhole.MainActivity.TAG;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    // permission related, can leave as is
    private static final String[] CAMERA_PERMISSION = new String[] {Manifest.permission.CAMERA};
    private static final String[] STORAGE_PERMISSION =
            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int CAMERA_REQUEST_CODE = 10;
    private static final int STORAGE_REQUEST_CODE = 101;
    // Bunch of classes that are needed to display a camera view
    private Executor executor;
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    Button captureButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Request permissions if necessary
        if (!hasCameraPermission() || !hasStoragePermission()) {
            requestPermission();
        }

        executor = Executors.newSingleThreadExecutor();
        captureButton = findViewById(R.id.captureButton);
        previewView = findViewById(R.id.previewView);

        // Needed for cameraX
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                            bindImageAnalysis(cameraProvider);
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                },
                ContextCompat.getMainExecutor(this));
    }

    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        // Resolution can be changed if wanted
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
        imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(this),
                new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(@NonNull ImageProxy image) {
                        image.close();
                    }
                });
        // Camera is displayed in Preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // For the capturing Image
        ImageCapture.Builder builder = new ImageCapture.Builder();

        // Picking which cam to use
        CameraSelector cameraSelector =
                new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

        // Need to set a rotation for whatever reason
        final ImageCapture imageCapture =
                builder.setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                        .build();

        // Binding all the above to ListenableFuture
        cameraProvider.bindToLifecycle(
                (LifecycleOwner) this, cameraSelector, imageAnalysis, preview, imageCapture);

        String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
        String name =
                new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis());

        File outputDir = CameraActivity.this.getCacheDir(); // context being the Activity pointer
        File outputFile = null;
        try {
            outputFile = File.createTempFile(name, ".jpg", outputDir);
        } catch (IOException e) {
            Log.e(TAG, "Failed to create temporary file for picture to be stored in.", e);
            Toast.makeText(
                            CameraActivity.this,
                            "An error occurred. Do we have storage permissions?",
                            Toast.LENGTH_SHORT)
                    .show();
            // Kick the user back to the map fragment
            Intent intent = new Intent(CameraActivity.this, MapFragment.class);
            startActivity(intent);
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");
        }

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(outputFile).build();

        captureButton.setOnClickListener(
                v -> {
                    imageCapture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(this),
                            new ImageCapture.OnImageSavedCallback() {
                                @Override
                                public void onImageSaved(
                                        @NonNull ImageCapture.OutputFileResults outputFileResults) {
                                    String msg = "Photo capture succeeded: ${output.savedUri}";
                                    //
                                    // Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                                    Log.d(TAG, msg);

                                    Intent intent =
                                            new Intent(
                                                    CameraActivity.this, ImageConfirmation.class);
                                    intent.putExtra("photoUri", outputFileResults.getSavedUri());
                                    // Keep passing the last known location through the workflow
                                    intent.putExtra(
                                            "location",
                                            (Parcelable)
                                                    getIntent().getParcelableExtra("location"));
                                    startActivity(intent);
                                }

                                @Override
                                public void onError(@NonNull ImageCaptureException exception) {
                                    Log.e(TAG, "Photo capture failed: ${exc.message}", exception);
                                }
                            });
                });
    }

    // StackOverflow function for converting the taken Image to a bitmap
    private Bitmap toBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        buffer.rewind();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        byte[] clonedBytes = bytes.clone();
        return BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);
    }

    // check camera perms
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    // check storage perms
    private boolean hasStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    // request permission
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, CAMERA_REQUEST_CODE);
        ActivityCompat.requestPermissions(this, STORAGE_PERMISSION, STORAGE_REQUEST_CODE);
    }
}
