package net.mangajunkie.android.widget;

import android.app.FragmentManager;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import net.mangajunkie.R;
import net.mangajunkie.android.graphics.drawable.PageCurlDrawable;
import net.mangajunkie.android.view.HorizontalPullDetector;
import net.mangajunkie.android.view.HorizontalPullDetector.OnHorizontalPullListener;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Page;
import net.mangajunkie.util.ReadingDirection;

//==============================================================================
public class SinglePagerContainer
extends      ChapterContainer
implements   OnPageChangeListener,
             OnHorizontalPullListener,
             OnClickListener {
	//--------------------------------------------------------------------------
	
	private View
	pull_shade_left,
	pull_shade_right,
	pull_text_left,
	pull_text_right,
	pull_indicator_left,
	pull_indicator_right;
	
	private ViewPager            pager;
	private PortraitPagesAdapter adapter;
	
	private ReadingDirection read_direction = ReadingDirection.RTL;
	
	//--------------------------------------------------------------------------
	
	public SinglePagerContainer( Chapter chapter, Context context, FragmentManager manager, View parent, int start_page ) {
		super( chapter, context );
		
		// Assign view members
		pager                = (ViewPager)parent.findViewById( R.id.pager );
		pull_shade_left      =            parent.findViewById( R.id.pullShade_left );
		pull_shade_right     =            parent.findViewById( R.id.pullShade_right );
		pull_text_left       =            parent.findViewById( R.id.pullText_left );
		pull_text_right      =            parent.findViewById( R.id.pullText_right );
		pull_indicator_left  =            parent.findViewById( R.id.pullIndicator_left );
		pull_indicator_right =            parent.findViewById( R.id.pullIndicator_right );
		
		
		adapter = new PortraitPagesAdapter( manager, chapter, this, getPageLoader() );
		pager.setAdapter( adapter );
		if ( start_page == -1 ) start_page = getChapter().getPageCount();
		pager.setCurrentItem( adapter.indexOf( chapter.getPage( start_page )), false );
		pager.setPageTransformer( false, new Transformer() );
		pager.setOnTouchListener( new HorizontalPullDetector( context, 0.5f, this ));
	
		pager.setOnPageChangeListener( this );
		pager.setOffscreenPageLimit( 1 );
	}

	//--------------------------------------------------------------------------
	// From LayoutContainer
	//--------------------------------------------------------------------------
	
	@Override public Page getCurrentPage() {
		return adapter.pageAt( pager.getCurrentItem() );
	}
	
	//--------------------------------------------------------------------------
	// From OnPageChangeListener
	//--------------------------------------------------------------------------

	@Override public void onPageScrolled( int position, float offset, int offset_pixels ) {}
	
	//--------------------------------------------------------------------------
	
	@Override public void onPageSelected( int index ) {
		notifyPageChanged( adapter.pageAt( index ));
	}
	
	//--------------------------------------------------------------------------
	
	@Override public void onPageScrollStateChanged( int state ) { hideUi(); }
	
	//--------------------------------------------------------------------------
	// From OnHorizontalPullListener
	//--------------------------------------------------------------------------
	
	@Override public void onHorizontalPullStarted( int direction ) {
		if ( direction == -1 ) {
			pull_shade_right.setAlpha( 0 );
			pull_shade_right.animate().alpha( 1 ).setInterpolator( PULL_TEXT_INTERPOLATOR );

			pull_text_right.setAlpha( 0 );
			pull_text_right.setTranslationX( pull_text_right.getWidth() );
			pull_text_right.animate().alpha( 1 ).translationX( 0 ).setInterpolator( PULL_TEXT_INTERPOLATOR );
			
			pull_indicator_right.setScaleY( 0 );
			pull_indicator_right.setAlpha( 1 );
		}
		if ( direction == 1 ) {
			pull_shade_left.setAlpha( 0 );
			pull_shade_left.animate().alpha( 1 ).setInterpolator( PULL_TEXT_INTERPOLATOR );

			pull_text_left.setAlpha( 0 );
			pull_text_left.setTranslationX( -pull_text_left.getWidth() );
			pull_text_left.animate().alpha( 1 ).translationX( 0 ).setInterpolator( PULL_TEXT_INTERPOLATOR );
			
			pull_indicator_left.setScaleY( 0 );
			pull_indicator_left.setAlpha( 1 );
		}
	}
	
	//--------------------------------------------------------------------------

	@Override public void onHorizontalPull( float amount ) {
		int direction = (int)Math.signum( amount ); 
		if ( direction == -1 ) pull_indicator_right.setScaleY( PULL_INTERPOLATOR.getInterpolation( amount ));
		if ( direction ==  1 ) pull_indicator_left .setScaleY( PULL_INTERPOLATOR.getInterpolation( amount )); 
	}
	
	//--------------------------------------------------------------------------

	@Override public void onHorizontalPullCompleted( int direction ) {
		if ( direction == -1 ) {
			pull_shade_right.animate().alpha( 0 );
			pull_text_right.animate().alpha( 0 ).translationX( pull_text_right.getWidth() );
			pull_indicator_right.animate().alpha( 0 );
		}
		if ( direction ==  1 ) {
			pull_shade_left.animate().alpha( 0 );
			pull_text_left.animate().alpha( 0 ).translationX( -pull_text_left.getWidth() );
			pull_indicator_left.animate().alpha( 0 );
		}
		
		openAdjacentChapter( read_direction.fromPull( direction ));
	}
	
	//--------------------------------------------------------------------------

	@Override public void onHorizontalPullCancelled( int direction ) {
		if ( direction == -1 ) {
			pull_shade_right.animate().alpha( 0 );
			pull_text_right.animate().alpha( 0 ).translationX( pull_text_right.getWidth() );
			pull_indicator_right.animate().scaleY( 0 );
		}
		if ( direction ==  1 ) {
			pull_shade_left.animate().alpha( 0 );
			pull_text_left.animate().alpha( 0 ).translationX( -pull_text_left.getWidth() );
			pull_indicator_left.animate().scaleY( 0 );
		}
	}
	
	//--------------------------------------------------------------------------
	// From OnClickListener
	//--------------------------------------------------------------------------
	
	@Override public void onClick( View v ) { toggleUi(); }
	
	//--------------------------------------------------------------------------

	//==========================================================================
	private class Transformer implements ViewPager.PageTransformer {
		//----------------------------------------------------------------------

		@Override public void transformPage( View page, float position ) {
			page.setTranslationX( page.getWidth() * Math.max( -position, 0 ));
			ImageView view = (ImageView)page.findViewById( R.id.page );
			if ( !( view.getDrawable() instanceof PageCurlDrawable )) return;

			PageCurlDrawable d = (PageCurlDrawable)view.getDrawable();
			d.setCurl( Math.max( position, 0 ));

			View shadow = page.findViewById( R.id.shadow );
			shadow.setTranslationX( page.getWidth() * position );
			shadow.setAlpha( position * -8 );
		}

		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
