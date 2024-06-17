package com.mollosradix.deals;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.content.BroadcastReceiver;
import android.content.Intent;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private CheckInternetListener listener;

    public NetworkChangeReceiver(CheckInternetListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        listener.onNetworkChanged(isConnected);
    }

    public interface CheckInternetListener {
        void onNetworkChanged(boolean isConnected);
    }
}

