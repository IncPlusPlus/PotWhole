package io.github.incplusplus.potwhole;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Objects;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "Map";
    private static final int DEFAULT_ZOOM = 17;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private LatLng locationLatLng;
    private SupportMapFragment supportMapFragment;
    private Context context;
    private LocationRequest locationRequest;

    public MapFragment() {}

    @NonNull
    private SupportMapFragment createSupportMapFragment() {
        GoogleMapOptions mapOptions = configMap();
        // check permissions
        getLocationPermission();
        this.fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(mapOptions);
        mapFragment.getMapAsync(this);
        return mapFragment;
    }

    /** Requests for permissions if they are not already allowed */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                        requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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
                        requireActivity(),
                        task -> {
                            if (task.isSuccessful()) {
                                // Set the map's camera position to the current location of the
                                // device.
                                lastKnownLocation = task.getResult();
                                if (lastKnownLocation != null) {

                                    locationLatLng =
                                            new LatLng(
                                                    lastKnownLocation.getLatitude(),
                                                    lastKnownLocation.getLongitude());
                                    map.moveCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                    locationLatLng, DEFAULT_ZOOM));
                                }
                            } else {
                                // location permission not allowed
                                Log.d(TAG, "Current location is null");
                                Log.e(TAG, "Exception: %s", task.getException());
                                map.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                                new LatLng(0, 0), DEFAULT_ZOOM));
                                map.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                        });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /** Updates map to be set to current location and enable my location button. */
    private void updateLocationUI() {
        try {
            // check location permissions
            if (requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED
                    && requireActivity()
                                    .checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                Log.d("Location permission allowed", TAG);
                // enable location
                map.setMyLocationEnabled(true);

                // enable button
                map.getUiSettings().setMyLocationButtonEnabled(true);
            }
            // if not allowed
            else {
                Log.d("Location permission not allowed", TAG);
                // disable location
                map.setMyLocationEnabled(false);
                // disable button
                map.getUiSettings().setMyLocationButtonEnabled(false);
                // cannot get location, so location is null
                this.lastKnownLocation = null;
                Log.d("lastKnowLocation is null", TAG);

                // request permissions
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
        }
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

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates(
            LocationCallback locationCallback, LocationRequest locationRequest) {
        if (locationPermissionGranted) {
            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.myLooper());
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
    /*
    Called in the LocationCallback when a new location is returned
     */
    private void onNewLocation(@NonNull Location location) {
        // Only move the camera when the new location is more than 5 meters from the old one
        if (location.distanceTo(lastKnownLocation) >= 5) {
            lastKnownLocation = location;
            locationLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            // if callback is run before map is ready, map will be null
            if (map != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, DEFAULT_ZOOM));
            }
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        this.context = getActivity();
        if (this.supportMapFragment == null) {
            this.supportMapFragment = createSupportMapFragment();
        }

        this.supportMapFragment.getMapAsync(this);
        Log.d(String.valueOf(locationLatLng), TAG);

        // Callback for Updating Location
        LocationCallback locationCallback =
                new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        Log.d("Location callback called", TAG);
                        onNewLocation(Objects.requireNonNull(locationResult.getLastLocation()));
                    }
                };

        createLocationRequest();
        startLocationUpdates(locationCallback, locationRequest);

        FloatingActionButton button = view.findViewById(R.id.new_report_button);
        button.setOnClickListener(
                v -> {
                    Intent intent = new Intent(context, CameraActivity.class);
                    // Pass the last known location down through the image capture workflow
                    intent.putExtra("location", lastKnownLocation);
                    startActivity(intent);
                });

        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.map_fragment, supportMapFragment)
                .commit();
        return view;
    }
}
