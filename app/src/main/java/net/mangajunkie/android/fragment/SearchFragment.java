package net.mangajunkie.android.fragment;

import android.app.Fragment;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;
import net.mangajunkie.R;
import net.mangajunkie.android.app.App;
import net.mangajunkie.android.app.IntentProvider;
import net.mangajunkie.content.library.Cover;
import net.mangajunkie.content.library.Manga;
import net.mangajunkie.network.API;

import java.util.HashMap;
import java.util.Map;

//==============================================================================
public class   SearchFragment
	extends    Fragment
	implements OnItemClickListener {
	//--------------------------------------------------------------------------

	private SearchAdapter adapter       = new SearchAdapter();
	private JSONObject    query_results = new JSONObject();

	private static final int COVER_SIZE = 320;

	//--------------------------------------------------------------------------

	public void invalidateQuery() {
		setResultsBrowseable( false );
	}

	//--------------------------------------------------------------------------

	public void query( String query ) {
		App.getRequestQueue().cancelAll( this );
		Request r = new JsonObjectRequest(
		API.getSearchUrl( query ),
		null,
		new Listener<JSONObject>() {
			@Override public void onResponse( JSONObject json ) {
				query_results = json;
				adapter.notifyDataSetChanged();
				setResultsBrowseable( true );
			}
		},
		new ErrorListener() {
			@Override public void onErrorResponse( VolleyError error ) {
				// Bad response, probably bad query
				query_results = new JSONObject();
				adapter.notifyDataSetChanged();
				setResultsBrowseable( true );
			}
		}
		);
		r.setTag( this );
		App.getRequestQueue().add( r );
	}

	//--------------------------------------------------------------------------

	private void setResultsBrowseable( boolean browseable ) {
		if ( getView() == null ) return;
		getView().findViewById( R.id.list ).setEnabled( browseable );
		getView().findViewById( R.id.list ).setAlpha( browseable ? 1.0f : 0.5f );
		getView().findViewById( R.id.progress ).setVisibility( browseable ? View.GONE : View.VISIBLE );
	}

	//--------------------------------------------------------------------------

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup parent, Bundle state ) {
		View view = inflater.inflate( R.layout.fragment_search, parent, false );

		ListView list = (ListView)view.findViewById( R.id.list );
		list.setAdapter( adapter );
		list.setOnItemClickListener( this );

		return view;
	}

	//--------------------------------------------------------------------------
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		adapter.cancelAll();
	}

	//--------------------------------------------------------------------------

	@Override
	public void onItemClick( AdapterView<?> parent, View view, int index, long id ) {
		IntentProvider result = adapter.getItem( index );
		startActivity( result.getIntent( getActivity() ));
	}
	
	//--------------------------------------------------------------------------
	
	//==========================================================================
	private class SearchAdapter extends BaseAdapter {
		//----------------------------------------------------------------------

		private Map<View, ImageContainer> cover_requests = new HashMap<>();
		
		//----------------------------------------------------------------------

		@Override
		public int getCount() {
			try { return query_results.getJSONArray( "sys_name" ).length(); }
			catch ( JSONException e ) { return 0; }
		}

		//----------------------------------------------------------------------
		
		@Override
		public Manga getItem( int index ) {
			try { return new Manga( query_results.getJSONArray( "sys_name" ).getString( index ));}
			catch ( JSONException e ) { return null; }
		}

		//----------------------------------------------------------------------

		@Override
		public long getItemId( int index ) {
			try { return query_results.getJSONArray( "id" ).getLong( index );}
			catch ( JSONException e ) { return 0; }
		}

		//----------------------------------------------------------------------
		
		public void cancelAll() {
			for ( ImageContainer request : cover_requests.values() ) request.cancelRequest();
		}
		
		//----------------------------------------------------------------------

		@Override
		public View getView( int index, View view, ViewGroup parent ) {
			if ( view == null ) { view = getActivity().getLayoutInflater().inflate( R.layout.listitem_search, parent, false ); }
			
			String sys_name, title, author, tags;
			try {
				sys_name = query_results.getJSONArray( "sys_name" ).getString( index );
				title    = query_results.getJSONArray( "title"    ).getString( index );
				author   = query_results.getJSONArray( "author"   ).getString( index );
				tags     = query_results.getJSONArray( "tags"     ).getString( index );
				
				author = author == null || author.equals( "null" ) ? "" : "by " + author;
				
			} catch ( JSONException e ) {
				sys_name = "";
				title    = "error";
				author   = "contact developer";
				tags     = null;
			}
			
			final Cover cover = new Manga( sys_name ).getCover();
			final ImageView cover_view = (ImageView)view.findViewById( R.id.cover );
			cover_view.setTag( cover );
			cover_view.setImageDrawable( null );
			

			( (TextView)view.findViewById( R.id.title  )).setText( title );
			( (TextView)view.findViewById( R.id.author )).setText( author );
			( (TextView)view.findViewById( R.id.tags   )).setText( tags   );
			
			
			// Queue and store image request for cover
			if ( cover_requests.containsKey( view )) cover_requests.get( view ).cancelRequest();
			
			ImageContainer request = App.getImageLoader().get(
				API.getCoverUrl( new Manga( sys_name ).getCover() ),
				new ImageListener() {
					@Override public void onResponse( ImageContainer container, boolean instant ) {
						if ( !cover.equals( cover_view.getTag() )) return;
						if ( instant ) cover_view.setImageBitmap( container.getBitmap() );
						else {
							TransitionDrawable cover_transition = new TransitionDrawable( new Drawable[]{
								new ColorDrawable(),
								new BitmapDrawable( getResources(), container.getBitmap() ) } );
							cover_view.setImageDrawable( cover_transition );
							cover_transition.startTransition( 200 );
						}
					}

					@Override public void onErrorResponse( VolleyError volleyError ) {
						if ( !cover.equals( cover_view.getTag() )) return;
						cover_view.setImageDrawable( null );
					}
				},
				COVER_SIZE, COVER_SIZE );
			
			cover_requests.put( view, request );
			
			return view;
		}

		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
