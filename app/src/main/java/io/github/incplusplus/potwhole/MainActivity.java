package io.github.incplusplus.potwhole;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "PotWhole";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the container in activity_main to the map fragment on startup
        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            Fragment mapFragment = new MapFragment();
            transaction.replace(R.id.container, mapFragment);
            transaction.commit();
        }

        NavigationBarView navBar = findViewById(R.id.bottom_navigation);
        navBar.setOnItemSelectedListener(
                item -> {
                    int itemId = item.getItemId();
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    /*
                     * A switch would be lovely here but would result in "Resource IDs will be
                     * non-final by default in Android Gradle Plugin version 8.0, avoid using them
                     * in switch case statements". As such, we're required to use an if/else block.
                     */
                    if (itemId == R.id.page_map) {
                        Fragment mapFragment = new MapFragment();
                        transaction.replace(R.id.container, mapFragment);
                        transaction.commit();
                        return true;
                    } else if (itemId == R.id.page_reports) {
                        Fragment reportsFragment = new ReportsFragment();
                        transaction.replace(R.id.container, reportsFragment);
                        transaction.commit();
                        return true;
                    } else if (itemId == R.id.page_my_reports) {
                        Fragment myReportsFragment = new MyReportsFragment();
                        transaction.replace(R.id.container, myReportsFragment);
                        transaction.commit();
                        return true;
                    } else if (itemId == R.id.page_my_account) {
                        Fragment myAccountFragment = new MyAccountFragment();
                        transaction.replace(R.id.container, myAccountFragment);
                        transaction.commit();
                        return true;
                    } else {
                        return false;
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }
}
