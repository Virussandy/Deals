package com.mollosradix.deals;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

public abstract class BaseFragment extends Fragment implements NetworkChangeReceiver.CheckInternetListener {

    private NetworkChangeReceiver networkChangeReceiver;
    private Snackbar snackbar;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        networkChangeReceiver = new NetworkChangeReceiver(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        requireContext().registerReceiver(networkChangeReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(networkChangeReceiver);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        if (!isConnected) {
            if (snackbar == null || !snackbar.isShown()) {
                snackbar = Snackbar.make(requireActivity().findViewById(android.R.id.content),
                        "No internet connection.",
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.day_background));
                snackbar.setAction("Reconnect", v -> {
                    if (isConnectedToInternet()) {
                        onNetworkReconnect();
                        snackbar.dismiss();
                    } else {
                        onNetworkChanged(false);
                    }
                }).show();
            }
        } else {
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
                onNetworkReconnect();
            }
        }
    }

    protected abstract void onNetworkReconnect();

    protected boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
