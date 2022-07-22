package io.github.incplusplus.potwhole;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterAccountPage extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText editTextEmail, editTextPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_register_account);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        Button RegisterAccountButton = findViewById(R.id.Register_Account_button);

        RegisterAccountButton.setOnClickListener(v -> createUserAccount());
    }

    private void createUserAccount() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmail.setError("Please Enter an Email Address");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please Enter a Valid Email Address");
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextEmail.setError("Please Enter a Password");
            editTextEmail.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(
                        authResult -> {
                            Log.v("MyApp", "User Account is Created");
                            Intent intent =
                                    new Intent(RegisterAccountPage.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        })
                .addOnFailureListener(
                        e -> {

                            // If sign in fails, display a message to the user.
                            Log.w("AUTH_INFO", "createUserWithEmail:failure", e);
                            // Toast.makeText(CreateAccountPage.this, "Error: Authentication
                            // failed. Please Check your email or password"
                            // Toast.LENGTH_SHORT).show();
                        });
    }
}