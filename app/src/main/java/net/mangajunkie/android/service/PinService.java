package net.mangajunkie.android.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;

import net.mangajunkie.R;
import net.mangajunkie.android.app.App;
import net.mangajunkie.content.collection.Collection;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Manga;
import net.mangajunkie.content.library.Page;
import net.mangajunkie.network.API;
import net.mangajunkie.storage.PinDataStorage;
import net.mangajunkie.util.Files;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//==============================================================================
public class PinService
	 extends Service {
	//--------------------------------------------------------------------------
	
	public static final String ACTION_NOTIFY_PIN_CHANGED   = "net.mangajunkie.action.NOTIFY_PIN_CHANGED",
							   ACTION_REQUEST_PIN_PROGRESS = "net.mangajunkie.action.REQUEST_PIN_PROGRESS",
							   ACTION_NOTIFY_PIN_PROGRESS  = "net.mangajunkie.action.NOTIFY_PIN_PROGRESS";
	
	private static final int NOTIFICATION_ID = 1;

	private QueueTask queue_task;

	private PinDataStorage storage;

	//--------------------------------------------------------------------------

	@Override
	public void onCreate() {
		super.onCreate();
		storage = new PinDataStorage( this );
	}

	//--------------------------------------------------------------------------

	@Override
	public int onStartCommand( Intent intent, int flags, int start_id ) {
		switch ( intent.getAction() ) {
			case ACTION_NOTIFY_PIN_CHANGED:
				if ( queue_task == null ) {
					queue_task = new QueueTask();
					queue_task.execute();

				} else queue_task.notifyPinsChanged();
				break;

			case ACTION_REQUEST_PIN_PROGRESS:
				if ( !intent.hasExtra( "manga" )) break;
				if ( intent.hasExtra( "chapter" )) {
					Chapter chapter = new Manga( intent.getStringExtra( "manga" ))
					.getChapter( intent.getStringExtra( "chapter" ));
					showProgress( chapter );
				}
				break;
		}
		return START_NOT_STICKY;
	}

	//--------------------------------------------------------------------------

	// TODO: Make this count total pages of pinned chapters
	private float getProgress( Manga manga ) {
		Cursor c = Collection.getDB().query(
			"pins", new String[]{ "chapter" },
			"manga=?", new String[]{ manga.getSysName() },
			null, null, null );
		
		float count = 0;
		while ( c.moveToNext() ) count += getProgress( manga.getChapter( c.getString( 0 )));
		
		try { return count / c.getCount(); }
		finally { c.close(); }
	}
	
	//--------------------------------------------------------------------------
	
	private float getProgress( Chapter chapter ) {
		File f = storage.getDirectory( chapter );
		return f.list().length / (float)chapter.getPageCount();
	}
	
	//--------------------------------------------------------------------------
	
	private void showProgress( Chapter chapter ) {
		Manga manga = chapter.getManga();
		Bundle data = new Bundle();
		data.putString( "manga", manga.getSysName() );
		data.putFloat( "manga_progress", getProgress( manga ));
		data.putString( "chapter", chapter.toString() );
		data.putFloat( "chapter_progress", getProgress( chapter ));
		sendBroadcast( new Intent( ACTION_NOTIFY_PIN_PROGRESS ).putExtras( data ));
	}
	
	//--------------------------------------------------------------------------
		
	private void showProgressNotification( Chapter chapter ) {
		final NotificationManager manager = (NotificationManager)getSystemService( NOTIFICATION_SERVICE );
		
		// Only show the chapter title if there is one
		String content_text = chapter.getManga().getTitle() + " " + chapter.toString();
		if ( chapter.hasTitle() ) content_text += ": " + chapter.getTitle();
		
		final Notification.Builder builder = new Notification.Builder( this )
			.setSmallIcon( R.drawable.ic_notification )
			.setContentTitle( "Saving pinned chapters" )
			.setContentText( content_text )
			.setProgress( 0, 0, true );
		
		App.getImageLoader().get( API.getCoverUrl( chapter.getManga().getCover() ), new ImageListener() {

			@Override
			public void onResponse( ImageContainer container, boolean immediate ) {
				if ( queue_task == null || queue_task.isCancelled() ) return;
				builder.setLargeIcon( container.getBitmap() );
				manager.notify( NOTIFICATION_ID, builder.getNotification() );
			}

			@Override public void onErrorResponse( VolleyError volleyError ) {
				if ( queue_task == null || queue_task.isCancelled() ) return;
				manager.notify( NOTIFICATION_ID, builder.getNotification() );
			}
		} );
		
	}
	
	//--------------------------------------------------------------------------
	
	private void showCompletedNotification( int chapter_count ) {
		if ( chapter_count == 0 ) return;
		NotificationManager manager = (NotificationManager)getSystemService( NOTIFICATION_SERVICE );
		
		Notification notification = new Notification.Builder( this )
			.setSmallIcon( R.drawable.ic_notification )
			.setContentTitle( "Saving pinned chapters" )
			.setContentText( "Download complete" )
			.getNotification();
		
		manager.notify( NOTIFICATION_ID, notification );
	}
	
	//--------------------------------------------------------------------------
	// From superclass Service
	//--------------------------------------------------------------------------
	
	@Override
	public IBinder onBind( Intent intent ) { return null; }
	
	//--------------------------------------------------------------------------	
	
	//==========================================================================
	private class QueueTask extends AsyncTask<Void,Chapter,List<Chapter>> {
		//----------------------------------------------------------------------
		
		private Cursor cursor;
		private boolean pins_changed;
		
		//----------------------------------------------------------------------
		
		public QueueTask() {
			notifyPinsChanged();
			cursor = query();
		}
		
		//----------------------------------------------------------------------
	
		public void notifyPinsChanged() {
			pins_changed = true;
		}
		
		//----------------------------------------------------------------------
		
		private Cursor query() {
			pins_changed = false;
			return Collection.getDB().query(
				"pins", new String[] { "manga", "chapter" },
				null, null,
				null, null, "manga DESC" );
		}
		
		//----------------------------------------------------------------------
		
		private Chapter getNextChapter() {
			Chapter chapter;
			
			while ( cursor.moveToNext() ) {
				chapter = new Manga( cursor.getString( 0 ))
					.getChapter( cursor.getString( 1 ));
				
				if ( chapter.getPageCount() < 1 ) chapter = updateChapter( chapter );
				if ( chapter == null ) continue;
				
				if ( !storage.has( chapter )) return chapter;
			}
			
			return null;
		}
		
		//----------------------------------------------------------------------
		
		private Chapter updateChapter( Chapter chapter ) {
			try {
				InputStream stream = new URL( API.getChapterUrl( chapter )).openStream();
				Scanner scanner = new Scanner( stream ).useDelimiter( "\\A" );
				if ( !scanner.hasNext() ) return null;
				return chapter.edit( new JSONObject( scanner.next() )).apply();
				
			} catch ( Exception e ) {
				e.printStackTrace();
				return null;
			}
		}
		
		//----------------------------------------------------------------------
		
		private boolean downloadPage( Page page ) {
			try {
				// Make sure the parent dir exists
				storage.getFile( page ).getParentFile().mkdirs();
				
				InputStream  input  = new URL( API.getPageUrl( page )).openStream();
				OutputStream output = new FileOutputStream( storage.getFile( page ));
				
				// Stream copy loop
				int bytes;
				byte[] buffer = new byte[1024];
				while (( bytes = input.read( buffer )) > -1 ) output.write( buffer, 0, bytes );
				
				input .close();
				output.flush();
				output.close();
				
				return true;
				
			} catch ( IOException e ) {
				e.printStackTrace();
				return false;
			}
		}
		
		//----------------------------------------------------------------------
		
		private int cleanUnpinnedChapters() {
			int count = 0;
			
			for ( Chapter ch : storage.listChapters() ) {
				if ( !ch.getPin().exists() ) {
					Files.deleteDirectory( storage.getDirectory( ch ));
					count++;
				}
			}
			
			return count;
		}
		
		//----------------------------------------------------------------------

		@Override protected List<Chapter> doInBackground( Void... v ) {
			Chapter chapter;
			List<Chapter> completed_chapters = new ArrayList<>();
			
			// Download pinned chapters
			while (( chapter = getNextChapter() ) != null & !isCancelled() ) {
				publishProgress( chapter );
				
				for ( Page page : chapter.getPages() ) {
					if ( isCancelled() ) return null;
					downloadPage( page );
				}
				completed_chapters.add( chapter );
				if ( pins_changed ) cursor = query();
			}
			
			// Clean up unpinned chapters
			cleanUnpinnedChapters();
			
			return completed_chapters;
		}
		
		//----------------------------------------------------------------------
		
		@Override protected void onProgressUpdate( Chapter... chapters ) {
			showProgressNotification( chapters[0] );
		}
		
		//----------------------------------------------------------------------
		
		@Override protected void onCancelled() {
			if ( queue_task == this ) {
				queue_task = null;
				showCompletedNotification( 0 );
			}
			stopSelf();
		}
		
		//----------------------------------------------------------------------
		
		@Override protected void onPostExecute( List<Chapter> chapters ) {
			if ( queue_task == this ) {
				queue_task = null;
				showCompletedNotification( chapters.size() );
			}
			stopSelf();
		}
		
		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------	
}
//------------------------------------------------------------------------------
