package com.duck.dataobject;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.duck.dataobject.exception.IllegalCharacterException;
import com.duck.dataobject.exception.ParsingException;
import com.duck.dataobject.node.DataElement;
import com.duck.dataobject.node.XMLAttribute;
import com.duck.dataobject.parser.Parser;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Bradley Duck on 2017/03/27
 * <p>
 * XML parser and data structure
 */
public class DataObject implements Comparable<DataObject>, Iterable<DataElement> {
    /**
     * List of Illegal characters.
     */
    public static final String[] illegals = {"<", ">", "/", "--", "[[", "]]", "&"};
    public static final String TAB = "\t";
    public static final String NEW_LINE = "\n";
    public static final String ANONYMOUS_ARRAY_TAG = "_anonymous_array_";
    public static final String CDATA_OPEN = "<![CDATA[";
    public static final String CDATA_CLOSE = "]]>";
    public static final String COMMENT_OPEN = "<!--";
    public static final String COMMENT_CLOSE = "-->";
    public static final String DOC_OPEN = "<?";
    public static final String DOC_CLOSE = "?>";
    public static final String LOG = "DataObject LOG";
    public static final int TAB_LEN = TAB.length();
    public static final int NEW_LINE_LEN = NEW_LINE.length();
    private HashMap<String, List<DataElement>> tagIndexMap = new HashMap<>(1);
    private boolean verbose = false;
    private boolean ignoreAttributes = false;

    /**
     * Constructor, sets the given {@link DataElement} as the firstElement of this {@link DataObject}.
     *
     * @param el   The object to be the firstElement of this {@link DataObject}.
     * @param args Optional, {@link Boolean Boolean[]}:<pre>	index 0: Ignore Attributes.<br/>	index 1: Verbose
     *                                     Logging.</pre>
     */
    public DataObject(@NonNull DataElement el, boolean... args) {
        this(args);
        //firstElement = el;
        List<DataElement> list = new ArrayList<DataElement>();
        list.add(el);
        tagIndexMap.put(el.tag, list);
    }

    /**
     * Default Constructor.
     *
     * @param args Optional, {@link Boolean Boolean[]}:<pre>	index 0: Ignore Attributes.<br/>	index 1: Verbose
     *                                     Logging.</pre>
     */
    public DataObject(boolean... args) {
        if (args != null) {
            if (args.length >= 1) {
                ignoreAttributes = args[0];
            }
            if (args.length >= 2) {
                verbose = args[1];
            }
        }
        if (verbose) {
            Log.v(LOG,
                  "Created an DataObject, verbose is on and ignoreAttributes is " + ((ignoreAttributes) ? "on" :
                                                                                     "off") + ".");
        }
    }

    public DataObject(@NonNull List<DataElement> elements, boolean... args) {
        this(args);
        for (int i = 0; i < elements.size(); i++) {
            elements.get(i).tag = String.valueOf(i);//for xml output
        }
        tagIndexMap.put(ANONYMOUS_ARRAY_TAG, elements);
    }

    public DataObject(@NonNull JSONObject data, boolean... args) {
        this(args);
        parse(data);
    }

    public void parse(@NonNull JSONObject data) throws ParsingException {
        Parser.parse(data, this, ignoreAttributes, verbose);
    }

    public DataObject(@NonNull JSONArray data, boolean... args) {
        this(args);
        parse(data);
    }

    public void parse(@NonNull JSONArray data) throws ParsingException {
        Parser.parse(data, this, ignoreAttributes, verbose);
    }

    public DataObject(@NonNull String tag, @NonNull List<DataElement> elements, boolean... args) {
        this(args);
        insert(tag, elements);
    }

    public DataObject insert(String tag, List<DataElement> elements) {
        if (tagIndexMap == null) {
            tagIndexMap = new HashMap<>();
            tagIndexMap.put(tag, elements);
        } else if (tagIndexMap.containsKey(tag)) {
            tagIndexMap.get(tag).addAll(elements);
        } else {
            tagIndexMap.put(tag, elements);
        }
        return this;
    }

    /**
     * Uses {@link Gson#toJson(Object)} to deserialize the given {@code src} into JSON and then parses that JSON into this
     * {@link DataObject}.
     *
     * @param src The src for which Json representation is to be created setting for Gson
     */
    public DataObject(@NonNull Object src, boolean... args) {
        this(new Gson().toJson(src), args);
    }

    /**
     * String constrictor, accepts XML as a String that will be parsed and used to construct this {@link DataObject}.
     *
     * @param data The XML String to be parsed.
     * @param args Optional, {@link Boolean Boolean[]}:<pre>	index 0: Ignore Attributes.<br/>	index 1: Verbose
     *                                     Logging.</pre>
     * @throws ParsingException If the given data String is malformed or invalid.
     */
    public DataObject(@NonNull String data, boolean... args) throws ParsingException {
        this(args);
        if (data != null && !data.equals("")) {
            //long parseStart = System.nanoTime();
            parse(data);
            //Parser.parse(data, this, ignoreAttributes, verbose);

			/*
			long parseEnd_toXMLStart = System.nanoTime();
			//String xmlString = toXML();
			//long toXMLEdnTime = System.nanoTime();
			App.Log.v(LOG, "DataObject.parse() execution time = " + ((parseEnd_toXMLStart-parseStart)/1000000.0) + " milliseconds." +
										"\n" +
										"DataObject.toXML() execution time = " + ((toXMLEdnTime-parseEnd_toXMLStart)/1000000.0) + " milliseconds.");*/
        }
    }

    /**
     * Deletes all current data and re-populates from the given {@code xmlData} xml String.
     *
     * @param xmlData The xml String to be parsed.
     * @throws ParsingException If the given xml String is malformed or invalid.
     */
    public void parse(@NonNull String xmlData) throws ParsingException {
        //root = null;
        if (tagIndexMap != null) {
            tagIndexMap.clear();
        } else {
            tagIndexMap = new HashMap<>();
        }
        Parser.parse(xmlData, this, ignoreAttributes, verbose);
    }

    /**
     * Uses {@link DataObject} to parse the XML or JSON and then, through the {@link DataObject#toJSON(String...)} method,
     * uses {@link Gson#fromJson(String, Class)} to deserialize the JSON into the T object.
     *
     * @param <T>       The type of the desired object.
     * @param data      The XML or JSON string from which the object is to be deserialized.
     * @param classOfT  The class of T
     * @param arrayTags (Optional) zero or more {@link String} 'tag' that should force output as an array in the JSON.
     * @return an object of type T from the string. Returns {@code null} if {@code json} is {@code null}.
     * @throws ParsingException    if the XML or JSON is not valid.
     * @throws JsonSyntaxException if json is not a valid representation for an object of type classOfT
     */
    @Nullable
    public static <T> T GSON_FromJSON(String data, Class<T> classOfT, String... arrayTags)
            throws ParsingException, JsonSyntaxException {
        return new Gson().fromJson(new DataObject(data).toJSON(arrayTags), classOfT);
    }

    @Nullable
    public static <T> String GSON_ToJSON(T data, String... arrayTags)
            throws ParsingException, JsonSyntaxException {
        return new Gson().toJson(data);
    }

    /**
     * Uses {@link Gson#toJson(Object)} to serialize the given {@code src} into JSON and then parses that JSON into a new {@link DataObject}.
     *
     * @param src The src for which Json representation is to be created setting for Gson
     * @return {@link DataObject} representation of {@code src}.
     */
    public static DataObject serialize(Object src) {
        return new DataObject(new Gson().toJson(src));
    }

    /**
     * Checks if the given value contains any Illegal characters that could break an XML parser.
     *
     * @param element The value to be checked.
     * @return {@code True} if and Illegal characters are found, {@code False} otherwise.
     */
    public static boolean hasIllegalValue(DataElement element) {
        String value = null;
        if (!element.isObject()) {
            try {
                value = element.getValueAsString("");
            } catch (Exception e) {
                //Log.e(LOG, "Error attempting to read value as string: ", e);
                //if its not a string then it can't have illegal values.
            }
        }
        if (value != null) {
            if (hasIllegalValue(value)) {
                element.CDATA = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given value contains any Illegal characters that could break an XML parser.
     *
     * @param value The value to be checked.
     * @return {@code True} if and Illegal characters are found, {@code False} otherwise.
     */
    public static boolean hasIllegalValue(String value) {
        for (String illegal : illegals) {
            if (value.contains(illegal)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given value contains any Illegal characters that could break an XML parser.
     *
     * @param value The value to be checked.
     * @return {@code True} if and Illegal characters are found, {@code False} otherwise.
     */
    public static boolean hasIllegal(String value) {
        for (String illegal : illegals) {
            if (value.contains(illegal)) {
                return true;
            }
        }
        return false;
    }

    public static int estimateLengthXML(@NonNull DataObject object) {
        return estimateLengthXML(object, null);
    }

    public static int estimateLengthXML(@NonNull DataObject object, @Nullable AtomicInteger length) {
        if (length == null) {
            length = new AtomicInteger(0);
        }
        for (DataElement element : object.getList()) {
            length.addAndGet(DataElement.estimateLengthXML(element, length));
        }
        return length.get();
    }

    public static int estimateLengthJSON(@NonNull DataObject object, String... arrayTags) {
        return estimateLengthJSON(object, null, arrayTags);
    }

    public static int estimateLengthJSON(@NonNull DataObject object, @Nullable AtomicInteger length, String... arrayTags) {
        if (length == null) {
            length = new AtomicInteger(0);
        }
        if (object.firstElement() != null) {
            //either we have an anonymous array or we have an object, it is illegal to have both
            if (object.tagIndexMap.containsKey(ANONYMOUS_ARRAY_TAG)) {
                //this object is a JSONArray.
                List<DataElement> elements = object.tagIndexMap.get(ANONYMOUS_ARRAY_TAG);
                length.addAndGet(1);//stringBuilder.append("[");
                for (int j = 0; j < elements.size(); j++) {
                    DataElement el = elements.get(j);
                    DataElement.estimateLengthJSON(el, length, arrayTags);//el.toJSON(stringBuilder, arrayTags);
                    if (j < elements.size() - 1) {
                        length.addAndGet(1);//stringBuilder.append(",");
                    }
                }
                length.addAndGet(1);//stringBuilder.append("]");
            } else {
                length.addAndGet(1);//stringBuilder.append("{");
                String[] tags = object.tagIndexMap.keySet().toArray(new String[]{});
                for (int i = 0; i < tags.length; i++) {
                    String tag = tags[i];
                    List<DataElement> elements = object.tagIndexMap.get(tag);
                    if (elements.size() == 1) {
                        //just one element for this tag
                        boolean forceArray = arrayTags != null && Arrays.asList(arrayTags)
                                                                        .contains(tag);// if this tag is in the arrayTags array then we know we need to output it as if it is a JSON_array even though it is only one element.
                        length.addAndGet(3 + tag.length());//stringBuilder.append("\"").append(tag).append("\":");
                        if (forceArray) {
                            length.addAndGet(1);//stringBuilder.append("[");
                        }
                        DataElement.estimateLengthJSON(elements.get(0),
                                                       length,
                                                       arrayTags);//elements.get(0).toJSON (stringBuilder, arrayTags);
                        if (forceArray) {
                            length.addAndGet(1);//stringBuilder.append("]");
                        }
                    } else {
                        //we have an array for this tag
                        length.addAndGet(4 + tag.length());//stringBuilder.append("\"").append(tag).append("\":[");
                        for (int j = 0; j < elements.size(); j++) {
                            DataElement el = elements.get(j);
                            DataElement.estimateLengthJSON(el, length, arrayTags);//el.toJSON(stringBuilder, arrayTags);
                            if (j < elements.size() - 1) {
                                length.addAndGet(1);//stringBuilder.append(",");
                            }
                        }
                        length.addAndGet(1);//stringBuilder.append("]");
                    }
                    if (i < tags.length - 1) {
                        length.addAndGet(1);//stringBuilder.append(",");
                    }
                }
                length.addAndGet(1);//stringBuilder.append("}");
            }
        }
        return length.get();
    }

    public static int estimateLengthFormattedJSON(@NonNull DataObject object, String... arrayTags) {
        return estimateLengthFormattedJSON(object, 0, null, arrayTags);
    }

    public static int estimateLengthFormattedJSON(@NonNull DataObject object, int indentCount, String... arrayTags) {
        return estimateLengthFormattedJSON(object, indentCount, null, arrayTags);
    }

    public static int estimateLengthFormattedJSON(
            @NonNull DataObject object, int indentCount, @Nullable AtomicInteger length,
            String... arrayTags) {
        if (length == null) {
            length = new AtomicInteger(0);
        }
        if (object.firstElement() != null) {
            int indentLen = TAB_LEN * indentCount;
            //either we have an anonymous array or we have an object, it is illegal to have both
            if (object.tagIndexMap.containsKey(ANONYMOUS_ARRAY_TAG)) {
                //this object is a JSONArray.
                List<DataElement> elements = object.tagIndexMap.get(ANONYMOUS_ARRAY_TAG);
                length.addAndGet(indentLen + 1 + NEW_LINE_LEN);
                //stringBuilder.append(indentSB).append("[").append(NEW_LINE);
                for (int j = 0; j < elements.size(); j++) {
                    DataElement el = elements.get(j);
                    DataElement.estimateLengthFormattedJSON(el, indentCount + 1, length, arrayTags);
                    //el.toFormattedJSON(indentCount+1, stringBuilder, arrayTags);
                    if (j < elements.size() - 1) {
                        length.addAndGet(1);//stringBuilder.append(",");
                    }
                    length.addAndGet(NEW_LINE_LEN);//stringBuilder.append(NEW_LINE);
                }
                length.addAndGet(indentLen + 1);//stringBuilder.append(indentSB).append("]");
            } else {
                /*if (stringBuilder.length() > 0 && stringBuilder.charAt(stringBuilder.length() - 1) == '\n') {
                    stringBuilder.append(indentSB);
                }*/
                length.addAndGet(1 + NEW_LINE_LEN);//stringBuilder.append("{").append(NEW_LINE);
                int innerIndentLen = indentLen + 1;
                //StringBuilder innerIndentSB = new StringBuilder(indentSB).append(TAB);
                String[] tags = object.tagIndexMap.keySet().toArray(new String[]{});
                for (int i = 0; i < tags.length; i++) {
                    String tag = tags[i];
                    List<DataElement> elements = object.tagIndexMap.get(tag);
                    if (elements.size() == 1) {
                        //just one element for this tag
                        length.addAndGet(innerIndentLen + 5 + tag.length());
                        //stringBuilder.append(innerIndentSB).append("\"").append(tag).append("\" : ");
                        if (arrayTags != null && Arrays.asList(arrayTags)
                                                       .contains(tag)) {// if this tag is in the arrayTags array then we know we need to output it as if it is a JSON_array even though it is only one element.
                            length.addAndGet(1 + NEW_LINE_LEN);
                            //stringBuilder.append("[").append(NEW_LINE);//.append(innerIndentSB);
                            DataElement.estimateLengthFormattedJSON(elements.get(0), indentCount + 1, length, arrayTags);
                            //elements.get(0).toFormattedJSON(indentCount+1, stringBuilder, arrayTags);
                            length.addAndGet(NEW_LINE_LEN + 1);
                            //stringBuilder.append(NEW_LINE).append("]");
                        } else {
                            DataElement.estimateLengthFormattedJSON(elements.get(0), indentCount + 1, length, arrayTags);
                            //elements.get(0).toFormattedJSON(indentCount, stringBuilder, arrayTags);
                        }
                    } else {
                        //we have an array for this tag
                        length.addAndGet(innerIndentLen + 6 + tag.length() + NEW_LINE_LEN);
                        //stringBuilder.append(innerIndentSB).append("\"").append(tag).append("\" : [").append(NEW_LINE);//.append(innerIndentSB);
                        for (int j = 0; j < elements.size(); j++) {
                            DataElement el = elements.get(j);
                            DataElement.estimateLengthFormattedJSON(el, indentCount + 1, length, arrayTags);
                            //el.toFormattedJSON(indentCount+1, stringBuilder, arrayTags);
                            if (j < elements.size() - 1) {
                                length.addAndGet(1);//stringBuilder.append(",");
                            }
                            length.addAndGet(NEW_LINE_LEN);//stringBuilder.append(NEW_LINE);//.append(innerIndentSB);
                        }
                        length.addAndGet(1);
                        //stringBuilder.append(innerIndentSB).append("]");
                    }
                    if (i < tags.length - 1) {
                        length.addAndGet(1);//stringBuilder.append(",");
                    }
                    length.addAndGet(NEW_LINE_LEN);//stringBuilder.append(NEW_LINE);
                }
                length.addAndGet(indentLen + 1);//stringBuilder.append(indentSB).append("}");
            }
        }
        return length.get();
    }

    /**
     * Searches the given {@link DataObject data}, navigating according to the given {@link JsonObject structure} and returns
     * a list of all the matching tags found at the end of the specified path.
     *
     * @param data      The  {@link DataObject} to search within.
     * @param structure The {@link JsonObject} defining the structure to search for.
     * @return a list of all the matching tags found at the end of the specified path.
     */
    @NonNull
    public static List<DataElement> getList(DataObject data, @NonNull JsonObject structure) {
        if (structure != null && data != null) {
            List<DataElement> elements = new ArrayList<>();
            if (!structure.has("xml") && data.contains("xml")) {
                return getList(data.get("xml").getValueAsObject(new DataObject()), structure);
            }
            for (Map.Entry<String, JsonElement> entry : structure.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                if (value.isJsonPrimitive()) {
                    DataElement element = data.get(key);
                    if (element != null) {
                        elements.add(element);
                    }
                } else if (value.isJsonObject()) {
                    elements = getList(getList(data, key), value.getAsJsonObject());
                }
            }
            return elements;
        }
        return new ArrayList<>();
    }

    /**
     * Searches the given {@link List}<{@link DataElement}> dataElements for all of the matching tags found at the given path
     * as defined in structure.
     *
     * @param dataElements {@link List}<{@link DataElement}> to search within.
     * @param structure    {@link JsonObject} The structure defining the path to follow to find the requested tag.
     * @return An {@link List}<{@link DataElement}> containing all the DataElements found at the given path as defined by
     *         structure.
     */
    @NonNull
    private static List<DataElement> getList(List<DataElement> dataElements, JsonObject structure) {
        if (structure != null && dataElements != null && dataElements.size() > 0) {
            List<DataElement> elements = new ArrayList<>();
            for (Map.Entry<String, JsonElement> entry : structure.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                for (DataElement element : dataElements) {
                    if (value.isJsonPrimitive()) {
                        if (element.isObject()) {
                            DataElement dataElement = element.getValueAsObject(new DataObject()).get(key);
                            if (dataElement != null) {
                                elements.add(dataElement);
                            }
                        }
                    } else if (value.isJsonObject()) {
                        if (element.isObject()) {
                            elements.addAll(getList(element.getValueAsObject(new DataObject()), key));
                        }
                    }
                }
            }
            return elements;
        }
        return new ArrayList<>();
    }

    /**
     * Searches through the given {@link String String[]} array of tags going 'into' each successive tag to look within for
     * the next tag until the final tag is reached which is then used to pull an {@link List} from the structure.
     *
     * @param data {@link DataObject} The object to search within.
     * @param tags The tag structure to move through to get to the final target tag.
     * @return An {@link List}<{@link DataElement}> Containing all of the matching tags found at the end of the given route.
     */
    @NonNull
    public static List<DataElement> getList(DataObject data, @NonNull String... tags) {
        if (tags != null) {
            if (tags.length > 1) {
                DataObject object = getObject(data, tags);
                if (object != null) {
                    String tag = tags[tags.length - 1];
                    return object.getList(tag);
                }
            } else {
                if (data != null) {
                    //only one tag provided.
                    if (!data.contains(tags[0]) && data.contains("xml")) {
                        DataElement xmlEl = data.get("xml");
                        if (xmlEl != null && xmlEl.isObject()) {
                            return getList(xmlEl.getValueAsObject(new DataObject()), tags);
                        }
                    } else {
                        return data.getList(tags[0]);
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * Searches through the given {@link String[]} array of tags going 'into' each successive tag to look within for the next
     * tag until the final tag is reached which is then returned.
     *
     * @param data {@link DataObject} The object to search within.
     * @param tags The tag structure to move through to get to the final target tag.
     * @return The {@link DataObject} found at the end of the given route.
     */
    @Nullable
    public static DataObject getObject(DataObject data, @NonNull String... tags) {
        if (tags != null) {
            if (tags.length > 1) {
                DataElement element = getElement(data, Arrays.copyOfRange(tags, 0, tags.length - 1));
                if (element != null) {
                    if (element.isObject()) {
                        return element.getValueAsObject(new DataObject());
                    }
                }
            } else {
                // only one tag provided...
                if (data != null) {
                    DataElement element = data.get(tags[0]);
                    if (element != null && element.isObject()) {
                        return element.getValueAsObject(new DataObject());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Searches through the given {@link String[]} array of tags going 'into' each successive tag to look within for the next
     * tag until the final tag is reached who's {@link DataElement} is then returned.
     *
     * @param data {@link DataObject} The object to search within.
     * @param tags The tag structure to move through to get to the final target tag.
     * @return The {@link DataElement} found at the end of the given route.
     */
    @Nullable
    public static DataElement getElement(DataObject data, @NonNull String... tags) {
        if (tags != null && tags.length > 0) {
            if (data != null) {
                int i = 0;
                //for (; i < tags.length; i++) {
                String tag = tags[i];
                if (!data.contains(tag) && data.contains("xml")) {
                    return getElement(data.get("xml").getValueAsObject(new DataObject()), tags);
                } else {
                    if (i == tags.length - 1) {
                        //The last given tag is the 'target' tag
                        return data.get(tag);
                    } else {
                        //Not yet the 'target' tag so keep digging
                        if (data.contains(tag)) {
                            DataElement element = data.get(tag);
                            if (element.isObject()) {
                                return getElement(element.getValueAsObject(new DataObject()),
                                                  Arrays.copyOfRange(tags, i + 1, tags.length));
                            } else {
                                //not at the target tag yet but we can't go any deeper...
                                return null;
                            }
                        } else {
                            //not at the target tag yet but the path ends here...
                            return null;
                        }
                    }
                }
            }
            //}
        }
        return null;
    }

    /**
     * Searches through the given {@link String[]} array of tags going 'into' each successive tag to look within for the next
     * tag until the final tag is reached and its {@link String} value it returned.
     *
     * @param data {@link DataObject} The object to search within.
     * @param tags The tag structure to move through to get to the final target tag.
     * @return The {@link String} value of the tag found at the end of the given route.
     */
    @Nullable
    public static String getValue(DataObject data, String defaultValue, @NonNull String... tags) {
        if (tags != null) {
            if (tags.length > 1) {
                DataElement element = getElement(data, tags);
                if (element != null) {
                    return element.getValueAsString(defaultValue);
                }
            } else {
                return data.get(tags[0]).getValueAsString(defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Uses {@link Gson#fromJson(String, Class)}, through the {@link DataObject#toJSON(String...)} method, to deserialize the
     * JSON into the T object.
     *
     * @param <T>       The type of the desired object.
     * @param classOfT  The class of T
     * @param arrayTags (Optional) zero or more {@link String} 'tag' that should force output as an array in the JSON.
     * @return an object of type T from the string. Returns {@code null} if {@code json} is {@code null}.
     * @throws ParsingException    if the XML or JSON is not valid.
     * @throws JsonSyntaxException if json is not a valid representation for an object of type classOfT
     */
    public final <T> T GSON_FromJSON(Class<T> classOfT, String... arrayTags) throws ParsingException, JsonSyntaxException {
        return new Gson().fromJson(toJSON(arrayTags), classOfT);
    }

    /**
     * Returns the first {@link DataElement} in this {@link DataObject} or {@code NULL} if there are no elements.
     *
     * @return The first {@link DataElement} in this {@link DataObject} or {@code NULL} if there are no elements.
     */
    public DataElement firstElement() {
        DataElement first = null;
        List<DataElement> arrayList = getList();
        if (arrayList.size() > 0) {
            first = arrayList.get(0);
        }
        return first;
    }

    public DataObject insert(List<DataElement> elements) {
        if (elements != null && elements.size() >= 1) {
            DataElement element = elements.get(0);
            if (element.tag.isEmpty()) {
                //this 'if' should only ever be true when the call comes from the JSONParser.
                int indx = 0;
                if (tagIndexMap.containsKey(ANONYMOUS_ARRAY_TAG)) {
                    indx = tagIndexMap.get(ANONYMOUS_ARRAY_TAG).size() - 1;
                } else if (!tagIndexMap.isEmpty()) {
                    tagIndexMap.clear();
                }
                for (int i = 0; i < elements.size(); i++) {
                    element = elements.get(i);
                    element.tag = String.valueOf(indx + i);//for xml output
                }
                tagIndexMap.put(ANONYMOUS_ARRAY_TAG, elements);
            } else {
                for (int i = 0; i < elements.size(); i++) {
                    insert(elements.get(i));
                }
            }
        }
        return this;
    }

    /**
     * Constructs an {@link DataElement} form the given {@code xmlTag} parameter and attempts to insert the created {@link
     * DataElement} after the last occurrence of the object's tag if there are or the end of the list.
     *
     * @param xmlTag The xml tag.
     * @return {@code True} if the tag and value was inserted successfully, {@code False} otherwise.
     */
    public DataObject insert(@NonNull String xmlTag) {
        return insert(new DataElement(xmlTag));
    }

    /**
     * Attempts to insert the given {@link DataElement} after the last occurrence of the object's tag if there are or the end
     * of the list.
     *
     * @param element The {@link DataElement} to be added.
     * @return {@code True} if the tag and value was inserted successfully, {@code False} otherwise.
     */
    public DataObject insert(@NonNull DataElement element) {
        if (tagIndexMap == null) {
            tagIndexMap = new HashMap<>(1);
        }
        if (element.tag.isEmpty()) {
            element.tag = getUniqueTag();
        }
        if (tagIndexMap.containsKey(element.tag)) {
            tagIndexMap.get(element.tag).add(element);
        } else {
            List<DataElement> elements = new ArrayList<>(1);
            elements.add(element);
            tagIndexMap.put(element.tag, elements);
        }
        return this;
    }

    private String getUniqueTag() {
        int tag = 0;
        while (tagIndexMap.containsKey(String.valueOf(tag++))) {
            ;
        }
        return String.valueOf(tag);
    }

    /**
     * Constructs an {@link DataElement} form the given {@code xmlTag}, {@code xmlValue} and (optional) {@code attributes}
     * parameters and attempts to insert the created {@link DataElement} after the last occurrence of the object's tag if
     * there are or the end of the list.
     *
     * @param xmlTag     The XML tag.
     * @param xmlValue   The value to be added as an {@link DataObject}.
     * @param attributes Optional, the attribute(s) for the {@link DataElement}.
     * @return {@code True} if the tag and value was inserted successfully, {@code False} otherwise.
     */
    public DataObject insert(@NonNull String xmlTag, @NonNull DataObject xmlValue, XMLAttribute... attributes) {
        return insert(new DataElement(xmlTag, xmlValue, attributes));
    }

    public DataObject insert(@NonNull String xmlTag, @NonNull String xmlValue, XMLAttribute... attributes) {
        return insert(xmlTag, xmlValue, false, attributes);
    }

    /**
     * Constructs an {@link DataElement} form the given {@code xmlTag}, {@code xmlValue}, {@code CDATA} and (optional) {@code
     * attributes} parameters and attempts to insert the created {@link DataElement} after the last occurrence of the
     * object's tag if there are or the end of the list.
     *
     * @param xmlTag     The XML tag.
     * @param xmlValue   The value to be added as a {@link String}.
     * @param CDATA      Set {@code True} if the value should be enclosed in CDATA.
     * @param attributes Optional, the attribute(s) for the {@link DataElement}.
     * @return {@code True} if the tag and value was inserted successfully, {@code False} otherwise.
     */
    public DataObject insert(@NonNull String xmlTag, @NonNull String xmlValue, boolean CDATA, XMLAttribute... attributes) {
        return insert(new DataElement(xmlTag, xmlValue, CDATA, attributes));
    }

    public DataObject insert(@NonNull String xmlTag, int xmlValue, XMLAttribute... attributes) {
        return insert(xmlTag, xmlValue, false, attributes);
    }

    public DataObject insert(@NonNull String xmlTag, int xmlValue, boolean CDATA, XMLAttribute... attributes) {
        return insert(new DataElement(xmlTag, xmlValue, CDATA, attributes));
    }

    public DataObject insert(@NonNull String xmlTag, float xmlValue, XMLAttribute... attributes) {
        return insert(xmlTag, xmlValue, false, attributes);
    }

    public DataObject insert(@NonNull String xmlTag, float xmlValue, boolean CDATA, XMLAttribute... attributes) {
        return insert(new DataElement(xmlTag, xmlValue, CDATA, attributes));
    }

    public DataObject insert(@NonNull String xmlTag, double xmlValue, XMLAttribute... attributes) {
        return insert(xmlTag, xmlValue, false, attributes);
    }

    public DataObject insert(@NonNull String xmlTag, double xmlValue, boolean CDATA, XMLAttribute... attributes) {
        return insert(new DataElement(xmlTag, xmlValue, CDATA, attributes));
    }

    /**
     * Constructs an {@link DataElement} form the given {@code xmlTag} and {@code xmlValue} parameters and attempts to insert
     * it, or if an DataElement exists with the same tag then that object will be replaced with the new object.
     *
     * @param xmlTag     The xml tag.
     * @param xmlValue   The xml valueAsString as an {@link DataObject}.
     * @param attributes Optional, The {@link XMLAttribute XMLAttribute(s)} for the new object.
     * @return {@code True} if the new object was inserted or replaced successfully, {@code False} otherwise.
     */
    public boolean update(@NonNull String xmlTag, @NonNull DataObject xmlValue, XMLAttribute... attributes) {
        return update(new DataElement(xmlTag, xmlValue, attributes));
    }

    /**
     * Accepts an {@link DataElement} as parameter and attempts to insert it, or if an DataElement exists with the same tag
     * and attribute(s) then that object will be replaced with the new object.
     *
     * @param newElement The new {@link DataElement}.
     * @param force      Optional, If {@code True} update will be forced even if value types don't match.
     * @return {@code True} if the new object was inserted or replaced successfully, {@code False} otherwise.
     */
    public boolean update(@NonNull DataElement newElement, boolean... force) {
        boolean Force = (force != null && force.length > 0 && force[0]);
        if (tagIndexMap.containsKey(newElement.tag)) {
            List<DataElement> elements = tagIndexMap.get(newElement.tag);
            if (elements.size() <= 0) {
                //if no elements then just add new one;
                elements.add(newElement);
                return true;
            }
            for (DataElement element : elements) {
                if (element.equals(newElement)) {
                    if (verbose) {
                        Log.v(LOG, "oldElement and newElement are the same so no need to do anything.");
                    }
                    return true;
                } else if (newElement.hasAttributes() && element.hasAttributes() && element.matchAttributes(newElement
                                                                                                                    .getAttributesAsArray())) {
                    updateByType(newElement, element, Force);
                    return true;
                }
            }
            //didn't find an exact matching object
            //update first occurrence of tag.
            updateByType(newElement, elements.get(0), Force);
            return true;
        } else {
            if (verbose) {
                Log.v(LOG, "there is no oldElement, doing normal insert.");
            }
            insert(newElement);
            return true;
        }
    }

    private void updateByType(DataElement newElement, DataElement oldElement, boolean force) {
        if (newElement.valueClass == String.class) {
            oldElement.update(newElement.getValueAsString(""), force);
        } else if (newElement.valueClass == int.class) {
            oldElement.update(newElement.getValueAsInteger(0), force);
        } else if (newElement.valueClass == float.class) {
            oldElement.update(newElement.getValueAsFloat(0), force);
        } else if (newElement.valueClass == DataObject.class) {
            oldElement.update(newElement.getValueAsObject(new DataObject()), force);
        }
        oldElement.CDATA = newElement.CDATA;
        oldElement.attributeRoot = newElement.attributeRoot;
    }

    /**
     * Constructs an {@link DataElement} form the given {@code xmlTag}, {@code xmlValue}, and (Optional){@code attributes}
     * parameters and attempts to insert it, or if an DataElement exists with the same tag and attributes then that object
     * will be replaced with the new object.
     *
     * @param xmlTag     The xml tag.
     * @param xmlValue   The xml valueAsString as an {@link DataObject}.
     * @param force      If {@code True} update will be forced even if value types don't match.
     * @param attributes Optional, The {@link XMLAttribute XMLAttribute(s)} for the new object.
     * @return {@code True} if the new object was inserted or replaced successfully, {@code False} otherwise.
     */
    public boolean update(@NonNull String xmlTag, @NonNull DataObject xmlValue, boolean force, XMLAttribute... attributes) {
        return update(new DataElement(xmlTag, xmlValue, attributes), force);
    }

    public boolean update(@NonNull String xmlTag, @NonNull String xmlValue, XMLAttribute... attributes) {
        return update(xmlTag, xmlValue, false, attributes);
    }

    /**
     * Constructs an {@link DataElement} form the given {@code xmlTag}, {@code xmlValue}, {@code CDATA} and (Optional){@code
     * attributes} parameters and attempts to insert it, or if an DataElement exists with the same tag and attributes then
     * that object will be replaced with the new object.
     *
     * @param xmlTag     The xml tag.
     * @param xmlValue   The xml valueAsString as an {@link String}.
     * @param CDATA      Set {@code True} if the value should be enclosed in CDATA.
     * @param attributes Optional, the {@link XMLAttribute XMLAttribute(s)}.
     * @return {@code True} if the new object was inserted or replaced successfully, {@code False} otherwise.
     */
    public boolean update(@NonNull String xmlTag, @NonNull String xmlValue, boolean CDATA, XMLAttribute... attributes) {
        return update(new DataElement(xmlTag, xmlValue, CDATA, attributes));
    }

    /**
     * Constructs an {@link DataElement} form the given {@code xmlTag}, {@code xmlValue}, {@code CDATA} and (Optional){@code
     * attributes} parameters and attempts to insert it, or if an DataElement exists with the same tag and attributes then
     * that object will be replaced with the new object.
     *
     * @param xmlTag     The xml tag.
     * @param xmlValue   The xml valueAsString as an {@link String}.
     * @param CDATA      Set {@code True} if the value should be enclosed in CDATA.
     * @param force      If {@code True} update will be forced even if value types don't match.
     * @param attributes Optional, the {@link XMLAttribute XMLAttribute(s)}.
     * @return
     */
    public boolean update(
            @NonNull String xmlTag, @NonNull String xmlValue, boolean CDATA, boolean force, XMLAttribute... attributes) {
        return update(new DataElement(xmlTag, xmlValue, CDATA, attributes), force);
    }

    /**
     * Constructs an {@link DataElement} form the given {@code xmlTag}, {@code xmlValue}, {@code CDATA} and (Optional){@code
     * attributes} parameters and attempts to insert it, or if an DataElement exists with the same tag and attributes then
     * that object will be replaced with the new object.
     *
     * @param xmlTag     The xml tag.
     * @param xmlValue   The xml valueAsString as an {@link Integer}.
     * @param CDATA      Set {@code True} if the value should be enclosed in CDATA.
     * @param attributes Optional, the {@link XMLAttribute XMLAttribute(s)}.
     * @return
     */
    public boolean update(@NonNull String xmlTag, int xmlValue, boolean CDATA, XMLAttribute... attributes) {
        return update(new DataElement(xmlTag, xmlValue, CDATA, attributes));
    }

    /**
     * Constructs an {@link DataElement} form the given {@code xmlTag}, {@code xmlValue}, {@code CDATA} and (Optional){@code
     * attributes} parameters and attempts to insert it, or if an DataElement exists with the same tag and attributes then
     * that object will be replaced with the new object.
     *
     * @param xmlTag     The xml tag.
     * @param xmlValue   The xml valueAsString as an {@link Integer}.
     * @param CDATA      Set {@code True} if the value should be enclosed in CDATA.
     * @param force      If {@code True} update will be forced even if value types don't match.
     * @param attributes Optional, the {@link XMLAttribute XMLAttribute(s)}.
     * @return
     */
    public boolean update(@NonNull String xmlTag, int xmlValue, boolean CDATA, boolean force, XMLAttribute... attributes) {
        return update(new DataElement(xmlTag, xmlValue, CDATA, attributes), force);
    }

    /**
     * Constructs an {@link DataElement} form the given {@code xmlTag}, {@code xmlValue}, {@code CDATA} and (Optional){@code
     * attributes} parameters and attempts to insert it, or if an DataElement exists with the same tag and attributes then
     * that object will be replaced with the new object.
     *
     * @param xmlTag     The xml tag.
     * @param xmlValue   The xml valueAsString as an {@link Float}.
     * @param CDATA      Set {@code True} if the value should be enclosed in CDATA.
     * @param attributes Optional, the {@link XMLAttribute XMLAttribute(s)}.
     * @return
     */
    public boolean update(@NonNull String xmlTag, float xmlValue, boolean CDATA, XMLAttribute... attributes) {
        return update(new DataElement(xmlTag, xmlValue, CDATA, attributes));
    }

    /**
     * Constructs an {@link DataElement} form the given {@code xmlTag}, {@code xmlValue}, {@code CDATA} and (Optional){@code
     * attributes} parameters and attempts to insert it, or if an DataElement exists with the same tag and attributes then
     * that object will be replaced with the new object.
     *
     * @param xmlTag     The xml tag.
     * @param xmlValue   The xml valueAsString as an {@link Float}.
     * @param CDATA      Set {@code True} if the value should be enclosed in CDATA.
     * @param force      If {@code True} update will be forced even if value types don't match.
     * @param attributes Optional, the {@link XMLAttribute XMLAttribute(s)}.
     * @return
     */
    public boolean update(@NonNull String xmlTag, float xmlValue, boolean CDATA, boolean force, XMLAttribute... attributes) {
        return update(new DataElement(xmlTag, xmlValue, CDATA, attributes), force);
    }

    /**
     * Constructs an {@link DataElement} form the given {@code xmlTag}, {@code xmlValue}, {@code CDATA} and (Optional){@code
     * attributes} parameters and attempts to insert it, or if an DataElement exists with the same tag and attributes then
     * that object will be replaced with the new object.
     *
     * @param xmlTag     The xml tag.
     * @param xmlValue   The xml valueAsString as an {@link Double}.
     * @param CDATA      Set {@code True} if the value should be enclosed in CDATA.
     * @param attributes Optional, the {@link XMLAttribute XMLAttribute(s)}.
     * @return
     */
    public boolean update(@NonNull String xmlTag, double xmlValue, boolean CDATA, XMLAttribute... attributes) {
        return update(new DataElement(xmlTag, xmlValue, CDATA, attributes));
    }

    /**
     * Constructs an {@link DataElement} form the given {@code xmlTag}, {@code xmlValue}, {@code CDATA} and (Optional){@code
     * attributes} parameters and attempts to insert it, or if an DataElement exists with the same tag and attributes then
     * that object will be replaced with the new object.
     *
     * @param xmlTag     The xml tag.
     * @param xmlValue   The xml valueAsString as an {@link Double}.
     * @param CDATA      Set {@code True} if the value should be enclosed in CDATA.
     * @param force      If {@code True} update will be forced even if value types don't match.
     * @param attributes Optional, the {@link XMLAttribute XMLAttribute(s)}.
     * @return
     */
    public boolean update(
            @NonNull String xmlTag, double xmlValue, boolean CDATA, boolean force, XMLAttribute... attributes) {
        return update(new DataElement(xmlTag, xmlValue, CDATA, attributes), force);
    }

    /**
     * Convenience method to merge in and update fron a {@link JsonObject}
     *
     * @param data The data to be merged in.
     */
    public DataObject updateMerge(@NonNull JsonObject data) {
        //DevNote: need testing
        return updateMerge(new DataObject(data));
    }

    /**
     * Merges the given {@param otherObject} into this, updating the value of existing tags and inserting new tags.
     *
     * @param otherObject The other {@link DataObject} to merge in.
     * @return {@link DataObject} this object.
     */
    public DataObject updateMerge(DataObject otherObject) {
        //DevNote: need testing
        for (DataElement otherElement : otherObject) {
            if (otherElement.isObject()) {
                // the other's element is an Object
                if (this.contains(otherElement.tag)) {
                    // I have the same tag, so get it and update
                    this.get(otherElement.tag).updateMerge(otherElement.getValueAsObject(new DataObject()));
                } else {
                    //I don't have the same tag, so insert it
                    this.insert(otherElement.tag, otherElement.getValueAsObject(new DataObject()));
                }
            } else {
                // the other's element is a simple value
                if (this.contains(otherElement.tag)) {
                    // I have the same tag, so get it and update
                    this.get(otherElement.tag).update(otherElement.getValueAsString(""), true);
                } else {
                    // I don't have the same tag, so insert it
                    this.insert(otherElement.tag, otherElement.getValueAsString(""), otherElement.CDATA);
                }
            }
        }
        return this;
    }

    /**
     * Finds and removes first occurrence the given {@code xmlTag} from the top level of this {@link DataObject}. If the
     * optional {@code attributes} parameter is set then the function will search for the first xml object with the matching
     * tag and attribute(s).
     *
     * @param xmlTag     The xml tag to be removed.
     * @param attributes Optional, one or more {@link XMLAttribute XMLAttribute(s)} to match to the xml object to be
     *                   removed.
     * @return The {@link DataElement} that was removed or null.
     */
    public DataElement remove(@NonNull String xmlTag, XMLAttribute... attributes) {
        return remove(xmlTag, false, attributes);
    }

    /**
     * Finds and removes first occurrence the given {@code xmlTag} containing the given {@code attributeTag} and matching
     * {@code attributeValue} from the top level of this {@link DataObject}. If the optional {@code deepRemove} parameter is
     * {@code true} then the function will search through the entire {@link DataObject}.
     *
     * @param xmlTag         The xml tag to be removed.
     * @param attributeTag   The Attribute tag to match.
     * @param attributeValue The Attribute value to match.
     * @param deepRemove     Optional, if {@code true} the function will perform a deep search for the object to be removed.
     * @return The {@link DataElement} that was removed or null.
     */
    public DataElement remove(
            @NonNull String xmlTag, @NonNull String attributeTag, @NonNull String attributeValue, boolean... deepRemove) {
        return remove(xmlTag,
                      ((deepRemove != null && deepRemove.length > 0) && deepRemove[0]),
                      new XMLAttribute(attributeTag, attributeValue));
    }

    /**
     * Finds and removes first occurrence the given {@code xmlTag} from the top level of this {@link DataObject}. If the
     * {@code deepRemove} parameter is {@code true} then the function will search through the entire {@link DataObject} to
     * remove the first occurrence of the given tag. If the optional {@code attributes} parameter is set the function will
     * remove the first object with a matching tag and attribute(s).
     *
     * @param xmlTag     The xml tag to be removed.
     * @param deepRemove Optional, if {@code true} the function will perform a deep search for the object to be removed.
     * @param attributes
     * @return The {@link DataElement} that was removed or null.
     */
    public DataElement remove(@NonNull String xmlTag, boolean deepRemove, XMLAttribute... attributes) {
        if (verbose) {
            StringBuilder tag = new StringBuilder("<" + xmlTag);
            if (attributes != null) {
                for (XMLAttribute attribute : attributes) {
                    tag.append(" ").append(attribute.toString());
                }
            }
            tag.append(">");
            Log.v(LOG, "Attempting to remove " + tag + ", DeepRemove is " + (deepRemove ? "on" : "off") + ".");
        }
        DataElement removed = null;
        List<DataElement> elements = tagIndexMap.get(xmlTag);
        if (elements != null) {
            if (attributes != null && attributes.length > 0) {
                for (DataElement element : elements) {
                    if (element.matchAttributes(attributes)) {
                        elements.remove(element);
                        removed = element;
                        break;
                    }
                }
            } else {
                removed = elements.remove(0);
            }
            if (elements.size() == 0) {
                tagIndexMap.remove(xmlTag);
            }
            if (!deepRemove || removed != null) {
                return removed;
            }
        }
        //deep search
        for (Map.Entry<String, List<DataElement>> listEntry : tagIndexMap.entrySet()) {
            for (DataElement element : listEntry.getValue()) {
                if (element.isObject()) {
                    removed = element.getValueAsObject(new DataObject()).remove(xmlTag, deepRemove, attributes);
                    if (removed != null) {
                        return removed;
                    }
                }
            }
        }
        if (verbose) {
            Log.v(LOG, "could not find anything to remove.");
        }
        return null;
    }

    public DataObject recursiveRemove(@NonNull JsonObject removeStructure) {
        return recursiveRemove(new DataObject(removeStructure));
    }

    public DataObject recursiveRemove(@NonNull DataObject removeStructure) {
        //DevNote: need testing
        for (DataElement removeElement : removeStructure) {
            if (this.contains(removeElement.tag)) {
                DataElement myElement = this.get(removeElement.tag);
                if (removeElement.isObject()) {
                    if (myElement.isObject()) {
                        myElement.getValueAsObject(new DataObject())
                                 .recursiveRemove(removeElement.getValueAsObject(new DataObject()));
                    } else {
                        this.remove(myElement.tag);
                    }
                } else {
                    this.remove(myElement.tag);
                }
            } else {
                //we do not contain the removeTag at this level
                if (this.contains(removeElement.tag, true)) {
                    // we do have the tag deeper in
                    if (removeElement.isObject()) {
                        List<DataElement> myElements = this.getList();
                        for (int i = 0; i < myElements.size(); i++) {
                            if (myElements.get(i).isObject()) {
                                DataObject myElObject = myElements.get(i).getValueAsObject(new DataObject());
                                if (myElObject.contains(removeElement.tag, true)) {
                                    myElObject.recursiveRemove(removeElement.getValueAsObject(new DataObject()));
                                    break;
                                }
                            }
                        }
                    } else {
                        List<DataElement> myElements = this.getList();
                        for (int i = 0; i < myElements.size(); i++) {
                            if (myElements.get(i).isObject()) {
                                DataObject myElObject = myElements.get(i).getValueAsObject(new DataObject());
                                if (myElObject.contains(removeElement.tag, true)) {
                                    myElObject.recursiveRemove(removeStructure);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return this;
    }

    /**
     * searches the top level of this {@link DataElement} for the given {@code xmlTag} and matching the (Optional){@code
     * attributes} and returns its {@link DataElement}. If the given {@code xmlTag} does not exist then it will be created
     * and returned.
     *
     * @param xmlTag     The xmlTag to search for.
     * @param attributes Optional, The {@link XMLAttribute XMLAttribute(s)} to match against.
     * @return The found or created {@link DataElement}
     */
    @NonNull
    public DataElement getOrCreate(@NonNull String xmlTag, XMLAttribute... attributes) {
        DataElement dataElement = get(xmlTag, false, attributes);
        if (dataElement == null) {
            dataElement = new DataElement(xmlTag, attributes);
            insert(dataElement);
        }
        return dataElement;
    }

    /**
     * searches the top level of this {@link DataElement} for the given {@code xmlTag} and matching the (Optional){@code
     * attributes} and returns its {@link DataElement}.
     *
     * @param xmlTag     The xmlTag to search for.
     * @param attributes Optional, The {@link XMLAttribute XMLAttribute(s)} to match against.
     * @return The found {@link DataElement} or null if no matching object could be found.
     */
    @Nullable
    public DataElement get(@NonNull String xmlTag, XMLAttribute... attributes) {
        return get(xmlTag, false, attributes);
    }

    /**
     * searches the top level of this {@link DataObject} for the given {@code xmlTag} with matching the {@code attributeTag}
     * and {@code attributeValue} and returns its {@link DataElement}. if the {@code deepGet} parameter is set to true then
     * the function will perform a deep (breadth first) search for the given tag.
     *
     * @param xmlTag         The xmlTag to search for.
     * @param attributeTag   The attribute tag to match.
     * @param attributeValue The attribute value to match.
     * @param deepGet        Optional, if {@code True} then the function will perform a deep search.
     * @return The found {@link DataElement} or null if no matching object could be found.
     */
    @Nullable
    public DataElement get(
            @NonNull String xmlTag, @NonNull String attributeTag, @NonNull String attributeValue, boolean... deepGet) {
        return get(xmlTag,
                   (deepGet != null && deepGet.length > 0 && deepGet[0]),
                   new XMLAttribute(attributeTag, attributeValue));
    }

    /**
     * searches the top level of this {@link DataElement} for the given {@code xmlTag} and matching the (Optional){@code
     * attributes} and returns its {@link DataElement}. if the {@code deepGet} parameter is set to true then the function
     * will perform a deep (breadth first) search for the given tag.
     *
     * @param xmlTag     The xmlTag to search for.
     * @param deepGet    Optional, if {@code True} then the function will perform a deep search.
     * @param attributes Optional, The {@link XMLAttribute XMLAttribute(s)} to match against.
     * @return The found {@link DataElement} or null if no matching object could be found.
     */
    @Nullable
    public DataElement get(@NonNull String xmlTag, boolean deepGet, XMLAttribute... attributes) {
        if (verbose) {
            StringBuilder tag = new StringBuilder("<" + xmlTag);
            if (attributes != null) {
                for (XMLAttribute attribute : attributes) {
                    tag.append(" ").append(attribute.toString());
                }
            }
            tag.append(">");
            Log.v(LOG, "Attempting to get " + tag + ", DeepGet is " + (deepGet ? "on" : "off") + ".");
        }
        if (tagIndexMap.containsKey(xmlTag)) {
            List<DataElement> elements = tagIndexMap.get(xmlTag);
            if (elements.size() < 1) {
                //tag exists but has not data, should never get here but if we do then we should clean-up...
                tagIndexMap.remove(xmlTag);
                if (!deepGet) {
                    return null;
                }
            }
            if (attributes != null) {
                for (DataElement element : elements) {
                    if (element.matchAttributes(attributes)) {
                        return element;
                    }
                }
                if (!deepGet) {
                    //didn't find and not deepGet so return null;
                    return null;
                }
            } else {
                //no attributes given, return the first one
                return elements.get(0);
            }
        }
        if (deepGet) {
            for (Map.Entry<String, List<DataElement>> listEntry : tagIndexMap.entrySet()) {
                for (DataElement element : listEntry.getValue()) {
                    if (element.isObject()) {
                        DataElement foundElement = element.getValueAsObject(new DataObject()).get(xmlTag, true, attributes);
                        if (foundElement != null) {
                            return foundElement;
                        }
                    }
                }
            }
        }
        //if not found at this point then it doesn't exist.
        return null;
    }

    /**
     * Creates and returns an {@link List< DataElement >} containing all the Elements with a matching {@code xmlTag} and
     * (optional) Attributes.
     *
     * @param xmlTag
     * @param attributes
     * @return
     */
    @NonNull
    public List<DataElement> getList(@NonNull String xmlTag, XMLAttribute... attributes) {
        List<DataElement> returnElements = null;
        if (attributes == null) {
            returnElements = tagIndexMap.get(xmlTag);
        } else {
            List<DataElement> elements = tagIndexMap.get(xmlTag);
            returnElements = new ArrayList<>();
            if (elements != null) {
                for (DataElement element : elements) {
                    if (element.hasAttribute(attributes)) {
                        returnElements.add(element);
                    }
                }
            }
        }
        return returnElements;
    }

    /**
     * Checks if an object with the given {@code xmlTag} exists within the top level of this {@link DataObject} or not. if
     * the (optional){@code deepSearch} parameter is {@code True} then this function will perform a deep search.
     *
     * @param xmlTag     The {@code xmlTag} to search for.
     * @param deepSearch Optional, if {@code True} then this function will perform a deep search.
     * @return {@code True} if the object exists, {@code False} otherwise.
     */
    public boolean contains(String xmlTag, boolean... deepSearch) {
        return get(xmlTag, (deepSearch != null && deepSearch.length > 0 && deepSearch[0])) != null;
    }

    @Override
    public int compareTo(@NonNull DataObject other) {
        if (tagIndexMap.equals(other.tagIndexMap)) {
        }
        return tagIndexMap.size() - other.tagIndexMap.size();
		/*if (root == null && other.root == null) {
			// both are empty.
			return 0;
		} else if (root != null && other.root != null) {
			//check this against other
			for (DataElement elementA : this) {
				boolean check = false;
				for (DataElement elementB : other) {
					if (elementA.equals(elementB)) {
						check = true;
						break;
					}
				}
				if (!check) {
					//found a mismatch
					return -1;
				}
			}
			//check other against this
			for (DataElement elementA : other) {
				boolean check = false;
				for (DataElement elementB : this) {
					if (elementA.equals(elementB)) {
						check = true;
						break;
					}
				}
				if (!check) {
					//found a mismatch
					return -1;
				}
			}
			// no mismatches were found so the must be the same.
			return 0;
		} else {
			//one's root is null and the other isn't
			return -1;
		}*/
    }

    /**
     * Returns an iterator over tags of type {@link DataElement}.
     *
     * @return an Iterator.
     */
    @NonNull
    @Override
    public Iterator<DataElement> iterator() {
        return getList().iterator();
		/*return new Iterator<DataElement>() {
			boolean firstEl = true;
			private DataElement cur = root;
			private DataElement prev = null;

			@Override
			public boolean hasNext() {
				return (cur != null) && (firstEl || cur.hasNext());
			}

			@Override
			public DataElement next() {
				if (hasNext()) {
					if (firstEl) {
						firstEl = false;
					} else {
						prev = cur;
						cur = cur.next;
					}
					return cur;
				}
				throw new NoSuchElementException();
			}

			/**
			 * WARNING calling remove will remove the object from the original source as well!
			 */
			/*@Override
			public void remove() {
				if (prev == null) {
					root = cur.next;
					cur = root;
				} else {
					prev.next = cur.next;
					cur = prev.next;
				}
			}
		};*/
    }

    @NonNull
    public List<DataElement> getList() {
        List<DataElement> list = new ArrayList<>(tagIndexMap.size());
        for (List<DataElement> value : tagIndexMap.values()) {
            list.addAll(value);
        }
        return list;
    }

    @Override
    public String toString() {
        return toXML();
    }

    /**
     * Constructs and returns an xml string representing this {@link DataObject}.
     *
     * @return an xml string representing this {@link DataObject}.
     */
    public String toXML() {
        return toXML(new StringBuilder(estimateLengthXML()));
    }

    public String toXML(@Nullable StringBuilder stringBuilder) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder(estimateLengthXML());
        }
        /*if (root == null) {
            root();
        }*/
        if (firstElement() != null) {
            for (DataElement el : this) {
                el.toXML(stringBuilder);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Constructs and returns an xml string representing this {@link DataObject}, wrapped with the given {@code
     * wrapWithTag}.
     *
     * @param wrapWithTag The tag to wrap with.
     * @return an xml string representing this {@link DataObject}, wrapped with the given {@code wrapWithTag}.
     * @throws IllegalCharacterException if the given {@code wrapWithTag} contains any Illegal characters.
     */
    public String toXML(String wrapWithTag) throws IllegalCharacterException {
        if (hasIllegalValue(wrapWithTag)) {
            throw new IllegalCharacterException("Received wrapWithTag: '" + wrapWithTag + "', may not contain illegal characters like: '<', '>', '/', '--', '[[', ']]', '&'");
        }
        return "<" + wrapWithTag + ">" + toXML() + "</" + wrapWithTag + ">";
    }

    /**
     * Constructs and returns a formatted xml string representing this {@link DataObject}.
     *
     * @return a formatted xml string representing this {@link DataObject}.
     */
    public String toFormattedXML() {
        return toFormattedXML(0);
    }

    /**
     * Constructs and returns a formatted xml string representing this {@link DataObject}.
     *
     * @param indentCount The starting indentation level.
     * @return a formatted xml string representing this {@link DataObject}.
     */
    public String toFormattedXML(int indentCount) {
        StringBuilder string = new StringBuilder();
        for (DataElement el : this) {
            string.append(el.toFormattedXML(indentCount)).append("\n");
        }
        while (string.toString().endsWith("\n")) {
            string = new StringBuilder(string.substring(0, string.lastIndexOf("\n")));
        }
        return string.toString();
    }

    public int estimateLengthXML() {
        return estimateLengthXML(this);
    }

    public int estimateLengthFormattedXML() {
        return estimateLengthFormattedXML(this);
    }

    public static int estimateLengthFormattedXML(@NonNull DataObject object) {
        return estimateLengthFormattedXML(object, 0, null);
    }

    public static int estimateLengthFormattedXML(
            @NonNull DataObject object, int indentCount, @Nullable AtomicInteger length) {
        if (length == null) {
            length = new AtomicInteger(0);
        }
        for (DataElement element : object.getList()) {
            length.addAndGet(DataElement.estimateLengthFormattedXML(element, indentCount, length));
        }
        return length.get();
    }

    public int estimateLengthFormattedXML(int indentCount) {
        return estimateLengthFormattedXML(this, indentCount);
    }

    public static int estimateLengthFormattedXML(@NonNull DataObject object, int indentCount) {
        return estimateLengthFormattedXML(object, indentCount, null);
    }

    public int estimateLengthJSON(String... arrayTags) {
        return estimateLengthJSON(this, arrayTags);
    }

    public int estimateLengthFormattedJSON(String... arrayTags) {
        return estimateLengthFormattedJSON(this, arrayTags);
    }

    public int estimateLengthFormattedJSON(int indentCount, String... arrayTags) {
        return estimateLengthFormattedJSON(this, indentCount, arrayTags);
    }

    /**
     * Outputs this {@link DataObject} as a JSON string.
     *
     * @param arrayTags (Optional) Set of tags to ensure are output as JSON Array even if there is only a single element.
     * @return {@link String} JSON representation of this {@link DataObject}.
     */
    public String toJSON(String... arrayTags) {
        return toJSON(new StringBuilder(estimateLengthJSON(arrayTags)), arrayTags);
    }

    /**
     * Outputs this {@link DataObject} as JSON string.
     *
     * @param stringBuilder {@link StringBuilder} Nullable.
     * @param arrayTags     (Optional) Set of tags to ensure are output as JSON Array even if there is only a single
     *                      element.
     * @return {@link String} JSON representation of this {@link DataObject}.
     */
    //Todo method could use some work - how so?
    public String toJSON(@Nullable StringBuilder stringBuilder, String... arrayTags) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder(estimateLengthJSON());
        }
        if (firstElement() != null) {
            //either we have an anonymous array or we have an object, it is illegal to have both
            if (tagIndexMap.containsKey(ANONYMOUS_ARRAY_TAG)) {
                //this object is a JSONArray.
                List<DataElement> elements = tagIndexMap.get(ANONYMOUS_ARRAY_TAG);
                stringBuilder.append("[");
                for (int j = 0; j < elements.size(); j++) {
                    DataElement el = elements.get(j);
                    el.toJSON(stringBuilder, arrayTags);
                    if (j < elements.size() - 1) {
                        stringBuilder.append(",");
                    }
                }
                stringBuilder.append("]");
            } else {
                stringBuilder.append("{");
                String[] tags = tagIndexMap.keySet().toArray(new String[]{});
                for (int i = 0; i < tags.length; i++) {
                    String tag = tags[i];
                    List<DataElement> elements = tagIndexMap.get(tag);
                    if (elements.size() == 1) {
                        //just one element for this tag
                        boolean forceArray = arrayTags != null && Arrays.asList(arrayTags)
                                                                        .contains(tag);// if this tag is in the arrayTags array then we know we need to output it as if it is a JSON_array even though it is only one element.
                        stringBuilder.append("\"").append(tag).append("\":");
                        if (forceArray) {
                            stringBuilder.append("[");
                        }
                        elements.get(0).toJSON(stringBuilder, arrayTags);
                        if (forceArray) {
                            stringBuilder.append("]");
                        }
                    } else {
                        //we have an array for this tag
                        stringBuilder.append("\"").append(tag).append("\":[");
                        for (int j = 0; j < elements.size(); j++) {
                            DataElement el = elements.get(j);
                            el.toJSON(stringBuilder, arrayTags);
                            if (j < elements.size() - 1) {
                                stringBuilder.append(",");
                            }
                        }
                        stringBuilder.append("]");
                    }
                    if (i < tags.length - 1) {
                        stringBuilder.append(",");
                    }
                }
                stringBuilder.append("}");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Outputs this {@link DataObject} as a formatted JSON string.
     *
     * @param arrayTags (Optional) Set of tags to ensure are output as JSON Array even if there is only a single element.
     * @return {@link String} JSON representation of this {@link DataObject}.
     */
    public String toFormattedJSON(String... arrayTags) {
        return toFormattedJSON(0, new StringBuilder(estimateLengthFormattedJSON(arrayTags)), arrayTags);
    }

    public String toFormattedJSON(int indentCount, String... arrayTags) {
        return toFormattedJSON(indentCount, new StringBuilder(estimateLengthFormattedJSON(arrayTags)), arrayTags);
    }

    /**
     * Outputs this {@link DataObject} as formatted JSON string.
     *
     * @param stringBuilder {@link StringBuilder} Nullable.
     * @param arrayTags     (Optional) Set of tags to ensure are output as JSON Array even if there is only a single
     *                      element.
     * @return {@link String} JSON representation of this {@link DataObject}.
     */
    public String toFormattedJSON(int indentCount, @Nullable StringBuilder stringBuilder, String... arrayTags) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder(estimateLengthFormattedJSON(indentCount, arrayTags));
        }
        if (firstElement() != null) {
            StringBuilder indentSB = new StringBuilder(TAB_LEN * indentCount);
            for (int i = 0; i < indentCount; i++) {
                indentSB.append(TAB);
            }
            //either we have an anonymous array or we have an object, it is illegal to have both
            if (tagIndexMap.containsKey(ANONYMOUS_ARRAY_TAG)) {
                //this object is a JSONArray.
                List<DataElement> elements = tagIndexMap.get(ANONYMOUS_ARRAY_TAG);
                stringBuilder.append(indentSB).append("[").append(NEW_LINE);
                for (int j = 0; j < elements.size(); j++) {
                    DataElement el = elements.get(j);
                    el.toFormattedJSON(indentCount + 1, stringBuilder, arrayTags);
                    if (j < elements.size() - 1) {
                        stringBuilder.append(",");
                    }
                    stringBuilder.append(NEW_LINE);
                }
                stringBuilder.append(indentSB).append("]");
            } else {
                if (stringBuilder.length() > 0 && stringBuilder.charAt(stringBuilder.length() - 1) == '\n') {
                    stringBuilder.append(indentSB);
                }
                stringBuilder.append("{").append(NEW_LINE);
                StringBuilder innerIndentSB = new StringBuilder(indentSB).append(TAB);
                String[] tags = tagIndexMap.keySet().toArray(new String[]{});
                for (int i = 0; i < tags.length; i++) {
                    String tag = tags[i];
                    List<DataElement> elements = tagIndexMap.get(tag);
                    if (elements.size() == 1) {
                        //just one element for this tag
                        stringBuilder.append(innerIndentSB).append("\"").append(tag).append("\" : ");
                        if (arrayTags != null && Arrays.asList(arrayTags)
                                                       .contains(tag)) {// if this tag is in the arrayTags array then we know we need to output it as if it is a JSON_array even though it is only one element.
                            stringBuilder.append("[").append(NEW_LINE);//.append(innerIndentSB);
                            elements.get(0).toFormattedJSON(indentCount + 1, stringBuilder, arrayTags);
                            stringBuilder.append(NEW_LINE).append(innerIndentSB).append("]");
                        } else {
                            elements.get(0).toFormattedJSON(indentCount, stringBuilder, arrayTags);
                        }
                    } else {
                        //we have an array for this tag
                        stringBuilder.append(innerIndentSB).append("\"").append(tag).append("\" : [")
                                     .append(NEW_LINE);//.append(innerIndentSB);
                        for (int j = 0; j < elements.size(); j++) {
                            DataElement el = elements.get(j);
                            el.toFormattedJSON(indentCount + 1, stringBuilder, arrayTags);
                            if (j < elements.size() - 1) {
                                stringBuilder.append(",");
                            }
                            stringBuilder.append(NEW_LINE);//.append(innerIndentSB);
                        }
                        stringBuilder.append(innerIndentSB).append("]");
                    }
                    if (i < tags.length - 1) {
                        stringBuilder.append(",");
                    }
                    stringBuilder.append(NEW_LINE);
                }
                stringBuilder.append(indentSB).append("}");
            }
        }
        return stringBuilder.toString();
    }

    public DataObject merge(DataObject other) {
        if (other != null) {
            String[] otherTags = other.tagIndexMap.keySet().toArray(new String[]{});
            for (int i = 0; i < otherTags.length; i++) {
                if (this.contains(otherTags[i])) {
                    //I have the otherTags[i] tag
                    tagIndexMap.put(otherTags[i], merge(tagIndexMap.get(otherTags[i]), other.tagIndexMap.get(otherTags[i])));
                } else {
                    // I do not have otherTags[i] tag so insert it.
                    this.insert(other.tagIndexMap.get(otherTags[i]));
                }
            }
        }
        return this;
    }

    public List<DataElement> merge(List<DataElement> myElements, List<DataElement> otherElements) {
        for (int i = 0; i < otherElements.size(); i++) {
            if (i < myElements.size()) {
                myElements.set(i, myElements.get(i).merge(otherElements.get(i)));
            } else {
                myElements.add(otherElements.get(i));
            }
        }
        return myElements;
    }

    /**
     * Returns <code>true</code> if this DataObject contains no data.
     *
     * @return boolean <code>true</code> if this DataObject contains no data.
     */
    public boolean isEmpty() {

        return tagIndexMap == null || tagIndexMap.isEmpty();
    }

//    /**
//     * This method will use the provided {@link EncryptionUtil.EncryptionInterface#encrypt(Context, String)} to encrypt the
//     * value of
//     * each element within
//     * this Object.
//     *
//     * @param encryptor {@link EncryptionUtil.EncryptionInterface} to be used for the actual encryption.
//     */
//    public void encryptValues(Context context, @NonNull EncryptionUtil.EncryptionInterface encryptor) {
//
//        if (!isEmpty() && encryptor != null) {
//            Collection<List<DataElement>> values = tagIndexMap.values();
//            for (List<DataElement> elements : values) {
//                for (DataElement element : elements) {
//                    element.encryptValues(context, encryptor);
//                }
//            }
//        }
//    }
//
//    public void decryptValues(Context context, @NonNull EncryptionUtil.EncryptionInterface encryptor) {
//
//        if (!isEmpty() && encryptor != null) {
//            Collection<List<DataElement>> values = tagIndexMap.values();
//            for (List<DataElement> elements : values) {
//                for (DataElement element : elements) {
//                    element.decryptValues(context, encryptor);
//                }
//            }
//        }
//    }
}
