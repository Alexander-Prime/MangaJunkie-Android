package net.mangajunkie.content.edit;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Library;
import net.mangajunkie.content.library.Manga;

//==============================================================================
public class ChapterEditor extends Editor<Chapter> {
	//--------------------------------------------------------------------------

	private final Manga  MANGA;
	private final String CHAPTER;
	private       String title;
	private       int    page_count;

	//--------------------------------------------------------------------------
	
	public ChapterEditor( Chapter chapter ) { this( chapter.getManga(), chapter.toString() ); }

	//--------------------------------------------------------------------------
	
	private ChapterEditor( Manga manga, String chapter ) {
		MANGA   = manga;
		CHAPTER = chapter;
	}

	//--------------------------------------------------------------------------

	public ChapterEditor setTitle(     String title      ) { this.title      = title;      return this; }
	public ChapterEditor setPageCount( int    page_count ) { this.page_count = page_count; return this; }

	//--------------------------------------------------------------------------
	
	public void clearTitle()     { title      = null; }
	public void clearPageCount() { page_count = 0;    }
	
	//--------------------------------------------------------------------------
	
	@Override
	public Chapter apply() {
		SQLiteDatabase db = Library.getDB();
		ContentValues values = new ContentValues();
		
		// Top-level metainfo
		if ( title      != null ) { values.put( "title",      title      ); }
		if ( page_count  > 0    ) { values.put( "page_count", page_count ); }
		db.update( "chapters", values, "manga=? AND chapter=?", new String[]{ MANGA.getSysName(), CHAPTER } );
		
		return new Chapter( MANGA, CHAPTER );
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
