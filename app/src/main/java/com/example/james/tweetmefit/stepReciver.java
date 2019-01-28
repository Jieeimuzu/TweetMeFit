package com.example.james.tweetmefit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;


public class stepReciver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(stepReciver.class.getSimpleName(), "Service stopper, receiver called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, stepService.class));
        } else {
            context.startService(new Intent(context, stepService.class));
        }
    }
}
