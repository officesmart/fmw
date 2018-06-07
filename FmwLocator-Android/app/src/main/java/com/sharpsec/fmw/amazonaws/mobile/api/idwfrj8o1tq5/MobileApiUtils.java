package com.sharpsec.fmw.amazonaws.mobile.api.idwfrj8o1tq5;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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


import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MobileApiUtils {
    private static String LOG_TAG = "MobileApiUtils";

    public static MobileApiClient createApiClient() {
        //create a client object
        MobileApiClient client = new ApiClientFactory()
                .credentialsProvider(AWSMobileClient.getInstance().getCredentialsProvider())
                .build(MobileApiClient.class);
        return client;
    }

    public static CognitoUserPool getUserPoolFromConfig(Context context) {
        CognitoUserPool userPool = null;
        try {
            JSONObject myJSON =
                    IdentityManager.getDefaultIdentityManager().getConfiguration().
                            optJsonObject("CognitoUserPool");
            final String COGNITO_POOL_ID = myJSON.getString("PoolId");
            final String COGNITO_CLIENT_ID = myJSON.getString("AppClientId");
            final String COGNITO_CLIENT_SECRET = myJSON.getString("AppClientSecret");
            final String REGION = myJSON.getString("Region");
            userPool = new CognitoUserPool(context, COGNITO_POOL_ID, COGNITO_CLIENT_ID,
                    COGNITO_CLIENT_SECRET, Regions.fromName(REGION));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return userPool;
    }

    public static void makeApiCall(final MobileApiClient client, String method, String path,
                                   String body, final MobileApiCallback callback) {

        //query parameters
        final Map parameters = new HashMap<>();
        parameters.put("lang", "en_US");
        //request headers
        final Map headers = new HashMap<>();
        // IdentityProvider cp = IdentityManager.getDefaultIdentityManager().getCurrentIdentityProvider();
        // String token = cp.getToken();
        // JWT jwt = new JWT(token);
        // headers.put("Authorization", "Bearer " + token);
        headers.put("Content-Type", "application/json");

        // Use components to create the api request
        ApiRequest localRequest =
                new ApiRequest(client.getClass().getSimpleName())
                        .withPath(path)
                        .withHttpMethod(HttpMethodName.valueOf(method))
                        .withHeaders(headers)
                        .withParameters(parameters);

        // Only set body if it has content.
        if ((body != null) && (body.length() > 0)) {
            final byte[] content = body.getBytes(StringUtils.UTF8);
            localRequest = localRequest
                    .addHeader("Content-Length", String.valueOf(content.length))
                    .withBody(content);
        }

        final ApiRequest request = localRequest;

        ApiCallTask task = new ApiCallTask(callback);
        task.execute(client, request);
    }

    private static class ApiCallTask extends AsyncTask<Object, Void, String> {

        private MobileApiCallback callback;

        public ApiCallTask(MobileApiCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Object... objects) {
            MobileApiClient client = (MobileApiClient) objects[0];
            ApiRequest request = (ApiRequest) objects[1];

            String responseData = null;

            try {
                Log.d(LOG_TAG,
                        "Invoking API w/ Request : " +
                                request.getHttpMethod() + ":" + request.getPath());

                final ApiResponse response = client.execute(request);
                final InputStream responseContentStream = response.getContent();
                if (responseContentStream != null) {
                    responseData = IOUtils.toString(responseContentStream);
                    Log.d(LOG_TAG, "Response : " + responseData);
                }
                Log.d(LOG_TAG, response.getStatusCode() + " " + response.getStatusText());

            } catch (final Exception exception) {
                Log.e(LOG_TAG, exception.getMessage(), exception);
                exception.printStackTrace();
            }

            return responseData;
        }

        @Override
        protected void onPostExecute(String result) {
            if(callback != null) {
                if(result == null) {
                    callback.onError();
                } else {
                    callback.onResponse(result);
                }
            }
        }
    }
}
