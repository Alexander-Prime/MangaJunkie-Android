package net.mangajunkie.content.edit;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import net.mangajunkie.content.collection.Collection;
import net.mangajunkie.content.collection.Pin;
import net.mangajunkie.content.library.Chapter;

//==============================================================================
public class PinEditor extends Editor<Pin> {
	//--------------------------------------------------------------------------

	private final Chapter CHAPTER;
	private final boolean BEFORE;
	private       boolean after;

	//--------------------------------------------------------------------------

	public PinEditor( Pin pin ) {
		CHAPTER = pin.getChapter();
		BEFORE = pin.exists();
		after = BEFORE;
	}

	//--------------------------------------------------------------------------

	public PinEditor add() {
		after = true;
		return this;
	}

	//--------------------------------------------------------------------------
	
	public PinEditor remove() {
		after = false;
		return this;
	}
	
	//--------------------------------------------------------------------------

	@Override public Pin apply() {
		if ( after && !BEFORE ) { // Added
			ContentValues values = new ContentValues( 2 );
			values.put( "manga",   CHAPTER.getManga().getSysName() );
			values.put( "chapter", CHAPTER.toString() );
			Collection.getDB().insertWithOnConflict( "pins", null, values, SQLiteDatabase.CONFLICT_IGNORE );
		}
		
		if ( BEFORE && !after ) { // Removed
			Collection.getDB().delete(
				"pins",
				"manga=? AND chapter=?",
				new String[] { CHAPTER.getManga().getSysName(), CHAPTER.toString() } );
		}
		
		return new Pin( CHAPTER );
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
