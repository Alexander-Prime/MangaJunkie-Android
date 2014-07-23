package net.mangajunkie.android.widget;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import net.mangajunkie.R;
import net.mangajunkie.android.view.VerticalPullDetector;
import net.mangajunkie.android.view.VerticalPullDetector.OnVerticalPullListener;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Page;

//==============================================================================
public class VerticalScrollContainer
extends      ChapterContainer
implements   OnItemClickListener,
             OnScrollListener,
             OnVerticalPullListener {
	//----------------------------------------------------------------------
	
	private View
	pull_shade_top,
	pull_shade_bottom,
	pull_text_top,
	pull_text_bottom,
	pull_indicator_top,
	pull_indicator_bottom;
	
	private ListView              list_view;
	private LandscapePagesAdapter adapter;
	
	private int visible_index;
	
	//----------------------------------------------------------------------
	
	public VerticalScrollContainer( Chapter chapter, Context context, View parent, int start_page ) {
		super( chapter, context );
		
		// Assign view members
		list_view             = (ListView)parent.findViewById( R.id.list );
		pull_shade_top        =           parent.findViewById( R.id.pullShade_top );
		pull_shade_bottom     =           parent.findViewById( R.id.pullShade_bottom );
		pull_text_top         =           parent.findViewById( R.id.pullText_top );
		pull_text_bottom      =           parent.findViewById( R.id.pullText_bottom );
		pull_indicator_top    =           parent.findViewById( R.id.pullIndicator_top );
		pull_indicator_bottom =           parent.findViewById( R.id.pullIndicator_bottom );
	
		adapter = new LandscapePagesAdapter( context, chapter, getPageLoader() );
		list_view.setAdapter( adapter );
		if ( start_page == -1 ) start_page = getChapter().getPageCount();
		list_view.setSelection( adapter.indexOf( chapter.getPage( start_page )));
		list_view.setOnTouchListener( new VerticalPullDetector( context, 0.5f, this ));
		list_view.setOnItemClickListener( this );
		list_view.setOnScrollListener( this );
	}

	//--------------------------------------------------------------------------
	// From LayoutContainer
	//--------------------------------------------------------------------------

	@Override public Page getCurrentPage() { return adapter.getItem( list_view.getFirstVisiblePosition() ); }

	//--------------------------------------------------------------------------
	// From OnClickListener
	//--------------------------------------------------------------------------

	@Override public void onItemClick( AdapterView<?> parent, View view, int position, long id ) { toggleUi(); }

	//--------------------------------------------------------------------------
	// From OnScrollListener
	//--------------------------------------------------------------------------

	@Override
	public void onScroll( AbsListView list_view, int index, int visible_count, int total_count ) {
		if ( visible_index != index ) {
			notifyPageChanged( adapter.getItem( index ));
			visible_index = index;
		}
	}

	//--------------------------------------------------------------------------

	@Override
	public void onScrollStateChanged( AbsListView list_view, int state ) {
		hideUi();
	}

	//--------------------------------------------------------------------------
	// From OnVerticalPullListener
	//--------------------------------------------------------------------------

	@Override public void onVerticalPullStarted( int direction ) {
		if ( direction == -1 ) {
			pull_shade_bottom.setAlpha( 0 );
			pull_shade_bottom.animate().alpha( 1 ).setInterpolator( PULL_TEXT_INTERPOLATOR );

			pull_text_bottom.setAlpha( 0 );
			pull_text_bottom.setTranslationY( pull_text_bottom.getHeight() );
			pull_text_bottom.animate().alpha( 1 ).translationY( 0 ).setInterpolator( PULL_TEXT_INTERPOLATOR );

			pull_indicator_bottom.setScaleX( 0 );
			pull_indicator_bottom.setAlpha( 1 );
		}
		if ( direction == 1 ) {
			pull_shade_top.setAlpha( 0 );
			pull_shade_top.animate().alpha( 1 ).setInterpolator( PULL_TEXT_INTERPOLATOR );

			pull_text_top.setAlpha( 0 );
			pull_text_top.setTranslationY( -pull_text_top.getHeight() );
			pull_text_top.animate().alpha( 1 ).translationY( 0 ).setInterpolator( PULL_TEXT_INTERPOLATOR );

			pull_indicator_top.setScaleX( 0 );
			pull_indicator_top.setAlpha( 1 );
		}
	}

	//--------------------------------------------------------------------------

	@Override public void onVerticalPull( float amount ) {
		int direction = (int)Math.signum( amount );
		if ( direction == -1 ) pull_indicator_bottom.setScaleX( PULL_INTERPOLATOR.getInterpolation( amount ) );
		if ( direction == 1 ) pull_indicator_top.setScaleX( PULL_INTERPOLATOR.getInterpolation( amount ) );

		Log.d( "MJ", amount + " -> " + PULL_INTERPOLATOR.getInterpolation( amount ) );
	}

	//--------------------------------------------------------------------------

	@Override public void onVerticalPullCompleted( int direction ) {
		if ( direction == -1 ) {
			pull_shade_bottom.animate().alpha( 0 );
			pull_text_bottom.animate().alpha( 0 ).translationY( pull_text_bottom.getHeight() );
			pull_indicator_bottom.animate().alpha( 0 );
		}
		if ( direction == 1 ) {
			pull_shade_top.animate().alpha( 0 );
			pull_text_top.animate().alpha( 0 ).translationY( -pull_text_bottom.getHeight() );
			pull_indicator_top.animate().alpha( 0 );
		}
		
		openAdjacentChapter( -direction );
	}

	//--------------------------------------------------------------------------

	@Override public void onVerticalPullCancelled( int direction ) {
		if ( direction == -1 ) {
			pull_shade_bottom.animate().alpha( 0 );
			pull_text_bottom.animate().alpha( 0 ).translationY( pull_text_bottom.getWidth() );
			pull_indicator_bottom.animate().scaleX( 0 );
		}
		if ( direction == 1 ) {
			pull_shade_top.animate().alpha( 0 );
			pull_text_top.animate().alpha( 0 ).translationY( -pull_text_top.getWidth() );
			pull_indicator_top.animate().scaleX( 0 );
		}
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
