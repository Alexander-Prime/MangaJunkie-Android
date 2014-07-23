package net.mangajunkie.storage;

import android.content.Context;

import java.io.File;

//==============================================================================
public class PrefetchDataStorage extends DataStorage {
	//--------------------------------------------------------------------------

	public PrefetchDataStorage( Context context ) { super( context ); }

	//--------------------------------------------------------------------------
	
	@Override public File getRootDirectory() {
		return new File( super.getRootDirectory(), "prefetch" );
	}
	
	//--------------------------------------------------------------------------
	
	@Override public boolean clearCache() { return false; }
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
