package com.capstone.mychatapp.utils;

import android.app.Application;
import android.content.Context;

import com.capstone.mychatapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GetMessageTime extends Application {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getMessageTime(long time, Context ctx) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return ctx.getString(R.string.time_just_now);
        } else if (diff < 90 * MINUTE_MILLIS) {
            SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
            String messageTime = sfd.format(new Date(time)).toString();
            messageTime = messageTime.substring(11, messageTime.length());
            return messageTime;
        } else if (diff < 24 * HOUR_MILLIS) {
            SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
            String messageTime = sfd.format(new Date(time)).toString();
            messageTime = messageTime.substring(11, messageTime.length());
            return messageTime;
        } else if (diff < 48 * HOUR_MILLIS) {
            return ctx.getString(R.string.time_yesterday);
        } else {
            SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String messageTime = sfd.format(new Date(time)).toString();
            messageTime = messageTime.substring(0, 10);
            return messageTime;
        }
    }

}