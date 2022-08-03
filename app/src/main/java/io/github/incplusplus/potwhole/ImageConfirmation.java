package io.github.incplusplus.potwhole;

import static io.github.incplusplus.potwhole.MainActivity.TAG;
import static io.github.incplusplus.potwhole.util.ImageFunctions.loadImage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

public class ImageConfirmation extends AppCompatActivity {

    ImageView displayImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_image_preview);

        Uri photoUri = getIntent().getParcelableExtra("photoUri");

        Bitmap bitmap = null;
        try {
            bitmap = loadImage(photoUri.getPath());
        } catch (IOException e) {
            Log.e(TAG, "Failed to load saved image", e);
            Toast.makeText(this, "Failed to load saved image.", Toast.LENGTH_SHORT).show();
        }

        Button yes = findViewById(R.id.useImage);
        Button no = findViewById(R.id.retakeImage);

        ImageView displayImage = findViewById(R.id.imagePreview);
        displayImage.setImageBitmap(bitmap);

        yes.setOnClickListener(
                v -> {
                    Intent confirmedIntent =
                            new Intent(ImageConfirmation.this, NewReportForm.class);
                    confirmedIntent.putExtra("photoUri", photoUri);
                    // Finally pass the last known location to the new report form
                    confirmedIntent.putExtra(
                            "location", (Parcelable) getIntent().getParcelableExtra("location"));
                    startActivity(confirmedIntent);
                });
        no.setOnClickListener(
                v -> {
                    Intent retakeIntent = new Intent(ImageConfirmation.this, CameraActivity.class);
                    startActivity(retakeIntent);
                });
    }
}
