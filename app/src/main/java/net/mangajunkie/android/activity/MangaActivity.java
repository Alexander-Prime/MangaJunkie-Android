package net.mangajunkie.android.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import org.json.JSONException;
import org.json.JSONObject;
import net.mangajunkie.R;
import net.mangajunkie.android.app.App;
import net.mangajunkie.android.service.PrefetchService;
import net.mangajunkie.content.collection.Bookmark;
import net.mangajunkie.content.collection.Pin;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Library;
import net.mangajunkie.content.library.Manga;
import net.mangajunkie.content.library.Tag;
import net.mangajunkie.graphics.BookmarkDrawable;
import net.mangajunkie.network.API;
import net.mangajunkie.android.service.PinService;

//==============================================================================
public class MangaActivity
extends      BaseFragmentActivity
implements   OnItemClickListener,
			 OnClickListener,
			 OnMenuItemClickListener {
	//--------------------------------------------------------------------------
	
	private TextView title_view, author_view, tags_view, summary_view, bookmark_title_view, bookmark_page_view;
	
	private ListView       list_view;
	//private ProgressButton pin_view;
	private ImageView      cover_view, summary_expander_view, bookmark_chapter_view, bookmark_menu_view;
	
	private View summary_wrapper_view, bookmark_wrapper_view;

	private Manga           manga;
	private ChaptersAdapter adapter;

	//private BroadcastReceiver pin_progress_receiver;
	//private final IntentFilter pin_progress_filter = new IntentFilter( PinService.ACTION_NOTIFY_PIN_PROGRESS );

	//private User user;

	private boolean fetching_info, fetching_chapters, fetching_cover, summary_expanded;

	//--------------------------------------------------------------------------

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
		super.onCreate( savedInstanceState );

		//user = App.getPrefs().getSyncUser();


		getActionBar().setHomeButtonEnabled( true );

		setContentView( R.layout.activity_manga );

		// Assign all persistent view references
		list_view = (ListView)findViewById( R.id.list );
		list_view.addHeaderView( getLayoutInflater().inflate( R.layout.listheader_mangainfo, null ), null, false );

		title_view = (TextView)findViewById( R.id.title );
		author_view = (TextView)findViewById( R.id.author );
		tags_view = (TextView)findViewById( R.id.tags );
		summary_view = (TextView)findViewById( R.id.summary );
		cover_view = (ImageView)findViewById( R.id.cover );
		//pin_view = (ProgressButton)findViewById( R.id.progress );

		summary_wrapper_view = findViewById( R.id.summary_wrapper );
		summary_expander_view = (ImageView)findViewById( R.id.summary_moreless );

		bookmark_wrapper_view = findViewById( R.id.bookmark_wrapper );
		bookmark_chapter_view = (ImageView)findViewById( R.id.bookmark_chapter );
		bookmark_title_view = (TextView)findViewById( R.id.bookmark_title );
		bookmark_page_view = (TextView)findViewById( R.id.bookmark_page );
		bookmark_menu_view = (ImageView)findViewById( R.id.bookmark_menu );

		manga = new Manga( getIntent().getStringExtra( "manga" ));
		setFields( manga );

		adapter = new ChaptersAdapter();
		adapter.setDescending( App.getPrefs().getSortOrderDescending() );
		list_view.setOnItemClickListener( this );
		list_view.setAdapter( adapter );

		summary_wrapper_view.setOnClickListener( this );
		bookmark_wrapper_view.setOnClickListener( this );
		bookmark_menu_view.setOnClickListener( this );

		// TODO Actually request the manga download on click
		/*pin_view.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick( View view ) {
				Intent intent = new Intent( DownloadManagerService.ACTION_REQUEST_DOWNLOAD );
				intent.putExtra( "manga", manga.getSysName() );
				startService( intent );
			}
		});
		pin_view.setMax( 10000 );*/

		setProgressBarIndeterminateVisibility( true );
		/*pin_progress_receiver = new BroadcastReceiver() {
			@Override
			public void onReceive( Context context, Intent intent ) {
				if ( !intent.getStringExtra( "manga" ).equals( manga.getSysName() ) )
					return;
				pin_view.setProgress( (int)( 10000 * intent.getFloatExtra( "manga_progress", 0 )));
			}
		};*/
		
		fetchInfo();
		fetchChapters();
		fetchCover();
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public void onStart() {
		super.onStart();
		//registerReceiver( pin_progress_receiver, pin_progress_filter );
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public void onResume() {
		super.onResume();
		adapter.query();
		
		updateBookmarkLink();
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public void onStop() {
		super.onStop();
		//unregisterReceiver( pin_progress_receiver );
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// Cancel queued requests
		App.getRequestQueue().cancelAll( this );
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		getMenuInflater().inflate( R.menu.activity_manga, menu );
		boolean desc = App.getPrefs().getSortOrderDescending();
		menu.findItem( R.id.sort_descending ).setVisible( !desc );
		menu.findItem( R.id.sort_ascending  ).setVisible(  desc );
		//menu.findItem( R.id.remove_bookmark ).setVisible( manga.getBookmark( user ).exists() );
		return super.onCreateOptionsMenu( menu );
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		switch ( item.getItemId() ) {
			
			case android.R.id.home:
				Intent n = new Intent( this, HomeActivity.class )
				.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
				startActivity( n );
				return true;
			
			case R.id.sort_descending:
				App.getPrefs().edit().setSortOrderDescending( true ).apply();
				adapter.setDescending( true );
				invalidateOptionsMenu();
				return true;
			
			case R.id.sort_ascending:
				App.getPrefs().edit().setSortOrderDescending( false ).apply();
				adapter.setDescending( false );
				invalidateOptionsMenu();
				return true;
			
			case R.id.settings:
				startActivity( new Intent( this, PrefsActivity.class ));
				return true;
			
			default:
				return super.onOptionsItemSelected( item );
		}
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public void onItemClick( AdapterView<?> parent, View v, int index, long id ) {
		index -= 1;
		Chapter chapter = adapter.getItem( index );
		startActivity( chapter.getIntent( this ));
	}
    
    //--------------------------------------------------------------------------
    
	@Override
	public void onClick( View view ) {
		switch ( view.getId() ) {
			
			case R.id.summary_wrapper:
				if ( summary_expanded ) {
					summary_view.setMaxLines( 3 );
					summary_expander_view.setImageResource( R.drawable.ic_more );
					summary_expanded = false;
					
				} else {
					summary_view.setMaxLines( Integer.MAX_VALUE );
					summary_expander_view.setImageResource( R.drawable.ic_less );
					summary_expanded = true;
				}
				break;
			
			case R.id.bookmark_wrapper:
				Bookmark bm = new Bookmark( manga );
				if ( bm.exists() ) startActivity( bm.getPage().getIntent( this ));
				break;
			
			case R.id.bookmark_menu:
				PopupMenu menu = new PopupMenu( this, view );
				menu.inflate( R.menu.context_infoitem_bookmark );
				menu.show();
				menu.setOnMenuItemClickListener( this );
		}
	}

	//--------------------------------------------------------------------------

	@Override public boolean onMenuItemClick( MenuItem item ) {
		switch( item.getItemId() ) {
			
			case R.id.remove_bookmark:
				manga.getBookmark().edit().delete().sync( App.getRequestQueue() ).apply();
				startService( new Intent( PrefetchService.ACTION_NOTIFY_BOOKMARKS_CHANGED ));
				updateBookmarkLink();
				return true;
			
			default: return false;
		}
	}
	
    //--------------------------------------------------------------------------
    
    private void setFields( Manga manga ) {
    	title_view  .setText(         manga.getTitle()            );
    	author_view .setText( "by " + manga.getAuthor().getName() );
    	summary_view.setText(         manga.getSummary()          );
	    tags_view   .setText( Tag.makeString( manga.getTags()    ));
    	
    	if ( manga.getSummary() != null
    	&& manga.getSummary().matches( "^\\s*$" )) summary_view.setVisibility( View.GONE );
    	else summary_view.setVisibility( View.VISIBLE );
    }
    
    //--------------------------------------------------------------------------
    
	private void fetchInfo() {
		fetching_info = true;
		updateProgress();
		App.getRequestQueue().add( manga.getRequest(
			new Listener<JSONObject>() {
				@Override
				public void onResponse( JSONObject json ) {
					try {
						manga = manga.edit( json ).apply();
					} catch ( JSONException e ) { e.printStackTrace(); }
					setFields( manga );
					fetching_info = false;
					updateProgress();
				}
			},
			new ErrorListener() {
				@Override
				public void onErrorResponse( VolleyError error ) {
					fetching_info = false;
					updateProgress();
				}
			},
			this
		));
	}
	
    //--------------------------------------------------------------------------
	
	private void fetchChapters() {
		fetching_chapters = true;
		updateProgress();
		App.getRequestQueue().add( manga.getChaptersRequest(
			new Listener<JSONObject>() {
				@Override
				public void onResponse( JSONObject json ) {
					try {
						manga = manga.edit( json ).apply();
					} catch ( JSONException e ) { e.printStackTrace(); }
					adapter.query();
					updateBookmarkLink();
					fetching_chapters = false;
					updateProgress();
				}
			},
			new ErrorListener() {
				@Override
				public void onErrorResponse( VolleyError error ) {
					fetching_chapters = false;
					updateProgress();
				}
			}
		));
	}
	
    //--------------------------------------------------------------------------
	
	private void fetchCover() {
		fetching_cover = true;
		updateProgress();
		
		App.getImageLoader().get( API.getCoverUrl( manga.getCover() ),
			new ImageListener() {
				@Override
				public void onResponse( ImageContainer container, boolean something ) {
					TransitionDrawable cover_transition = new TransitionDrawable( new Drawable[]{
					cover_view.getDrawable(),
					new BitmapDrawable( getResources(), container.getBitmap() ) } );
					cover_view.setImageDrawable( cover_transition );
					cover_transition.startTransition( 200 );
					fetching_cover = false;
					updateProgress();
				}
	
				@Override public void onErrorResponse( VolleyError error ) {
					Log.e( "MJ", "Failed to fetch cover for " + manga.getSysName() );
					fetching_cover = false;
					updateProgress();
				}
			}
		);
	}
	
	//--------------------------------------------------------------------------
	
	private void updateProgress() {
		setProgressBarIndeterminateVisibility( fetching_info | fetching_chapters | fetching_cover );
	}
	
	//--------------------------------------------------------------------------
	
	private void updateBookmarkLink() {
		Bookmark bm = new Bookmark( manga );
		if ( !bm.exists() ) {
			bookmark_wrapper_view.setVisibility( View.GONE );
			return;	
		}
		
		bookmark_wrapper_view.setVisibility( View.VISIBLE );
		
		BookmarkDrawable drawable = new BookmarkDrawable();
		Resources res = getResources();
		
		if ( bm.isCurrent() ) {
			// Gray
			drawable.setFillColor(   res.getColor( R.color.bookmark_fill   ));
			drawable.setStrokeColor( res.getColor( R.color.bookmark_stroke ));
			drawable.setTextColor( res.getColor( R.color.bookmark_text ) );
			
		} else {
			// Pink
			drawable.setFillColor(   res.getColor( R.color.bookmark_fill_current   ));
			drawable.setStrokeColor( res.getColor( R.color.bookmark_stroke_current ));
			drawable.setTextColor(   res.getColor( R.color.bookmark_text_current   ));
		}
		drawable.setChapter( bm.getChapter() );
		bookmark_chapter_view.setImageDrawable( drawable );
		bookmark_title_view.setText( bm.getChapter().getTitle() );
		bookmark_page_view.setText( "page " + bm.getPage() );
	}
	
	//--------------------------------------------------------------------------
    
	//==========================================================================
	public class ChaptersAdapter extends BaseAdapter {
		//----------------------------------------------------------------------
		
		private Chapter[] chapters;
		private boolean descending = true;
		
		//----------------------------------------------------------------------
		
		public ChaptersAdapter() {
			query();
		}
		
		//----------------------------------------------------------------------
		
		public void setDescending( boolean desc ) {
			descending = desc;
			query();
		}
		
		//----------------------------------------------------------------------
		
		public void query() {
			final Manga manga = MangaActivity.this.manga;
			if ( manga == null ) return;
			new AsyncTask<Void,Void,Chapter[]>() {
				public Chapter[] doInBackground( Void... params ) {
					Cursor cursor = Library.getDB().query(
						"chapters", new String[]{ "chapter" },
						"manga=?", new String[]{ manga.getSysName() },
						null, null, "CAST( chapter AS REAL ) " + ( descending ? "DESC" : "ASC" ));
					Chapter[] result = new Chapter[cursor.getCount()];
					while ( cursor.moveToNext() ) {
						result[cursor.getPosition()] = new Chapter( manga, cursor.getString( 0 ));
					}
					return result;
				}
				
				public void onPostExecute( Chapter[] result ) {
					chapters = result;
					notifyDataSetChanged();
				}
			}.executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR );
		}
		
		//----------------------------------------------------------------------

		@Override
		public int getCount() {
			return chapters == null ? 0 : chapters.length;
		}
		
		//----------------------------------------------------------------------

		@Override
		public Chapter getItem( int index ) {
			return chapters[index];
		}
		
		//----------------------------------------------------------------------

		@Override
		public long getItemId( int index ) {
			return index;
		}
		
		//----------------------------------------------------------------------

		@Override
		public View getView( int index, View view, ViewGroup parent ) {
			if ( view == null ) {
				view = getLayoutInflater().inflate( R.layout.listitem_chapter, parent, false );
			}
			
			String chapter = chapters[index].toString(),
					 title = chapters[index].getTitle();
			
			TextView  chapter_view = (TextView )view.findViewById( R.id.chapter ),
					  title_view   = (TextView )view.findViewById( R.id.title   );
			ImageView menu_button  = (ImageView)view.findViewById( R.id.menu    );
			
			chapter_view.setText( chapter );
			title_view  .setText( title   );
			menu_button .setOnClickListener( new ChapterMenuSelectListener( chapters[index] ));
			
			if ( new Pin( chapters[index] ).exists() ) view.findViewById( R.id.pin ).setVisibility( View.VISIBLE );
			else view.findViewById( R.id.pin ).setVisibility( View.GONE );
			
			return view;
		}
		
		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
	
	//==========================================================================
	private class ChapterMenuSelectListener
	implements    OnClickListener,
				  OnMenuItemClickListener {
		//----------------------------------------------------------------------
		
		private final Chapter CHAPTER;
		
		//----------------------------------------------------------------------
		
		public ChapterMenuSelectListener( Chapter chapter ) { CHAPTER = chapter; }
		
		//----------------------------------------------------------------------

		@Override
		public void onClick( View view ) {
			PopupMenu menu = new PopupMenu( MangaActivity.this, view );
			menu.inflate( R.menu.context_listitem_manga );
			Pin pin = new Pin( CHAPTER );
			menu.getMenu().findItem( R.id.pin ).setChecked( pin.exists() );
			menu.show();
			menu.setOnMenuItemClickListener( this );
		}

		//----------------------------------------------------------------------

		@Override public boolean onMenuItemClick( MenuItem item ) {
			switch( item.getItemId() ) {
				
				/*case R.id.set_bookmark:
					manga.getBookmark( user ).edit()
					     .setPage( CHAPTER.getPage( 1 ))
					     .sync( App.getRequestQueue() )
					     .apply();
					adapter.notifyDataSetChanged();
					return true;*/
				
				case R.id.pin:
					if ( !item.isChecked() ) new Pin( CHAPTER ).edit().add()   .apply();
					else                     new Pin( CHAPTER ).edit().remove().apply();
					adapter.notifyDataSetChanged();
					startService(
						new Intent( MangaActivity.this, PinService.class )
						.setAction( PinService.ACTION_NOTIFY_PIN_CHANGED ));
					return true;
				
				default: return false;
			}
		}

		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------