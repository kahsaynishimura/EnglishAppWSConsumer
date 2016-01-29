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
import com.karina.alicesadventures.util.EchoPractice;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;
import com.purplebrain.adbuddiz.sdk.AdBuddizDelegate;
import com.purplebrain.adbuddiz.sdk.AdBuddizError;

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

        //AdBuddiz
        showAd(TransitionActivity.this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AdBuddiz.onDestroy(); // to minimize memory footprint
    }


    public static void showAd(final Activity activity) {
        AdBuddiz.cacheAds(activity);                    // start caching ads

        AdBuddiz.showAd(activity);
        // OPTIONAL, to get more info about the SDK behavior for AdBuddiz methods.
        // All callbacks in the delegate will be called in UI thread.
        AdBuddiz.setDelegate(new AdBuddizDelegate() {

            @Override
            public void didCacheAd() {
                //Toast.makeText(activity, "didCacheAd", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void didShowAd() {
                // Toast.makeText(activity, "didShowAd", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void didFailToShowAd(AdBuddizError error) {
                // Toast.makeText(activity, error.name(), Toast.LENGTH_SHORT).show();
                continueToPractice();
            }

            @Override
            public void didClick() {
                //  Toast.makeText(activity, "didClick", Toast.LENGTH_SHORT).show();
                continueToPractice();
            }

            @Override
            public void didHideAd() {
                //  Toast.makeText(activity, "didHideAd", Toast.LENGTH_SHORT).show();
                continueToPractice();
            }

            public void continueToPractice() {
                Intent i = new Intent(activity, PracticeActivity.class);
                activity.startActivity(i);
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                activity.finish();
                AdBuddiz.onDestroy();
            }

        });
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

