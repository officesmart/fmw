package com.sharpsec.fmw;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.sharpsec.fmw.location.indoors.LocationEvent;
import com.sharpsec.fmw.location.indoors.LocationService;

import java.util.ArrayList;

public class FmwDebugActivity extends AppCompatActivity {

    private static int MAX_READINGS = 1000;
    private EventListAdapter listAdapter;
    private ListView listView;
    private ArrayList<LocationEvent> items = new ArrayList<LocationEvent>();

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == LocationService.ACTION_SERVICE_STARTED) {
                Toast.makeText(getApplicationContext(), "Service started",
                        Toast.LENGTH_SHORT).show();
            } else if(action == LocationService.ACTION_SERVICE_STOPPED) {
                Toast.makeText(getApplicationContext(), "Service stopped",
                        Toast.LENGTH_SHORT).show();
            } else if(action== LocationService.ACTION_LOCATION_EVENT) {
                LocationEvent event =
                        (LocationEvent) intent.getSerializableExtra(LocationEvent.LOCATION_EVENT);

                listAdapter.insert(event, 0);
                int count = listAdapter.getCount();
                if(count > MAX_READINGS) {
                    LocationEvent lastEvent = listAdapter.getItem(count - 1);
                    listAdapter.remove(lastEvent);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Unknown event received",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        listAdapter = new EventListAdapter(this, items);
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_debug, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.clear:
                listAdapter.clear();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

}
