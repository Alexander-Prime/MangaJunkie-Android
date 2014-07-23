package net.mangajunkie.android.fragment;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import net.mangajunkie.R;
import net.mangajunkie.android.app.App;

//==============================================================================
public class BrightnessFragment
extends      DialogFragment
implements   OnCheckedChangeListener,
             OnSeekBarChangeListener {
	//--------------------------------------------------------------------------
	
	private CheckBox checkBox_useSystemBrightness;
	private SeekBar seekBar_brightness;
	
	private Handler handler;
	private Runnable runnable_dismiss;
	
	//--------------------------------------------------------------------------
	
	@Override public void onCreate( Bundle state ) {
		super.onCreate( state );
		handler = new Handler();
	}
	
	//--------------------------------------------------------------------------
	
	@Override public Dialog onCreateDialog( Bundle state ) {
		View parent = getActivity().getLayoutInflater().inflate( R.layout.dialog_brightness, null );
		
		checkBox_useSystemBrightness = (CheckBox)parent.findViewById( R.id.useSystemBrightness );
		seekBar_brightness = (SeekBar)parent.findViewById( R.id.brightness );

		checkBox_useSystemBrightness.setChecked( App.getPrefs().getUseSystemBrightness() );
		seekBar_brightness.setProgress( (int)( 100 * App.getPrefs().getReadingBrightness() ));
		seekBar_brightness.setEnabled( !checkBox_useSystemBrightness.isChecked() );
		
		checkBox_useSystemBrightness.setOnCheckedChangeListener( this );
		seekBar_brightness.setOnSeekBarChangeListener( this );
		
		Dialog dialog = new Builder( getActivity() ).setView( parent ).create();
		dialog.getWindow().clearFlags( LayoutParams.FLAG_DIM_BEHIND );
		
		delayDismiss();
		
		return dialog;
	}

	//--------------------------------------------------------------------------
	
	private void delayDismiss() {
		handler.removeCallbacks( runnable_dismiss );
		
		if ( runnable_dismiss == null ) runnable_dismiss = new Runnable() {
			@Override public void run() {
				if ( getDialog() == null ) return;
				getDialog().dismiss();
			}
		};
		
		// Keep visible for 4 seconds
		handler.postDelayed( runnable_dismiss, 4000 );
	}
	
	//--------------------------------------------------------------------------

	@Override public void onCheckedChanged( CompoundButton checkBox, boolean checked ) {
		App.getPrefs().edit().setUseSystemBrightness( checked ).apply();
		seekBar_brightness.setEnabled( !checked );
		delayDismiss();
	}

	//--------------------------------------------------------------------------

	@Override public void onProgressChanged( SeekBar bar, int progress, boolean userInitiated ) {
		App.getPrefs().edit().setReadingBrightness( progress / 100f ).apply();
		delayDismiss();
	}

	//--------------------------------------------------------------------------

	@Override public void onStartTrackingTouch( SeekBar seekBar ) {/*STUB*/}
	@Override public void onStopTrackingTouch( SeekBar seekBar ) {/*STUB*/}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
