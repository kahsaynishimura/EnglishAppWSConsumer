package com.karina.alicesadventures;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.karina.alicesadventures.Util.HTTPConnection;
import com.karina.alicesadventures.model.Book;
import com.karina.alicesadventures.model.DBHandler;
import com.karina.alicesadventures.model.Lesson;
import com.karina.alicesadventures.model.Product;
import com.karina.alicesadventures.parsers.BookXmlParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class LessonCompletedActivity extends ActionBarActivity {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_completed);
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
       savePracticeSummary(sharedPreferences.getInt("user_id", 0), sharedPreferences.getInt("lesson_id", 0),
                totalHits, startTime.getTime(), finishTime.getTime(), totalHits);
    }


    private void savePracticeSummary(int userId, int lessonId, Integer totalHits, Long startTime, Long finishTime, Integer totalPoints) {
        DBHandler db = null;

        try {
            InputStream is = getBaseContext().getAssets()
                    .open(DBHandler.DATABASE_NAME);
            db = new DBHandler(this, is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (db != null) {
            //String id = db.addPracticeHistory(userId, lessonId, totalHits, startTime.toString(), finishTime.toString(), totalPoints);
        }
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

    public void nextLesson(View v) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LessonCompletedActivity.this);

        ArrayList<Lesson> lessons = getLessons(sharedPreferences.getInt("book_id", 1));
        Lesson lastLesson = lessons.get(lessons.size() - 1);
        Integer lessonId = sharedPreferences.getInt("lesson_id", 0);

        if (lessonId != lastLesson.get_id()) {//if that was not the last lesson, start the next one
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("exercise_count", 0);
            editor.putInt("lesson_id", (lessonId + 1));
            editor.putInt("wrong_sentence_count", 0);
            editor.putLong("start_time", 0);
            editor.commit();

            Intent i = new Intent(LessonCompletedActivity.this, TransitionActivity.class);
            startActivity(i);
        } else {
            Intent i = new Intent(LessonCompletedActivity.this, BookCompletedActivity.class);
            startActivity(i);
        }
        finish();
    }

    private ArrayList<Lesson> getLessons(Integer bookId) {

        DBHandler db = null;

        try {
            InputStream is = getBaseContext().getAssets()
                    .open(DBHandler.DATABASE_NAME);
            db = new DBHandler(this, is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (db != null) {
            return db.findLessons(bookId);
        }
        return null;
    }


}
