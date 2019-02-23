package com.xpressy.rider.Server;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.xpressy.rider.session.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by android on 18/4/17.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "firebase token";

    @Override
    public void onTokenRefresh() {

        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        if (refreshedToken !=null)
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.

        try {
            RequestParams params = new RequestParams();
            SessionManager.getInstance().setGcmToken(getApplicationContext(),token);
            Server.setHeader(SessionManager.getInstance().getKEY(getApplicationContext()));
            params.put("user_id", SessionManager.getInstance().getUid(getApplicationContext()));
            params.put("gcm_token", token);
            Server.postSync("api/user/update/format/json", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Log.d(TAG, response.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    Log.d(TAG, responseString);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
