package com.example.hhoa.trackme;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.util.Log;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class Util {
    public static double distance(double lat1, double lat2, double lon1, double lon2) {

//        final int R = 6371; // Radius of the earth
//
//        double latDistance = Math.toRadians(lat2 - lat1);
//        double lonDistance = Math.toRadians(lon2 - lon1);
//        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
//                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
//                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        double distance = R * c * 1000; // convert to meters
//
//        double height = el1 - el2;
//
//        distance = Math.pow(distance, 2) + Math.pow(height, 2);
//
//        return Math.sqrt(distance);
        float[] res = new float[]{0, 0, 0};
        Location.distanceBetween(lat1, lon1, lat2, lon2, res);
        return res[0];
    }

    public static long[] splitTimes(long mySeconds)
    {
        long hours = mySeconds / 3600;
        long minutes = (mySeconds % 3600) / 60;
        long seconds = mySeconds % 60;

        long[] res = {hours , minutes , seconds};
        return res;
    }

    public static double average(ArrayList<Double> listSpeed) {
        if (listSpeed == null || listSpeed.isEmpty()) {
            return 0;
        }

        double sum = 0;
        for (double item: listSpeed) {
            sum += item;
        }

        return sum / listSpeed.size();
    }

    public static boolean checkNetworkStatus(Context context) {

        final ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //Check Wifi
        final android.net.NetworkInfo net;
        if (manager != null)
            net = manager.getActiveNetworkInfo();
        else
            return false;
        if (net == null)
            return false;

        Log.i(TAG, "checkNetworkStatus: " + net.toString());
        return net.getType() == ConnectivityManager.TYPE_WIFI ||
                net.getType() == ConnectivityManager.TYPE_MOBILE;

    }
}
