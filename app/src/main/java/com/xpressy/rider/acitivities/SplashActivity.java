package com.xpressy.rider.acitivities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;

import com.github.javiersantos.appupdater.AppUpdater;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.xpressy.rider.R;
import com.xpressy.rider.session.SessionManager;
import com.thebrownarrow.permissionhelper.ActivityManagePermission;
import com.thebrownarrow.permissionhelper.PermissionResult;
import com.thebrownarrow.permissionhelper.PermissionUtils;

/**
 * Created by android on 7/3/17.
 */

public class SplashActivity extends ActivityManagePermission {
    String permissionAsk[] = {PermissionUtils.Manifest_CAMERA, PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE, PermissionUtils.Manifest_READ_EXTERNAL_STORAGE, PermissionUtils.Manifest_ACCESS_FINE_LOCATION, PermissionUtils.Manifest_ACCESS_COARSE_LOCATION};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppUpdater appUpdater = new AppUpdater(this);
        appUpdater.start();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

//Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

//set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.splash_activity);
        int SPLASH_TIME_OUT = 2000;
        isGooglePlayServicesAvailable(SplashActivity.this);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity

                askCompactPermissions(permissionAsk, new PermissionResult() {
                    @Override
                    public void permissionGranted() {
                        changement();
                    }

                    @Override
                    public void permissionDenied() {
                        changement();
                    }

                    @Override
                    public void permissionForeverDenied() {
                        changement();
                    }
                });

            }
        }, SPLASH_TIME_OUT);

    }

    public void changement() {
        SessionManager.getInstance().setPref(getApplicationContext());
        if (SessionManager.getInstance().isLoggedIn(getApplicationContext())) {
            startActivity(new Intent(SplashActivity.this, HomeActivity.class));
        } else {
            Intent i = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(i);
        }
        finish();
    }

    public boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        } else {

        }
        return true;
    }
}
