package com.karina.alicesadventures;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.karina.alicesadventures.Util.HTTPConnection;
import com.karina.alicesadventures.Util.IntentIntegrator;
import com.karina.alicesadventures.Util.IntentResult;
import com.karina.alicesadventures.Util.SessionManager;
import com.karina.alicesadventures.model.Book;
import com.karina.alicesadventures.model.GeneralResponse;
import com.karina.alicesadventures.parsers.BookXmlParser;
import com.karina.alicesadventures.parsers.GeneralResponseXmlParser;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

public class BookActivity extends ActionBarActivity {

    private ListBooksTask mListBooksTask;
    private ValidateCodeTask mValidateCodeTask;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        sessionManager = new SessionManager(BookActivity.this);
        sessionManager.checkLogin();
        mListBooksTask = new ListBooksTask();
        mListBooksTask.execute("http://www.karinanishimura.com.br/cakephp/books/index_api.xml");
    }

    private class ListBooksTask extends AsyncTask<String, Void, List<Book>> {
        @Override
        protected List<Book> doInBackground(String... params) {
            List<Book> books = null;
            HTTPConnection httpConnection = new HTTPConnection();
            BookXmlParser bookXmlParser = new BookXmlParser();

            try {
                String result = httpConnection.sendGet(params[0]);
                books = bookXmlParser.parse(new StringReader(result));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return books;
        }


        @Override
        protected void onPostExecute(List<Book> books) {
            mListBooksTask = null;
            super.onPostExecute(books);
            if (books == null) {
                Toast.makeText(BookActivity.this, getText(R.string.verify_internet_connection), Toast.LENGTH_LONG).show();
            } else {
                ArrayAdapter<Book> a =
                        new ArrayAdapter<Book>(BookActivity.this, android.R.layout.simple_list_item_1, books);

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
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book, menu);

        if (!"partner".equals(sessionManager.getUserDetails().get(
                SessionManager.KEY_ROLE))) {
            menu.removeItem(R.id.action_validate_code);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            sessionManager.logoutUser();
        } else if (id == R.id.action_validate_code) {
            IntentIntegrator scanIntegrator = new IntentIntegrator(BookActivity.this);
            scanIntegrator.initiateScan();
        } else if (id == R.id.action_view_prizes) {
            Intent i = new Intent(BookActivity.this, PrizesActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            if (scanFormat.equals("QR_CODE")) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("data[Trade][qr_code]", scanContent);

                try {
                    mValidateCodeTask = new ValidateCodeTask("http://karinanishimura.com.br/cakephp/trades/validateQR.xml", hashMap);
                    mValidateCodeTask.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    private class ValidateCodeTask extends AsyncTask<Void, Void, GeneralResponse> {

        private final String url;
        HashMap hashMap;

        public ValidateCodeTask(String url, HashMap<String, String> hashMap) {
            this.hashMap = hashMap;
            this.url = url;
        }

        @Override
        protected GeneralResponse doInBackground(Void... params) {
            GeneralResponse generalResponse = null;
            HTTPConnection httpConnection = new HTTPConnection();
            GeneralResponseXmlParser generalXmlParser = new GeneralResponseXmlParser();
            String result = "";
            try {
                result = httpConnection.sendPost(url, hashMap);
                generalResponse = generalXmlParser.parse(new StringReader(result));
            } catch (Exception e) {
                e.printStackTrace();
                mValidateCodeTask = null;

            }
            System.out.println(result);
            return generalResponse;
        }


        @Override
        protected void onPostExecute(GeneralResponse response) {
            super.onPostExecute(response);
            mValidateCodeTask = null;
            if (response == null) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        getText(R.string.verify_internet_connection), Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast.makeText(getApplicationContext(),
                        response.getMessage(), Toast.LENGTH_SHORT).show();

                if (response.getStatus().equals("success")) {

                    Intent intent = new Intent(BookActivity.this, ProductDetailActivity.class);
                    intent.putExtra(ProductDetailFragment.ARG_ITEM_ID, response.getData());

                    startActivity(intent);

                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            mValidateCodeTask = null;

        }

        Bitmap encodeAsBitmap(String str) throws WriterException {
            BitMatrix result;

            int WHITE = 0xFFFFFFFF;
            int BLACK = 0xFF000000;
            int WIDTH = 150;
            int HEIGHT = 150;
            try {
                result = new MultiFormatWriter().encode(str,
                        BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);
            } catch (IllegalArgumentException iae) {
                // Unsupported format
                return null;
            }
            int w = result.getWidth();
            int h = result.getHeight();
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                int offset = y * w;
                for (int x = 0; x < w; x++) {
                    pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, WIDTH, 0, 0, w, h);
            return bitmap;
        }
    }

}
