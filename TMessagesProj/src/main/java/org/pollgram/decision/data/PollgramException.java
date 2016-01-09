package org.pollgram.decision.data;

/**
 * Created by davide on 07/01/16.
 */
public class PollgramException extends  Exception {

    public PollgramException(String detailMessage) {
        super(detailMessage);
    }

    public PollgramException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
