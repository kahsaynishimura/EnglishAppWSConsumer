package com.karina.alicesadventures;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.karina.alicesadventures.Util.HTTPConnection;
import com.karina.alicesadventures.Util.SessionManager;
import com.karina.alicesadventures.model.Product;
import com.karina.alicesadventures.parsers.MessageXmlParser;

import java.io.StringReader;
import java.util.HashMap;

/**
 * An activity representing a single Product detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ProductListActivity}.
 */
public class ProductDetailActivity extends AppCompatActivity {
    private static final String QR_CODE =  "todo";
    private AddTradeTask mAddTradeTask;
    private Product product = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            String id =
                    getIntent().getStringExtra(ProductDetailFragment.ARG_ITEM_ID);
            if (id != null) {
                product = new Product();
                product.setId(Integer.parseInt(id));
                arguments.putString(ProductDetailFragment.ARG_ITEM_ID, product.getId().toString());
                ProductDetailFragment fragment = new ProductDetailFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.product_detail_container, fragment)
                        .commit();
            }
        }
    }

    public void trade(View v) {
        SessionManager sessionManager = new SessionManager(ProductDetailActivity.this);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("data[Trade][user_id]", sessionManager.getUserDetails().get(SessionManager.KEY_ID));
        hashMap.put("data[Trade][product_id]", product.getId().toString());
        hashMap.put("data[Trade][qr_code]",QR_CODE);//TODO
        hashMap.put("data[Trade][validated]", "0");


        //   discount points


        try {
            mAddTradeTask = new AddTradeTask("http://karinanishimura.com.br/cakephp/trades/add_api.xml", hashMap);
            mAddTradeTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class AddTradeTask extends AsyncTask<Void, Void, String> {

        private final String url;
        HashMap hashMap;

        public AddTradeTask(String url, HashMap<String, String> hashMap) {
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
                mAddTradeTask = null;

            }
            System.out.println(result);
            return message;
        }


        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            mAddTradeTask = null;
            if (message == null) {
                Snackbar.make((findViewById(R.id.fab)), getText(R.string.verify_internet_connection), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                //update user points field
                Snackbar.make((findViewById(R.id.fab)), message, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                try {
                    ((ImageView) findViewById(R.id.qr_code)).setVisibility(View.VISIBLE);
                    Bitmap bitmap = encodeAsBitmap("karinanishimura.com.br");
                    ((ImageView) findViewById(R.id.qr_code)).setImageBitmap(bitmap);

                } catch (WriterException e) {
                    e.printStackTrace();
                }

            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            mAddTradeTask = null;

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
        }}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, ProductListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
