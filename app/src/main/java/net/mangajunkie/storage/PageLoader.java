package net.mangajunkie.storage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.os.AsyncTask;
import net.mangajunkie.content.library.Page;
import net.mangajunkie.network.API;
import net.mangajunkie.util.Net;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

//==============================================================================
public class PageLoader {
	//--------------------------------------------------------------------------

	private final CascadingDataStorage                     STORAGE;
	private final CacheDataStorage                         CACHE;
	private final Map<Page, AsyncTask<Void, Void, Bitmap>> TASKS;

	private final Options OPTS = new Options();

	{
		OPTS.inTempStorage = new byte[1024];
	}

	// Contstrain loaded image size to screen dimensions
	// TODO Find a better size
	private final static int IMAGE_SIZE = 2048;

	//--------------------------------------------------------------------------

	public PageLoader( Context context ) {
		CACHE = new CacheDataStorage( context );
		STORAGE = new CascadingDataStorage( context, CACHE, new PrefetchDataStorage( context ), new PinDataStorage( context ));
		TASKS = new HashMap<>();
	}

	//--------------------------------------------------------------------------

	public void load( Page page, OnPageLoadedListener listener ) {
		load( page, -1, -1, listener );
	}

	//--------------------------------------------------------------------------

	public void load( Page page, int target_width, int target_height, OnPageLoadedListener listener ) {
		if ( STORAGE.has( page )) loadFromStorage( page, target_width, target_height, listener );
		else loadFromNetwork( page, target_width, target_height, listener );
	}
	
	//--------------------------------------------------------------------------
	
	private void loadFromStorage( final Page page, final int target_width, final int target_height, final OnPageLoadedListener listener ) {
		cancel( page );
		
		AsyncTask<Void,Void,Bitmap> t = new AsyncTask<Void,Void,Bitmap>() {
			@Override protected Bitmap doInBackground( Void... X ) {
				if ( isCancelled() ) return null;
				
				Bitmap b = BitmapFactory.decodeFile( STORAGE.getFile( page ).getAbsolutePath(), OPTS );

				if ( target_width <= 0 || target_height <= 0 ) return b;
				else return padBitmap( b, (float)target_height / target_width, 0xFF00FFFF );
			}
			
			@Override protected void onPostExecute( Bitmap result ) {
				if ( result != null ) listener.onPageLoaded( page, result );
				else listener.onPageLoadFailed( page );
			}
		};
		
		TASKS.put( page, t );
		t.execute();
	}
	
	//--------------------------------------------------------------------------
	
	private void loadFromNetwork( final Page page, final int target_width, final int target_height, final OnPageLoadedListener listener ) {
		cancel( page );
		
		AsyncTask<Void,Void,Bitmap> t = new AsyncTask<Void,Void,Bitmap>() {
			@Override protected Bitmap doInBackground( Void... X ) {
				if ( isCancelled() ) return null;

				String source = API.getPageUrl( page );
				File destination = CACHE.getFile( page );
				
				if ( !Net.download( source, destination )) cancel( false );
				return null;
			}
			
			@Override protected void onPostExecute( Bitmap X ) {
				loadFromStorage( page, target_width, target_height, listener );
			}
		};
		
		TASKS.put( page, t );
		t.execute();
	}
	
	//--------------------------------------------------------------------------
	
	private static Bitmap padBitmap( Bitmap b, float aspect, int color ) {
		int w = (int)Math.max( b.getHeight() * aspect, b.getWidth()  ),
		    h = (int)Math.max( b.getWidth()  / aspect, b.getHeight() ),
		    left = ( w - b.getWidth()  ) / 2,
		    top  = ( h - b.getHeight() ) / 2;
		
		Bitmap padded_bitmap = Bitmap.createBitmap( w, h, Config.RGB_565 );
		Canvas c = new Canvas( padded_bitmap );
		c.drawColor( color );
		c.drawBitmap( b, left, top, null );
		
		return padded_bitmap;
	}
	
	//--------------------------------------------------------------------------
	
	public void clear() {
		for ( AsyncTask task : TASKS.values() ) try {
			TASKS.remove( task ).cancel( false );
		} catch ( NullPointerException e ) { /* That's fine, whatever. */ }
		STORAGE.clearCache();
	}
	
	//--------------------------------------------------------------------------
	
	public void cancel( Page page ) {
		AsyncTask old_t = TASKS.remove( page );
		if ( old_t != null ) old_t.cancel( false );
	}
	
	//--------------------------------------------------------------------------
	
	//==========================================================================
	public interface OnPageLoadedListener {
		//----------------------------------------------------------------------
		
		public void onPageLoaded( Page page, Bitmap image );
		
		//----------------------------------------------------------------------
		
		public void onPageLoadFailed( Page page );
		
		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
