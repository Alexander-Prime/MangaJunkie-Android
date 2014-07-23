package net.mangajunkie.android.activity;

import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.mangajunkie.R;
import net.mangajunkie.android.app.App;
import net.mangajunkie.android.app.PreferenceWrapper;
import net.mangajunkie.android.app.PreferenceWrapper.PreferenceChangedListener;
import net.mangajunkie.android.fragment.BrightnessFragment;
import net.mangajunkie.android.service.PrefetchService;
import net.mangajunkie.android.widget.ChapterContainer;
import net.mangajunkie.android.widget.ChapterContainer.ChapterChanger;
import net.mangajunkie.android.widget.ChapterContainer.OnPageChangedListener;
import net.mangajunkie.android.widget.ChapterContainer.UiToggler;
import net.mangajunkie.android.widget.SinglePagerContainer;
import net.mangajunkie.android.widget.VerticalScrollContainer;
import net.mangajunkie.content.edit.BookmarkEditor;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Manga;
import net.mangajunkie.content.library.Page;
import net.mangajunkie.storage.PageLoader;

import org.json.JSONException;
import org.json.JSONObject;

//==============================================================================
public class ChapterActivity
extends      BaseFragmentActivity
implements   PreferenceChangedListener,
             OnPageChangedListener,
             UiToggler,
             ChapterChanger {
	//--------------------------------------------------------------------------

	private BookmarkEditor bookmark_edit;

	private Chapter chapter;

	private int start_page_number;

	private TextView page_number_view;

	private boolean system_ui_visible = true; // Init to true so first run of setSystemUiVisible( false ) works

	private ChapterContainer container;

	private Handler  handler;
	private Runnable hide_page_numbers_runnable;

	private PageLoader loader;

	//--------------------------------------------------------------------------
	// from Activity
	//--------------------------------------------------------------------------

	@Override public void onCreate( Bundle state ) {
		super.onCreate( state );
		getWindow().addFlags( LayoutParams.FLAG_KEEP_SCREEN_ON );

		Intent n = getIntent();
		Bundle params = n.getExtras();
		handler = new Handler();

		// Setup members to be shared across containers
		chapter = new Chapter( new Manga( params.getString( "manga" )), params.getString( "chapter" ));
		bookmark_edit = chapter.getManga().getBookmark().edit();

		if ( state != null ) start_page_number = state.getInt( "page", 1 );
		else start_page_number = getIntent().getIntExtra( "page", 1 );

		if ( chapter.getPageCount() < 1 ) {
			App.getRequestQueue().add( chapter.getRequest(
				new Listener<JSONObject>() {
					@Override
					public void onResponse( JSONObject json ) {
						try {
							chapter = chapter.edit( json ).apply();
						} catch ( JSONException e ) {
							e.printStackTrace();
						}
				
						setupChapter();
					}
				},
				new ErrorListener() {
					@Override
					public void onErrorResponse( VolleyError error ) {
						Toast.makeText(
							ChapterActivity.this,
							"Couldn't load chapter info. Check your Internet connection.",
							Toast.LENGTH_LONG ).show();
					}
				}
			));

		} else setupChapter();

		// Apply and listen for user's screen brightness setting
		setScreenBrightness( App.getPrefs().getEffectiveReadingBrightness() );
		App.getPrefs().addListener( this );
		
		// This won't be used here, but is passed down to the ChapterContainer
		loader = new PageLoader( this );
	}

	//--------------------------------------------------------------------------

	@Override protected void onSaveInstanceState( Bundle state ) {
		if ( container == null ) return;
		state.putInt( "page", container.getCurrentPage().toInt() );
	}

	//--------------------------------------------------------------------------
	
	@Override public void onPause() {
		super.onPause();
		
		// Upload bookmark to user sync server
		// Calling this in onStop() or onDestroy() makes it wait until after
		// MangaActivity.onResume(). That's bad. Do it now instead.
		bookmark_edit.updateTimestamp().sync( App.getRequestQueue() ).apply();
		startService( new Intent( PrefetchService.ACTION_NOTIFY_BOOKMARKS_CHANGED ));
	}
	
	//--------------------------------------------------------------------------
	
	@Override public void onDestroy() {
		super.onDestroy();
		if ( container != null ) container.cancelAll();
	}

	//--------------------------------------------------------------------------
	
	@Override public boolean onCreateOptionsMenu( Menu menu ) {
		getMenuInflater().inflate( R.menu.activity_chapter, menu );
		return super.onCreateOptionsMenu( menu );
	}
	
	//--------------------------------------------------------------------------
	
	@Override public boolean onOptionsItemSelected( MenuItem item ) {
		switch ( item.getItemId() ) {
			
			case android.R.id.home:
				startActivity( chapter.getManga().getIntent( this ));
				return true;
			
			case R.id.brightness:
				new BrightnessFragment().show( getFragmentManager(), "brightness" );
				return true;
			
			case R.id.settings:
				startActivity( new Intent( this, PrefsActivity.class ));
				return true;
		}
		return false;
	}
	
	//--------------------------------------------------------------------------

	@Override public void onPreferenceChanged() {
		setScreenBrightness( App.getPrefs().getEffectiveReadingBrightness() );
	}
	
	//--------------------------------------------------------------------------
	// From OnPageChangedListener
	//--------------------------------------------------------------------------
	
	@Override public void onPageChanged( Page page ) {
		bookmark_edit.setPage( page );
		updatePageNumber();
		
		if ( App.getPrefs().getPageNumberVisibility() == PreferenceWrapper.PAGE_NUMBER_SHOWN ) {
			showPageNumber();
			delayHidePageNumber();
		}
	}
	
	//--------------------------------------------------------------------------
	// From UiToggler
	//--------------------------------------------------------------------------
	
	@Override public void toggleUi() { setSystemUiVisible( !system_ui_visible ); }
	@Override public void showUi() { setSystemUiVisible( true ); }
	@Override public void hideUi() { setSystemUiVisible( false ); }
	
	//--------------------------------------------------------------------------
	// From ChapterChanger
	//--------------------------------------------------------------------------
	
	@Override public void openAdjacentChapter( int direction ) {
		Chapter adj_chapter = chapter.getRelativeChapter( direction );
		
		if ( adj_chapter == null ) startActivity( chapter.getManga().getIntent( this ));
		else startActivity( adj_chapter.getIntent( this ).putExtra( "page", direction ));
	}

	//--------------------------------------------------------------------------

	private void setupChapter() {
		setContentView( R.layout.activity_chapter );

		page_number_view = (TextView)findViewById( R.id.pageNumber );
		Log.d( "MJ", "page_number_view == " + page_number_view );
		
		View parent = findViewById( android.R.id.content );
		
		if ( findViewById( R.id.chapter_landscape ) != null ) {
			container = new VerticalScrollContainer( chapter, this, parent, start_page_number );

		} else {
			container = new SinglePagerContainer( chapter, this, getFragmentManager(), parent, start_page_number );
		}
		
		container.setOnPageChangedListener( this );
		container.setUiToggler( this );
		container.setChapterChanger( this );
		
		hideUi();
		onPageChanged( chapter.getPage( start_page_number ));
	}
	
	//--------------------------------------------------------------------------
	
	private void updatePageNumber() {
		page_number_view.setText( container.getCurrentPage() + "/" + chapter.getPageCount() );
	}
	
	//--------------------------------------------------------------------------
	
	private void delayHidePageNumber() {
		handler.removeCallbacks( hide_page_numbers_runnable );
		hide_page_numbers_runnable = new Runnable() {
			@Override public void run() {
				hidePageNumber();
				hide_page_numbers_runnable = null;
			}
		};
		handler.postDelayed( hide_page_numbers_runnable, 3000 );
	}
	
	//--------------------------------------------------------------------------
	
	private void showPageNumber() {
		handler.removeCallbacks( hide_page_numbers_runnable );
		page_number_view.animate().alpha( 1 ).translationY( 0 );
	}
	
	//--------------------------------------------------------------------------
	
	private void hidePageNumber() {
		page_number_view.animate().alpha( 0 ).translationY( page_number_view.getHeight() );
	}
	
	//--------------------------------------------------------------------------
	
	private void setSystemUiVisible( boolean visible ) {
		if ( visible == system_ui_visible ) return;
		
		View view = findViewById( android.R.id.content );
		
		if ( VERSION.SDK_INT >= 16 ) {
			if ( visible ) {
				getActionBar().show();
				view.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
				View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				);

			} else {
				getActionBar().hide();
				view.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
				View.SYSTEM_UI_FLAG_FULLSCREEN |
				View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
				View.SYSTEM_UI_FLAG_LOW_PROFILE
				);
			}
			
		} else {
			if ( visible ) {
				getActionBar().show();
				view.setSystemUiVisibility( 0 );

			} else {
				getActionBar().hide();
				view.setSystemUiVisibility( View.SYSTEM_UI_FLAG_LOW_PROFILE );
			}
		}
		
		if ( App.getPrefs().getPageNumberVisibility() != PreferenceWrapper.PAGE_NUMBER_HIDDEN ) {
			if ( visible ) showPageNumber();
			else hidePageNumber();
		}
		
		system_ui_visible = visible;
	}
	
	//--------------------------------------------------------------------------
	
	private boolean isSystemUiVisible() { return system_ui_visible; }
	
	//--------------------------------------------------------------------------
	
	private void setScreenBrightness( float brightness ) {
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.screenBrightness = brightness;
		getWindow().setAttributes( params );
	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------