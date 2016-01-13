package com.karina.alicesadventures.parsers;

import android.util.Xml;

import com.karina.alicesadventures.model.Partner;
import com.karina.alicesadventures.model.Product;
import com.karina.alicesadventures.model.Trade;
import com.karina.alicesadventures.model.User;

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
public class TradesXmlParser { // We don't use namespaces
    private static final String ns = null;

    public List<Trade> parse(StringReader in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in);
            parser.nextTag();
            return readTrades(parser);
        } finally {
            in.close();
        }
    }

    private List<Trade> readTrades(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Trade> trades = new ArrayList<Trade>();

        parser.require(XmlPullParser.START_TAG, ns, "response");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("trades")) {
                trades.add(readTradesParent(parser));
            } else {
                skip(parser);
            }
        }

        return trades;
    }

    private Trade readTradesParent(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "trades");
        Trade trade = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Trade")) {
                trade = readTrade(parser);
            } else if (name.equals("Product")) {
               trade.setProduct(readProduct(parser));
            } else {
                skip(parser);
            }
        }

        return trade;
    }

    private Trade readTrade(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Trade");
        Integer id = null;
        String qrCode = null;
        Integer validated = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("qr_code")) {
                qrCode = readText(parser);
            } else if (name.equals("validated")) {
                validated = Integer.parseInt(readText(parser));
            }  else if (name.equals("id")) {
                id = Integer.parseInt(readText(parser));
            } else if (name.equals("id")) {
                id = Integer.parseInt(readText(parser));
            } else {
                skip(parser);
            }
        }
        return new Trade(id, qrCode, validated);
    }

    private Product readProduct(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Product");
        String productName = null;
        Integer id = null;
        Partner partner = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("name")) {
                productName = readText(parser);
            } else if (name.equals("id")) {
                id = Integer.parseInt(readText(parser));
            } else if (name.equals("Partner")) {
                partner = readPartner(parser);
            } else {
                skip(parser);
            }
        }
        return new Product(id, productName, partner);
    }

    private Partner readPartner(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Partner");
        String companyName = null;
        String address = null;
        String phone = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("name")) {
                companyName = readText(parser);
            } else if (name.equals("address")) {
                address = readText(parser);
            } else if (name.equals("phone")) {
                phone = readText(parser);
            } else {
                skip(parser);
            }
        }
        return new Partner(companyName, address, phone);
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
