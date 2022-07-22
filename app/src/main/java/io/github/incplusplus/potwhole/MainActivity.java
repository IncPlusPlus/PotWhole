package io.github.incplusplus.potwhole;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null){
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            Fragment mapFragment = new MapFragment();
            transaction. replace(R.id.container, mapFragment);
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
                        Fragment reportsFragment = new ReportsFragment();
                        transaction.replace(R.id.container, reportsFragment);
                        transaction.commit();
                        return true;
                    } else if (itemId == R.id.page_my_reports) {
                        Fragment myReportsFragment = new MyReportsFragment();
                        transaction.replace(R.id.container, myReportsFragment);
                        transaction.commit();
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
