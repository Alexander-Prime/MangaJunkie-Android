package net.mangajunkie.android.widget;

import android.content.Context;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Page;
import net.mangajunkie.storage.PageLoader;

//==============================================================================
public abstract class ChapterContainer {
	//--------------------------------------------------------------------------

	private final Chapter CHAPTER;

	private OnPageChangedListener listener;
	private UiToggler             toggler;
	private ChapterChanger        changer;
	
	private final PageLoader LOADER;

	protected static final Interpolator
	PULL_INTERPOLATOR      = new AccelerateInterpolator(),
	PULL_TEXT_INTERPOLATOR = new DecelerateInterpolator();

	//--------------------------------------------------------------------------

	protected ChapterContainer( Chapter chapter, Context context ) {
		CHAPTER = chapter;
		LOADER = new PageLoader( context );
	}

	//--------------------------------------------------------------------------

	protected Chapter getChapter() { return CHAPTER; }

	//--------------------------------------------------------------------------

	public final void setOnPageChangedListener( OnPageChangedListener listener ) { this.listener = listener; }

	//--------------------------------------------------------------------------
	
	protected final void notifyPageChanged( Page page ) { if ( listener != null ) listener.onPageChanged( page ); }
	
	//--------------------------------------------------------------------------
	
	public final void setUiToggler( UiToggler toggler ) { this.toggler = toggler; }
	
	//--------------------------------------------------------------------------

	protected final void toggleUi() { if ( toggler != null ) toggler.toggleUi(); }
	protected final void showUi()   { if ( toggler != null ) toggler.showUi();   }
	protected final void hideUi()   { if ( toggler != null ) toggler.hideUi();   }
	
	//--------------------------------------------------------------------------
	
	public final void setChapterChanger( ChapterChanger changer ) { this.changer = changer; }
	
	//--------------------------------------------------------------------------
	
	protected final void openAdjacentChapter( int direction ) { if ( changer != null ) changer.openAdjacentChapter( direction ); }
	
	//--------------------------------------------------------------------------
	
	public final PageLoader getPageLoader() { return LOADER; }
	
	//--------------------------------------------------------------------------
	
	public final void cancelAll() {
		LOADER.clear();
	}
	
	//--------------------------------------------------------------------------
	// Abstracts
	//--------------------------------------------------------------------------
	
	public abstract Page getCurrentPage();
	
	//--------------------------------------------------------------------------
	
	//==========================================================================
	public interface OnPageChangedListener {
		//----------------------------------------------------------------------
		
		public void onPageChanged( Page page );
		
		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
	
	//==========================================================================
	public interface UiToggler {
		//----------------------------------------------------------------------
		
		public void toggleUi();
		public void showUi();
		public void hideUi();
		
		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
	
	//==========================================================================
	public interface ChapterChanger {
		//----------------------------------------------------------------------
		
		public void openAdjacentChapter( int direction );
		
		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
