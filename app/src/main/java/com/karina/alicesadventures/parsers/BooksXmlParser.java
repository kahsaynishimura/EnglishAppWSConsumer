package com.karina.alicesadventures.parsers;

import android.util.Xml;

import com.karina.alicesadventures.model.Book;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karina on 2016-01-06.
 */
public class BooksXmlParser { // We don't use namespaces
    private static final String ns = null;

    public List parse( StringReader in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in);
            parser.nextTag();
            return readBooks(parser);
        } finally {
            in.close();
        }
    }
    private List<Book> readBooks(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Book> entries = new ArrayList<Book>();

        parser.require(XmlPullParser.START_TAG, ns, "response");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("books")) {
                entries.add(readBooksParent(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }
    // Parses the contents of a book. If it encounters a name and id, hands them off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private Book readBooksParent(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "books");
        Book book = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Book")) {
                book = readBook(parser);
            } else {
                skip(parser);
            }
        }
        return book;
    }

    private Book readBook(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Book");
        String bookName = null;
        Integer id = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("name")) {
                bookName = readText(parser);
            } else if (name.equals("id")) {
                id = Integer.parseInt(readText(parser));
            }  else {
                skip(parser);
            }
        }
        return new Book(id, bookName);
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
