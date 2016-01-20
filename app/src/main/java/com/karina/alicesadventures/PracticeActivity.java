package com.karina.alicesadventures;

import android.Manifest;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.karina.alicesadventures.util.AnalyticsApplication;
import com.karina.alicesadventures.util.HTTPConnection;
import com.karina.alicesadventures.model.CurrentPracticeData;
import com.karina.alicesadventures.model.Exercise;
import com.karina.alicesadventures.model.SpeechScript;
import com.karina.alicesadventures.parsers.ExercisesXmlParser;
import com.karina.alicesadventures.parsers.ScriptsXmlParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PracticeActivity extends AppCompatActivity {

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

    CurrentPracticeData current;//stores current screen info - current screen state
    public static List<Exercise> exercises = new ArrayList<Exercise>();
    private TextToSpeech TTS;
    public final long INSTRUCTION_PAUSE = 1000;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        checkConnection();
        current = new CurrentPracticeData();
        Integer lessonId = sharedPreferences.getInt("lesson_id", 0);
        if (exercises.size() == 0) {//it is the first exercise
            loadExercises(lessonId);
        } else {
            if (hasMoreExercises()) {
                current.setCurrentExercise(exercises.get(sharedPreferences.getInt("exercise_count", 0)));
                getScripts(current.getCurrentExercise().get_id());
            }
        }
        setContentView(R.layout.activity_practice);
        AdView mAdView = (AdView) findViewById(R.id.ad_view);

        AdRequest.Builder b = new AdRequest.Builder();

//        String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
//        String deviceId = LessonCompletedActivity.md5(android_id).toUpperCase();
//        b.addTestDevice(deviceId);

        AdRequest adRequest = b.build();
        b.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        mAdView.loadAd(adRequest);


        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
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
        protected void onPostExecute(List<Exercise> exercises) {
            mListExercisesTask = null;
            super.onPostExecute(exercises);
            if (exercises == null) {
                Toast.makeText(PracticeActivity.this, getText(R.string.exercise_not_found), Toast.LENGTH_LONG).show();
                finish();
            } else {
                if (exercises.size() <= sharedPreferences.getInt("exercise_count", 0)) {
                    finish();
                } else {
                    if (exercises.get(0) != null) {
                        PracticeActivity.exercises = exercises;
                        current.setCurrentExercise(exercises.get(sharedPreferences.getInt("exercise_count", 0)));

                        getScripts(current.getCurrentExercise().get_id());
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
                        //TODO: update number_attemps to +1 on the current execution
                        // db.updateNumberAttempts  (current.getCurrentSpeechScript().get_id(),lessonId);
                        editor.commit();
                    }
                    updateLastSentences(recognizedSentence);
                    current.setShouldRunScript(true);
                    runScriptEntry();//user should not stop in the middle of the lesson.

                }
                //Result code for various error.
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void updateLastSentences(String sentence) {
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
        tv1.setText(sentence);
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
                    exercises.get(sharedPreferences.getInt("exercise_count", 0)).setScriptEntries(scripts);

                    speech = SpeechRecognizer.createSpeechRecognizer(PracticeActivity.this);
                    recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, (Locale.US).toString());
                    recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, PracticeActivity.this.getPackageName());
                    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);


                    if (hasMoreExercises()) {
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
        TTS.setLanguage(Locale.US);
        runScriptEntry();
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
        mListSpeechScriptsTask = new ListSpeechScriptsTask("http://karinanishimura.com.br/cakephp/speech_scripts/index_api.xml", hashMap);
        mListSpeechScriptsTask.execute();
    }

    /*Selects the exercise to run*/
    public void selectNextExercise() {


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        current.setCurrentExercise(exercises.get(sharedPreferences.getInt("exercise_count", 0)));

        if (hasMoreExercises()) {

            editor.putInt("exercise_count", (sharedPreferences.getInt("exercise_count", 0) + 1));

            if (current.getCurrentExercise().getScriptEntries().size() > 0) {
                current.setCurrentScriptIndex(0);
                current.setCurrentSpeechScript(current.getCurrentExercise().getScriptEntries().get(current.getCurrentScriptIndex()));
            }
        }

        editor.commit();
    }


    private void loadExercises(Integer lessonId) {
        //retrieve sentences to practice from db for each exercise
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("data[Exercise][lesson_id]", lessonId.toString());

        mListExercisesTask = new ListExercisesTask("http://www.karinanishimura.com.br/cakephp/exercises/index_api.xml", hashMap);
        mListExercisesTask.execute();
    }

    private void runScriptEntry() {
        //gravar - start_time na tabela de user_script
        //
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
                            speak(s.getTextToRead(), s.get_id().toString(), b);

                            break;

                        case 2:
                            //The device is to Read text(tts), Show sentence- tts, Listen to speech, Check against database info= stt. Listen and compare.

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
                            runOnUiThread(new Runnable() {
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
                                            promptSpeechInput();
                                        }
                                    });
                                    v.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                        @Override
                                        public void onPrepared(MediaPlayer mp) {

                                            v.start();
                                        }
                                    });
                                }
                            });

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


                if (exercises.size() > sharedPreferences.getInt("exercise_count", 0)) {
                    Intent i = new Intent(PracticeActivity.this, TransitionActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    current.setShouldRunScript(true);
                } else {
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
                finish();
            }
        }
    }

    /**
     * Showing google speech input dialog ->starts listening to audio input
     */

    private void promptSpeechInput() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { //versao api >21
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (TTS != null) {
            TTS.shutdown();
        }
    }

    private void changeDrawable(ImageButton view, String uri, Context context, int id) {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) { //versao api >21
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
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
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
}
