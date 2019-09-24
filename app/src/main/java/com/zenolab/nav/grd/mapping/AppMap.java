package com.zenolab.nav.grd.mapping;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.RingtoneManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by grd on 9/7/17.
 */


public class AppMap extends Application {

    public static final String FILENAME = "Map_Itinerary_Points";
    public static Location currentLocation;
    public static List<LatLng> pointsGlobalVarApp;
    public static ArrayList<LatLng> pointsLoads = new ArrayList<LatLng>();
    public static Polyline gpsTrackPolyline;
    public static LatLng lastKnownLatLng;
    static List<LatLng> datArrayList = new ArrayList<LatLng>();
    public static Context contextApp;
    public static TextView tvDistanceDuration;
    public static GoogleMap map;

    private String str;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w("AppMap", "onCreate MyApp");
        str = "onCreate MyApp";
        addNotification(str);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        str = "onLowMemory";
        addNotification(str);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        str = "onTerminate";
        addNotification(str);

    }

    public void addNotification(String string) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.notification_icon))
                        .setContentTitle("Notifications from Application")
                        //.setContentText("This is a test notification " + string);
                        .setContentText("Happened is " + string);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

}
