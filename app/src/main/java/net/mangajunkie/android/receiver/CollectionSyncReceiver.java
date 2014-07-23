package net.mangajunkie.android.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

import net.mangajunkie.android.app.App;
import net.mangajunkie.database.CollectionDatabaseHelper;
import net.mangajunkie.network.API;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


//==============================================================================
public class CollectionSyncReceiver extends BroadcastReceiver {
	//--------------------------------------------------------------------------
	
	@Override
	public void onReceive( final Context context, Intent intent ) {
		if ( !App.getPrefs().getSyncEnabled() ) return;
		App.getRequestQueue().add( new JsonObjectRequest(
			API.getCollectionUrl( App.getPrefs().getSyncUser() ),
		    null,
		    new Listener<JSONObject>() {
			    @Override public void onResponse( JSONObject json ) {
				    update( context, json );
				    context.sendBroadcast( new Intent( App.ACTION_COLLECTION_SYNCED ));
			    }
		    },
		    null
		));
	}
	
	//--------------------------------------------------------------------------
	
	public static void update( Context context, JSONObject json ) {
		SQLiteDatabase db = CollectionDatabaseHelper.getInstance( context ).getWritableDatabase();
		db.beginTransaction();
		try {
			JSONArray manga   = json.getJSONArray( "manga"   ),
			          chapter = json.getJSONArray( "chapter" ),
			          page    = json.getJSONArray( "page"    );
			if ( manga.length() != chapter.length()
			|| chapter.length() != page.length() ) throw new JSONException( "" );
			
			ContentValues values = new ContentValues();
			for ( int i = 0; i < manga.length(); i++ ) {
				values.clear();
				values.put( "manga",   manga  .getString( i ));
				values.put( "chapter", chapter.getString( i ));
				values.put( "page",    page   .getInt(    i ));
				db.insertWithOnConflict( "bookmarks", null, values, SQLiteDatabase.CONFLICT_REPLACE );
			}
			
			// Clear remotely deleted bookmarks
			JSONObject names = chapter.toJSONObject( manga );
			Cursor c = db.query( "bookmarks", new String[] { "manga" }, null, null, null, null, null );
			while ( c.moveToNext() ) if ( names == null || !names.has( c.getString( 0 ))) {
				db.delete( "bookmarks", "manga=?", new String[] { c.getString( 0 ) } );
			}
			
			db.setTransactionSuccessful();
			
		} catch ( JSONException e ) {
			e.printStackTrace();
			
		} finally {
			db.endTransaction();
		}
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------