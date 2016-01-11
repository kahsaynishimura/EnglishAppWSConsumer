package com.karina.alicesadventures;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.karina.alicesadventures.Util.HTTPConnection;
import com.karina.alicesadventures.Util.SessionManager;
import com.karina.alicesadventures.model.GeneralResponse;
import com.karina.alicesadventures.model.Product;
import com.karina.alicesadventures.model.User;
import com.karina.alicesadventures.parsers.GeneralResponseXmlParser;
import com.karina.alicesadventures.parsers.ProductXmlParser;
import com.karina.alicesadventures.parsers.ProductsXmlParser;
import com.karina.alicesadventures.parsers.UserXmlParser;

import java.io.StringReader;
import java.util.HashMap;

/**
 * An activity representing a single Product detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ProductListActivity}.
 */
public class ProductDetailActivity extends AppCompatActivity {
    private AddTradeTask mAddTradeTask;
    private ViewProductTask mViewProductTask;

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

                if (ProductsXmlParser.ITEM_MAP.get(id) == null) {
                    try {
                        mViewProductTask = new ViewProductTask("http://karinanishimura.com.br/cakephp/products/view_api/" + id + ".xml", null);
                        mViewProductTask.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    arguments.putString(ProductDetailFragment.ARG_ITEM_ID, id);
                    ProductDetailFragment fragment = new ProductDetailFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.product_detail_container, fragment)
                            .commit();
                }
            }
        }
    }

    public void trade(View v) {
        SessionManager sessionManager = new SessionManager(ProductDetailActivity.this);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("data[Trade][user_id]", sessionManager.getUserDetails().get(SessionManager.KEY_ID));
        hashMap.put("data[Trade][product_id]", product.getId().toString());

        try {
            mAddTradeTask = new AddTradeTask("http://karinanishimura.com.br/cakephp/trades/add_api.xml", hashMap);
            mAddTradeTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class ViewProductTask extends AsyncTask<Void, Void, Product> {

        private final String url;
        HashMap hashMap;

        public ViewProductTask(String url, HashMap<String, String> hashMap) {
            this.hashMap = hashMap;
            this.url = url;
        }

        @Override
        protected Product doInBackground(Void... params) {
            Product product = null;
            HTTPConnection httpConnection = new HTTPConnection();
            ProductXmlParser productXmlParser = new ProductXmlParser();
            String result = "";
            try {
                result = httpConnection.sendGet(url);
                product = productXmlParser.parse(new StringReader(result));
            } catch (Exception e) {
                e.printStackTrace();
                mViewProductTask = null;
            }
            System.out.println(result);
            return product;
        }


        @Override
        protected void onPostExecute(Product product) {
            super.onPostExecute(product);
            mViewProductTask = null;
            if (product != null) {
               // ProductsXmlParser.ITEM_MAP.put(product.getId().toString(), product);
                Bundle arguments = new Bundle();
                arguments.putString(ProductDetailFragment.ARG_ITEM_ID, product.getId().toString());
                ProductDetailFragment fragment = new ProductDetailFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.product_detail_container, fragment)
                        .commit();

            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            mViewProductTask = null;

        }
    }

    private class AddTradeTask extends AsyncTask<Void, Void, GeneralResponse> {

        private final String url;
        HashMap hashMap;

        public AddTradeTask(String url, HashMap<String, String> hashMap) {
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
                mAddTradeTask = null;

            }
            System.out.println(result);
            return generalResponse;
        }


        @Override
        protected void onPostExecute(GeneralResponse response) {
            super.onPostExecute(response);
            mAddTradeTask = null;
            if (response == null) {
                Snackbar.make((findViewById(R.id.fab)), getText(R.string.verify_internet_connection), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                Snackbar.make((findViewById(R.id.fab)), response.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if (response.getStatus().equals("success")) {

                    try {
                        ((ImageView) findViewById(R.id.qr_code)).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.qr_code_instructions)).setVisibility(View.VISIBLE);
                        ((Button) findViewById(R.id.btn_trade)).setVisibility(View.GONE);
                        ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.GONE);

                        Bitmap bitmap = encodeAsBitmap(response.getData());
                        ((ImageView) findViewById(R.id.qr_code)).setImageBitmap(bitmap);

                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    //update screen values
                    Product mProduct = ProductsXmlParser.ITEM_MAP.get(product.getId().toString());
                    mProduct.setQuantity_available(mProduct.getQuantity_available() - 1);
                    ProductsXmlParser.ITEM_MAP.put(product.getId().toString(), mProduct);
                    CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
                    if (appBarLayout != null) {
                        appBarLayout.setTitle(mProduct.getName() + " (" + mProduct.getQuantity_available() + ")");
                    }
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
        }
    }

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
