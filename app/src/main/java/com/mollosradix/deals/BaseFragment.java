package com.mollosradix.deals;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

public abstract class BaseFragment extends Fragment {

    private Snackbar snackbar;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        registerNetworkCallback();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unregisterNetworkCallback();
    }

    private void registerNetworkCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                new Handler(Looper.getMainLooper()).post(() -> onNetworkChanged(true));
            }

            @Override
            public void onLost(@NonNull Network network) {
                new Handler(Looper.getMainLooper()).post(() -> onNetworkChanged(false));
            }
        };

        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    private void unregisterNetworkCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    private void onNetworkChanged(boolean isConnected) {
        if (getActivity() != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
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
            });
        }
    }

    protected abstract void onNetworkReconnect();

    protected boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
