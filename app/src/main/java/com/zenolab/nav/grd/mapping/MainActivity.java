package com.zenolab.nav.grd.mapping;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Address;
import android.location.Geocoder;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.zenolab.nav.grd.mapping.movie_camera.DownloadTask;
import com.zenolab.nav.grd.mapping.tracking.LocationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,

         OnMapReadyCallback{

    public static final String TAG = MainActivity.class.getSimpleName();
    //public static GoogleMap map = AppMap.map;
    private SupportMapFragment mapFragment;

    public static PolylineOptions polylineOptions = new PolylineOptions();
    public static Polyline polyline;
    //------------------search-------------------------
    private static String location;
    //----------------------distance-------------------------
    ArrayList<LatLng> markerPoints;
    //public static TextView tvDistanceDuration;
    //--------------movieMAP----------------------------
    SharedPreferences sPref;
    public final static String BROADCAST_ACTION = "com.zenolab.nav.map.servicebackbroadcast";

    private boolean visibleOnScreen = false;

    // при выключенном экране (sleep mode ) получает сообщения от сервиса gps реже примерно раз-два в минуту
    // BroadCast можно реализовать отдельны классом
    // Данные с сервиса лучше пердавать через ServiceBinder или Loader

    // для передачи координат советуют распарсить 2 double c LatLng
    private BroadcastReceiver pointerMapBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            Log.d(TAG, "-------BroadcastReceiver--------");
            if(visibleOnScreen) updateUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "-------onCreate--------");
        AppMap.contextApp = getApplicationContext();
        AppMap.tvDistanceDuration = (TextView) findViewById(R.id.tv_distance_time);
        //---------------NavigationDrawer-----------------
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action" +" Clear map", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                AppMap.map.clear();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle); // is deprecated use addDrawerListener() instead.
        toggle.syncState(); // the Hamburger icon

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        //------------------------------------------------
        // Initializing
        markerPoints = new ArrayList<LatLng>();
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        AppMap.map = fm.getMap();

        // Enable MyLocation Button in the Map
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        AppMap.map.setMyLocationEnabled(true);
        AppMap.map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                // Already two locations
                if(markerPoints.size()>1){
                    markerPoints.clear();
                    AppMap.map.clear();
                }
                markerPoints.add(point);
                MarkerOptions options = new MarkerOptions();
                // Setting the position of the marker
                options.position(point);
                /**
                 * The distance
                 * For the start location, the color of marker is GREEN and
                 * for the end location, the color of marker is ORANGE.
                 */
                if(markerPoints.size()==1){
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }else if(markerPoints.size()==2){
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                }

                // Add new marker to the Google Map Android API V2
                AppMap.map.addMarker(options);

                // Checks, whether start and end locations are captured
                if(markerPoints.size() >= 2){
                    LatLng origin = markerPoints.get(0);
                    LatLng dest = markerPoints.get(1);
                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);
                    DownloadTask downloadTask = new DownloadTask();
                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }

            }
        });

        setUpMapIfNeeded();

        //----------------------Movie MAP-----------------
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        startService(new Intent(this, LocationService.class));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.MILLISECONDS.sleep(110);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        showNoticeDialog();

    }//-----END --onCreate()

  public void loadPointsDisplay()
    {
        Log.d(TAG, "-------VIEW POINTS--------");
        PolylineOptions lineOptions = null;
        lineOptions = new PolylineOptions();
        List<LatLng> temporary = new ArrayList<LatLng>();
        temporary = FileHelper.loadPointsFromFile();
        lineOptions.addAll(temporary);
        Log.d(TAG, "-------AppMap.pointsLoads SIZE--------"+AppMap.pointsLoads.size());
        lineOptions.width(14);
        lineOptions.color(Color.BLUE);
        AppMap.map.addPolyline(lineOptions);
    }

    //--------------------Handler NavigationDrawer Menu----------------
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.navigation_item_attachment) {
            Toast.makeText(this,"Add marker",Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(AppMap.currentLocation.getLatitude() , AppMap.currentLocation.getLongitude());
            AppMap.map.addMarker(new MarkerOptions().position(latLng).title("My_Marker "+AppMap.currentLocation.getAltitude()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) );

        }

        else if (id == R.id.navigation_item_clear) {
            AppMap.map.clear();
            FileHelper.clearFile();
            Toast.makeText(this,"Clear map & Clear File",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "-------  Clear map & Clear File --------");
        }
        else if (id == R.id.navigation_item_images) {
            if(AppMap.map.getMapType() == GoogleMap.MAP_TYPE_NORMAL)
            {
                AppMap.map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
            else
                AppMap.map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        } else if (id == R.id.navigation_item_location) {
            try{
            Toast.makeText(this,"Your location : Lat "+AppMap.currentLocation.getLatitude()+"  Long  "+AppMap.currentLocation.getLongitude(),Toast.LENGTH_LONG).show();
            }catch(Exception e){
                Toast.makeText(this,"Not found satelits!",Toast.LENGTH_LONG).show();
            }

        } else if (id == R.id.navigation_item_settings1) {
            //Toast.makeText(this,"Settings map",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

        } else if (id == R.id.navigation_item_settings2) {
            //Toast.makeText(this,"Settings map",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(
                    Settings.ACTION_APPLICATION_SETTINGS));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //=================== Search & Zooming ========================================
    public void onSearch(View view)
    {
        EditText location_tf = (EditText)findViewById(R.id.TFaddress);
        location = location_tf.getText().toString();
        List<Address> addressList = null;
        if(location != null || !location.equals(""))
        {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location , 1);

            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude() , address.getLongitude());
            AppMap.map.addMarker(new MarkerOptions().position(latLng).title("My_Marker "+location));

            /** animateCamera(update,callback)
            * animateCamera--Изменение, которое должно быть применено к камере.
            * callback-- Обратный вызов для вызова из основного потока при остановке анимации.
            * Если анимация завершается нормально, вызывается onFinish ();
            * В противном случае вызывается onCancel ().
            * Не обновляйте и не анимируйте камеру изнутри onCancel ().
             */
            AppMap.map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    public void onZoom(View view)
    {
        if(view.getId() == R.id.Bzoomin)
        {
            AppMap.map.animateCamera(CameraUpdateFactory.zoomIn());
        }
        if(view.getId() == R.id.Bzoomout)
        {
            AppMap.map.animateCamera(CameraUpdateFactory.zoomOut());
        }
    }

    private void setUpMapIfNeeded() {
        if (AppMap.map == null) {
            AppMap.map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (AppMap.map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        AppMap.map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker is "+location));
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            AppMap.map.setMyLocationEnabled(true);
        } else {
            Toast.makeText(this,"Required permission to display location on digital map",Toast.LENGTH_SHORT).show();
        }
    }

    //============================Distance & Duration =============================
    private String getDirectionsUrl(LatLng origin,LatLng dest){
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        return url;
    }
    //================================= MovieMap ==================================
   /**
    * abstract void
    * onMapReady(GoogleMap googleMap)
    * Called when the map is ready to be used.
    * it is called at initialization, once
    * To get hold of the GoogleMap object in our MainActivity class we need
    * to implement the OnMapReadyCallback interface and override the onMapReady callback method.
    */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Run  once after  onCreate -> onStart()-> onResume()
        if(AppMap.map == null){
            AppMap.map = googleMap;
        }
        Log.d(TAG, "-------onMapReady--------");
        LatLng calymayor = new LatLng(50.4591376,30.3508866);
        AppMap.map.moveCamera(CameraUpdateFactory.newLatLng(calymayor));
        AppMap.map.moveCamera(CameraUpdateFactory.newLatLngZoom(calymayor, 15));
         //------------------------рисование линиии на карте--------------------------
        /*
        Ломаные линии

        https://developers.google.com/maps/documentation/android-api/shapes?hl=ru#customizing_appearances

        Класс Polyline определяет набор соединенных сегментов линии на карте.
        Объект Polyline состоит из набора точек с координатами LatLng и создает серию сегментов линии,
        соединяющих эти точки в упорядоченной последовательности.

        Чтобы построить ломаную линию, сначала необходимо создать объект PolylineOptions и добавить
        к нему точки. Точка представляет собой местоположение на поверхности земли и выражается
        в виде объекта LatLng. Сегменты линии рисуются между точками в порядке
        их добавления к объекту PolylineOptions.
        Чтобы добавить точки к объекту PolylineOptions, вызовите PolylineOptions.add().
        Обратите внимание, что этот метод принимает переменное число параметров,
        поэтому вы можете добавлять несколько точек одновременно (вы можете также вызвать
        'PolylineOptions.addAll(Iterable)', если точки уже указаны в списке).

        Затем вы можете добавить ломаную линию на карту, вызвав GoogleMap.addPolyline(PolylineOptions).
        Этот метод возвращает объект Polyline, с помощью которого вы можете впоследствии изменить ломаную линию.

        Чтобы изменить форму ломаной линии после ее добавления,
        можно вызвать метод Polyline.setPoints() и передать в него новый список точек для ломаной линии.

        Вы можете изменить внешний вид ломаной линии либо до, либо после ее добавления на карту.
         */

        polylineOptions.color(Color.MAGENTA);
        polylineOptions.width(8);
        Toast.makeText(this,"MainActivity ----- YELLOW",Toast.LENGTH_SHORT).show();
        //Also there is a drawing of points in a separate method updateUI()
        polyline = AppMap.map.addPolyline(polylineOptions);
            Log.d(TAG, "-------polyline--------"+AppMap.gpsTrackPolyline);

        //--------------------------------------------------

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        AppMap.map.setMyLocationEnabled(true);
        Toast.makeText(this,"MainActivity ----- onMapReady()",Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        Log.w(TAG,"updateUI()");
        List<LatLng> pointsLocal;
        /**
        * The list returned is a copy of the list of vertices and so changes
        * to the polyline's vertices will not be reflected by this list,
        * nor will changes to this list be reflected by the polyline.
        * To change the vertices of the polyline, call setPoints(List).
        * https://developers.google.com/android/reference/com/google/android/gms/maps/model/Polyline.html#getPoints()
        */
        pointsLocal = polyline.getPoints();//получить прошлые точки с переменной
        pointsLocal.add(AppMap.lastKnownLatLng);
        //save the existing point to the global variable
        AppMap.pointsGlobalVarApp = pointsLocal;
        //drawing lines from points
        polyline.setPoints(AppMap.pointsGlobalVarApp);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "---onStart()---");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "---onStop()---");
        FileHelper.recPoints(FileHelper.loadPointsFromFile());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "---onPause()---");
        visibleOnScreen = false;
        unregisterReceiver(pointerMapBroadcast);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "---onResume()---");
        visibleOnScreen = true;
        registerReceiver(pointerMapBroadcast, new IntentFilter(BROADCAST_ACTION));
        FileHelper.loadPointsFromFile();
        //-------------waiting for load------
        try {
            TimeUnit.MILLISECONDS.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //-----------------------------------
        loadPointsDisplay();
        setUpMapIfNeeded();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "-------onDestroy()--------");
        //unregisterReceiver(pointerMapBroadcast);
        //stopService(new Intent(this, LocationService.class));
        addNotification("onDestroy()");
        super.onDestroy();
    }
//-----------------------------------------------------------------------------------
    private  void addNotification(String string) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentTitle("Notifications MainActivity")
                      //  .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.detailed_world_map_800x600))
                        .setContentText("Happened is "+ string);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

///============================ RESTORE Display Component==================================
    /*
    Ошибка: (992, 19) error: onRetainNonConfigurationInstance () в MainActivity не может переопределить onRetainNonConfigurationInstance () в FragmentActivity
переопределенный метод является окончательным
     */
    /*
    @Override
    public Object onRetainNonConfigurationInstance() {
        // TODO Auto-generated method stub
        return this;
    }
    */
    //Кроме метода onRestoreInstanceState, доступ к сохраненным данным также можно получить в методе onCreate.
    // На вход ему подается тот же самый Bundle.
    // Если восстанавливать ничего не нужно, он будет = null.
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //cnt = savedInstanceState.getInt("count");
        Log.d(TAG, "onRestoreInstanceState");
    }
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
       // outState.putInt("count", cnt);
        Log.d(TAG, "onSaveInstanceState");
    }
    //==============================================================================================
    public void showNoticeDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.notification_icon)
                .setTitle("Warning !")
                .setMessage("You need allow Permission to display Location !");
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.show();
    }

}

