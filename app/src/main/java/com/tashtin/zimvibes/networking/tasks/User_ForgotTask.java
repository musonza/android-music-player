package com.tashtin.zimvibes.networking.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.tashtin.zimvibes.networking.API;
import com.tashtin.zimvibes.networking.API_KEY;
import com.tashtin.zimvibes.networking.PostRequest;

public class User_ForgotTask extends PostRequest{
	
	private String email;
	
	public User_ForgotTask(String email) {
		super(String.class);
		
		this.email = email;
	}
	
	
	@Override
	public String loadDataFromNetwork() throws Exception {
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair(API_KEY.EMAIL, email));
		
		setApi(API.FORGOT);
		setEntity(new UrlEncodedFormEntity(nameValuePairs));
		return super.loadDataFromNetwork();
	}
	
	/**
	* This method generates a unique cache key for this request. In this case
	* our cache key depends just on the keyword.
	* @return
	*/
	public String createCacheKey() {
		return "signin." + email;
	}

}

