package com.xpressy.rider.custom;

import android.content.Context;
import android.content.SharedPreferences;
/**
 * Created by Atiar Talukdar on 12/02/19.
 * www.atiar.info
 * +8801917445888
 *
 * */

public class Utils {

    public static  final String country = "BD";
    public static  final String country_Code = "+8801";

   //public static  final String country = "KE";
   //public static  final String country_Code = "+254";
    /*****************************//* Strat shared preferences *//******************************/
    private static final String PREFS_NAME = "preferenceName";

    public static final String key_gcmToken = "gcmToken";
    public static final String key_userID = "DriverUserID";
    public static final String key_phoneNumber = "phoneNumber";
    public static final String key_sessionKey = "sessionKey";

    public static boolean setPreference(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static String getPreference(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getString(key, "None");
    }

    public static boolean setPreferenceInt(Context context, String key, int value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public static int getPreferenceInt(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getInt(key, 0);
    }
    public static int getPreferenceIntDrawable(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getInt(key,0);
    }

    public static void deletePreference(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        settings.edit().clear().commit();
    }


    /*****************************//* End shared preferences *//******************************/

}
