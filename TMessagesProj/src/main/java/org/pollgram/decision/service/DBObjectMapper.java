package org.pollgram.decision.service;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by davide on 27/10/15.
 */
interface DBObjectMapper<T> {

    String getTableName();

    String getIdFiledName();

    T from(Cursor c);
    ContentValues toCV(T t);
}
