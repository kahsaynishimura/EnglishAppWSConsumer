package com.karina.alicesadventures;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.karina.alicesadventures.util.AnalyticsApplication;
import com.karina.alicesadventures.util.EchoPractice;
import com.karina.alicesadventures.util.HTTPConnection;
import com.karina.alicesadventures.util.SessionManager;
import com.karina.alicesadventures.model.User;
import com.karina.alicesadventures.parsers.UserXmlParser;

import java.io.StringReader;
import java.util.HashMap;

public class SelectUserActivity extends FragmentActivity {
    private LoginTask mLoginTask;
    private SessionManager sessionManager;
    private Tracker mTracker;
    private static final String TAG = "SelectUserActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(SelectUserActivity.this);
       
        if (sessionManager.isLoggedIn()) {

            Intent i = new Intent(SelectUserActivity.this, BookActivity.class);

            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);

        } else {
            setContentView(R.layout.activity_select_user);
            FacebookSdk.sdkInitialize(getApplicationContext());
            FacebookFragment fragment = new FacebookFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.facebook_container, fragment)
                    .commit();

            LoginManager.getInstance().logOut();
            // Obtain the shared Tracker instance.
            AnalyticsApplication application = (AnalyticsApplication) getApplication();
            mTracker = application.getDefaultTracker();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        String name = "Login";
        Log.i(TAG, "Setting screen name: " + name);
        if (mTracker != null) {
            mTracker.setScreenName("Screen~" + name);
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    public void createAccount(View v) {
        Intent i = new Intent(SelectUserActivity.this, AddUserActivity.class);
        startActivity(i);
    }

    public void login(View v) {

        if (mLoginTask != null) {
            return;
        }
        EditText mEmailView = (EditText) findViewById(R.id.txtEmail);
        EditText mPasswordView = (EditText) findViewById(R.id.txtPassword);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String mEmail = mEmailView.getText().toString();
        String mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 8) {
            mPasswordView.setError(getString(R.string.error_field_length));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {

            HashMap<String, String> hashMap = new HashMap<>();

            hashMap.put("data[User][password]", mPassword);
            hashMap.put("data[User][username]", mEmail);
            try {
                mLoginTask = new LoginTask(EchoPractice.SERVER_BASE_URL + "users/login_api.xml", hashMap);
                mLoginTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class LoginTask extends AsyncTask<Void, Void, User> {

        private final String url;
        HashMap hashMap;

        public LoginTask(String url, HashMap<String, String> hashMap) {
            this.hashMap = hashMap;
            this.url = url;
        }

        @Override
        protected User doInBackground(Void... params) {
            User user = null;
            HTTPConnection httpConnection = new HTTPConnection();
            UserXmlParser userXmlParser = new UserXmlParser();
            String result =
                    "";
            try {
                result = httpConnection.sendPost(url, hashMap);
                user = userXmlParser.parse(new StringReader(result));
            } catch (Exception e) {
                e.printStackTrace();
                mLoginTask = null;
            }
            System.out.println(result);
            return user;
        }


        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            mLoginTask = null;
            if (user == null) {
                Snackbar.make(((Button) findViewById(R.id.btn_login)), getText(R.string.verify_internet_connection), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else if (user.get_id() == null) {
                Snackbar.make(((Button) findViewById(R.id.btn_login)), getText(R.string.user_pass_not_found), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                // save logged in user
                sessionManager.createLoginSession(user);

                Intent i = new Intent(SelectUserActivity.this, BookActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            mLoginTask = null;

        }
    }

}
