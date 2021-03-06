package org.decisiongram.data;

import android.support.annotation.Nullable;

import java.util.Date;

/**
 * Created by davide on 01/10/15.
 */
public class Vote extends DBBean {

    private Boolean vote;
    private final long optionId;
    private final int userId;
    private Date voteTime;

    public Vote(long id, Boolean vote, Date voteTime, int userId, long optionId) {
        this(vote, voteTime, userId, optionId);
        setId(id);
    }

    public Vote(Boolean vote, Date voteTime, int userId, long optionId) {
        this.vote = vote;
        this.voteTime = voteTime;
        this.userId = userId;
        this.optionId = optionId;
    }

    /**
     * User for express a vote that the user never express
     * @param userId
     * @param optionId
     */
    public Vote(int userId, long optionId) {
        this(null, null ,userId,optionId);

    }

    public int getUserId() {
        return userId;
    }

    public long getOptionId() {
        return optionId;
    }

    public
    @Nullable
    Boolean isVote() {
        return vote;
    }

    public void setVote(@Nullable Boolean vote) {
        this.vote = vote;
    }

    public Date getVoteTime() {
        return voteTime;
    }

    public void setVoteTime(Date voteTime) {
        this.voteTime = voteTime;
    }

    @Override
    public String toString() {
        return vote == null ? null : Boolean.toString(vote);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vote)) return false;
        if (!super.equals(o)) return false;

        Vote vote1 = (Vote) o;

        if (getOptionId() != vote1.getOptionId()) return false;
        if (getUserId() != vote1.getUserId()) return false;
        return !(vote != null ? !vote.equals(vote1.vote) : vote1.vote != null);

    }

    @Override
    public int hashCode() {
        int result = (vote != null ? vote.hashCode() : 0);
        result = 31 * result + (int) (getOptionId() ^ (getOptionId() >>> 32));
        result = 31 * result + getUserId();
        return result;
    }
}
