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
import com.google.firebase.auth.FirebaseAuth;

/** * A simple {@link Fragment} subclass. Use the factory method to create an instance of this
 * fragment.
 */
public class MyAccountFragment extends Fragment {

    private FirebaseAuth mAuth;

    private EditText editTextEmail, editTextPassword;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

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
                        Intent intent = new Intent(getActivity(), RegisterAccountPage.class);
                        startActivity(intent);
                    });

        } else {
            Button signOutButton = requireView().findViewById(R.id.sign_out_button);

            Button functionButton = requireView().findViewById(R.id.functions);

            signOutButton.setOnClickListener(
                    v -> {
                        mAuth.signOut();

                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                    });

            functionButton.setOnClickListener(
                    v -> {
                        Intent intent = new Intent(getActivity(), FirebaseFunctionExample.class);
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