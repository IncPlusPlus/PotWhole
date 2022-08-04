package io.github.incplusplus.potwhole;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportItemAdapter extends ArrayAdapter<ReportItem> {
    private LayoutInflater inflater;
    Bitmap bitmap;
    ImageView image;
    String urlImage;

    public ReportItemAdapter(@NonNull Context context, int resource, List<ReportItem> list) {
        super(context, resource, list);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the report item
        ReportItem item = getItem(position);
        urlImage = item.getImageUrl();

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
        secondaryText.setText("0.51 mi away");

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
