package com.mollosradix.deals;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
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
                checkForUpdate(latestVersion,context);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch latest version from database", databaseError.toException());
            }
        });
    }

    private void checkForUpdate(String latestVersion,Context context) {
        String currentVersion = BuildConfig.VERSION_NAME; // Ensure this is correctly referenced

        Log.d(TAG, "Current Version: " + currentVersion);
        Log.d(TAG, "Latest Version: " + latestVersion);

        if (!currentVersion.equals(latestVersion)) {
            showUpdateDialog(context);
            Log.d(TAG, "checkForUpdate: Not Equal");
        } else {
            Log.d(TAG, "checkForUpdate: Equal ");
        }
    }

    public void showUpdateDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_update, null);
        builder.setView(dialogView);
        MaterialButton buttonUpdate = dialogView.findViewById(R.id.buttonUpdate);
        MaterialButton buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        builder.setCancelable(false);
        AlertDialog dialog = builder.create();

        buttonUpdate.setOnClickListener(v -> {
            String packageName = context.getPackageName();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));

            // Check if there's an app that can handle this intent
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                // Fallback to a web URL
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                context.startActivity(intent);
            }
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
