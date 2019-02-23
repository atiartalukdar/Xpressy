package com.xpressy.rider.acitivities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.xpressy.rider.R;
import com.xpressy.rider.Server.Server;
import com.xpressy.rider.custom.CheckConnection;
import com.xpressy.rider.custom.Utils;
import com.xpressy.rider.pojo.User;
import com.xpressy.rider.session.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import android.widget.Toast;
import com.thebrownarrow.permissionhelper.ActivityManagePermission;
import com.thebrownarrow.permissionhelper.PermissionResult;
import com.thebrownarrow.permissionhelper.PermissionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

/**
 * Created by android on 7/3/17.
 *
 * updated on 13/02/19 by Atiar
 * updated the registration process.
 */

public class RegisterActivity extends ActivityManagePermission implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    Context mContext;
    String userRegisterType ="";
    String permissionAsk[] = {PermissionUtils.Manifest_CAMERA, PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE, PermissionUtils.Manifest_READ_EXTERNAL_STORAGE, PermissionUtils.Manifest_ACCESS_FINE_LOCATION, PermissionUtils.Manifest_ACCESS_COARSE_LOCATION};
    RelativeLayout relative_signin;
    TextInputEditText input_email, input_password, input_confirmPassword, input_mobile, input_name;
    AppCompatButton sign_up;
    SwipeRefreshLayout swipeRefreshLayout;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Double currentLatitude;
    private Double currentLongitude;
    private View rootView;
    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        Intent I = getIntent();
        userRegisterType = I.getStringExtra("regType");

        mContext = this;

        phoneNumber = Utils.getPreference(this,Utils.key_phoneNumber);

        BindView();
        applyfonts();
        AskPermission();

        relative_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    if (CheckConnection.haveNetworkConnection(getApplicationContext())) {

                        String latitude = "";
                        String longitude = "";

                        latitude = String.valueOf(currentLatitude);
                        longitude = String.valueOf(currentLongitude);
                        String city = null, state = null, country = null;
                        String email = input_email.getText().toString().trim();
                        String mobile = input_mobile.getText().toString().trim();
                        String password = input_password.getText().toString().trim();
                        String name = input_name.getText().toString().trim();
                        try {
                            Geocoder geocoder;
                            List<Address> addresses = null;
                            geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            if (!latitude.equals("0.0") && !longitude.equals("0.0")) {
                                try {
                                    addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
                                    if (addresses != null && addresses.size() > 0) {
                                        String merged = "";
                                        city = addresses.get(0).getLocality();
                                        country = addresses.get(0).getCountryName();
                                        state = addresses.get(0).getAdminArea();
                                        if (city != null) {
                                            merged = city;
                                        } else {
                                            city = "null";
                                        }
                                        if (state != null) {
                                            merged = city + "," + state;

                                        } else {
                                            state = "null";
                                        }
                                        if (country != null) {
                                            merged = city + "," + state + "," + country;

                                        } else {
                                            country = "null";
                                        }


                                    }
                                } catch (IOException | IllegalArgumentException e) {

                                    //  e.printStackTrace();
                                    Log.e("data", e.toString());
                                }
                            } else {
                                latitude = "0.0";
                                longitude = "0.0";
                                city = "null";
                                state = "null";
                                country = "null";
                            }
                        } catch (Exception e) {
                            latitude = "0.0";
                            longitude = "0.0";
                            city = "null";
                            state = "null";
                            country = "null";
                        }


                        register(userRegisterType,email, mobile, password, name, latitude, longitude, country, state, city, "0", "gcmtoken", "0", "Splendor Plus");

                    } else {
                        Toast.makeText(RegisterActivity.this, "network is not available", Toast.LENGTH_LONG).show();

                    }
                } else {
                    // do nothing
                }
            }
        });



        //this button is deactivated from from UI. (setVisibility Gone.)
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    if (CheckConnection.haveNetworkConnection(getApplicationContext())) {

                        String latitude = "";
                        String longitude = "";

                        latitude = String.valueOf(currentLatitude);
                        longitude = String.valueOf(currentLongitude);
                        String city = null, state = null, country = null;
                        String email = input_email.getText().toString().trim();
                        String mobile = input_mobile.getText().toString().trim();
                        String password = input_password.getText().toString().trim();
                        String name = input_name.getText().toString().trim();
                        try {
                            Geocoder geocoder;
                            List<Address> addresses = null;
                            geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            if (!latitude.equals("0.0") && !longitude.equals("0.0")) {
                                try {
                                    addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
                                    if (addresses != null && addresses.size() > 0) {
                                        String merged = "";
                                        city = addresses.get(0).getLocality();
                                        country = addresses.get(0).getCountryName();
                                        state = addresses.get(0).getAdminArea();
                                        if (city != null) {
                                            merged = city;
                                        } else {
                                            city = "null";
                                        }
                                        if (state != null) {
                                            merged = city + "," + state;

                                        } else {
                                            state = "null";
                                        }
                                        if (country != null) {
                                            merged = city + "," + state + "," + country;

                                        } else {
                                            country = "null";
                                        }


                                    }
                                } catch (IOException | IllegalArgumentException e) {

                                    //  e.printStackTrace();
                                    Log.e("data", e.toString());
                                }
                            } else {
                                latitude = "0.0";
                                longitude = "0.0";
                                city = "null";
                                state = "null";
                                country = "null";
                            }
                        } catch (Exception e) {
                            latitude = "0.0";
                            longitude = "0.0";
                            city = "null";
                            state = "null";
                            country = "null";
                        }


                        register(userRegisterType,email, mobile, password, name, latitude, longitude, country, state, city, "0", "gcmtoken", "0", "Splendor Plus");

                    } else {
                        Toast.makeText(RegisterActivity.this, "network is not available", Toast.LENGTH_LONG).show();

                    }
                } else {
                    // do nothing
                }
            }
        });
    }


    public Boolean validate() {
        Boolean value = true;

        if (input_name.getText().toString().trim().equals("")) {
            input_name.setError(getString(R.string.fiels_is_required));
            value = false;
        } else {
            input_name.setError(null);
        }


        if (input_mobile.getText().toString().trim().equals("")) {
            input_mobile.setError(getString(R.string.mobile_invalid));
            value = false;
        } else {
            input_mobile.setError(null);
        }


        /*if (!(input_password.length() >= 6)) {
            value = false;
            input_password.setError(getString(R.string.password_length));
        } else {
            input_password.setError(null);
        }
        if (!input_password.getText().toString().trim().equals("") && (!input_confirmPassword.getText().toString().trim().equals(input_password.getText().toString().trim()))) {
            value = false;
            input_confirmPassword.setError(getString(R.string.password_nomatch));
        } else {
            input_confirmPassword.setError(null);
        }*/


        return value;
    }

    public void BindView() {
        relative_signin = (RelativeLayout) findViewById(R.id.relative_signin);
        input_email = (TextInputEditText) findViewById(R.id.input_email);
        input_password = (TextInputEditText) findViewById(R.id.input_password);
        input_confirmPassword = (TextInputEditText) findViewById(R.id.input_confirmPassword);
        input_name = (TextInputEditText) findViewById(R.id.input_name);
        input_mobile = (TextInputEditText) findViewById(R.id.input_mobile);
        sign_up = (AppCompatButton) findViewById(R.id.sign_up);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        input_mobile.setText(phoneNumber);
        input_mobile.setEnabled(false);

    }

    public void applyfonts() {
        TextView textView = (TextView) findViewById(R.id.txt_register);
        Typeface font = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Medium.otf");
        Typeface font1 = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Book.otf");
        textView.setTypeface(font);
        input_email.setTypeface(font1);
        input_password.setTypeface(font1);
        input_confirmPassword.setTypeface(font1);
        input_mobile.setTypeface(font1);
        sign_up.setTypeface(font);
        AskPermission();

    }

    public void register(String regType, String email, String mobile, String password, String name, String latitude, String longitude,
                         String country , String state, String city, String mtype, String gcm_token, String utype, String vehicle_info) {
        RequestParams params = new RequestParams();
        params.put("reg_type",regType);
        params.put("email", email);
        params.put("mobile", mobile);
        params.put("password", password);
        params.put("name", name);
        params.put("latitude", latitude);
        params.put("longitude", longitude);
        params.put("country", country);
        params.put("state", state);
        params.put("city", city);
        params.put("mtype", mtype);
        params.put("utype", utype);
        params.put("vehicle_info", vehicle_info);
        params.put("avatar", "");

        String GcmToken = FirebaseInstanceId.getInstance().getToken();
        if (GcmToken != null) {
            params.put("gcm_token", GcmToken);
        } else {
            params.put("gcm_token", gcm_token);
        }


        Server.post("user/register/format/json", params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);


                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {

                        Toast.makeText(RegisterActivity.this, "success", Toast.LENGTH_LONG).show();

                        Gson gson = new Gson();
                        User user = gson.fromJson(response.getJSONObject("data").toString(), User.class);

                        Log.e("Atiar Register= ",user.toString());
                        SessionManager.getInstance().setKEY(mContext,user.getKey());
                        String userString = gson.toJson(user);
                        Log.e("Atiar Register user= ",userString);
                        SessionManager.getInstance().setUser(mContext,userString);
                        SessionManager.getInstance().setIsLogin(mContext);
                        CheckConnection checkConnection = new CheckConnection(RegisterActivity.this);
                        checkConnection.isAnonymouslyLoggedIn();

                        //SessionManager.getInstance().setKEY(response.getJSONObject("data").getString("key"));
                        Utils.setPreference(mContext,Utils.key_sessionKey,response.getJSONObject("data").getString("key"));
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                        finish();

                    } else {
                        Toast.makeText(RegisterActivity.this, response.getString("data"), Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    Toast.makeText(RegisterActivity.this, "error occurred", Toast.LENGTH_LONG).show();


                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(RegisterActivity.this, "error occurred", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);


            }

            @Override
            public void onFinish() {
                super.onFinish();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        try {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

            } else {
                //If everything went fine lets get latitude and longitude
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();

                //Toast.makeText(getActivity(), currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

    }

    public void getCurrentlOcation() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
    }

    public void tunonGps() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            mGoogleApiClient.connect();
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(30 * 1000);
            mLocationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            // **************************
            builder.setAlwaysShow(true); // this is the key ingredient
            // **************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result
                            .getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can
                            // initialize location
                            // requests here.
                            getCurrentlOcation();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and setting the result in onActivityResult().
                                status.startResolutionForResult(RegisterActivity.this, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have
                            // no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {

                getCurrentlOcation();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    public void AskPermission() {
        askCompactPermissions(permissionAsk, new PermissionResult() {
            @Override
            public void permissionGranted() {
                if (!GPSEnable()) {
                    tunonGps();
                } else {
                    getCurrentlOcation();
                }

            }

            @Override
            public void permissionDenied() {

            }

            @Override
            public void permissionForeverDenied() {
                //  openSettingsApp(getApplicationContext());
            }
        });
    }

    public Boolean GPSEnable() {
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;

        }
        return false;
    }
}
