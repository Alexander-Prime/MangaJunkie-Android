package net.mangajunkie.content.edit;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import net.mangajunkie.content.library.Library;

//==============================================================================
public class LibraryEditor extends Editor<Library> {
	//--------------------------------------------------------------------------
	
	private final JSONObject json;
	
	//--------------------------------------------------------------------------
	
	public LibraryEditor( JSONObject json ) {
		this.json = json;
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public Library apply() {
		SQLiteDatabase db = Library.getDB();
		db.beginTransaction();
		try {
			JSONArray titles    = json.getJSONArray( "title"    ),
			          sys_names = json.getJSONArray( "sys_name" );
			if ( titles.length() != sys_names.length() ) throw new JSONException( "titles.length() and sys_names.length() are not equal" );
			
			ContentValues values = new ContentValues();
			for ( int i = 0; i < titles.length(); i++ ) {
				values.clear();
				values.put( "sys_name", sys_names.getString( i ));
				values.put( "title", titles.getString( i ) );
				db.insert( "manga", null, values );
			}
			
			db.setTransactionSuccessful();
			
		} catch ( JSONException e ) {
			e.printStackTrace();
			
		} finally { db.endTransaction(); }
		
		return new Library();
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
