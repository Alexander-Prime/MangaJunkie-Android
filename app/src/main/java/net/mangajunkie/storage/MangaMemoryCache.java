package net.mangajunkie.storage;

import android.graphics.Bitmap;
import android.util.LruCache;
import com.android.volley.toolbox.ImageLoader.ImageCache;

//==============================================================================
public class MangaMemoryCache
extends      LruCache<String,Bitmap>
implements   ImageCache {
	//--------------------------------------------------------------------------
	
	public MangaMemoryCache() {
		super( 10 );
	}
	//--------------------------------------------------------------------------

	@Override
	public Bitmap getBitmap( String s ) {
		return get( s );
	}
	
	//--------------------------------------------------------------------------

	@Override
	public void putBitmap( String s, Bitmap bitmap ) {
		put( s, bitmap );
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
