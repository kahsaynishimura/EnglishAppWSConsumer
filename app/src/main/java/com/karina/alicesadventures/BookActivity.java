package com.karina.alicesadventures;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.karina.alicesadventures.Util.HTTPConnection;
import com.karina.alicesadventures.model.Book;
import com.karina.alicesadventures.parsers.BookXmlParser;

import java.io.StringReader;
import java.util.List;

public class BookActivity extends ActionBarActivity {

    private ListBooksTask mListBooksTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        loadComponents();
    }

    private void loadComponents() {
        mListBooksTask = new ListBooksTask();
        mListBooksTask.execute("http://www.karinanishimura.com.br/cakephp/books/index_api.xml");
        String[] books = getResources().getStringArray(R.array.books);
        ArrayAdapter<String> a =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, books);

        ListView myBooks = (ListView) findViewById(R.id.books);
        myBooks.setAdapter(a);

        myBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(BookActivity.this, LessonActivity.class);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BookActivity.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putInt("book_id", ++position);//be careful with this fixed id for books. it presumes that in the database the book id is in orther
                editor.commit();
                startActivity(i);
            }
        });
    }
    List<Book> books = null;
    private class ListBooksTask extends AsyncTask<String, Void, List<Book>> {
        @Override
        protected List<Book> doInBackground(String... params) {
            HTTPConnection httpConnection = new HTTPConnection();
            BookXmlParser bookXmlParser = new BookXmlParser();

            try {
                String result = httpConnection.sendGet(params[0]);
                books = bookXmlParser.parse(new StringReader(result));
                //  addBooksToList(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return books;
        }



        @Override
        protected void onPostExecute(List<Book> books) {
            super.onPostExecute(books);
            Toast.makeText(BookActivity.this, "onpostexecute", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
