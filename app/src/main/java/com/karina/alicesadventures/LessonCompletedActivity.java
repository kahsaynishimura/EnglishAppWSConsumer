package com.karina.alicesadventures;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.karina.alicesadventures.parsers.MessageXmlParser;
import com.karina.alicesadventures.util.AnalyticsApplication;
import com.karina.alicesadventures.util.HTTPConnection;
import com.karina.alicesadventures.util.SessionManager;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;
import com.purplebrain.adbuddiz.sdk.AdBuddizRewardedVideoDelegate;
import com.purplebrain.adbuddiz.sdk.AdBuddizRewardedVideoError;

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
        final Activity activity = LessonCompletedActivity.this;
        AdBuddiz.RewardedVideo.setDelegate(new AdBuddizRewardedVideoDelegate() {
            @Override
            public void didFetch() { // Next rewarded video is ready to be displayed

                Toast.makeText(activity, "didFetch", Toast.LENGTH_SHORT).show();
                AdBuddiz.RewardedVideo.show(activity);
                //  AdBuddiz.RewardedVideo.show(activity);
                //  btnShowVideoAd.setEnabled(true); // enable the button since the video can be displayed
            }

            @Override
            public void didComplete() { // User closed the ad after having fully watched the video
                Toast.makeText(activity, "didComplete", Toast.LENGTH_SHORT).show();

                // giveRewardToUser(); // reward the user since he watched the video

                // fetching manually next rewarded video
                // btnShowVideoAd.setEnabled(false); // disable the button while fetching
                AdBuddiz.RewardedVideo.fetch(activity);
            }

            @Override
            public void didFail(AdBuddizRewardedVideoError error) { // Something went wrong when fetching or showing a video
                Toast.makeText(activity, error.name(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void didNotComplete() { // Media player encountered an error
                Toast.makeText(activity, "didNotComplete", Toast.LENGTH_SHORT).show();
            }
        });


        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        PracticeActivity.exercises = new ArrayList<>();
        SessionManager sessionManager = new SessionManager(LessonCompletedActivity.this);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("data[User][id]", sessionManager.getUserDetails().get(SessionManager.KEY_ID));
        hashMap.put("data[User][last_completed_lesson]", getIntent().getExtras().getString("lesson_id"));
        try {
            mSaveLastLessonTask = new SaveLastLessonTask(HTTPConnection.SERVER_BASE_URL + "users/save_last_lesson_api.xml", hashMap);
            mSaveLastLessonTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        AdView mAdView = (AdView) findViewById(R.id.ad_view);

        AdRequest.Builder b = new AdRequest.Builder();

        String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = md5(android_id).toUpperCase();
        b.addTestDevice(deviceId);

        AdRequest adRequest = b.build();
        b.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
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


    public void viewPrizes(View v) {
        Intent i = new Intent(LessonCompletedActivity.this, ProductListActivity.class);
        startActivity(i);
    }

//    public void nextLesson(View v) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LessonCompletedActivity.this);
//
//        ArrayList<Lesson> lessons = getLessons(sharedPreferences.getString("book_id", "1"));
//        Lesson lastLesson = lessons.get(lessons.size() - 1);
//        Integer lessonId = sharedPreferences.getInt("lesson_id", 0);
//
//        if (lessonId != lastLesson.get_id()) {//if that was not the last lesson, start the next one
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putInt("exercise_count", 0);
//            editor.putInt("lesson_id", (lessonId + 1));
//
//            editor.putLong("start_time", 0);
//            editor.commit();
//
//            Intent i = new Intent(LessonCompletedActivity.this, TransitionActivity.class);
//            startActivity(i);
//        } else {
//            Intent i = new Intent(LessonCompletedActivity.this, BookCompletedActivity.class);
//            startActivity(i);
//        }
//        finish();
//    }


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

}
