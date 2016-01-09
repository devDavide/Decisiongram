package org.pollgram.decision.data;

/**
 * Created by davide on 01/10/15.
 */
public class TextOption extends Option {

    // icon/image will be retrived externally by url or file

    private String title;
    private String longDescription;

    public TextOption(String title, String longDescription, long decisionId) {
        super(decisionId);
        this.title = title;
        this.longDescription = longDescription;
    }

    public TextOption(long id, String title, String longDescription, long decisionId) {
        this(title,longDescription,decisionId);
        setId(id);
    }

    public TextOption() {
        this(null,null,ID_NOT_SET);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextOption)) return false;

        TextOption that = (TextOption) o;

        if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null)
            return false;
        return equalString(getLongDescription(), that.getLongDescription());

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getLongDescription() != null ? getLongDescription().hashCode() : 0);
        return result;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }



    @Override
    public String toString() {
        return title;
    }
}
