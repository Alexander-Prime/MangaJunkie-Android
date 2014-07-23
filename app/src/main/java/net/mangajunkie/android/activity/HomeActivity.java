package net.mangajunkie.android.activity;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import net.mangajunkie.R;
import net.mangajunkie.R.id;
import net.mangajunkie.android.app.App;
import net.mangajunkie.android.app.DrawerAdapter;
import net.mangajunkie.android.fragment.CollectionFragment;
import net.mangajunkie.android.fragment.SearchFragment;
import net.mangajunkie.content.library.Library;
import net.mangajunkie.network.API;

import org.json.JSONObject;

//==============================================================================
public class   HomeActivity
	extends    BaseFragmentActivity
	implements OnItemClickListener,
			   OnQueryTextListener,
			   OnActionExpandListener {
	//--------------------------------------------------------------------------

	private DrawerLayout drawer;
	private Fragment collection_fragment;
	private SearchFragment search_fragment;
	
	private ListView      drawer_view;
	private DrawerAdapter drawer_adapter;
	private CharSequence  app_title, fragment_title;
	private ActionBarDrawerToggle drawer_toggle;

	private SearchView search_view;

	private boolean search_enabled = true;

	private Handler  handler;
	private Runnable submit_query;

	//--------------------------------------------------------------------------

	@Override
	public void onCreate( Bundle state ) {
		super.onCreate( state );
		
		handler = new Handler();
		
		getActionBar().setHomeButtonEnabled( true );

		setContentView( R.layout.activity_home );

		// Content fragments
		collection_fragment = new CollectionFragment();
		search_fragment = new SearchFragment();

		Bundle extras = getIntent().getExtras();
		collection_fragment.setArguments( extras );
		search_fragment.setArguments( extras );

		app_title = getResources().getString( R.string.app_name );

		// Drawer action handling
		drawer = (DrawerLayout)findViewById( R.id.drawer );
		drawer_toggle = new ActionBarDrawerToggle(
			this, drawer,
			R.drawable.ic_drawer,
			R.string.drawer_open,
			R.string.drawer_close ) {

			@Override public void onDrawerOpened( View view ) {
				getActionBar().setTitle( app_title );
				invalidateOptionsMenu();
			}

			@Override public void onDrawerClosed( View view ) {
				getActionBar().setTitle( fragment_title );
				invalidateOptionsMenu();
			}
		};
		drawer.setDrawerListener( drawer_toggle );
		
		// TODO DELETEME
		drawer.setDrawerLockMode( DrawerLayout.LOCK_MODE_LOCKED_CLOSED );
		
		//getActionBar().setDisplayHomeAsUpEnabled( true );
		//getActionBar().setHomeButtonEnabled( true );
		getActionBar().setDisplayHomeAsUpEnabled( false );
		getActionBar().setHomeButtonEnabled( false );
		// Drawer contents
		drawer_view = (ListView)findViewById( R.id.nav );
		drawer_adapter = new DrawerAdapter( this );
		drawer_view.setAdapter( drawer_adapter );
		drawer_view.setOnItemClickListener( this );

		Cursor c = Library.getDB().rawQuery( "SELECT sys_name FROM manga", null );

		if ( c.moveToFirst() ) {
			// Default fragment
			if ( state == null ) pickNavItem( 0 );

		} else {
			// Need to populate library
			getFragmentManager().beginTransaction()
			                    .replace( id.content, new PopulateLibraryFragment() )
			                    .commit();
		}
		c.close();
	}

	//--------------------------------------------------------------------------
	
	@Override
	public void onPostCreate( Bundle state ) {
		super.onPostCreate( state );
		drawer_toggle.syncState();
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public void onResume() {
		super.onResume();
		if ( App.getPrefs().getDrawerHintEnabled() ) {
			drawer.openDrawer( Gravity.LEFT );
			App.getPrefs().edit().setDrawerHintEnabled( false ).apply();
		}
	}
	
	//--------------------------------------------------------------------------
	
	private void pickNavItem( int target ) {
		Fragment new_frag;
		
		switch( (int)drawer_adapter.getItemId( target )) {
			case 0: new_frag = collection_fragment; break;
		
			default: drawer.closeDrawer( Gravity.LEFT ); return;
		}
		
		getFragmentManager().beginTransaction()
			.replace( R.id.content, new_frag )
			.commit();

		drawer_view.setItemChecked( target, true );
		setTitle( "Manga Junkie" );
		drawer.closeDrawer( Gravity.LEFT );
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public void setTitle( CharSequence title ) {
		fragment_title = title;
		getActionBar().setTitle( title );
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
	    getMenuInflater().inflate( R.menu.activity_home, menu );
		menu.findItem( id.search ).setOnActionExpandListener( this );
		search_view = (SearchView)menu.findItem( id.search ).getActionView();
		search_view.setOnQueryTextListener( this );
	    return super.onCreateOptionsMenu( menu );
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public boolean onPrepareOptionsMenu( Menu menu ) {
		menu.findItem( id.search ).setEnabled( search_enabled );
		return super.onPrepareOptionsMenu( menu );
	}
	
	//--------------------------------------------------------------------------

	@Override
	public void onConfigurationChanged( Configuration new_config ) {
		super.onConfigurationChanged( new_config );
		drawer_toggle.onConfigurationChanged( new_config );
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		
		if ( drawer_toggle.onOptionsItemSelected( item )) return true;
		
		switch ( item.getItemId() ) {
		
		case android.R.id.home:
			getFragmentManager().beginTransaction().replace( id.content, collection_fragment ).commit();
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
	public void onItemClick( AdapterView parent, View view, int index, long id ) {
		pickNavItem( index );
	}
	
	//--------------------------------------------------------------------------

	@Override
	public boolean onQueryTextSubmit( String s ) { return false; }
	
	//--------------------------------------------------------------------------

	@Override
	public boolean onQueryTextChange( final String s ) {
		search_fragment.invalidateQuery();
		
		if ( submit_query != null ) handler.removeCallbacks( submit_query );
		submit_query =  new Runnable() {
			@Override public void run() { search_fragment.query( s ); }
		};
		handler.postDelayed( submit_query, 500 );
		return true;
	}

	//--------------------------------------------------------------------------

	@Override public boolean onMenuItemActionExpand( MenuItem item ) {
		switch ( item.getItemId() ) {
			
			case id.search:
				getFragmentManager().beginTransaction().replace( id.content, search_fragment ).commit();
				return true;
			
			default: return false;
		}
	}

	//--------------------------------------------------------------------------

	@Override public boolean onMenuItemActionCollapse( MenuItem item ) {
		switch ( item.getItemId() ) {
			
			case id.search:
				getFragmentManager().beginTransaction().replace( id.content, collection_fragment ).commit();
				return true;
			
			default: return false;
		}
	}

	//--------------------------------------------------------------------------
	
	public void showCollection() {
		getFragmentManager().beginTransaction()
			.replace( id.content, collection_fragment )
			.commit();
	}

	//--------------------------------------------------------------------------
	
	public void setSearchEnabled( boolean enabled ) {
		search_enabled = enabled;
	}
	
	//--------------------------------------------------------------------------
	
	//==========================================================================
	public static class PopulateLibraryFragment
	extends             Fragment
	implements          OnClickListener {
		//----------------------------------------------------------------------
		
		@Override
		public View onCreateView( LayoutInflater inflater, ViewGroup parent, Bundle state ) {
			View view = inflater.inflate( R.layout.fragment_fetchinglibrary, parent, false );
			view.findViewById( id.retry ).setOnClickListener( this );
			return view;
		}
		
		//----------------------------------------------------------------------

		@Override
		public void onResume() {
			super.onResume();
			sendLibraryRequest();
			( (HomeActivity)getActivity() ).setSearchEnabled( false );
			getActivity().invalidateOptionsMenu();
			//drawer_toggle.setDrawerIndicatorEnabled( false );
			//drawer.setDrawerLockMode( DrawerLayout.LOCK_MODE_LOCKED_CLOSED );
			//getActionBar().setDisplayHomeAsUpEnabled( false );
			//getActionBar().setHomeButtonEnabled( false );
		}

		//----------------------------------------------------------------------
		
		@Override
		public void onPause() {
			super.onPause();
			App.getRequestQueue().cancelAll( this );
			( (HomeActivity)getActivity() ).setSearchEnabled( true );
			getActivity().invalidateOptionsMenu();
			//drawer_toggle.setDrawerIndicatorEnabled( true );
			//drawer.setDrawerLockMode( DrawerLayout.LOCK_MODE_UNLOCKED );
			//getActionBar().setHomeButtonEnabled( true );
			//getActionBar().setDisplayHomeAsUpEnabled( true );
		}
		
		//----------------------------------------------------------------------

		@Override
		public void onClick( View view ) {
			sendLibraryRequest();
		}
		
		//----------------------------------------------------------------------
		
		private void sendLibraryRequest() {
			getActivity().findViewById( id.progress ).setVisibility( View.VISIBLE );
			getActivity().findViewById( id.failed ).setVisibility( View.GONE    );
			JsonObjectRequest request = new JsonObjectRequest(
				API.getMangaDirectoryUrl(),
				null,
				new Listener<JSONObject>() {
					@Override
					public void onResponse( JSONObject json ) {
						new Library().edit( json ).apply();
						( (HomeActivity)getActivity() ).showCollection();
					}
				},
				new ErrorListener() {
					@Override public void onErrorResponse( VolleyError volleyError ) {
						getActivity().findViewById( id.progress ).setVisibility( View.GONE );
						getActivity().findViewById( id.failed ).setVisibility( View.VISIBLE );
					}
				}
			);
			request.setTag( this );
			App.getRequestQueue().add( request );
		}

		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------