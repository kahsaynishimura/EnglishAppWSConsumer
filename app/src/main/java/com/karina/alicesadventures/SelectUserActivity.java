package com.karina.alicesadventures;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.karina.alicesadventures.Util.HTTPConnection;
import com.karina.alicesadventures.Util.SessionManager;
import com.karina.alicesadventures.model.User;
import com.karina.alicesadventures.parsers.UserXmlParser;

import java.io.StringReader;
import java.util.HashMap;


public class SelectUserActivity extends ActionBarActivity {
    private LoginTask mLoginTask;
    private SessionManager session;
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        session = new SessionManager(getApplicationContext());
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
        }
        /*TODO:else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }*/

        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        /*else if (!mEmail.contains("@")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
*/
        if (cancel) {
            focusView.requestFocus();
        } else {

            HashMap<String,String> hashMap=new HashMap<>();

            hashMap.put("data[User][password]","t");
            hashMap.put("data[User][username]","t");
            try {
                mLoginTask = new LoginTask("http://karinanishimura.com.br/cakephp/users/login_api.xml", hashMap);
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
            }
            System.out.println(result);
            return user;
        }


        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            mLoginTask = null;
            if (user == null) {
                Toast.makeText(SelectUserActivity.this, getText(R.string.verify_internet_connection), Toast.LENGTH_LONG).show();
            } else{
                // save logged in user
                session.createLoginSession(user);

                Intent i=new Intent(SelectUserActivity.this,BookActivity.class);
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
