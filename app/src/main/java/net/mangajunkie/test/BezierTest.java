package net.mangajunkie.test;

import android.graphics.PointF;
import android.test.InstrumentationTestCase;

import net.mangajunkie.util.Bezier;

import java.util.Arrays;

//==============================================================================
public class BezierTest extends InstrumentationTestCase {
	//--------------------------------------------------------------------------

	public void testGetPoints() {
		float[] points = new float[] { 0.0f, 0.0f, 0.5f, 0.5f, 1.0f, 1.0f };
		Bezier bezier = new Bezier( points );

		assertTrue( Arrays.equals( points, bezier.getPoints() ) );
	}

	//--------------------------------------------------------------------------

	public void testEndPoints() {
		Bezier bezier = new Bezier( 0, 0,  0, 1,  1, 1,  1, 0 );
		assertEquals( new PointF( 0, 0 ), bezier.getPoint( 0, new PointF() ));
		assertEquals( new PointF( 1, 0 ), bezier.getPoint( 1, new PointF() ));
	}

	//--------------------------------------------------------------------------

	public void testPointInterpolation() {
		Bezier bezier = new Bezier( 0, 0,  0, 1,  1, 1,  1, 0 );

		assertEquals( new PointF( 0.5f, 0.75f ), bezier.getPoint( 0.5f, new PointF() ));
	}

	//--------------------------------------------------------------------------

	public void testCurveInterpolation() {
		Bezier b1 = new Bezier( 0, 0,  0, 1,  1, 1,  1, 0 ),
		       b2 = new Bezier( 0, 0,  0, 0,  1, 0,  1, 0 ),
		       b3 = new Bezier( 0, 0,  0, 0,  0, 0,  0, 0 );
		float[] expected_points = new float[] { 0, 0, 0, 0.5f, 1, 0.5f, 1, 0 };

		b3.setInterpolated( b1, b2, 0.5f );

		assertTrue( Arrays.equals( expected_points, b3.getPoints() ));
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
