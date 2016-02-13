package org.decisiongram.data;

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


    /**
     * compare two string considering null == "" == " "
     * @param s1
     * @param s2
     * @return
     */
    protected static boolean equalString(String s1, String s2) {
        boolean isS1Empty = s1 == null || s1.trim().isEmpty() || s1.equals(""+null);
        boolean isS2Empty = s2 == null || s2.trim().isEmpty() || s2.equals(""+null);
        if (isS1Empty && isS2Empty)
            return true;
        else
            return s1.equals(s2);
    }
}
