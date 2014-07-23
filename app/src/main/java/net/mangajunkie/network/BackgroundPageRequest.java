package net.mangajunkie.network;

/*
//==============================================================================
public class BackgroundPageRequest extends Request<File> {
	//--------------------------------------------------------------------------

	private final Page PAGE;
	private final Listener<File> LISTENER;

	//--------------------------------------------------------------------------

	public BackgroundPageRequest( Page page, Listener<File> listener, ErrorListener error_listener ) {
		super( Method.GET, API.getPageUrl( page ), error_listener );
		PAGE = page;
		LISTENER = listener;
	}
	
	//--------------------------------------------------------------------------

	@Override
	protected Response<File> parseNetworkResponse( NetworkResponse network_response ) {
		File file = App.getDiskCache().getCacheFile( PAGE );
		if ( !file.isFile() ) return null;
		return Response.success( file, HttpHeaderParser.parseCacheHeaders( network_response ));
	}

	//--------------------------------------------------------------------------

	@Override
	protected void deliverResponse( File file ) { LISTENER.onResponse( file ); }
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
*/