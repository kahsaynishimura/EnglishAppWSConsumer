package com.karina.alicesadventures;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.karina.alicesadventures.util.AnalyticsApplication;
import com.karina.alicesadventures.model.Exercise;

//keeps track of the current Exercise
public class TransitionActivity extends AppCompatActivity {

    private static final long TRANSITION_PAUSE = 1000;
    private Tracker mTracker;
    private static final String TAG = "TransitionActivity";

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition);
        RelativeLayout container = (RelativeLayout) findViewById(R.id.container_transition);

        //pegar imagem do exercicio dependendo do contador e da licao
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TransitionActivity.this);

        Exercise exercise = loadExercise(sharedPreferences.getInt("exercise_count", 0));
        if (exercise != null) {
            try {
                int imageResource = getResources().getIdentifier("@drawable/" + exercise.getTransitionImage(), null, getPackageName());
                if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) { //versao api >=21
                    container.setBackground(getDrawable(imageResource));
                } else {

                    Drawable res = getResources().getDrawable(imageResource);
                    container.setBackgroundResource(imageResource);
                }
            } catch (Resources.NotFoundException e) {
                Toast.makeText(this, "Erro", Toast.LENGTH_SHORT).show();
            }
        }

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(TransitionActivity.this, PracticeActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                TransitionActivity.this.finish();
            }
        }, TRANSITION_PAUSE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }



    @Override
    protected void onResume() {
        super.onResume();
        String name = "Transition";
        Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName("Screen~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private Exercise loadExercise(Integer exerciseCount) {
        //retrieve sentences to practice from db for each exercise

        if (PracticeActivity.exercises.size() > exerciseCount) {//is there another exercise
            //yes
            return PracticeActivity.exercises.get(exerciseCount);
        } else {
            //no
            return null;
        }
    }
}

