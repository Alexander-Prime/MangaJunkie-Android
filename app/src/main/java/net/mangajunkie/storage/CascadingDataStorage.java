package net.mangajunkie.storage;

import android.content.Context;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Page;

import java.io.File;

//==============================================================================
public class CascadingDataStorage
extends      DataStorage {
	//--------------------------------------------------------------------------

	private final DataStorage[] STORAGES;

	//--------------------------------------------------------------------------

	public CascadingDataStorage( Context context, DataStorage... storages ) {
		super( context );
		STORAGES = storages;
	}

	//--------------------------------------------------------------------------

	@Override
	public File getDirectory( Chapter chapter ) {
		for ( DataStorage storage : STORAGES ) {
			if ( storage.has( chapter )) return storage.getDirectory( chapter ); 
		}
		return null;
	}

	//--------------------------------------------------------------------------
	
	@Override
	public File getFile( Page page ) {
		for ( DataStorage storage : STORAGES ) {
			if ( storage.has( page )) return storage.getFile( page );
		}
		return null;
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public boolean has( Chapter chapter ) {
		for ( DataStorage storage : STORAGES ) {
			if ( storage.has( chapter )) return true;
		}
		return false;
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public boolean has( Page page ) {
		for ( DataStorage storage : STORAGES ) {
			if ( storage.has( page )) return true;
		}
		return false;
	}
	
	//--------------------------------------------------------------------------
	
	@Override public boolean clearCache() {
		boolean cleared = false;
		for ( DataStorage storage : STORAGES ) {
			cleared = cleared || storage.clearCache();
		}
		return cleared;
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
