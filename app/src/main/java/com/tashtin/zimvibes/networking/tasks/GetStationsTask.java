package com.tashtin.zimvibes.networking.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.tashtin.zimvibes.networking.API;
import com.tashtin.zimvibes.networking.API_KEY;
import com.tashtin.zimvibes.networking.GetRequest;


public class GetStationsTask  extends GetRequest {
	
	public String access_token;
	
	public GetStationsTask(String access_token) {
		super(String.class);
		this.access_token = access_token;
	}
	
	
	@Override
	public String loadDataFromNetwork() throws Exception {
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair(API_KEY.ACCESS_TOKEN, ""));
		
		setApi(API.GET_STATIONS + "?access_token=" + access_token);

		return super.loadDataFromNetwork();
	}

}
