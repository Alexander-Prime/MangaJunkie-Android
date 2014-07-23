package net.mangajunkie.storage;

import android.content.Context;
import net.mangajunkie.content.collection.Pin;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Manga;
import net.mangajunkie.content.library.Page;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//==============================================================================
public abstract class DataStorage {
	//--------------------------------------------------------------------------

	private final File ROOT;

	//--------------------------------------------------------------------------

	public DataStorage( Context context ) {
		ROOT = context.getExternalFilesDir( null );
	}

	//--------------------------------------------------------------------------

	public File getRootDirectory() {
		return ROOT;
	}
	
	//--------------------------------------------------------------------------
	
	public File getDirectory( Pin pin ) {
		return getDirectory( pin.getChapter() );
	}
	
	//--------------------------------------------------------------------------
	
	public File getDirectory( Manga manga ) {
		return new File( getRootDirectory(), manga.getSysName() );
	}
	
	//--------------------------------------------------------------------------
	
	public File getDirectory( Chapter chapter ) {
		return new File( getDirectory( chapter.getManga() ), chapter.toString() );
	}
	
	//--------------------------------------------------------------------------
	
	public File getFile( Page page ) {
		return new File( getDirectory( page.getChapter() ), page.toString() );
	}
	
	//--------------------------------------------------------------------------
	
	public Manga[] listManga() {
		File[] files = getRootDirectory().listFiles();
		Manga[] manga = new Manga[files.length];
		
		for ( int i = 0; i < files.length; i++ ) {
			manga[i] = new Manga( files[i].getName() );
		}
		return manga;
	}
	
	//--------------------------------------------------------------------------
	
	// All saved chapters
	public Chapter[] listChapters() {
		List<Chapter> chapters = new ArrayList<>();
		for ( Manga m : listManga() ) {
			Collections.addAll( chapters, listChapters( m ));
		}
		return chapters.toArray( new Chapter[chapters.size()] );
	}
	
	//--------------------------------------------------------------------------
	
	// Chapters of a single manga
	public Chapter[] listChapters( Manga manga ) {
		if ( manga != null ) {
			File[] files = getDirectory( manga ).listFiles();
			if ( files == null ) return new Chapter[0];
			Chapter[] chapters = new Chapter[files.length];
			
			for ( int i = 0; i < files.length; i++ ) {
				chapters[i] = new Chapter( manga, files[i].getName() );
			}
			
			return chapters;
		}
		return new Chapter[0];
	}
	
	//--------------------------------------------------------------------------
	
	public float getProgress( Pin pin ) { // 0..1
		return getProgress( pin.getChapter() );
	}
	
	//--------------------------------------------------------------------------
	
	public float getProgress( Chapter chapter ) { // 0..1
		float max = chapter.getPageCount();
		if ( max <= 0 ) return 0;
		
		float count = 0;
		for ( Page page : chapter.getPages() ) if ( has( page )) count++;
		
		return count / max;
	}
	
	//--------------------------------------------------------------------------
	
	public boolean has( Pin pin ) { return has( pin.getChapter() ); }
	
	//--------------------------------------------------------------------------

	public boolean has( Chapter chapter ) {
		// Pages on storage don't matter if we don't know how many to have
		if ( chapter.getPageCount() <= 0 ) return false;

		for ( Page page : chapter.getPages() ) if ( !has( page )) return false;
		
		return true;
	}
	
	//--------------------------------------------------------------------------
	
	public boolean has( Page page ) {
		return getFile( page ).exists();
	}

	//--------------------------------------------------------------------------
	
	public abstract boolean clearCache();
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
