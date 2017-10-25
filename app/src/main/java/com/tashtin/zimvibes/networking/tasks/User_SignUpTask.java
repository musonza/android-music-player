package com.tashtin.zimvibes.networking.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.tashtin.zimvibes.networking.API;
import com.tashtin.zimvibes.networking.API_KEY;
import com.tashtin.zimvibes.networking.PostRequest;

public class User_SignUpTask extends PostRequest{

	private String username;
	private String password;
	private String confirm_password;
	
	public User_SignUpTask(String username, String password, String confirm_password) {
		super(String.class);
		
		this.username = username;
		this.password = password;
		this.confirm_password = confirm_password;
	}
	
	
	@Override
	public String loadDataFromNetwork() throws Exception {
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair(API_KEY.EMAIL, username));
		nameValuePairs.add(new BasicNameValuePair(API_KEY.USERNAME, username));
		nameValuePairs.add(new BasicNameValuePair(API_KEY.PASSWORD, password));
		nameValuePairs.add(new BasicNameValuePair(API_KEY.CONFIRM_PASSWORD, confirm_password));
		nameValuePairs.add(new BasicNameValuePair(API_KEY.CLIENT_ID, ""));
		nameValuePairs.add(new BasicNameValuePair(API_KEY.CLIENT_SECRET, ""));
		nameValuePairs.add(new BasicNameValuePair(API_KEY.GRANT_TYPE, "password"));
		
		setApi(API.SIGN_UP);
		setEntity(new UrlEncodedFormEntity(nameValuePairs));
		return super.loadDataFromNetwork();
	}
	
	/**
	* This method generates a unique cache key for this request. In this case
	* our cache key depends just on the keyword.
	* @return
	*/
	public String createCacheKey() {
		return "signin." + username;
	}

}

