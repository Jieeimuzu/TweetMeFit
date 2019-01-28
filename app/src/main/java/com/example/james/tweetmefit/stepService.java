package com.example.james.tweetmefit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.ContentValues.TAG;


public class stepService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepSensor;
    SharedPreferences Prefs;
    SharedPreferences.Editor edit;
    private int name;

    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat mdformat = new SimpleDateFormat("dd/MM/yy ");
    String strDate = mdformat.format(calendar.getTime());
    private encryptions en = new encryptions();

    private NotificationManager notificationManager;

    private static final String NOTIFICATION_CHANNEL_ID = "my_notification_channel";

    @RequiresApi(Build.VERSION_CODES.O)


    @Override
    public void onCreate() {
        super.onCreate();

        Prefs = getSharedPreferences(en.encrypt("Steps"), MODE_PRIVATE);
        name = Prefs.getInt(strDate, 0);
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST);

        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("Step Start", "Start");

        this.name = getSharedPreferences(en.encrypt("Steps"), MODE_PRIVATE).getInt(strDate, 0);

        startTimer();
        startTimer();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        edit = Prefs.edit();
        name++;
        edit.putInt(strDate, name);
        edit.apply();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        Log.e(TAG, "Service Closed");
        stoptimertask();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartStepper");
        broadcastIntent.setClass(this, stepReciver.class);
        this.sendBroadcast(broadcastIntent);
    }


    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
            }
        };
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}
