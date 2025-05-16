package com.example.lakastextilwebshop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.Notification;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "reminder_channel";
        NotificationChannel channel = new NotificationChannel(channelId, "Emlékeztetők", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
        Notification notification = new Notification.Builder(context, channelId)
                .setContentTitle("Emlékeztető")
                .setContentText("Ne felejts el visszanézni a webshopba!")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
        notificationManager.notify(2, notification);
    }
}