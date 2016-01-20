package com.karina.alicesadventures;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.karina.alicesadventures.Util.HTTPConnection;
import com.karina.alicesadventures.Util.SessionManager;
import com.karina.alicesadventures.model.User;
import com.karina.alicesadventures.parsers.UserXmlParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by karina on 2016-01-19.
 */
public class FacebookFragment extends Fragment {
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private AddUserTask mAddUserTask;

        @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.facebook_login, container, false);

        loginButton = (LoginButton) view.findViewById(R.id.fb_login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile, email"));
        // If using in a fragment
        loginButton.setFragment(this);
        // Other app specific specialization

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
               Profile p= Profile.getCurrentProfile();
//                info.setText(
//                        "User ID: "
//                                + loginResult.getAccessToken().getUserId()
//                                + "\n" +
//                                "Auth Token: "
//                                + loginResult.getAccessToken().getToken()
//                );
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                // Application code
                                Log.v("LoginActivity", response.toString());
                                try {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("data[User][username]", object.get("email").toString());
                                    hashMap.put("data[User][name]", object.get("name").toString());

                                        mAddUserTask=null;
                                        mAddUserTask = new AddUserTask("http://karinanishimura.com.br/cakephp/users/add_fb_api.xml", hashMap);
                                        mAddUserTask.execute();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public class AddUserTask extends AsyncTask<Void, Void, User> {

        private final String url;
        HashMap hashMap;

        public AddUserTask(String url, HashMap<String, String> hashMap) {
            this.hashMap = hashMap;
            this.url = url;
        }

        @Override
        protected User doInBackground(Void... params) {
            User user= null;
            HTTPConnection httpConnection = new HTTPConnection();
            UserXmlParser userXmlParser = new UserXmlParser();
            String result = "";
            try {
                result = httpConnection.sendPost(url, hashMap);
                user = userXmlParser.parse(new StringReader(result));
            } catch (Exception e) {
                e.printStackTrace();
                mAddUserTask = null;
            }
            System.out.println(result);
            return user;
        }


        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            mAddUserTask = null;
            if (user != null) {
               SessionManager sessionManager=new SessionManager(getActivity());

                sessionManager.createLoginSession(user);

                Intent i = new Intent(getActivity(), BookActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
               getActivity(). finish();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            mAddUserTask = null;

        }
    }
}
