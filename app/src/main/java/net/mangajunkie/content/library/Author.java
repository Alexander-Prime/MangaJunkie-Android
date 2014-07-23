package net.mangajunkie.content.library;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import net.mangajunkie.android.app.IntentProvider;

//==============================================================================
public class Author implements IntentProvider {
	//--------------------------------------------------------------------------
	
	private final String name;
	
	//--------------------------------------------------------------------------
	
	public Author( String name ) {
		this.name = name;
	}
	
	//--------------------------------------------------------------------------
	
	public Manga[] getManga() {
		Cursor c = Library.getDB().query(
			"manga", new String[]{ "sys_name" },
			"author=?", new String[]{ name },
			null, null, null );
		try {
			Manga[] result = new Manga[c.getCount()];
			while ( c.moveToNext() ) {
				result[c.getPosition()] = new Manga( c.getString( 0 ));
			}
			return result;
		} finally { c.close(); }
	}
	
	//--------------------------------------------------------------------------
	
	public String getName() {
		return name;
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public Intent getIntent( Context context ) {
		return null; //return new Intent( context, AuthorActivity.class ).putExtra( "sys_name", name );
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------