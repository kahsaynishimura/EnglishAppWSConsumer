package com.karina.alicesadventures;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import com.karina.alicesadventures.Util.HTTPConnection;
import com.karina.alicesadventures.adapters.LessonAdapter;
import com.karina.alicesadventures.model.DBHandler;
import com.karina.alicesadventures.model.Exercise;
import com.karina.alicesadventures.model.Lesson;
import com.karina.alicesadventures.parsers.ExercisesXmlParser;
import com.karina.alicesadventures.parsers.LessonsXmlParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LessonActivity extends ActionBarActivity {
    private ListLessonsTask mListLessonsTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        loadComponents();
    }

    private void loadComponents() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LessonActivity.this);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("data[Lesson][book_id]", sharedPreferences.getString("book_id", "1"));
        mListLessonsTask = new ListLessonsTask("http://karinanishimura.com.br/cakephp/lessons/index_api.xml", hashMap);
        mListLessonsTask.execute();

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
