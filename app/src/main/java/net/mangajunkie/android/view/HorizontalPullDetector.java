package net.mangajunkie.android.view;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

//==============================================================================
public class HorizontalPullDetector
extends      SimpleOnGestureListener
implements   OnTouchListener {
	//--------------------------------------------------------------------------

	private View                     touch_view;
	private GestureDetector          detector;
	private OnHorizontalPullListener pull_listener;

	private float current_distance, max_amount;

	private boolean completed;

	//--------------------------------------------------------------------------

	public HorizontalPullDetector( Context context ) {
		this( context, 0.5f, null ); // Is 50% a sensible default?
	}

	//--------------------------------------------------------------------------

	public HorizontalPullDetector( Context context, float pull_distance ) {
		this( context, pull_distance, null );
	}

	//--------------------------------------------------------------------------

	public HorizontalPullDetector( Context context, float pull_distance, OnHorizontalPullListener listener ) {
		detector = new GestureDetector( context, this );
		max_amount = pull_distance;
		pull_listener = listener;
	}

	//--------------------------------------------------------------------------

	public void setMaxPullDistance( float distance ) {
		max_amount = distance;
	}

	//--------------------------------------------------------------------------
	
	public void setOnPullListener( OnHorizontalPullListener listener ) { pull_listener = listener; }
	
	//--------------------------------------------------------------------------

	@Override public boolean onTouch( View view, MotionEvent event ) {
		if ( view == null ) return false; // Can this happen?
		
		// Save this view to be processed by other events
		// Since all touch events are on the same thread, there is no danger of
		// this changing during events, but maybe it isn't needed if we make
		// max_amount an absolute pixel value instead of a percentage?
		touch_view = view;
		
		if ( event.getActionMasked() == MotionEvent.ACTION_UP ) {
			if ( !completed && current_distance != 0 ) cancelPull();
			completed = false;
		}
		
		return !completed && detector.onTouchEvent( event );
	}
	
	//--------------------------------------------------------------------------

	@Override
	public boolean onScroll( MotionEvent event1, MotionEvent event2, float x_dist, float y_dist ) {
		// Ignore glitchy less-than-one-pixel events
		if ( Math.abs( x_dist ) >= 1 ) {
			
			// Reduce huge events
			// TODO: Why does this happen sometimes?
			float max_step = touch_view.getWidth() * max_amount * 0.1f;
			if ( Math.abs( x_dist ) > max_step ) x_dist = max_step * Math.signum( x_dist );
			
			// Cancel if scrolling in the opposite direction from current_distance 
			if ( Math.signum( current_distance ) ==  Math.signum( x_dist )) cancelPull(); // Went the other way
			
			// Add scroll value to pull if the container isn't doing its own scrolling
			if ( !touch_view.canScrollHorizontally( (int)x_dist )) {
				current_distance -= x_dist;
				if ( current_distance == -x_dist ) startPull();
				pull();
				if ( Math.abs( current_distance ) > touch_view.getWidth() * max_amount ) completePull();
			}
		}
		return false;
	}
	
	//--------------------------------------------------------------------------
	
	private void startPull() { // Gesture has begun
		if ( pull_listener != null ) pull_listener.onHorizontalPullStarted( (int)Math.signum( current_distance ) );
	}
	
	//--------------------------------------------------------------------------
	
	private void pull() { // Gesture continues to pull in the same direction
		if ( pull_listener != null ) pull_listener.onHorizontalPull( current_distance / ( max_amount * touch_view.getWidth() ) );
	}
	
	//--------------------------------------------------------------------------
	
	private void completePull() { // Gesture has met the target; do action
		if ( pull_listener != null ) pull_listener.onHorizontalPullCompleted( (int)Math.signum( current_distance ) );
		current_distance = 0;
		completed = true;
	}
	
	//--------------------------------------------------------------------------
	
	private void cancelPull() { // Gesture has reversed or ended
		if ( pull_listener != null ) pull_listener.onHorizontalPullCancelled( (int)Math.signum( current_distance ) );
		current_distance = 0;
	}
	
	//--------------------------------------------------------------------------
	
	//==========================================================================
	public static interface OnHorizontalPullListener {
		//----------------------------------------------------------------------
		
		public void onHorizontalPullStarted( int direction );
		public void onHorizontalPull( float amount );
		public void onHorizontalPullCompleted( int direction );
		public void onHorizontalPullCancelled( int direction );
		
		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
