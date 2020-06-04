package com.ytlcomms.smsverifycatcher;

import android.app.Service;
import android.content.IntentFilter;

public class SmsVerifyCatcherBackground {

    private Service activity;
    private OnSmsCatchListener<String> onSmsCatchListener;
    private SmsReceiver smsReceiver;
    private String phoneNumber;
    private String filter;

    public SmsVerifyCatcherBackground(Service activity, OnSmsCatchListener<String> onSmsCatchListener) {
        this.activity = activity;
        this.onSmsCatchListener = onSmsCatchListener;
        smsReceiver = new SmsReceiver();
        smsReceiver.setCallback(this.onSmsCatchListener);
    }


    public void onStart() {
        registerReceiver();

    }

    private void registerReceiver() {
        smsReceiver = new SmsReceiver();
        smsReceiver.setCallback(onSmsCatchListener);
        smsReceiver.setPhoneNumberFilter(phoneNumber);
        smsReceiver.setFilter(filter);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        activity.registerReceiver(smsReceiver, intentFilter);
    }

    public void setPhoneNumberFilter(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void onStop() {
        try {
            activity.unregisterReceiver(smsReceiver);
        } catch (IllegalArgumentException ignore) {
            //receiver not registered
        }
    }

    public void setFilter(String regexp) {
        this.filter = regexp;
    }


}
