package org.pollgram.decision.data;

/**
 * Created by davide on 01/10/15.
 */
public class TextOption extends Choice {

    private final Decision decision;

    // icon/image will be retrived externally by url or file

    private String title;
    private String longDescription;

    public TextOption(long id, int positiveVoteCount, int negativeVoteCount, Decision decision) {
        super(id, positiveVoteCount, negativeVoteCount);
        this.decision = decision;
    }

    public TextOption(long id, int positiveVoteCount, int negativeVoteCount, Decision decision, String title, String longDescription) {
        super(id, positiveVoteCount, negativeVoteCount);
        this.title = title;
        this.decision = decision;
        this.longDescription = longDescription;
    }

    public Decision getDecision() {
        return decision;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextOption textOption = (TextOption) o;

        return id == textOption.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return title;
    }
}
