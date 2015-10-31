package org.pollgram.decision.dao;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by davide on 27/10/15.
 */
public interface DBObjectMapper<T> {
    T from(Cursor c);
    ContentValues toCV(T t);
}
