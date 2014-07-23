package net.mangajunkie.content.library;

import android.database.sqlite.SQLiteDatabase;
import org.json.JSONObject;
import net.mangajunkie.content.edit.LibraryEditor;

//==============================================================================
public class Library {
	//--------------------------------------------------------------------------
	
	private static SQLiteDatabase DB;
	
	//--------------------------------------------------------------------------
	
	public static void setDB( SQLiteDatabase db ) {
		if ( DB != null ) throw new RuntimeException( "Called Library.setDB() more than once" );
		DB = db;
	}
	
	//--------------------------------------------------------------------------
	
	public static SQLiteDatabase getDB() {
		if ( DB == null ) throw new RuntimeException( "Called Library.getDB() before Collection.setDB()" );
		return DB;
	}
	
	//--------------------------------------------------------------------------
	
	public LibraryEditor edit( JSONObject json ) {
		return new LibraryEditor( json );
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
