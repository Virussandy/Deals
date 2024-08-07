package com.mollosradix.deals;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;
import com.mollosradix.deals.fragments.HotDeals;
import com.mollosradix.deals.fragments.HourDeals;
import com.mollosradix.deals.fragments.RealtimeDeals;

public class MainActivity extends AppCompatActivity {

    private SearchView searchView;
    private HourDeals hour;
    private RealtimeDeals realtime;
    private HotDeals hot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivitiesIfAvailable(getApplication());
        }
        setContentView(R.layout.activity_main);

        // Initialize fragments here
        hour = new HourDeals();
        realtime = new RealtimeDeals();
        hot = HotDeals.newInstance(null);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case FragmentConstants.HOUR_DEALS_ID:
                    selectedFragment = hour;
                    break;
                case FragmentConstants.REALTIME_DEALS_ID:
                    selectedFragment = realtime;
                    break;
                case FragmentConstants.HOT_DEALS_ID:
                    selectedFragment = hot;
                    break;
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flFragment, selectedFragment)
//                        .addToBackStack(null) // Add to back stack to handle back navigation
                        .commit();
                collapseSearchView();
            }
            return true;
        });

        if (savedInstanceState == null) {
            // Set the default fragment
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.flFragment, hot)
                    .commit();
        }

        // Delay the update check to avoid blocking the main UI thread
        new Handler().postDelayed(() -> {
            CheckUpdate checkUpdate = new CheckUpdate(this);
            checkUpdate.check();
        }, 1000); // Delay of 1 second to ensure the main UI is displayed first
    }

    private void collapseSearchView() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.setIconified(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchViewItem.getActionView();
        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                HotDeals hotDealsFragment = HotDeals.newInstance(query);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flFragment, hotDealsFragment)
                        .addToBackStack(null) // Add to back stack for navigation
                        .commit();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
