package com.duck.dataobject.node;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Bradley Duck on 2017/03/31.
 * <p>
 * Node class for XML Attributes.
 */
public class XMLAttribute implements Comparable<XMLAttribute> {

    private static final String LOGTAG = "XMLAttribute LOG";
    public XMLAttribute next = null;
    private String tag;
    private String value;

    /**
     * Constructs an {@link XMLAttribute} using the given tag and value.
     *
     * @param tag   the attribute tag.
     * @param value the attribute value as a {@link String}.
     */
    public XMLAttribute(@NonNull String tag, @NonNull String value) {
        this(tag, value, null);
    }

    /**
     * Constructs an {@link XMLAttribute} using the given tag and value and sets the next field as the {@code next}
     * parameter.
     *
     * @param tag   the attribute tag.
     * @param value the attribute value as a {@link String}.
     * @param next  the attribute to ad as next.
     */
    public XMLAttribute(@NonNull String tag, @NonNull String value, @Nullable XMLAttribute next) {
        this.tag = tag;
        this.value = value;
        this.next = next;
    }

    /**
     * Constructs an {@link XMLAttribute} using the given tag and value.
     *
     * @param tag   the attribute tag.
     * @param value the attribute value as a {@link Integer}.
     */
    public XMLAttribute(@NonNull String tag, @NonNull Integer value) {
        this(tag, value, null);
    }

    /**
     * Constructs an {@link XMLAttribute} using the given tag and value and sets the next field as the {@code next}
     * parameter.
     *
     * @param tag   the attribute tag.
     * @param value the attribute value as a {@link Integer}.
     * @param next  the attribute to ad as next.
     */
    public XMLAttribute(@NonNull String tag, @NonNull Integer value, @Nullable XMLAttribute next) {
        this.tag = tag;
        this.value = value.toString();
        this.next = next;
    }

    /**
     * Constructs an {@link XMLAttribute} using the given tag and value.
     *
     * @param tag   the attribute tag.
     * @param value the attribute value as a {@link Double}.
     */
    public XMLAttribute(@NonNull String tag, @NonNull Double value) {
        this(tag, value, null);
    }

    /**
     * Constructs an {@link XMLAttribute} using the given tag and value and sets the next field as the {@code next}
     * parameter.
     *
     * @param tag   the attribute tag.
     * @param value the attribute value as a {@link Double}.
     * @param next  the attribute to ad as next.
     */
    public XMLAttribute(@NonNull String tag, @NonNull Double value, @Nullable XMLAttribute next) {
        this.tag = tag;
        this.value = value.toString();
        this.next = next;
    }

    /**
     * Constructs an {@link XMLAttribute} using the given tag and value.
     *
     * @param tag   the attribute tag.
     * @param value the attribute value as a {@link Float}.
     */
    public XMLAttribute(@NonNull String tag, @NonNull Float value) {
        this(tag, value, null);
    }

    /**
     * Constructs an {@link XMLAttribute} using the given tag and value and sets the next field as the {@code next}
     * parameter.
     *
     * @param tag   the attribute tag.
     * @param value the attribute value as a {@link Float}.
     * @param next  the attribute to ad as next.
     */
    public XMLAttribute(@NonNull String tag, @NonNull Float value, @Nullable XMLAttribute next) {
        this.tag = tag;
        this.value = value.toString();
        this.next = next;
    }

    /**
     * Constructs an {@link XMLAttribute} using the given tag and value.
     *
     * @param tag   the attribute tag.
     * @param value the attribute value as a {@link Boolean}.
     */
    public XMLAttribute(@NonNull String tag, @NonNull Boolean value) {
        this(tag, value, null);
    }

    /**
     * Constructs an {@link XMLAttribute} using the given tag and value and sets the next field as the {@code next}
     * parameter.
     *
     * @param tag   the attribute tag.
     * @param value the attribute value as a {@link Boolean}.
     * @param next  the attribute to ad as next.
     */
    public XMLAttribute(@NonNull String tag, @NonNull Boolean value, @Nullable XMLAttribute next) {
        this.tag = tag;
        this.value = value.toString();
        this.next = next;
    }

    /**
     * Copy constructor, constructs an {@link XMLAttribute} identical to the given {@link XMLAttribute}.
     *
     * @param other the other XMLAttribute to be copied.
     */
    public XMLAttribute(@NonNull XMLAttribute other, boolean... keeplinks) {
        this.tag = other.tag;
        this.value = other.value;
        if (other.next != null && keeplinks != null && keeplinks[0]) {

            this.next = new XMLAttribute(other.next);
        } else {
            this.next = null;
        }
    }

    public String getTag() {
        return tag;
    }

    public String getValueAsString() {
        return value;
    }

    public Integer getValueAsInteger(int defaultValue) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Double getValueAsDouble(double defaultValue) {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Float getValueAsFloat(float defaultValue) {
        try {
            return Float.valueOf(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getValueAsBoolean(boolean defaultValue) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1")) {
            return true;
        } else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("0")) {
            return false;
        } else {
            return defaultValue;
        }
    }

    /**
     * constructs an xml valid String representation of this {@link XMLAttribute}.
     *
     * @return an xml valid String representation of this {@link XMLAttribute}.
     */
    @Override
    public String toString() {
        return tag + "=\"" + value + "\"";
    }

    @Override
    public int compareTo(@NonNull XMLAttribute other) {
        return this.equals(other) ? 0 : -1;
    }

    public boolean equals(@NonNull XMLAttribute other) {
        return (this.tag.equals(other.tag) && this.value.equals(other.value));
    }
}
