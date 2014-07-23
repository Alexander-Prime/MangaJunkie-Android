package net.mangajunkie.graphics;

import android.graphics.*;
import android.graphics.Paint.Align;
import android.graphics.Paint.Join;
import android.graphics.drawable.Drawable;
import net.mangajunkie.content.library.Chapter;

//==============================================================================
public class BookmarkDrawable extends Drawable {
	//--------------------------------------------------------------------------
	
	private final Path PATH = new Path(), SHADOW_PATH = new Path();
	private final Paint FILL_PAINT, STROKE_PAINT, SHADOW_PAINT, TEXT_PAINT;
	
	private String chapter = "";
	private float text_y;
	
	//--------------------------------------------------------------------------
	
	public BookmarkDrawable() {
		FILL_PAINT   = new Paint();
		STROKE_PAINT = new Paint();
		SHADOW_PAINT = new Paint();
		TEXT_PAINT   = new Paint();
		
		FILL_PAINT.setStyle( Paint.Style.FILL );
		
		STROKE_PAINT.setStyle( Paint.Style.STROKE );
		STROKE_PAINT.setStrokeWidth( 2 );
		STROKE_PAINT.setStrokeJoin( Join.ROUND );
		STROKE_PAINT.setAntiAlias( true );
		
		SHADOW_PAINT.setStyle( Paint.Style.FILL_AND_STROKE );
		SHADOW_PAINT.setStrokeWidth( 2 );
		SHADOW_PAINT.setStrokeJoin( Join.ROUND );
		SHADOW_PAINT.setAntiAlias( true );
		SHADOW_PAINT.setARGB( 48, 0, 0, 0 );
		
		TEXT_PAINT.setStyle(     Paint.Style.FILL         );
		TEXT_PAINT.setTypeface(  Typeface   .DEFAULT_BOLD );
		TEXT_PAINT.setTextAlign( Align      .CENTER       );
		TEXT_PAINT.setAntiAlias( true                     );
	}
	
	//--------------------------------------------------------------------------
	
	public void setFillColor( int color ) {
		FILL_PAINT.setColor( color );
	}

	//--------------------------------------------------------------------------
	
	public void setStrokeColor( int color ) {
		STROKE_PAINT.setColor( color );
	}
	
	//--------------------------------------------------------------------------
	
	public void setTextColor( int color ) {
		TEXT_PAINT.setColor( color );
	}
	
	//--------------------------------------------------------------------------
	
	public void setChapter( String chapter ) {
		this.chapter = chapter;
		scaleText();
	}

	//--------------------------------------------------------------------------
	
	public void setChapter( Chapter chapter ) {
		setChapter( chapter.toString() );
	}

	//--------------------------------------------------------------------------
	// Private parts
	//--------------------------------------------------------------------------
	
	private void scaleText() {
		TEXT_PAINT.setTextScaleX( 1 );
		TEXT_PAINT.setTextScaleX( Math.min(
			( getBounds().width() * 0.8f ) / TEXT_PAINT.measureText( chapter ), 1 ));
	}
	
	//--------------------------------------------------------------------------
	// From superclass Drawable
	//--------------------------------------------------------------------------
	
	@Override
	public void setBounds( int left, int top, int right, int bottom ) {
		super.setBounds( left, top, right, bottom );
		
		int width  = right - left,
			height = bottom - top,
			center_x = ( left + right ) / 2;
		
		final float peak = bottom - (( right - left ) / 6 );
		
		PATH.reset();
		PATH.moveTo( left  + 1,    bottom - 3 );
		PATH.lineTo( left  + 1,    top    + 1 );
		PATH.lineTo( right - 1,    top    + 1 );
		PATH.lineTo( right - 1,    bottom - 3 );
		PATH.lineTo( center_x, peak   );
		PATH.close();
		
		PATH.offset( 0, 2, SHADOW_PATH );
		
		TEXT_PAINT.setTextSize( (float)Math.min( width, height ) * 0.5f );
		
		text_y = top
		+ (((( bottom + peak ) / 2 ) - top ) / 2 )
		- (( TEXT_PAINT.ascent() + TEXT_PAINT.descent() )/ 2 );
		
		scaleText();
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public void draw( Canvas c ) {
		c.drawPath( SHADOW_PATH, SHADOW_PAINT );
		
		c.drawPath( PATH, FILL_PAINT   );
		c.drawPath( PATH, STROKE_PAINT );
		
		
		
		c.drawText( chapter, getBounds().centerX(), text_y, TEXT_PAINT );
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public void setAlpha( int arg0 ) {
		// TODO Auto-generated method stub
		
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public void setColorFilter( ColorFilter arg0 ) {
		// TODO Auto-generated method stub
		
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------