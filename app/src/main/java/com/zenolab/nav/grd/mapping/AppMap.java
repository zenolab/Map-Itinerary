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

//Application loaded - uploaded first ( even eairler than MainActivity)
//The purpose of the Application class is to store the state or data of a global application
public class AppMap extends Application  {

    /*
    If you are not using so many static variables so this may not affect your application.
    But the problem with static variable may arise when your app goes
    to background and the app running on front requires memory so it may clear your static data,
    so when you will go to your app you may find nothing (null) in place of static data.
     */

    public static final String FILENAME = "Map_Itinerary_Points";
    public static Location currentLocation;
    public static List<LatLng> pointsGlobalVarApp;
    public static ArrayList<LatLng> pointsLoads = new ArrayList<LatLng>();
    public static Polyline gpsTrackPolyline;
    public static LatLng lastKnownLatLng;
    static List<LatLng>  datArrayList = new ArrayList<LatLng>();
    public static Context contextApp ;
    public static TextView tvDistanceDuration;
    public static GoogleMap map;

    private String str;


    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();
        Log.w("AppMap", "onCreate MyApp");
        str="onCreate MyApp";
        addNotification(str);

    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

     /** This is called when the overall system is running low on memory,
     *   and would like actively running processes to tighten their belts.
     *   Overriding this method is totally optional!
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        str = "onLowMemory";
        addNotification(str);
    }

    /*
    // onTrimMemory() - called when the Android system requests that the application cleans up memory.
    @Override
    public void onTrimMemory(){
        super.onTrimMemory();
        str = "onTrimMemory";
        addNotification(str);
    }
    */

    //onTerminate() - only for testing, not called in production
    @Override
    public void onTerminate(){
        super.onTerminate();
        str = "onTerminate";
        addNotification(str);

    }

    //,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
    public void addNotification(String string) {
        // https://www.tutorialspoint.com/android/android_notifications.htm
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
