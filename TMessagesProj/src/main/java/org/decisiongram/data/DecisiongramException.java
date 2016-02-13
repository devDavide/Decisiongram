package org.decisiongram.data;

/**
 * Created by davide on 07/01/16.
 */
public class DecisiongramException extends  Exception {

    public DecisiongramException(String detailMessage) {
        super(detailMessage);
    }

    public DecisiongramException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
