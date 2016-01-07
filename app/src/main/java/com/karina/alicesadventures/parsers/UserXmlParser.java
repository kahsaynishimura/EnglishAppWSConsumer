package com.karina.alicesadventures.parsers;

import android.util.Xml;

import com.karina.alicesadventures.model.Book;
import com.karina.alicesadventures.model.User;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karina on 2016-01-06.
 */
public class UserXmlParser { // We don't use namespaces
    private static final String ns = null;

    public User parse(StringReader in) throws XmlPullParserException, IOException {
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

    private User readRoot(XmlPullParser parser) throws XmlPullParserException, IOException {
        User user = null;

        parser.require(XmlPullParser.START_TAG, ns, "response");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("user")) {
                user = readUser(parser);
            } else {
                skip(parser);
            }
        }
        return user;
    }


    private User readUser(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "user");
        Integer id = null;
        String name = null;
        String role = null;
        String username = null;
        Integer lastCompletedExercise = null;
        int countFields = 0;
        while (parser.next() != XmlPullParser.END_TAG) {
            countFields++;
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            if (tagName.equals("id")) {
                id = Integer.parseInt(readText(parser));
            } else if (tagName.equals("name")) {
                name = readText(parser);
            } else if (tagName.equals("role")) {
                role = readText(parser);
            } else if (tagName.equals("username")) {
                username = readText(parser);
            } else if (tagName.equals("last_completed_lesson")) {
                lastCompletedExercise = Integer.parseInt(readText(parser));
            } else {
                skip(parser);
            }
        }
        if (countFields <= 0) {
            return null;
        }
        return new User(id, name, role, username, lastCompletedExercise);
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
