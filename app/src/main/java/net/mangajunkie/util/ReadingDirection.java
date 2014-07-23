package net.mangajunkie.util;

//==============================================================================
public enum ReadingDirection {
	//--------------------------------------------------------------------------
	
	//==========================================================================
	RTL( -1 ) {
		//----------------------------------------------------------------------
		
		@Override public int toRelative( int abs_dir  ) { return -abs_dir;  }
		@Override public int toAbsolute( int rel_dir  ) { return -rel_dir;  }
		@Override public int fromPull(   int pull_dir ) { return  pull_dir; }
					
		//----------------------------------------------------------------------
	},
	//--------------------------------------------------------------------------

	//==========================================================================
	LTR( 1 ) {
		//----------------------------------------------------------------------
		
		@Override public int toRelative( int abs_dir  ) { return  abs_dir;  }
		@Override public int toAbsolute( int rel_dir  ) { return  rel_dir;  }
		@Override public int fromPull(   int pull_dir ) { return -pull_dir; }
		
		//----------------------------------------------------------------------
	};
	//--------------------------------------------------------------------------
	
	public final int PREV, NEXT, PREV_PULL, NEXT_PULL;
	
	//--------------------------------------------------------------------------
	
	private ReadingDirection( int factor ) {
		PREV      = -factor;
		NEXT      =  factor;
		PREV_PULL =  factor;
		NEXT_PULL = -factor;
	}
	
	//--------------------------------------------------------------------------
	
	public abstract int toRelative( int abs_dir  );
	public abstract int toAbsolute( int rel_dir  );
	public abstract int fromPull(   int pull_dir );

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
