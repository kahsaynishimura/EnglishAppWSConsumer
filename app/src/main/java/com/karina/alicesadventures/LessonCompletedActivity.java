package com.karina.alicesadventures;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.karina.alicesadventures.Util.HTTPConnection;
import com.karina.alicesadventures.Util.SessionManager;
import com.karina.alicesadventures.model.DBHandler;
import com.karina.alicesadventures.model.Lesson;
import com.karina.alicesadventures.parsers.MessageXmlParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;


public class LessonCompletedActivity extends ActionBarActivity {
    private AddPracticeTask mAddPracticeTask;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_completed);

        PracticeActivity.exercises=new ArrayList<>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LessonCompletedActivity.this);

        saveLastLessonCompletedId(sharedPreferences);

        long millis = sharedPreferences.getLong("start_time", 0L);
        Date startTime = new Date(millis);
        Integer totalHits = sharedPreferences.getInt("correct_sentence_count", 0);
        Integer wrongSentenceCount = sharedPreferences.getInt("wrong_sentence_count", 0);

        //no matter what happens, if the student gets here, he is rewarded.

        DateFormat df = DateFormat.getTimeInstance();
        Date finishTime = new Date();
        ((TextView) findViewById(R.id.txt_start_time)).setText(getString(R.string.start_time) + ": " + df.format(startTime));
        ((TextView) findViewById(R.id.txt_finish_time)).setText(getString(R.string.finish_time) + ": " + df.format(finishTime));
        ((TextView) findViewById(R.id.txt_correct)).setText(getString(R.string.correct) + ": " + totalHits);

        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SessionManager sessionManager = new SessionManager(LessonCompletedActivity.this);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("data[Practice][user_id]", sessionManager.getUserDetails().get(SessionManager.KEY_ID));
        hashMap.put("data[Practice][start_time]", df.format(startTime));
        hashMap.put("data[Practice][finish_time]", df.format(finishTime));
        hashMap.put("data[Practice][points]", totalHits.toString());
        try {
            mAddPracticeTask = new AddPracticeTask("http://karinanishimura.com.br/cakephp/practices/add_api.xml", hashMap);
            mAddPracticeTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        AdView mAdView = (AdView) findViewById(R.id.ad_view);

        AdRequest.Builder b=new AdRequest.Builder();

            String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceId = md5(android_id).toUpperCase();
            b.addTestDevice(deviceId);

        AdRequest adRequest = b.build();
        b.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override

            public void onAdLoaded() {
                //Toast.makeText(LessonCompletedActivity.this, "entrou", Toast.LENGTH_LONG).show();
            }
        });
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
    public void saveLastLessonCompletedId(SharedPreferences sharedPreferences) {

        DBHandler db = null;

        try {
            InputStream is = getBaseContext().getAssets()
                    .open(DBHandler.DATABASE_NAME);
            db = new DBHandler(this, is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (db != null) {
            db.saveLastLessonCompletedId(sharedPreferences.getInt("user_id", 0), sharedPreferences.getInt("lesson_id", 0));
        }
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
//            editor.putInt("wrong_sentence_count", 0);
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


    private class AddPracticeTask extends AsyncTask<Void, Void, String> {

        private final String url;
        HashMap hashMap;

        public AddPracticeTask(String url, HashMap<String, String> hashMap) {
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
                mAddPracticeTask = null;

            }
            System.out.println(result);
            return message;
        }


        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            mAddPracticeTask = null;
            if (message == null) {
                Snackbar.make(((FloatingActionButton) findViewById(R.id.fab)), getText(R.string.verify_internet_connection), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                Toast.makeText(LessonCompletedActivity.this, message, Toast.LENGTH_LONG).show();


            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            mAddPracticeTask = null;

        }
    }

}
