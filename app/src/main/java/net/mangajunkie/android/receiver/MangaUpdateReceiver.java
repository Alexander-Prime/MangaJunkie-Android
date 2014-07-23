package net.mangajunkie.android.receiver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import net.mangajunkie.R;
import net.mangajunkie.android.activity.HomeActivity;
import net.mangajunkie.android.activity.MangaActivity;
import net.mangajunkie.android.app.AlarmCycle;
import net.mangajunkie.android.app.App;
import net.mangajunkie.content.collection.Collection;
import net.mangajunkie.content.library.Manga;
import net.mangajunkie.network.API;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


//==============================================================================
public class MangaUpdateReceiver extends BroadcastReceiver {
	//--------------------------------------------------------------------------
	
	public static final String
		ACTION_PERFORM_MANGA_UPDATE = "net.mangajunkie.action.PERFORM_MANGA_UPDATE",
		ACTION_NOTIFY_MANGA_UPDATED = "net.mangajunkie.action.NOTIFY_MANGA_UPDATED";

	private final static long
		SLEEP_INTERVAL = AlarmManager.INTERVAL_HALF_HOUR,
		WAKE_INTERVAL  = AlarmManager.INTERVAL_HALF_DAY;
	
	private static final int NOTIFICATION_ID = 0;

	//--------------------------------------------------------------------------

	@Override
	public void onReceive( Context context, Intent intent ) {
		switch ( intent.getAction() ) {

			case Intent.ACTION_BOOT_COMPLETED:
				// Done booting, start the update cycle
				startUpdateCycle( context );
				break;

			case ACTION_PERFORM_MANGA_UPDATE:
				// An update has been requested
				doUpdate( context );
				break;

			case ACTION_NOTIFY_MANGA_UPDATED:
				// One or more manga have been updated
				// Won't be received if the collection view is visible
				notifyUpdated( context, intent.getStringArrayListExtra( "manga" ));
				break;
		}
	}

	//--------------------------------------------------------------------------

	public static void startUpdateCycle( Context context ) {
		if ( !App.getPrefs().getAutomaticUpdatesEnabled() ) return;

		new AlarmCycle( context, "autoupdate_sleep" )
			.setAction( ACTION_PERFORM_MANGA_UPDATE )
			.setInterval( SLEEP_INTERVAL )
			.setReceiver( MangaUpdateReceiver.class )
			.setWakeup( false )
			.start();

		new AlarmCycle( context, "autoupdate_wake" )
			.setAction( ACTION_PERFORM_MANGA_UPDATE )
			.setInterval( WAKE_INTERVAL )
			.setReceiver( MangaUpdateReceiver.class )
			.setWakeup( true )
			.start();
	}
	
	//--------------------------------------------------------------------------
	
	public static void doUpdate( Context context ) {
		final Context ctx = context.getApplicationContext(); // So activity context can be released
		
		App.getRequestQueue().add( new MangaUpdateRequest(
			API.getAllChaptersUrl( new Collection().getBookmarkedManga() ),
			
		    new Listener<JSONObject>() {
			    @Override public void onResponse( JSONObject json ) {
				    new ParseResponseTask( ctx ).execute( json );
			    }
		    },
		    
		    new ErrorListener() {
			    @Override public void onErrorResponse( VolleyError error ) {
				    Log.e( "MJ", "Error on server response" );
				    error.printStackTrace();
			    }
		    }
		));
	}
	
	//--------------------------------------------------------------------------
	
	public static void notifyUpdated( Context context, List<String> sys_names ) {
		NotificationManager manager = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
		StringBuilder builder = new StringBuilder();
		String delimiter = ", ";
		List<Manga> manga = new ArrayList<>(3);
		
		// Build a string listing the manga that are updated
		for ( int i = 0; i < 3 && i < sys_names.size(); i++ ) {
			manga.add( new Manga( sys_names.get( i )));
			builder.append( i == 0 ? "" : delimiter ).append( manga.get( i ).getTitle() );
		}
		
		if ( sys_names.size() > manga.size() ) {
			builder.append( delimiter ).append( sys_names.size() - manga.size() ).append( " more" );
		}
		
		// Construct intent to call when notification is selected
		Intent intent;
		if ( sys_names.size() == 1 ) {
			intent = new Intent( context, MangaActivity.class );
			intent.putExtra( "manga", sys_names.get( 0 ));	
		} else {
			intent = new Intent( context, HomeActivity.class );
		}
		PendingIntent pend = PendingIntent.getActivity( context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
		
		Notification notification = new Notification.Builder( context )
			.setSmallIcon( R.drawable.ic_notification )
			.setTicker( "New chapters" )
			.setContentTitle( "New chapters" )
			.setContentText( builder.toString() )
			.setContentIntent( pend )
			.setSound( App.getPrefs().getNotificationSoundUri() )
			.getNotification();
		
		manager.notify( NOTIFICATION_ID, notification );
	}
	
	//--------------------------------------------------------------------------
	
	//==========================================================================
	private static class MangaUpdateRequest extends JsonObjectRequest {
		//----------------------------------------------------------------------
		
		public MangaUpdateRequest( String url, Listener<JSONObject> listener, ErrorListener error_listener ) {
			super( url, null, listener, error_listener );
		}
		
		//----------------------------------------------------------------------
		
		@Override public Priority getPriority() {
			return Priority.LOW;
		}
		
		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
	
	//==========================================================================
	private static class ParseResponseTask extends AsyncTask<JSONObject,Void,ArrayList<String>> {
		//----------------------------------------------------------------------
		
		private final Context CONTEXT;
		
		//----------------------------------------------------------------------
		
		public ParseResponseTask( Context context ) {
			CONTEXT = context.getApplicationContext();
		} 
		
		//----------------------------------------------------------------------

		@Override protected ArrayList<String> doInBackground( JSONObject... jsons ) {
			JSONObject json = jsons[0];
			ArrayList<String> sys_names = new ArrayList<>();
			
			for ( Manga manga : new Collection().getBookmarkedManga() ) {
				try {
					JSONObject o = json.getJSONObject( manga.getSysName() );
					JSONArray  a = o.getJSONArray( "chapters" );
					if ( a.length() > manga.getChapters().length ) {
						sys_names.add( manga.edit( o ).apply().getSysName() );
					}
					
				} catch ( JSONException ignored ) { /* Assume no updates */ }
			}
			
			return sys_names;
		}
		
		//----------------------------------------------------------------------
		
		@Override protected void onPostExecute( ArrayList<String> sys_names ) {
			if ( sys_names.isEmpty() ) return;
			
			Intent intent = new Intent( ACTION_NOTIFY_MANGA_UPDATED )
				.putStringArrayListExtra( "manga", sys_names );
			
			CONTEXT.sendOrderedBroadcast( intent, null );
		}
		
		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
