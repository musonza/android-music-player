package com.tashtin.zimvibes;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;

import com.tashtin.zimvibes.models.StationSong;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class GlobalContext {
	
	private static AndroidHttpClient httpclient;
	private static HttpContext localContext;
	private static MediaPlayer mediaPlayer;
	private static String username;
	private static int current_song_index = 0;
	private static int skip_count = 0;
	private static int station_index = -1;
	private static JSONArray likesArray;
	private static boolean session_expired;
	private static List<StationSong> songs = new ArrayList<StationSong>();
	
	@SuppressLint("NewApi") public static final void configureHttpService() {
		Log.i("GlobalContext", "Configuring HttpService");
		setHttpClient(AndroidHttpClient.newInstance("Android"));
		setLocalContext(new BasicHttpContext());
		BasicCookieStore cookieStore = new BasicCookieStore();
		getLocalContext().setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		GlobalContext.setHttpClient(getHttpClient());
		GlobalContext.setLocalContext(getLocalContext());
	}
	
	public static AndroidHttpClient getHttpClient() {
		return httpclient;
	}
	
	public static void setHttpClient(AndroidHttpClient httpclient) {
		GlobalContext.httpclient = httpclient;
	}
	
	public static HttpContext getLocalContext() {
		return localContext;
	}
	
	public static void setLocalContext(HttpContext localContext) {
		GlobalContext.localContext = localContext;
	}
	
	public static final void configurePlayer(){
		mediaPlayer = new MediaPlayer();
	}
	
	public static MediaPlayer getMediaPlayerObj(){
		return mediaPlayer;
	}
	
	public static List<StationSong> getSongs(){
		return songs;
	}
	
	public static void setSongs(List<StationSong> songs){
		GlobalContext.songs = songs;
	}
	
	public static void setMediaPlayerObj(MediaPlayer mediaPlayer){
		GlobalContext.mediaPlayer = mediaPlayer;
	}
	
	public static String getUsername(){        
		return username;
	}
	
	public static void setUsername(String username){
		GlobalContext.username = username;
	}
	
	public static void setCurrentSongIndex(int index){
		GlobalContext.current_song_index = index;
	}
	
	public static int getCurrentSongIndex(){
		return GlobalContext.current_song_index;
	}
	
	public static void setSkipCount(int count){
		GlobalContext.skip_count = count;
	}
	
	public static int getSkipCount(){
		return skip_count;
	}
	
	public static void setStationIndex(int index){
		GlobalContext.station_index = index;
	}
	
	public static int getStationIndex(){
		return station_index;
	}

	public static void setLikesArray(JSONArray likes){
		likesArray = likes;
	}

	public static JSONArray getLikesArray(){
		return likesArray;
	}

	public static void setSession_expired(boolean is_expired){
		session_expired = is_expired;
	}

	public static boolean getSessionExpired(){
		return session_expired;
	}

}