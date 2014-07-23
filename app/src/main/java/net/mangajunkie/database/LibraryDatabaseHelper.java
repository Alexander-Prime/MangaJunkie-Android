package net.mangajunkie.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//==============================================================================
public class LibraryDatabaseHelper extends SQLiteOpenHelper {
	//--------------------------------------------------------------------------
	
	private static final String DB_NAME    = "library.db";
	private static final int    DB_VERSION = 1;

	private static LibraryDatabaseHelper instance;
	
	//--------------------------------------------------------------------------
	
	private LibraryDatabaseHelper( Context context ) {
		super( context, DB_NAME, null, DB_VERSION );
	}
	
	//--------------------------------------------------------------------------
	
	public static LibraryDatabaseHelper getInstance( Context context ) {
		if ( instance == null )
			instance = new LibraryDatabaseHelper( context.getApplicationContext() );
		return instance;
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public void onCreate( SQLiteDatabase db ) {
		db.beginTransaction();
		db.execSQL(
				"CREATE TABLE manga (" +
					"sys_name TEXT PRIMARY KEY," +
					"title TEXT," +
					"summary TEXT," +
					"author TEXT," +
					"broken INTEGER" +
				")" );
		
		db.execSQL(
				"CREATE TABLE chapters (" +
					"manga TEXT," +
					"chapter TEXT," +
					"title TEXT," +
					"page_count INTEGER," +
					"PRIMARY KEY (manga, chapter)," +
					"FOREIGN KEY (manga) REFERENCES manga (sys_name)" +
				")" );
		
		db.execSQL(
				"CREATE TABLE tags (" +
					"name TEXT PRIMARY KEY," +
					"description TEXT" +
				")" );
		
		db.execSQL(
				"CREATE TABLE manga_tags (" +
					"manga TEXT," +
					"tag TEXT," +
					"PRIMARY KEY (manga, tag)," +
					"FOREIGN KEY (manga) REFERENCES manga (sys_name)," +
					"FOREIGN KEY (tag)   REFERENCES tags (name)" +
				")" );
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	//--------------------------------------------------------------------------

	@Override
	public void onUpgrade( SQLiteDatabase db, int old_version, int new_version ) {
		switch( old_version ) {
			
			case 1: // Version 1 did not have the "broken" column 
				db.execSQL( "ALTER TABLE manga ADD broken INTEGER DEFAULT 0" );
			
		}
	}
    
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------