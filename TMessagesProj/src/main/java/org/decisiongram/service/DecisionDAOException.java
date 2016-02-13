package org.decisiongram.service;

/**
 * Created by davide on 27/10/15.
 */
public class DecisionDAOException extends RuntimeException {

    public DecisionDAOException(String detailMessage) {
        super(detailMessage);
    }

    public DecisionDAOException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
