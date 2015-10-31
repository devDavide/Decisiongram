package org.pollgram.decision.data;

/**
 * Created by davide on 01/10/15.
 */
public class Decision {

    private final long id;
    private final int fullChatId;
    private final long userCreatorId;

    private String description;
    private boolean open;
    private int voteCount;

    // icon/image will be retrived externally by url or file.
    // Like the file containing the decision image can be named like decisio_<id>.png, and cached on the fs

    public Decision(long id, int fullChatId, long userCreatorId) {
        this.id = id;
        this.fullChatId = fullChatId;
        this.userCreatorId = userCreatorId;
    }

    public Decision(long id, int fullChatId, long userCreatorId, String description, boolean open, int voteCount) {
        this.id = id;
        this.fullChatId = fullChatId;
        this.userCreatorId = userCreatorId;
        this.description = description;
        this.open = open;
        this.voteCount = voteCount;
    }

    public long getId() {
        return id;
    }

    /**
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Decision decision = (Decision) o;

        return id == decision.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

}
