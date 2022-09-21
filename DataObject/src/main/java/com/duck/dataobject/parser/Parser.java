package com.duck.dataobject.parser;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.duck.dataobject.DataObject;
import com.duck.dataobject.exception.ParsingException;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Bradley Duck on 2018/05/13.
 */
public class Parser {
    private static final String LOGTAG = "Parser LOG";
    private static final int XML = 0, JSON = 1, HTML = 3, UNKNOWN = -1;
    static boolean ignoreAttributes = false, verbose = false;

    /**
     * Parses the given XML or JSON String into the given {@link DataObject}.
     *
     * @param data       The XML or JSON String to parse
     * @param dataObject The {@link DataObject} to parse into.
     * @param args       Optional, Argument flags: index 1: IgnoreAttributes, index 2: Verbose Logging.
     * @throws ParsingException if the given XML or JSON is malformed and invalid.
     */
    public static void parse(@NonNull String data, @NonNull DataObject dataObject, boolean... args) throws ParsingException {
        processArgs(args);
        parse(data, dataObject);
    }

    /**
     * Sets the argument flags.
     *
     * @param args the arguments: index 1 = Ignore Attributes, index 2 = verbose logging.
     */
    private static void processArgs(boolean... args) {
        if (args != null) {
            if (args.length >= 1) {
                ignoreAttributes = args[0];
            }
            if (args.length >= 2) {
                verbose = args[1];
            }
        }
    }

    private static void parse(@NonNull String data, @NonNull DataObject dataObject) throws ParsingException {
        if (data != null && !data.isEmpty() && data.length() >= 4) {
            switch (looksLike(data)) {
                case XML:
                    XMLParser.parse(data, dataObject);
                    break;
                case JSON:
                    JSONParser.parse(data, dataObject);
                    break;
                case HTML:
                    throw new ParsingException("Can not parse HTML data=\"" + data + "\"");
                case UNKNOWN:
                    throw new ParsingException("Data Type unknown, data=\"" + data + "\"");
            }
        }
    }

    private static @parseType
    int looksLike(String data) {
        int looksLike;
        if (looksLikeHTML(data)) {
            looksLike = HTML;
        } else if (looksLikeXML(data)) {
            looksLike = XML;
        } else if (looksLikeJSON(data)) {
            looksLike = JSON;
        } else {
            looksLike = UNKNOWN;
        }
        return looksLike;
    }

    private static boolean looksLikeHTML(String data) {
        boolean looksLikeHTML = false;
        if (!data.isEmpty() && data.toUpperCase().contains("<!DOCTYPE HTML")) {
            looksLikeHTML = true;
        }
        return looksLikeHTML;
    }

    private static boolean looksLikeXML(String data) {
        boolean looksLikeXML = false;
        if (data.startsWith("<") || (data.contains("<") && !data.contains("{"))) {
            looksLikeXML = true;
        } else if (data.contains("<") && data.contains("{")) {
            looksLikeXML = data.indexOf('<') < data.indexOf("{");
        }
        return looksLikeXML;
    }

    private static boolean looksLikeJSON(String data) {
        boolean looksLikeJSON = false;
        if ((data.startsWith("{") || data.startsWith("[")) || ((data.contains("{") || data.contains("[")) && !data.contains(
                "<"))) {
            looksLikeJSON = true;
        } else if ((data.contains("{") || data.contains("[")) && data.contains("<")) {
            looksLikeJSON = (data.indexOf('{') < data.indexOf('<')) || (data.indexOf('[') < data.indexOf('<'));
        }
        return looksLikeJSON;
    }

    public static void parse(@NonNull JSONObject data, @NonNull DataObject dataObject, boolean... args) throws
            ParsingException {
        processArgs(args);
        JSONParser.parse(data, dataObject);
    }

    public static void parse(@NonNull JSONArray data, @NonNull DataObject dataObject, boolean... args)
            throws ParsingException {
        processArgs(args);
        JSONParser.parse(data, dataObject);
    }

    @IntDef(value = {XML, JSON, HTML, UNKNOWN})
    private @interface parseType {
    }
}
