package net.mangajunkie.android.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import net.mangajunkie.R;
import net.mangajunkie.android.graphics.drawable.PageCurlDrawable;
import net.mangajunkie.android.view.TouchImageView;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Manga;
import net.mangajunkie.content.library.Page;
import net.mangajunkie.storage.PageLoader;
import net.mangajunkie.storage.PageLoader.OnPageLoadedListener;

//==============================================================================
public class PageFragment
extends      Fragment
implements   OnClickListener,
             OnPageLoadedListener {
	//--------------------------------------------------------------------------

	private Page page;

	private TouchImageView page_view;
	private View           failed_view, progress;

	private PageLoader      loader;
	private OnClickListener listener;

	//--------------------------------------------------------------------------

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup parent, Bundle state ) {
		Manga manga = new Manga( getArguments().getString( "manga" ));
		Chapter chapter = new Chapter( manga, getArguments().getString( "chapter" ));
		page = new Page( chapter, getArguments().getInt( "page" ));

		View view = inflater.inflate( R.layout.page, parent, false );

		failed_view = view.findViewById( R.id.failed );
		progress = view.findViewById( R.id.progress );
		progress.setVisibility( View.VISIBLE );

		page_view = (TouchImageView)view.findViewById( R.id.page );
		page_view.setOnClickListener( this );

		view.findViewById( R.id.retry ).setOnClickListener( this );
		return view;
	}
	
	//--------------------------------------------------------------------------
	
	public void setPageLoader( PageLoader page_loader ) {
		loader = page_loader;
	}
	
	//--------------------------------------------------------------------------

	@Override public void onViewCreated( View parent, Bundle state ) {
		loadPage();
	}

	//--------------------------------------------------------------------------

	@Override public void onClick( View view ) {
		switch ( view.getId() ) {
			case R.id.retry: // "Try again" button
				showProgress();
				loadPage();
				break;
			
			case R.id.page:
				if ( listener != null ) listener.onClick( view );
				break;
		}
	}

	//--------------------------------------------------------------------------
	
	private void loadPage() {
		loader.load( page, new OnPageLoadedListener() {
			@Override public void onPageLoaded( Page page, Bitmap image ) {
				page_view.setImageDrawable( new PageCurlDrawable( getResources(), image ));
				showPage();
			}

			@Override public void onPageLoadFailed( Page page ) {
				showFailed();
			}
		} );
	}
	
	//--------------------------------------------------------------------------

	private void showProgress() {
		progress   .setVisibility( View.VISIBLE );
		failed_view.setVisibility( View.GONE    );
		page_view  .setVisibility( View.GONE    );
	}

	//--------------------------------------------------------------------------

	private void showFailed() {
		progress   .setVisibility( View.GONE    );
		failed_view.setVisibility( View.VISIBLE );
		page_view  .setVisibility( View.GONE    );
	}

	//--------------------------------------------------------------------------

	private void showPage() {
		progress   .setVisibility( View.GONE    );
		failed_view.setVisibility( View.GONE    );
		page_view  .setVisibility( View.VISIBLE );
	}

	//--------------------------------------------------------------------------

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		loader.cancel( page );
	}

	//--------------------------------------------------------------------------

	@Override
	public void onSaveInstanceState( Bundle state ) {
		super.onSaveInstanceState( state );
		state.putInt( "page", page.toInt() );
	}
	
	//--------------------------------------------------------------------------
	
	public void setOnClickListener( OnClickListener listener ) {
		this.listener = listener;
	}
	
	//--------------------------------------------------------------------------
	//--------------------------------------------------------------------------
	
	@Override public void onPageLoaded( Page page, Bitmap image ) {
		if ( page == this.page ) page_view.setImageBitmap( image );
		showPage();
	}
	
	//--------------------------------------------------------------------------
	
	@Override public void onPageLoadFailed( Page page ) {
		showFailed();
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
