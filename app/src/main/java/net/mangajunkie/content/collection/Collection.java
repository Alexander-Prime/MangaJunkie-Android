package net.mangajunkie.content.collection;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import net.mangajunkie.content.library.Manga;

//==============================================================================
public class Collection {
	//--------------------------------------------------------------------------

	private static SQLiteDatabase DB;

	//--------------------------------------------------------------------------

	public static void setDB( SQLiteDatabase db ) {
		if ( DB != null ) throw new RuntimeException( "Called Collection.setDB() more than once" );
		DB = db;
	}
	
	//--------------------------------------------------------------------------
	
	public static SQLiteDatabase getDB() {
		if ( DB == null ) throw new RuntimeException( "Called Collection.getDB() before Collection.setDB()" );
		return DB;
	}
	
	//--------------------------------------------------------------------------
	
	//--------------------------------------------------------------------------
	
	public static Bookmark[] getBookmarks() {
		Manga[] manga = getBookmarkedManga();
		Bookmark[] results = new Bookmark[manga.length];
		
		for ( int i = 0; i < manga.length; i++ ) results[i] = new Bookmark( manga[i] );
		
		return results;
	}
	
	//--------------------------------------------------------------------------
	
	public static Manga[] getBookmarkedManga() {
		Cursor c = DB.query( "bookmarks", new String[] { "manga" },
		                     null, null, null, null, null );
		
		Manga[] results = new Manga[c.getCount()];
		while ( c.moveToNext() ) results[c.getPosition()] = new Manga( c.getString( 0 ));
		
		return results;
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
