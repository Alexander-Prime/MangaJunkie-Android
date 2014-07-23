package net.mangajunkie.content.edit;

import android.content.ContentValues;
import android.database.sqlite.SQLiteException;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;
import net.mangajunkie.android.app.App;
import net.mangajunkie.content.collection.Bookmark;
import net.mangajunkie.content.collection.Collection;
import net.mangajunkie.content.collection.User;
import net.mangajunkie.content.library.Page;
import net.mangajunkie.network.API;

//==============================================================================
public class BookmarkEditor extends Editor<Bookmark> {
	//--------------------------------------------------------------------------

	private Bookmark bookmark;
	private Page     page;
	private boolean  is_deleted;
	
	private long timestamp;

	//--------------------------------------------------------------------------

	public BookmarkEditor( Bookmark bookmark ) {
		if ( bookmark == null ) throw new NullPointerException( "Must supply non-null value for 'bookmark'" );
		this.bookmark = bookmark;
		updateTimestamp();
	}

	//--------------------------------------------------------------------------

	public BookmarkEditor setPage( Page page ) {
		if ( !bookmark.is( page.getManga() )) throw new IllegalArgumentException( "Page is from a different manga" );
		this.page = page;
		return this;
	}

	//--------------------------------------------------------------------------
	
	public BookmarkEditor updateTimestamp() {
		timestamp = System.currentTimeMillis() / 1000;
		return this;
	}
	
	//--------------------------------------------------------------------------
	
	public BookmarkEditor delete() {
		is_deleted = true;
		return this;
	}
	
	//--------------------------------------------------------------------------

	public BookmarkEditor sync( RequestQueue queue ) {
		if ( !App.getPrefs().getSyncEnabled() ) return this;
		User user = App.getPrefs().getSyncUser();
		
		if ( is_deleted ) {
			queue.add( new StringRequest(
			Method.DELETE,
			API.getBookmarkUrl( user, bookmark ),
			new Listener<String>() { @Override public void onResponse( String s ) {}},
			new ErrorListener() { @Override public void onErrorResponse( VolleyError error ) {}} ));
			
		} else {
			JSONObject post_data = new JSONObject();
			try {
				// If a page hasn't been supplied, get the one already saved
				Page post_page = page == null ? bookmark.getPage() : page;
				post_data.put( "chapter", post_page.getChapter().toString() );
				post_data.put( "page",    post_page.toString()              );
				post_data.put( "timestamp", timestamp );
			} catch ( JSONException e ) { e.printStackTrace(); }
			queue.add( new JsonObjectRequest( 
				Method.PUT,
				API.getBookmarkUrl( user, bookmark ),
				post_data,
				new Listener<JSONObject>() { @Override public void onResponse( JSONObject json ) {}},
				new ErrorListener() { @Override public void onErrorResponse( VolleyError error ) {}} ));
		}
		
		return this;
	}

	//--------------------------------------------------------------------------

	@Override
	public Bookmark apply() {
		if ( is_deleted ) {
			// Delete bookmark entry
			Collection.getDB().delete(
				"bookmarks",
				"manga=?", new String[]{ bookmark.getManga().getSysName() } );
			
		} else if ( page != null ) {
			// Commit changes to database
			ContentValues values = new ContentValues();
			values.put( "manga",     bookmark.getManga().getSysName() );
			values.put( "chapter",   page.getChapter().toString()     );
			values.put( "page",      page.toInt()                     );
			values.put( "timestamp", timestamp                        );
			try { Collection.getDB().insertOrThrow( "bookmarks", null, values ); }
			catch ( SQLiteException e ) {
				Collection.getDB().update(
					"bookmarks", values,
					"manga=?", new String[]{ bookmark.getManga().getSysName() } );
			}
		}
		
		return new Bookmark( bookmark.getManga() );
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
