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
    //==========================================================

public static void recPointsToFile(LatLng serviceLastKnownLatLng) {

    AppMap.datArrayList.add(serviceLastKnownLatLng);
    AppMap.datArrayList.size();

    PrintWriter pw = null;
    FileOutputStream fos=null;

    try {
        try {
            fos = AppMap.contextApp.openFileOutput(AppMap.FILENAME,  MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        pw = new PrintWriter(fos);
        int datList = AppMap.datArrayList.size();
        for (int i = 0; i < datList; i++) {

            Log.d(TAG," datArrayList.get(i).toString() ==== "+AppMap.datArrayList.get(i).toString());
            pw.write(AppMap.datArrayList.get(i).toString() + "\n");

            Log.v(TAG, "%-%-%-%-%-%-%-%-%%-%-%-%-%-%%"+AppMap.datArrayList.get(i).toString() + "\n");

        }

        Log.d(TAG,"File wrote size is "+AppMap.datArrayList.size());
        Log.d("Application", "---file write from Service----pw.write !!!!!!!!!!!!!!!!!!!!!!!!!--------");

    } finally {
        if(pw!=null){}
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
public static ArrayList<LatLng> loadPointsFromFile()
{

    ArrayList<LatLng> loadPoints = new ArrayList<LatLng>();
    Log.d(TAG, "-------  LoadPointsFromFile --------");
    FileInputStream fstream = null;
    try
    {
        FileInputStream fis = AppMap.contextApp.openFileInput(AppMap.FILENAME);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String strLine = "";
        String[] tokens = strLine.split(", ");
        //Read file line by line
        while ((strLine = br.readLine()) != null)   {
            String resLine;
            //remove all symbol
            resLine=strLine.replaceAll("[^,." +
                    "0-9 -]", "").trim();
            Log.d(TAG, "-------Read :"+resLine);
            String str_lat,str_lgn;

            // Перегрузка файла
            // Caused by: java.lang.StringIndexOutOfBoundsException:
            // length=4; regionStart=0; regionLength=-1
            //at java.lang.String.substring
            str_lat = resLine.substring(0, resLine.indexOf(','));
            Log.d(TAG, "-------Read str_lat :"+str_lat);
            str_lat = resLine.split(",", 2)[0];
            Log.d(TAG, "-------Read str_lat 2:"+str_lat);

            str_lgn = resLine.substring(resLine.indexOf(",")+1);
            Log.d(TAG, "---is----Read str_lgn TWO:"+str_lgn);

            String[] split = resLine.split(",");
            Log.d(TAG, "-------Read String[] split 1 :"+split[0]);
            Log.d(TAG, "-------Read String[] split 2:"+split[1]);
            double numberLat = Double.parseDouble(split[0]);
            double numberLng = Double.parseDouble(split[1]);
            LatLng position = new LatLng(numberLat, numberLng);
            loadPoints.add(position);
        }
        Log.d(TAG, "-------AppMap.pointsLoads.add(position09); COMPLETE-------");
    }
    catch (IOException e) {
        e.printStackTrace();
    }
    finally {
        try { fstream.close(); } catch ( Exception ignore ) {}
    }
    return loadPoints;
}
    //==========================================================
    public static  void clearFile()  {
        if(AppMap.contextApp.deleteFile(AppMap.FILENAME))
            Log.i(TAG, " ---- !! deleted. file "+AppMap.FILENAME);
    }
    //==========================================================
    //public  void recPoints(List<LatLng> datArrayList) throws IOException {
    public static void recPoints(List<LatLng> datArrayList)  {

        PrintWriter pw = null;
        FileOutputStream fos=null;
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
            Log.d(TAG, "-------pw.write !!!!!!!!!!!!!!!!!!!!!!!!!--------");

        }
        //определенный участок кода будет выполняться независимо от того, какие исключения были возбуждены и перехвачены
        finally {
            try {
                pw.flush();
                pw.close();
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }

    }

}
