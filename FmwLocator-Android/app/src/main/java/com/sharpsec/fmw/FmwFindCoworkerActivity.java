package com.sharpsec.fmw;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.sharpsec.fmw.amazonaws.mobile.api.idwfrj8o1tq5.MobileApiCallback;
import com.sharpsec.fmw.amazonaws.mobile.api.idwfrj8o1tq5.MobileApiClient;
import com.sharpsec.fmw.amazonaws.mobile.api.idwfrj8o1tq5.MobileApiUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FmwFindCoworkerActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    static final String LOG_TAG = "FmwFindCoworkerActivity";

    private TextView userName;
    private TextView zoneId;
    private TextView timeUpdated;
    private LinearLayout userInfo;
    private ListView userList;

    private ArrayList<String> userNames = new ArrayList<String>();
    private ArrayAdapter<String> listAdapter;
    private MobileApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_coworker);

        userName = (TextView) findViewById(R.id.user_name);
        zoneId = (TextView) findViewById(R.id.zone_id);
        timeUpdated = (TextView) findViewById(R.id.time_updated);
        userInfo = (LinearLayout) findViewById(R.id.user_info);

        userList = (ListView) findViewById(R.id.user_list);

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, userNames);
        userList.setAdapter(listAdapter);
        userList.setOnItemClickListener(this);

        //create a client object
        apiClient = MobileApiUtils.createApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();

        final String method = "GET";
        final String path = "/fmwapi/locations";

        UserListCallback listCallback = new UserListCallback();
        MobileApiUtils.makeApiCall(apiClient, method, path, null, listCallback);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(LOG_TAG, userNames.get(position));

        final String method = "GET";
        final String path = "/fmwapi/locations/" + userNames.get(position);

        UserCallback userCallback = new UserCallback();
        MobileApiUtils.makeApiCall(apiClient, method, path, null, userCallback);
    }

    private class UserListCallback implements MobileApiCallback {

        @Override
        public void onResponse(String result) {
            Log.d(LOG_TAG, result);

            try {
                JSONArray userList = new JSONArray(result);

                userNames.clear();
                for(int i = 0; i < userList.length(); i++) {
                    JSONObject user = userList.getJSONObject(i);
                    String name = user.getString("userName");
                    userNames.add(name);
                }
                listAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError() {
            Log.d(LOG_TAG, "Error occured retrieving user list");
        }
    }

    private class UserCallback implements MobileApiCallback {

        @Override
        public void onResponse(String result) {
            Log.d(LOG_TAG, result);

            try {
                JSONArray userList = new JSONArray(result);
                JSONObject userObject = userList.getJSONObject(0);

                String user = userObject.getString("userName");
                String zone = userObject.getString("lastKnownLoc");
                long timeInMillis = userObject.getLong("lastKnownLocTimeInMillis");

                userInfo.setVisibility(View.VISIBLE);
                userName.setText(user);
                zoneId.setText(zone);

                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a zzz");
                String time = formatter.format(new Date(timeInMillis));
                timeUpdated.setText(time);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError() {
            Log.d(LOG_TAG, "Error occured retrieving user information");
            userName.setText("????????");
            zoneId.setText("????????");
        }
    }
}
