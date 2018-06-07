package com.fmwrn;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.facebook.react.ReactActivity;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.fmwrn.indoors.LocationEvent;
import com.fmwrn.indoors.LocationService;

import java.util.ArrayList;

import static android.content.Context.LOCATION_SERVICE;

public class MainActivity extends ReactActivity {

    private static final int UPDATE_PREFERENCES = 11112; // Random update preferences, user your own
    private static final int REQUEST_CODE_PERMISSIONS = 34162; // Random request code, use your own
    private static final int REQUEST_CODE_LOCATION = 68774; // Random request code, use your own

    private static int MAX_READINGS = 10;
    private ArrayList<LocationEvent> items = new ArrayList<LocationEvent>();

    /**
     * Returns the name of the main component registered from JavaScript. This is
     * used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "fmwRN";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // We start by requesting permissions from the user
        requestPermissionsFromUser();
    }

    private void requestPermissionsFromUser() {
        /**
         * Since API level 23 we need to request permissions for so called dangerous
         * permissions
         *
         * from the user. You can see a full list of needed permissions in the Manifest
         * File.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheckForLocation = ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (permissionCheckForLocation != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                        REQUEST_CODE_PERMISSIONS);
            } else {
                // If permissions were already granted,
                // we can go on to check if Location Services are enabled.
                checkLocationIsEnabled();
            }
        } else {
            // Continue loading Indoors if we don't need user-settable-permissions.
            // In this case we are pre-Marshmallow.
            continueLoading();
        }
    }

    /**
     * The Android system calls us back after the user has granted permissions (or
     * denied them)
     * 
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // Since we have requested multiple permissions,
            // we need to check if any were denied
            for (int grant : grantResults) {
                if (grant == PackageManager.PERMISSION_DENIED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        // User has *NOT* allowed us to use ACCESS_COARSE_LOCATION
                        // permission on first try. This is the last chance we get
                        // to ask the user, so we explain why we want this permission
                        Toast.makeText(this, "Location is used for Bluetooth location", Toast.LENGTH_SHORT).show();
                        // Re-ask for permission
                        requestPermissionsFromUser();
                        return;
                    }

                    // The user has finally denied us permissions.
                    Toast.makeText(this, "Cannot continue without permissions.", Toast.LENGTH_SHORT).show();
                    this.finishAffinity();
                    return;
                }
            }

            checkLocationIsEnabled();
        }
    }

    private void checkLocationIsEnabled() {
        // Android Marshmallow needs to have active Location Services (GPS or Network
        // based)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean isNetworkLocationProviderEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean isGPSLocationProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isGPSLocationProviderEnabled && !isNetworkLocationProviderEnabled) {
                // Only if both providers are disabled we need to ask the user to do something
                Toast.makeText(this, "Location is off, enable it in system settings.", Toast.LENGTH_LONG).show();
                Intent locationInSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                this.startActivityForResult(locationInSettingsIntent, REQUEST_CODE_LOCATION);
            } else {
                continueLoading();
            }
        } else {
            continueLoading();
        }
    }

    // we can continue to load the Indoo.rs SDK as we did with previous android
    // versions
    private void continueLoading() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        startService(serviceIntent);
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationService.ACTION_SERVICE_STARTED);
        filter.addAction(LocationService.ACTION_SERVICE_STOPPED);
        filter.addAction(LocationService.ACTION_LOCATION_EVENT);
        registerReceiver(receiver, filter);

        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);

        super.onPause();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == LocationService.ACTION_SERVICE_STARTED) {
                Toast.makeText(getApplicationContext(), "Service started", Toast.LENGTH_SHORT).show();
            } else if (action == LocationService.ACTION_SERVICE_STOPPED) {
                Toast.makeText(getApplicationContext(), "Service stopped", Toast.LENGTH_SHORT).show();
            } else if (action == LocationService.ACTION_LOCATION_EVENT) {
                LocationEvent event = (LocationEvent) intent.getSerializableExtra(LocationEvent.LOCATION_EVENT);
                WritableMap params = Arguments.createMap();
                params.putString(event.getType().toString(), event.getAction().toString());
                params.putString("ZONENAME", event.getId());
                // params.putString("ACTIONTIME", new Long(event.getTime().));

                sendEventToJavaScript(getReactInstanceManager().getCurrentReactContext(), "IndoorsZoneEvent", params);

            } else {
                Toast.makeText(getApplicationContext(), "Unknown event received", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void sendEventToJavaScript(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

}
