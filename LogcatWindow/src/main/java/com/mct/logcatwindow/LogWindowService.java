package com.mct.logcatwindow;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.mct.logcatwindow.utils.Utils;

public class LogWindowService extends Service {

    public static final String EXTRA_LOG_CONFIG = "log_config";
    public static final String EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area";

    private static final int NOTIFY_ID = 369;
    private static final String CHANNEL_ID = "LogcatWindowChannel";
    private static final String ACTION_STOP = "act_stop";

    private boolean isInit;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null
                || ACTION_STOP.equals(intent.getAction())
                || !Utils.isOverlayGranted(this)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (!isInit) {
            isInit = true;
            LogWindow.init(this);
            LogWindow.instance()
                    .setLogConfig((LogConfig) intent.getSerializableExtra(EXTRA_LOG_CONFIG))
                    .setSafeInsetRect(intent.getParcelableExtra(EXTRA_CUTOUT_SAFE_AREA))
                    .attachBubbleControl(this, this::stopSelf);
            startForeground(NOTIFY_ID, createNotification());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isInit = false;
        stopForeground(true);
        LogWindow.dispose();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "LogcatWindow";
            String channelDesc = "LogcatWindow channel!";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(channelDesc);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }

    @NonNull
    private Notification createNotification() {
        createNotificationChannel();

        Intent intent = new Intent(this, LogWindowService.class).setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, getFlags());

        NotificationCompat.Builder notificationBuilder = new NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.lw_small_notify_icon)
                .setContentTitle("LogcatWindow")
                .setContentText("LogcatWindow is running!")
                .addAction(0, "Stop", pendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle());
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFY_ID, notificationBuilder.build());
        return notificationBuilder.build();
    }


    int getFlags() {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return flags;
    }
}
