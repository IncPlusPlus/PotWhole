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
import java.util.Properties;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "Map";
    private static final int DEFAULT_ZOOM = 17;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    /*
     *  Lat Long of Wentworth Hall
     *   Degrees: 42°20'11"N 71°05'41"W
     *   Decimal: 42.336389 lat  71.094722 long
     */
    private static final LatLng defaultLocation = new LatLng(42.336389, 71.094722);
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = getSupportMapFragment();
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
                        transaction.replace(R.id.container, getSupportMapFragment());
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

    @NonNull
    private SupportMapFragment getSupportMapFragment() {
        GoogleMapOptions mapOptions = configMap();

        // check permissions
        getLocationPermission();
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = SupportMapFragment.newInstance(mapOptions);
        getSupportFragmentManager().beginTransaction().add(R.id.container, mapFragment).commit();

        mapFragment.getMapAsync(this);
        return mapFragment;
    }

    /***
     * Configure a mapOptions object for initializing a map
     * @return mapOptions
     */
    private GoogleMapOptions configMap() {
        GoogleMapOptions mapOptions = new GoogleMapOptions();
        mapOptions.mapType(GoogleMap.MAP_TYPE_NORMAL);
        mapOptions.ambientEnabled(true);
        mapOptions.compassEnabled(true);
        mapOptions.zoomGesturesEnabled(true);
        mapOptions.scrollGesturesEnabledDuringRotateOrZoom(true);
        mapOptions.scrollGesturesEnabled(true);

        return mapOptions;
    }

    /**
     * Reads a property from local.properties file given a key
     *
     * @param key, key of a value in local.properties
     * @return String, value corresponding to the key, if it is not found returns empty string
     */
    private String getProperty(String key) {
        try {
            Properties properties = new Properties();
            FileInputStream inputStream = new FileInputStream("local.properties");
            properties.load(inputStream);
            return properties.getProperty(key);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Properties file not found");
            return "";
        } catch (IOException e) {
            Log.e(TAG, "Error reading from properties files");
            return "";
        }
    }

    /**
     * Loads when map is ready to be used
     *
     * @param googleMap, GoogleMap object to set as the map in this class
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        updateLocationUI();
        getDeviceLocation();
    }
    /** Updates map to be set to current location and enable my location button. */
    private void updateLocationUI() {
        try {
            // check location permissions
            if (locationPermissionGranted) {
                // enable location
                map.setMyLocationEnabled(true);

                // enable button
                map.getUiSettings().setMyLocationButtonEnabled(true);
            }
            // if not allowed
            else {
                // disable location
                map.setMyLocationEnabled(false);
                // disable button
                map.getUiSettings().setMyLocationButtonEnabled(false);
                // cannot get location, so location is null
                this.lastKnownLocation = null;

                // request permissions
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
        }
    }
    /** Gets the most recent location of the device */
    private void getDeviceLocation() {
        // Get the most recent location of the phone
        try {
            if (locationPermissionGranted) {
                // location permission allowed
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(
                        this,
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful()) {
                                    // Set the map's camera position to the current location of the
                                    // device.
                                    lastKnownLocation = task.getResult();
                                    if (lastKnownLocation != null) {
                                        map.moveCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                        new LatLng(
                                                                lastKnownLocation.getLatitude(),
                                                                lastKnownLocation.getLongitude()),
                                                        DEFAULT_ZOOM));
                                    }
                                } else {
                                    // location permission not allowed
                                    Log.d(TAG, "Current location is null");
                                    Log.e(TAG, "Exception: %s", task.getException());
                                    map.moveCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                    defaultLocation, DEFAULT_ZOOM));
                                    map.getUiSettings().setMyLocationButtonEnabled(false);
                                }
                            }
                        });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
    /** Requests for permissions if they are not already allowed */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                        this.getApplicationContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    /**
     * Handles results from the request permission pop up
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) { // If request is cancelled, the
            // result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }
}
