package net.mangajunkie.storage;

import android.content.Context;
import net.mangajunkie.util.Files;

import java.io.File;

//==============================================================================
public class CacheDataStorage extends DataStorage {
	//--------------------------------------------------------------------------
	
	private final File ROOT;
	
	//--------------------------------------------------------------------------
	
	public CacheDataStorage( Context context ) {
		super( context );
		ROOT = context.getExternalCacheDir();
	}
	
	//--------------------------------------------------------------------------
	
	@Override public File getRootDirectory() {
		return ROOT;
	}
	
	//--------------------------------------------------------------------------
	
	@Override public boolean clearCache() {
		Files.deleteDirectory( ROOT );
		return true;
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
