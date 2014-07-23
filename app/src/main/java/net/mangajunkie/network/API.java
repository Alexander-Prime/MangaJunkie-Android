package net.mangajunkie.network;

import net.mangajunkie.content.collection.Bookmark;
import net.mangajunkie.content.collection.User;
import net.mangajunkie.content.library.Chapter;
import net.mangajunkie.content.library.Cover;
import net.mangajunkie.content.library.Manga;
import net.mangajunkie.content.library.Page;

import java.net.URLEncoder;

//==============================================================================
public abstract class API {
	//--------------------------------------------------------------------------
	
	private static final String PROTOCOL = "http",
	                            HOST     = "api.mangajunkie.net",
	                            PATH     = "/v2";
	
	//--------------------------------------------------------------------------
	
	private static String getRootUrl() {
		return PROTOCOL + "://" + HOST + PATH;
	}
	
	//--------------------------------------------------------------------------
	
	public static String getMangaDirectoryUrl() {
		return getRootUrl() + "/manga";
	}
	
	//--------------------------------------------------------------------------
	
	public static String getSearchUrl( String query ) {
		String url_query = "";
		try { url_query = URLEncoder.encode( query, "UTF-8" ); }
		finally { return getMangaDirectoryUrl() + "/search?q=" + url_query; }
	}
	
	//--------------------------------------------------------------------------
	
	public static String getMangaUrl( Manga... manga ) {
		StringBuilder builder = new StringBuilder();
		String delimiter = ""; // Nothing goes before first element
		for ( Manga m : manga ) {
			builder.append( delimiter ).append( m.getSysName() );
			delimiter = ",";
		}
		return getMangaDirectoryUrl() + "/" + builder.toString();
	}
	
	//--------------------------------------------------------------------------
	
	public static String getAllChaptersUrl( Manga... manga ) {
		return getMangaUrl( manga ) + "/chapters";
	}
	
	//--------------------------------------------------------------------------
	
	public static String getCoverUrl( Cover cover ) {
		return getMangaUrl( cover.getManga() ) + "/cover";
	}
	
	//--------------------------------------------------------------------------
	
	public static String getChapterUrl( Chapter chapter ) {
		return getAllChaptersUrl( chapter.getManga() ) + "/" + chapter;
	}
	
	//--------------------------------------------------------------------------
	
	public static String getPageUrl( Page page ) {
		return getChapterUrl( page.getChapter() ) + "/" + page;
	}
	
	//--------------------------------------------------------------------------
	
	public static String getUserUrl( User user ) {
		return getRootUrl() + "/users/" + user.getUsername();
	}
	
	//--------------------------------------------------------------------------
	
	public static String getCollectionUrl( User user ) {
		return getUserUrl( user ) + "/bookmarks";
	}
	
	//--------------------------------------------------------------------------
	
	public static String getBookmarkUrl( User user, Bookmark bookmark ) {
		return getCollectionUrl( user ) + "/" + bookmark.getManga().getSysName();
	}
	
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------