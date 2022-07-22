package io.github.incplusplus.potwhole;

import static io.github.incplusplus.potwhole.MainActivity.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportItemAdapter extends ArrayAdapter<ReportItem> {
    private LayoutInflater inflater;

    public ReportItemAdapter(@NonNull Context context, int resource, List<ReportItem> list) {
        super(context, resource, list);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the report item
        ReportItem item = getItem(position);

        // Use the report_item layout to create a view
        View view = inflater.inflate(R.layout.report_item, null);

        // Link the image bitmap to the ImageView element
        ImageView imageView = view.findViewById(R.id.report_image);
        // Set a default image to use if the download fails
        Bitmap image =
                BitmapFactory.decodeResource(
                        getContext().getResources(),
                        R.drawable.ic_exclamation_fill0_wght400_grad0_opsz48);
        try {
            URL url = new URL(item.getImageUrl());
            // Download the image from the provided URL and put it in a Bitmap object
            image = BitmapFactory.decodeStream(url.openStream());
        } catch (IOException e) {
            Log.e(TAG, "Failed to get image for report " + item.getId(), e);
        }

        imageView.setImageBitmap(image);

        TextView title = view.findViewById(R.id.report_title);
        title.setText(item.getTimeSubmitted().format(DateTimeFormatter.ISO_LOCAL_DATE));

        TextView secondaryText = view.findViewById(R.id.report_secondary_text);
        // TODO: Make some sort of way to show the distance from the last recorded GPS coordinates
        secondaryText.setText("0.51 mi away");

        TextView supportingText = view.findViewById(R.id.report_supporting_text);
        // TODO: Truncate and add an ellipse after a certain # of chars.
        supportingText.setText(item.getDescription());

        return view;
    }
}
