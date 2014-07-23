package net.mangajunkie.android.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import net.mangajunkie.R;

//==============================================================================
public class DrawerAdapter extends BaseAdapter {
	//--------------------------------------------------------------------------
	
	private final LayoutInflater inflater;
	
	//--------------------------------------------------------------------------
	
	public DrawerAdapter( Context context ) {
		inflater = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
	}
	
	//--------------------------------------------------------------------------

	@Override
	public int getCount() {
		return 1;
	}
	
	//--------------------------------------------------------------------------

	@Override
	public String getItem( int i ) {
		return "All Bookmarks";
	}
	
	//--------------------------------------------------------------------------

	@Override
	public long getItemId( int index ) {
		/*switch ( index ) {
			case 0:
				return R.id.all_bookmarks;
		}*/
		return index;
	}
	
	//--------------------------------------------------------------------------

	@Override
	public View getView( int index, View view, ViewGroup parent ) {
		if ( view == null ) view = inflater.inflate( R.layout.listitem_nav, parent, false );
		( (TextView)view.findViewById( R.id.title )).setText( getItem( index ));
		return view;
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
