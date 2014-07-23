package net.mangajunkie.content.library;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;
import net.mangajunkie.android.activity.ChapterActivity;
import net.mangajunkie.content.collection.Pin;
import net.mangajunkie.content.edit.ChapterEditor;
import net.mangajunkie.error.MissingContentException;
import net.mangajunkie.network.API;

//==============================================================================
public class Chapter {
	//--------------------------------------------------------------------------
	
	private final Manga  manga;
	private final String chapter;

	//--------------------------------------------------------------------------

	@Override
	public String toString() { return chapter; }

	//--------------------------------------------------------------------------
	
	public float toFloat() { return Float.valueOf( chapter ); }
	
	//--------------------------------------------------------------------------

	public Chapter( Manga m, String ch ) {
		manga = m;
		chapter = ch;
	}

	//--------------------------------------------------------------------------

	public Manga getManga() {
		return manga;
	}

	//--------------------------------------------------------------------------

	public Page getPage( int page ) {
		return new Page( this, page );
	}

	//--------------------------------------------------------------------------
	
	public Page[] getPages() {
		Page[] pages = new Page[getPageCount()];
		for ( int i = 0; i < pages.length; i++ ) {
			pages[i] = new Page( this, i + 1 );
		}
		return pages;
	}
	
	//--------------------------------------------------------------------------
	
	public int getPageCount() {
		Cursor c = Library.getDB().query(
		"chapters", new String[]{ "page_count" },
		"manga=? AND chapter=?", new String[]{ manga.getSysName(), chapter },
		null, null, null, "1" );
		try {
			if ( !c.moveToFirst() ) throw new MissingContentException( this );
			return c.getInt( 0 );
		}
		finally { c.close(); }
	}
	
	//--------------------------------------------------------------------------
	
	public boolean hasTitle() {
		return !getRawTitle().matches( "^\\s*$" );
	}
	
	//--------------------------------------------------------------------------
	
	public String getTitle() {
		String title = getRawTitle();
		return title.matches( "^\\s*$" ) ? "Chapter " + chapter : title;
	}
	
	//--------------------------------------------------------------------------
	
	public String getRawTitle() {
		Cursor c = Library.getDB().query(
		"chapters", new String[]{ "title" },
		"manga=? AND chapter=?", new String[]{ manga.getSysName(), chapter },
		null, null, null, "1" );
		try {
			if ( !c.moveToFirst() ) return ""; //throw new MissingContentException( this );
			return c.getString( 0 );
		} finally { c.close(); }
	}
	
	//--------------------------------------------------------------------------
	
	/*public Chapter getRelativeChapter( int direction ) {
		switch( (int)Math.signum( direction ) ) {
			case -1: return getPreviousChapter();
			case  1: return getNextChapter();
			default:
			case  0: return this;
		}
	}*/
	
	//--------------------------------------------------------------------------
	
	public Chapter getNextChapter() {
		Cursor c = Library.getDB().query(
		"chapters", new String[]{ "chapter" },
		"manga=? AND CAST( chapter AS REAL )>CAST( ? AS REAL )", new String[]{ manga.getSysName(), chapter },
		null, null, "CAST( chapter AS REAL ) ASC", "1" );
		try {
			if ( !c.moveToFirst() ) return null;
			return new Chapter( manga, c.getString( 0 ));
		} finally { c.close(); }
	}
	
	//--------------------------------------------------------------------------
	
	public Chapter getPreviousChapter() {
		Cursor c = Library.getDB().query(
		"chapters", new String[]{ "chapter" },
		"manga=? AND CAST( chapter AS REAL )<CAST( ? AS REAL )", new String[]{ manga.getSysName(), chapter },
		null, null, "CAST( chapter AS REAL ) DESC", "1" );
		try {
			if ( !c.moveToFirst() ) return null;
			return new Chapter( manga, c.getString( 0 ));
		} finally { c.close(); }
	}
	
	//--------------------------------------------------------------------------
	
	public Chapter getRelativeChapter( final int offset ) {
		String[] args = new String[] { manga.getSysName(), chapter };
		int length = Math.abs( offset );

		if ( offset > 0 ) {
			Cursor c = Library.getDB().query(
				"chapters", new String[] { "chapter" },
			    "manga=? AND CAST( chapter AS real )>CAST( ? AS REAL )", args,
			    null, null, "CAST( chapter AS REAL ) ASC", "" + length );
			
			try {
				if ( !c.moveToLast() || c.getCount() < length ) return null;
				return new Chapter( manga, c.getString( 0 ));
				
			} finally { c.close(); }
		}

		if ( offset < 0 ) {
			Cursor c = Library.getDB().query(
				"chapters", new String[] { "chapter" },
			    "manga=? AND CAST( chapter AS real )<CAST( ? AS REAL )", args,
			    null, null, "CAST( chapter AS REAL ) DESC", "" + length );
			
			try {
				if ( !c.moveToLast() || c.getCount() < length ) return null;
				return new Chapter( manga, c.getString( 0 ));
				
			} finally { c.close(); }
		}
		
		return this; // offset == 0
	}
	
	//--------------------------------------------------------------------------
	
	public Pin getPin() { return new Pin( this ); }
	
	//--------------------------------------------------------------------------
	
	public JsonObjectRequest getRequest( Listener<JSONObject> listener, ErrorListener error_listener ) {
		Log.d( "MJ", API.getChapterUrl( this ));
		return new JsonObjectRequest( API.getChapterUrl( this ), null, listener, error_listener );
	}
	
	//--------------------------------------------------------------------------
	
	public ChapterEditor edit() {
		return new ChapterEditor( this );
	}
	
	//--------------------------------------------------------------------------
	
	public ChapterEditor edit( JSONObject json ) throws JSONException {
		ChapterEditor edit = edit();
		if ( json.has( "title" ))      edit.setTitle(     json.getString( "title"      ));
		if ( json.has( "page_count" )) edit.setPageCount( json.getInt(    "page_count" ));
		return edit;
	}
	
	//--------------------------------------------------------------------------
	
	public Intent getIntent( Context context ) {
		return new Intent( context, ChapterActivity.class )
			.putExtra( "manga",   manga.getSysName() ) 
			.putExtra( "chapter", chapter         )
			.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public boolean equals( Object other ) {
		return other instanceof Chapter &&
			   ( (Chapter)other ).getManga().equals( getManga() ) &&
			   other.toString().equals( chapter );
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public int hashCode() {
		return 17 * getManga().hashCode() + chapter.hashCode();
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------