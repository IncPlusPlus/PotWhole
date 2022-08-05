package io.github.incplusplus.potwhole;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportItemAdapter extends ArrayAdapter<ReportItem> {
    private LayoutInflater inflater;
    private Location lastKnownLocation;
    Bitmap bitmap;
    ImageView image;
    String urlImage;
    Location reportLocation;

    public ReportItemAdapter(
            @NonNull Context context,
            int resource,
            List<ReportItem> list,
            Location lastKnownLocation) {
        super(context, resource, list);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.lastKnownLocation = lastKnownLocation;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the report item
        ReportItem item = getItem(position);
        urlImage = item.getImageUrl();
        LatLng reportLatLng = item.getCoordinates();
        Log.d(String.valueOf(reportLatLng), "report location");
        reportLocation = new Location("reportLocation");
        reportLocation.setLatitude(reportLatLng.latitude);
        reportLocation.setLongitude(reportLatLng.longitude);

        Log.d(String.valueOf(lastKnownLocation), "last location");

        // Use the report_item layout to create a view
        View view = inflater.inflate(R.layout.report_item, null);

        // Link the image bitmap to the ImageView element
        image = view.findViewById(R.id.report_image);
        new GetImageFromUrl(image).execute(urlImage);

        TextView title = view.findViewById(R.id.report_title);
        title.setText(
                ZonedDateTime.parse(item.getTimeSubmitted())
                        .format(DateTimeFormatter.ISO_LOCAL_DATE));

        TextView secondaryText = view.findViewById(R.id.report_secondary_text);
        // TODO: Make some sort of way to show the distance from the last recorded GPS coordinates
        if (lastKnownLocation != null) {
            float distance = reportLocation.distanceTo(lastKnownLocation);
            float distance_miles = (float) (distance / 1609.344);

            secondaryText.setText(String.format("%.2f miles away", distance_miles));
        } else {
            secondaryText.setText("unable to get distance from report");
        }

        TextView supportingText = view.findViewById(R.id.report_supporting_text);
        // TODO: Truncate and add an ellipse after a certain # of chars.
        supportingText.setText(item.getDescription());

        return view;
    }

    public class GetImageFromUrl extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public GetImageFromUrl(ImageView img) {
            this.imageView = img;
        }

        @Override
        protected Bitmap doInBackground(String... url) {
            String stringUrl = url[0];
            bitmap = null;
            InputStream inputStream;
            try {
                inputStream = new java.net.URL(stringUrl).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imageView.setImageBitmap(bitmap);
        }
    }
}
