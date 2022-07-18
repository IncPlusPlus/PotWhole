package io.github.incplusplus.potwhole;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportsFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_reports, container, false);

        List<ReportItem> reports = new ArrayList<>();
        reports.add(
                new ReportItem(
                        "r1",
                        ZonedDateTime.now(),
                        "person1",
                        "Big ol' gash in the road.",
                        "some coords",
                        ""));
        reports.add(
                new ReportItem(
                        "r2",
                        ZonedDateTime.now().plusHours(5),
                        "person2",
                        "Hole in the middle of the left lane",
                        "some coords",
                        ""));
        reports.add(
                new ReportItem(
                        "r3",
                        ZonedDateTime.now().plusDays(2),
                        "person3",
                        "Uhhh... Huntington Ave is no more.",
                        "some coords",
                        ""));

        ReportItemAdapter adapter = new ReportItemAdapter(inflater.getContext(), 0, reports);

        ListView listView = rootView.findViewById(R.id.reports_view);
        listView.setAdapter(adapter);
        return rootView;
    }
}
