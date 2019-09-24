package com.zenolab.nav.grd.mapping;

import android.os.Parcel;

import java.io.Serializable;

/**
 * Created by grd on 9/11/17.
 */

public  class CustomLatLng  implements Serializable , com.google.android.gms.common.internal.safeparcel.SafeParcelable{

    public static  com.google.android.gms.maps.model.zze CREATOR;
    private  int mVersionCode;
    public  double latitude;
    public double longitude;

    public CustomLatLng(double lat, double lng) {
       this.latitude = lat;
       this.longitude = lng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }


}
