package net.mangajunkie.android.activity;

import android.app.Activity;
import android.os.Bundle;
import com.android.debug.hv.ViewServer;

//==============================================================================
public class BaseActivity extends Activity { // For debugging; release version is a stub
	//--------------------------------------------------------------------------
	
	@Override public void onCreate( Bundle state ) {
		super.onCreate( state );
		ViewServer.get( this ).addWindow( this );
	}
	
	//--------------------------------------------------------------------------
	
	@Override public void onDestroy() {
		super.onDestroy();
		ViewServer.get( this ).removeWindow( this );
	}
	
	//--------------------------------------------------------------------------
	
	@Override public void onResume() {
		super.onResume();
		ViewServer.get( this ).setFocusedWindow( this );
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
