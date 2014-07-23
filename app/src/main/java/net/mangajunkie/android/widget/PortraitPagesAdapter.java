package net.mangajunkie.android.widget;


import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.view.View.OnClickListener;
import net.mangajunkie.android.fragment.PageFragment;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Page;
import net.mangajunkie.storage.PageLoader;

//==============================================================================
public class PortraitPagesAdapter extends FragmentStatePagerAdapter {
	//--------------------------------------------------------------------------

	private Chapter chapter;
	private OnClickListener listener;
	
	private final PageLoader LOADER;

	//--------------------------------------------------------------------------

	public PortraitPagesAdapter( FragmentManager fm, Chapter ch, OnClickListener listener, PageLoader loader ) {
		super( fm );
		chapter = ch;
		this.listener = listener;
		LOADER = loader;
	}

	//--------------------------------------------------------------------------

	@Override
	public int getCount() { return chapter.getPageCount(); }

	//--------------------------------------------------------------------------

	@Override
	public Fragment getItem( int index ) {
		Page page = pageAt( index );

		Bundle args = new Bundle();
		args.putString( "manga", chapter.getManga().getSysName() );
		args.putString( "chapter", chapter.toString() );
		args.putInt( "page", page.toInt() );
		PageFragment frag = new PageFragment();
		frag.setPageLoader( LOADER );
		frag.setOnClickListener( listener );
		frag.setArguments( args );
		return frag;
	}
	
	//--------------------------------------------------------------------------
	
	public Page pageAt( int index ) {
		return chapter.getPage( chapter.getPageCount() - index );
	}
	
	//--------------------------------------------------------------------------
	
	public int indexOf( Page page ) {
		return page.getChapter().getPageCount() - page.toInt();
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
