package com.karina.alicesadventures;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.karina.alicesadventures.parsers.MessageXmlParser;
import com.karina.alicesadventures.util.AnalyticsApplication;
import com.karina.alicesadventures.util.EchoPractice;
import com.karina.alicesadventures.util.HTTPConnection;
import com.karina.alicesadventures.util.SessionManager;

import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;


public class LessonCompletedActivity extends ActionBarActivity {
    private SaveLastLessonTask mSaveLastLessonTask;
    private Tracker mTracker;
    private static final String TAG = "LessonCompletedActivity";


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_completed);


        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        PracticeActivity.exercises = new ArrayList<>();
        SessionManager sessionManager = new SessionManager(LessonCompletedActivity.this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Integer lessonId = sharedPreferences.getInt("lesson_id", 0);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("data[User][id]", sessionManager.getUserDetails().get(SessionManager.KEY_ID));
        hashMap.put("data[User][last_completed_lesson]", lessonId.toString());
        try {
            mSaveLastLessonTask = new SaveLastLessonTask(EchoPractice.SERVER_BASE_URL + "users/save_last_lesson_api.xml", hashMap);
            mSaveLastLessonTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Admob
        AdView mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest.Builder b = new AdRequest.Builder();

        if (EchoPractice.DEBUG_MODE) {
            String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceId = md5(android_id).toUpperCase();
            b.addTestDevice(deviceId);
        }
        AdRequest adRequest = b.build();
        mAdView.loadAd(adRequest);

    }



    @Override
    protected void onResume() {
        super.onResume();
        String name = "Lesson Completed";
        Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName("Screen~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void viewPrizes(View v) {
        Intent i = new Intent(LessonCompletedActivity.this, ProductListActivity.class);
        startActivity(i);
    }

    private class SaveLastLessonTask extends AsyncTask<Void, Void, String> {

        private final String url;
        HashMap hashMap;

        public SaveLastLessonTask(String url, HashMap<String, String> hashMap) {
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
                mSaveLastLessonTask = null;

            }
            System.out.println(result);
            return message;
        }


        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            mSaveLastLessonTask = null;
            if (message == null) {
                Toast.makeText(LessonCompletedActivity.this, getText(R.string.verify_internet_connection), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LessonCompletedActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            mSaveLastLessonTask = null;

        }
    }

    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
