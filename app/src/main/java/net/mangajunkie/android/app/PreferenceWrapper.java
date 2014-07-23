package net.mangajunkie.android.app;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import net.mangajunkie.content.collection.User;

import java.util.ArrayList;
import java.util.List;

//==============================================================================
public class PreferenceWrapper implements OnSharedPreferenceChangeListener {
	//--------------------------------------------------------------------------

	public static final int
	PAGE_NUMBER_HIDDEN               = 0,
	PAGE_NUMBER_HIDDEN_IN_FULLSCREEN = 1,
	PAGE_NUMBER_SHOWN                = 2;

	private final Context           CONTEXT;
	private final SharedPreferences PREFS;
	private final List<PreferenceChangedListener> LISTENERS = new ArrayList<>();

	//--------------------------------------------------------------------------

	public PreferenceWrapper( Context context ) {
		CONTEXT = context.getApplicationContext();
		PREFS = PreferenceManager.getDefaultSharedPreferences( context );

		// Set some default preferences

		AccountManager manager = AccountManager.get( context );
		if ( !PREFS.contains( "accountSync_user" ) && manager.getAccountsByType( "com.google" ).length > 0 ) {
			PREFS.edit()
			     .putString( "accountSync_user", manager.getAccountsByType( "com.google" )[0].name )
			     .apply();
		}
	}

	//--------------------------------------------------------------------------

	public void listen() {
		PREFS.registerOnSharedPreferenceChangeListener( this );
	}

	//--------------------------------------------------------------------------

	public void unlisten() {
		PREFS.unregisterOnSharedPreferenceChangeListener( this );
	}
	
	//--------------------------------------------------------------------------
	
	public void addListener( PreferenceChangedListener listener ) {
		LISTENERS.add( listener );
	}
	
	//--------------------------------------------------------------------------
	
	public void removeListener( PreferenceChangedListener listener ) {
		LISTENERS.remove( listener );
	}
	
	//--------------------------------------------------------------------------
	
	public PreferenceEditWrapper edit() { return new PreferenceEditWrapper(); }

	//--------------------------------------------------------------------------

	public boolean getSortOrderDescending() {
		return PREFS.getBoolean( "sort_descending", false );
	}
	
	//--------------------------------------------------------------------------
	
	public boolean getNotificationsEnabled() {
		return PREFS.getBoolean( "notifications", true );
	}
	
	//--------------------------------------------------------------------------

	public boolean getAutomaticUpdatesEnabled() {
		return PREFS.getBoolean( "automatic_updates", true );
	}

	//--------------------------------------------------------------------------
	
	public boolean getSyncEnabled() {
		return PREFS.getBoolean( "accountSync", true );
	}

	//--------------------------------------------------------------------------
	
	public boolean getSyncConfirmed() {
		return PREFS.getBoolean( "accountSync_confirmed", false );
	}

	//--------------------------------------------------------------------------

	public User getSyncUser() {
		return new User( PREFS.getString( "accountSync_user", "" ));
	}
	
	//--------------------------------------------------------------------------
	
	public Uri getNotificationSoundUri() {
		try { return Uri.parse( PREFS.getString( "notificationSound", null )); }
		catch ( NullPointerException e ) { return Settings.System.DEFAULT_NOTIFICATION_URI; }
	}
	
	//--------------------------------------------------------------------------
	
	public String getNotificationSoundName() {
		Uri uri = getNotificationSoundUri();
		Ringtone tone = RingtoneManager.getRingtone( CONTEXT, uri );
		return tone.getTitle( CONTEXT );
	}
	
	//--------------------------------------------------------------------------
	
	public int getPageNumberVisibility() {
		switch ( PREFS.getString( "pageNumberVisibility", "hidden" )) {
			
			default:
			case "hidden":
				return PAGE_NUMBER_HIDDEN;
			
			case "hiddenInFullscreen":
				return PAGE_NUMBER_HIDDEN_IN_FULLSCREEN;
			
			case "shown":
				return PAGE_NUMBER_SHOWN;
		}
	}
	
	//--------------------------------------------------------------------------
	
	public float getReadingBrightness() {
		return PREFS.getFloat( "readingBrightness", 0.2f );
	}
	
	//--------------------------------------------------------------------------
	
	public boolean getUseSystemBrightness() {
		return PREFS.getBoolean( "useSystemBrightness", true );
	}
	
	//--------------------------------------------------------------------------
	
	public float getEffectiveReadingBrightness() {
		return getUseSystemBrightness() ? -1 : getReadingBrightness();
	}
	
	//--------------------------------------------------------------------------
	
	public boolean getDrawerHintEnabled() {
		return PREFS.getBoolean( "drawerHint", false );
	}
	
	//--------------------------------------------------------------------------
	
	public int getPrefetchCount() {
		return PREFS.getInt( "prefetch_count", 3 );
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String s ) {
		for ( PreferenceChangedListener listener : LISTENERS ) {
			listener.onPreferenceChanged();
		}
	}

	//--------------------------------------------------------------------------
	
	//==========================================================================
	public class PreferenceEditWrapper {
		//----------------------------------------------------------------------
		
		private final Editor EDITOR = PREFS.edit();
		
		//----------------------------------------------------------------------
		
		public PreferenceEditWrapper setNotificationsEnabled( boolean enabled ) {
			EDITOR.putBoolean( "notifications", enabled );
			return this;
		}
		
		//----------------------------------------------------------------------
		
		public PreferenceEditWrapper setSortOrderDescending( boolean desc ) {
			EDITOR.putBoolean( "sort_descending", desc );
			return this;
		}
		
		//----------------------------------------------------------------------
		
		public PreferenceEditWrapper setSyncConfirmed( boolean confirmed ) {
			EDITOR.putBoolean( "accountSync_confirmed", confirmed );
			return this;
		}
		
		//----------------------------------------------------------------------
		
		public PreferenceEditWrapper setSyncEnabled( boolean enabled ) {
			EDITOR.putBoolean( "accountSync", enabled );
			return this;
		}
		
		//----------------------------------------------------------------------
		
		public PreferenceEditWrapper setSyncUser( User user ) {
			EDITOR.putString( "accountSync_user", user.getUsername() );
			return this;
		}
		
		//----------------------------------------------------------------------
		
		public PreferenceEditWrapper setDrawerHintEnabled( boolean enabled ) {
			EDITOR.putBoolean( "drawerHint", enabled );
			return this;
		}
		
		//----------------------------------------------------------------------
		
		public PreferenceEditWrapper setReadingBrightness( float brightness ) {
			EDITOR.putFloat( "readingBrightness", brightness );
			return this;
		}
		
		//----------------------------------------------------------------------

		public PreferenceEditWrapper setUseSystemBrightness( boolean use ) {
			EDITOR.putBoolean( "useSystemBrightness", use );
			return this;
		}
		
		//----------------------------------------------------------------------
		
		public void apply() {
			EDITOR.apply();
		}
		
		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
	
	//==========================================================================
	public interface PreferenceChangedListener {
		// Does NOT pass the name of the preference, just ask for the one you want
		//----------------------------------------------------------------------
		
		public void onPreferenceChanged();
		
		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
