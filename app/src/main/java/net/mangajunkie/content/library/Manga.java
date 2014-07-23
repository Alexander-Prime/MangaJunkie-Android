package net.mangajunkie.content.library;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import net.mangajunkie.android.activity.MangaActivity;
import net.mangajunkie.android.app.IntentProvider;
import net.mangajunkie.content.collection.Bookmark;
import net.mangajunkie.content.edit.MangaEditor;
import net.mangajunkie.error.MissingContentException;
import net.mangajunkie.network.API;

//==============================================================================
public class Manga implements IntentProvider {
	//--------------------------------------------------------------------------
	
	private final String sys_name;

	//--------------------------------------------------------------------------

	public Manga( String sys_name ) {
		this.sys_name = sys_name;
	}

	//--------------------------------------------------------------------------
	
	public static boolean exists( String sys_name ) {
		Cursor c = Library.getDB().query(
				"manga", new String[]{ "COUNT(sys_name)" },
		        "sys_name=?", new String[]{ sys_name },
		        null, null, null );
		try { return c.getCount() > 0; }
		finally { c.close(); }
	}
	
	//--------------------------------------------------------------------------

	public Author getAuthor() {
		Cursor c = Library.getDB().query(
				"manga", new String[] { "author" },
		        "sys_name=?", new String[] { sys_name },
		        null, null, null, "1" );
		try {
			if ( !c.moveToFirst() ) throw new MissingContentException( this );
			return new Author( c.getString( 0 ));
		} finally { c.close(); }
	}

	//--------------------------------------------------------------------------
	
	public Chapter[] getChapters() {
		Cursor c = Library.getDB().query(
				"chapters", new String[]{ "chapter" },
				"manga=?",  new String[]{ sys_name },
				null, null, "CAST( chapter AS INTEGER )"
				);
		try {
			Chapter[] ch = new Chapter[c.getCount()];
			while ( c.moveToNext() ) {
				ch[c.getPosition()] = new Chapter( this, c.getString( 0 ));
			}
			return ch;
		} finally { c.close(); }
	}
	
	//--------------------------------------------------------------------------
	
	public Chapter getLatestChapter() {
		Cursor c = Library.getDB().query(
				"chapters", new String[]{ "chapter" },
				"manga=?",  new String[]{ sys_name },
				null, null, "CAST( chapter AS REAL ) DESC", "1" );
		if ( c.moveToFirst() ) return new Chapter( this, c.getString( 0 ));
		else return null;
	}
	
	//--------------------------------------------------------------------------
	
	public String getSysName() { return sys_name; }
	
	//--------------------------------------------------------------------------
	
	public String getSummary() {
		Cursor c = Library.getDB().query(
				"manga", new String[] { "summary" },
		        "sys_name=?", new String[] { sys_name },
		        null, null, null, "1" );
		try {
			if ( !c.moveToFirst() ) throw new MissingContentException( this );
			return c.getString( 0 );
		} finally { c.close(); }
	}
	
	//--------------------------------------------------------------------------
	
	public Tag[] getTags() {
		Cursor c = Library.getDB().query(
				"manga_tags", new String[] { "tag" },
		        "manga=?", new String[] { sys_name },
		        null, null, "tag ASC" );
		try {
			Tag[] tags = new Tag[c.getCount()];
			while ( c.moveToNext() ) tags[c.getPosition()] = new Tag( c.getString( 0 ));
			return tags;
		} finally { c.close(); }
	}
	
	//--------------------------------------------------------------------------
	
	public String getTitle() {
		Cursor c = Library.getDB().query(
				"manga", new String[] { "title" },
		        "sys_name=?", new String[] { sys_name },
		        null, null, null, "1" );
		try {
			if ( !c.moveToFirst() ) throw new MissingContentException( this );
			return c.getString( 0 );
		} finally { c.close(); }
	}

	//--------------------------------------------------------------------------
	
	public boolean isBroken() {
		Cursor c = Library.getDB().query(
			"manga", new String[] { "broken" },
		    "sys_name=?", new String[] { sys_name },
		    null, null, null, "1" );
		try {
			if ( !c.moveToFirst() ) throw new MissingContentException( this );
			return c.getInt( 0 ) != 0;
		} finally { c.close(); }
	}
	
	//--------------------------------------------------------------------------
	
	public Cover getCover() {
		return new Cover( this );
	}
	
	//--------------------------------------------------------------------------
	
	public Chapter getChapter( String chapter ) {
		return new Chapter( this, chapter );
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public Intent getIntent( Context context ) {
		return new Intent( context, MangaActivity.class ).putExtra( "manga", sys_name ).setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
	}
	
	//--------------------------------------------------------------------------
	
	@TargetApi( Build.VERSION_CODES.JELLY_BEAN )
	public Bundle getActivityOpts( View zoom_view ) {
		if ( zoom_view != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
			return ActivityOptions.makeScaleUpAnimation( zoom_view, 0, 0, zoom_view.getWidth(), zoom_view.getHeight() ).toBundle();
		} else return null;
	}
	
	//--------------------------------------------------------------------------
	
	public Request getRequest( Listener<JSONObject> listener, ErrorListener error_listener, Object tag ) {
		Request request = new JsonObjectRequest( API.getMangaUrl( this ), null, listener, error_listener );
		request.setTag( tag );
		return request;
	}
	
	//--------------------------------------------------------------------------
	
	public JsonObjectRequest getChaptersRequest( Listener<JSONObject> listener, ErrorListener error_listener ) {
		return new JsonObjectRequest( API.getAllChaptersUrl( this ), null, listener, error_listener );
	}
	
	//--------------------------------------------------------------------------
	
	public Bookmark getBookmark() {
		return new Bookmark( this );
	}
	
	//--------------------------------------------------------------------------
	
	public MangaEditor edit() { return new MangaEditor( this ); }
	
	//--------------------------------------------------------------------------
	
	public MangaEditor edit( JSONObject json ) throws JSONException {
		MangaEditor edit = edit();
		if ( json.has( "title"   )) edit.setTitle( json.getString( "title" ));
		if ( json.has( "author"  )) edit.setAuthor( new Author( json.getString( "author" )));
		if ( json.has( "summary" )) edit.setSummary( json.getString( "summary" ));
		if ( json.has( "tags"    )) {
			JSONArray tags_json = json.getJSONArray( "tags" );
			Tag[] tags = new Tag[tags_json.length()];
			for ( int i = 0; i < tags_json.length(); i++ ) {
				tags[i] = new Tag( tags_json.getString( i ));
			}
			edit.setTags( tags );
		}
		if ( json.has( "chapters" ) && json.has( "chapter_titles" )) {
			int length = json.getJSONArray( "chapters" ).length();
			String[] chapters       = new String[length],
			         chapter_titles = new String[length];
			for ( int i = 0; i < length; i++ ) {
				chapters      [i] = json.getJSONArray( "chapters"       ).getString( i );
				chapter_titles[i] = json.getJSONArray( "chapter_titles" ).getString( i );
			}
			edit.setChapters( chapters, chapter_titles );
		}
		return edit;
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public boolean equals( Object o ) {
		return o instanceof Manga &&
			   ( (Manga)o ).getSysName().equals( sys_name );
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public int hashCode() {
		return sys_name.hashCode();
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return sys_name;
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------