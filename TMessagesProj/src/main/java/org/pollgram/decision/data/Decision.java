package org.pollgram.decision.data;

/**
 * Created by davide on 01/10/15.
 */
public class Decision extends  DBBean {

    private String title;
    private final String longDescription;
    private final int fullChatId;
    private final long userCreatorId;
    private boolean open;

    // icon/image will be retrived externally by url or file.
    // Like the file containing the decision image can be named like decisio_<id>.png, and cached on the fs

    public Decision(int fullChatId, long userCreatorId, String title, String longDescription,
                    boolean open) {
        this.fullChatId = fullChatId;
        this.userCreatorId = userCreatorId;
        this.title = title;
        this.longDescription = longDescription;
        this.open = open;
    }

    public Decision(long id, int fullChatId, long userCreatorId, String title, String longDescription,
                    boolean open) {
        this(fullChatId,userCreatorId,title, longDescription, open);
        setId(id);

    }

    /*
     * @return the id of an TLRPC.ChatFull
     */
    public int getChatId() {
        return fullChatId;
    }

    /**
     * @return the id of the user that create the current decision.
     * This id referes to TLRPC.User
     */
    public long getUserCreatorId() {
        return userCreatorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

        if (fullChatId != decision.fullChatId) return false;
        if (getUserCreatorId() != decision.getUserCreatorId()) return false;
        if (isOpen() != decision.isOpen()) return false;
        if (getTitle() != null ? !getTitle().equals(decision.getTitle()) : decision.getTitle() != null)
            return false;
        return !(getLongDescription() != null ? !getLongDescription().equals(decision.getLongDescription()) : decision.getLongDescription() != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getLongDescription() != null ? getLongDescription().hashCode() : 0);
        result = 31 * result + fullChatId;
        result = 31 * result + (int) (getUserCreatorId() ^ (getUserCreatorId() >>> 32));
        result = 31 * result + (isOpen() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Decision{" +
                "fullChatId=" + fullChatId +
                ", userCreatorId=" + userCreatorId +
                ", longDescription='" + longDescription + '\'' +
                ", title='" + title + '\'' +
                ", open=" + open +
                '}';
    }


}
