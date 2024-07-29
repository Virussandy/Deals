package com.mollosradix.deals;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CheckUpdate {

    private final Context context;
    private DatabaseReference databaseReference;
    private static final String TAG = "CheckUpdate";

    public CheckUpdate(Context context) {
        this.context = context;
        initializeDatabase();
    }

    private void initializeDatabase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("latest_version");
    }

    public void check() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String latestVersion = dataSnapshot.getValue(String.class);
                checkForUpdate(latestVersion);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch latest version from database", databaseError.toException());
            }
        });
    }

    private void checkForUpdate(String latestVersion) {
        String currentVersion = BuildConfig.VERSION_NAME; // Ensure this is correctly referenced

        Log.d(TAG, "Current Version: " + currentVersion);
        Log.d(TAG, "Latest Version: " + latestVersion);

        if (!currentVersion.equals(latestVersion)) {
            showUpdateDialog();
            Log.d(TAG, "checkForUpdate: Not Equal");
        } else {
            Log.d(TAG, "checkForUpdate: Equal ");
        }
    }

    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Update Available")
                .setMessage("A new version of the app is available. Please update to continue.")
                .setPositiveButton("Update", (dialog, which) -> {
                    String packageName = context.getPackageName();
                    Uri playStoreUri = Uri.parse("market://details?id=" + packageName);
                    Intent intent = new Intent(Intent.ACTION_VIEW, playStoreUri);
                    context.startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).setCancelable(false); // Prevent the user from dismissing the dialog without updating

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
