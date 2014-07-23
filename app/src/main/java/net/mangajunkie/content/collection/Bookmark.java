package net.mangajunkie.content.collection;


import android.database.Cursor;
import net.mangajunkie.content.edit.BookmarkEditor;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Manga;
import net.mangajunkie.content.library.Page;

//==============================================================================
public class Bookmark {
	//--------------------------------------------------------------------------

	//private final User  USER;
	private final Manga MANGA;

	//--------------------------------------------------------------------------

	public Bookmark( /*User user,*/ Manga manga ) { 
	//	USER  = user;
		MANGA = manga;
	}

	//--------------------------------------------------------------------------

	public boolean exists() {
		Cursor c = query( "timestamp" );
		try { return c.moveToFirst(); } finally { c.close(); }
	}

	//--------------------------------------------------------------------------
	
	public boolean is( Manga manga ) {
		return getManga().equals( manga );
	}
	
	//--------------------------------------------------------------------------
	
	public boolean is( Chapter chapter ) {
		return getChapter().equals( chapter );
	}
	
	//--------------------------------------------------------------------------
	
	public boolean is( Page page ) {
		return getPage().equals( page );
	}
	
	//--------------------------------------------------------------------------

	public Manga getManga() { return MANGA; }
	
	//--------------------------------------------------------------------------
	
	public Chapter getChapter() {
		Cursor c = query( "chapter" );
		try {
			if ( c.moveToFirst() ) return new Chapter( MANGA, c.getString( 0 ));
			else throw new RuntimeException( "No bookmark for " + MANGA.getSysName() );
		}
		finally { c.close(); }
	}
	
	//--------------------------------------------------------------------------
	
	public Page getPage() {
		Cursor c = query( "chapter", "page" );
		try {
			if ( c.moveToFirst() ) {
				Chapter chapter = new Chapter( MANGA, c.getString( 0 ));
				return new Page( chapter, c.getInt( 1 ));
				
			} else {
				Chapter chapter = new Chapter( MANGA, "0" );
				return new Page( chapter, 0 ); //throw new RuntimeException( "No bookmark for " + MANGA.getSysName() );
			}
		}
		finally { c.close(); }
	}
	
	//--------------------------------------------------------------------------
	
	public long getTimestamp() {
		Cursor c = query( "timestamp" );
		try {
			if ( c.moveToFirst() ) {
				return c.getLong( 0 );
			}
			else return 0; //throw new RuntimeException( "No bookmark for " + MANGA.getSysName() );
		}
		finally { c.close(); }
	}

	//--------------------------------------------------------------------------
	
	public boolean isCurrent() {
		return is( getManga().getLatestChapter() );
	}
	
	//--------------------------------------------------------------------------
	
	public BookmarkEditor edit() {
		return new BookmarkEditor( this );
	}
	
	//--------------------------------------------------------------------------
	
	private Cursor query( String... cols ) {
		return Collection.getDB().query(
			"bookmarks", cols,
		    "MANGA=?", new String[]{ MANGA.getSysName() },
		    null, null, null, "1" );
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------