package com.fmwrn.indoors;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Regions;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringUtils;
import com.customlbs.library.Indoors;
import com.customlbs.library.IndoorsException;
import com.customlbs.library.IndoorsFactory;
import com.customlbs.library.IndoorsLocationListener;
import com.customlbs.library.LocalizationParameters;
import com.customlbs.library.callbacks.IndoorsServiceCallback;
import com.customlbs.library.callbacks.LoadingBuildingStatus;
import com.customlbs.library.model.Building;
import com.customlbs.library.model.Zone;
import com.customlbs.shared.Coordinate;
import com.fmwrn.Prefs;
import com.fmwrn.idlc5hu0ls70.FmwlocationClient;
import com.fmwrn.indoors.model.BodyParams;
import com.fmwrn.indoors.model.EventPayload;
import com.fmwrn.indoors.model.LocationApiBody;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationService extends Service
        implements IndoorsServiceCallback, IndoorsLocationListener {
    private static final String LOG_TAG = LocationService.class.getSimpleName();
        public static final String ACTION_SERVICE_STARTED = "com.sharpsec.fmw.ACTION_SERVICE_STARTED";
    public static final String ACTION_SERVICE_STOPPED = "com.sharpsec.fmw.ACTION_SERVICE_STOPPED";
    public static final String ACTION_LOCATION_EVENT = "com.sharpsec.fmw.ACTION_LOCATION_EVENT";

    private List<Zone> currentZones = new ArrayList<>();

    private Indoors indoors = null;
    private String serverUrl = "";  // need to point to our own Cloud's Location Service endpoint
    private String indoorsApiKey = "";  // Indoors-provided API KEY for Android SDK
    
    private CognitoUserPool cognitoUserPool;
    private FmwlocationClient apiLocationClient;
    Gson objectToJson = null;

    
    @Override
    public void onCreate() {
        super.onCreate();
        updateValuesFromPreferences();
        initializeIndoorsLibrary();
        objectToJson = new GsonBuilder().disableHtmlEscaping().create();
        //create a client object
         apiLocationClient = new ApiClientFactory()
                 .credentialsProvider(AWSMobileClient.getInstance().getCredentialsProvider())
                 .build(FmwlocationClient.class);
    }

    private void updateValuesFromPreferences() {
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            serverUrl = sp.getString(Prefs.SERVER_URL, Prefs.SERVER_URL_VALUE);
            indoorsApiKey = sp.getString(Prefs.INDOORS_API_KEY, Prefs.INDOORS_API_KEY_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Server URL or 'Indoors' API KEY not set." +
                            "Set them first from 'Preferences' menu...",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void connected() {
        indoors = IndoorsFactory.getInstance();
        indoors.registerLocationListener(this);

        indoors.setLocatedCloudBuilding(1160951400, new LocalizationParameters(), true);
        Log.i(LOG_TAG, "connected");
    }

    @Override
    public void onError(IndoorsException indoorsException) {
        Log.i(LOG_TAG, indoorsException.getMessage());
    }

    @Override
    public void loadingBuilding(LoadingBuildingStatus loadingBuildingStatus) {
        // indoo.rs is still downloading or parsing the requested building
    }

    @Override
    public void buildingLoaded(Building building) {
        // indoo.rs SDK successfully loaded the building you requested and
        // calculates a position now
    }

    @Override
    public void leftBuilding(Building building) {
        // Deprecated
    }

    @Override
    public void buildingReleased(Building building) {
        // Another building was loaded, you can release any resources related to linked building
    }

    @Override
    public void positionUpdated(Coordinate userPosition, int accuracy) {
        /*
          Is called each time the Indoors Library calculated a new position for the user
          If Lat/Lon/Rotation of your building are set correctly you can calculate a
          GeoCoordinate for your users current location in the building.
         */
    }

    @Override
    public void orientationUpdated(float orientation) {
        // user changed the direction he's heading to
    }

    @Override
    public void changedFloor(int floorLevel, String name) {
        // user changed the floor
    }

    @Override
    public void enteredZones(List<Zone> zones) {
        long time = new Date().getTime();

        for (Zone currentZone : currentZones) {
            if (!LocationUtil.containsZone(zones, currentZone)) {
                // left zone event
                LocationUtil.removeZone(currentZones, currentZone);

                LocationEvent event =
                        new LocationEvent(currentZone.getName(), time,
                                LocationEvent.Type.ZONE, LocationEvent.Action.OUT);
                // notifyEvent(event);
                //call location api to upload location event info
                // callCloudLogicFmwLocationApi(event);
                break;
            }
        }
        for (Zone zone : zones) {
            if (!LocationUtil.containsZone(currentZones, zone)) {
                // enter zone event
                currentZones.add(zone);

                LocationEvent event =
                        new LocationEvent(zone.getName(), time,
                                LocationEvent.Type.ZONE, LocationEvent.Action.IN);
                // notifyEvent(event);
                //call location api to upload location event info
                // callCloudLogicFmwLocationApi(event);
                break;
            }
        }
    }

    @Override
    public void buildingLoadingCanceled() {
        // Loading of building was cancelled
    }

    // build a post body object and returns the json string
    private String getJsonForPostBody(LocationEvent le) {
        cognitoUserPool = getUserPoolFromConfig();
        LocationApiBody pb = new LocationApiBody();
        BodyParams bp = new BodyParams();
        EventPayload epl = new EventPayload();
        pb.setBodyParams(bp);
        bp.setEventType("zoneEvent");
        epl.setTenantId(cognitoUserPool.getUserPoolId());
        epl.setTime(le.getTime());
        epl.setActionTime(getDateCurrentTimeZone(le.getTime()));
        epl.setUserName(cognitoUserPool.getCurrentUser().getUserId());
        epl.setZoneId(le.getId());
        epl.setZoneName(le.getId());
        epl.setAction(le.getAction().name());
        bp.setEventPayload(epl);

        String jsonStr = objectToJson.toJson(pb);
        return jsonStr;
    }

    public  String getDateCurrentTimeZone(long timestamp) {

        SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy 'at' HH:mm:ss z");
        String value = formatter.format(new Date(timestamp));
        return value;
    }

    private CognitoUserPool getUserPoolFromConfig() {
        cognitoUserPool = null;
        try {
            JSONObject myJSON = IdentityManager.getDefaultIdentityManager().getConfiguration().optJsonObject("CognitoUserPool");
            final String COGNITO_POOL_ID = myJSON.getString("PoolId");
            final String COGNITO_CLIENT_ID = myJSON.getString("AppClientId");
            final String COGNITO_CLIENT_SECRET = myJSON.getString("AppClientSecret");
            final String REGION = myJSON.getString("Region");
            cognitoUserPool = new CognitoUserPool(getApplicationContext(), COGNITO_POOL_ID, COGNITO_CLIENT_ID, COGNITO_CLIENT_SECRET, Regions.fromName(REGION));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return cognitoUserPool;
    }

    private void initializeIndoorsLibrary() {
        try {
            IndoorsFactory.createInstance(this, indoorsApiKey, this, false);
        }catch(Exception e) {
            Log.d(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        // notifyServiceStarted();
        return START_STICKY;
    }

    private void notifyServiceStarted() {
        Intent intent = new Intent(ACTION_SERVICE_STARTED);
        sendBroadcast(intent);
    }

    private void notifyServiceStoped() {
        Intent intent = new Intent(ACTION_SERVICE_STOPPED);
        sendBroadcast(intent);
    }

    private void notifyEvent(LocationEvent event) {
        Intent intent = new Intent(ACTION_LOCATION_EVENT);
        intent.putExtra(LocationEvent.LOCATION_EVENT, event);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        // notifyServiceStoped();
        super.onDestroy();
    }
    //calls the api gateway api for uploading the location info
    public void callCloudLogicFmwLocationApi(LocationEvent lEvent) {
        // Create components of api request
        final String method = "POST";
        final String path = "/fmw-location";
        //construct the POST body from LocationEvent and also form logged in user
        final String body = getJsonForPostBody(lEvent);

        final byte[] content = body.getBytes(StringUtils.UTF8);
        //query parameters
        final Map parameters = new HashMap<>();
        parameters.put("lang", "en_US");
        //request headers
        final Map headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        // Use components to create the api request
        ApiRequest localRequest =
                new ApiRequest(apiLocationClient.getClass().getSimpleName())
                        .withPath(path)
                        .withHttpMethod(HttpMethodName.valueOf(method))
                        .withHeaders(headers)
                        .withParameters(parameters);

        // Only set body if it has content.
        if (body.length() > 0) {
            localRequest = localRequest
                    .addHeader("Content-Length", String.valueOf(content.length))
                    .withBody(content);
        }

        final ApiRequest request = localRequest;

        // Make network call on background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(LOG_TAG,
                            "Invoking API w/ Request : " +
                                    request.getHttpMethod() + ":" + request.getPath());

                    final ApiResponse response = apiLocationClient.execute(request);
                    final InputStream responseContentStream = response.getContent();
                    if (responseContentStream != null) {
                        final String responseData = IOUtils.toString(responseContentStream);
                        Log.d(LOG_TAG, "Response : " + responseData);
                    }
                    Log.d(LOG_TAG, response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e(LOG_TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

}

// public class LocationService extends Service
//         implements IndoorsServiceCallback, IndoorsLocationListener {
//     private static final String LOG_TAG = LocationService.class.getSimpleName();


//     public static final String ACTION_SERVICE_STARTED = "com.sharpsec.fmw.ACTION_SERVICE_STARTED";
//     public static final String ACTION_SERVICE_STOPPED = "com.sharpsec.fmw.ACTION_SERVICE_STOPPED";
//     public static final String ACTION_LOCATION_EVENT = "com.sharpsec.fmw.ACTION_LOCATION_EVENT";

//     private List<Zone> currentZones = new ArrayList<>();
//     private static String tag = "LocationService";

//     private Indoors indoors = null;
//     private String serverUrl = "";  // need to point to our own Cloud's Location Service endpoint
//     private String indoorsApiKey = "";  // Indoors-provided API KEY for Android SDK

//     private FmwlocationClient apiLocationClient;

//     // private AWSConfiguration configInfo;
//     // final int apiIndex = 0;
//     // String endpoint = "";
//     Gson objectToJson = null;
//     private CognitoUserPool cognitoUserPool;

//     @Nullable
//     @Override
//     public IBinder onBind(Intent intent) {
//         return null;
//     }

//     @Override
//     public void onCreate() {
//         super.onCreate();
//         updateValuesFromPreferences();
//         initializeIndoorsLibrary();
//         objectToJson = new GsonBuilder().disableHtmlEscaping().create();
//         // configInfo = AWSMobileClient.getInstance().getConfiguration();

//         //create a client object
//         apiLocationClient = new ApiClientFactory()
//                 .credentialsProvider(AWSMobileClient.getInstance().getCredentialsProvider())
//                 .build(FmwlocationClient.class);

//     }


//     private void updateValuesFromPreferences() {
//         try {
//             SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
//             serverUrl = sp.getString(Prefs.SERVER_URL, Prefs.SERVER_URL_VALUE);
//             indoorsApiKey = sp.getString(Prefs.INDOORS_API_KEY, Prefs.INDOORS_API_KEY_VALUE);
//         } catch (Exception e) {
//             e.printStackTrace();
//             Toast.makeText(this, "Server URL or 'Indoors' API KEY not set." +
//                             "Set them first from 'Preferences' menu...",
//                     Toast.LENGTH_SHORT).show();
//         }
//     }

//     private void initializeIndoorsLibrary() {
//         IndoorsFactory.createInstance(this, indoorsApiKey, this, false);
//     }

//     @Override
//     public int onStartCommand(Intent intent, int flags, int startId) {
//         super.onStartCommand(intent, flags, startId);
//         notifyServiceStarted();
//         return START_STICKY;
//     }

//     private void notifyServiceStarted() {
//         Intent intent = new Intent(ACTION_SERVICE_STARTED);
//         sendBroadcast(intent);
//     }

//     private void notifyServiceStoped() {
//         Intent intent = new Intent(ACTION_SERVICE_STOPPED);
//         sendBroadcast(intent);
//     }

//     private void notifyEvent(LocationEvent event) {
//         Intent intent = new Intent(ACTION_LOCATION_EVENT);
//         intent.putExtra(LocationEvent.LOCATION_EVENT, event);
//         // sendBroadcast(intent);
//     }

//     @Override
//     public void onDestroy() {
//         notifyServiceStoped();
//         super.onDestroy();
//     }

//     @Override
//     public void connected() {
//         indoors = IndoorsFactory.getInstance();
//         indoors.registerLocationListener(this);

//         indoors.setLocatedCloudBuilding(1160951400, new LocalizationParameters(), true);
//         Log.i(tag, "connected");
//     }

//     @Override
//     public void onError(IndoorsException indoorsException) {
//         // error occurred connecting with indoo.rs
//     }

//     @Override
//     public void loadingBuilding(LoadingBuildingStatus loadingBuildingStatus) {
//         // indoo.rs is still downloading or parsing the requested building
//     }

//     @Override
//     public void buildingLoaded(Building building) {
//         // indoo.rs SDK successfully loaded the building you requested and
//         // calculates a position now
//     }

//     @Override
//     public void leftBuilding(Building building) {
//         // Deprecated
//     }

//     @Override
//     public void buildingReleased(Building building) {
//         // Another building was loaded, you can release any resources related to linked building
//     }

//     @Override
//     public void positionUpdated(Coordinate userPosition, int accuracy) {
//         /*
//           Is called each time the Indoors Library calculated a new position for the user
//           If Lat/Lon/Rotation of your building are set correctly you can calculate a
//           GeoCoordinate for your users current location in the building.
//          */
//     }

//     @Override
//     public void orientationUpdated(float orientation) {
//         // user changed the direction he's heading to
//     }

//     @Override
//     public void changedFloor(int floorLevel, String name) {
//         // user changed the floor
//     }

//     @Override
//     public void enteredZones(List<Zone> zones) {
//         long time = new Date().getTime();

//         for (Zone currentZone : currentZones) {
//             if (!LocationUtil.containsZone(zones, currentZone)) {
//                 // left zone event
//                 LocationUtil.removeZone(currentZones, currentZone);

//                 LocationEvent event =
//                         new LocationEvent(currentZone.getName(), time,
//                                 LocationEvent.Type.ZONE, LocationEvent.Action.OUT);
//                 notifyEvent(event);
//                 //call location api to upload location event info
//                 // callCloudLogicFmwLocationApi(event);
//                 break;
//             }
//         }
//         for (Zone zone : zones) {
//             if (!LocationUtil.containsZone(currentZones, zone)) {
//                 // enter zone event
//                 currentZones.add(zone);

//                 LocationEvent event =
//                         new LocationEvent(zone.getName(), time,
//                                 LocationEvent.Type.ZONE, LocationEvent.Action.IN);
//                 notifyEvent(event);
//                 //call location api to upload location event info
//                 // callCloudLogicFmwLocationApi(event);
//                 break;
//             }
//         }
//     }

//     @Override
//     public void buildingLoadingCanceled() {
//         // Loading of building was cancelled
//     }

//     // build a post body object and returns the json string
//     private String getJsonForPostBody(LocationEvent le) {
//         cognitoUserPool = getUserPoolFromConfig();
//         LocationApiBody pb = new LocationApiBody();
//         BodyParams bp = new BodyParams();
//         EventPayload epl = new EventPayload();
//         pb.setBodyParams(bp);
//         bp.setEventType("zoneEvent");
//         epl.setTenantId(cognitoUserPool.getUserPoolId());
//         epl.setTime(le.getTime());
//         epl.setActionTime(getDateCurrentTimeZone(le.getTime()));
//         epl.setUserName(cognitoUserPool.getCurrentUser().getUserId());
//         epl.setZoneId(le.getId());
//         epl.setZoneName(le.getId());
//         epl.setAction(le.getAction().name());
//         bp.setEventPayload(epl);

//         String jsonStr = objectToJson.toJson(pb);
//         return jsonStr;
//     }

//     public  String getDateCurrentTimeZone(long timestamp) {

//         SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy 'at' HH:mm:ss z");
//         String value = formatter.format(new Date(timestamp));
//         return value;
//     }

//     //constructs and returns user pool object from the config file
//     private CognitoUserPool getUserPoolFromConfig() {
//         cognitoUserPool = null;
//         try {
//             JSONObject myJSON = IdentityManager.getDefaultIdentityManager().getConfiguration().optJsonObject("CognitoUserPool");
//             final String COGNITO_POOL_ID = myJSON.getString("PoolId");
//             final String COGNITO_CLIENT_ID = myJSON.getString("AppClientId");
//             final String COGNITO_CLIENT_SECRET = myJSON.getString("AppClientSecret");
//             final String REGION = myJSON.getString("Region");
//             cognitoUserPool = new CognitoUserPool(getApplicationContext(), COGNITO_POOL_ID, COGNITO_CLIENT_ID, COGNITO_CLIENT_SECRET, Regions.fromName(REGION));
//         } catch (JSONException e) {
//             e.printStackTrace();
//         }

//         return cognitoUserPool;
//     }

//     //calls the api gateway api for uploading the location info
//     public void callCloudLogicFmwLocationApi(LocationEvent lEvent) {
//         // Create components of api request
//         final String method = "POST";
//         final String path = "/fmw-location";
//         //construct the POST body from LocationEvent and also form logged in user
//         final String body = getJsonForPostBody(lEvent);

//         final byte[] content = body.getBytes(StringUtils.UTF8);
//         //query parameters
//         final Map parameters = new HashMap<>();
//         parameters.put("lang", "en_US");
//         //request headers
//         final Map headers = new HashMap<>();
//         // IdentityProvider cp = IdentityManager.getDefaultIdentityManager().getCurrentIdentityProvider();
//         // String token = cp.getToken();
//         // JWT jwt = new JWT(token);
//         // headers.put("Authorization", "Bearer " + token);
//         headers.put("Content-Type", "application/json");

//         // Use components to create the api request
//         ApiRequest localRequest =
//                 new ApiRequest(apiLocationClient.getClass().getSimpleName())
//                         .withPath(path)
//                         .withHttpMethod(HttpMethodName.valueOf(method))
//                         .withHeaders(headers)
//                         .withParameters(parameters);

//         // Only set body if it has content.
//         if (body.length() > 0) {
//             localRequest = localRequest
//                     .addHeader("Content-Length", String.valueOf(content.length))
//                     .withBody(content);
//         }

//         final ApiRequest request = localRequest;

//         // Make network call on background thread
//         new Thread(new Runnable() {
//             @Override
//             public void run() {
//                 try {
//                     Log.d(LOG_TAG,
//                             "Invoking API w/ Request : " +
//                                     request.getHttpMethod() + ":" + request.getPath());

//                     final ApiResponse response = apiLocationClient.execute(request);
//                     final InputStream responseContentStream = response.getContent();
//                     if (responseContentStream != null) {
//                         final String responseData = IOUtils.toString(responseContentStream);
//                         Log.d(LOG_TAG, "Response : " + responseData);
//                     }
//                     Log.d(LOG_TAG, response.getStatusCode() + " " + response.getStatusText());

//                 } catch (final Exception exception) {
//                     Log.e(LOG_TAG, exception.getMessage(), exception);
//                     exception.printStackTrace();
//                 }
//             }
//         }).start();
//     }

// }
