package net.mangajunkie.util;

import java.io.File;

//==============================================================================
public abstract class Files {
	//--------------------------------------------------------------------------
	/**
	 * Deletes a directory and all files and subdirectories within it
	 * 
	 * @return true if deleting succeeded*/
	public static final boolean deleteDirectory( File dir ) {
		if ( dir == null ) return false;
		if ( !dir.isDirectory() ) return dir.delete();
		
		for ( File child : dir.listFiles() ) {
			if ( !deleteDirectory( child )) return false;
		}
		
		return true; // Nothing broke!
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
