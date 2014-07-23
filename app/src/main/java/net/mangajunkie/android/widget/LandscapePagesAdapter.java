package net.mangajunkie.android.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import org.jetbrains.annotations.Nullable;
import net.mangajunkie.R;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Page;
import net.mangajunkie.storage.PageLoader;
import net.mangajunkie.storage.PageLoader.OnPageLoadedListener;

import java.util.HashMap;
import java.util.Map;

//==============================================================================
public class LandscapePagesAdapter
extends      BaseAdapter {
	//--------------------------------------------------------------------------

	private final Chapter    CHAPTER;
	private final Context    CONTEXT;
	private final PageLoader LOADER;

	private final Map<ImageView,Page> VIEWS = new HashMap<>();

	//--------------------------------------------------------------------------

	public LandscapePagesAdapter( Context context, Chapter ch, PageLoader loader ) {
		CONTEXT = context.getApplicationContext();
		CHAPTER = ch;

		LOADER = loader;
	}

	//--------------------------------------------------------------------------

	@Override public int getCount() { return CHAPTER.getPageCount(); }

	//--------------------------------------------------------------------------

	@Override
	public Page getItem( int index ) { return CHAPTER.getPage( index + 1 ); }

	//--------------------------------------------------------------------------

	@Override public long getItemId( int index ) { return index; }

	//--------------------------------------------------------------------------

	public int indexOf( Page page ) {
		return page.toInt() - 1;
	}
	
	//--------------------------------------------------------------------------

	@Nullable @Override
	public View getView( int index, View view, ViewGroup parent ) {
		if ( view == null ) { view = LayoutInflater.from( CONTEXT ).inflate( R.layout.page, parent, false ); }
		LayoutParams params = new LayoutParams( parent.getWidth(), (int)( parent.getWidth() * 1.5 ));
		params.height = (int)( params.width * 1.5 );
		view.setLayoutParams( params );
		
		final ImageView page_view = (ImageView)view.findViewById( R.id.page );
		
		page_view.setImageBitmap( null );
		LOADER.cancel( VIEWS.get( page_view ));
		VIEWS.put( page_view, getItem( index ));
		LOADER.load( getItem( index ), new OnPageLoadedListener() {
			@Override public void onPageLoaded( Page page, Bitmap image ) {
				page_view.setImageBitmap( image );
			}
			@Override public void onPageLoadFailed( Page page ) {}
		});
		
		return view;
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
