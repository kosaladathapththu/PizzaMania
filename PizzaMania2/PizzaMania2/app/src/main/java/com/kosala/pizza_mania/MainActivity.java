package com.kosala.pizza_mania;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // ✅ Load default fragment safely
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (itemId == R.id.nav_account) {
                selected = new AccountFragment();
            } else if (itemId == R.id.nav_settings) {
                selected = new SettingsFragment();
            } else if (itemId == R.id.nav_map) {  // ✅ Added Map option
                selected = new MapFragment();
            }

            if (selected != null) {
                replaceFragment(selected);
            }
            return true;
        });
    }

    // ✅ Safe fragment replacement method
    private void replaceFragment(Fragment fragment) {
        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitAllowingStateLoss(); // prevents crash if state saved
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}
