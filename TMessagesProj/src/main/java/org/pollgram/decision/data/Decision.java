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
    private int userThatVoteCount;

    // icon/image will be retrived externally by url or file.
    // Like the file containing the decision image can be named like decisio_<id>.png, and cached on the fs

    public Decision(int fullChatId, long userCreatorId, String title, String longDescription,
                    boolean open, int userThatVoteCount) {
        this.fullChatId = fullChatId;
        this.userCreatorId = userCreatorId;
        this.title = title;
        this.longDescription = longDescription;
        this.open = open;
        this.userThatVoteCount = userThatVoteCount;
    }

    public Decision(long id, int fullChatId, long userCreatorId, String title, String longDescription,
                    boolean open, int userThatVoteCount) {
        this(fullChatId,userCreatorId,title, longDescription, open, userThatVoteCount);
        setId(id);

    }

    /*
     * @return the id of an TLRPC.ChatFull
     */
    public int getFullChatId() {
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

    public int getUserThatVoteCount() {
        return userThatVoteCount;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getUsersThatVoteCount() {
        return userThatVoteCount;
    }

    public void setUserThatVoteCount(int userThatVoteCount) {
        this.userThatVoteCount = userThatVoteCount;
    }

    @Override
    public String toString() {
        return "Decision{" +
                "fullChatId=" + fullChatId +
                ", userCreatorId=" + userCreatorId +
                ", longDescription='" + longDescription + '\'' +
                ", title='" + title + '\'' +
                ", open=" + open +
                ", userThatVoteCount=" + userThatVoteCount +
                '}';
    }
}
