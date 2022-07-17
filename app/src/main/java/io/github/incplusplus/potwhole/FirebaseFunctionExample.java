package io.github.incplusplus.potwhole;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class FirebaseFunctionExample extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFunctions mFunctions;

    private String reportId = "";

    private double INSERT_REPORT_LATITUDE, INSERT_REPORT_LONGITUDE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_function_example);

        Button createPost = findViewById(R.id.createPost);

        Button getPost = findViewById(R.id.getPost);

        createPost.setOnClickListener(
                v -> {
                    createReport();
                });

        getPost.setOnClickListener(
                v -> {
                    getReport();
                });
    }

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
}
