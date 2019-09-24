package com.zenolab.nav.grd.mapping;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by grd on 9/14/17.
 */

public class FileHelper extends ContextWrapper {

    public static final String TAG = FileHelper.class.getSimpleName();

    public FileHelper(Context base) {
        super(base);
    }

    public static void recPointsToFile(LatLng serviceLastKnownLatLng) {

        AppMap.datArrayList.add(serviceLastKnownLatLng);
        AppMap.datArrayList.size();

        PrintWriter pw = null;
        FileOutputStream fos = null;

        try {
            try {
                fos = AppMap.contextApp.openFileOutput(AppMap.FILENAME, MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            pw = new PrintWriter(fos);
            int datList = AppMap.datArrayList.size();
            for (int i = 0; i < datList; i++) {
                pw.write(AppMap.datArrayList.get(i).toString() + "\n");
            }
        } finally {
            if (pw != null) {
            }
            pw.flush();
            pw.close();
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //==========================================================
    public static ArrayList<LatLng> loadPointsFromFile() {
        ArrayList<LatLng> loadPoints = new ArrayList<LatLng>();
        Log.d(TAG, "-------  LoadPointsFromFile --------");
        FileInputStream fstream = null;
        try {
            FileInputStream fis = AppMap.contextApp.openFileInput(AppMap.FILENAME);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String strLine = "";
            String[] tokens = strLine.split(", ");
            while ((strLine = br.readLine()) != null) {
                String resLine;
                resLine = strLine.replaceAll("[^,." +
                        "0-9 -]", "").trim();
                String[] split = resLine.split(",");
                double numberLat = Double.parseDouble(split[0]);
                double numberLng = Double.parseDouble(split[1]);
                LatLng position = new LatLng(numberLat, numberLng);
                loadPoints.add(position);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fstream.close();
            } catch (Exception ignore) {
            }
        }
        return loadPoints;
    }

    public static void clearFile() {
        if (AppMap.contextApp.deleteFile(AppMap.FILENAME))
            Log.i(TAG, "Deleted" + AppMap.FILENAME);
    }

    public static void recPoints(List<LatLng> datArrayList) {

        PrintWriter pw = null;
        FileOutputStream fos = null;
        try {
            try {
                fos = AppMap.contextApp.openFileOutput(AppMap.FILENAME, Context.MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            pw = new PrintWriter(fos);
            int datList = datArrayList.size();
            for (int i = 0; i < datList; i++) {
                pw.write(datArrayList.get(i).toString() + "\n");
                Log.d(TAG, datArrayList.get(i).toString() + "\n");
            }

        } finally {
            try {
                pw.flush();
                pw.close();
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

    }

}
