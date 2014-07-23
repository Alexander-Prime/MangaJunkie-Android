package net.mangajunkie.android.graphics.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;

import net.mangajunkie.util.Bezier;

//==============================================================================
public class PageCurlDrawable extends BitmapDrawable {
	//--------------------------------------------------------------------------
	
	private static final int
		ROWS          = 20,
		COLUMNS       = 20;

	private static final float BULGE_FACTOR = 0.45f; // To reduce vertical stretching

	// TODO private static final Bezier REST_CURVE = new Bezier( 0, 0, 0.33f, 0,  0.66f, 0, 1, 0 );

	// Page curl shape via cubic beziers
	// Defined assuming left-side binding, even though the default is reversed
	private static final Bezier[] CURL_CURVES = new Bezier[] {
		new Bezier( 0.0f, 0.0f,  0.33f,0.0f,     0.66f, 0.0f,  1.0f, 0.0f  ),
		new Bezier( 0.2f, 0.0f,  0.3f, 0.22f,    0.66f, 0.0f,  1.0f, 0.0f  ),
		new Bezier( 0.4f, 0.0f,  0.2f, 0.2f,     0.66f, 0.06f, 1.0f, 0.04f ),
		new Bezier( 0.6f, 0.0f,  0.2f, 0.1f,     0.58f, 0.1f,  1.0f, 0.1f  ),
		new Bezier( 0.8f, 0.0f,  0.24f,0.06f,    0.58f, 0.14f, 1.0f, 0.08f ),
		new Bezier( 1.0f, 0.0f,  0.52f,0.0f,     0.54f, 0.1f,  1.0f, 0.02f )
	};

	private final Bezier CURVE = new Bezier( 0, 0, 0, 0, 0, 0, 0, 0 );
	private final float[] MESH = new float[(ROWS+1)*(COLUMNS+1)*2];
	private final int[] COLORS = new int[(ROWS+1)*(COLUMNS+1)];

	private int gravity;
	private float curl;

	//--------------------------------------------------------------------------

	public PageCurlDrawable( Resources res, Bitmap bitmap ) {
		this( res, bitmap, Gravity.RIGHT );
	}
	
	//--------------------------------------------------------------------------
	
	public PageCurlDrawable( Resources res, Bitmap bitmap, int gravity ) {
		super( res, bitmap );
		this.gravity = gravity;
	}
	
	//--------------------------------------------------------------------------
	
	@Override public void draw( Canvas c ) {
		c.save();

		if ( gravity == Gravity.RIGHT ) {
			c.scale( -1, 1, getBounds().exactCenterX(), getBounds().exactCenterY() );
		}

		c.drawBitmapMesh( getBitmap(), ROWS, COLUMNS, MESH, 0, COLORS, 0, null );

//		Paint paint = new Paint();
//		paint.setStrokeWidth( 5 );
//		paint.setColor( 0xffff0000 );
//		paint.setStyle( Paint.Style.STROKE );
//		c.drawPoints( MESH, paint );

		c.restore();
	}

	//--------------------------------------------------------------------------

	public void setCurl( float amount /*0..1*/ ) {
		curl = amount;
		updateMesh();
	}

	//--------------------------------------------------------------------------

	@Override public void setBounds( Rect bounds ) {
		super.setBounds( bounds );
		updateMesh();
	}

	//--------------------------------------------------------------------------

	@Override public void setBounds( int left, int top, int right, int bottom ) {
		super.setBounds( left, top, right, bottom );
		updateMesh();
	}

	//--------------------------------------------------------------------------

	private void updateMesh() {
		if ( getBounds().isEmpty() || curl < 0 || curl > 1 ) return; // Invalid state

		int   curl_index    = (int)( curl * ( CURL_CURVES.length - 1 ));
		float curl_fraction =      ( curl * ( CURL_CURVES.length - 1 )) % 1;

		// Calculate intermediate bezier
		if ( curl_fraction == 0 ) CURVE.set( CURL_CURVES[curl_index] );
		else CURVE.setInterpolated( CURL_CURVES[curl_index], CURL_CURVES[curl_index+1], curl_fraction );

		if ( gravity == Gravity.RIGHT ) CURVE.reverse();

		// Apply curve values to mesh
		int vert_col, vert_row;
		float y_factor;
		PointF point = new PointF();
		for ( int i = 0; i < COLORS.length; i += 1 ) {

			vert_col = i % ( COLUMNS + 1 );
			vert_row = i / ( COLUMNS + 1 );

			y_factor = ( vert_row / ( ROWS + 1f ) - 0.5f ) * 2;
			CURVE.getPoint( vert_col / ( (float)COLUMNS ), point );
			MESH[i*2]   = point.x;
			MESH[i*2+1] = ( vert_row / ( (float)ROWS )) + ( point.y * y_factor * BULGE_FACTOR );

			// Scale to bounds size
			MESH[i*2]   *= getBounds().width();
			MESH[i*2+1] *= getBounds().height();

			// Get color from slope
			float slope = CURVE.getSlope( vert_col / ( (float)COLUMNS ) );
			int shade = (int)Math.max( 0xff - Math.abs( 0xff * slope * 0.1 ), 0x88 );
			COLORS[i] = Color.rgb( shade, shade, shade );
		}

		invalidateSelf();
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
