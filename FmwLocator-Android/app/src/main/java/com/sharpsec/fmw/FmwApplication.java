package com.sharpsec.fmw;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class FmwApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setDefaultPreferences();
    }

    private void setDefaultPreferences() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if(sp.getString(Prefs.SERVER_URL, null) == null) {
            sp.edit().putString(Prefs.SERVER_URL, Prefs.SERVER_URL_VALUE).commit();
        }
        if(sp.getString(Prefs.INDOORS_API_KEY, null) == null) {
            sp.edit().putString(Prefs.INDOORS_API_KEY, Prefs.INDOORS_API_KEY_VALUE).commit();
        }
    }
}
