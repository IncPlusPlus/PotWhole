package io.github.incplusplus.potwhole;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

public class ImageConfirmation extends AppCompatActivity {

    ImageView displayImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_image_preview);

        Uri photoUri = getIntent().getParcelableExtra("photoUri");

        Intent intent = getIntent();

        ExifInterface ei = null;
        try {
            ei = new ExifInterface(photoUri.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation =
                ei.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap bitmap = BitmapFactory.decodeFile(photoUri.getPath());
        Bitmap rotatedBitmap = null;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }

        Button yes = findViewById(R.id.useImage);
        Button no = findViewById(R.id.retakeImage);

        ImageView displayImage = findViewById(R.id.imagePreview);
        displayImage.setImageBitmap(rotatedBitmap);

        yes.setOnClickListener(
                v -> {
                    /*Intent intent1 = new Intent(ImageConfirmation.this, CameraActivity.class);
                    startActivity(intent1); */
                    // something magical happens
                });
        no.setOnClickListener(
                v -> {
                    Intent intent2 = new Intent(ImageConfirmation.this, CameraActivity.class);
                    startActivity(intent2);
                });
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // Could result in OOM error for large bitmaps as two will exist in memory at once
        return Bitmap.createBitmap(
                source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
