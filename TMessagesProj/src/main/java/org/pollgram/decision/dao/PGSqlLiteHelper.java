package org.pollgram.decision.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PGSqlLiteHelper extends SQLiteOpenHelper {

	public static final String DBName = "pollgramDecisions";
	public static final int DatabaseVersion = 1;
	private static final String LOG_TAG = "PGSQLH";

	// id
	public PGSqlLiteHelper(Context context) {
		super(context, DBName, null, DatabaseVersion);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(LOG_TAG, "Creazione");

		db.execSQL("DROP TABLE IF EXISTS operation");
//		db.execSQL(OperationDAOImpl.getTableSqlDeclaration());
//		db.execSQL(PaymentTypeDAOImpl.getTableSqlDeclaration());
//		db.execSQL(CategoryDAOImpl.getTableSqlDeclaration());
//		db.execSQL(PlannedOperationDAOImpl.getTableSqlDeclaration());
		Log.d(LOG_TAG, "Creazione tab ok");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		throw new RuntimeException("Not yet implemented");
	}
	
}
