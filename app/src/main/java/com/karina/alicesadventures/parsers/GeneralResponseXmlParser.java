package com.karina.alicesadventures.parsers;

import android.util.Xml;

import com.karina.alicesadventures.model.GeneralResponse;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by karina on 2016-01-07.
 */
public class GeneralResponseXmlParser {

    private static final String ns = null;

    public GeneralResponse parse(StringReader in) throws XmlPullParserException, IOException {
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

    private GeneralResponse readRoot(XmlPullParser parser) throws XmlPullParserException, IOException {
        GeneralResponse response = null;

        parser.require(XmlPullParser.START_TAG, ns, "response");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("general_response")) {
                response = readGeneralResponse(parser);
            } else {
                skip(parser);
            }
        }
        return response;
    }

    private GeneralResponse readGeneralResponse(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "general_response");
        String data = null;
        String status = null;
        String message = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            if (tagName.equals("data")) {
                data = readText(parser);
            } else if (tagName.equals("status")) {
                status = readText(parser);
            } else if (tagName.equals("message")) {
                message = readText(parser);
            } else {
                skip(parser);
            }
        }
        return new GeneralResponse(data, status, message);
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
