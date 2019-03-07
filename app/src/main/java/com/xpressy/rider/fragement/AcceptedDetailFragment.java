package com.xpressy.rider.fragement;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.xpressy.rider.R;
import com.xpressy.rider.Server.Server;
import com.xpressy.rider.acitivities.HomeActivity;
import com.xpressy.rider.custom.CheckConnection;
import com.xpressy.rider.custom.GPSTracker;
import com.xpressy.rider.custom.SetCustomFont;
import com.xpressy.rider.custom.Utils;
import com.xpressy.rider.pojo.PendingRequestPojo;
import com.xpressy.rider.pojo.Tracking;
import com.xpressy.rider.session.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.thebrownarrow.permissionhelper.FragmentManagePermission;
import com.thebrownarrow.permissionhelper.PermissionResult;
import com.thebrownarrow.permissionhelper.PermissionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by android on 14/3/17.
 *
 * Edited by Atiar on 16/02/2019
 * Added driver contact information from UI
 *
 */

public class AcceptedDetailFragment extends FragmentManagePermission
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    double finalAmount = 0.0;


    Context mContext;
    private View view;
    AppCompatButton trackRide;
    Bundle bundle;
    SessionManager sessionManager;
    String userid = "";
    String key = "";

    private String mobile = "";
    AppCompatButton btn_cancel, btn_payment, btn_complete;
    LinearLayout linearChat;
    TextView title, drivername, mobilenumber, pickup_location, drop_location, fare, payment_status;
    private AlertDialog alert;
    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
    private static final String CONFIG_ENVIRONMENT = Server.ENVIRONMENT;
    private static PayPalConfiguration config;
    PayPalPayment thingToBuy;
    TableRow mobilenumber_row;
    PendingRequestPojo pojo;
    String permissions[] = {PermissionUtils.Manifest_ACCESS_FINE_LOCATION, PermissionUtils.Manifest_ACCESS_COARSE_LOCATION};
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Double currentLatitude;
    private Double currentLongitude;

    String permissionAsk[] = {PermissionUtils.Manifest_CAMERA, PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE, PermissionUtils.Manifest_READ_EXTERNAL_STORAGE, PermissionUtils.Manifest_ACCESS_FINE_LOCATION, PermissionUtils.Manifest_ACCESS_COARSE_LOCATION};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mContext = container.getContext();
        view = inflater.inflate(R.layout.accepted_detail_fragmnet, container, false);
        ((HomeActivity) getActivity()).fontToTitleBar(getString(R.string.passenger_info));


        btn_complete = (AppCompatButton) view.findViewById(R.id.btn_complete);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mobilenumber_row = (TableRow) view.findViewById(R.id.mobilenumber_row);
        linearChat = (LinearLayout) view.findViewById(R.id.linear_chat);
        title = (TextView) view.findViewById(R.id.title);
        drivername = (TextView) view.findViewById(R.id.driver_name);
        mobilenumber = (TextView) view.findViewById(R.id.txt_mobilenumber);
        pickup_location = (TextView) view.findViewById(R.id.txt_pickuplocation);
        drop_location = (TextView) view.findViewById(R.id.txt_droplocation);
        fare = (TextView) view.findViewById(R.id.txt_basefare);
        trackRide = (AppCompatButton) view.findViewById(R.id.btn_trackride);
        btn_payment = (AppCompatButton) view.findViewById(R.id.btn_payment);
        btn_cancel = (AppCompatButton) view.findViewById(R.id.btn_cancel);
        payment_status = (TextView) view.findViewById(R.id.txt_paymentstatus);
        pickup_location.setSelected(true);
        drop_location.setSelected(true);
        sessionManager = new SessionManager(getActivity());



        try{
            Bundle bundle = getArguments();
            if (bundle.getString("status") != null && bundle.getString("status").equals("ACCEPTED")){
                Log.e("Atiar - ", "AcceptedDetailsFragment = "+"Accepted Called");
                sessionManager = new SessionManager(getActivity());
                if (sessionManager != null) {
                    HashMap<String, String> users = sessionManager.getUserDetails();
                    if (users != null) {
                        String id = users.get(SessionManager.USER_ID);
                        if (id != null) {
                            userid = id;
                        }
                        String k = sessionManager.getKEY(mContext);
                        if (k != null) {
                            key = k;
                        }

                    }
                }


                if (!CheckConnection.haveNetworkConnection(getActivity())) {
                    getAcceptedRequest(userid,  bundle.getString("status"), key);
                }

            }else {
                Log.e("Atiar - ", "AcceptedDetailsFragment = "+"BindView Called");
                BindView();
            }
        }catch (Exception e){
            e.printStackTrace();
        }



/*
        BindView();
*/
        configPaypal();



        if (!CheckConnection.haveNetworkConnection(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            askCompactPermissions(permissionAsk, new PermissionResult() {
                @Override
                public void permissionGranted() {
                    if (!GPSEnable()) {
                        turnonGps();
                    } else {
                        getcurrentlocation();
                    }
                }

                @Override
                public void permissionDenied() {

                }

                @Override
                public void permissionForeverDenied() {

                    openSettingsApp(getActivity());
                }
            });

        } else {
            if (!GPSEnable()) {
                turnonGps();
            } else {
                getcurrentlocation();
            }

        }

//        trackRide.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        return view;
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

    public void BindView() {

        Bundle bundle = getArguments();
        if (bundle != null) {
            pojo = (PendingRequestPojo) bundle.getSerializable("data");
            title.setText(getString(R.string.taxi));
            pickup_location.setText(pojo.getPickup_adress() + " ");
            drop_location.setText(pojo.getDrop_address());
            drivername.setText(pojo.getDriver_name());
            if (pojo.getFinal_amount() == null || pojo.getFinal_amount().equalsIgnoreCase("")) {
                fare.setText(pojo.getAmount() + " " + SessionManager.getInstance().getUnit(getContext()));
            } else {
                fare.setText(pojo.getFinal_amount() + " " + SessionManager.getInstance().getUnit(getContext()));
            }

            mobilenumber.setText(pojo.getDriver_mobile());
            mobile = pojo.getDriver_mobile();

            mobilenumber_row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    askCompactPermission(PermissionUtils.Manifest_CALL_PHONE, new PermissionResult() {
                        @Override
                        public void permissionGranted() {

                            if (mobile != null && !mobile.equals("")) {
                                Intent callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setData(Uri.parse("tel:" + mobile));
                                startActivity(callIntent);
                            }
                        }

                        @Override
                        public void permissionDenied() {

                        }

                        @Override
                        public void permissionForeverDenied() {

                        }
                    });
                }
            });

            if (pojo.getStatus().equalsIgnoreCase("PENDING")) {
                btn_cancel.setVisibility(View.VISIBLE);
            }
            if (pojo.getStatus().equalsIgnoreCase("CANCELLED")) {
                btn_complete.setVisibility(View.GONE);
                btn_cancel.setVisibility(View.GONE);
                btn_payment.setVisibility(View.GONE);
                trackRide.setVisibility(View.GONE);
                payment_status.setText(pojo.getPayment_status());
            }
            if (pojo.getStatus().equalsIgnoreCase("COMPLETED")) {
                btn_payment.setVisibility(View.GONE);
                trackRide.setVisibility(View.GONE);
                btn_cancel.setVisibility(View.GONE);
                btn_complete.setVisibility(View.GONE);
                payment_status.setText(pojo.getPayment_status());
            }
            if (pojo.getStatus().equalsIgnoreCase("ACCEPTED")) {
                trackRide.setVisibility(View.VISIBLE);
                if (pojo.getPayment_status().equals("") && pojo.getPayment_mode().equals("")) {

                    btn_cancel.setVisibility(View.VISIBLE);

                    btn_payment.setVisibility(View.VISIBLE);
                } else {
                    btn_complete.setVisibility(View.VISIBLE);

                    mobilenumber_row.setVisibility(View.VISIBLE);
                }
                if (!pojo.getPayment_status().equals("PAID") && pojo.getPayment_mode().equals("OFFLINE")) {
                    btn_complete.setVisibility(View.GONE);
                }
            }

            if (pojo.getPayment_status().equals("") && pojo.getPayment_mode().equals("")) {
                payment_status.setText(getString(R.string.unpaid));

            } else {
                if (!pojo.getPayment_status().equals("PAID") && pojo.getPayment_mode().equals("OFFLINE")) {
                    payment_status.setText(R.string.cash_on_hand);

                } else {
                    payment_status.setText(pojo.getPayment_status());
                }

            }

        }

        SetCustomFont setCustomFont = new SetCustomFont();
        setCustomFont.overrideFonts(getActivity(), view);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        isStarted();

       /* linearChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                b.putString("name", pojo.getDriver_name());
                b.putString("id", pojo.getRide_id());
                b.putString("user_id", pojo.getDriver_id());
                ChatFragment chatFragment = new ChatFragment();
                chatFragment.setArguments(b);
                ((HomeActivity) getActivity()).changeFragment(chatFragment, "Messages");
            }
        });*/


        btn_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckConnection.haveNetworkConnection(getActivity())) {

                    new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.payment_method)).setItems(R.array.payment_mode, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {

                                RequestParams params = new RequestParams();
                                params.put("ride_id", pojo.getRide_id());
                                params.put("payment_mode", "OFFLINE");
                                //params.put("final_amount",getFinalAmount());
                                Server.setContetntType();
                                Server.setHeader(SessionManager.getInstance().getKEY(mContext));
                                Server.post("api/user/rides", params, new JsonHttpResponseHandler() {
                                    @Override
                                    public void onStart() {
                                        swipeRefreshLayout.setRefreshing(true);
                                    }

                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        super.onSuccess(statusCode, headers, response);
                                        pojo.setPayment_mode("OFFLINE");
                                        if (pojo.getPayment_mode().equals("OFFLINE")) {
                                            payment_status.setText(R.string.cash_on_hand);
                                        } else {
                                            payment_status.setText(pojo.getPayment_status());
                                        }

                                        btn_payment.setVisibility(View.GONE);
                                        trackRide.setVisibility(View.VISIBLE);
                                        Toast.makeText(getActivity(), getString(R.string.payment_update), Toast.LENGTH_LONG).show();
                                        makeOfflinPayment("COMPLETED");
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                        super.onFailure(statusCode, headers, responseString, throwable);

                                    }

                                    @Override
                                    public void onFinish() {
                                        super.onFinish();
                                        if (getActivity() != null) {
                                            swipeRefreshLayout.setRefreshing(false);
                                        }
                                    }
                                });

                            } else {
                                MakePayment();
                            }
                        }
                    }).create().show();

                    //MakePayment();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialogCreate(getString(R.string.ride_request_cancellation),
                        getString(R.string.want_to_cancel), "CANCELLED");
            }
        });


        btn_complete.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialogCreate(getString(R.string.ride_request_cancellation), getString(R.string.want_to_accept), "COMPLETED");
                    }
                });
    }

    public void BindViewForAcceptedRequest() {

        bundle = getArguments();
        if (bundle != null) {
            pojo = (PendingRequestPojo) bundle.getSerializable("data");

            title.setText(getString(R.string.taxi));
            pickup_location.setText(pojo.getPickup_adress() + " ");
            drop_location.setText(pojo.getDrop_address());
            drivername.setText(pojo.getDriver_name());
            if (pojo.getFinal_amount() == null || pojo.getFinal_amount().equalsIgnoreCase("")) {
                fare.setText(pojo.getAmount() + " " + SessionManager.getInstance().getUnit(getContext()));
            } else {
                fare.setText(pojo.getFinal_amount() + " " + SessionManager.getInstance().getUnit(getContext()));
            }

            mobilenumber.setText(pojo.getDriver_mobile());
            mobile = pojo.getDriver_mobile();

            mobilenumber_row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    askCompactPermission(PermissionUtils.Manifest_CALL_PHONE, new PermissionResult() {
                        @Override
                        public void permissionGranted() {

                            if (mobile != null && !mobile.equals("")) {
                                Intent callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setData(Uri.parse("tel:" + mobile));
                                startActivity(callIntent);
                            }
                        }

                        @Override
                        public void permissionDenied() {

                        }

                        @Override
                        public void permissionForeverDenied() {

                        }
                    });
                }
            });

            if (pojo.getStatus().equalsIgnoreCase("PENDING")) {
                btn_cancel.setVisibility(View.VISIBLE);
            }
            if (pojo.getStatus().equalsIgnoreCase("CANCELLED")) {
                btn_complete.setVisibility(View.GONE);
                btn_cancel.setVisibility(View.GONE);
                btn_payment.setVisibility(View.GONE);
                trackRide.setVisibility(View.GONE);
                payment_status.setText(pojo.getPayment_status());
            }
            if (pojo.getStatus().equalsIgnoreCase("COMPLETED")) {
                btn_payment.setVisibility(View.GONE);
                trackRide.setVisibility(View.GONE);
                btn_cancel.setVisibility(View.GONE);
                btn_complete.setVisibility(View.GONE);
                payment_status.setText(pojo.getPayment_status());
            }
            if (pojo.getStatus().equalsIgnoreCase("ACCEPTED")) {
                trackRide.setVisibility(View.VISIBLE);
                if (pojo.getPayment_status().equals("") && pojo.getPayment_mode().equals("")) {

                    btn_cancel.setVisibility(View.VISIBLE);

                    btn_payment.setVisibility(View.VISIBLE);
                } else {
                    btn_complete.setVisibility(View.VISIBLE);

                    mobilenumber_row.setVisibility(View.VISIBLE);
                }
                if (!pojo.getPayment_status().equals("PAID") && pojo.getPayment_mode().equals("OFFLINE")) {
                    btn_complete.setVisibility(View.GONE);
                }
            }

            if (pojo.getPayment_status().equals("") && pojo.getPayment_mode().equals("")) {
                payment_status.setText(getString(R.string.unpaid));

            } else {
                if (!pojo.getPayment_status().equals("PAID") && pojo.getPayment_mode().equals("OFFLINE")) {
                    payment_status.setText(R.string.cash_on_hand);

                } else {
                    payment_status.setText(pojo.getPayment_status());
                }

            }

        }

        SetCustomFont setCustomFont = new SetCustomFont();
        setCustomFont.overrideFonts(getActivity(), view);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        isStarted();


        btn_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckConnection.haveNetworkConnection(getActivity())) {

                    new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.payment_method)).setItems(R.array.payment_mode, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                RequestParams params = new RequestParams();
                                params.put("ride_id", pojo.getRide_id());
                                params.put("payment_mode", "OFFLINE");
                                Server.setContetntType();
                                Server.setHeader(SessionManager.getInstance().getKEY(mContext));
                                Server.post("api/user/rides", params, new JsonHttpResponseHandler() {
                                    @Override
                                    public void onStart() {
                                        swipeRefreshLayout.setRefreshing(true);
                                    }

                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        super.onSuccess(statusCode, headers, response);
                                        pojo.setPayment_mode("OFFLINE");
                                        if (pojo.getPayment_mode().equals("OFFLINE")) {
                                            payment_status.setText(R.string.cash_on_hand);
                                        } else {
                                            payment_status.setText(pojo.getPayment_status());
                                        }

                                        btn_payment.setVisibility(View.GONE);
                                        trackRide.setVisibility(View.GONE);
                                        btn_cancel.setVisibility(View.GONE);
                                        Toast.makeText(getActivity(), getString(R.string.payment_update), Toast.LENGTH_LONG).show();
                                        makeOfflinPayment("COMPLETED");

                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                        super.onFailure(statusCode, headers, responseString, throwable);

                                    }

                                    @Override
                                    public void onFinish() {
                                        super.onFinish();
                                        if (getActivity() != null) {
                                            swipeRefreshLayout.setRefreshing(false);
                                        }
                                    }
                                });

                            } else {
                                MakePayment();
                            }
                        }
                    }).create().show();

                    //MakePayment();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialogCreate(getString(R.string.ride_request_cancellation),
                        getString(R.string.want_to_cancel), "CANCELLED");
            }
        });

        btn_complete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialogCreate(getString(R.string.ride_request_cancellation), getString(R.string.want_to_accept), "COMPLETED");
                    }
                });
    }

    public void Updatepayemt(String ride_id, String payment_status) {
        RequestParams params = new RequestParams();
        params.put("ride_id", ride_id);
        params.put("payment_status", payment_status);
        params.put("payment_mode", "PAYPAL");
        Server.setContetntType();
        Server.setHeader(SessionManager.getInstance().getKEY(mContext));

        Server.post("api/user/rides", params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Toast.makeText(getActivity(), getString(R.string.payment_update), Toast.LENGTH_LONG).show();
                ((HomeActivity) getActivity()).onBackPressed();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

                Toast.makeText(getActivity(), getString(R.string.error_payment), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(getActivity(), getString(R.string.server_not_respond), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (getActivity() != null) {
                    swipeRefreshLayout.setRefreshing(false);

                }
            }
        });

    }

    public void satisfied_popupDialogue(final String status,
                                        final String final_fare) {
        if (getActivity() == null)
            return;

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.satisfied_popup_layout);
        dialog.setCancelable(false);
        //dialog.show();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        ImageView happyIcon_Popup, sadIcon_Popup;
        TextView finalPrice_popup;

        happyIcon_Popup = dialog.findViewById(R.id.happyIcon_Popup);
        sadIcon_Popup = dialog.findViewById(R.id.sadIcon_Popup);
        finalPrice_popup = dialog.findViewById(R.id.finalPrice_popup);

        if (SessionManager.getInstance().getUnit(getContext()) != null &&
                !SessionManager.getInstance().getUnit(getContext()).equalsIgnoreCase("")) {
            finalPrice_popup.setText(final_fare + " " + SessionManager.getInstance().getUnit(getContext()));

        } else {
            finalPrice_popup.setText(final_fare);
        }

        happyIcon_Popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                feedback_complaints_popupDialogue(status, final_fare, true);


            }
        });
        sadIcon_Popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                feedback_complaints_popupDialogue(status, final_fare, false);
            }
        });
        dialog.show();
    }

    public void feedback_complaints_popupDialogue(String status, String final_fare, boolean isFeedbackOrNot) {
        if (getActivity() == null)
            return;

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.feedback_complaints_popup_layout);
        dialog.setCancelable(false);
        //dialog.show();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        TextView titletext_FCpopup;
        TextView skip_FCpopup, submit_FCpopup;
        EditText inputUser_FCpopup;

        titletext_FCpopup = dialog.findViewById(R.id.titletext_FCpopup);
        skip_FCpopup = dialog.findViewById(R.id.skip_FCpopup);
        submit_FCpopup = dialog.findViewById(R.id.submit_FCpopup);
        inputUser_FCpopup = dialog.findViewById(R.id.inputUser_FCpopup);

        if (isFeedbackOrNot) {
            titletext_FCpopup.setText(getResources().getString(R.string.feedback));
            inputUser_FCpopup.setHint(getResources().getString(R.string.write_your_feedback));
        } else {
            titletext_FCpopup.setText(getResources().getString(R.string.complaints));
            inputUser_FCpopup.setHint(getResources().getString(R.string.write_your_complaints));
        }

        submit_FCpopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (inputUser_FCpopup.getText().toString().equalsIgnoreCase("")) {
                    if (isFeedbackOrNot) {
                        Toast.makeText(getActivity(), "Please write your feedback.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Please write your complaints.", Toast.LENGTH_LONG).show();

                    }
                } else {
                    dialog.dismiss();
                    if (isFeedbackOrNot) {
                        sendStatus(pojo.getRide_id(), status, JSONObject.NULL.toString()
                                , inputUser_FCpopup.getText().toString(), final_fare);

                    } else {
                        sendStatus(pojo.getRide_id(), status,
                                inputUser_FCpopup.getText().toString()
                                , JSONObject.NULL.toString(), final_fare);

                    }
                }


            }
        });

        skip_FCpopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                sendStatus(pojo.getRide_id(), status, JSONObject.NULL.toString()
                        , JSONObject.NULL.toString(), final_fare);

            }
        });
        dialog.show();
    }

    public void AlertDialogCreate(String title, String message, final String status) {
        if (getActivity() == null)
            return;
        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.mipmap.ic_warning_white_24dp);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, Color.RED);
        new AlertDialog.Builder(getActivity())
                .setIcon(drawable)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();


                        if (status.equalsIgnoreCase("COMPLETED")) {
                            Double finalfare = (double) 0;
                            Double finalDistance = (double) 0;
                            if (SessionManager.getInstance().getBASE_COST(getContext()) != null &&
                                    !SessionManager.getInstance().getBASE_COST(getContext())
                                            .equalsIgnoreCase("")
                                    && SessionManager.getInstance().getCOST(getContext()) != null &&
                                    !SessionManager.getInstance().getCOST(getContext())
                                            .equalsIgnoreCase("")) {
                                Double aDouble = distance(currentLatitude, currentLongitude
                                        , getLocationFromAddress(pojo.getPickup_adress()).latitude
                                        , getLocationFromAddress(pojo.getPickup_adress()).longitude);

                                finalDistance = aDouble;
                                Log.e("Atiar - ", "From Discatnce calculator - Distance = " + aDouble);

                                int reminderValue = (int) ((Integer.parseInt(
                                        SessionManager.getInstance().getBASE_COST(getContext()))
                                        + ((aDouble - 2) *
                                        Integer.parseInt(SessionManager.getInstance().getCOST(getContext()))))
                                        % 10);
                                Log.e("Atiar - ", "From Discatnce calculator -  reminderValue = " + aDouble);

                                finalfare = Integer.parseInt(
                                        SessionManager.getInstance().getBASE_COST(getContext()))
                                        + ((aDouble - 2) *
                                        Integer.parseInt(SessionManager.getInstance().getCOST(getContext())));

                                Log.e("Atiar - ", "From Discatnce calculator -  Final fare = " + aDouble);


                                if (reminderValue >= 1 && reminderValue <= 4) {
                                    finalfare = finalfare - reminderValue;
                                } else if (reminderValue >= 5 && reminderValue <= 9) {
                                    finalfare = finalfare + (10 - reminderValue);
                                }

                                if (finalfare <= Double.parseDouble(SessionManager.getInstance().getBASE_COST(getContext()))){
                                    finalfare = Double.parseDouble(SessionManager.getInstance().getBASE_COST(getContext()));
                                }
                            }
                            updateFinalFareToServer(finalDistance+"", finalfare+"");
                            satisfied_popupDialogue(status, String.valueOf(finalfare).split("\\.")[0]);

                        } else {
                            sendStatus(pojo.getRide_id(), status, JSONObject.NULL.toString()
                                    , JSONObject.NULL.toString(), pojo.getAmount());

                        }
//                        txt_fare.setText(String.format("%.0f", finalfare );

//                        System.out.println(currentLatitude+","+ currentLongitude);
//                        System.out.println(distance(currentLatitude,currentLongitude
//                        ,getLocationFromAddress(pojo.getPickup_adress()).latitude
//                                ,getLocationFromAddress(pojo.getPickup_adress()).longitude));

                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    public void makeOfflinPayment(final String status) {
        if (status.equalsIgnoreCase("COMPLETED")) {
            Double finalfare = (double) 0;
            Double finalDistance = (double) 0;
            if (SessionManager.getInstance().getBASE_COST(getContext()) != null &&
                    !SessionManager.getInstance().getBASE_COST(getContext())
                            .equalsIgnoreCase("")
                    && SessionManager.getInstance().getCOST(getContext()) != null &&
                    !SessionManager.getInstance().getCOST(getContext())
                            .equalsIgnoreCase("")) {
                Double aDouble = distance(currentLatitude, currentLongitude
                        , getLocationFromAddress(pojo.getPickup_adress()).latitude
                        , getLocationFromAddress(pojo.getPickup_adress()).longitude);

                finalDistance = aDouble;
                Log.e("Atiar - ", "From Discatnce calculator - Distance = " + aDouble);

                int reminderValue = (int) ((Integer.parseInt(
                        SessionManager.getInstance().getBASE_COST(getContext()))
                        + ((aDouble - 2) *
                        Integer.parseInt(SessionManager.getInstance().getCOST(getContext()))))
                        % 10);
                Log.e("Atiar - ", "From Discatnce calculator -  reminderValue = " + aDouble);

                finalfare = Integer.parseInt(
                        SessionManager.getInstance().getBASE_COST(getContext()))
                        + ((aDouble - 2) *
                        Integer.parseInt(SessionManager.getInstance().getCOST(getContext())));

                Log.e("Atiar - ", "From Discatnce calculator -  Final fare = " + aDouble);


                if (reminderValue >= 1 && reminderValue <= 4) {
                    finalfare = finalfare - reminderValue;
                } else if (reminderValue >= 5 && reminderValue <= 9) {
                    finalfare = finalfare + (10 - reminderValue);
                }

                if (finalfare <= Double.parseDouble(SessionManager.getInstance().getBASE_COST(getContext()))){
                    finalfare = Double.parseDouble(SessionManager.getInstance().getBASE_COST(getContext()));
                }
            }
            updateFinalFareToServer(finalDistance+"", finalfare+"");
            satisfied_popupDialogue(status, String.valueOf(finalfare).split("\\.")[0]);

        } else {
            sendStatus(pojo.getRide_id(), status, JSONObject.NULL.toString()
                    , JSONObject.NULL.toString(), pojo.getAmount());

        }
    }


    public void cancelAlert(String title, String message, final String status) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(title);

        alertDialog.setMessage(message);
        alertDialog.setCancelable(true);
        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.mipmap.ic_warning_white_24dp);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, Color.RED);
        alertDialog.setIcon(drawable);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                sendStatus(pojo.getRide_id(), status);

            }
        });


        alertDialog.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alert.cancel();
            }
        });
        alert = alertDialog.create();
        alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert.show();
    }

    public void sendStatus(String ride_id, final String status, String complaints, String feedback
            , String final_fare) {

        RequestParams params = new RequestParams();
        params.put("ride_id", ride_id);
        params.put("status", status);
        params.put("feedback", feedback);
        params.put("complaint", complaints);
        params.put("final_amount", final_fare);

        Server.setHeader(SessionManager.getInstance().getKEY(mContext));
        Server.setContetntType();
        Server.post("api/user/rides", params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {

                    AcceptedRequestFragment acceptedRequestFragment = new AcceptedRequestFragment();
                    Bundle bundle = null;
                    if (response.has("status") && response.getString("status").equals("success")) {
                        if (status.equalsIgnoreCase("COMPLETED")) {
                            Toast.makeText(getActivity(), getString(R.string.ride_request_completed), Toast.LENGTH_LONG).show();
                            bundle = new Bundle();
                            bundle.putString("status", "COMPLETED");
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.ride_request_cancelled), Toast.LENGTH_LONG).show();
                            bundle = new Bundle();
                            bundle.putString("status", "CANCELLED");
                        }
                        acceptedRequestFragment.setArguments(bundle);
                        ((HomeActivity) getActivity()).changeFragment(acceptedRequestFragment, "Accepted Request");
                    } else {
                        String error = response.getString("data");
                        Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(getActivity(), getString(R.string.try_again), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (getActivity() != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

    }

    public void configPaypal() {
        config = new PayPalConfiguration()
                .environment(CONFIG_ENVIRONMENT)
                .clientId(Server.PAYPAL_KEY)
                .merchantName(getString(R.string.merchant_name))
                .merchantPrivacyPolicyUri(
                        Uri.parse(getString(R.string.merchant_privacy)))
                .merchantUserAgreementUri(
                        Uri.parse(getString(R.string.merchant_user_agreement)));
    }

    public void MakePayment() {

        if (getActivity() == null)
            return;

        if (pojo.getAmount() != null && !pojo.getAmount().equals("")) {
            Intent intent = new Intent(getActivity(), PayPalService.class);
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
            getActivity().startService(intent);
            thingToBuy = new PayPalPayment(new java.math.BigDecimal(String.valueOf(pojo.getAmount())), getString(R.string.paypal_payment_currency), "Ride Payment", PayPalPayment.PAYMENT_INTENT_SALE);
            Intent payment = new Intent(getActivity(),
                    PaymentActivity.class);

            payment.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
            payment.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

            startActivityForResult(payment, REQUEST_CODE_PAYMENT);


        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                //  String.valueOf(finalfare)
                PaymentConfirmation confirm = data
                        .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        System.out.println(confirm.toJSONObject().toString(4));
                        System.out.println(confirm.getPayment().toJSONObject()
                                .toString(4));
                        Updatepayemt(pojo.getRide_id(), "PAID");

                    } catch (JSONException e) {

                        Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), getString(R.string.payment_hbeen_cancelled), Toast.LENGTH_LONG).show();
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                //  Log.d("payment", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        } else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PayPalAuthorization auth = data
                        .getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
                if (auth != null) {
                    try {
                        Log.i("FuturePaymentExample", auth.toJSONObject()
                                .toString(4));

                        String authorization_code = auth.getAuthorizationCode();
                        Log.d("FuturePaymentExample", authorization_code);

                        /*sendAuthorizationToServer(auth);
                        Toast.makeText(getActivity(),
                                "Future Payment code received from PayPal",
                                Toast.LENGTH_LONG).show();*/
                        Log.e("paypal", "future Payment code received from PayPal  :" + authorization_code);

                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "failure Occurred", Toast.LENGTH_LONG).show();
                        Log.e("FuturePaymentExample",
                                "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), getString(R.string.payment_hbeen_cancelled), Toast.LENGTH_LONG).show();

                Log.d("FuturePaymentExample", "The user canceled.");
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {

                Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

                Log.d("FuturePaymentExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
            }
        }

    }

    public Boolean GPSEnable() {
        GPSTracker gpsTracker = new GPSTracker(getActivity());
        return gpsTracker.canGetLocation();
    }

    public void getcurrentlocation() {

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
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

    public void turnonGps() {

        if (getActivity() == null)
            return;

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
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
                                status.startResolutionForResult(getActivity(), 1000);
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
    public void onConnected(@Nullable Bundle bundle) {
        if (getActivity() == null)
            return;
        int result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            android.location.Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                //If everything went fine lets get latitude and longitude
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                if (SessionManager.getInstance().getBASE_COST(getContext()) == null
                        || SessionManager.getInstance().getCOST(getContext()) == null) {
                    NeaBy(String.valueOf(currentLatitude), String.valueOf(currentLongitude)
                            , pojo.getVehicle_type(), pojo.getRide_type());
                }
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
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
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            if (SessionManager.getInstance().getBASE_COST(getContext()) == null
                    || SessionManager.getInstance().getCOST(getContext()) == null) {
                NeaBy(String.valueOf(currentLatitude), String.valueOf(currentLongitude)
                        , pojo.getVehicle_type(), pojo.getRide_type());
            }
        }
    }


    private void isStarted() {

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference().child("Tracking/" + pojo.getRide_id());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot + " dataSnapshot " + dataSnapshot.hasChildren());
                if (dataSnapshot != null && dataSnapshot.hasChildren()) {
                    Tracking tracking = dataSnapshot.getValue(Tracking.class);

                    System.out.println(tracking.getStatus() + " getStatus");
                    if (tracking.getStatus().equalsIgnoreCase("accepted")) {
                        trackRide.setText(getString(R.string.Track_Driver));

                        trackRide.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                System.out.println(tracking.getStatus() + " Track_Driver");
                                askCompactPermissions(permissions, new PermissionResult() {
                                    @Override
                                    public void permissionGranted() {

                                        if (GPSEnable()) {

                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("data", pojo);
                                            MapView mapView = new MapView();
                                            mapView.setArguments(bundle);
                                            ((HomeActivity) getActivity()).changeFragment(mapView, "MapView");


                                        } else {
                                            turnonGps();
                                        }
                                    }

                                    @Override
                                    public void permissionDenied() {

                                    }

                                    @Override
                                    public void permissionForeverDenied() {

                                    }
                                });
                            }
                        });


                    } else if (tracking.getStatus().equalsIgnoreCase("started")) {
                        trackRide.setText(getString(R.string.Track_Ride));

                        trackRide.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                System.out.println(tracking.getStatus() + " Track_Ride");
                                askCompactPermissions(permissions, new PermissionResult() {
                                    @Override
                                    public void permissionGranted() {
                                        if (GPSEnable()) {

                                            try {
                                                String[] latlong = pojo.getPikup_location().split(",");
                                                double latitude = Double.parseDouble(latlong[0]);
                                                double longitude = Double.parseDouble(latlong[1]);
                                                String[] latlong1 = pojo.getDrop_locatoin().split(",");
                                                double latitude1 = Double.parseDouble(latlong1[0]);
                                                double longitude1 = Double.parseDouble(latlong1[1]);


// Create a NavigationViewOptions object to package everything together
                                                Point origin = Point.fromLngLat(longitude, latitude);
                                                Point destination = Point.fromLngLat(longitude1, latitude1);
                                                NavigationLauncherOptions.Builder navigationLauncherOptions = NavigationLauncherOptions.builder();
                                                navigationLauncherOptions.origin(origin);
                                                navigationLauncherOptions.destination(destination);
                                                navigationLauncherOptions.shouldSimulateRoute(false);
                                                navigationLauncherOptions.enableOffRouteDetection(true);
                                                navigationLauncherOptions.snapToRoute(true);
                                /*NavigationLauncher.startNavigation(getActivity(), o, d,
                                        null, false);*/
                                                NavigationLauncher.startNavigation(getActivity(), navigationLauncherOptions.build());
                                            } catch (Exception e) {
                                                Toast.makeText(getActivity(), e.toString() + " ", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            turnonGps();
                                        }
                                    }

                                    @Override
                                    public void permissionDenied() {

                                    }

                                    @Override
                                    public void permissionForeverDenied() {

                                    }
                                });
                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(getActivity());
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {

        Log.e("Atiar - ", "Pickup Location - " + lat1 + " + "+lon1);
        Log.e("Atiar - ", "Dropout Location - " + lat2 + " + "+lon2);

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        return Double.parseDouble((new DecimalFormat("##.##").format(dist)));
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    public void NeaBy(String latitude, String longitude, String vehicle_type, String ride_type) {
        final String RIDE = "Ride";
        final String MOTORBIKE = "Motorbike";

        RequestParams params = new RequestParams();
        params.put("lat", latitude);
        params.put("long", longitude);
        params.put("vehicle_type", "");
        Server.setHeader(SessionManager.getInstance().getKEY(mContext));
        Server.get("api/user/nearby/format/json", params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);


                Log.e("Atiar - nearBy= ", "nearBy onSuccess:" + response.toString());
                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {

                        if (ride_type.equals(RIDE)) {
                            if (vehicle_type.equals(MOTORBIKE)) {
                                SessionManager.getInstance().setCost(getContext(),
                                        response.getJSONObject("fair")
                                                .getString("motorbike_ride_fare"));

                                SessionManager.getInstance().setBaseCost(getContext(),
                                        response.getJSONObject("fair")
                                                .getString("motorbike_ride_base_fare"));
                            } else {
                                SessionManager.getInstance().setCost(getContext(),
                                        response.getJSONObject("fair")
                                                .getString("tuktuk_ride_fare"));
                                SessionManager.getInstance().setBaseCost(getContext(),
                                        response.getJSONObject("fair")
                                                .getString("tuktuk_ride_base_fare"));
                            }
                        } else {
                            if (ride_type.equals(MOTORBIKE)) {
                                SessionManager.getInstance().setCost(getContext(),
                                        response.getJSONObject("fair")
                                                .getString("motorbike_delivery_fare"));
                                SessionManager.getInstance().setBaseCost(getContext(),
                                        response.getJSONObject("fair")
                                                .getString("motorbike_delivery_base_fare"));
                            } else {
                                SessionManager.getInstance().setCost(getContext(),
                                        response.getJSONObject("fair")
                                                .getString("tuktuk_delivery_fare"));
                                SessionManager.getInstance().setBaseCost(getContext(),
                                        response.getJSONObject("fair")
                                                .getString("tuktuk_delivery_base_fare"));
                            }
                        }
                        SessionManager.getInstance().setUnit(getContext(),
                                response.getJSONObject("fair").getString("unit"));
                        fare.setText(pojo.getAmount() + " "
                                + SessionManager.getInstance().getUnit(getContext()));


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);


                Toast.makeText(getActivity(), getString(R.string.try_again), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);


            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (getActivity() != null) {
                }
            }
        });
    }



    public void getAcceptedRequest(String id, String status, String key) {

        //created by atiar for automatically open when driver accept ride request.

        RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("status", status);
        params.put("utype", "1");
        Server.setHeader(key);
        Server.get("api/user/rides/format/json", params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Gson gson = new GsonBuilder().create();

                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {
                        List<PendingRequestPojo> list = gson.fromJson(response.getJSONArray("data").toString(), new TypeToken<List<PendingRequestPojo>>() {
                        }.getType());
                        if (list.size() == 0) {
                            // txt_error.setVisibility(View.VISIBLE);
                        } else {

                            final PendingRequestPojo pojo = list.get(0);
                            if (status.equals("PENDING")){
                                bundle = new Bundle();
                                bundle.putSerializable("data", pojo);
                                BindViewForAcceptedRequest();
                            }
                        }

                    } else {

                        Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {

                    Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void updateFinalFareToServer(String finalDistance, String finalAmount){
        RequestParams params = new RequestParams();
        params.put("ride_id", pojo.getRide_id());
        params.put("payment_mode", "OFFLINE");
        params.put("final_amount",finalAmount);
        params.put("final_distance",finalDistance);
        Server.setContetntType();
        Server.setHeader(SessionManager.getInstance().getKEY(mContext));
        Server.post("api/user/rides", params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                pojo.setPayment_mode("OFFLINE");
                pojo.setFinal_amount(finalAmount);
                try {
                    Log.e("Atiar - ", "Final amount updated server "+response.getString("final_amount"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (getActivity() != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private double getFinalAmount (){

        Double finalfare = (double) 0;
        if (SessionManager.getInstance().getBASE_COST(getContext()) != null &&
                !SessionManager.getInstance().getBASE_COST(getContext())
                        .equalsIgnoreCase("")
                && SessionManager.getInstance().getCOST(getContext()) != null &&
                !SessionManager.getInstance().getCOST(getContext())
                        .equalsIgnoreCase("")) {
            Double aDouble = distance(currentLatitude, currentLongitude
                    , getLocationFromAddress(pojo.getPickup_adress()).latitude
                    , getLocationFromAddress(pojo.getPickup_adress()).longitude);

            Log.e("Atiar - ", "From Discatnce calculator - Distance = " + aDouble);

            int reminderValue = (int) ((Integer.parseInt(
                    SessionManager.getInstance().getBASE_COST(getContext()))
                    + ((aDouble - 2) *
                    Integer.parseInt(SessionManager.getInstance().getCOST(getContext()))))
                    % 10);
            Log.e("Atiar - ", "From Discatnce calculator -  reminderValue = " + aDouble);

            finalfare = Integer.parseInt(
                    SessionManager.getInstance().getBASE_COST(getContext()))
                    + ((aDouble - 2) *
                    Integer.parseInt(SessionManager.getInstance().getCOST(getContext())));

            Log.e("Atiar - ", "From Discatnce calculator -  Final fare = " + aDouble);


            if (reminderValue >= 1 && reminderValue <= 4) {
                finalfare = finalfare - reminderValue;
            } else if (reminderValue >= 5 && reminderValue <= 9) {
                finalfare = finalfare + (10 - reminderValue);
            }

        }

        return finalfare;
    }
}
