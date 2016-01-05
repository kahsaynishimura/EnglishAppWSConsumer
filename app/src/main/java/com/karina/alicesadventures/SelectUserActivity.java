package com.karina.alicesadventures;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;

import com.karina.alicesadventures.model.DBHandler;
import com.karina.alicesadventures.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;


public class SelectUserActivity extends ActionBarActivity {

    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);
        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                DBHandler db = null;
                User user = new User();
                try {
                    InputStream is = getBaseContext().getAssets()
                            .open(DBHandler.DATABASE_NAME);
                    db = new DBHandler(SelectUserActivity.this, is);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (db != null) {
                   // String code = ((EditText) findViewById(R.id.student_code)).getText().toString();

                    //user = db.findUser(code);
                    //if (user != null) {
                        Intent i = new Intent(SelectUserActivity.this, LessonActivity.class);

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SelectUserActivity.this);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        //getting the current time in milliseconds, and creating a Date object from it:
                        Date date = new Date(System.currentTimeMillis()); //or simply new Date();

                        //converting it back to a milliseconds representation:
                        long millis = date.getTime();

                        editor.putInt("exercise_count", 0);
                        editor.putInt("correct_sentence_count", 0);
                        editor.putInt("wrong_sentence_count", 0);
                       // editor.putInt("user_id", user.get_id());
                      //  editor.putInt("last_lesson_completed_id", user.getLastCompletedLessonId());
                        editor.commit();
                        startActivity(i);
                        finish();
                 //   }
                }
            }
        }, SPLASH_TIME_OUT);
    }
}
