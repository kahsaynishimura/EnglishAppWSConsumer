package com.karina.alicesadventures.parsers;

import android.util.Xml;

import com.karina.alicesadventures.model.Book;
import com.karina.alicesadventures.model.Lesson;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karina on 2016-01-06.
 */
public class LessonsXmlParser { // We don't use namespaces
    private static final String ns = null;

    public List<Lesson> parse( StringReader in) throws XmlPullParserException, IOException {
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
    private List<Lesson> readRoot(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Lesson> lessons = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "response");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("lessons")) {
                lessons.add(readLessonsParent(parser));
            } else {
                skip(parser);
            }
        }
        return lessons;
    }
    // Parses the contents of a book. If it encounters a name and id, hands them off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private Lesson readLessonsParent(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "lessons");
        Lesson lesson = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Lesson")) {
                lesson = readLesson(parser);
            } else {
                skip(parser);
            }
        }
        return lesson;
    }

    private Lesson readLesson(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Lesson");
        String lessonName = null;
        Integer id = null;
        Integer bookId = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("name")) {
                lessonName = readText(parser);
            } else if (name.equals("id")) {
                id = Integer.parseInt(readText(parser));
            } else if (name.equals("book_id")) {
                bookId = Integer.parseInt(readText(parser));
            }  else {
                skip(parser);
            }
        }
        return new Lesson(id,lessonName,bookId);
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
