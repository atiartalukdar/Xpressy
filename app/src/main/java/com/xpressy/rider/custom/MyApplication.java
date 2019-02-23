package com.xpressy.rider.custom;

import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.xpressy.rider.R;
import com.mapbox.mapboxsdk.Mapbox;
import io.fabric.sdk.android.Fabric;



/**
 * Created by android on 15/3/17.
 */

public class MyApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapboxkey));


    }


}
