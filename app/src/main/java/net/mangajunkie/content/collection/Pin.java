package net.mangajunkie.content.collection;

import android.database.Cursor;
import net.mangajunkie.content.edit.PinEditor;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Manga;

//==============================================================================
public class Pin {
	//--------------------------------------------------------------------------
	
	private final Chapter CHAPTER;
	
	//--------------------------------------------------------------------------
	
	public Pin( Chapter chapter ) { CHAPTER = chapter; }
	
	//--------------------------------------------------------------------------
	
	public Manga getManga() { return CHAPTER.getManga(); }
	
	//--------------------------------------------------------------------------
	
	public Chapter getChapter() { return CHAPTER; }
	
	//--------------------------------------------------------------------------
	
	public boolean exists() {
		Manga manga = CHAPTER.getManga();
		Cursor c = Collection.getDB().query(
			"pins", new String[] { "manga" },
			"manga=? AND chapter=?", new String[] { manga.getSysName(), CHAPTER.toString() },
		    null, null, null, "1" );
		
		try { return c.moveToFirst(); }
		finally { c.close(); }
	}

	//--------------------------------------------------------------------------
	
	public PinEditor edit() {
		return new PinEditor( this );
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
