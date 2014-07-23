package net.mangajunkie.util;

import java.io.*;
import java.net.URL;

//==============================================================================
public abstract class Net {
	//--------------------------------------------------------------------------
	
	public static final boolean download( String source, File destination ) {
		try {
			destination.getParentFile().mkdirs();
			InputStream input = new URL( source ).openConnection().getInputStream();
			OutputStream output = new FileOutputStream( destination );
			
			byte[] buffer = new byte[1024];
			int bytes = 0;
			while (( bytes = input.read( buffer )) > -1 ) output.write( buffer, 0, bytes );
		
		} catch ( IOException e ) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
