package com.karina.alicesadventures;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.karina.alicesadventures.util.AnalyticsApplication;
import com.karina.alicesadventures.util.EchoPractice;
import com.karina.alicesadventures.util.HTTPConnection;
import com.karina.alicesadventures.parsers.MessageXmlParser;

import java.io.StringReader;
import java.util.HashMap;

public class AddUserActivity extends AppCompatActivity {
    private AddUserTask mAddUserTask;
    private Tracker mTracker;
    private static final String TAG = "AddUserActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //make email edit text field the next field after name
        EditText e = (EditText) findViewById(R.id.txtName);
        e.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    ((EditText) findViewById(R.id.txtEmail)).requestFocus();
                    return true;
                }
                return false;
            }
        });
        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

    }

    @Override
    protected void onResume() {
        super.onResume();

        String name = "Create account";
        Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName("Screen~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void createAccount(View v) {

        if (mAddUserTask != null) {
            return;
        }

        EditText mNameView = (EditText) findViewById(R.id.txtName);
        EditText mEmailView = (EditText) findViewById(R.id.txtEmail);
        EditText mPasswordView = (EditText) findViewById(R.id.txtPassword);
        EditText mPasswordCheckView = (EditText) findViewById(R.id.txtPasswordCheck);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String mName = mNameView.getText().toString();
        String mEmail = mEmailView.getText().toString();
        String mPassword = mPasswordView.getText().toString();
        String mPasswordCheck = mPasswordCheckView.getText().toString();

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
        } else if (!mPassword.equals(mPasswordCheck)) {
            mPasswordCheckView.setError(getString(R.string.error_pass_mismatch));
            focusView = mPasswordView;
            cancel = true;
        }
        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!mEmail.contains("@")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(mName)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {

            HashMap<String, String> hashMap = new HashMap<>();

            hashMap.put("data[User][password]", mPassword);
            hashMap.put("data[User][username]", mEmail);
            hashMap.put("data[User][name]", mName);
            try {
                mAddUserTask = new AddUserTask(EchoPractice.SERVER_BASE_URL + "users/add_api.xml", hashMap);
                mAddUserTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public class AddUserTask extends AsyncTask<Void, Void, String> {

        private final String url;
        HashMap hashMap;

        public AddUserTask(String url, HashMap<String, String> hashMap) {
            this.hashMap = hashMap;
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            String message = null;
            HTTPConnection httpConnection = new HTTPConnection();
            MessageXmlParser messageXmlParser = new MessageXmlParser();
            String result = "";
            try {
                result = httpConnection.sendPost(url, hashMap);
                message = messageXmlParser.parse(new StringReader(result));
            } catch (Exception e) {
                e.printStackTrace();
                mAddUserTask = null;
            }
            System.out.println(result);
            return message;
        }


        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            mAddUserTask = null;
            if (message == null) {
                Snackbar.make(((FloatingActionButton) findViewById(R.id.fab)), getText(R.string.verify_internet_connection), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                finish();
            } else {
                Toast.makeText(AddUserActivity.this, message, Toast.LENGTH_LONG).show();
                //confirm email

                Intent i = new Intent(AddUserActivity.this, SelectUserActivity.class);
                startActivity(i);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            mAddUserTask = null;

        }
    }

}
