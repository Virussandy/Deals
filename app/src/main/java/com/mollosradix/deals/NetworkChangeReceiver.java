package com.mollosradix.deals;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private final CheckInternetListener listener;

    public NetworkChangeReceiver(CheckInternetListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        {
            // Use NetworkCapabilities for API 21 and above
            Network network = cm.getActiveNetwork();
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            boolean isConnected = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            listener.onNetworkChanged(isConnected);
        }
    }

    public interface CheckInternetListener {
        void onNetworkChanged(boolean isConnected);
    }
}
