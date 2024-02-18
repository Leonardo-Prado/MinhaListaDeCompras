package com.lspsoftwares.minhalistadecompras.conn;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnManager {
    private ConnectivityManager connectivityManager;
    private boolean isConnected;

    public ConnManager(Object connectivityManagerService){
        connectivityManager = (ConnectivityManager)connectivityManagerService;
    }


    public ConnectivityManager getConnectivityManager() {
        return connectivityManager;
    }

    public boolean isConnected() {
        return ((connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED));
    }
}

