package net.mangajunkie.android.app;

import android.app.Application;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import net.mangajunkie.content.collection.Collection;
import net.mangajunkie.content.library.Library;
import net.mangajunkie.database.CollectionDatabaseHelper;
import net.mangajunkie.database.LibraryDatabaseHelper;
import net.mangajunkie.android.receiver.MangaUpdateReceiver;
import net.mangajunkie.storage.MangaMemoryCache;

//==============================================================================
public class App
     extends Application {
	//--------------------------------------------------------------------------

	
	public static final String ACTION_SYNC_COLLECTION   = "net.mangajunkie.action.SYNC_COLLECTION",
	                           ACTION_COLLECTION_SYNCED = "net.mangajunkie.action.COLLECTION_SYNCED";

	private static DiskBasedCache DISK_CACHE;
	private static ImageCache     MEMORY_CACHE;
	private static RequestQueue   REQUEST_QUEUE;
	private static ImageLoader    IMAGE_LOADER;

	private static PreferenceWrapper PREFS;

	private static final int DISK_CACHE_SIZE = 4 * 1024 * 1024; // 4 MiB

	//--------------------------------------------------------------------------

	@Override
	public void onCreate() {
		super.onCreate();

		PREFS = new PreferenceWrapper( this );
		PREFS.listen();

		DISK_CACHE = new DiskBasedCache( getExternalCacheDir(), DISK_CACHE_SIZE );
		MEMORY_CACHE = new MangaMemoryCache();

		// Use the same disk cache for main and background download queues
		REQUEST_QUEUE = new RequestQueue( DISK_CACHE, new BasicNetwork( new HurlStack() ));
		REQUEST_QUEUE.start();

		// Use a custom in-memory LruCache for the loader
		IMAGE_LOADER = new ImageLoader( REQUEST_QUEUE, MEMORY_CACHE );

		// Setup databases
		Library.setDB( LibraryDatabaseHelper.getInstance( this ).getWritableDatabase() );
		Collection.setDB( CollectionDatabaseHelper.getInstance( this ).getWritableDatabase() );

		MangaUpdateReceiver.startUpdateCycle( this );
	}

	//--------------------------------------------------------------------------

	public static DiskBasedCache    getDiskCache()           { return DISK_CACHE;            }
	public static ImageCache        getMemoryCache()         { return MEMORY_CACHE;          }
	public static RequestQueue      getRequestQueue()        { return REQUEST_QUEUE;         }
	public static ImageLoader       getImageLoader()         { return IMAGE_LOADER;          }
	public static PreferenceWrapper getPrefs()               { return PREFS;                 }
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------