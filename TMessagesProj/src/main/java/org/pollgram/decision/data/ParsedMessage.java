package org.pollgram.decision.data;

/**
 * Created by davide on 04/01/16.
 */
public class ParsedMessage extends  DBBean{

    private final long groupId;
    private final int messageId;
    private final boolean parsedSuccessfully;

    public ParsedMessage(long id, long groupId, int messageId, boolean parsedSuccessfully) {
        this(groupId,messageId, parsedSuccessfully);
        setId(id);
    }

    public ParsedMessage(long groupId, int messageId, boolean parsedSuccessfully) {
        this.groupId = groupId;
        this.messageId = messageId;
        this.parsedSuccessfully = parsedSuccessfully;
    }

    public long getGroupId() {
        return groupId;
    }

    public int getMessageId() {
        return messageId;
    }

    public boolean isParsedSuccessfully() {
        return parsedSuccessfully;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParsedMessage)) return false;

        ParsedMessage that = (ParsedMessage) o;

        if (getGroupId() != that.getGroupId()) return false;
        return getMessageId() == that.getMessageId();

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (getGroupId() ^ (getGroupId() >>> 32));
        result = 31 * result + getMessageId();
        return result;
    }

    @Override
    public String toString() {
        return "ParsedMessage{" +
                "groupId=" + groupId +
                ", messageId=" + messageId +
                ", parsedSuccessfully=" + parsedSuccessfully +
                '}';
    }
}
