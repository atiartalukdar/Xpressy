package com.xpressy.rider.acitivities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.xpressy.rider.R;
import com.xpressy.rider.custom.Utils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by Atiar Talukdar on 07/02/19.
 * www.atiar.info
 * +8801917445888
 *
 * updated on 12/02/2019 by Atiar
 * Kenya phone number on reformatPhoneNumber
 *
 */

public class VerificationActivity extends AppCompatActivity {
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    String countryCode = Utils.country_Code;

    private FirebaseAuth mAuth;
    Context mContext;

    EditText phoneed,countrycode;
    Button verificationButton;
    String mVerificationId;
    TextView timertext,_infoBar, _warningId;
    Timer timer;
    ImageView verifiedimg;
    Boolean mVerified = false;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    String temp = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        mContext = this;

        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        phoneed = (EditText) findViewById(R.id.numbered);
        countrycode = (EditText) findViewById(R.id.countryCode);
        verificationButton =  findViewById(R.id.sendverifybt);
        timertext = (TextView) findViewById(R.id.timertv);
        _infoBar = (TextView) findViewById(R.id.infoBar);
        _warningId = (TextView) findViewById(R.id.warningId);
        verifiedimg = (ImageView) findViewById(R.id.verifiedsign);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                Log.d("TAG", "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("TAG", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Snackbar snackbar = Snackbar
                            .make((CoordinatorLayout) findViewById(R.id.parentlayout), "Verification Failed !! Invalied verification Code", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }
                else if (e instanceof FirebaseTooManyRequestsException) {
                    Snackbar snackbar = Snackbar
                            .make((CoordinatorLayout) findViewById(R.id.parentlayout), "Verification Failed !! Too many request. Try after some time. ", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("TAG", "Atiar - onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

        verificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verificationButton.getTag().equals(getResources().getString(R.string.tag_send))) {
                    if (!phoneed.getText().toString().trim().isEmpty() && phoneed.getText().toString().trim().length() == 9) {
                        startPhoneNumberVerification(phoneed.getText().toString().trim());
                        mVerified = false;
                        starttimer();
                        temp = phoneed.getText().toString().trim();
                        countrycode.setVisibility(View.GONE);
                        _infoBar.setText("What's your confirmation code?");
                        phoneed.setText("");
                        phoneed.setHint("XXXXXX");
                        _warningId.setClickable(true);
                        _warningId.setText("Did you type the wrong phone number?");
                        phoneed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                        verificationButton.setText("Verify confirmation code".toUpperCase());
                        verificationButton.setTag(getResources().getString(R.string.tag_verify));
                    }
                    else {
                        phoneed.setError("Please enter valid mobile number");
                    }
                }

                if (verificationButton.getTag().equals(getResources().getString(R.string.tag_verify))) {
                    if (!phoneed.getText().toString().trim().isEmpty() && !mVerified) {
                        Snackbar snackbar = Snackbar
                                .make((CoordinatorLayout) findViewById(R.id.parentlayout), "Please wait...", Snackbar.LENGTH_LONG);

                        snackbar.show();
                        try {
                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, phoneed.getText().toString().trim());
                            signInWithPhoneAuthCredential(credential);
                        }catch (Exception e){
                            e.printStackTrace();
                            verifiedimg.setColorFilter(ContextCompat.getColor(VerificationActivity.this, R.color.red), PorterDuff.Mode.MULTIPLY);
                            verifiedimg.setImageResource(R.drawable.ic_close_black_24dp);

                        }
                    }
                    if (mVerified) {
                        Intent intent = new Intent(VerificationActivity.this, RegisterActivity.class);
                        intent.putExtra("phone",phoneed.getText().toString());
                        intent.putExtra("regType","mobile");
                        startActivity(intent);
                        finish();
                    }

                }


            }
        });

        _warningId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _infoBar.setText("What's your phone number?");
                _warningId.setText("By tapping \"SEND CONFIRMATION CODE\" above, we will send you an SMS to confirm your phone number.");
                _warningId.setClickable(false);
                phoneed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});
                verificationButton.setText("Send Confirmation Code".toUpperCase());
                verificationButton.setTag(getResources().getString(R.string.tag_send));
                timertext.setVisibility(View.GONE);
                countrycode.setVisibility(View.VISIBLE);
                phoneed.setText(temp);
                phoneed.setHint("XXX XXXXXX");
                phoneed.setSelection(temp.length());

                InputMethodManager imm = (InputMethodManager)   getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                verifiedimg.setColorFilter(ContextCompat.getColor(VerificationActivity.this, R.color.overlay), PorterDuff.Mode.MULTIPLY);
                verifiedimg.setImageResource(R.drawable.ic_perm_phone_msg_black_24dp);

            }
        });

        timertext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!temp.isEmpty() && temp.length() == 9) {
                    resendVerificationCode(temp, mResendToken);
                    mVerified = false;
                    starttimer();
                    verificationButton.setTag(getResources().getString(R.string.tag_verify));
                    Snackbar snackbar = Snackbar
                            .make((CoordinatorLayout) findViewById(R.id.parentlayout), "Resending verification code...", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }
            }
        });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            mVerified = true;
                            timer.cancel();
                            verifiedimg.setColorFilter(ContextCompat.getColor(VerificationActivity.this, R.color.yellow), PorterDuff.Mode.MULTIPLY);
                            verifiedimg.setImageResource(R.drawable.ic_check_circle_black_24dp);
                            timertext.setVisibility(View.INVISIBLE);
                            phoneed.setEnabled(false);
                            //codeed.setVisibility(View.GONE);  //verification code

                            Snackbar snackbar = Snackbar
                                    .make((CoordinatorLayout) findViewById(R.id.parentlayout), "Successfully Verified", Snackbar.LENGTH_LONG);

                            snackbar.show();

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            // ...
                            // Start the Main activity

                            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                            intent.putExtra("phone",phoneed.getText().toString());
                            intent.putExtra("regType","mobile");
                            startActivity(intent);
                            finish();

                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Snackbar snackbar = Snackbar
                                        .make((CoordinatorLayout) findViewById(R.id.parentlayout), "Invalid OTP ! Please enter correct OTP", Snackbar.LENGTH_LONG);

                                snackbar.show();
                                verifiedimg.setColorFilter(ContextCompat.getColor(VerificationActivity.this, R.color.red), PorterDuff.Mode.MULTIPLY);
                                verifiedimg.setImageResource(R.drawable.ic_close_black_24dp);

                            }
                        }
                    }
                });
    }

    private void startPhoneNumberVerification(String phoneNumber) {

        phoneNumber = reformatPhoneNumberWithCountryCode(phoneNumber);

        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

    }

    public void starttimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {

            int second = 60;

            @Override
            public void run() {
                if (second <= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timertext.setText("RESEND CODE");
                            timer.cancel();
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timertext.setText("00:" + second--);
                        }
                    });
                }

            }
        }, 0, 1000);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {

        phoneNumber = reformatPhoneNumberWithCountryCode(phoneNumber);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private String reformatPhoneNumberWithCountryCode(String phoneNumber){
        String phone = phoneNumber;

        //phone = countrycode.getText().toString()+phoneNumber;
        phone = countryCode+phoneNumber;

        if (phoneed.getText().toString().length()==9){
            Utils.setPreference(mContext,Utils.key_phoneNumber,phone);
        }

        return phone;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(VerificationActivity.this, LoginActivity.class));
        finish();
    }

}
