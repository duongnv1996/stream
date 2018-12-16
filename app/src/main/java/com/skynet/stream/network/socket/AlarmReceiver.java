package com.skynet.stream.network.socket;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.Html;

import com.blankj.utilcode.util.LogUtils;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Huy on 4/24/2018.
 */

public class AlarmReceiver extends BroadcastReceiver {
    Notification notification;
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.e("sang đây");
        if(intent != null){
            DateFormat dfTime = new SimpleDateFormat("HH:mm");
            String time = dfTime.format(Calendar.getInstance().getTime());
            LogUtils.e(time);

        }
    }

    public void showNotificationInStack(Context context, int id) {
        if (notification != null) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(id, notification);
            notification = null;
        }
    }
}
