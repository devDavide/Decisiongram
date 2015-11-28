package org.pollgram.decision.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.pollgram.decision.data.DBBean;
import org.pollgram.decision.data.Decision;
import org.pollgram.decision.data.TextOption;
import org.pollgram.decision.data.Vote;
import org.telegram.messenger.ApplicationLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

class PGSqlLiteHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "pollgramDecisions";
    public static final int DATABASE_VERSION = 1;
    private static final String LOG_TAG = "PGSQLH";
    private static final String DEFAULT_ID_FILE_NAME = "id";

    static class T_Decision {
        static final String TABLE_NAME = "decision";

        static final String ID = DEFAULT_ID_FILE_NAME;
        static final String TITLE = "title";
        static final String LONG_DESCRIPTION = "long_description";
        static final String FULL_CHAT_ID = "full_chat_id";
        static final String USER_CREATOR_ID = "user_creator_id";
        static final String OPEN = "open";

        public static String cloumns(String tableAlias) {
            return createColumns(tableAlias, ID, TITLE, LONG_DESCRIPTION, FULL_CHAT_ID, USER_CREATOR_ID, OPEN);
        }

    }

    static class T_TextOption {
        static final String TABLE_NAME = "text_option";
        static final String ID = DEFAULT_ID_FILE_NAME;
        static final String TITLE = "title";
        static final String LONG_DESCRIPTION = "long_description";
        static final String FK_DECISION = "fk_decision";

        public static String cloumns(String tableAlias) {
            return createColumns(tableAlias, ID, TITLE, LONG_DESCRIPTION, FK_DECISION);
        }
    }

    static class T_Vote {
        static final String TABLE_NAME = "vote";
        static final String ID = DEFAULT_ID_FILE_NAME;
        static final String VOTE = "vote";
        static final String VOTE_TIME = "voteTime";
        static final String USER_ID = "user_id";
        static final String FK_OPTION = "fk_option";

        static String cloumns(String tableAlias){
            return createColumns(tableAlias, ID,VOTE,VOTE_TIME,USER_ID,FK_OPTION);
        }
    }

    private static String createColumns(String tableName, String... columnsNames){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columnsNames.length; i++) {
            if (tableName != null) {
                sb.append(tableName);
                sb.append('.');
            }
            sb.append(columnsNames[i]);
            if (i != columnsNames.length - 1)
                sb.append(',');
        }
        return sb.toString();
    }

    /**
     * DBObjectMapper for Vote
     */
    static final DBObjectMapper<Vote> VOTE_MAPPER = new DBObjectMapper<Vote>() {
        @Override
        public String getTableName() {
            return T_Vote.TABLE_NAME;
        }

        @Override
        public String getIdFiledName() {
            return T_Vote.ID;
        }

        @Override
        public Vote from(Cursor c) {
            long id = getLong(c, T_Vote.ID);
            Boolean voteValue = getBoolean(c, T_Vote.VOTE);
            Date voteTime = getDate(c, T_Vote.VOTE_TIME);
            int userId = getInt(c, T_Vote.USER_ID);
            long optionId = getLong(c, T_Vote.FK_OPTION);
            return new Vote(id,voteValue, voteTime, userId, optionId);
        }

        @Override
        public ContentValues toCV(Vote v) {
            ContentValues cv = new ContentValues();
            cv.put(T_Vote.VOTE, v.isVote());
            cv.put(T_Vote.VOTE_TIME,v.getVoteTime().getTime());
            cv.put(T_Vote.USER_ID, v.getUserId());
            cv.put(T_Vote.FK_OPTION, v.getOptionId());
            return cv;
        }
    };


    /**
     * DBObjectMapper for Decision
     */
    static final DBObjectMapper<Decision> DECISION_MAPPER = new DBObjectMapper<Decision>() {

        @Override
        public String getTableName() {
            return T_Decision.TABLE_NAME;
        }

        @Override
        public String getIdFiledName() {
            return T_Decision.ID;
        }

        @Override
        public Decision from(Cursor c) {
            long id = getLong(c, T_Decision.ID);
            String title = getString(c, T_Decision.TITLE);
            String description = getString(c, T_Decision.LONG_DESCRIPTION);
            int fullChatId = getInt(c, T_Decision.FULL_CHAT_ID);
            long userCreatorId = getLong(c, T_Decision.USER_CREATOR_ID);
            boolean isOpen = getBoolean(c, T_Decision.OPEN);
            return new Decision(id, fullChatId, userCreatorId, title, description, isOpen);
        }

        @Override
        public ContentValues toCV(Decision d) {
            ContentValues cv = new ContentValues();
            cv.put(T_Decision.TITLE, d.getTitle());
            cv.put(T_Decision.LONG_DESCRIPTION, d.getLongDescription());
            cv.put(T_Decision.FULL_CHAT_ID, d.getChatId());
            cv.put(T_Decision.USER_CREATOR_ID, d.getUserCreatorId());
            cv.put(T_Decision.OPEN, d.isOpen());
            return cv;
        }
    };

    /**
     * DBObjectMapper for TextOption
     */
    static final DBObjectMapper<TextOption> TEXT_OPTION_MAPPER = new DBObjectMapper<TextOption>() {
        @Override
        public String getTableName() {
            return T_TextOption.TABLE_NAME;
        }

        @Override
        public String getIdFiledName() {
            return T_TextOption.ID;
        }

        @Override
        public TextOption from(Cursor c) {
            long id = getLong(c, T_TextOption.ID);
            String title = getString(c, T_TextOption.TITLE) ;
            String longDescription = getString(c, T_TextOption.LONG_DESCRIPTION);
            long decisionId = getLong(c, T_TextOption.FK_DECISION);
            return new TextOption(id, title, longDescription, decisionId);
        }

        @Override
        public ContentValues toCV(TextOption to) {
            ContentValues cv = new ContentValues();
            cv.put(T_TextOption.TITLE, to.getTitle());
            cv.put(T_TextOption.LONG_DESCRIPTION, to.getLongDescription());
            cv.put(T_TextOption.FK_DECISION, to.getDecisionId());
            return cv;
        }
    };

    static boolean getBoolean(int num) {
        return num == 1;
    }

    static int toInt(boolean b) {
        return b ? 1 : 0;
    }

    static String toString(boolean b) {
        return Integer.toString(toInt(b));
    }

    static Boolean getBoolean(Cursor c, String colName) {
        int columnIdx = c.getColumnIndex(colName);
        if (c.isNull(columnIdx))
            return null;
        else
            return getBoolean(c.getInt(columnIdx));
    }

    private static String getString(Cursor c, String colName) {
        return c.getString(c.getColumnIndex(colName));
    }

    private static long getLong(Cursor c, String colName) {
        return c.getLong(c.getColumnIndex(colName));
    }

    private static int getInt(Cursor c, String colName) {
        return c.getInt(c.getColumnIndex(colName));
    }

    private static Date getDate(Cursor c, String colName){
        return new Date(c.getLong(c.getColumnIndex(colName)));
    }

    public PGSqlLiteHelper() {
        super(ApplicationLoader.applicationContext, DB_NAME, null, DATABASE_VERSION);
    }


    public <T extends DBBean> T insert(T bean, DBObjectMapper<T> mapper) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            long id = db.insert(mapper.getTableName(), null, mapper.toCV(bean));
            db.close();
            if (id == -1)
                throw new PollgramDAOException("Error inserting: " + bean);
            bean.setId(id);
            return bean;
        } finally {
            if (db != null && db.isOpen())
                db.close();
        }
    }

    public <T extends DBBean> void update(T bean, DBObjectMapper<T> mapper) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            int nrow = db.update(mapper.getTableName(), mapper.toCV(bean),
                    mapper.getIdFiledName() + "= ?", new String[]{Long.toString(bean.getId())});
            if (nrow == 0)
                throw new PollgramDAOException("Error updating: " + bean);

        } finally {
            if (db != null && db.isOpen())
                db.close();
        }
    }

    public <T extends DBBean> List<T> query(DBObjectMapper<T> mapper, String selection,
                                            String[] selectionArgs) {
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor cursor = db.query(mapper.getTableName(), null, selection,
                    selectionArgs, null, null, null);
            List<T> result = new ArrayList<>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                result.add(mapper.from(cursor));
                cursor.moveToNext();
            }
            return result;
        } finally {
            if (db != null && db.isOpen())
                db.close();
        }
    }

    public <T extends DBBean> T findFirst(DBObjectMapper<T> mapper, String selection,
                                            String[] selectionArgs) {
        List<T> list = query(mapper,selection,selectionArgs);
        if (list.isEmpty())
            return null;

        if (list.size() > 1){
            Log.e(LOG_TAG, "Found multiple records for selection["+selection+"] " +
                    "selectionArgs["+ Arrays.toString(selectionArgs)+"] i will return the first");
        }
        return list.get(0);
    }

    public <T extends DBBean> T findById(long id, DBObjectMapper<T> mapper) {
        SQLiteDatabase db = getReadableDatabase();

        try {
            Cursor cursor = db.query(mapper.getTableName(), null, mapper.getIdFiledName() + "= ?",
                    new String[]{Long.toString(id)}, null, null, null);
            if (!cursor.moveToFirst())
                return null;

            return mapper.from(cursor);
        } finally {
            if (db != null && db.isOpen())
                db.close();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(LOG_TAG, "Creating brand new db");
        db.execSQL("CREATE TABLE " + T_Decision.TABLE_NAME + " (" +
                T_Decision.ID + " INTEGER PRIMARY KEY, " +
                T_Decision.TITLE + " TEXT, " +
                T_Decision.LONG_DESCRIPTION + " TEXT, " +
                T_Decision.FULL_CHAT_ID + " INTEGER," +
                T_Decision.USER_CREATOR_ID + " INTEGER," +
                T_Decision.OPEN + " INTEGER, " +
                "UNIQUE ("+T_Decision.TITLE+","+T_Decision.FULL_CHAT_ID+")" +
                ") ;");
        db.execSQL("CREATE TABLE " + T_TextOption.TABLE_NAME + " (" +
                T_TextOption.ID + " INTEGER PRIMARY KEY, " +
                T_TextOption.TITLE + " TEXT, " +
                T_TextOption.LONG_DESCRIPTION + " TEXT, " +
                T_TextOption.FK_DECISION + " INTEGER, " +
                "UNIQUE("+T_TextOption.TITLE +", " + T_TextOption.FK_DECISION +"), "+
                "FOREIGN KEY(" + T_TextOption.FK_DECISION + ") REFERENCES " + T_Decision.TABLE_NAME + "(" + T_Decision.ID + ") ) ;");
        db.execSQL("CREATE TABLE " + T_Vote.TABLE_NAME + " (" +
                T_Vote.ID + " INTEGER PRIMARY KEY," +
                T_Vote.VOTE + " Boolean," +
                T_Vote.VOTE_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                T_Vote.FK_OPTION + " INTEGER, " +
                T_Vote.USER_ID + " INTEGER, " +
                "FOREIGN KEY(" + T_Vote.FK_OPTION + ")REFERENCES " + T_TextOption.TABLE_NAME + " (" + T_TextOption.ID + ") ) ;");
        Log.i(LOG_TAG, "Db creation completed");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new RuntimeException("Not yet implemented");
    }

}
