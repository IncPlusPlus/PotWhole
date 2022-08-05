package io.github.incplusplus.potwhole;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyReportsFragment extends Fragment {

    private List<ReportItem> reports = new ArrayList<>();

    private ArrayList<Map<String, String>> convert = new ArrayList<>();
    private ArrayList<Map<String, Map<String, Object>>> convertLoc = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;

    private void getLastLocation(Activity activity) {
        try {
            // location permission allowed
            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();

            locationResult.addOnCompleteListener(
                    activity,
                    task -> {
                        if (task.isSuccessful()) {
                            lastKnownLocation = task.getResult();
                        }
                    });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());
        getLastLocation(requireActivity());
        convert = (ArrayList) getArguments().getStringArrayList("data");
        convertLoc = (ArrayList) getArguments().getStringArrayList("data");

        for (int i = 0; i < convert.size(); i++) {
            double latitude = (double) convertLoc.get(i).get("location").get("latitude");
            double longitude = (double) convertLoc.get(i).get("location").get("longitude");

            reports.add(
                    new ReportItem(
                            convert.get(i).get("title"),
                            convert.get(i).get("timestamp"),
                            convert.get(i).get("originalReporterUsername"),
                            convert.get(i).get("description"),
                            new LatLng(latitude, longitude),
                            convert.get(i).get("image")));
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_reports, container, false);

        Task<Location> result = fusedLocationProviderClient.getLastLocation();
        result.addOnCompleteListener(
                requireActivity(),
                task -> {
                    if (task.isSuccessful()) {
                        ReportItemAdapter adapter =
                                new ReportItemAdapter(
                                        inflater.getContext(), 0, reports, lastKnownLocation);
                        ListView listView = rootView.findViewById(R.id.my_reports_view);
                        listView.setAdapter(adapter);
                    } else {
                        ReportItemAdapter adapter =
                                new ReportItemAdapter(inflater.getContext(), 0, reports, null);
                        ListView listView = rootView.findViewById(R.id.my_reports_view);
                        listView.setAdapter(adapter);
                    }
                });

        return rootView;
    }
}
