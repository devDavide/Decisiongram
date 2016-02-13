package org.decisiongram.service;

/**
 * Created by davide on 27/10/15.
 */
public class MessageParseException extends Exception {

    public MessageParseException(String detailMessage) {
        super(detailMessage);
    }

    public MessageParseException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
