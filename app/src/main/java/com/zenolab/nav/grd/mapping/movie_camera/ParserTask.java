package com.zenolab.nav.grd.mapping.movie_camera;

import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.zenolab.nav.grd.mapping.AppMap;
import com.zenolab.nav.grd.mapping.CustomLatLng;
import com.zenolab.nav.grd.mapping.MainActivity;
import com.zenolab.nav.grd.mapping.duration.DirectionsJSONParser;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by grd on 7/18/17.
 */

/**
 * A class to parse the Google Places in JSON format
 */

public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData[0]);
            DirectionsJSONParser parser = new DirectionsJSONParser();
            routes = parser.parse(jObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return routes;
    }

    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points = null;
        // ArrayList<CustomLatLng> points = null;
        PolylineOptions lineOptions = null;
        MarkerOptions markerOptions = new MarkerOptions();
        String distance = "";
        String duration = "";

        if (result.size() < 1) {
            Toast.makeText(AppMap.contextApp, "No Points", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();
            List<HashMap<String, String>> path = result.get(i);
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                if (j == 0) {
                    distance = (String) point.get("distance");
                    continue;
                } else if (j == 1) {
                    duration = (String) point.get("duration");
                    continue;
                }

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }

            lineOptions.addAll(points);
            lineOptions.width(12);
            lineOptions.color(Color.CYAN);

        }

        AppMap.tvDistanceDuration.setText("Distance:" + distance + ", Duration:" + duration);
        AppMap.map.addPolyline(lineOptions);

    }

}
