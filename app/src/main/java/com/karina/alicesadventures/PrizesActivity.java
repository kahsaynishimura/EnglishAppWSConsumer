package com.karina.alicesadventures;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.karina.alicesadventures.util.AnalyticsApplication;
import com.karina.alicesadventures.util.HTTPConnection;
import com.karina.alicesadventures.util.SessionManager;
import com.karina.alicesadventures.model.Trade;
import com.karina.alicesadventures.parsers.TradesXmlParser;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

public class PrizesActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ListTradesTask mListTradesTask;
    private Tracker mTracker;
    private static final String TAG = "PrizesActivity";
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prizes);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SessionManager sessionManager = new SessionManager(PrizesActivity.this);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("data[Trade][user_id]", sessionManager.getUserDetails().get(SessionManager.KEY_ID));

        mListTradesTask = new ListTradesTask("http://karinanishimura.com.br/cakephp/trades/index_api.xml", hashMap);
        mListTradesTask.execute();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
    }

    private class ListTradesTask extends AsyncTask<String, Void, List<Trade>> {
        private final String url;
        HashMap hashMap;

        public ListTradesTask(String url, HashMap<String, String> hashMap) {
            this.hashMap = hashMap;
            this.url = url;
        }

        @Override
        protected List<Trade> doInBackground(String... params) {
            List<Trade> trades = null;
            HTTPConnection httpConnection = new HTTPConnection();
            TradesXmlParser tradesXmlParser = new TradesXmlParser();

            try {
                String result = httpConnection.sendPost(url, hashMap);
                trades = tradesXmlParser.parse(new StringReader(result));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return trades;
        }

        @Override
        protected void onPostExecute(List<Trade> trades) {
            mListTradesTask = null;
            super.onPostExecute(trades);
            if (trades == null) {
                Toast.makeText(PrizesActivity.this, getText(R.string.verify_internet_connection), Toast.LENGTH_LONG).show();
            } else if (trades.get(0) == null) {
                Toast.makeText(PrizesActivity.this, getText(R.string.no_prizes), Toast.LENGTH_LONG).show();
                finish();
            } else {
                mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), trades);

                // Set up the ViewPager with the sections adapter.
                mViewPager = (ViewPager) findViewById(R.id.container);
                mViewPager.setAdapter(mSectionsPagerAdapter);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String name = "Prizes";
        Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName("Screen~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_PRODUCT_NAME = "product_name";
        private static final String ARG_PARTNER_NAME = "partner_name";
        private static final String ARG_PARTNER_ADDRESS = "partner_address";
        private static final String ARG_PARTNER_PHONE = "partner_phone";
        private static final String ARG_QR_CODE = "qr_code";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(Trade trade) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(ARG_PRODUCT_NAME, trade.getProduct().getName());
            args.putString(ARG_PARTNER_NAME, trade.getProduct().getPartner().getCompanyName());
            args.putString(ARG_PARTNER_ADDRESS, trade.getProduct().getPartner().getAddress());
            args.putString(ARG_PARTNER_PHONE, trade.getProduct().getPartner().getPhone());
            args.putString(ARG_QR_CODE, trade.getQr_code());
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_prizes, container, false);
            TextView productName = (TextView) rootView.findViewById(R.id.product_name);
            productName.setText(getString(R.string.product_name, getArguments().getString(ARG_PRODUCT_NAME)));
            TextView partnerName = (TextView) rootView.findViewById(R.id.partner_name);
            partnerName.setText(getString(R.string.partner_name, getArguments().getString(ARG_PARTNER_NAME)));
            TextView partnerAddress = (TextView) rootView.findViewById(R.id.partner_address);
            partnerAddress.setText(getString(R.string.partner_address, getArguments().getString(ARG_PARTNER_ADDRESS)));
            TextView partnerPhone = (TextView) rootView.findViewById(R.id.partner_phone);
            partnerPhone.setText(getString(R.string.partner_phone, getArguments().getString(ARG_PARTNER_PHONE)));
            try {
                Bitmap bitmap = encodeAsBitmap(getArguments().getString(ARG_QR_CODE));
                ((ImageView) rootView.findViewById(R.id.qr_code)).setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }

            return rootView;
        }

        static Bitmap encodeAsBitmap(String str) throws WriterException {
            BitMatrix result;

            int WHITE = 0xFFFFFFFF;
            int BLACK = 0xFF000000;
            int WIDTH = 250;
            int HEIGHT = 250;
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


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        List items;

        public SectionsPagerAdapter(FragmentManager fm, List items) {
            super(fm);
            this.items = items;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance((Trade) items.get(position));
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return items.size();
        }

    }
}
