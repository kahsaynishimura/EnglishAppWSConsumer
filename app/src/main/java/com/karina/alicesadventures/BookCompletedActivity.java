package com.karina.alicesadventures;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BookCompletedActivity extends ActionBarActivity {
    TextView txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_completed);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BookCompletedActivity.this);
        txt = (TextView) findViewById(R.id.txt_correct);
        txt.setText(getString(R.string.you_graduated_from) + " " + getBookName(sharedPreferences.getInt("book_id", 1)));
        Button btn = (Button) findViewById(R.id.exit_btn);
        btn.setText(getString(R.string.exit));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public String getBookName(Integer bookId) {

        String[] books = getResources().getStringArray(R.array.books);

        return books[bookId - 1];
    }
}
