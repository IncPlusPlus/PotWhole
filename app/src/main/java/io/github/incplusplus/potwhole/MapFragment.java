package io.github.incplusplus.potwhole;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * A simple {@link Fragment} subclass. Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

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
    private SupportMapFragment supportMapFragment;
    private Context context;

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
                        context.getApplicationContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(
                    getActivity(),
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
                        getActivity(),
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
    /** Updates map to be set to current location and enable my location button. */
    private void updateLocationUI() {
        try {
            // check location permissions
            if (ActivityCompat.checkSelfPermission(
                                    context, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                                    context, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
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

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        this.context = getActivity();
        SupportMapFragment supportMapFragment = createSupportMapFragment();
        supportMapFragment.getMapAsync(this);

        FloatingActionButton button = container.findViewById(R.id.new_report_button);
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO add camera functionality
                    }
                });

        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.map_fragment, supportMapFragment)
                .commit();
        return view;
    }
}
