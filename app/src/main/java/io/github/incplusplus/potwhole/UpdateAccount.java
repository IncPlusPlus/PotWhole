package io.github.incplusplus.potwhole;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class UpdateAccount extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFunctions mFunctions;

    private EditText editUsername;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_account);

        editUsername = findViewById(R.id.updateUsername);

        Button updateAccount = findViewById(R.id.update_account_button);

        updateAccount.setOnClickListener(
                v -> {
                    sendUpdateAccount();
                });
    }

    private void sendUpdateAccount() {
        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("username", editUsername.getText().toString());
        Log.v("USERNAME", editUsername.getText().toString());

        Gson gson = new Gson();
        final String jsonData = gson.toJson(data);

        Log.v("EDIT_ACCOUNT", "Editing account in database...");

        mFunctions
                .getHttpsCallable("updateUserDocument")
                .call(jsonData)
                .addOnFailureListener(
                        e -> {
                            Log.v("EDIT_ACCOUNT", "Updating User Document Failed");
                            Log.v("EDIT_ACCOUNT", "Exception - " + e);
                        })
                .addOnSuccessListener(
                        httpsCallableResult -> {
                            Log.v("EDIT_ACCOUNT", "Getting Report Document Successful");
                            Log.v(
                                    "EDIT_ACCOUNT",
                                    "Return From Database - "
                                            + httpsCallableResult.getData().toString());

                            Map<String, Object> dataFromDatabase = new HashMap<>();
                            dataFromDatabase.putAll(
                                    (Map<? extends String, ?>) httpsCallableResult.getData());

                            Intent intent = new Intent(this, FirebaseFunctionExample.class);
                            startActivity(intent);
                        });
    }
}
