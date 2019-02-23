package com.xpressy.rider.session;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.xpressy.rider.acitivities.LoginActivity;
import com.xpressy.rider.custom.Utils;
import com.xpressy.rider.pojo.User;

import java.util.HashMap;


/**
 * Created by android on 9/3/17.
 */


public class SessionManager {


    static SessionManager app;
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "taxiapp";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "name";
    public static final String KEY_MOBILE = "mobile";
    public static final String AVATAR = "avatar";
    public static final String GCM_TOKEN = "gcm_token";


    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";
    public static final String FARE_UNIT = "unit";
    public static final String COST = "cost";
    public static final String BASE_COST = "base_cost";
    public static final String LOGIN_AS = "login_as";
    public static final String USER_ID = "user_id";
    public static final String IS_ONLINE = "false";
    public String KEY = "key";
    public static final String USER = "user";
    public static final String VEHICLE = "vehicle";
    private String avatar;


    private SessionManager() {
    }


    public static SessionManager getInstance() {
        if (app == null) {
            app = new SessionManager();

        }
        return (app);
    }

    public SharedPreferences setPref(Context context) {

        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        return pref;

    }

    public String getName(Context context) {
        return getUser(context).getName();
    }

    public void setKEY(Context context, String k) {
        Utils.setPreference(context, Utils.key_sessionKey,k);
    }

    public void setGcmToken(Context context, String gcmToken) {
        Log.e("Atiar SessionManager - ", "GCM Token updated " + gcmToken);
        /*editor.putString(GCM_TOKEN, gcmToken);
        editor.commit();*/
        Utils.setPreference(context,GCM_TOKEN,gcmToken);

    }

    public void setUnit(Context context, String unit) {
        /*editor.putString(FARE_UNIT, unit);
        editor.commit();*/
        Utils.setPreference(context,FARE_UNIT,unit);
    }
    public void setCost(Context context, String cost) {
        /*editor.putString(COST, cost);
        editor.commit();*/
        Utils.setPreference(context,COST,cost);

    }

    public void setBaseCost(Context context, String baseCost) {
       /* editor.putString(BASE_COST, baseCost);
        editor.commit();*/
        Utils.setPreference(context,BASE_COST,baseCost);

    }

    public String getBASE_COST(Context context) {
      return   Utils.getPreference(context,BASE_COST);
    }
    public String getUnit(Context context) {
      //return   pref.getString(FARE_UNIT, null);
      return   Utils.getPreference(context,FARE_UNIT);
    }
    public String getCOST(Context context) {
      //return   pref.getString(COST, null);
      return   Utils.getPreference(context,COST);
    }

    public String getGcmToken() {
       return pref.getString(GCM_TOKEN, null);
    }


    public void setStatus(String staus) {
        editor.putString(IS_ONLINE, staus);
        editor.commit();
    }

    public String getStatus() {

        return pref.getString(IS_ONLINE, null);
    }

    public String getKEY(Context context) {
        return Utils.getPreference(context,Utils.key_sessionKey);

    }

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public void setIsLogin(Context context) {
        /*editor.putBoolean(IS_LOGIN, true);
        editor.commit();*/
        Utils.setPreference(context,IS_LOGIN,"true");
    }

    /**
     * Create login session
     */
    public void createLoginSession(String name, String email, String user, String avatar, String mobile) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_MOBILE, mobile);
        // Storing email in pref
        editor.putString(KEY_EMAIL, email);
        editor.putString(USER_ID, user);
        editor.putString(AVATAR, avatar);

        // commit changes
        editor.commit();
    }

    /**
     * Check login method wil setting user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    public void checkLogin(Context context) {
        // Check login status
        if (!this.isLoggedIn(context)) {
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }


    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_MOBILE, pref.getString(KEY_MOBILE, null));

        // user email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(USER_ID, pref.getString(USER_ID, null));
        user.put(AVATAR, pref.getString(AVATAR, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     */
    public void logoutUser(Context context) {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
        Utils.deletePreference(context);
        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        /*i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);*/
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick setting for login
     **/
    // Get Login State
    public boolean isLoggedIn(Context context) {
        boolean isL = false;
        if ((Utils.getPreference(context, IS_LOGIN)).equals("true")) {
            isL = true;
        }

        return isL;
    }

    public String getAvatar(Context context) {
        return getUser(context).getAvatar();
    }

    public String getUid(Context context) {
        return getUser(context).getUser_id();
    }

    public void setUser(Context context,String user) {
        /*editor.putString(USER, user);
        editor.commit();*/
        Utils.setPreference(context, USER,user);

    }

    public User getUser(Context con) {
        Gson gson = new Gson();
        return gson.fromJson(Utils.getPreference(con,USER), User.class);

    }

    public void setAvatar(Context context, String avatar) {
        /*editor.putString(AVATAR, avatar);
        editor.commit();*/
        Utils.setPreference(context, AVATAR,avatar);

    }

    public void setVehicle(Context context, String vehicle) {
        /*editor.putString(VEHICLE, vehicle);
        editor.commit();*/

        Utils.setPreference(context, VEHICLE,vehicle);

    }

    public String getVehicle(Context context) {
        //return pref.getString(VEHICLE, "");
        return Utils.getPreference(context, VEHICLE);
    }
}
