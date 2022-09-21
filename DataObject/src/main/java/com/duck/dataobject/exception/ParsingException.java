package com.duck.dataobject.exception;

/**
 * Created by Bradley Duck on 2017/04/03.
 */

public class ParsingException extends RuntimeException {
    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
