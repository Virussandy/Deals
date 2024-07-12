package com.mollosradix.deals;

import android.os.Build;
import android.os.Bundle;
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

    private BottomNavigationView bottomNavigationView;
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
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.hotdeals);

        hourDeals = new HourDeals();
        realtimedeals = new Realtimedeals();
        hotDeals = HotDeals.newInstance(null); // Initialize HotDeals fragment
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.hourdeals:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, hourDeals).commit();
                collapseSearchView();
                return true;
            case R.id.realtimedeals:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, realtimedeals).commit();
                collapseSearchView();
                return true;
            case R.id.hotdeals:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, hotDeals).commit();
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
