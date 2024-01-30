package com.mollosradix.deals;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;
import com.mollosradix.deals.Fragments.HotDeals;
import com.mollosradix.deals.Fragments.HourDeals;
import com.mollosradix.deals.Fragments.Realtimedeals;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivitiesIfAvailable(getApplication());
        }
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.realtimedeals);
    }
    HourDeals hourDeals = new HourDeals();
    Realtimedeals realtimedeals = new Realtimedeals();
    HotDeals hotDeals = new HotDeals();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.hourdeals: getSupportFragmentManager().beginTransaction().replace(R.id.flFragment,hourDeals).commit();
                return true;
            case R.id.realtimedeals: getSupportFragmentManager().beginTransaction().replace(R.id.flFragment,realtimedeals).commit();
                return true;
//            case R.id.hotdeals: getSupportFragmentManager().beginTransaction().replace(R.id.flFragment,hotDeals).commit();
//                return true;
        }
        return false;
    }

}