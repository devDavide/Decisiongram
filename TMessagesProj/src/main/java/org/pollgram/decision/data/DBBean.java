package org.pollgram.decision.data;

/**
 * Created by davide on 27/10/15.
 */
public abstract class DBBean {
    public static final long ID_NOT_SET = -1;

    private long id;

    protected DBBean(){
        this(ID_NOT_SET);
    }

    private DBBean(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBBean)) return false;

        DBBean dbBean = (DBBean) o;

        return getId() == dbBean.getId();

    }

    @Override
    public int hashCode() {
        return (int) (getId() ^ (getId() >>> 32));
    }
}
