package net.mangajunkie.error;

import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Manga;

//==============================================================================
public class MissingContentException extends RuntimeException {
	//--------------------------------------------------------------------------
	
	public MissingContentException( Manga manga ) {
		super( "Missing manga "  + manga.getSysName() );
	}
	
	//--------------------------------------------------------------------------
	
	public MissingContentException( Chapter chapter ) {
		super( "Missing chapter " + chapter.toString() + " of " + chapter.getManga().getSysName() );
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
