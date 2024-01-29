package ru.samsung.smartintercom.util;

import android.content.Context;
import android.net.ConnectivityManager;

import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;

public class Internet {
    public static boolean isAccessible(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return connectivityManager
                    .getNetworkCapabilities(connectivityManager.getActiveNetwork())
                    .hasCapability(NET_CAPABILITY_INTERNET);
        } catch (Exception e) {
            return false;
        }
    }
}
