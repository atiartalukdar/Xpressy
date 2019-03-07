package com.xpressy.rider.acitivities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.javiersantos.appupdater.AppUpdater;
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
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thebrownarrow.permissionhelper.PermissionResult;
import com.thebrownarrow.permissionhelper.PermissionUtils;
import com.xpressy.rider.R;
import com.xpressy.rider.Server.Server;
import com.xpressy.rider.custom.CheckConnection;
import com.xpressy.rider.custom.GPSTracker;
import com.xpressy.rider.custom.Utils;
import com.xpressy.rider.fragement.AcceptedDetailFragment;
import com.xpressy.rider.fragement.AcceptedRequestFragment;
import com.xpressy.rider.fragement.HomeFragment;
import com.xpressy.rider.fragement.MapView;
import com.xpressy.rider.fragement.ProfileFragment;
import com.xpressy.rider.pojo.PendingRequestPojo;
import com.xpressy.rider.pojo.User;
import com.xpressy.rider.session.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.thebrownarrow.permissionhelper.ActivityManagePermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by android on 7/3/17.
 *
 * updated on 09/02/2019
 * force update added
 *
 * updated on 12/02/2019 by Atiar Talukdar
 * force GCM token generator in onCreate and update on server (UpdateGCM)
 *
 *
 */

public class HomeActivity extends ActivityManagePermission
        implements NavigationView.OnNavigationItemSelectedListener,
        ProfileFragment.ProfileUpdateListener, ProfileFragment.UpdateListener
        , GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    Context mContext;
    final String TAG = "Atiar - HomeActivity= ";

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    public Toolbar toolbar;
    TextView is_online, username;
    SwitchCompat switchCompat;
    LinearLayout linearLayout;
    NavigationView navigationView;

    private ImageView avatar;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        SessionManager sessionManager;

        mContext = this;
        AppUpdater appUpdater = new AppUpdater(this);
        appUpdater.start();


        //this try catch block generating the refreshed GCM token and storing that token to SP
        try {
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            Log.e("Atiar - HomeActivity = ", "Forced Refreshed token: " + refreshedToken);
            if (refreshedToken != null){
                Utils.setPreference(this,Utils.key_gcmToken,refreshedToken);
            }
            UpdateGCM(refreshedToken);
        } catch (Exception e) {
            e.printStackTrace();
        }

        BindView();


        sessionManager = new SessionManager(getApplicationContext());

        if (sessionManager.isLoggedIn(this)) {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("action")) {
                String action = intent.getStringExtra("action");
                AcceptedRequestFragment commonRequestFragment = new AcceptedRequestFragment();
                AcceptedDetailFragment acceptedRequestDetails = new AcceptedDetailFragment();
                Bundle b = new Bundle();
                b.putString("status", action);

                Log.e("Atiar - ", "HomeActivity , status = "+action);

                if (action.equals("ACCEPTED")){
                    acceptedRequestDetails.setArguments(b);
                    changeFragment(acceptedRequestDetails, "Ride Information");
                }else{
                    commonRequestFragment.setArguments(b);
                    changeFragment(commonRequestFragment, "details");
                }

            }

        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }



        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("action")) {
            String action = intent.getStringExtra("action");
            AcceptedRequestFragment acceptedRequestFragment = new AcceptedRequestFragment();
            Bundle bundle = new Bundle();
            bundle.putString("status", action);
            acceptedRequestFragment.setArguments(bundle);
            changeFragment(acceptedRequestFragment, "Requests");
        }







        Menu m = navigationView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);

            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }
            //the method we have create in activity
            applyFontToMenuItem(mi);
        }


    }

    private void setupDrawer() {
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        //  globatTitle = );
        getSupportActionBar().setTitle(getString(R.string.app_name));

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }
        };


        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        //drawer.shouldDelayChildPressedState();


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void drawer_close() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        AcceptedRequestFragment acceptedRequestFragment = new AcceptedRequestFragment();
        Bundle bundle;

        switch (item.getItemId()) {
            case R.id.home:
                changeFragment(new HomeFragment(), getString(R.string.home));
                break;
            case R.id.pending_requests:
                bundle = new Bundle();
                bundle.putString("status", "PENDING");
                acceptedRequestFragment.setArguments(bundle);
                changeFragment(acceptedRequestFragment, "Requests");
                break;
            case R.id.accepted_requests:
                bundle = new Bundle();
                bundle.putString("status", "ACCEPTED");
                acceptedRequestFragment.setArguments(bundle);
                changeFragment(acceptedRequestFragment, "Requests");
                break;
            case R.id.completed_rides:
                bundle = new Bundle();
                bundle.putString("status", "COMPLETED");
                acceptedRequestFragment.setArguments(bundle);
                changeFragment(acceptedRequestFragment, "Requests");
                break;
            case R.id.cancelled:
                bundle = new Bundle();
                bundle.putString("status", "CANCELLED");
                acceptedRequestFragment.setArguments(bundle);
                changeFragment(acceptedRequestFragment, "Requests");
                break;
            case R.id.track_ride:
                ThirdPersonInputDialogue();
//                changeFragment(new MapView(), getString(R.string.track_ride));
                break;
            case R.id.profile:
                changeFragment(new ProfileFragment(), getString(R.string.profile));
                break;
            case R.id.logout:
                SessionManager.getInstance().logoutUser(mContext);
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    public void ThirdPersonInputDialogue() {

        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.third_party_input_popup);
        dialog.setCancelable(true);
        //dialog.show();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        TextView submitButton_thirdpopup;
        EditText email_mobile_thirdpartyPopup;
        email_mobile_thirdpartyPopup = dialog.findViewById(R.id.email_mobile_thirdpartyPopup);
        email_mobile_thirdpartyPopup.setHint("Tracking number");
        submitButton_thirdpopup = dialog.findViewById(R.id.submitButton_thirdpopup);
        submitButton_thirdpopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String character = email_mobile_thirdpartyPopup.getText().toString();
                if (character.length() == 6) {
                    GetTrackDelivery(character,
                            "ACCEPTED", SessionManager.getInstance().getKEY(mContext),dialog);
                } else {
                    Toast.makeText(HomeActivity.this, "Please enter valid tracking number", Toast.LENGTH_LONG).show();

                }
            }
        });


        dialog.show();
    }


    public void GetTrackDelivery(String id, String status, String key,
                                 Dialog dialog ) {
        final RequestParams params = new RequestParams();
        params.put("tracking_id", id);
        params.put("status", status);
        Server.setHeader(key);
        Server.get("api/user/GetTrackDelivery/format/json", params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    System.out.println(response.toString());
                    Gson gson = new GsonBuilder().create();


                    dialog.dismiss();
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {
                        List<PendingRequestPojo> list = gson.fromJson(response.getJSONArray("data").toString(), new TypeToken<List<PendingRequestPojo>>() {
                        }.getType());

                        Log.e("data-customer", list.size() + " ");
                        if (response.has("data") && response.getJSONArray("data").length() == 0) {
                            Toast.makeText(HomeActivity.this,
                                    "This ride is not accept from driver side" +
                                            ", so please try after some time", Toast.LENGTH_LONG).show();
                            drawer_close();
                        } else {
                            if (GPSEnable()) {

                                Bundle bundle = new Bundle();
                                bundle.putSerializable("data", list.get(0));
                                MapView mapView = new MapView();
                                mapView.setArguments(bundle);
                                changeFragment(mapView, getString(R.string.track_ride));


                            } else {
                                turnonGps();
                            }
                        }


                    } else {
                        drawer_close();
                        Toast.makeText(HomeActivity.this, response.getString("data"), Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    dialog.dismiss();
//                    Toast.makeText(getActivity(), getString(R.string.contact_admin), Toast.LENGTH_LONG).show();


                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                dialog.dismiss();
            }
        });
    }


    public void changeFragment(final Fragment fragment, final String fragmenttag) {

        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawer_close();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().addToBackStack(null);
                    fragmentTransaction.replace(R.id.frame, fragment, fragmenttag);
                    fragmentTransaction.commit();
                    fragmentTransaction.addToBackStack(null);
                }
            }, 50);
        } catch (Exception e) {

        }

    }


    @Override
    public void update(String url) {
        if (!url.equals("")) {
            Glide.with(getApplicationContext()).load(url).error(R.drawable.images).into(avatar);
        }
    }

    @Override
    public void name(String name) {
        if (!name.equals("")) {
            username.setText(name);
        }
    }

    @SuppressLint("ParcelCreator")
    public class CustomTypefaceSpan extends TypefaceSpan {

        private final Typeface newType;

        public CustomTypefaceSpan(String family, Typeface type) {
            super(family);
            newType = type;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            applyCustomTypeFace(ds, newType);
        }

        @Override
        public void updateMeasureState(TextPaint paint) {
            applyCustomTypeFace(paint, newType);
        }

        private void applyCustomTypeFace(Paint paint, Typeface tf) {
            int oldStyle;
            Typeface old = paint.getTypeface();
            if (old == null) {
                oldStyle = 0;
            } else {
                oldStyle = old.getStyle();
            }

            int fake = oldStyle & ~tf.getStyle();
            if ((fake & Typeface.BOLD) != 0) {
                paint.setFakeBoldText(true);
            }

            if ((fake & Typeface.ITALIC) != 0) {
                paint.setTextSkewX(-0.25f);
            }

            paint.setTypeface(tf);
        }
    }


    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Medium.otf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    public void fontToTitleBar(String title) {
        try {
            Typeface font = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Book.otf");
            title = "<font color='#000000'>" + title + "</font>";
            SpannableString s = new SpannableString(title);
            s.setSpan(font, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                toolbar.setTitle(Html.fromHtml(String.valueOf(s), Html.FROM_HTML_MODE_LEGACY));
            } else {
                toolbar.setTitle((Html.fromHtml(String.valueOf(s))));
            }
        } catch (Exception e) {
            Log.e("catch", e.toString());
        }
    }


    public Fragment getVisibleFragment() {
        FragmentManager fragmentManager = HomeActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }

    public void BindView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        switchCompat = (SwitchCompat) navigationView.getHeaderView(0).findViewById(R.id.online);
        avatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.profile);
        linearLayout = (LinearLayout) navigationView.getHeaderView(0).findViewById(R.id.linear);
        is_online = (TextView) navigationView.getHeaderView(0).findViewById(R.id.is_online);
        username = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txt_name);
        TextView version = (TextView) navigationView.getHeaderView(0).findViewById(R.id.version);
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String ver = pInfo.versionName;
            version.setText("V ".concat(ver));
        } catch (PackageManager.NameNotFoundException e) {

        }

        navigationView.setCheckedItem(R.id.home);
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.home));
        setupDrawer();
        try {
            Typeface font = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Book.otf");
            username.setTypeface(font);
        } catch (Exception e) {

        }
        toolbar.setTitle("");

        if (CheckConnection.haveNetworkConnection(getApplicationContext())) {
            getUserInfo();

        } else {
            Toast.makeText(HomeActivity.this, getString(R.string.network), Toast.LENGTH_LONG).show();
            String name = SessionManager.getInstance().getName(getApplicationContext());
            String url = SessionManager.getInstance().getAvatar(getApplicationContext());
            username.setText(name);
            Glide.with(getApplicationContext()).load(url).error(R.mipmap.ic_account_circle_black_24dp).into(avatar);


        }
    }


    public void getUserInfo() {
        String uid = SessionManager.getInstance().getUid(getApplicationContext());
        RequestParams params = new RequestParams();
        params.put("user_id", uid);
        Server.setHeader(SessionManager.getInstance().getKEY(mContext));
        Server.get("api/user/profile/format/json", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {

                        Gson gson = new Gson();
                        User user = gson.fromJson(response.getJSONObject("data").toString(), User.class);
                        user.setKey(SessionManager.getInstance().getKEY(mContext));
                        SessionManager.getInstance().setUser(mContext,gson.toJson(user));

                        Glide.with(HomeActivity.this).load(user.getAvatar()).error(R.drawable.images).into(avatar);

                        username.setText(user.getName());

                    }
                } catch (Exception e) {

                }
            }

        });

    }


    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawer_close();
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
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

    public Boolean GPSEnable() {
        GPSTracker gpsTracker = new GPSTracker(HomeActivity.this);
        return gpsTracker.canGetLocation();
    }

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    public void turnonGps() {

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
                public void onResult(@NonNull LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result
                            .getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can
                            // initialize location
                            // requests here.

                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and setting the result in onActivityResult().
                                status.startResolutionForResult(HomeActivity.this, 1000);
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

    String permissionAsk[] = {PermissionUtils.Manifest_CAMERA, PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE, PermissionUtils.Manifest_READ_EXTERNAL_STORAGE, PermissionUtils.Manifest_ACCESS_FINE_LOCATION, PermissionUtils.Manifest_ACCESS_COARSE_LOCATION};

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        int result = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            android.location.Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                //If everything went fine lets get latitude and longitude

            }
        } else {
            askCompactPermissions(permissionAsk, new PermissionResult() {
                @Override
                public void permissionGranted() {

                }

                @Override
                public void permissionDenied() {
                }

                @Override
                public void permissionForeverDenied() {

                }
            });

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

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
        if (location != null) {

        }
    }

    public void UpdateGCM(String token) {

        if (token ==  null){
            token = Utils.getPreference(mContext,Utils.key_gcmToken);
        }
        RequestParams params = new RequestParams();
        params.put("gcm_token", token); //taking the GCM key from SP.

        Server.setHeader(SessionManager.getInstance().getKEY(mContext));

        params.put("user_id", SessionManager.getInstance().getUid(getApplicationContext()));
        Server.post("api/user/update/format/json", params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {

                        User user = SessionManager.getInstance().getUser(getApplicationContext());
                        Utils.setPreference(mContext,Utils.key_userID,user.getUser_id());

                    }
                } catch (JSONException e) {
                    Toast.makeText(mContext, getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });


    }




}
