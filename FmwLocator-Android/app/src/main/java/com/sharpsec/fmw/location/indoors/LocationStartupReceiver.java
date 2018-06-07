package com.sharpsec.fmw.location.indoors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sharpsec.fmw.auth.cognito.LoginActivity;

public class LocationStartupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent activityIntent = new Intent(context, LoginActivity.class);
        context.startActivity(activityIntent);
    }
}
