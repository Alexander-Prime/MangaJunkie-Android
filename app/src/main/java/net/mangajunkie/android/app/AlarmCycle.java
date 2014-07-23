package net.mangajunkie.android.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import net.mangajunkie.util.T;

//==============================================================================
public class AlarmCycle {
	//--------------------------------------------------------------------------

	private final Context CONTEXT;
	private final String  NAME;
	
	private String action;
	private Class<? extends BroadcastReceiver> receiver_class;
	private long interval = 24 * T.HOURS;
	private boolean wakeup;

	//--------------------------------------------------------------------------

	public AlarmCycle( Context context, String name ) {
		CONTEXT = context.getApplicationContext();
		NAME    = name;
	}

	//--------------------------------------------------------------------------
	
	public AlarmCycle setAction( String action ) {
		this.action = action;
		return this;
	}
	
	//--------------------------------------------------------------------------
	
	public AlarmCycle setReceiver( Class<? extends BroadcastReceiver> cls ) {
		receiver_class = cls;
		return this;
	}
	
	//--------------------------------------------------------------------------
	
	public AlarmCycle setInterval( long ms ) {
		interval = ms;
		return this;
	}
	
	//--------------------------------------------------------------------------
	
	public AlarmCycle setWakeup( boolean wakeup ) {
		this.wakeup = wakeup;
		return this;
	}
	
	//--------------------------------------------------------------------------
	
	public void start() {
		Intent intent = new Intent( action, Uri.parse( "alarm:" + NAME ), CONTEXT, receiver_class );
		
		PendingIntent pending_intent = PendingIntent.getBroadcast( CONTEXT, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
		
		( (AlarmManager)CONTEXT.getSystemService( Context.ALARM_SERVICE )).setInexactRepeating(
			wakeup ? AlarmManager.RTC_WAKEUP : AlarmManager.RTC,
			System.currentTimeMillis(),
			interval,
			pending_intent );
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
