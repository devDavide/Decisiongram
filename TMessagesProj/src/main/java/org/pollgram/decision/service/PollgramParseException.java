package org.pollgram.decision.service;

/**
 * Created by davide on 27/10/15.
 */
public class PollgramParseException extends Exception {

    public PollgramParseException(String detailMessage) {
        super(detailMessage);
    }

    public PollgramParseException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
