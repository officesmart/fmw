package com.sharpsec.fmw.auth.cognito;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amazonaws.mobile.auth.core.IdentityHandler;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.SignInStateChangeListener;
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration;
import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.sharpsec.fmw.FmwMainActivity;
import com.sharpsec.fmw.R;

public class LoginActivity extends AppCompatActivity {

    private static final String tag = "LoginActivity";
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(final AWSStartupResult awsStartupResult) {

                showSignIn();

            }
        }).execute();


        // Sign-in listener
        IdentityManager.getDefaultIdentityManager().addSignInStateChangeListener(new SignInStateChangeListener() {
            @Override
            public void onUserSignedIn() {
                Log.d(LOG_TAG, "User Signed In");
            }

            // Sign-out listener
            @Override
            public void onUserSignedOut() {

                Log.d(LOG_TAG, "User Signed Out");
                showSignIn();
            }
        });

    }

    private void showSignIn() {

        Log.d(LOG_TAG, "showSignIn");
        AuthUIConfiguration config =
                new AuthUIConfiguration.Builder()
                        .userPools(true)  // true? show the Email and Password UI
                        .logoResId(R.drawable.follow_me_256x256) // Change the logo
                        .backgroundColor(Color.BLACK) // Change the backgroundColor
                        .isBackgroundColorFullScreen(true) // Full screen backgroundColor
                        .fontFamily("sans-serif-light") // sans-serif-light as global font
                        .canCancel(false)
                        .build();

        SignInUI signin = (SignInUI) AWSMobileClient.getInstance().getClient(LoginActivity.this, SignInUI.class);
        signin.login(LoginActivity.this, FmwMainActivity.class).authUIConfiguration(config).execute();
    }


}
