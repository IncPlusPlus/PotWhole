package io.github.incplusplus.potwhole;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyReportsFragment extends Fragment {

    private List<ReportItem> reports = new ArrayList<>();

    private ArrayList<Map<String, String>> convert = new ArrayList<>();
    private ArrayList<Map<String, Map<String, Object>>> convertLoc = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        convert = (ArrayList) getArguments().getStringArrayList("data");
        convertLoc = (ArrayList) getArguments().getStringArrayList("data");

        for (int i = 0; i < convert.size(); i++) {
            String coordinates =
                    convertLoc.get(i).get("location").get("latitude").toString()
                            + ' '
                            + convertLoc.get(i).get("location").get("longitude").toString();
            reports.add(
                    new ReportItem(
                            convert.get(i).get("title"),
                            convert.get(i).get("timestamp"),
                            convert.get(i).get("originalReporterUsername"),
                            convert.get(i).get("description"),
                            coordinates,
                            convert.get(i).get("image")));
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_reports, container, false);

        ReportItemAdapter adapter = new ReportItemAdapter(inflater.getContext(), 0, reports);

        ListView listView = rootView.findViewById(R.id.my_reports_view);
        listView.setAdapter(adapter);
        return rootView;
    }
}
