package com.karina.alicesadventures.model;

/**
 * Created by Karina Nishimura on 15-09-30.
 */


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DBHandler {

    public static String DATABASE_NAME = "englishapp.db";

    static final int DATABASE_VERSION = 1;


    private static final String TABLE_LESSON = "lesson";
    private static final String TABLE_EXERCISE = "exercise";
    private static final String TABLE_SCRIPT_ENTRY = "script_entry";
    private static final String TABLE_USER = "user";
    private static final String TABLE_PRACTICE_HISTORY = "practice_history";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_LESSON_ID = "lesson_id";
    private static final String COLUMN_FUNCTION_ID = "function_id";
    private static final String COLUMN_EXERCISE_ID = "exercise_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CODE = "code";
    private static final String COLUMN_LAST_COMPLETED_LESSON_ID = "last_completed_lesson_id";
    private static final String COLUMN_BOOK_ID = "book_id";
    private static final String COLUMN_TEXT_TO_SHOW = "text_to_show";
    private static final String COLUMN_TRANSITION_IMAGE = "transition_image";
    private static final String COLUMN_TEXT_TO_CHECK = "text_to_check";
    private static final String COLUMN_SCRIPT_INDEX = "script_index";
    private static final String COLUMN_TEXT_TO_READ = "text_to_read";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_TOTAL_HITS = "total_hits";
    private static final String COLUMN_PERCENTAGE_WRONG = "percentage_wrong";
    private static final String COLUMN_START_TIME = "start_time";
    private static final String COLUMN_FINISH_TIME = "finish_time";
    private static final String COLUMN_TOTAL_POINTS = "total_points";

    private DBHelper DBHelper;
    private SQLiteDatabase db = null;

    public DBHandler(Context ctx, InputStream inputStream) {
        DBHelper = new DBHelper(ctx);
        try {
            // Environment.getExternalStorageDirectory().getPath()
            String destinationPath = "/data/data/" + ctx.getPackageName()
                    + "/databases";
            File f = new File(destinationPath);
            //  if (!f.exists()) {
            boolean bool = f.mkdirs();
            Log.i("DBHandler", "Made directory for DB: " + bool);
            bool = f.createNewFile();
            Log.i("DBHandler", "DB File created: " + bool);
                /* copy the db from the assets folder into

                 the databases folder*/
            DBHandler.CopyDB(inputStream, new FileOutputStream(destinationPath
                    + "/" + DBHandler.DATABASE_NAME));
            //    }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void CopyDB(InputStream inputStream, OutputStream outputStream)
            throws IOException {
        // ---copy 1K bytes at a time---
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.close();
    }

    // ---opens the database---
    public DBHandler open() {
        try {
            db = DBHelper.getWritableDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    // ---closes the database---
    public void close() {
        DBHelper.close();
    }

    public Lesson findLesson(Integer lessonId) {
        Lesson e = new Lesson();
        open();
        Cursor c = db.query(TABLE_LESSON,
                new String[]{COLUMN_NAME
                },
                COLUMN_ID + "= ? ", new String[]{lessonId.toString()},
                null, null, null, null);
        if (c.moveToFirst()) {
            do {

                e.setName(c.getString(0));
            } while (c.moveToNext());
        }
        close();

        return e;
    }

    public Exercise findExercise(Integer exerciseId) {
        Exercise e = new Exercise();
        open();
        Cursor c = db.query(TABLE_EXERCISE,
                new String[]{COLUMN_NAME, COLUMN_TRANSITION_IMAGE
                },
                COLUMN_ID + "= ? ", new String[]{exerciseId.toString()},
                null, null, null, null);
        if (c.moveToFirst()) {
            do {

                e.setName(c.getString(0));
                e.setTransitionImage(c.getString(1));
            } while (c.moveToNext());
        }
        close();

        return e;
    }


    private static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    public String addPracticeHistory(Integer userId, Integer lessonId, Integer totalHits, Integer percentageWrong, String startTime, String finishTime, Integer totalPoints) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(COLUMN_USER_ID, userId);
        insertValues.put(COLUMN_LESSON_ID, lessonId);
        insertValues.put(COLUMN_START_TIME, startTime);//mili
        insertValues.put(COLUMN_FINISH_TIME, finishTime);//mili
        insertValues.put(COLUMN_TOTAL_HITS, totalHits);
        insertValues.put(COLUMN_PERCENTAGE_WRONG, percentageWrong);
        insertValues.put(COLUMN_TOTAL_POINTS, totalPoints);

        open();
        long rowId = db.insert(TABLE_PRACTICE_HISTORY, null, insertValues);

        db.close();
        return rowId + "";

    }

    public Boolean saveLastLessonCompletedId(Integer userId, Integer lessonCompletedId) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(COLUMN_LAST_COMPLETED_LESSON_ID, lessonCompletedId.toString());
        open();
        Integer numberRows = db.update(
                TABLE_USER, insertValues, COLUMN_ID + " = " + userId, null);

        db.close();
        return numberRows > 0;

    }

    public ArrayList<ScriptEntry> findScripts(Integer exerciseId) {
        ArrayList<ScriptEntry> scriptEntries = new ArrayList<>();
        open();
        Cursor c = db.query(TABLE_SCRIPT_ENTRY,
                new String[]{COLUMN_ID, COLUMN_TEXT_TO_SHOW,
                        COLUMN_TEXT_TO_READ, COLUMN_TEXT_TO_CHECK, COLUMN_SCRIPT_INDEX, COLUMN_FUNCTION_ID},
                COLUMN_EXERCISE_ID + "= ? ", new String[]{exerciseId.toString()},
                null, null, null, null);
        if (c.moveToFirst()) {
            do {
                Exercise e = new Exercise();
                e.set_id(exerciseId);
                scriptEntries.add(new ScriptEntry(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getInt(5), e));
            } while (c.moveToNext());
        }
        close();

        return scriptEntries;
    }

    public User findUser(String userCode) {
        User user = null;
        open();
        Cursor c = db.query(TABLE_USER,
                new String[]{COLUMN_ID, COLUMN_NAME,
                        COLUMN_LAST_COMPLETED_LESSON_ID, COLUMN_CODE},
                COLUMN_CODE + " LIKE ? ", new String[]{userCode.toString()},
                null, null, null, null);
        if (c.moveToFirst()) {
            do {
                user = new User(c.getInt(0), c.getString(1), userCode, c.getInt(2));
            } while (c.moveToNext());
        }
        close();

        return user;
    }

    public ArrayList<Lesson> findLessons(Integer bookId) {
        ArrayList<Lesson> lessons = new ArrayList<>();
        open();
        Cursor c = db.query(TABLE_LESSON,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_BOOK_ID},
                COLUMN_BOOK_ID + "= ? ", new String[]{bookId.toString()},
                null, null, null, null);
        if (c.moveToFirst()) {
            do {
                lessons.add(new Lesson(c.getInt(0), c.getString(1), bookId));
            } while (c.moveToNext());
        }
        close();

        return lessons;
    }

    public ArrayList<Exercise> findExercises(Integer lessonId) {
        ArrayList<Exercise> exercises = new ArrayList<>();
        open();
        Cursor c = db.query(TABLE_EXERCISE,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_TRANSITION_IMAGE, COLUMN_LESSON_ID},
                COLUMN_LESSON_ID + "= ? ", new String[]{lessonId.toString()},
                null, null, null, null);
        if (c.moveToFirst()) {
            do {
                Lesson lesson = new Lesson();
                lesson.set_id(lessonId);
                exercises.add(new Exercise(c.getInt(0), c.getString(1), c.getString(2), lesson));
            } while (c.moveToNext());
        }
        close();

        return exercises;
    }
}
