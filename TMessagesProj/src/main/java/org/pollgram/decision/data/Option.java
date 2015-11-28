package org.pollgram.decision.data;

/**
 * Created by davide on 03/10/15.
 */
public abstract class Option extends DBBean {

    private long decisionId;

    protected Option(long decisionidPar) {
        decisionId = decisionidPar;
    }

    public long getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(long decisionId) {
        this.decisionId = decisionId;
    }

    public abstract String getTitle();



}
