package com.example.nicolefelch.timer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

public class MainActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private Button startStop;

    private static final String NOTIFICATION_CHANNEL = "com.example.nicolefelch.timerapp";
    private static final String KEY_CHRONOMETER_BASE = "key_base";
    private static final String PREF_FILE = "prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chronometer = findViewById(R.id.text);
        startStop = findViewById(R.id.start_stop);
        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startStop.getText().equals(getString(R.string.start))) {
                    startTimer(SystemClock.elapsedRealtime());
                } else {
                    stopTimer();
                }
            }
        });
    }

    private void stopTimer() {
        getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit().putLong(KEY_CHRONOMETER_BASE, -1).apply();
        chronometer.stop();
        startStop.setText(R.string.start);
    }

    private void startTimer(long base) {
        chronometer.setBase(base);
        getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit().putLong(KEY_CHRONOMETER_BASE, chronometer.getBase()).apply();
        chronometer.start();
        startStop.setText(R.string.stop);
    }

    @Override
    protected void onStop() {
        super.onStop();
        long base = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).getLong(KEY_CHRONOMETER_BASE, -1);
        if (base > 0) startNotification(base);
    }

    @Override
    protected void onStart() {
        super.onStart();
        cancelNotification();
        long base = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).getLong(KEY_CHRONOMETER_BASE, -1);
        if (base > 0) startTimer(base);
    }

    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    private void startNotification(long chronometerBase) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setContentTitle("Timer")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setUsesChronometer(true)
                .setOngoing(true)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));

        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.O) notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL, getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH));
        notification.when = System.currentTimeMillis() - (SystemClock.elapsedRealtime() - chronometerBase);
        notificationManager.notify(0, notification);
    }
}
