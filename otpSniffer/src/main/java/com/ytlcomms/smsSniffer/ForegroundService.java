package com.ytlcomms.smsSniffer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ytlcomms.smsverifycatcher.OnSmsCatchListener;
import com.ytlcomms.smsverifycatcher.SmsVerifyCatcherBackground;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final String TAG = MainActivity.class.getSimpleName();
    private SmsVerifyCatcherBackground smsVerifyCatcher;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private String userId;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //init SmsVerifyCatcher
        smsVerifyCatcher = new SmsVerifyCatcherBackground(ForegroundService.this, new OnSmsCatchListener<String>() {
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


                        /*etCode.setText(otp);
                        bankNameTv.setText("Bank Name - "+bank);
                        timeTv.setText("Time - "+time);
                        dateTv.setText("Date - "+date);
                        amountTv.setText("RM - "+amount);
                        fullMessageTv.setText(orignalMessage);*/


                        saveMessage(amount, bank, date, orignalMessage, otp, time);

                    } else {

                        String otp = parseCode(message);
                        String date = parseMaybankDate(message);
                        String time = parseMaybankTime(message);
                        String amount = parseMaybankAmount(message);
                        String bank = "MayBank";
                        String orignalMessage = message;

                        /*etCode.setText(otp);
                        bankNameTv.setText("Bank Name - "+bank);
                        timeTv.setText("Time - "+time);
                        dateTv.setText("Date - "+date);
                        amountTv.setText("RM - "+amount);
                        fullMessageTv.setText(orignalMessage);*/

                        saveMessage(amount, bank, date, orignalMessage, otp, time);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        smsVerifyCatcher.onStart();


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

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read app title value.", error.toException());
            }
        });

        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("YTLCOMMS OTP Sniffer Service")
                .setContentText(input)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);


        //do heavy work on a background thread
        //stopSelf();
        return START_NOT_STICKY;
    }

   /* private void registerReceiver() {
        SmsReceiver  smsReceiver = new SmsReceiver();
        smsReceiver.setCallback(onSmsCatchListener);
        smsReceiver.setPhoneNumberFilter(phoneNumber);
        smsReceiver.setFilter(filter);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        this.registerReceiver(smsReceiver, intentFilter);
    }*/

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
    private void saveMessage(String amount, String bank, String dateMsg, String orignalMessage, String otp, String time) {

        Date date = Calendar.getInstance().getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss.SSS a");
        String strDate = dateFormat.format(date);
        strDate = strDate.replace("-", "");
        strDate = strDate.replace(" ", "");
        strDate = strDate.replace(":", "");
        strDate = strDate.replace(".", "");

        userId = mFirebaseDatabase.push().getKey();
        Messages user = new Messages(amount, bank, dateMsg, orignalMessage, otp, time);
        //mFirebaseDatabase.child(userId).child("lucky1").setValue(user);
        mFirebaseDatabase.child(strDate).setValue(user);

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
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}