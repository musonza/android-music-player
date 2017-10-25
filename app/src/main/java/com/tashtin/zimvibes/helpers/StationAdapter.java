package com.tashtin.zimvibes.helpers;

import java.util.List;

import com.tashtin.zimvibes.GlobalContext;
import com.tashtin.zimvibes.R;
import com.tashtin.zimvibes.models.Station;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StationAdapter extends ArrayAdapter<Station> {

    public ImageLoader imageLoader;

	public StationAdapter(Activity activity, List<Station> stations) {
		super(activity, R.layout.row_station, stations);
        imageLoader = new ImageLoader(activity.getApplicationContext());
	}
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
    	View row = convertView;
    	if(row == null){
    		row = LayoutInflater.from(getContext()).inflate(R.layout.row_station, parent, false);
    	}
    	Station item = getItem(position);
    	TextView title = (TextView) row.findViewById(R.id.stationTitle);
    	title.setText(item.getTitle());

        ImageView img = (ImageView) row.findViewById(R.id.stationImage);

        imageLoader.DisplayImage(item.getImage(), img);
    	
    	if(GlobalContext.getStationIndex() == position)
    	   row.setBackgroundResource(R.color.theme_gray);
    	
    	return row;
    }

}
