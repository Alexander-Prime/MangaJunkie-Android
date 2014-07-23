package net.mangajunkie.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//==============================================================================
public class CollectionDatabaseHelper extends SQLiteOpenHelper {
	//--------------------------------------------------------------------------

	private static final String DB_NAME    = "collection.db";
	private static final int    DB_VERSION = 2;

	private static CollectionDatabaseHelper instance;

	//--------------------------------------------------------------------------

	private CollectionDatabaseHelper( Context context ) {
		super( context, DB_NAME, null, DB_VERSION );
	}

	//--------------------------------------------------------------------------

	public static CollectionDatabaseHelper getInstance( Context context ) {
		if ( instance == null ) instance = new CollectionDatabaseHelper( context.getApplicationContext() );
		return instance;
	}
	
	//--------------------------------------------------------------------------

	@Override
	public void onCreate( SQLiteDatabase db ) {
		db.beginTransaction();
		db.execSQL(
				"CREATE TABLE bookmarks (     " +
				"manga     TEXT PRIMARY KEY,  " +
				"chapter   TEXT,              " +
				"page      INTEGER DEFAULT 1, " +
				"timestamp INTEGER DEFAULT 0  " +
				");" );
		db.execSQL(
				"CREATE TABLE bookmark_categories ( " +
				"manga    TEXT,                     " +
				"category TEXT,                     " +
				"PRIMARY KEY ( manga, category )    " +
				");" ); 
		db.execSQL(
				"CREATE TABLE pins (           " +
				"manga   TEXT,                 " +
				"chapter TEXT,                 " +
				"PRIMARY KEY ( manga, chapter )" +
				");" );
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	//--------------------------------------------------------------------------
	
	@Override
	public void onUpgrade( SQLiteDatabase db, int old_version, int new_version ) {
		switch( old_version ) {
			
			case 1: // Version 1 did not have the "pins" table 
				db.execSQL(
						"CREATE TABLE pins (           " +
						"manga   TEXT,                 " +
						"chapter TEXT,                 " +
						"PRIMARY KEY ( manga, chapter )" +
						");" );
			
		}
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------