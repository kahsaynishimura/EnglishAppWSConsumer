package com.karina.alicesadventures;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.karina.alicesadventures.model.CurrentPracticeData;
import com.karina.alicesadventures.model.Exercise;
import com.karina.alicesadventures.model.Practice;
import com.karina.alicesadventures.model.SpeechScript;
import com.karina.alicesadventures.parsers.ExercisesXmlParser;
import com.karina.alicesadventures.parsers.MessageXmlParser;
import com.karina.alicesadventures.parsers.ScriptsXmlParser;
import com.karina.alicesadventures.util.AnalyticsApplication;
import com.karina.alicesadventures.util.EchoPractice;
import com.karina.alicesadventures.util.HTTPConnection;
import com.karina.alicesadventures.util.SessionManager;

import org.w3c.dom.Text;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PracticeActivity extends Activity {
    private AddPracticeTask mAddPracticeTask;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;
    private ListExercisesTask mListExercisesTask;
    private ListSpeechScriptsTask mListSpeechScriptsTask;
    private static final long TRANSITION_PAUSE = 1000;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 100;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private Tracker mTracker;
    private static final String TAG = "PracticeActivity";

    private String LOG_TAG = "PracticeActivity";
    InterstitialAd mInterstitialAd;

    CurrentPracticeData current;//stores current screen info - current screen state
    public static List<Exercise> exercises = new ArrayList<Exercise>();
    private TextToSpeech TTS;

    SharedPreferences sharedPreferences;


    //Progress Bar
    private ProgressBar mProgressScripts;
    private int mProgressScriptsStatus = 1;
    //Progress Bar
    private ProgressBar mProgressExercises;
    private int mProgressExercisesStatus = 0;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        checkConnection();
        current = new CurrentPracticeData();
        Integer lessonId = sharedPreferences.getInt("lesson_id", 0);
        setContentView(R.layout.activity_practice);

        //Progress Bar
        mProgressScripts = (ProgressBar) findViewById(R.id.scriptsProgressBar);
        mProgressExercises = (ProgressBar) findViewById(R.id.exercisesProgressBar);

        if (exercises.size() == 0) {//it is the first exercise
            loadExercises(lessonId);
        } else {
            if (hasMoreExercises()) {

                mProgressExercises.setMax(sharedPreferences.getInt("total_exercises", 0));
                changeCurrentExercise(sharedPreferences.getInt("exercise_count", 0));
                getScripts(current.getCurrentExercise().get_id());
            }
        }

        //Ads
        AdView mAdView = (AdView) findViewById(R.id.ad_view);

        AdRequest.Builder b = new AdRequest.Builder();
        if (EchoPractice.DEBUG_MODE) {
            String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceId = LessonCompletedActivity.md5(android_id).toUpperCase();
            b.addTestDevice(deviceId);
        }
        AdRequest adRequest = b.build();
        mAdView.loadAd(adRequest);


        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));


        requestNewInterstitial();

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Practice Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.karina.alicesadventures/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Practice Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.karina.alicesadventures/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class ListExercisesTask extends AsyncTask<Void, Void, List<Exercise>> {
        private final String url;
        HashMap hashMap;

        public ListExercisesTask(String url, HashMap<String, String> hashMap) {
            this.hashMap = hashMap;
            this.url = url;
        }

        @Override
        protected List<Exercise> doInBackground(Void... params) {
            List<Exercise> exercises = null;
            HTTPConnection httpConnection = new HTTPConnection();
            ExercisesXmlParser exercisesXmlParser = new ExercisesXmlParser();

            try {
                String result = httpConnection.sendPost(url, hashMap);
                exercises = exercisesXmlParser.parse(new StringReader(result));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return exercises;
        }


        @Override
        protected void onPostExecute(final List<Exercise> exercises) {
            mListExercisesTask = null;
            super.onPostExecute(exercises);
            if (exercises == null) {
                Toast.makeText(PracticeActivity.this, getText(R.string.exercise_not_found), Toast.LENGTH_LONG).show();
                finish();
            } else {
                if (exercises.size() <= sharedPreferences.getInt("exercise_count", 0)) {
                    finish();
                } else {
                    Exercise firstExercise = exercises.get(0);
                    if (firstExercise != null) {//if there is at least one fulfilled exercise
                        PracticeActivity.exercises = exercises;
                        setTotalExercisesCount();
                        if (firstExercise.getPractices().get(0) != null && firstExercise.getPractices().get(0).get_id() != null) {//has the user practiced this lesson already? so, its first exercise should have practices

                            //ask if they want to jump to the last exercise
                            new AlertDialog.Builder(PracticeActivity.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle(R.string.continue_from_last_exercise)
                                    .setMessage(R.string.continue_from_last_exercise_message)
                                    .setCancelable(false)
                                    .setNegativeButton(getText(R.string.no), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            SharedPreferences.Editor editor = sharedPreferences.edit();

                                            editor.putInt("exercise_count", 0);
                                            editor.commit();
                                            loadPracticeSentences();

                                        }
                                    })
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //remove exercises that contain practices
                                            List<Exercise> exercisesTemp = exercises;
                                            int i = 0;
                                            while (i < exercises.size() &&
                                                    exercises.size() > 0 &&
                                                    exercises.get(i).getPractices().size() > 0 &&
                                                    exercises.get(i).getPractices().get(0).get_id() != null) {
                                                i++;
                                            }

                                            if (exercises.size() > i) {
                                                //if its not the last exercise
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putInt("exercise_count", i);
                                                editor.commit();
                                                loadPracticeSentences();
                                            } else {
                                                PracticeActivity.exercises = null;
                                                finish();
                                                Toast.makeText(PracticeActivity.this, getString(R.string.you_completed_this_lesson), Toast.LENGTH_LONG).show();
                                            }
                                        }

                                    })
                                    .show();

                        } else {
                            loadPracticeSentences();
                        }
                    }
                }
            }
        }

        private void setTotalExercisesCount() {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("total_exercises", exercises.size());
            editor.commit();
            mProgressExercises.setMax(exercises.size());
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mListExercisesTask = null;
        }

        private void loadPracticeSentences() {
            changeCurrentExercise(sharedPreferences.getInt("exercise_count", 0));
            getScripts(current.getCurrentExercise().get_id());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (TTS != null) {
            TTS.shutdown();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)

            //If Voice recognition is successful then it returns RESULT_OK
            if (resultCode == RESULT_OK) {

                ArrayList<String> matches = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                if (!matches.isEmpty()) {
                    // If first Match contains the 'search' word
                    // Then start web search.
                    String recognizedSentence = (matches.size() > 0) ? matches.get(0) : "";
                    Boolean hit = false;
                    for (String r : matches) {
                        hit = current.getCurrentSpeechScript().getTextToCheck().toLowerCase().replaceAll("[^a-zA-Z0-9]", "")
                                .equals(r.toLowerCase().replaceAll("[^a-zA-Z0-9]", ""));
                        if (hit) {
                            recognizedSentence = r;
                            break;
                        }
                    }

                    if (hit) {
                        mProgressScripts.setProgress(mProgressScriptsStatus++);
                        ((TextView)findViewById(R.id.txt_scripts_progress)).setText(String.format(
                                getString(R.string.progress_mask),
                                current.getCurrentScriptIndex()+1,
                                current.getCurrentExercise().getScriptEntries().size()));

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PracticeActivity.this);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("correct_sentence_count", sharedPreferences.getInt("correct_sentence_count", 0) + 1);
                        editor.commit();
                        if (current.hasMoreScripts()) {
                            current.selectNextScript();
                        }

                    } else {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PracticeActivity.this);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("wrong_sentence_count", sharedPreferences.getInt("wrong_sentence_count", 0) + 1);
                        editor.commit();
                        //TODO: update number_attemps to +1 on the current execution
                        // db.updateNumberAttempts  (current.getCurrentSpeechScript().get_id(),lessonId);

                        recognizedSentence = formatWrongSentence(recognizedSentence, current.getCurrentSpeechScript().getTextToCheck());
                    }

                    updateLastSentences(recognizedSentence);
                    current.setShouldRunScript(true);
                    runScriptEntry();//user should not stop in the middle of the lesson.

                }
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String formatWrongSentence(String recognizedSentence, String strCheck) {

        String[] arSpeech = recognizedSentence.split("[ ]");
        String[] arCheck = strCheck.split("[ ]");

        String result = "";
        for (int i = 0; i < arCheck.length && i < arSpeech.length; i++) {
            if (arSpeech[i].toLowerCase().replaceAll("[^a-zA-Z0-9]", "")
                    .equals(arCheck[i].toLowerCase().replaceAll("[^a-zA-Z0-9]", ""))) {
                result += arSpeech[i] + " ";
            } else {
                result += "<b><font color='red'>" + arSpeech[i] + " </font></b>";
            }
        }

        return result;
    }

    public void updateLastSentences(String htmlSentence) {
        TextView tv1 = ((TextView) findViewById(R.id.recognizedText1));
        TextView tv2 = ((TextView) findViewById(R.id.recognizedText2));
        TextView tv3 = ((TextView) findViewById(R.id.recognizedText3));
        TextView tv4 = ((TextView) findViewById(R.id.recognizedText4));
        TextView tv5 = ((TextView) findViewById(R.id.recognizedText5));
        TextView tv6 = ((TextView) findViewById(R.id.recognizedText6));
        TextView tv7 = ((TextView) findViewById(R.id.recognizedText7));
        TextView tv8 = ((TextView) findViewById(R.id.recognizedText8));
        tv8.setText(tv7.getText());
        tv7.setText(tv6.getText());
        tv6.setText(tv5.getText());
        tv5.setText(tv4.getText());
        tv4.setText(tv3.getText());
        tv3.setText(tv2.getText());
        tv2.setText(tv1.getText());
        tv1.setText(Html.fromHtml(htmlSentence));
    }

    private class ListSpeechScriptsTask extends AsyncTask<Void, Void, List<SpeechScript>> {
        private final String url;
        HashMap hashMap;

        public ListSpeechScriptsTask(String url, HashMap<String, String> hashMap) {
            this.hashMap = hashMap;
            this.url = url;
        }

        @Override
        protected List<SpeechScript> doInBackground(Void... params) {
            List<SpeechScript> speechScripts = null;
            HTTPConnection httpConnection = new HTTPConnection();
            ScriptsXmlParser scriptsXmlParser = new ScriptsXmlParser();

            try {
                String result = httpConnection.sendPost(url, hashMap);
                speechScripts = scriptsXmlParser.parse(new StringReader(result));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return speechScripts;
        }


        @Override
        protected void onPostExecute(List<SpeechScript> scripts) {
            mListExercisesTask = null;
            super.onPostExecute(scripts);
            if (scripts == null) {
                Toast.makeText(PracticeActivity.this, getText(R.string.practices_not_found), Toast.LENGTH_LONG).show();
                finish();
            } else {
                if (scripts.get(0) == null) {
                    finish();
                } else {

                    Collections.sort(scripts);
                    int i = 0;
                    for (SpeechScript s : scripts) {
                        s.setScriptIndex(i);
                        i++;
                    }
                    if (hasMoreExercises()) {
                        exercises.get(sharedPreferences.getInt("exercise_count", 0)).setScriptEntries(scripts);

                        speech = SpeechRecognizer.createSpeechRecognizer(PracticeActivity.this);
                        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, (Locale.US).toString());
                        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, PracticeActivity.this.getPackageName());
                        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);


                        //Progress Bar
                        mProgressScripts.setMax(scripts.size());
                        mProgressScripts.setProgress(0);
                        ((TextView)findViewById(R.id.txt_scripts_progress)).setText(String.format(
                                getString(R.string.progress_mask),
                                0,
                                current.getCurrentExercise().getScriptEntries().size()));
                        selectNextExercise();
                        TTS = new TextToSpeech(PracticeActivity.this, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                startExercise();
                            }
                        });
                    }


                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mListExercisesTask = null;
        }
    }

    public void startExercise() {
        if (TTS != null) {
            TTS.setLanguage(Locale.US);
            runScriptEntry();
        }
    }

    /*Whenever tryAgain is called, the function runscriptentry is allowed because the variable shouldRunScript was changed to true*/
    public void tryAgain(View v) {
        current.setShouldRunScript(true);
        runScriptEntry();
    }

    private Boolean hasMoreExercises() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return exercises.size() > sharedPreferences.getInt("exercise_count", 0);
    }

    private void getScripts(Integer exerciseId) {
        HashMap<String, String> hashMap = new HashMap();
        hashMap.put("data[SpeechScript][exercise_id]", exerciseId.toString());


        mListSpeechScriptsTask = new ListSpeechScriptsTask(EchoPractice.SERVER_BASE_URL + "speech_scripts/index_api.xml", hashMap);
        mListSpeechScriptsTask.execute();
    }

    /*Selects the exercise to run*/
    public void selectNextExercise() {


        changeCurrentExercise(sharedPreferences.getInt("exercise_count", 0));
        if (hasMoreExercises()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("exercise_count", (sharedPreferences.getInt("exercise_count", 0) + 1));
            editor.commit();
            if (current.getCurrentExercise().getScriptEntries().size() > 0) {
                current.setCurrentScriptIndex(0);
                current.setCurrentSpeechScript(current.getCurrentExercise().getScriptEntries().get(current.getCurrentScriptIndex()));
            }
        }
    }

    private void changeCurrentExercise(Integer count) {

        Exercise exercise = exercises.get(count);
        current.setCurrentExercise(exercise);
        mProgressExercises.setProgress(count);
        ((TextView)findViewById(R.id.txt_exercises_progress)).setText(String.format(
                getString(R.string.progress_mask),
                count+1,
                exercises.size()));
    }


    private void loadExercises(Integer lessonId) {
        //retrieve sentences to practice from db for each exercise
        SessionManager sessionManager = new SessionManager(PracticeActivity.this);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("data[Exercise][lesson_id]", lessonId.toString());
        hashMap.put("data[Exercise][user_id]", sessionManager.getUserDetails().get(SessionManager.KEY_ID));

        mListExercisesTask = new ListExercisesTask(EchoPractice.SERVER_BASE_URL + "exercises/index_api.xml", hashMap);
        mListExercisesTask.execute();
    }

    private void runScriptEntry() {
        if (current.getShouldRunScript()) {

            current.setShouldRunScript(false);//prove to me again that I can execute everything ->go to the next exercise.

            if (current.getCurrentScriptIndex() < current.getCurrentExercise().getScriptEntries().size()) {
                final SpeechScript s = current.getCurrentSpeechScript();
                if (s != null) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            TextView child = new TextView(PracticeActivity.this);
                            child.setTextSize(20f);

                            LinearLayout parent = (LinearLayout) findViewById(R.id.contentFrame);
                            child.setText(s.getTextToShow());
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins(30, 20, 30, 20);
                            params.gravity = Gravity.CENTER_HORIZONTAL;
                            parent.setBackgroundColor(Color.parseColor("#AFE4E2"));
                            child.setLayoutParams(params);
                            ArrayList<TextView> items = new ArrayList<>();

                            items.add(child);
                            for (int i = parent.getChildCount() - 1; i >= 0; i--) {
                                TextView t = (TextView) parent.getChildAt(i);
                                parent.removeViewAt(i);
                            }
                            parent.addView(child);

                        }
                    });

                    //speak
                    Bundle b = new Bundle();
                    b.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, s.get_id().toString());
                    recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, s.getTextToShow());

                    switch (s.getFunctionId()) {

                        case 1://The device is to speak (tts) the text_to_read (used to give instructions about the exercises)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) { //versao api >

                                TTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                    @Override
                                    public void onStart(String utteranceId) {

                                    }

                                    @Override
                                    public void onDone(String utteranceId) {
                                        if (current.getCurrentSpeechScript().getFunctionId() == 1) {
                                            current.setShouldRunScript(true);
                                            current.selectNextScript();
                                            runScriptEntry();

                                        }
                                    }

                                    @Override
                                    public void onError(String utteranceId) {

                                    }
                                });
                            }
                            speak(s.getTextToRead(), s.get_id().toString(), b);

                            break;

                        case 2:
                            //The device is to Read text(tts), Show sentence- tts, Listen to speech, Check against database info= stt. Listen and compare.
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) { //versao api >

                                TTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                    @Override
                                    public void onStart(String utteranceId) {
                                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(10);
                                    }

                                    @Override
                                    public void onDone(String utteranceId) {
                                        promptSpeechInput();//shows mic screen
                                        //if voice recognition fails, ask again. no touching button
                                    }

                                    @Override
                                    public void onError(String utteranceId) {

                                    }
                                });
                            }
                            speak(s.getTextToRead(), s.get_id().toString(), b);
                            break;

                        case 3:
                            //only checks the speech -> do not provide any kind of model
                            // (neither spoken by the device nor on video)
//                            this method needs a little more time, for the sake of uability, to ask for the nest input. Users were getting confused about the sounds built in the Voice Recognition
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    promptSpeechInput();
                                }
                            }, TRANSITION_PAUSE);
                            break;
                        case 4:
                            //shows video and asks for audio input then checks audio
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    final VideoView v = new VideoView(PracticeActivity.this);
//                                    final LinearLayout r = (LinearLayout) findViewById(R.id.videoFrame);
//                                    r.setVisibility(View.VISIBLE);
//                                    r.addView(v);
//                                    int videoResource = getResources().getIdentifier("raw/" + s.getTextToRead(), null, getPackageName());
//
//                                    String path = "android.resource://" + getPackageName() + "/" + videoResource;
//                                    v.setVideoURI(Uri.parse(path));
//                                    v.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                                        @Override
//                                        public void onCompletion(MediaPlayer mp) {
//                                            r.removeView(v);
//                                            promptSpeechInput();
//                                        }
//                                    });
//                                    v.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                                        @Override
//                                        public void onPrepared(MediaPlayer mp) {
//
//                                            v.start();
//                                        }
//                                    });
//                                }
//                            });

                            break;
                        case 5://only shows a video containing instructions. do not ask for audio back
                            /*runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final VideoView v = new VideoView(PracticeActivity.this);
                                    final LinearLayout r = (LinearLayout) findViewById(R.id.videoFrame);
                                    r.setVisibility(View.VISIBLE);
                                    r.addView(v);
                                    int videoResource = getResources().getIdentifier("raw/" + s.getTextToRead(), null, getPackageName());

                                    String path = "android.resource://" + getPackageName() + "/" + videoResource;
                                    v.setVideoURI(Uri.parse(path));
                                    v.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mp) {
                                            r.removeView(v);
                                            current.setShouldRunScript(true);
                                            current.selectNextScript();
                                            runScriptEntry();
                                        }
                                    });
                                    v.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                        @Override
                                        public void onPrepared(MediaPlayer mp) {

                                            v.start();
                                        }
                                    });
                                }
                            });*/

                            break;
                    }

                }

            } else { //exercise completed
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

                savePractice();

                if (exercises.size() > sharedPreferences.getInt("exercise_count", 0)) {
                    mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            requestNewInterstitial();
                            makeTransition();
                        }

                    });
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {

                        makeTransition();
                    }
                } else {
                    mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            requestNewInterstitial();
                            finishLesson();
                        }

                    });
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        finishLesson();
                    }
                }
                finish();
            }
        }
    }

    private void makeTransition() {

        Intent i = new Intent(PracticeActivity.this, TransitionActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        current.setShouldRunScript(true);
    }

    private void finishLesson() {

        Intent i = new Intent(PracticeActivity.this, LessonCompletedActivity.class);
        //getting the current time in milliseconds, and creating a Date object from it:
        Date date = new Date(System.currentTimeMillis()); //or simply new Date();

        //converting it back to a milliseconds representation:
        long millis = date.getTime();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("finish_time", date.getTime());
        editor.commit();

        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void savePractice() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PracticeActivity.this);

        //save lesson completed
        long millis = sharedPreferences.getLong("start_time", 0L);
        Date startTime = new Date(millis);
        Integer totalHits = current.getCurrentExercise().getScriptEntries().size();

        //no matter what happens, if the student gets here, he is rewarded.

        DateFormat df = DateFormat.getTimeInstance();
        Date finishTime = new Date();

        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        SessionManager sessionManager = new SessionManager(PracticeActivity.this);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("data[Practice][user_id]", sessionManager.getUserDetails().get(SessionManager.KEY_ID));
        hashMap.put("data[Practice][start_time]", df.format(startTime));
        hashMap.put("data[Practice][finish_time]", df.format(finishTime));
        hashMap.put("data[Practice][points]", totalHits.toString());
        hashMap.put("data[Practice][exercise_id]", current.getCurrentExercise().get_id().toString());
        try {
            mAddPracticeTask = new AddPracticeTask(EchoPractice.SERVER_BASE_URL + "practices/add_api.xml", hashMap);
            mAddPracticeTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Showing google speech input dialog ->starts listening to audio input
     */

    private void promptSpeechInput() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //versao api >21
                    // verify if user has granted this dangerous permission
                    int permissionCheck = ContextCompat.checkSelfPermission(PracticeActivity.this,
                            Manifest.permission.RECORD_AUDIO);
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(PracticeActivity.this,
                                Manifest.permission.RECORD_AUDIO)) {

                            // Show an expanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.

                        } else {

                            // No explanation needed, we can request the permission.

                            ActivityCompat.requestPermissions(PracticeActivity.this,
                                    new String[]{Manifest.permission.RECORD_AUDIO},
                                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                            // app-defined int constant. The callback method gets the
                            // result of the request.
                        }

                    } else {
                        startActivityForResult(recognizerIntent, VOICE_RECOGNITION_REQUEST_CODE);
                    }
                } else {
                    startActivityForResult(recognizerIntent, VOICE_RECOGNITION_REQUEST_CODE);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        String name = "Practice";
        Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName("Screen~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    //  task you need to do.

                    startActivityForResult(recognizerIntent, VOICE_RECOGNITION_REQUEST_CODE);

                } else {
                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void changeDrawable(ImageButton view, String uri, Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //versao api >21
            view.setImageDrawable(context.getDrawable(id));
        } else {
            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

            Drawable res = context.getResources().getDrawable(imageResource);
            view.setImageDrawable(res);
        }
    }

    public void checkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setMessage(R.string.no_connection);
            builder.setTitle(R.string.no_connection_title);
            builder.setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    return;
                }
            });

            builder.show();
        }
    }


    private void speak(String textToSpeak, String id, Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TTS.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, bundle, id);
        } else {
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
            TTS.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //Ask the user if they want to quit
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.quit_confirm)
                    .setMessage(R.string.quit_text)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //Stop the activity
                            PracticeActivity.this.finish();
                        }

                    })
                    .setNegativeButton(R.string.no, null)
                    .show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


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
                Toast.makeText(PracticeActivity.this, getText(R.string.verify_internet_connection), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(PracticeActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            mAddPracticeTask = null;

        }
    }

    private void requestNewInterstitial() {

        AdRequest adRequest;
        if (EchoPractice.DEBUG_MODE) {
            String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceId = LessonCompletedActivity.md5(android_id).toUpperCase();
            adRequest = new AdRequest.Builder()
                    .addTestDevice(deviceId)
                    .build();

        } else {
            adRequest = new AdRequest.Builder().build();
        }


        mInterstitialAd.loadAd(adRequest);
    }

}
