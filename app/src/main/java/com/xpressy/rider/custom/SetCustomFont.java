package com.xpressy.rider.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by android on 12/4/17.
 */

public class SetCustomFont {
    public void overrideFonts(final Context context, final View v) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child);
                }
            } else if (v instanceof TextInputEditText) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "font/AvenirLTStd_Medium.otf"));
            } else if (v instanceof AppCompatButton) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "font/AvenirLTStd_Book.otf"));
            } else if (v instanceof EditText) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "font/AvenirLTStd_Medium.otf"));
            } else if (v instanceof TextView) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "font/AvenirLTStd_Medium.otf"));
            }
        } catch (Exception e) {
            Log.d("catch","font   "+e.toString());
        }
    }
}
