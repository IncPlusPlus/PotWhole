package io.github.incplusplus.potwhole;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FirebaseFunctionExample extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFunctions mFunctions;

    // Uri indicates, where the image will be picked from
    private Uri filePath;

    // bitmap for image grabbed from gallery
    private Bitmap bitmap;
    private Bitmap downloadImage;

    // request code
    private final int PICK_IMAGE_REQUEST = 22;

    private String downloadURL;
    private String reportId = "";

    private double INSERT_REPORT_LATITUDE, INSERT_REPORT_LONGITUDE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_function_example);

        Button uploadImage = findViewById(R.id.uploadImage);

        Button createPost = findViewById(R.id.createPost);

        Button getPost = findViewById(R.id.getPost);

        Button getPosts = findViewById(R.id.getPosts);

        uploadImage.setOnClickListener(
                v -> {
                    try {
                        sendImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        createPost.setOnClickListener(
                v -> {
                    createReport();
                });

        getPost.setOnClickListener(
                v -> {
                    getReport();
                });

        getPosts.setOnClickListener(
                v -> {
                    getAllReports();
                });
    }

    private void sendImage() throws IOException {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Select Image from here..."), PICK_IMAGE_REQUEST);
    }

    // Override onActivityResult method
    // Method from GeeksOfGeeks for testing upload.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                uploadImage();
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // create a reference to complete image path
        StorageReference imagesUploadRef = storageRef.child("reports/reportImages/test.jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imagesUploadRef.putBytes(data);
        uploadTask
                .addOnFailureListener(
                        exception -> {
                            // Handle unsuccessful uploads
                        })
                .addOnSuccessListener(
                        taskSnapshot -> {
                            // taskSnapshot.getMetadata() contains file metadata such as size,
                            // content-type, etc.
                            // ...
                        });

        imagesUploadRef
                .getDownloadUrl()
                .addOnSuccessListener(
                        uri -> {
                            downloadURL = uri.toString();
                            Log.v("MY_URL", downloadURL);
                            // sendToBitmap();
                        });
    }

    private void sendToBitmap() {}

    private void createReport() {

        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

        FirebaseUser currUser = mAuth.getCurrentUser();

        Log.v("This", currUser.getUid());

        // Latitude and Longitude are Numbers, not strings
        Map<String, Object> locationDevice = new HashMap<>();
        locationDevice.put("latitude", INSERT_REPORT_LATITUDE);
        locationDevice.put("longitude", INSERT_REPORT_LONGITUDE);

        Map<String, Object> data = new HashMap<>();
        data.put("title", "INSERT_REPORT_TITLE");
        data.put("image", downloadURL);
        data.put("description", "INSERT_REPORT_DESCRIPTION");
        data.put("location", locationDevice);

        Gson gson = new Gson();
        final String jsonData = gson.toJson(data);

        Log.v("REPORT_CREATE", "Creating report in database...");

        mFunctions
                .getHttpsCallable("createReportDocument")
                .call(jsonData)
                .addOnFailureListener(
                        e -> {
                            Log.v("REPORT_CREATE", "Creating Report Document Failed");
                            Log.v("REPORT_CREATE", "Exception - " + e);
                        })
                .addOnSuccessListener(
                        httpsCallableResult -> {
                            Log.v("REPORT_CREATE", "Creating Report Document Successful");
                            Log.v(
                                    "REPORT_CREATE",
                                    "Return From Database - "
                                            + httpsCallableResult.getData().toString());

                            Map<String, Object> dataFromDatabase = new HashMap<>();
                            dataFromDatabase.putAll(
                                    (Map<? extends String, ?>) httpsCallableResult.getData());

                            reportId = dataFromDatabase.get("rid").toString();

                            // Code for passing report document reference
                            // Intent intent = new Intent(FirebaseFunctionExample.this,
                            // ReportDetailPage.class);
                            // intent.putExtra("issueReference", (String)
                            // dataFromDatabase.get("reportId"));
                            // startActivity(intent);
                            // finish();
                        });
    }

    private void getReport() {

        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("rid", reportId);
        Log.v("RID", reportId);

        Gson gson = new Gson();
        final String jsonData = gson.toJson(data);

        Log.v("REPORT_GET", "Getting report from database...");

        mFunctions
                .getHttpsCallable("getReportDocument")
                .call(jsonData)
                .addOnFailureListener(
                        e -> {
                            Log.v("REPORT_GET", "Getting Report Document Failed");
                            Log.v("REPORT_GET", "Exception - " + e);
                        })
                .addOnSuccessListener(
                        httpsCallableResult -> {
                            Log.v("REPORT_GET", "Getting Report Document Successful");
                            Log.v(
                                    "REPORT_GET",
                                    "Return From Database - "
                                            + httpsCallableResult.getData().toString());

                            Map<String, Object> dataFromDatabase = new HashMap<>();
                            dataFromDatabase.putAll(
                                    (Map<? extends String, ?>) httpsCallableResult.getData());
                        });
    }

    private void getAllReports() {

        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

        Log.v("REPORT_GET", "Getting all reports from database...");

        mFunctions
                .getHttpsCallable("getAllReportDocuments")
                .call()
                .addOnFailureListener(
                        e -> {
                            Log.v("REPORT_GET", "Getting Report Document Failed");
                            Log.v("REPORT_GET", "Exception - " + e);
                        })
                .addOnSuccessListener(
                        httpsCallableResult -> {
                            Log.v("REPORT_GET", "Getting Report Document Successful");
                            Log.v(
                                    "REPORT_GET",
                                    "Return From Database - " + httpsCallableResult.getData());

                            Map<String, Object> dataFromDatabase = new HashMap<>();
                            dataFromDatabase.putAll(
                                    (Map<? extends String, ?>) httpsCallableResult.getData());
                        });
    }
}
