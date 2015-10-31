package org.pollgram.decision.data;

/**
 * Created by davide on 03/10/15.
 */
public abstract class Option implements Comparable<Option> {

    protected final long id;
    private final int positiveVoteCount;
    private final int negativeVoteCount;

    public Option(long id, int positiveVoteCount, int negativeVoteCount) {
        this.id = id;
        this.positiveVoteCount = positiveVoteCount;
        this.negativeVoteCount = negativeVoteCount;
    }

    public long getId() {
        return id;
    }

    public int getPositiveVoteCount() {
        return positiveVoteCount;
    }

    public int getNegativeVoteCount() {
        return negativeVoteCount;
    }

    public abstract String getTitle();

    @Override
    public int compareTo(Option another) {
        return this.positiveVoteCount - another.positiveVoteCount;
    }

}
