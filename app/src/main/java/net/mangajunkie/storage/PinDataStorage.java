package net.mangajunkie.storage;

import android.content.Context;

import java.io.File;

//==============================================================================
public class PinDataStorage extends DataStorage {
	//--------------------------------------------------------------------------
	
	public PinDataStorage( Context context ) {
		super( context );
	}
	
	//--------------------------------------------------------------------------
	
	@Override public File getRootDirectory() {
		return new File( super.getRootDirectory(), "pins" );
	}
	
	//--------------------------------------------------------------------------
	
	@Override public boolean clearCache() { return false; }
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
