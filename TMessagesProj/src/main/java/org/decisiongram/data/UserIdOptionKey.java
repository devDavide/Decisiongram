package org.decisiongram.data;

/**
 * Created by davide on 29/10/15.
 */
public class UserIdOptionKey {
    private final int userId;
    private final long optionId;

    public UserIdOptionKey(int userId, long optionId) {
        this.userId = userId;
        this.optionId = optionId;
    }

    public long getOptionId() {
        return optionId;
    }

    public int getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserIdOptionKey)) return false;

        UserIdOptionKey that = (UserIdOptionKey) o;

        if (getUserId() != that.getUserId()) return false;
        return getOptionId() == that.getOptionId();

    }

    @Override
    public int hashCode() {
        int result = getUserId();
        result = 31 * result + (int) (getOptionId() ^ (getOptionId() >>> 32));
        return result;
    }
}
