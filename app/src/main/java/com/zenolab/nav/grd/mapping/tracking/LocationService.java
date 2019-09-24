package com.zenolab.nav.grd.mapping.tracking;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.zenolab.nav.grd.mapping.AppMap;
import com.zenolab.nav.grd.mapping.MainActivity;
import com.zenolab.nav.grd.mapping.R;
import com.zenolab.nav.grd.mapping.FileHelper;

import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "LocationService";
    private GoogleApiClient googleApiClient;

    private LocationManager locationManager;
    private Timer timer;
    private TimerTask tTask;

    private final long INTERVAL_REQUEST_LOCATION = 9000;

    private Location gpsLocation = null;

    public Location getGpsLocation() {
        return gpsLocation;
    }

    private void setGpsLocation(Location gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    private android.location.LocationListener listener = new android.location.LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            setGpsLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTracking();
        new Thread(new Runnable() {
            @Override
            public void run() {
                timerListenerGPS();
            }
        }).start();
        return START_STICKY;
    }

    private void startTracking() {
        Log.d(TAG, "startTracking");
        if (googleApiClient == null) {

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            Log.e(TAG, "unable to connect to google play services.");
        }
    }

    private void timerListenerGPS() {
        timer = new Timer();
        tTask = new TimerTask() {

            public void run() {
                Log.d(TAG, "run");
                try {
                    Log.d("UI thread", "I am the UI thread");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            locationManager = (LocationManager) getApplicationContext()
                                    .getSystemService(Context.LOCATION_SERVICE);
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(AppMap.contextApp,
                                    Manifest.permission.ACCESS_COARSE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                    7000, 0, listener);
                            addToFileLocation(getGpsLocation());
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        };
        //void schedule (TimerTask task, long delay, long period)
        timer.schedule(tTask, 0, INTERVAL_REQUEST_LOCATION);
        Log.e(TAG, "TIMER: ");

    }

    private void addToFileLocation(Location location) {
        if (location != null) {
            Log.e(TAG, "POSITION BACKGROUND: " + location.getLatitude() + ", " + location.getLongitude() + " точность-accuracy: " + location.getAccuracy());
            AppMap.lastKnownLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            AppMap.currentLocation = location;
            Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
            addNotificationService("timer Service LatLng" + location.getLatitude() + " " + location.getLongitude());
            Log.d("SERVICE", "########### LOCATION ##########");
            FileHelper.recPointsToFile(AppMap.lastKnownLatLng);
            Log.w(TAG, "---attempt invoke broadcast----");
            sendBroadcast(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        stopLocationUpdates();
        stopSelf();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.e(TAG, "googleApiClient: " + location.getLatitude() + ", "
                    + location.getLongitude() + " accuracy: " + location.getAccuracy());
            setGpsLocation(location);
            AppMap.lastKnownLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            AppMap.currentLocation = location;
            Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
            addNotificationService(" LatLng" + location.getLatitude() + " " + location.getLongitude());
            sendBroadcast(intent);
        }

    }

    //=======================================================
    protected void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(8000);
        locationRequest.setFastestInterval(4000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    private void addNotificationService(String string) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Notifications LocationService")
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                                R.drawable.detailed_world_map_800x600))
                        .setContentText(">> " + string);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        new AppMap().addNotification("Service onDestroy() !!!");
    }

}
