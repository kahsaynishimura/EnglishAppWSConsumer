package com.karina.alicesadventures;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.karina.alicesadventures.Util.HTTPConnection;
import com.karina.alicesadventures.model.Book;
import com.karina.alicesadventures.model.DBHandler;
import com.karina.alicesadventures.model.User;
import com.karina.alicesadventures.parsers.BookXmlParser;
import com.karina.alicesadventures.parsers.UserXmlParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class SelectUserActivity extends ActionBarActivity {
    private LoginTask mLoginTask;

    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

      /*  new Handler().postDelayed(new Runnable() {


             // Showing splash screen with a timer. This will be useful when you
             // want to show case your app logo / company


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
                        Intent i = new Intent(SelectUserActivity.this, BookActivity.class);

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
*/
        login(null);
    }
    private final String EMAIL_KEY="data[User][username]";
    private final String PASSWORD_KEY="data[User][password]";
    public void login(View v){

        HashMap<String,String> hashMap=new HashMap<>();
        hashMap.put(EMAIL_KEY,((EditText)findViewById(R.id.txtEmail)).getText().toString());
        hashMap.put(PASSWORD_KEY,((EditText)findViewById(R.id.txtPassword)).getText().toString());

        HTTPConnection httpClient= new HTTPConnection();
        try {
            mLoginTask = new LoginTask(hashMap);
            mLoginTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class LoginTask extends AsyncTask<Void, Void, User> {

        HashMap<String,String> hashMap;
       public LoginTask (HashMap<String,String> hashMap){
           this.hashMap=hashMap;
       }
        @Override
        protected User doInBackground(Void... params) {
            User user = null;
            HTTPConnection httpConnection = new HTTPConnection();
            BookXmlParser bookXmlParser = new BookXmlParser();
            String result=
                    "";
            try {
                 result = httpConnection.sendPost("http://karinanishimura.com.br/cakephp/users/login_api.xml",hashMap);
              //  user = UserXmlParser.parse(new StringReader(result));
                //  addBooksToList(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(result);
            return user;
        }


        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            if (user == null) {
                Toast.makeText(SelectUserActivity.this, getText(R.string.verify_internet_connection), Toast.LENGTH_LONG).show();
            } else {
             //TODO: save logged in user
            }
        }
    }

}
