package org.decisiongram.data;

/**
 * Created by davide on 01/10/15.
 */
public class TextOption extends Option {

    // icon/image will be retrived externally by url or file

    private String title;
    private String notes;

    public TextOption(String title, String notes, long decisionId) {
        super(decisionId);
        this.title = title;
        this.notes = notes;
    }

    public TextOption(long id, String title, String notes, long decisionId) {
        this(title, notes,decisionId);
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

    public String getNotes() {
        return notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextOption)) return false;

        TextOption that = (TextOption) o;

        if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null)
            return false;
        return equalString(getNotes(), that.getNotes());

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getNotes() != null ? getNotes().hashCode() : 0);
        return result;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }



    @Override
    public String toString() {
        return title;
    }
}
