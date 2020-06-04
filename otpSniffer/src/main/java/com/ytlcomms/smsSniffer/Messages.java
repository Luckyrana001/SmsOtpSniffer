package com.ytlcomms.smsSniffer;

public class Messages {


    public String amount;
    public String bank;
    public String date;
    public String orignalMessage;
    public String otp;
    public String time;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public Messages() {
    }

    public Messages(String amount, String bank, String date, String orignalMessage, String otp, String time) {
        this.amount = amount;
        this.bank = bank;
        this.date = date;
        this.orignalMessage = orignalMessage;
        this.otp = otp;
        this.time = time;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOrignalMessage() {
        return orignalMessage;
    }

    public void setOrignalMessage(String orignalMessage) {
        this.orignalMessage = orignalMessage;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}