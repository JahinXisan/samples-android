package com.okta.android.samples.browser_sign_in;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.okta.android.samples.browser_sign_in.util.OktaProgressDialog;
import com.okta.appauth.android.OktaAppAuth;

import net.openid.appauth.AuthorizationException;

public class BrowserSignInActivity extends AppCompatActivity {
    private String TAG = "BrowserSignIn";
    private static final String EXTRA_FAILED = "failed";
    private OktaProgressDialog oktaProgressDialog;

    private OktaAppAuth mOktaAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser_sign_in);

        oktaProgressDialog = new OktaProgressDialog(this);

        ((Button) findViewById(R.id.browser_sign_in_btn)).setOnClickListener(v -> signIn());

        init();
    }

    private void init() {
        oktaProgressDialog.show();
        mOktaAuth = OktaAppAuth.getInstance(getApplicationContext());

        mOktaAuth.init(
                getApplicationContext(),
                new OktaAppAuth.OktaAuthListener() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            oktaProgressDialog.hide();
                            if (mOktaAuth.isUserLoggedIn()) {

                                Log.i(TAG, "User is already authenticated, proceeding " +
                                        "to token activity");
                                showUserInfo();
                            } else {
                                Log.i(TAG, "Login activity setup finished");
                                displayAuth();
                            }
                        });
                    }

                    @Override
                    public void onTokenFailure(@NonNull AuthorizationException ex) {
                        runOnUiThread(() -> {
                            oktaProgressDialog.hide();
                            showMessage(getString(R.string.init_error)
                                    + ":"
                                    + ex.errorDescription);
                        });
                    }
                },
                getResources().getColor(R.color.colorPrimary));
    }

    private void signIn() {
        Intent completionIntent = new Intent(this, UserInfoActivity.class);
        Intent cancelIntent = new Intent(this, BrowserSignInActivity.class);
        cancelIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        cancelIntent.putExtra(EXTRA_FAILED, true);

        mOktaAuth.login(
                getApplicationContext(),
                PendingIntent.getActivity(this, 0, completionIntent, 0),
                PendingIntent.getActivity(this, 0, cancelIntent, 0)
        );
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent().getBooleanExtra(EXTRA_FAILED, false)) {
            showMessage(getString(R.string.auth_canceled));
        }
    }

    private void displayAuth() {
        findViewById(R.id.auth_container).setVisibility(View.VISIBLE);
    }

    @MainThread
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @MainThread
    private void showUserInfo(){
        startActivity(new Intent(BrowserSignInActivity.this, UserInfoActivity.class));
        finish();
    }
}
