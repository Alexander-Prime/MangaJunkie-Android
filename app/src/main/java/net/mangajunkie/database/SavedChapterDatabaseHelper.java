package net.mangajunkie.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//==============================================================================
public class SavedChapterDatabaseHelper extends SQLiteOpenHelper {
	//--------------------------------------------------------------------------

	private static final String DB_NAME    = "pins.db";
	private static final int    DB_VERSION = 1;

	private static SavedChapterDatabaseHelper instance;

	//--------------------------------------------------------------------------

	private SavedChapterDatabaseHelper( Context context ) {
		super( context, DB_NAME, null, DB_VERSION );
	}

	//--------------------------------------------------------------------------

	public static SavedChapterDatabaseHelper getInstance( Context context ) {
		if ( instance == null ) instance = new SavedChapterDatabaseHelper( context.getApplicationContext() );
		return instance;
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public void onCreate( SQLiteDatabase db ) {
		db.execSQL( "CREATE TABLE saved_chapters ( " +
					"manga TEXT, " +
					"chapter TEXT, " +
		            "PRIMARY KEY( manga, chapter ))" );
	}
	
	//--------------------------------------------------------------------------

	@Override
	public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
	}
    
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------