package io.github.incplusplus.potwhole;

import static io.github.incplusplus.potwhole.MainActivity.TAG;
import static io.github.incplusplus.potwhole.util.ImageFunctions.loadImage;

import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
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
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class NewReportForm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_report_form);

        /*
         * The image from the camera activity should've been saved to a temporary file like so:
         * https://stackoverflow.com/a/6485850/1687436 and we'll read it in from that path.
         * This is because passing the raw Image or the image as a Bitmap will make the Bundle
         * bigger than 1 MB, resulting in an error. We need storage permissions for this.
         *
         * Here's one way we could save the ImageProxy to a temp file:
         * https://stackoverflow.com/a/71797946/1687436.
         */
        Location lastKnownLocation = getIntent().getParcelableExtra("location");
        Uri photoUri = getIntent().getParcelableExtra("photoUri");
        Bitmap bitmap = null;
        try {
            bitmap = loadImage(photoUri.getPath());
        } catch (IOException e) {
            Log.e(TAG, "Failed to load saved image", e);
            Toast.makeText(this, "Failed to load saved image.", Toast.LENGTH_SHORT).show();
        }

        ImageView previewImage = findViewById(R.id.new_report_image_preview);
        previewImage.setImageBitmap(bitmap);

        EditText title = findViewById(R.id.title_box);
        EditText description = findViewById(R.id.description_box);

        Button submitButton = findViewById(R.id.submit_report_button);
        // Variable used in lambda expression should be final or effectively final
        Bitmap finalBitmap = bitmap;
        submitButton.setOnClickListener(
                v ->
                        submitReport(
                                lastKnownLocation,
                                title.getText().toString(),
                                description.getText().toString(),
                                photoUri.getLastPathSegment(),
                                finalBitmap));
    }

    private void submitReport(
            Location location, String title, String description, String imageName, Bitmap picture) {
        uploadImage(picture, imageName, location, title, description);
    }

    private void uploadImage(
            Bitmap bitmap,
            String fileName,
            Location lastKnownLocation,
            String title,
            String description) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // create a reference to complete image path
        StorageReference imagesUploadRef = storageRef.child("reports/reportImages/" + fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imagesUploadRef.putBytes(data);
        uploadTask
                .addOnFailureListener(
                        exception -> {
                            Log.e(TAG, "Failed to upload image.", exception);
                            // Handle unsuccessful uploads
                        })
                .addOnSuccessListener(
                        taskSnapshot -> {
                            Log.v(TAG, "Upload task completed successfully");
                            imagesUploadRef
                                    .getDownloadUrl()
                                    .addOnFailureListener(
                                            exception -> {
                                                Log.e(
                                                        TAG,
                                                        "Failed to get uploaded image URL.",
                                                        exception);
                                            })
                                    .addOnSuccessListener(
                                            uri -> {
                                                String downloadURL = uri.toString();
                                                Log.v("MY_URL", downloadURL);
                                                createReport(
                                                        lastKnownLocation,
                                                        downloadURL,
                                                        title,
                                                        description);
                                            });
                        });
    }

    private void createReport(
            Location lastKnownLocation, String imageUrl, String title, String description) {

        FirebaseAuth mAuth;
        FirebaseFunctions mFunctions;

        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

        FirebaseUser currUser = mAuth.getCurrentUser();

        Log.v("This", currUser.getUid());

        // Latitude and Longitude are Numbers, not strings
        Map<String, Object> locationDevice = new HashMap<>();
        locationDevice.put("latitude", lastKnownLocation.getLatitude());
        locationDevice.put("longitude", lastKnownLocation.getLongitude());

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("image", imageUrl);
        data.put("description", description);
        data.put("location", locationDevice);
        data.put("timestamp", ZonedDateTime.now().toString());

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
                        });
    }
}
