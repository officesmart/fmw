package com.fmwrn.indoors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fmwrn.MainActivity;


public class LocationStartupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent activityIntent = new Intent(context, MainActivity.class);
        context.startActivity(activityIntent);
    }
}
