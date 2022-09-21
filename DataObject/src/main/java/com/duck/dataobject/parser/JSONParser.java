package com.duck.dataobject.parser;

import androidx.annotation.NonNull;

import com.duck.dataobject.DataObject;
import com.duck.dataobject.exception.ParsingException;
import com.duck.dataobject.node.DataElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Bradley Duck on 2018/05/13.
 */
public class JSONParser extends Parser {
    private static final String LOGTAG = "JSONParser LOG";

    /**
     * Parses the given JSON String into the given {@link DataObject}.
     *
     * @param data       The JSON String to parse
     * @param dataObject The {@link DataObject} to parse into.
     * @throws ParsingException if the given JSON is malformed and invalid.
     */
    protected static void parse(@NonNull String data, @NonNull DataObject dataObject) throws ParsingException {
        try {
            parseObject(new JSONObject(data), dataObject);
        } catch (JSONException e) {
            try {
                dataObject.insert(parseArray(new JSONArray(data)));
            } catch (JSONException ex) {
                throw new ParsingException("Encountered a JSONException", e);
            }
        }
    }

    protected static void parse(@NonNull JSONObject data, @NonNull DataObject dataObject) {
        try {
            parseObject(data, dataObject);
        } catch (JSONException e) {
            throw new ParsingException("Encountered a JSONException", e);
        }
    }

    protected static void parse(@NonNull JSONArray data, @NonNull DataObject dataObject) {
        try {
            dataObject.insert(parseArray(data));
        } catch (JSONException e) {
            throw new ParsingException("Encountered a JSONException", e);
        }
    }

    private static void parseObject(@NonNull JSONObject jsonObject, @NonNull DataObject dataObject) throws JSONException {
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String name = keys.next();
            try {
                ArrayList<DataElement> elements = parseArray(name, jsonObject.getJSONArray(name));
                if (elements != null && elements.size() > 0) {
                    dataObject.insert(name, elements);
                }
            } catch (JSONException e) {
                try {
                    dataObject.insert(name, parseObject(jsonObject.getJSONObject(name)));
                } catch (JSONException ex) {
                    dataObject.insert(name, jsonObject.getString(name), false);
                }
            }
        }
    }

    private static DataObject parseObject(@NonNull JSONObject jsonObject) throws JSONException {
        DataObject dataObject = new DataObject();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String name = keys.next();
            try {
                ArrayList<DataElement> elements = parseArray(name, jsonObject.getJSONArray(name));
                if (elements != null && elements.size() > 0) {
                    dataObject.insert(name, elements);
                }
            } catch (JSONException e) {
                try {
                    dataObject.insert(name, parseObject(jsonObject.getJSONObject(name)));
                } catch (JSONException ex) {
                    dataObject.insert(name, jsonObject.getString(name), false);
                }
            }
        }
        return dataObject;
    }

    private static ArrayList<DataElement> parseArray(@NonNull JSONArray jsonArray) throws JSONException {
        ArrayList<DataElement> elements = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                ArrayList<DataElement> newElements = parseArray(jsonArray.getJSONArray(i));
                if (newElements != null && newElements.size() > 0) {
                    elements.add(new DataElement(newElements));
                }
            } catch (JSONException e) {
                try {
                    elements.add(new DataElement(parseObject(jsonArray.getJSONObject(i))));
                } catch (JSONException ex) {
                    elements.add(new DataElement("", jsonArray.getString(i)));
                }
            }
        }
        return elements;
    }

    private static ArrayList<DataElement> parseArray(String name, @NonNull JSONArray jsonArray) throws JSONException {
        ArrayList<DataElement> elements = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                ArrayList<DataElement> newElements = parseArray(jsonArray.getJSONArray(i));
                if (newElements != null && newElements.size() > 0) {
                    elements.add(new DataElement(name, newElements));
                }
            } catch (JSONException e) {
                try {
                    elements.add(new DataElement(name, parseObject(jsonArray.getJSONObject(i))));
                } catch (JSONException ex) {
                    elements.add(new DataElement(name, jsonArray.getString(i)));
                }
            }
        }
        return elements;
    }
}
