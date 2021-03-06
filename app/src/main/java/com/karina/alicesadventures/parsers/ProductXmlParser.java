package com.karina.alicesadventures.parsers;

import android.util.Xml;

import com.karina.alicesadventures.model.Product;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by karina on 2016-01-06.
 */
public class ProductXmlParser { // We don't use namespaces
    private static final String ns = null;

    public Product parse(StringReader in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in);
            parser.nextTag();
            return readRoot(parser);
        } finally {
            in.close();
        }
    }

    private Product readRoot(XmlPullParser parser) throws XmlPullParserException, IOException {
      Product product = new Product();

        parser.require(XmlPullParser.START_TAG, ns, "response");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("product")) {

                product=readProductsParent(parser);
            } else {
                skip(parser);
            }
        }
           ProductsXmlParser. ITEM_MAP.put(product.getId().toString(), product);

        return product;
    }

    // Parses the contents of a product. If it encounters a name and id, hands them off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private Product readProductsParent(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "product");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Product")) {
               return readProduct(parser);
            } else {
                skip(parser);
            }
        }
        return null;
    }

    private Product readProduct(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Product");
        Integer id = null;
        String productName = null;
        String description = null;
        Integer payment_status = null;
        Integer points_value = null;
        Integer quantity_available = null;
        String thumb = null;
        String created = null;
        String modified = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("name")) {
                productName = readText(parser);
            } else if (name.equals("id")) {
                id = Integer.parseInt(readText(parser));
            } else if (name.equals("description")) {
                description = readText(parser);
            } else if (name.equals("quantity_available")) {
                quantity_available = Integer.parseInt(readText(parser));
            } else if (name.equals("points_value")) {
                points_value = Integer.parseInt(readText(parser));
            }  else {
                skip(parser);
            }
        }
        return new Product(id, productName, description, quantity_available,  points_value);
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }


}
