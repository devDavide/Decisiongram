package org.pollgram.decision.dao;

/**
 * Created by davide on 27/10/15.
 */
public class PollgramDAOException extends RuntimeException {

    public PollgramDAOException(String detailMessage) {
        super(detailMessage);
    }

    public PollgramDAOException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
