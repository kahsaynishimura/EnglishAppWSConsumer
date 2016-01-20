package com.karina.alicesadventures;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.karina.alicesadventures.util.AnalyticsApplication;
import com.karina.alicesadventures.util.HTTPConnection;
import com.karina.alicesadventures.adapters.LessonAdapter;
import com.karina.alicesadventures.model.Lesson;
import com.karina.alicesadventures.parsers.LessonsXmlParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LessonActivity extends ActionBarActivity {
    private ListLessonsTask mListLessonsTask;
    private Tracker mTracker;
    private static final String TAG = "LessonActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        loadComponents();
    }

    private void loadComponents() {

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LessonActivity.this);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("data[Lesson][book_id]", sharedPreferences.getString("book_id", "1"));
        mListLessonsTask = new ListLessonsTask("http://karinanishimura.com.br/cakephp/lessons/index_api.xml", hashMap);
        mListLessonsTask.execute();
        AdView mAdView = (AdView) findViewById(R.id.ad_view);

        AdRequest.Builder b = new AdRequest.Builder();

//        String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
//        String deviceId = LessonCompletedActivity.md5(android_id).toUpperCase();
//        b.addTestDevice(deviceId);

        AdRequest adRequest = b.build();
        b.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        mAdView.loadAd(adRequest);

    }

    @Override
    protected void onResume() {
        super.onResume();

        String name = "List of lessons";
        Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName("Screen~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private class ListLessonsTask extends AsyncTask<Void, Void, List<Lesson>> {
        private final String url;
        HashMap hashMap;

        public ListLessonsTask(String url, HashMap<String, String> hashMap) {
            this.hashMap = hashMap;
            this.url = url;
        }

        @Override
        protected List<Lesson> doInBackground(Void... params) {
            List<Lesson> lessons = null;
            HTTPConnection httpConnection = new HTTPConnection();
            LessonsXmlParser lessonsXmlParser = new LessonsXmlParser();

            try {
                String result = httpConnection.sendPost(url, hashMap);
                lessons = lessonsXmlParser.parse(new StringReader(result));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return lessons;
        }


        @Override
        protected void onPostExecute(List<Lesson> lessons) {
            mListLessonsTask = null;
            super.onPostExecute(lessons);
            if (lessons == null) {
                Toast.makeText(LessonActivity.this, getText(R.string.verify_internet_connection), Toast.LENGTH_LONG).show();
            } else if (lessons.get(0) != null) {
                final ArrayAdapter<Lesson> a = new LessonAdapter(LessonActivity.this, R.layout.lesson_list_item, (ArrayList<Lesson>) lessons);
                ListView myLessons = (ListView) findViewById(R.id.lessons);
                myLessons.setAdapter(a);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mListLessonsTask = null;
        }
    }
}
