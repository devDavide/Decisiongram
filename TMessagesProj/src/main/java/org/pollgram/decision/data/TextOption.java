package org.pollgram.decision.data;

/**
 * Created by davide on 01/10/15.
 */
public class TextOption extends Option {

    private final long decisionId;

    // icon/image will be retrived externally by url or file

    private String title;
    private String longDescription;

    public TextOption(String title, String longDescription, long decisionId) {
        this.title = title;
        this.decisionId = decisionId;
        this.longDescription = longDescription;
    }

    public TextOption(long id, String title, String longDescription, long decisionId) {
        this(title,longDescription,decisionId);
        setId(id);
    }

    public long getDecisionId() {
        return decisionId;
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
    public String toString() {
        return title;
    }
}
