package com.karina.alicesadventures.parsers;

import android.util.Xml;

import com.karina.alicesadventures.model.Exercise;
import com.karina.alicesadventures.model.SpeechScript;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karina on 2016-01-06.
 */
public class ScriptsXmlParser { // We don't use namespaces
    private static final String ns = null;

    public List<SpeechScript> parse(StringReader in) throws XmlPullParserException, IOException {
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

    private List<SpeechScript> readRoot(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<SpeechScript> speechScripts = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "response");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("speech_scripts")) {
                speechScripts.add(readSpeechScriptsParent(parser));
            } else {
                skip(parser);
            }
        }
        return speechScripts;
    }

    // Parses the contents of a book. If it encounters a name and id, hands them off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private SpeechScript readSpeechScriptsParent(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "speech_scripts");
        SpeechScript speechScript = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("SpeechScript")) {
                speechScript = readSpeechScript(parser);
            } else {
                skip(parser);
            }
        }
        return speechScript;
    }

    private SpeechScript readSpeechScript(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "SpeechScript");
        String text_to_read = null;
        String text_to_check = null;
        String text_to_show = null;
        Integer id = null;
        Integer speech_function_id = null;
        Integer exerciseId = null;
        Integer script_index = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("id")) {
                id = Integer.parseInt(readText(parser));
            } else if (name.equals("speech_function_id")) {
                speech_function_id = Integer.parseInt(readText(parser));
            } else if (name.equals("exerciseId")) {
                exerciseId = Integer.parseInt(readText(parser));
            } else if (name.equals("script_index")) {
                script_index = Integer.parseInt(readText(parser));
            } else  if (name.equals("text_to_read")) {
                text_to_read = readText(parser);
            } else  if (name.equals("text_to_check")) {
                text_to_check = readText(parser);
            } else  if (name.equals("text_to_show")) {
                text_to_show = readText(parser);
            } else {
                skip(parser);
            }
        }
        Exercise e=new Exercise();
        e.set_id(exerciseId);
        return new SpeechScript(id, text_to_show,text_to_read,text_to_check,script_index,speech_function_id,e);
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
