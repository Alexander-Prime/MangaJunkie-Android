package net.mangajunkie.android.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import net.mangajunkie.content.collection.Bookmark;
import net.mangajunkie.content.collection.Collection;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Manga;
import net.mangajunkie.content.library.Page;
import net.mangajunkie.error.MissingContentException;
import net.mangajunkie.network.API;
import net.mangajunkie.storage.PrefetchDataStorage;
import net.mangajunkie.util.Files;

import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Scanner;

//==============================================================================
public class PrefetchService extends Service {
	//--------------------------------------------------------------------------
	
	public static final String ACTION_NOTIFY_BOOKMARKS_CHANGED = "net.mangajunkie.action.NOTIFY_BOOKMARKS_CHANGED";
	
	private static final int PREFETCH_LIMIT = 3;
	
	private QueueTask queue_task;
	
	private PrefetchDataStorage storage;
	
	//--------------------------------------------------------------------------

	@Override public void onCreate() {
		super.onCreate();
		storage = new PrefetchDataStorage( this );
	}
	
	//--------------------------------------------------------------------------
	
	@Override public int onStartCommand( Intent intent, int flags, int start_id ) {
		Log.d( "MJ", "onStartCommand()" );
		if ( ACTION_NOTIFY_BOOKMARKS_CHANGED.equals( intent.getAction() )) {
			if ( queue_task == null ) {
				queue_task = new QueueTask();
				queue_task.executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR );

			} else queue_task.notifyBookmarksChanged();
		}
		
		return START_NOT_STICKY;
	}
	
	//--------------------------------------------------------------------------
	// From superclass Service
	//--------------------------------------------------------------------------
	
	@Override
	public IBinder onBind( Intent intent ) { return null; }
	
	//--------------------------------------------------------------------------
	
	//==========================================================================
	private class QueueTask extends AsyncTask<Void,Chapter,Void> {
		//----------------------------------------------------------------------


		private Cursor cursor;
		private boolean bookmarks_changed;
		private int depth = 0;

		//----------------------------------------------------------------------

		public QueueTask() {
			notifyBookmarksChanged();
			cursor = query();
		}

		//----------------------------------------------------------------------

		public void notifyBookmarksChanged() {
			bookmarks_changed = true;
		}

		//----------------------------------------------------------------------

		private Cursor query() {
			bookmarks_changed = false;
			depth = 0;
			return Collection.getDB().query(
			"bookmarks", new String[]{ "manga", "chapter" },
			null, null, null, null, null );
		}

		//----------------------------------------------------------------------

		private Chapter getNextChapter() {
			Chapter chapter;

			while ( cursor.moveToNext() ) {
				chapter = new Manga( cursor.getString( 0 ))
				.getChapter( cursor.getString( 1 )).getRelativeChapter( depth );
				
				if ( chapter == null ) continue;
				try {
					if ( chapter.getPageCount() < 1 ) {
						chapter = updateChapter( chapter );
					}
				} catch ( MissingContentException e ) {
					continue;
				} // Bad chapter in bookmark

				if ( !storage.has( chapter )) return chapter;
			}
			
			// This depth is complete; increment and try again
			if ( depth < PREFETCH_LIMIT ) {
				depth++;
				cursor.moveToPosition( -1 );
				return getNextChapter();
			}
			
			return null;
		}

		//----------------------------------------------------------------------

		private Chapter updateChapter( Chapter chapter ) {
			try {
				InputStream stream = new URL( API.getChapterUrl( chapter ) ).openStream();
				Scanner scanner = new Scanner( stream ).useDelimiter( "\\A" );
				if ( !scanner.hasNext() ) return null;
				return chapter.edit( new JSONObject( scanner.next() ) ).apply();

			} catch ( Exception e ) {
				e.printStackTrace();
				return null;
			}
		}

		//----------------------------------------------------------------------

		private boolean downloadPage( Page page, byte[] buffer ) {
			try {
				// Make sure the parent dir exists
				storage.getFile( page ).getParentFile().mkdirs();

				InputStream input = new URL( API.getPageUrl( page ) ).openStream();
				OutputStream output = new FileOutputStream( storage.getFile( page ) );

				// Stream copy loop
				int bytes;
				while ( ( bytes = input.read( buffer ) ) > -1 )
					output.write( buffer, 0, bytes );

				input.close();
				output.flush();
				output.close();

				return true;

			} catch ( IOException e ) {
				e.printStackTrace();
				return false;
			}
		}
		
		//----------------------------------------------------------------------
		
		private void cleanFinishedChapters() {
			for ( Manga manga : storage.listManga() ) {
				Bookmark bookmark = new Bookmark( manga );
				if ( !bookmark.exists() ) {
					Files.deleteDirectory( storage.getDirectory( manga ));
					continue;
				}
				
				Chapter min_ch = bookmark.getChapter(),
				        max_ch = min_ch.getRelativeChapter( PREFETCH_LIMIT - 1 );
				
				if ( max_ch == null ) max_ch = bookmark.getManga().getLatestChapter();
				
				float min_float = min_ch.toFloat(),
				      max_float = max_ch.toFloat();
				
				for ( Chapter chapter : storage.listChapters( manga )) {
					if ( chapter.toFloat() < min_float || chapter.toFloat() > max_float ) {
						Files.deleteDirectory( storage.getDirectory( chapter ));
					}
				}
			}
		}
		
		//----------------------------------------------------------------------

		@Override protected Void doInBackground( Void... v ) {
			Chapter chapter;
			byte[] buffer = new byte[1024];
			
			// Download pinned chapters
			while (( chapter = getNextChapter() ) != null & !isCancelled() ) {
				publishProgress( chapter );
				Log.d( "MJ", "Working on " + chapter.getManga() + "/" + chapter );
				
				for ( Page page : chapter.getPages() ) {
					if ( isCancelled() ) return null;
					downloadPage( page, buffer );
				}
				
				if ( bookmarks_changed ) cursor = query();
			}
			
			// Clean up unpinned chapters
			cleanFinishedChapters();
			
			return null;
		}
		
		//----------------------------------------------------------------------
		
		@Override protected void onProgressUpdate( Chapter... chapters ) {
			// ...Do nothing?
		}
		
		//----------------------------------------------------------------------
		
		@Override protected void onCancelled() {
			if ( queue_task == this ) queue_task = null;
			stopSelf();
		}
		
		//----------------------------------------------------------------------
		
		@Override protected void onPostExecute( Void v ) {
			if ( queue_task == this ) queue_task = null;
			stopSelf();
		}
		
		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
