package net.mangajunkie.content.library;

import android.content.Context;
import android.content.Intent;

//==============================================================================
public class Page {
	//--------------------------------------------------------------------------

	private final int     page;
	private final Chapter chapter;
	
	//--------------------------------------------------------------------------
	
	public Page( Chapter chapter, int page ) {
		this.chapter = chapter;
		this.page    = page;
	}
	
	//--------------------------------------------------------------------------
	
	public Manga getManga() { return chapter.getManga(); }
	
	//--------------------------------------------------------------------------
	
	public Chapter getChapter() { return chapter; }
	
	//--------------------------------------------------------------------------
	
	@Override
	public String toString() { return String.valueOf( page ); }
	
	//--------------------------------------------------------------------------
	
	public int toInt() { return page; }
	
	//--------------------------------------------------------------------------

	public float toFloat() { return page; }
	
	//--------------------------------------------------------------------------
	
	public Intent getIntent( Context context ) {
		return chapter.getIntent( context ).putExtra( "page", page );
	}
	
	//--------------------------------------------------------------------------
	
	@Override public boolean equals( Object other ) {
		return other instanceof Page && ( (Page)other ).getChapter().equals( chapter ) && ( (Page)other ).toInt() == page;
	}
	
	//--------------------------------------------------------------------------

	@Override public int hashCode() {
		return 17 * chapter.hashCode() + page;
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
