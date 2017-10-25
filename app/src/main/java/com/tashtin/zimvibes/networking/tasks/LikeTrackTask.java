package com.tashtin.zimvibes.networking.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.tashtin.zimvibes.networking.API;
import com.tashtin.zimvibes.networking.API_KEY;
import com.tashtin.zimvibes.networking.PostRequest;

public class LikeTrackTask extends PostRequest{
	
	private String track_id;
	private String access_token;
	private String title;
	private Boolean isLike;
	
	public LikeTrackTask(String track_id, String access_token, String title, Boolean isLike) {
		super(String.class);
		
		this.track_id = track_id;
		this.access_token = access_token;
		this.title = title;
		this.isLike = isLike;
	}
	
	
	@Override
	public String loadDataFromNetwork() throws Exception {
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair(API_KEY.ACCESS_TOKEN, access_token));
		nameValuePairs.add(new BasicNameValuePair(API_KEY.TRACK_ID, track_id));
		nameValuePairs.add(new BasicNameValuePair("provider", "soundcloud"));
		nameValuePairs.add(new BasicNameValuePair("title", title));

		if(isLike){
			setApi(API.LIKE);
		}else{
			setApi(API.DISLIKE);
		}


		setEntity(new UrlEncodedFormEntity(nameValuePairs));
		return super.loadDataFromNetwork();
	}
}
