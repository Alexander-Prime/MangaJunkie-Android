package net.mangajunkie.content.library;

import android.database.Cursor;

//==============================================================================
public class Tag {
	//--------------------------------------------------------------------------
	
	private final String name;
	
	//--------------------------------------------------------------------------
	
	public Tag( String n ) {
		name = n;
	}
	
	//--------------------------------------------------------------------------
	
	public static String makeString( Tag[] tags ) {
		StringBuilder builder = new StringBuilder();
		for ( Tag tag : tags ) {
			if ( tag != tags[0] ) builder.append( ", " );
			builder.append( tag.getName() );
		}
		return builder.toString();
	}
	
	//--------------------------------------------------------------------------
	
	public Manga[] getManga() {
		Cursor c = Library.getDB().query(
			"manga_tags", new String[]{ "manga" },
			"tag LIKE ?", new String[]{ "%" + name + "%" },
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
}
//------------------------------------------------------------------------------