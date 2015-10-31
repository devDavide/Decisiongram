package org.pollgram.decision.data;

/**
 * Created by davide on 01/10/15.
 */
public class Vote {

    private final long id;
    private final Choice option;
    private final int userId;

    private boolean vote;


    public Vote(long id, Choice option, int userId) {
        this.id = id;
        this.option = option;
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public long getId() {
        return id;
    }

    public Choice getChoice() {
        return option;
    }

    public boolean isVote() {
        return vote;
    }

    public void setVote(boolean vote) {
        this.vote = vote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vote vote = (Vote) o;

        return id == vote.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return Boolean.toString(vote);
    }
}
