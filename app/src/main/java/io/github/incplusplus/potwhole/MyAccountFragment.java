package io.github.incplusplus.potwhole;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import java.util.Map;

public class MyAccountFragment extends Fragment {

    private FirebaseAuth mAuth;

    private FirebaseFunctions mFunctions;

    private EditText editTextEmail, editTextPassword;

    private TextView username;

    public MyAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if (mAuth.getCurrentUser() == null) {
            return inflater.inflate(R.layout.fragment_my_account_not_signed_in, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_my_account_signed_in, container, false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mAuth.getCurrentUser() == null) {

            Button logInButton = requireView().findViewById(R.id.log_in_button);

            TextView createAccount = requireView().findViewById(R.id.createAccount);

            editTextEmail = requireView().findViewById(R.id.editTextEmail);
            editTextPassword = requireView().findViewById(R.id.editTextPassword);

            logInButton.setOnClickListener(v -> userLogin());

            createAccount.setOnClickListener(
                    v -> {
                        // Create new fragment and transaction
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.container, RegisterAccountFragment.class, null);

                        transaction.commit();
                    });

        } else {
            Button signOutButton = requireView().findViewById(R.id.sign_out_button);

            username = requireView().findViewById(R.id.username);

            mFunctions = FirebaseFunctions.getInstance();

            mFunctions
                    .getHttpsCallable("getUserDocument")
                    .call()
                    .addOnFailureListener(
                            e -> {
                                Log.v("USER_DOC_GET", "Getting User Document Failed");
                                Log.v("USER_DOC_GET", "Exception - " + e);
                            })
                    .addOnSuccessListener(
                            httpsCallableResult -> {
                                Log.v("USER_DOC_GET", "Getting User Document Successful");
                                Log.v(
                                        "USER_DOC_GET",
                                        "Return From Database - " + httpsCallableResult.getData());

                                Map<String, Object> data =
                                        (Map<String, Object>) httpsCallableResult.getData();

                                Log.v("USERNAME", data.get("username").toString());

                                username.setText(data.get("username").toString());
                            });

            signOutButton.setOnClickListener(
                    v -> {
                        mAuth.signOut();

                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                    });
        }
    }

    private void userLogin() {
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

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(
                        task -> {
                            // redirect
                            Log.v("MyAPP", "User is Signed In");
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                        })
                .addOnFailureListener(
                        e -> Log.w("AUTH_INFO", "signInWithEmailAndPassword:failure", e));
    }
}
