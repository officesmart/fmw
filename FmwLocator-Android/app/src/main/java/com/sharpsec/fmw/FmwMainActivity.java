package com.sharpsec.fmw;

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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.sharpsec.fmw.location.indoors.LocationEvent;
import com.sharpsec.fmw.location.indoors.LocationService;

import java.util.ArrayList;

public class FmwMainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int UPDATE_PREFERENCES = 11111; // Random update preferences, user your own
    private static final int VIEW_DEBUG = 22222; // Random update preferences, user your own
    private static final int REQUEST_CODE_PERMISSIONS = 34168; // Random request code, use your own
    private static final int REQUEST_CODE_LOCATION = 58774; // Random request code, use your own

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button findCoworker = (Button) this.findViewById(R.id.find_coworker);
        findCoworker.setOnClickListener(this);

        // We start by requesting permissions from the user
        requestPermissionsFromUser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menuLogout:
                IdentityManager.getDefaultIdentityManager().signOut();
                return true;
            case R.id.preferences:
                // Go to FmwPreferencesActivity to set up preferences, then come back
                Intent prefIntent = new Intent(this, FmwPreferenceActivity.class);
                startActivityForResult(prefIntent, UPDATE_PREFERENCES);
                return true;
            case R.id.debug:
                // Go to FmwDebugActivity to view location event traffic
                Intent debugIntent = new Intent(this, FmwDebugActivity.class);
                startActivityForResult(debugIntent, VIEW_DEBUG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case REQUEST_CODE_LOCATION:
                // Check if the user has really enabled Location services.
                checkLocationIsEnabled();
                break;
            case UPDATE_PREFERENCES:
                Toast.makeText(this, "Preferences changed." +
                        " You need to restart app to take effect...", Toast.LENGTH_LONG).show();
                break;
            case VIEW_DEBUG:
                // currently no process needed when returned from debug activity
                break;
            default:
                break;
        }
    }

    private void requestPermissionsFromUser() {
        /**
         * Since API level 23 we need to request permissions for so called dangerous permissions
         *
         * from the user.  You can see a full list of needed permissions in the Manifest File.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheckForLocation = ContextCompat.checkSelfPermission(
                    FmwMainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if (permissionCheckForLocation != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[] {
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        REQUEST_CODE_PERMISSIONS);
            } else {
                //If permissions were already granted,
                // we can go on to check if Location Services are enabled.
                checkLocationIsEnabled();
            }
        } else {
            //Continue loading Indoors if we don't need user-settable-permissions.
            // In this case we are pre-Marshmallow.
            continueLoading();
        }
    }

    /**
     * The Android system calls us back
     * after the user has granted permissions (or denied them)
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // Since we have requested multiple permissions,
            // we need to check if any were denied
            for (int grant : grantResults) {
                if (grant == PackageManager.PERMISSION_DENIED) {
                    if (shouldShowRequestPermissionRationale(
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        // User has *NOT* allowed us to use ACCESS_COARSE_LOCATION
                        // permission on first try. This is the last chance we get
                        // to ask the user, so we explain why we want this permission
                        Toast.makeText(this,
                                "Location is used for Bluetooth location",
                                Toast.LENGTH_SHORT).show();
                        // Re-ask for permission
                        requestPermissionsFromUser();
                        return;
                    }

                    // The user has finally denied us permissions.
                    Toast.makeText(this,
                            "Cannot continue without permissions.",
                            Toast.LENGTH_SHORT).show();
                    this.finishAffinity();
                    return;
                }
            }

            checkLocationIsEnabled();
        }
    }

    private void checkLocationIsEnabled() {
        // Android Marshmallow needs to have active Location Services (GPS or Network based)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean isNetworkLocationProviderEnabled =
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean isGPSLocationProviderEnabled =
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isGPSLocationProviderEnabled && !isNetworkLocationProviderEnabled) {
                // Only if both providers are disabled we need to ask the user to do something
                Toast.makeText(this, "Location is off, enable it in system settings.",
                        Toast.LENGTH_LONG).show();
                Intent locationInSettingsIntent =
                        new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                this.startActivityForResult(locationInSettingsIntent, REQUEST_CODE_LOCATION);
            } else {
                continueLoading();
            }
        } else {
            continueLoading();
        }
    }

    // we can continue to load the Indoo.rs SDK as we did with previous android versions
    private void continueLoading() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        startService(serviceIntent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.find_coworker:
                Intent findIntent =  new Intent(this, FmwFindCoworkerActivity.class);
                startActivity(findIntent);
                break;
                default:
                    break;
        }
    }
}
