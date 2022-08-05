package io.github.incplusplus.potwhole;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class RegisterAccountFragment extends Fragment {

    private FirebaseAuth mAuth;

    private FirebaseFunctions mFunctions;

    private EditText editTextEmail, editTextPassword, editTextUsername;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextEmail = requireView().findViewById(R.id.editTextEmail);
        editTextPassword = requireView().findViewById(R.id.editTextPassword);
        editTextUsername = requireView().findViewById(R.id.editTextUsername);

        Button RegisterAccountButton = requireView().findViewById(R.id.Register_Account_button);

        TextView loginButton = requireView().findViewById(R.id.login);

        RegisterAccountButton.setOnClickListener(v -> createUserAccount());

        loginButton.setOnClickListener(
                v -> {
                    // Create new fragment and transaction
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.container, MyAccountFragment.class, null);

                    transaction.commit();
                });
    }

    private void createUserAccount() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();

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

        if (username.isEmpty()) {
            editTextEmail.setError("Please Enter a Username");
            editTextEmail.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(
                        authResult -> {
                            Log.v("MyApp", "User Account is Created");
                            updateUsername();
                        })
                .addOnFailureListener(
                        e -> {

                            // If sign in fails, display a message to the user.
                            Log.w("AUTH_INFO", "createUserWithEmail:failure", e);
                            Toast toast =
                                    Toast.makeText(
                                            getActivity(),
                                            "Error: Authentication failed. Please Check your email or password",
                                            Toast.LENGTH_SHORT);
                            toast.show();
                        });
    }

    private void updateUsername(){
        String username = editTextUsername.getText().toString().trim();

        mFunctions = FirebaseFunctions.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        Log.v("USERNAME", username);

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

                            Intent intent =
                                    new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                        });
    }
}