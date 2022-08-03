package io.github.incplusplus.potwhole;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.functions.FirebaseFunctions;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "PotWhole";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            Fragment mapFragment = new MapFragment();
            transaction.replace(R.id.container, mapFragment);
            transaction.commit();
        }

        NavigationBarView navBar = findViewById(R.id.bottom_navigation);
        navBar.setOnItemSelectedListener(
                item -> {
                    int itemId = item.getItemId();
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    /*
                     * A switch would be lovely here but would result in "Resource IDs will be
                     * non-final by default in Android Gradle Plugin version 8.0, avoid using them
                     * in switch case statements". As such, we're required to use an if/else block.
                     */
                    if (itemId == R.id.page_map) {
                        Fragment mapFragment = new MapFragment();
                        transaction.replace(R.id.container, mapFragment);
                        transaction.commit();
                        return true;
                    } else if (itemId == R.id.page_reports) {
                        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

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
                                            Log.v(
                                                    "REPORT_GET",
                                                    "Getting Report Document Successful");
                                            Log.v(
                                                    "REPORT_GET",
                                                    "Return From Database - "
                                                            + httpsCallableResult.getData());
                                            ArrayList a;
                                            a = (ArrayList) httpsCallableResult.getData();

                                            Bundle bundle = new Bundle();
                                            bundle.putStringArrayList("data", a);

                                            Fragment ReportsFragment = new ReportsFragment();
                                            ReportsFragment.setArguments(bundle);
                                            transaction.replace(R.id.container, ReportsFragment);
                                            transaction.commit();
                                        });
                        return true;
                    } else if (itemId == R.id.page_my_reports) {
                        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

                        Log.v("REPORT_GET", "Getting all reports from database...");

                        mFunctions
                                .getHttpsCallable("getUserReports")
                                .call()
                                .addOnFailureListener(
                                        e -> {
                                            Log.v("REPORT_GET", "Getting Report Document Failed");
                                            Log.v("REPORT_GET", "Exception - " + e);
                                        })
                                .addOnSuccessListener(
                                        httpsCallableResult -> {
                                            Log.v(
                                                    "REPORT_GET",
                                                    "Getting Report Document Successful");
                                            Log.v(
                                                    "REPORT_GET",
                                                    "Return From Database - "
                                                            + httpsCallableResult.getData());
                                            ArrayList a;
                                            a = (ArrayList) httpsCallableResult.getData();

                                            Bundle bundle = new Bundle();
                                            bundle.putStringArrayList("data", a);

                                            Fragment myReportsFragment = new MyReportsFragment();
                                            myReportsFragment.setArguments(bundle);
                                            transaction.replace(R.id.container, myReportsFragment);
                                            transaction.commit();
                                        });
                        return true;

                    } else if (itemId == R.id.page_my_account) {
                        Fragment myAccountFragment = new MyAccountFragment();
                        transaction.replace(R.id.container, myAccountFragment);
                        transaction.commit();
                        return true;
                    } else {
                        return false;
                    }
                });
    }
}
