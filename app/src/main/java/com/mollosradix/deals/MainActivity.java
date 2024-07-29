package com.mollosradix.deals;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;
import com.mollosradix.deals.Fragments.HotDeals;
import com.mollosradix.deals.Fragments.HourDeals;
import com.mollosradix.deals.Fragments.Realtimedeals;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private SearchView searchView;
    private HourDeals hourDeals;
    private Realtimedeals realtimedeals;
    private HotDeals hotDeals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivitiesIfAvailable(getApplication());
        }
        setContentView(R.layout.activity_main);


        // Initialize fragments here
        hourDeals = new HourDeals();
        realtimedeals = new Realtimedeals();
        hotDeals = HotDeals.newInstance(null);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.hotdeals);


        // Delay the update check to avoid blocking the main UI thread
        new Handler().postDelayed(() -> {
            CheckUpdate checkUpdate = new CheckUpdate(this);
            checkUpdate.check();
        }, 1000); // Delay of 1 second to ensure the main UI is displayed first
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.hourdeals:
                if (hourDeals != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, hourDeals).commit();
                }
                collapseSearchView();
                return true;
            case R.id.realtimedeals:
                if (realtimedeals != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, realtimedeals).commit();
                }
                collapseSearchView();
                return true;
            case R.id.hotdeals:
                if (hotDeals != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, hotDeals).commit();
                }
                collapseSearchView();
                return true;
        }
        return false;
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
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, hotDealsFragment).commit();
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
