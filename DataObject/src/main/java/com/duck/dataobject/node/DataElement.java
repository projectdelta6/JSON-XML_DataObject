package com.duck.dataobject.node;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.duck.dataobject.DataObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.duck.dataobject.DataObject.NEW_LINE;
import static com.duck.dataobject.DataObject.NEW_LINE_LEN;
import static com.duck.dataobject.DataObject.TAB;
import static com.duck.dataobject.DataObject.TAB_LEN;

/**
 * Created by Bradley Duck on 2017/03/31
 * <p>
 * Node class for an XML element.
 */
public class DataElement implements Comparable<DataElement>, Iterable<XMLAttribute> {
    public String tag;
    public XMLAttribute attributeRoot = null;
    public boolean CDATA = false;
    public Class valueClass = null;
    public Object value;

    /**
     * Default empty constructor
     */
    DataElement() {
    }

    /**
     * Constructs an empty {@link DataElement} with the given {@code tag} and optional {@code attributes}.
     *
     * @param tag        The tag for this {@link DataElement}.
     * @param attributes Optional, {@link XMLAttribute XMLAttribute(s)} for this element.
     */
    public DataElement(String tag, XMLAttribute... attributes) {
        this(tag, false, attributes);
    }

    /**
     * Constructs an empty {@link DataElement} with the given {@code tag} and optional {@code attributes}.
     *
     * @param tag        The tag for this {@link DataElement}.
     * @param CDATA      CDATA flag, if true the value of this element will be wrapped in a CDATA tag.
     * @param attributes Optional, {@link XMLAttribute XMLAttribute(s)} for this element.
     */
    public DataElement(String tag, boolean CDATA, XMLAttribute... attributes) {
        this.tag = tag;
        this.CDATA = CDATA;
        addAttributes(attributes);
    }

    /**
     * Adds the given set of {@link XMLAttribute XMLAttributes} to this {@link DataElement}
     *
     * @param attributes The set of {@link XMLAttribute XMLAttributes} to be added.
     */
    public void addAttributes(XMLAttribute... attributes) {
        if (attributes != null && attributes.length >= 1) {
            for (XMLAttribute att : attributes) {
                addAttribute(att);
            }
        }
    }

    /**
     * Adds the given {@link XMLAttribute} to this {@link DataElement}.
     *
     * @param xmlAttribute The {@link XMLAttribute} to be added.
     */
    public void addAttribute(XMLAttribute xmlAttribute) {
        if (xmlAttribute != null) {
            if (attributeRoot == null) {
                attributeRoot = new XMLAttribute(xmlAttribute, true);
            } else {
                XMLAttribute walker = attributeRoot;
                while (walker.next != null) {
                    walker = walker.next;
                }
                walker.next = new XMLAttribute(xmlAttribute, true);
            }
        }
    }

    /**
     * Constructs an {@link DataElement} with the given {@code tag} and {@code valueAsString} and optional {@code
     * attributes}.
     *
     * @param tag           The tag for this {@link DataElement}.
     * @param valueAsString The valueAsString for this {@link DataElement}.
     * @param attributes    Optional, {@link XMLAttribute XMLAttribute(s)} for this element.
     */
    public DataElement(String tag, String valueAsString, XMLAttribute... attributes) {
        this(tag, valueAsString, false, attributes);
    }

    /**
     * Constructs an {@link DataElement} with the given {@code tag} and {@code valueAsString} and optional {@code
     * attributes}.
     *
     * @param tag        The tag for this {@link DataElement}.
     * @param value      The valueAsString for this {@link DataElement}.
     * @param CDATA      CDATA flag, if true the valueAsString of this element will be wrapped in a CDATA tag.
     * @param attributes Optional, {@link XMLAttribute XMLAttribute(s)} for this element.
     */
    public DataElement(String tag, String value, boolean CDATA, XMLAttribute... attributes) {
        this.tag = tag;
        this.value = value;
        this.CDATA = CDATA;
        //next = null;
        valueClass = String.class;
        addAttributes(attributes);
    }

    public DataElement(DataObject value) {
        this("", value);
    }

    /**
     * Constructs an {@link DataElement} with the given {@code tag} and {@code value} and optional {@code attributes}.
     *
     * @param tag        The tag for this {@link DataElement}.
     * @param value      The value for this {@link DataElement} as an {@link DataObject}.
     * @param attributes Optional, {@link XMLAttribute XMLAttribute(s)} for this element.
     */
    public DataElement(String tag, DataObject value, XMLAttribute... attributes) {
        this.tag = tag;
        this.CDATA = false;
        this.value = value;
        //this.next = null;
        this.valueClass = DataObject.class;
        addAttributes(attributes);
    }

    public DataElement(List<DataElement> elements) {
        this("", elements);
    }

    public DataElement(String tag, List<DataElement> elements) {
        this.tag = tag;
        this.CDATA = false;
        this.value = elements;
        this.valueClass = List.class;
    }

    public DataElement(String tag, double value, XMLAttribute... attributes) {
        this.tag = tag;
        //this.next = null;
        this.valueClass = double.class;
        this.value = value;
        addAttributes(attributes);
    }

    public DataElement(String tag, double value, boolean CDATA, XMLAttribute... attributes) {
        this.tag = tag;
        this.CDATA = CDATA;
        //this.next = null;
        this.valueClass = double.class;
        this.value = value;
        addAttributes(attributes);
    }

    public DataElement(String tag, float value, XMLAttribute... attributes) {
        this.tag = tag;
        //this.next = null;
        this.valueClass = float.class;
        this.value = value;
        addAttributes(attributes);
    }

    public DataElement(String tag, float value, boolean CDATA, XMLAttribute... attributes) {
        this.tag = tag;
        this.CDATA = CDATA;
        //this.next = null;
        this.valueClass = float.class;
        this.value = value;
        addAttributes(attributes);
    }

    public DataElement(String tag, int value, XMLAttribute... attributes) {
        this.tag = tag;
        //this.next = null;
        this.valueClass = int.class;
        this.value = value;
        addAttributes(attributes);
    }

    public DataElement(String tag, int value, boolean CDATA, XMLAttribute... attributes) {
        this.tag = tag;
        this.CDATA = CDATA;
        //this.next = null;
        this.valueClass = int.class;
        this.value = value;
        addAttributes(attributes);
    }

    public static int estimateLengthXML(@NonNull DataElement element) {
        return estimateLengthXML(element, null);
    }

    public static int estimateLengthXML(@NonNull DataElement element, @Nullable AtomicInteger length) {
        if (length == null) {
            length = new AtomicInteger(0);
        }
        int len = 0;
        if (element.value == null || element.value.equals("")) {
            len += element.tag.length() + 3;// empty element, the xml will be '<"tag"/>'
        } else {
            len += (element.tag.length() * 2) + 5;// *2=(open and close tags), +5=('<'*2+'>'*2+'/'*1)
            if (element.CDATA || DataObject.hasIllegalValue(element)) {
                len += DataObject.CDATA_OPEN.length() + DataObject.CDATA_CLOSE.length();
            }
            if (element.isObject()) {
                DataObject.estimateLengthXML((DataObject) element.value, length);
            } else {
                len += element.value.toString().length();
            }
        }
        return len;
    }

    public static int estimateLengthFormattedXML(@NonNull DataElement element, int indentCount) {
        return estimateLengthFormattedXML(element, indentCount, null);
    }

    public static int estimateLengthFormattedXML(
            @NonNull DataElement element, int indentCount, @Nullable AtomicInteger length) {
        if (length == null) {
            length = new AtomicInteger(0);
        }
        int len = 0;
        if (element.value == null || element.value.equals("")) {
            len += element.tag.length() + 3;// empty element, the xml will be '<"tag"/>'
        } else {
            len += (element.tag.length() * 2) + 5;// *2=(open and close tags), +5=('<'*2+'>'*2+'/'*1)
            if (element.CDATA || DataObject.hasIllegalValue(element)) {
                len += DataObject.CDATA_OPEN.length() + DataObject.CDATA_CLOSE.length();
            }
            if (element.isObject()) {
                DataObject.estimateLengthFormattedXML((DataObject) element.value, indentCount, length);
            } else {
                len += element.value.toString().length();
            }
        }
        return len;
    }

    /**
     * True if this {@link DataElement} holds an {@link DataObject}, False if this element holds only a String value.
     *
     * @return True if this {@link DataElement} holds an {@link DataObject}, False otherwise.
     */
    public boolean isObject() {
        return value != null && valueClass == DataObject.class;
    }

    public static int estimateLengthJSON(@NonNull DataElement element, String... arrayTags) {
        return estimateLengthJSON(element, null, arrayTags);
    }

    public static int estimateLengthJSON(@NonNull DataElement element, @Nullable AtomicInteger length, String... arrayTags) {
        if (length == null) {
            length = new AtomicInteger(0);
        }
        if (element.isObject()) {
            // the 'value' is an object.
            DataObject.estimateLengthJSON(((DataObject) element.value), length, arrayTags);
            //((DataObject) value).toJSON(stringBuilder, arrayTags);
        } else if (element.isArray()) {
            List arrayList = (List) element.value;
            if (arrayList.size() > 0) {
                length.addAndGet(1);//stringBuilder.append("[");
                for (int i = 0; i < arrayList.size(); i++) {
                    Object item = arrayList.get(i);
                    if (item instanceof DataElement) {
                        estimateLengthJSON(((DataElement) item), length, arrayTags);
                        //((DataElement) item).toJSON(stringBuilder, arrayTags);
                    } else if (item instanceof DataObject) {
                        DataObject.estimateLengthJSON(((DataObject) item), length, arrayTags);
                        //((DataObject) item).toJSON(stringBuilder, arrayTags);
                    } else {
                        length.addAndGet(2 + element.value.toString().length());
                        //stringBuilder.append("\"").append(value).append("\"");
                    }
                    if (i < arrayList.size() - 1) {
                        length.addAndGet(1);//stringBuilder.append(",");
                    }
                }
                length.addAndGet(1);//stringBuilder.append("]");
            }
        } else if (element.value == null) {
            length.addAndGet(6);
            //stringBuilder.append("\"").append("null").append("\"");
        } else {
            // the 'value' is just a value.
            length.addAndGet(2 + element.value.toString().length());
            //stringBuilder.append("\"").append(value).append("\"");
        }
        return length.get();
    }

    public static int estimateLengthFormattedJSON(@NonNull DataElement element, String... arrayTags) {
        return estimateLengthFormattedJSON(element, 0, null, arrayTags);
    }

    public static int estimateLengthFormattedJSON(@NonNull DataElement element, int indentCount, String... arrayTags) {
        return estimateLengthFormattedJSON(element, indentCount, null, arrayTags);
    }

    public static int estimateLengthFormattedJSON(
            @NonNull DataElement element, int indentCount, @Nullable AtomicInteger length, String... arrayTags) {
        if (length == null) {
            length = new AtomicInteger(0);
        }
        if (element.isObject()) {
            // the 'value' is an object.
            DataObject.estimateLengthFormattedJSON(((DataObject) element.value), indentCount + 1, length, arrayTags);
            //((DataObject) value).toFormattedJSON(indentCount + 1, stringBuilder, arrayTags);
        } else if (element.isArray()) {
            /*StringBuilder indentSB = new StringBuilder();
            for (int i = 0; i < indentCount; i++) {
                indentSB.append(TAB);
            }*/
            int indentLen = TAB_LEN * indentCount;
            List arrayList = (List) element.value;
            if (arrayList.size() > 0) {
                length.addAndGet(indentLen + 1 + NEW_LINE_LEN);
                //stringBuilder.append(indentSB).append("[").append(NEW_LINE);
                for (int i = 0; i < arrayList.size(); i++) {
                    Object item = arrayList.get(i);
                    if (item instanceof DataElement) {
                        estimateLengthFormattedJSON(((DataElement) item), indentCount, length, arrayTags);
                        //((DataElement) item).toFormattedJSON(indentCount + 1, stringBuilder, arrayTags);
                    } else if (item instanceof DataObject) {
                        DataObject.estimateLengthFormattedJSON(((DataObject) item), indentCount, length, arrayTags);
                        //((DataObject) item).toFormattedJSON(indentCount + 1, stringBuilder, arrayTags);
                    } else {
                        length.addAndGet(indentLen + TAB_LEN + 2 + item.toString().length());
                        //stringBuilder.append(indentSB).append(TAB).append("\"").append(item).append("\"");
                    }
                    if (i < arrayList.size() - 1) {
                        length.addAndGet(1);//stringBuilder.append(",");
                    }
                    length.addAndGet(NEW_LINE_LEN);//stringBuilder.append(NEW_LINE);
                }
                length.addAndGet(indentLen + 1 + NEW_LINE_LEN);
                //stringBuilder.append(indentSB).append("]").append(NEW_LINE);
            }
        } else if (element.value == null) {
            length.addAndGet(6);
            //stringBuilder.append("\"").append("null").append("\"");
        } else {
            // the 'value' is just a value.
            length.addAndGet(2 + element.value.toString().length());
            //stringBuilder.append("\"").append(value).append("\"");
        }
        return length.get();
    }

    /**
     * Removes the attributes matching the given set of attribute tags.
     *
     * @param attributeTags The list of tags of attributes to be removed.
     */
    public void removeAttributes(String... attributeTags) {
        if (attributeTags != null && attributeTags.length >= 1) {
            for (String tag : attributeTags) {
                removeAttribute(tag);
            }
        }
    }

    /**
     * Removes the attribute matching the given attribute tag from this element.
     *
     * @param attributeTag the tag of the attribute to be removed.
     */
    public void removeAttribute(String attributeTag) {
        XMLAttribute walker = attributeRoot;
        XMLAttribute prev = null;
        while (walker != null && walker.next != null) {
            if (walker.getTag().equals(attributeTag)) {
                if (prev == null) {
                    attributeRoot = walker.next;
                } else {
                    prev.next = walker.next;
                }
            }
            prev = walker;
            walker = walker.next;
        }
    }

    public String getValueAsString(String defaultValue) {
        if (valueClass == String.class) {
            return (String) value;
        } else if (!isObject()) {
            try {
                return value.toString();
            } catch (Exception e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public void setValueAsString(String value) {
        this.valueClass = String.class;
        this.value = value;
    }

    public boolean isArray() {
        return value != null && valueClass == List.class;
    }

    public int getValueAsInteger(int defaultValue) {
        if (valueClass == int.class) {
            return (int) value;
        } else if (valueClass == String.class) {
            try {
                return Integer.valueOf((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public float getValueAsFloat(float defaultValue) {
        if (valueClass == float.class) {
            return (float) value;
        } else if (valueClass == String.class) {
            try {
                return Float.valueOf((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public double getValueAsDouble(double defaultValue) {
        if (valueClass == double.class) {
            return (double) value;
        } else if (valueClass == String.class) {
            try {
                return Double.valueOf((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public DataObject getValueAsObject(DataObject defaultValue) {
        if (valueClass == DataObject.class) {
            return (DataObject) value;
        } else {
            return defaultValue;
        }
    }

    /**
     * Attempts to add the given {@link DataElement} as a child to this element. If this element is not a complex element, ie
     * an element whose value is made up of other elements, and this element's value is not empty then the function will
     * <strong>not</strong> insert the element and return
     * <code>False</code>.
     *
     * @param element The {@link DataElement} to be added.
     * @return {@code True} if the given DataElement was successfully added.
     */
    public boolean insert(DataElement element) {
        if (isObject()) {
            ((DataObject) value).insert(element);
            return true;
        } else if (value == null) {
            value = new DataObject(element);
            valueClass = DataObject.class;
            return true;
        }
        return false;
    }

    /**
     * Gives the xml opening tag for this element.
     *
     * @return The xml opening tag for this element.
     */
    public String openTag() {
        return openTag(false);
    }

    /**
     * Gives the xml opening tag for this element.
     *
     * @param ignoreAttributes flag, if true attributes will be ignored.
     * @return The xml opening tag for this element.
     */
    public String openTag(boolean ignoreAttributes) {
        if (ignoreAttributes || attributeRoot == null) {
            return "<" + tag + ">";
        } else {
            StringBuilder string = new StringBuilder("<" + tag);
            for (XMLAttribute att : this) {
                string.append(" ").append(att.toString());
            }
            string.append(">");
            return string.toString();
        }
    }

    /**
     * Attempts to update the first matching {@link DataElement} within this element's {@link DataObject} with the values
     * from the given {@code element} parameter. If this element is not a complex element, ie an element whose value is made
     * up of other elements, and this element's valueAsString is not empty then the function will
     * <strong>not</strong> insert the element and return <code>False</code>.
     *
     * @param element The {@link DataElement} to update with.
     * @return {@code True} if the given DataElement was successfully updated.
     */
    public boolean update(DataElement element) {
        if (isObject()) {
            return ((DataObject) value).update(element);
        } else if (value == null) {
            this.value = new DataObject(element);
            this.valueClass = DataObject.class;
            return true;
        }
        return false;
    }

    public boolean update(String value, boolean... force) {
        if (valueClass == String.class || (force != null && force.length > 0 && force[0])) {
            this.value = value;
            this.valueClass = String.class;
            return true;
        } else {
            return false;
        }
    }

    public boolean update(int value, boolean... force) {
        if (valueClass == int.class || (force != null && force.length > 0 && force[0])) {
            this.value = value;
            this.valueClass = int.class;
            return true;
        } else {
            return false;
        }
    }

    public boolean update(float value, boolean... force) {
        if (valueClass == float.class || (force != null && force.length > 0 && force[0])) {
            this.value = value;
            this.valueClass = float.class;
            return true;
        } else {
            return false;
        }
    }

    public boolean update(double value, boolean... force) {
        if (valueClass == double.class || (force != null && force.length > 0 && force[0])) {
            this.value = value;
            this.valueClass = double.class;
            return true;
        } else {
            return false;
        }
    }

    public boolean update(DataObject value, boolean... force) {
        if (valueClass == DataObject.class || (force != null && force.length > 0 && force[0])) {
            this.value = value;
            this.valueClass = DataObject.class;
            this.CDATA = false;
            return true;
        } else {
            return false;
        }
    }

    public void updateMerge(DataObject dataObject) {
        if (this.valueClass == DataObject.class) {
            ((DataObject) value).updateMerge(dataObject);
        } else {
            valueClass = DataObject.class;
            value = new DataObject(dataObject.toXML());// to do a deep copy
        }
    }

    /**
     * Check for if this DataElement has any attributes
     *
     * @return
     */
    public boolean hasAttributes() {
        return attributeRoot != null;
    }

    public boolean hasTag(@NonNull String xmlTag) {
        return this.tag.equals(xmlTag);
    }

    /**
     * Compares this {@link DataElement} to {@code Other} {@link DataElement}
     *
     * @param other The {@link DataElement} to compare to this
     * @return {@code True} if {@code this == Other}, {@code False} otherwise.
     */
    public boolean equals(DataElement other) {
        return this.compareTo(other) == 0;
    }

    /**
     * Compares this {@link DataElement} to {@code Other} {@link DataElement}
     *
     * @param other The {@link DataElement} to compare to this
     * @return 0 if {@code this == Other}, -1 otherwise.
     */
    @Override
    public int compareTo(@NonNull DataElement other) {
        if (this.tag.equals(other.tag) && (matchAttributes(other.getAttributesAsArray())) && this.valueClass == other.valueClass) {
			/*if((!this.isObject() && this.value != null) && (!other.isObject() && other.value != null))
			{
				return (this.value.compareTo(other.value) == 0) ? 0 : -1;
			}
			else if((this.isObject() && other.value != null) && (other.isObject() && other.value != null))
			{
				return this.value.compareTo(other.value);
			}*/
            if (valueClass == String.class) {
                return this.value.equals(other.value) ? 0 : -1;
            } else if (valueClass == DataObject.class) {
                return ((DataObject) this.value).compareTo((DataObject) other.value);
            } else if (valueClass == int.class) {
                return ((int) this.value) == ((int) other.value) ? 0 : -1;
            } else if (valueClass == float.class) {
                return ((float) this.value) == ((float) other.value) ? 0 : -1;
            } else if (valueClass == double.class) {
                return ((double) this.value) == ((double) other.value) ? 0 : -1;
            }
        }
        return -1;
    }

    /**
     * Checks if this {@link DataElement DataElement's} set of {@link XMLAttribute XMLAttributes} exactly match the given
     * {@code attributes} set.
     *
     * @param attributes The {@link XMLAttribute} set to compare against.
     * @return {@code True} if this element's attribute set exactly matches the given attribute set, {@code False}
     *         otherwise.
     */
    public boolean matchAttributes(XMLAttribute... attributes) {
        if ((attributes == null || attributes.length <= 0) && this.attributeRoot == null)/*you gave me nothing and I've got nothing.*/ {
            return true;
        }
        if ((attributes == null || attributes.length <= 0) && this.attributeRoot != null)/* you gave me nothing but I've got something.*/ {
            return true;
        }
        if ((attributes != null && attributes.length > 0) && this.attributeRoot == null)/* you gave me something but I have nothing.*/ {
            return false;
        }
        if (getAttributesAsArray().length == attributes.length)/* you gave me the same number of somethings that I have.*/ {
            if (hasAttribute(attributes))/*I have all the things you gave me.*/ {
                return true;
            }
        }
        return false;
    }

    /**
     * Constructs and returns an {@link XMLAttribute XMLAttribute[]} array containing all the Attributes of this element.
     *
     * @return an {@link XMLAttribute XMLAttribute[]} array containing all the Attributes of this element.
     */
    public XMLAttribute[] getAttributesAsArray() {
        if (attributeRoot == null) {
            return null;
        }
        List<XMLAttribute> list = new ArrayList<>();
        for (XMLAttribute attribute : this) {
            list.add(attribute);
        }
        return list.toArray(new XMLAttribute[list.size()]);
    }

    /**
     * Checks if this {@link DataElement} contains all of the given {@link XMLAttribute XMLAttributes}.
     *
     * @param attributes The {@link XMLAttribute XMLAttributes} to check for.
     * @return {@code True} if this {@link DataElement} contains all of the given {@link XMLAttribute XMLAttributes}, {@code
     *         False} otherwise.
     */
    public boolean hasAttribute(XMLAttribute... attributes) {
        if (attributes == null || attributes.length <= 0) {
            return true;
        }
        for (XMLAttribute attribute : attributes) {
            if (!hasAttribute(attribute)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if this {@link DataElement} contains the given {@link XMLAttribute}.
     *
     * @param attribute The {@link XMLAttribute} to check for.
     * @return {@code True} if this {@link DataElement} contains the given {@link XMLAttribute}, {@code False} otherwise.
     */
    public boolean hasAttribute(XMLAttribute attribute) {
        for (XMLAttribute att : this) {
            if (att.equals(attribute)) {
                return true;
            }
        }
        return false;
    }

    public DataElement copy() {
        DataElement el = new DataElement(tag, getAttributesAsArray());
        el.value = value;
        el.valueClass = valueClass;
        el.CDATA = CDATA;
        return el;
    }

    /**
     * Gives the xml closing tag for this element.
     *
     * @return The xml closing tag for this element.
     */
    public String closeTag() {
        return "</" + tag + ">";
    }

    /**
     * Constructs the xml Code representing this Element as a String
     *
     * @return the xml Code representing this Element as a String
     */
    @Override
    public String toString() {
        return toXML();
    }

    public String toXML() {
        return toXML(new StringBuilder(estimateLengthXML()));
    }

    public String toXML(@Nullable StringBuilder stringBuilder) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder(estimateLengthXML());
        }
        stringBuilder.append("<").append(tag);
        if (attributeRoot != null) {
            for (XMLAttribute att : this) {
                stringBuilder.append(" ").append(att.toString());
            }
        }
        if (isObject()) {
            stringBuilder.append(">");
            ((DataObject) value).toXML(stringBuilder);
            stringBuilder.append(closeTag());
        } else {
            if (value != null && value.equals("")) {
                stringBuilder.append("/>");
            } else {
                stringBuilder.append(">");
                if (CDATA || DataObject.hasIllegalValue(this)) {
                    stringBuilder.append(DataObject.CDATA_OPEN).append(value).append(DataObject.CDATA_CLOSE);
                } else {
                    stringBuilder.append(value);
                }
                stringBuilder.append(closeTag());
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Constructs and returns a formatted xml string representing this {@link DataElement}.
     *
     * @return a formatted xml string representing this {@link DataElement}.
     */
    public String toFormattedXML() {
        return toFormattedXML(0);
    }

    /**
     * Constructs and returns a formatted xml string representing this {@link DataElement}.
     *
     * @param indentCount The starting indentation level.
     * @return a formatted xml string representing this {@link DataElement}.
     */
    public String toFormattedXML(int indentCount) {
        StringBuilder string = new StringBuilder(estimateLengthFormattedXML());
        StringBuilder indent = new StringBuilder(TAB.length() * indentCount);
        for (int i = 0; i < indentCount; i++) {
            indent.append(TAB);
        }
        string.append(indent).append("<").append(tag);
        for (XMLAttribute att : this) {
            string.append(" ").append(att.toString());
        }
        string.append(">");
        if (isObject()) {
            string.append(NEW_LINE)
                  .append(((DataObject) value).toFormattedXML(++indentCount))
                  .append(indent);
            string.append(NEW_LINE)
                  .append(indent)
                  .append("</")
                  .append(tag)
                  .append(">");
        } else {
            if (CDATA) {
                string.append(DataObject.CDATA_OPEN).append(value).append(DataObject.CDATA_CLOSE);
            } else {
                string.append(value);
            }
            string.append("</").append(tag).append(">");
        }
        return string.toString();
    }

    public int estimateLengthFormattedXML() {
        return estimateLengthFormattedXML(this);
    }

    public static int estimateLengthFormattedXML(@NonNull DataElement element) {
        return estimateLengthFormattedXML(element, 0, null);
    }

    /**
     * Outputs this {@link DataElement} as a formatted JSON String.
     *
     * @param arrayTags (Optional) Set of tags to ensure are output as JSON Array even if there is only a single element.
     * @return {@link String} This {@link DataElement} as JSON.
     */
    public String toJSON(String... arrayTags) {
        return toJSON(new StringBuilder(estimateLengthJSON()), arrayTags);
    }

    /**
     * Outputs this {@link DataElement} as a formatted JSON String.
     *
     * @param stringBuilder {@link StringBuilder} Nullable.
     * @param arrayTags     (Optional) Set of tags to ensure are output as JSON Array even if there is only a single
     *                      element.
     * @return {@link String} This {@link DataElement} as JSON.
     */
    public String toJSON(@Nullable StringBuilder stringBuilder, String... arrayTags) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder(estimateLengthJSON());
        }
        if (isObject()) {
            // the 'value' is an object.
            ((DataObject) value).toJSON(stringBuilder, arrayTags);
        } else if (isArray()) {
            List arrayList = (List) this.value;
            if (arrayList.size() > 0) {
                stringBuilder.append("[");
                for (int i = 0; i < arrayList.size(); i++) {
                    Object item = arrayList.get(i);
                    if (item instanceof DataElement) {
                        ((DataElement) item).toJSON(stringBuilder, arrayTags);
                    } else if (item instanceof DataObject) {
                        ((DataObject) item).toJSON(stringBuilder, arrayTags);
                    } else {
                        stringBuilder.append("\"").append(item).append("\"");
                    }
                    if (i < arrayList.size() - 1) {
                        stringBuilder.append(",");
                    }
                }
                stringBuilder.append("]");
            }
        } else if (value == null) {
            stringBuilder.append("\"").append("null").append("\"");
        } else {
            // the 'value' is just a value.
            if (valueClass == String.class) {
                String quote = JSONObject.quote((String) value);//escapes all necessary characters and wraps with quotes("")
                quote = quote.substring(1, quote.length() - 1);//removes the quotes("") added above
                stringBuilder.append("\"").append(quote).append("\"");
            } else {
                stringBuilder.append("\"").append(value).append("\"");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Outputs this {@link DataElement} as a formatted JSON String.
     *
     * @param arrayTags (Optional) Set of tags to ensure are output as JSON Array even if there is only a single element.
     * @return {@link String} This {@link DataElement} as JSON.
     */
    public String toFormattedJSON(String... arrayTags) {
        return toFormattedJSON(0, new StringBuilder(estimateLengthFormattedJSON()), arrayTags);
    }

    /**
     * Outputs this {@link DataElement} as a formatted JSON String.
     *
     * @param arrayTags (Optional) Set of tags to ensure are output as JSON Array even if there is only a single element.
     * @return {@link String} This {@link DataElement} as JSON.
     */
    public String toFormattedJSON(int indentCount, String... arrayTags) {
        return toFormattedJSON(indentCount, new StringBuilder(estimateLengthFormattedJSON()), arrayTags);
    }

    /**
     * Outputs this {@link DataElement} as a formatted JSON String.
     *
     * @param stringBuilder {@link StringBuilder} Nullable.
     * @param arrayTags     (Optional) Set of tags to ensure are output as JSON Array even if there is only a single
     *                      element.
     * @return {@link String} This {@link DataElement} as JSON.
     */
    public String toFormattedJSON(int indentCount, @Nullable StringBuilder stringBuilder, String... arrayTags) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder(estimateLengthFormattedJSON());
        }
        if (isObject()) {
            // the 'value' is an object.
            ((DataObject) value).toFormattedJSON(indentCount + 1, stringBuilder, arrayTags);
        } else if (isArray()) {
            StringBuilder indentSB = new StringBuilder();
            for (int i = 0; i < indentCount; i++) {
                indentSB.append(TAB);
            }
            List arrayList = (List) this.value;
            if (arrayList.size() > 0) {
                stringBuilder.append(indentSB).append("[").append(NEW_LINE);
                for (int i = 0; i < arrayList.size(); i++) {
                    Object item = arrayList.get(i);
                    if (item instanceof DataElement) {
                        ((DataElement) item).toFormattedJSON(indentCount + 1, stringBuilder, arrayTags);
                    } else if (item instanceof DataObject) {
                        ((DataObject) item).toFormattedJSON(indentCount + 1, stringBuilder, arrayTags);
                    } else {
                        stringBuilder.append(indentSB).append(TAB).append("\"").append(item).append("\"");
                    }
                    if (i < arrayList.size() - 1) {
                        stringBuilder.append(",");
                    }
                    stringBuilder.append(NEW_LINE);
                }
                stringBuilder.append(indentSB).append("]").append(NEW_LINE);
            }
        } else if (value == null) {
            stringBuilder.append("\"").append("null").append("\"");
        } else {
            // the 'value' is just a value.
            if (valueClass == String.class) {
                String quote = JSONObject.quote((String) value);//escapes all necessary characters and wraps with quotes("")
                quote = quote.substring(1, quote.length() - 1);//removes the quotes("") added above
                stringBuilder.append("\"").append(quote).append("\"");
            } else {
                stringBuilder.append("\"").append(value).append("\"");
            }
        }
        return stringBuilder.toString();
    }

    public int estimateLengthXML() {
        return estimateLengthXML(this);
    }

    public int estimateLengthJSON() {
        return estimateLengthJSON(this);
    }

    public int estimateLengthFormattedJSON() {
        return estimateLengthFormattedJSON(this);
    }

    /**
     * Returns an iterator over elements of type {@link XMLAttribute}.
     *
     * @return an Iterator.
     */
    @NonNull
    @Override
    public Iterator<XMLAttribute> iterator() {
        return new Iterator<XMLAttribute>() {
            XMLAttribute cur = attributeRoot;
            boolean firstEl = true;
            XMLAttribute prev = null;

            @Override
            public boolean hasNext() {
                return (cur != null) && (firstEl || cur.next != null);
            }

            @Override
            public XMLAttribute next() {
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
             * WARNING calling remove will remove the element from the original source as well!
             */
            @Override
            public void remove() {
                if (prev == null) {
                    attributeRoot = cur.next;
                    cur = attributeRoot;
                } else {
                    prev.next = cur.next;
                    cur = prev.next;
                }
            }
        };
    }

    public DataElement merge(DataElement other) {
        if (other != null) {
            if (other.valueClass == valueClass) {
                // we are the same type
                if (valueClass == DataObject.class) {
                    value = ((DataObject) value).merge(((DataObject) other.value));
                } else {
                    value = other.value;
                }
            }
        }
        return this;
    }
//
//    /**
//     * This method will use the provided {@link EncryptionUtil.EncryptionInterface#encrypt(Context, String)} to encrypt the
//     * value of
//     * each element within
//     * this Object.
//     *
//     * @param encryptor {@link EncryptionUtil.EncryptionInterface} to be used for the actual encryption.
//     */
//    public void encryptValues(Context context, EncryptionUtil.EncryptionInterface encryptor) {
//
//        if (isObject()) {
//            getValueAsObject(new DataObject()).encryptValues(context, encryptor);
//        } else if (isArray()) {
//            List<DataElement> elements = (List<DataElement>) this.value;
//            for (DataElement element : elements) {
//                element.encryptValues(context, encryptor);
//            }
//        } else {
//            value = encryptor.encrypt(context, (String) value);
//            valueClass = String.class;
//        }
//    }
//
//    public void decryptValues(Context context, EncryptionUtil.EncryptionInterface encryptor) {
//
//        if (isObject()) {
//            getValueAsObject(new DataObject()).decryptValues(context, encryptor);
//        } else if (isArray()) {
//            List<DataElement> elements = (List<DataElement>) this.value;
//            for (DataElement element : elements) {
//                element.decryptValues(context, encryptor);
//            }
//        } else {
//            value = encryptor.decrypt(context, (String) value);
//        }
//    }
}
