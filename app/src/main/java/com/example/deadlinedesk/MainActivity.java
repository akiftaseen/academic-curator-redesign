package com.example.deadlinedesk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.deadlinedesk.ui.AddEditDeadlineActivity;
import com.example.deadlinedesk.ui.CalendarFragment;
import com.example.deadlinedesk.ui.UpcomingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        // Start with Calendar Fragment (as per refined_calendar_agenda mockup being primary)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CalendarFragment()).commit();
            bottomNav.setSelectedItemId(R.id.nav_calendar);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_calendar) {
                selectedFragment = new CalendarFragment();
            } else if (item.getItemId() == R.id.nav_upcoming) {
                selectedFragment = new UpcomingFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        });

        View fabAdd = findViewById(R.id.fab_add_deadline);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditDeadlineActivity.class);
            startActivity(intent);
        });
    }
}
