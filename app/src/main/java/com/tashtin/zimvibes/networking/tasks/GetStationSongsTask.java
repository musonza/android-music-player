package com.tashtin.zimvibes.networking.tasks;

import com.tashtin.zimvibes.networking.API;
import com.tashtin.zimvibes.networking.GetRequest;


public class GetStationSongsTask  extends GetRequest {
	
	public String access_token;
	public String station_id;
	public Boolean isAuth;
	
	public GetStationSongsTask(String station_id, String access_token, Boolean isAuth) {
		super(String.class);
		this.access_token = access_token;
		this.station_id = station_id;
		this.isAuth = isAuth;
	}
	
	
	@Override
	public String loadDataFromNetwork() throws Exception {

		if(isAuth){
			setApi(API.GET_AUTH_STATION_SONGS + "?station_id=" + station_id
					+ "&page=1&limit=50&q=test&access_token=" + access_token);
		}else{
			setApi(API.GET_STATION_SONGS + "?station_id=" + station_id
					+ "&page=1&limit=50&q=test");
		}
		
		return super.loadDataFromNetwork();
	}

}
