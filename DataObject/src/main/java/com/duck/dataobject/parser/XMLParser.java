package com.duck.dataobject.parser;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.duck.dataobject.DataObject;
import com.duck.dataobject.exception.ParsingException;
import com.duck.dataobject.node.DataElement;
import com.duck.dataobject.node.XMLAttribute;

/**
 * Created by Bradley Duck on 2017/04/01
 */

public class XMLParser extends Parser {
    private static final String LOGTAG = "XMLParser LOG";

    /**
     * Parses the given XML String into the given {@link DataObject}.
     *
     * @param xml        The XML String to parse
     * @param dataObject The {@link DataObject} to parse into.
     * @throws ParsingException if the given XML is malformed and invalid.
     */
    protected static void parse(@NonNull String xml, @NonNull DataObject dataObject) throws ParsingException {
        if (verbose) {
            Log.v(LOGTAG,
                  "Parser starting, verbose is on, ignoreAttributes is " + ((ignoreAttributes) ? "on" :
                                                                            "off") + ".\nxml String to parse: " + xml);
        }
        if (!xml.isEmpty() && xml.contains("<") && xml.contains(">") && xml.contains("</")) {
            //check for and remove DocType Declaration
            if (xml.contains(DataObject.DOC_OPEN)) {
                xml = stripDocType(xml);
            }
            //check for and remove comments
            if (xml.contains(DataObject.COMMENT_OPEN)) {
                xml = stripComments(xml);
            }
            //begin parsing
            for (int i = 0; i < xml.length(); i++) {
                if (xml.charAt(i) == '<') {
                    if (xml.charAt(i + 1) == '/') {
                        Log.wtf(LOGTAG, "WTF we should not be here");
                        return;
                    }
                    i = readEl(i, dataObject, xml);
                }
            }
        }
    }

    /**
     * private helper function to assist in parsing process. reads and processes an XML tag starting at the given index
     *
     * @param i          The index to start reading.
     * @param dataObject The {@link DataObject} to save the read element into.
     * @param xml        The XML String to process.
     * @return The index after reading.
     */
    private static int readEl(int i, @NonNull DataObject dataObject, @NonNull String xml) throws ParsingException {
        StringBuilder openingTag = new StringBuilder();
        //read opening tag with attributes if there are
        for (; i < xml.length(); i++) {
            if (!(xml.charAt(i) == '<' || xml.charAt(i) == '>')) {
                openingTag.append(xml.charAt(i));
            } else if (xml.charAt(i) == '>') {
                i++;
                break;
            }
        }
        DataElement el;
        int t = 0;
        if (openingTag.toString().contains("/")) {
            el = startEl(openingTag.substring(0, openingTag.indexOf("/")));
            //add the element to the xml structure
            dataObject.insert(el);
        } else {
            el = startEl(openingTag.toString());
            //read up to the closing tag that matches the opening tag
            t = readToCloseTag(el, xml.substring(i), dataObject);
        }
        return i + t - 1;
    }

    /**
     * Creates an element with only the tag and attributes if there are any.
     *
     * @param tag The tag and attributes string, the xml opening tag without the "&lt;" "&gt;"
     * @return The created {@link DataElement}.
     */
    private static DataElement startEl(String tag) {
        if (!tag.contains(" ")) {
            return new DataElement(tag);
        }
        if (ignoreAttributes) {
            return new DataElement(tag.substring(0, tag.indexOf(' ')));
        }
        String attributesString = tag.substring(tag.indexOf(' ') + 1);
        String Tag = tag.substring(0, tag.indexOf(' '));
        XMLAttribute attributeRoot = separateAttributes(attributesString);
        return new DataElement(Tag, attributeRoot);
    }

    /**
     * creates an {@link XMLAttribute} for each attribute in the given {@code attributes} String.
     *
     * @param attributes The attribute string.
     * @return The rood of the constructed {@link XMLAttribute} Linked list;
     */
    @Nullable
    private static XMLAttribute separateAttributes(String attributes) {
        if (!(attributes.contains("=") && (attributes.contains("\"") || attributes.contains("\'")))) {
            return null;
        }
        String attribute;
        boolean more = false;
        if (attributes.contains(" ")) {
            more = true;
            attribute = attributes.substring(0, attributes.indexOf(' '));
        } else {
            attribute = attributes;
        }

        String attributeTag = attribute.substring(0, attribute.indexOf('='));
        String attributeValue;
        if (attribute.charAt(attribute.indexOf('=') + 1) == '\'') {
            attributeValue = attribute.substring(attribute.indexOf('\'') + 1, attribute.lastIndexOf('\''));
        } else {
            attributeValue = attribute.substring(attribute.indexOf('\"') + 1, attribute.lastIndexOf('\"'));
        }

        if (more) {
            return new XMLAttribute(attributeTag,
                                    attributeValue,
                                    separateAttributes(attributes.substring(attributes.indexOf(' ') + 1)));
        } else {
            return new XMLAttribute(attributeTag, attributeValue);
        }
    }

    private static int readToCloseTag(@NonNull DataElement element, @NonNull String xml, DataObject dataObject)
            throws ParsingException {
        if (xml.length() >= 1) {
            String openingTag = "<" + element.tag + ">";
            String closingTag = element.closeTag();
            int end = xml.indexOf(closingTag), index = 0;
            while (xml.substring(index == 0 ? index : index + openingTag.length()).contains(openingTag)) {
                index = xml.indexOf(openingTag, index == 0 ? index : index + openingTag.length());
                if (end < index) {
                    //found closing tag before duplicate openingTag
                    break;
                } else {
                    end = xml.indexOf(closingTag, end + closingTag.length());
                }
            }
            if (end == -1) {
                throw new ParsingException("Invalid XML. Could not find closing tag for " + element.openTag());
            }
            int finalEnd = end;
            processContent(element, xml.substring(0, finalEnd), dataObject);
            return end + closingTag.length();
        }
        return 0;
    }

    private static void processContent(@NonNull DataElement element, @NonNull String contents, DataObject dataObject)
            throws ParsingException {
        if (contents.contains("<")) {
            while (!contents.startsWith("<")) {
                contents = contents.substring(1);
            }
            if (contents.startsWith(DataObject.CDATA_OPEN)) {
                element.update(extractCDATA(contents), true);
                element.CDATA = true;
            } else {
                element.update(new DataObject(contents, ignoreAttributes, verbose), true);
            }
        } else {
            element.update(contents, true);
        }
        dataObject.insert(element);
    }

    private static String extractCDATA(String cdataString) {
        if (verbose) {
            Log.v(LOGTAG, "Extracting CDATA, recieved: " + cdataString);
        }
        String str = cdataString.substring(cdataString.indexOf(DataObject.CDATA_OPEN) + 9,
                                           cdataString.indexOf(DataObject.CDATA_CLOSE));
        if (verbose) {
            Log.v(LOGTAG, "Extracting CDATA, extracted: " + str);
        }
        return str;
    }

    private static String stripDocType(String xml) throws ParsingException {
        if (xml.contains(DataObject.DOC_OPEN)) {
            int startIndex = xml.indexOf(DataObject.DOC_OPEN);
            int endIndex = xml.indexOf(DataObject.DOC_CLOSE);
            if (endIndex == -1) {
                throw new ParsingException("Invalid XML: No closing brace on Doctype Declaration: " + print(startIndex,
                                                                                                            xml));
            }
            xml = xml.substring(0, startIndex) + xml.substring(endIndex + 2);
            if (xml.contains(DataObject.DOC_OPEN)) {
                xml = stripDocType(xml);
            }
        }
        return xml;
    }

    private static String stripComments(String xml) throws ParsingException {
        if (xml.contains(DataObject.COMMENT_OPEN)) {
            int startIndex = xml.indexOf(DataObject.COMMENT_OPEN);
            int endIndex = xml.indexOf(DataObject.COMMENT_CLOSE, startIndex);
            if (endIndex == -1) {
                throw new ParsingException("Invalid XML: No closing brace for comment: " + print(startIndex, xml));
            }
            xml = xml.substring(0, startIndex) + xml.substring(endIndex + 3);
            if (xml.contains(DataObject.COMMENT_OPEN)) {
                xml = stripComments(xml);
            }
        }
        return xml;
    }

    private static String print(int start, String xml) {
        final int max = 50;
        if (xml.length() - start <= max) {
            return xml.substring(start, xml.length());
        } else {
            return xml.substring(start, max + start) + "...";
        }
    }
}
