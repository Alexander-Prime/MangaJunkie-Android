package net.mangajunkie.android.activity;

import android.R.id;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;
import android.util.Log;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import net.mangajunkie.R;
import net.mangajunkie.android.app.App;
import net.mangajunkie.android.app.PreferenceWrapper;
import net.mangajunkie.content.collection.Bookmark;
import net.mangajunkie.content.collection.Collection;

//==============================================================================
public class PrefsActivity
extends      BaseFragmentActivity {
	//--------------------------------------------------------------------------

	@Override
	public void onCreate( Bundle bundle ) {
		super.onCreate( bundle );
		getFragmentManager().beginTransaction().replace( id.content, new PrefsFragment() ).commit();
	}

	//--------------------------------------------------------------------------
    
    //==========================================================================
    public static class PrefsFragment
    extends             PreferenceFragment
    implements          OnSharedPreferenceChangeListener {
	    //----------------------------------------------------------------------

	    private ListPreference pref_user;
	    private RingtonePreference pref_notificationSound;
	    private ListPreference pref_showPageNumbers;

	    //----------------------------------------------------------------------

	    @Override
	    public void onCreate( Bundle state ) {
		    super.onCreate( state );
		    setHasOptionsMenu( true );
		    addPreferencesFromResource( R.xml.prefs );

		    PreferenceWrapper prefs = App.getPrefs();

		    prefs.edit().setSyncConfirmed( true ).apply();
		    
		    // Collection sync username
		    pref_user = (ListPreference)findPreference( "accountSync_user" );
		    String[] account_names = getAccountNames();
		    pref_user.setEntries( account_names );
		    pref_user.setEntryValues( account_names );
		    pref_user.setSummary( prefs.getSyncUser().getUsername() );
		    
		    // New chapter notification sound
		    pref_notificationSound = (RingtonePreference)findPreference( "notificationSound" );
		    pref_notificationSound.setSummary( prefs.getNotificationSoundName() );
		    
		    // Page number visibility
		    pref_showPageNumbers = (ListPreference)findPreference( "pageNumberVisibility" );
		    pref_showPageNumbers.setSummary( pref_showPageNumbers.getEntry() );
		    
		    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
	    }

	    //----------------------------------------------------------------------

	    @Override public void onDestroy() {
		    super.onDestroy();
		    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
	    }

	    //----------------------------------------------------------------------

	    @Override
	    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater ) {
		    inflater.inflate( R.menu.fragment_prefs, menu );
	    }
	    
	    //----------------------------------------------------------------------
	    
	    @Override
	    public boolean onOptionsItemSelected( MenuItem item ) {
		    if ( item.getItemId() == R.id.eraseCollection ) {
			    showEraseCollectionDialog();
			    return true;
		    }
		    return false;
	    }
	    
	    //----------------------------------------------------------------------
	    
	    private String[] getAccountNames() {
		    Account[] accts = AccountManager.get( getActivity() ).getAccountsByType( "com.google" );
		    String[] names = new String[accts.length];
		    for ( int i = 0; i < accts.length; i++ ) names[i] = accts[i].name;
		    return names;
	    }

	    //----------------------------------------------------------------------
	    
	    @Override
	    public void onSharedPreferenceChanged( SharedPreferences prefs, String key ) {
		    Log.d( "MJ", "onSharedPreferenceChanged() / " + key );
		    switch ( key ) {
			    case "accountSync_user":
				    pref_user.setSummary( prefs.getString( key, "" ));
				    break;
			    
			    case "notificationSound":
				    pref_notificationSound.setSummary( App.getPrefs().getNotificationSoundName() );
				    break;
			    
			    case "pageNumberVisibility":
				    pref_showPageNumbers.setSummary( pref_showPageNumbers.getEntry() );
				    break;
		    }
	    }
	    
    	//----------------------------------------------------------------------
	
	    private void showEraseCollectionDialog() {
		    AlertDialog.Builder builder = new Builder( getActivity() );
		    
		    builder
		        .setTitle( R.string.dialog_title_eraseCollection )
		        .setMessage( R.string.dialog_message_eraseCollection )
		        .setNegativeButton( R.string.dialog_button_negative, null )
		        
		        .setPositiveButton( R.string.dialog_button_positive_eraseCollection, new OnClickListener() {
			        @Override public void onClick( DialogInterface d, int i ) {
				        for ( Bookmark b : Collection.getBookmarks() ) {
					        b.edit().delete().sync( App.getRequestQueue() ).apply();
				        }
				        Intent intent = new Intent( getActivity(), HomeActivity.class );
				        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
				        startActivity( intent );
			        }
		        } )
		        
		        .show();
	   	}
	   	
	   	//----------------------------------------------------------------------
    	
		//======================================================================
		private class AccountAdapter extends BaseAdapter {
			//------------------------------------------------------------------
			
			public final AccountManager manager;
			
			//------------------------------------------------------------------
			
			public AccountAdapter() {
				manager = AccountManager.get( getActivity() );
			}
			
			//------------------------------------------------------------------

			@Override
			public int getCount() {
				return manager.getAccountsByType( "com.google" ).length;
			}
			
	    	//------------------------------------------------------------------

			@Override
			public String getItem( int index ) {
				return manager.getAccountsByType( "com.google" )[index].name;
			}
			
	    	//------------------------------------------------------------------

			@Override
			public long getItemId( int index ) {
				return index;
			}
			
	    	//------------------------------------------------------------------

			@Override
			public View getView( int index, View view, ViewGroup parent ) {
				if ( view == null ) {
					view = getActivity().getLayoutInflater()
						.inflate( R.layout.listitem_accountsync_selector, parent, false );
				}
				
				view.setTag( getItem( index ));
				
				( (TextView)view.findViewById( android.R.id.title )).setText( getItem( index ));
				
				( (RadioButton)view.findViewById( R.id.radio )).setChecked(
					App.getPrefs().getSyncUser().getUsername().equals( getItem( index )));
				
				return view;
			}
			
	    	//------------------------------------------------------------------
		}
    	//----------------------------------------------------------------------
    }
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------