package net.mangajunkie.android.fragment;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import net.mangajunkie.R;
import net.mangajunkie.android.activity.PrefsActivity;
import net.mangajunkie.android.app.App;
import net.mangajunkie.android.app.PreferenceWrapper;
import net.mangajunkie.content.collection.Collection;
import net.mangajunkie.content.library.Cover;
import net.mangajunkie.content.library.Manga;
import net.mangajunkie.graphics.BookmarkDrawable;
import net.mangajunkie.network.API;

import java.util.HashMap;
import java.util.Map;

//==============================================================================
public class CollectionFragment
     extends Fragment
  implements OnClickListener,
             OnItemClickListener,
             OnItemLongClickListener {
	//--------------------------------------------------------------------------

	private GridView          grid;
	private CollectionAdapter adapter;
	private View              collection_empty, banner_accountSync;
	private BroadcastReceiver update_receiver;
	//private User              user;

	private Map<View, ImageContainer> cover_requests = new HashMap<>();

	private static final IntentFilter UPDATE_FILTER = new IntentFilter( App.ACTION_COLLECTION_SYNCED );

	//--------------------------------------------------------------------------

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		adapter = new CollectionAdapter();

		update_receiver = new BroadcastReceiver() {
			@Override
			public void onReceive( Context context, Intent intent ) {
				adapter.query();
				updateViewVisibility();
			}
		};

		//user = App.getPrefs().getSyncUser();

		getActivity().registerReceiver( update_receiver, UPDATE_FILTER );
	}

	//--------------------------------------------------------------------------

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle state ) {
		View output = inflater.inflate( R.layout.fragment_collection, container, false );

		grid = ( (GridView)output.findViewById( R.id.grid ) );
		collection_empty = output.findViewById( R.id.notice_empty );

		grid.setAdapter( adapter );
		grid.setOnItemClickListener( this );
		grid.setOnItemLongClickListener( this );

		banner_accountSync = output.findViewById( R.id.banner_accountSync );

		TextView text = (TextView)output.findViewById( R.id.banner_accountSync_text );
		text.setText( text.getText() + " " + App.getPrefs().getSyncUser().getUsername() );
		output.findViewById( R.id.banner_accountSync_button_remove ).setOnClickListener( this );
		output.findViewById( R.id.banner_accountSync_button_settings ).setOnClickListener( this );

		return output;
	}

	//--------------------------------------------------------------------------

	@Override
	public void onActivityCreated( Bundle state ) {
		super.onActivityCreated( state );

		if ( state == null ) getActivity().sendBroadcast( new Intent( App.ACTION_SYNC_COLLECTION ));
	}

	//--------------------------------------------------------------------------

	@Override
	public void onResume() {
		super.onResume();

		PreferenceWrapper prefs = App.getPrefs();
		
		if ( !prefs.getSyncConfirmed() && prefs.getSyncEnabled() ) {
			App.getPrefs().edit().setSyncConfirmed( true ).apply();
			
		} else {
			banner_accountSync.setVisibility( View.GONE );
		}
		
		adapter.query();
		updateViewVisibility();
	}
	
    //--------------------------------------------------------------------------
	
	@Override
	public void onDestroy() {
		super.onDestroy();
        
		getActivity().unregisterReceiver( update_receiver );
		for ( ImageContainer request : cover_requests.values() ) request.cancelRequest();
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public void onClick( View view ) {
		switch( view.getId() ) {
		
		case R.id.banner_accountSync_button_remove:
			App.getPrefs().edit().setSyncConfirmed( true ).apply();
			break;
			
		case R.id.banner_accountSync_button_settings:
			startActivity( new Intent( getActivity(), PrefsActivity.class )
				.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT,
						"net.mangajunkie.activity.PrefsActivity$AccountSyncFragment" ));
			break;
		}
		
		banner_accountSync.setVisibility( View.GONE );
	}
	
	//--------------------------------------------------------------------------
	
	@TargetApi( Build.VERSION_CODES.JELLY_BEAN )
	@Override
	public void onItemClick( AdapterView<?> parent, View view, int index, long id ) {
		Manga manga = adapter.getItem( index );
		
		getActivity().startActivity( manga.getIntent( getActivity() ));
	}
    
	//--------------------------------------------------------------------------
	
	@Override
	public boolean onItemLongClick( AdapterView<?> parent, View view, int index, long id ) {
		Toast.makeText( getActivity(), adapter.getItem( index ).getTitle(), Toast.LENGTH_SHORT ).show();
		return true;
	}
    
	//--------------------------------------------------------------------------
	
	private void updateViewVisibility() {Cursor cursor = Collection.getDB().rawQuery( "SELECT COUNT(*) FROM bookmarks WHERE chapter=CAST(chapter AS REAL)", null );
		if ( cursor.moveToFirst() && cursor.getInt( 0 ) > 0 ) {
			collection_empty.setVisibility( View.GONE );
		} else {
			collection_empty.setVisibility( View.VISIBLE );
		}
		cursor.close();
	}
    
	//--------------------------------------------------------------------------
	
	//==========================================================================
	private class CollectionAdapter extends BaseAdapter {
		//----------------------------------------------------------------------
		
		private Manga[] manga;
		
		//----------------------------------------------------------------------
		
		public void query() {
			Cursor c = Collection.getDB().query(
					"bookmarks", new String[]{ "manga" },
					"chapter=CAST( chapter AS REAL )", null,
					null, null, "timestamp DESC" );
			
			manga = new Manga[c.getCount()];
			while ( c.moveToNext() ) {
				manga[c.getPosition()] = new Manga( c.getString( 0 ));
			}
			notifyDataSetChanged();
		}
		
		//----------------------------------------------------------------------
		
		@Override
		public int getCount() {
			return manga == null ? 0 : manga.length;
		}
		
		//----------------------------------------------------------------------
		
		@Override
		public Manga getItem( int position ) {
			return manga[position];
		}
		
		//----------------------------------------------------------------------
		
		@Override
		public long getItemId( int position ) {
			return position;
		}
		
		//----------------------------------------------------------------------
		
		@Override
		public View getView( final int position, View view, ViewGroup parent ) {
			if ( view == null ) view = getActivity().getLayoutInflater().inflate( R.layout.griditem_collection, parent, false );
			
			// Set cover and queue loading
			final ImageView cover_view = (ImageView)view.findViewById( R.id.cover );
			final Cover cover = manga[position].getCover();
			cover_view.setTag( cover );
			cover_view.setImageDrawable( null );
			
			
			// Queue and store image request for cover
			if ( cover_requests.containsKey( view )) cover_requests.get( view ).cancelRequest();
			
			ImageContainer request = App.getImageLoader().get(
				API.getCoverUrl( manga[position].getCover() ),
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
	
					@Override
					public void onErrorResponse( VolleyError volleyError ) {
						if ( !cover.equals( cover_view.getTag() )) return;
						cover_view.setImageDrawable( null );
					}
				} );
			cover_requests.put( view, request );
			
			
			Resources res = getResources();
			ImageView bookmark_view = (ImageView)view.findViewById( R.id.bookmark );
			
			int fill_color, stroke_color, text_color;
			if ( manga[position].getBookmark().isCurrent() ) {
				// Gray
				fill_color   = res.getColor( R.color.bookmark_fill   );
				stroke_color = res.getColor( R.color.bookmark_stroke );
				text_color   = res.getColor( R.color.bookmark_text   );
				
			} else {
				// Pink
				fill_color   = res.getColor( R.color.bookmark_fill_current   );
				stroke_color = res.getColor( R.color.bookmark_stroke_current );
				text_color   = res.getColor( R.color.bookmark_text_current   );
			}
			
			BookmarkDrawable drawable = new BookmarkDrawable();
			drawable.setFillColor(   fill_color   );
			drawable.setStrokeColor( stroke_color );
			drawable.setTextColor(   text_color   );
			drawable.setChapter( manga[position].getBookmark().getChapter() );
			bookmark_view.setImageDrawable( drawable );
			
			( (TextView)view.findViewById( R.id.title )).setText( manga[position].getTitle() );
			
			return view;
		}

		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------