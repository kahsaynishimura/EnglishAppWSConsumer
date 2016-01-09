package com.karina.alicesadventures;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.karina.alicesadventures.Util.SessionManager;
import com.karina.alicesadventures.model.Product;
import com.karina.alicesadventures.parsers.ProductXmlParser;

/**
 * A fragment representing a single Product detail screen.
 * This fragment is either contained in a {@link ProductListActivity}
 * in two-pane mode (on tablets) or a {@link ProductDetailActivity}
 * on handsets.
 */
public class ProductDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Product mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProductDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            String key = getArguments().getString(ARG_ITEM_ID);
            mItem = ProductXmlParser.ITEM_MAP.get(key);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.getName() + " (" + mItem.getPoints_value() + ")");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.product_detail, container, false);
        SessionManager sessionManager = new SessionManager(getActivity());
        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.product_detail)).setText(mItem.getDescription());
            ((TextView) rootView.findViewById(R.id.tvPoints)).setText(getText(R.string.you_have) + " " + sessionManager.getUserDetails().get(SessionManager.KEY_TOTAL_POINTS) + " " + getText(R.string.points));
            try {
                Bitmap bitmap = encodeAsBitmap("karinanishimura.com.br");
                ((ImageView) rootView.findViewById(R.id.qr_code)).setImageBitmap(bitmap);

            } catch (WriterException e) {
                e.printStackTrace();
            }
            //  ((ImageView) rootView.findViewById(R.id.qr_code)).changeDrawable((ImageView) rootView.findViewById(R.id.qr_code));
            //   ((TextView) rootView.findViewById(R.id.product_detail)).setText(mItem.getDesption());
        }

        return rootView;
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
