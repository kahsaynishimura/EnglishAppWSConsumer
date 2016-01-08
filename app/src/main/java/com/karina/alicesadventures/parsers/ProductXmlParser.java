package com.karina.alicesadventures.parsers;

import android.util.Xml;

import com.karina.alicesadventures.model.Partner;
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
    public static final  Map<String, Product> ITEM_MAP = new HashMap<>();
    private static final String ns = null;

    public List parse(StringReader in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in);
            parser.nextTag();
            return readProducts(parser);
        } finally {
            in.close();
        }
    }

    private List<Product> readProducts(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Product> entries = new ArrayList<Product>();

        parser.require(XmlPullParser.START_TAG, ns, "response");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("products")) {

                entries.add(readProductsParent(parser));
            } else {
                skip(parser);
            }
        }
        for(Product item:entries){

            ITEM_MAP.put(item.getId().toString(), item);
        }
        return entries;
    }

    // Parses the contents of a product. If it encounters a name and id, hands them off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private Product readProductsParent(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "products");
        Product product = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Product")) {
                product = readProduct(parser);
            } else {
                skip(parser);
            }
        }
        return product;
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
            } else if (name.equals("payment_status")) {
                payment_status = Integer.parseInt(readText(parser));
            } else if (name.equals("thumb")) {
                thumb = readText(parser);
            } else if (name.equals("created")) {
                created = readText(parser);
            } else if (name.equals("modified")) {
                modified = readText(parser);
            } else {
                skip(parser);
            }
        }
        return new Product(id, productName, description, quantity_available,  points_value, payment_status,thumb, created, modified);
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
