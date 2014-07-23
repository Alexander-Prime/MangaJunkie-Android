package net.mangajunkie.util;

import android.graphics.PointF;

import java.util.Arrays;
import java.util.Collections;

//==============================================================================
public class Bezier {
	//--------------------------------------------------------------------------

	private PointF[] points, buffer;

	//--------------------------------------------------------------------------

	public Bezier( float... coords ) {
		if ( coords.length == 0 ) throw new RuntimeException( "No points specified" );
		if ( coords.length % 2 != 0 ) throw new RuntimeException( "Need an even number of coordinates" );

		points = new PointF[coords.length/2];
		buffer = new PointF[points.length-1];

		for ( int i = 0; i < points.length; i++ ) points[i] = new PointF( coords[i*2], coords[i*2+1] );
		for ( int i = 0; i < buffer.length; i++ ) buffer[i] = new PointF();
	}

	//--------------------------------------------------------------------------

	public void set( Bezier b ) {
		for ( int i = 0; i < Math.min( points.length, b.points.length ); i++ ) {
			points[i].set( b.points[i] );
		}
	}

	//--------------------------------------------------------------------------

	public void setInterpolated( Bezier b1, Bezier b2, float interp ) {
		for ( int i = 0; i < Math.min( points.length, Math.min( b1.points.length, b2.points.length )); i++ ) {
			interpolate( b1.points[i], b2.points[i], interp, points[i] );
		}
	}

	//--------------------------------------------------------------------------

	public PointF getPoint( float interp, PointF dest ) {
		// TODO Can this be done in one pass?
		PointF[] src = points;
		for ( int size = points.length - 1; size >= 1; size-= 1 ) {
			if ( size < points.length - 1 ) src = buffer;

			for ( int j = 0; j < size; j++ ) interpolate( src[j], src[j+1], interp, buffer[j] );
		}

		dest.set( src[0] );
		return dest;
	}

	//--------------------------------------------------------------------------

	public float getSlope( float interp ) {
		PointF[] src = points;
		for ( int size = points.length - 1; size >= 2; size-= 1 ) {
			if ( size < points.length - 1 ) src = buffer;

			for ( int j = 0; j < size; j++ ) interpolate( src[j], src[j+1], interp, buffer[j] );
		}
		return ( buffer[1].y - buffer[0].y ) / ( buffer[1].x - buffer[0].x );
	}

	//--------------------------------------------------------------------------

	private PointF interpolate( PointF a, PointF b, float i, PointF dest ) {
		dest.set( interpolate( a.x, b.x, i ), interpolate( a.y, b.y, i ));
		return dest;
	}

	//--------------------------------------------------------------------------

	private float interpolate( float a, float b, float i ) { return a + i * ( b - a ); }

	//--------------------------------------------------------------------------

	public void reverse() {
		Collections.reverse( Arrays.asList( points ));
	}

	//--------------------------------------------------------------------------

	public float[] getPoints() {
		float[] output = new float[points.length*2];
		for ( int i = 0; i < points.length; i++ ) {
			output[i*2]   = points[i].x;
			output[i*2+1] = points[i].y;
		}
		return output;
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
