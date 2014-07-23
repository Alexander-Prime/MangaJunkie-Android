package net.mangajunkie.content.edit;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import net.mangajunkie.content.library.Author;
import net.mangajunkie.content.library.Library;
import net.mangajunkie.content.library.Manga;
import net.mangajunkie.content.library.Tag;

//==============================================================================
public class MangaEditor extends Editor<Manga> {
	//--------------------------------------------------------------------------

	private final String SYS_NAME;
	private String title, summary;
	private Author author;
	private Tag[]  tags;
	private String[] chapters, chapter_titles;

	//--------------------------------------------------------------------------
	
	public MangaEditor( Manga manga ) { this( manga.getSysName() ); }
	
	private MangaEditor( String sys_name ) { SYS_NAME = sys_name; }

	//--------------------------------------------------------------------------

	public MangaEditor setTitle(   String title   ) { this.title   = title;   return this; }
	public MangaEditor setSummary( String summary ) { this.summary = summary; return this; }
	public MangaEditor setAuthor(  Author author  ) { this.author  = author;  return this; }
	public MangaEditor setTags(    Tag[]  tags    ) { this.tags    = tags;    return this; }
	
	public MangaEditor setChapters( String[] chapters, String[] chapter_titles ) {
		this.chapters       = chapters;
		this.chapter_titles = chapter_titles;
		return this;
	}

	//--------------------------------------------------------------------------

	public void clearTitle()   { title   = null; }
	public void clearSummary() { summary = null; }
	public void clearAuthor()  { author  = null; }
	public void clearTags()    { tags    = null; }
	
	public void clearChapters() {
		chapters = null;
		chapter_titles = null;
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public Manga apply() {
		SQLiteDatabase db = Library.getDB();
		ContentValues values = new ContentValues();
		
		db.beginTransaction();
		
		try {
			// Top-level metainfo
			if ( title   != null ) { values.put( "title",   title            ); }
			if ( summary != null ) { values.put( "summary", summary          ); }
			if ( author  != null ) { values.put( "author",  author.getName() ); }
			try { db.update( "manga", values, "sys_name=?", new String[]{ SYS_NAME } ); }
			catch ( IllegalArgumentException e ) { /* Empty values; move on */ }
			
			// Tags
			if ( tags != null ) {
				// Deleting and replacing all tags is simpler than selecting
				// which ones are missing
				db.delete( "manga_tags", "manga=?", new String[]{ SYS_NAME } );
				
				for ( Tag tag : tags ) {
					// Make sure the tags exist...
					values.clear();
					values.put( "name", tag.getName() );
					db.insertWithOnConflict( "tags", null, values, SQLiteDatabase.CONFLICT_IGNORE );
					// ... then link 'em to the manga
					values.clear();
					values.put( "manga", SYS_NAME );
					values.put( "tag",   tag.getName() );
					db.insert( "manga_tags", null, values );
				}
			}
			
			// Chapters
			if ( chapters        != null
			&&   chapter_titles  != null
			&&   chapters.length == chapter_titles.length ) {
				for ( int i = 0; i < chapters.length; i++ ) {
					values.clear();
					values.put( "manga",   SYS_NAME          );
					values.put( "chapter", chapters[i]       );
					values.put( "title",   chapter_titles[i] );
					try { db.insertOrThrow( "chapters", null, values ); }
					catch ( SQLiteException e ) {
						db.update( "chapters", values, "manga=? AND chapter=?", new String[] { SYS_NAME, chapters[i] } );
					}
				}
			}
			
			db.setTransactionSuccessful();
		}
		finally { db.endTransaction(); }
		
		return new Manga( SYS_NAME );
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
