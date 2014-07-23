package net.mangajunkie.content.library;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

//==============================================================================
public class Cover {
	//--------------------------------------------------------------------------
	
	private final Manga manga;
	private final int   volume;

	private static final BitmapFactory.Options OPTS = new BitmapFactory.Options();
	
	static {
		OPTS.inDither                 = true;
		OPTS.inPreferQualityOverSpeed = true;
		OPTS.inTempStorage            = new byte[1024];
		OPTS.inPreferredConfig        = Bitmap.Config.RGB_565;
		OPTS.inPurgeable              = true;
	}
	
	//--------------------------------------------------------------------------
	
	public Cover( Manga manga ) {
		this( manga, -1 );
	}
	
	//--------------------------------------------------------------------------
	
	private Cover( Manga manga, int volume ) {
		this.manga  = manga;
		this.volume = volume;
	}
	
	//--------------------------------------------------------------------------
	
	public Manga getManga() { return manga; }
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
