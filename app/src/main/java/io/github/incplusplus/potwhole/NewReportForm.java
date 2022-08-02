package io.github.incplusplus.potwhole;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;

public class NewReportForm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_report_form);

        Bundle bundle = this.getIntent().getExtras();

        /*
         * The image from the camera activity should've been saved to a temporary file like so:
         * https://stackoverflow.com/a/6485850/1687436 and we'll read it in from that path.
         * This is because passing the raw Image or the image as a Bitmap will make the Bundle
         * bigger than 1 MB, resulting in an error. We might need storage permissions for this.
         *
         * Here's one way we could save the ImageProxy to a temp file:
         * https://stackoverflow.com/a/71797946/1687436.
         */
        File imageFile = new File(bundle.getString("imagePath"));
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());

        ImageView previewImage = findViewById(R.id.new_report_image_preview);
        previewImage.setImageBitmap(bitmap);

        EditText description = findViewById(R.id.description_box);

        Button submitButton = findViewById(R.id.submit_report_button);
        submitButton.setOnClickListener(v -> submitReport(imageFile, description.getText().toString()));
    }

    private void submitReport(File imageFile, String description) {

    }
}
