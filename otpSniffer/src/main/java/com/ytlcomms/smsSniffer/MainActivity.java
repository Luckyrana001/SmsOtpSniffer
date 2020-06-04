package com.ytlcomms.smsSniffer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ytlcomms.smsverifycatcher.OnSmsCatchListener;
import com.ytlcomms.smsverifycatcher.SmsVerifyCatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ytlcomms.smsverifycatcher.SmsVerifyCatcher.permissionGranted;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private SmsVerifyCatcher smsVerifyCatcher;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String userId;

    private TextView bankNameTv,timeTv,dateTv,amountTv,fullMessageTv;
    private Button startServiceBtn,stopServiceBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // Displaying toolbar icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        mFirebaseInstance = FirebaseDatabase.getInstance();
        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference("messages");
        // store app title to 'app_title' node
        mFirebaseInstance.getReference("App_Title").setValue("otpSniffer");

        // app_title change listener
        mFirebaseInstance.getReference("App_Title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "App title updated");

                String appTitle = dataSnapshot.getValue(String.class);
                // update toolbar title
                getSupportActionBar().setTitle(appTitle);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read app title value.", error.toException());
            }
        });


        //init views
        startServiceBtn = findViewById(R.id.startServiceBtn);
        startServiceBtn.setOnClickListener(this);
        stopServiceBtn = findViewById(R.id.stopServiceBtn);
        stopServiceBtn.setOnClickListener(this);

        checkServiceIsRuningOrNot();

        final EditText etCode =  findViewById(R.id.et_code);
        bankNameTv = findViewById(R.id.bankNameTv);
        timeTv = findViewById(R.id.timeTv);
        dateTv = findViewById(R.id.dateTv);
        amountTv = findViewById(R.id.amountTv);
        fullMessageTv = findViewById(R.id.fullMessageTv);

        //init SmsVerifyCatcher
        smsVerifyCatcher = new SmsVerifyCatcher(this, new OnSmsCatchListener<String>() {
            @Override
            public void onSmsCatch(String message) {
                String catchOTP = parseCode(message);
                try {

                    //String message = "HSBC: OTP 860054 for card ending 5070 for online txn MYR409.90 at YTL COMMS-3D on 06/02/20 03:41.OTP will expire in 3mins";
                    //String message = "RM0 Your MSOS Code for card ending 0274 is 862842 for online txn at YTL COMM-Y-MAX- of MYR 1.00.Code will expire in 4 mins.02/06/2020@12:18";

                    if (message.contains("HSBC")) {
                        String otp = parseCode(message);
                        String date = parseHsbcDate(message);
                        String time = parseHsbcTime(message);
                        String amount = parseHsbcAmount(message);
                        String bank = "HSBC";
                        String orignalMessage = message;


                        etCode.setText(otp);
                        bankNameTv.setText("Bank Name - "+bank);
                        timeTv.setText("Time - "+time);
                        dateTv.setText("Date - "+date);
                        amountTv.setText("Amount - RM "+amount);
                        fullMessageTv.setText(orignalMessage);


                       // saveMessage(amount, bank, date, orignalMessage, otp, time);

                    } else {

                        String otp = parseCode(message);
                        String date = parseMaybankDate(message);
                        String time = parseMaybankTime(message);
                        String amount = parseMaybankAmount(message);
                        String bank = "MayBank";
                        String orignalMessage = message;

                        etCode.setText(otp);
                        bankNameTv.setText("Bank Name - "+bank);
                        timeTv.setText("Time - "+time);
                        dateTv.setText("Date - "+date);
                        amountTv.setText("Amount - RM "+amount);
                        fullMessageTv.setText(orignalMessage);

                       // saveMessage(amount, bank, date, orignalMessage, otp, time);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //set phone number filter if needed
       // smsVerifyCatcher.setPhoneNumberFilter("777");
       // smsVerifyCatcher.setFilter("regexp");


    }

    private String parseMaybankDate(String message) {
        String[] dateAndTime = message.split("mins.");
        String dateTime = dateAndTime[1];
        String[] dateTimeArray = dateTime.split("@");
        String date = dateTimeArray[0];
        return date;
    }
    private String parseHsbcTime(String message) {
        String[] dateAndTime = message.split("on ");
        String dateTime = dateAndTime[1];
        String[] dateTimeArray = dateTime.split(" ");
        String timeString = dateTimeArray[1];
        timeString = timeString.replace(".OTP", "");
        return timeString;
    }

    private String parseHsbcDate(String message) {
        String[] dateAndTime = message.split("on ");
        String dateTime = dateAndTime[1];
        String[] dateTimeArray = dateTime.split(" ");
        String date = dateTimeArray[0];
        return date;
    }
    private String parseMaybankTime(String message) {
        String[] dateAndTime = message.split("mins.");
        String dateTime = dateAndTime[1];
        String[] dateTimeArray = dateTime.split("@");
        String time = dateTimeArray[1];
        return time;
    }
    private String parseMaybankAmount(String message) {
        String[] dateAndTime = message.split("MYR ");
        String dateTime = dateAndTime[1];
        String[] dateTimeArray = dateTime.split(".Code");
        String amount = dateTimeArray[0];
        return amount;
    }

    private String parseHsbcAmount(String message) {
        String[] dateAndTime = message.split("MYR");
        String dateTime = dateAndTime[1];
        String[] dateTimeArray = dateTime.split(" ");
        String amount = dateTimeArray[0];
        return amount;
    }

    /**
     * Creating new message node under 'message'
     */
    private void saveMessage(String amount, String bank,String date, String orignalMessage,String otp, String time) {
        userId = mFirebaseDatabase.push().getKey();
        Messages user = new Messages(amount,bank,date,orignalMessage,otp,time);
        mFirebaseDatabase.child(userId).setValue(user);
        addUserChangeListener();
    }

    /**
     * message data change listener
     */
    private void addUserChangeListener() {
        // User data change listener
        mFirebaseDatabase.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Messages user = dataSnapshot.getValue(Messages.class);

                // Check for null
                if (user == null) {
                    Log.e(TAG, "User data is null!");
                    return;
                }

                Log.e(TAG, "User data is changed!" + user.amount + ", " + user.orignalMessage);

                // Display newly updated name and email
                //txtDetails.setText(user.amount + ", " + user.orignalMessage);
                //toggleButton();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read user", error.toException());
            }
        });
    }
    /**
     * Parse verification code
     *
     * @param message sms message
     * @return only four numbers from massage string
     */
    private String parseCode(String message) {
        Pattern p = Pattern.compile("\\b\\d{6}\\b");
        Matcher m = p.matcher(message);
        String code = "";
        while (m.find()) {
            code = m.group(0);
        }
        return code;
    }

    @Override
    protected void onStart() {
        super.onStart();
        smsVerifyCatcher.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        smsVerifyCatcher.onStop();
    }

    /**
     * need for Android 6 real time permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        smsVerifyCatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.startServiceBtn:
                if(permissionGranted)
                startService();
                else
                    Toast.makeText(this, "Please accept SMS Read Permission to Continue!", Toast.LENGTH_SHORT).show();
                break;

            case R.id.stopServiceBtn:
                stopService();
                break;
        }
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "is working in the Background");
        ContextCompat.startForegroundService(this, serviceIntent);
        checkServiceIsRuningOrNot();

    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
        checkServiceIsRuningOrNot();
    }

    public void checkServiceIsRuningOrNot(){
        if(isMyServiceRunning(ForegroundService.class)){
            startServiceBtn.setVisibility(View.GONE);
            stopServiceBtn.setVisibility(View.VISIBLE);
        }else{
            startServiceBtn.setVisibility(View.VISIBLE);
            stopServiceBtn.setVisibility(View.GONE);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
