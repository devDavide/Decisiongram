package org.pollgram.decision.data;

import java.util.Date;

/**
 * Created by davide on 01/10/15.
 */
public class Decision extends  DBBean {

    private final Date creationDate;
    private String title;
    private final String longDescription;
    private final long groupCharId;
    private final int userCreatorId;
    private boolean open;

    // icon/image will be retrived externally by url or file.
    // Like the file containing the decision image can be named like decisio_<id>.png, and cached on the fs

    public Decision(long groupCharId, int userCreatorId, String title, String longDescription,
                    Date creationDate, boolean open) {
        this.groupCharId = groupCharId;
        this.title = title;
        this.longDescription = longDescription;
        this.userCreatorId = userCreatorId;
        this.creationDate = creationDate;
        this.open = open;
    }

    public Decision(long id, long groupCharId, int userCreatorId, String title, String longDescription,
                    Date creationDate, boolean open) {
        this(groupCharId,userCreatorId,title, longDescription, creationDate, open);
        setId(id);

    }

    /*
     * @return the id of an TLRPC.ChatFull
     */
    public long getChatId() {
        return groupCharId;
    }

    /**
     * @return the id of the user that create the current decision.
     * This id referes to TLRPC.User
     */
    public int getUserCreatorId() {
        return userCreatorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public boolean isOpen() {
        return open;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Decision)) return false;

        Decision decision = (Decision) o;

        if (groupCharId != decision.groupCharId) return false;
        if (getUserCreatorId() != decision.getUserCreatorId()) return false;
        if (isOpen() != decision.isOpen()) return false;
        if (getTitle() != null ? !getTitle().equals(decision.getTitle()) : decision.getTitle() != null)
            return false;
        return equalString(getLongDescription(), decision.getLongDescription());

    }

    private boolean equalString(String s1, String s2) {
        boolean isS1Emtpy = s1 == null || s1.trim().isEmpty() || s1.equals(""+null);
        boolean isS2Emtpy = s2 == null || s2.trim().isEmpty() || s2.equals(""+null);
        if (isS1Emtpy && isS2Emtpy)
            return true;
        else
            return s1.equals(s2);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getLongDescription() != null ? getLongDescription().hashCode() : 0);
        result = (int) (31 * result + groupCharId);
        result = 31 * result + (int) (getUserCreatorId() ^ (getUserCreatorId() >>> 32));
        result = 31 * result + (isOpen() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Decision{" +
                "groupCharId=" + groupCharId +
                ", userCreatorId=" + userCreatorId +
                ", longDescription='" + longDescription + '\'' +
                ", title='" + title + '\'' +
                ", open=" + open +
                '}';
    }


}
