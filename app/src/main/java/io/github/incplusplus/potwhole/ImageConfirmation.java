package io.github.incplusplus.potwhole;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ImageConfirmation extends AppCompatActivity {

    ImageView displayImage;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_image_preview);

        Intent intent = getIntent();
        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("BitmapImage");

        Button yes = findViewById(R.id.useImage);
        Button no = findViewById(R.id.retakeImage);

        ImageView displayImage = findViewById(R.id.imagePreview);
        displayImage.setImageBitmap(bitmap);

        yes.setOnClickListener(v->{
            /*Intent intent1 = new Intent(ImageConfirmation.this, CameraActivity.class);
            startActivity(intent1); */
            //something magical happens
        });
        no.setOnClickListener(v->{
            Intent intent2 = new Intent(ImageConfirmation.this, CameraActivity.class);
            startActivity(intent2);
        });

    }
}
