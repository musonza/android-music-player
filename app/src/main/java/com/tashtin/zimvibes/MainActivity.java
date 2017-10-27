package com.tashtin.zimvibes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.login.LoginManager;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.UncachedSpiceService;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import com.tashtin.zimvibes.helpers.StationAdapter;
import com.tashtin.zimvibes.models.Station;
import com.tashtin.zimvibes.models.StationSong;
import com.tashtin.zimvibes.networking.ResultHandler;
import com.tashtin.zimvibes.networking.tasks.GetStationSongsTask;
import com.tashtin.zimvibes.networking.tasks.GetStationsTask;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity {

    Menu menu;

    private final SpiceManager spiceManager = new SpiceManager(UncachedSpiceService.class);

    private ArrayAdapter<?> stationsArrayAdapter;
    List<Station> stations = new ArrayList<Station>();

    ProgressDialog progress;

    String[] station_songs;

    List<StationSong> songs = new ArrayList<StationSong>();

    SharedPreferences prefs;

    Editor editor;

    long current_song_index = 0;

    String access_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (GlobalContext.getHttpClient() == null || GlobalContext.getLocalContext() == null) {
            GlobalContext.configureHttpService();
        }

        createStations();

        prefs = getSharedPreferences("zimvibes_prefs", MODE_PRIVATE);
        editor = prefs.edit();
        current_song_index = prefs.getLong("current_song_index", 0);

        if (android.os.Build.VERSION.SDK_INT >= 11) {
            invalidateOptionsMenu();
        }
    }

    /**
     * This method will be called when the activity is created.
     * It will get a list of music / radio stations
     */
    public void createStations() {
        progress = new ProgressDialog(this);
        progress.setMessage("loading stations...");
        progress.show();

        String accesstoken = "";

        GetStationsTask request = new GetStationsTask(accesstoken);
        getSpiceManager().execute(request, new GetStationsRequestListener());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                menu.findItem(R.id.action_login).setVisible(false);
                menu.findItem(R.id.action_logout).setVisible(true);
            }
        }
    }

    /**
     * Renders a list of the radio stations in a ListView
     *
     * @param stations List of stations to display
     */
    public void renderStations(List<Station> stations) {
        stationsArrayAdapter = new StationAdapter(this, stations);
        setListAdapter(stationsArrayAdapter);
    }

    /**
     * This method will be called when an item in the list is of stations selected.
     *
     * @param l        The ListView where the click happened
     * @param v        The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ListAdapter list = getListAdapter();
        Station station = (Station) list.getItem(position);

        if (station.getFavorite().equals("1") && !isLoggedIn()) {

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Notice!")
                    .setMessage("You have to login to play your liked songs")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                            getLogin();
                        }
                    }).create().show();

            return;
        }

        try {
            l.getChildAt(GlobalContext.getStationIndex()).setBackgroundColor(Color.argb(0, 0, 0, 0));
        } catch (Exception e) {

        }

        l.getChildAt(position).setBackgroundColor(Color.GRAY);

        //check if same station
        //go to activity
        if (Long.valueOf(station.getId()) == prefs.getLong("station_id", 0) &&
                GlobalContext.getCurrentSongIndex() < GlobalContext.getSongs().size() - 1) {
            Intent intent = new Intent(getApplicationContext(), StreamingMp3Player.class);
            intent.putExtra("station_id", station.getId());
            startActivity(intent);
        } else {
            //get songs
            GlobalContext.setCurrentSongIndex(0);

            progress = new ProgressDialog(this);
            progress.setMessage("loading songs...");
            progress.show();

            editor.putLong("station_id", 0);
            editor.commit();

            Boolean isAuth;

            isAuth = station.getFavorite().equals("1");

            GetStationSongsTask request = new GetStationSongsTask(station.getId(), access_token, isAuth);
            getSpiceManager().execute(request, new GetStationSongsRequestListener(station.getId(), position));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    public SpiceManager getSpiceManager() {
        return spiceManager;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences prefs = getSharedPreferences("zimvibes_prefs", MODE_PRIVATE);
        String savedUsername = prefs.getString("username", null);

        if (savedUsername == null) {
            this.menu.findItem(R.id.action_login).setVisible(true);
            this.menu.findItem(R.id.action_logout).setVisible(false);
        } else {
            this.menu.findItem(R.id.action_login).setVisible(false);
            this.menu.findItem(R.id.action_logout).setVisible(true);
        }
        return true;
    }

    /**
     * Checks if logged in in local storage
     *
     * @return Boolean
     */
    public boolean isLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("zimvibes_prefs", MODE_PRIVATE);
        String savedUsername = prefs.getString("username", null);
        access_token = prefs.getString("access_token", null);

        return savedUsername != null;
    }

    /**
     * Clears the local storage session info for the user
     */
    public void clearSessionInfo() {
        editor.putString("username", null);
        editor.putString("access_token", null);
        editor.putString("user_id", null);
        editor.commit();

        try {
            LoginManager.getInstance().logOut();
        } catch (Exception e) {

        }

        menu.findItem(R.id.action_login).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            editor.putString("username", null);
            editor.putString("access_token", null);
            editor.putString("user_id", null);
            editor.commit();

            item.setVisible(false);
            menu.findItem(R.id.action_login).setVisible(true);

            return true;
        }

        if (id == R.id.action_login) {

            if (isLoggedIn()) {
                return true;
            }
            getLogin();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getLogin() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(intent, 1);
    }

    private class GetStationSongsRequestListener implements RequestListener<String> {

        private String station_id;
        int position;

        public GetStationSongsRequestListener(String station_id, int position) {
            this.station_id = station_id;
            this.position = position;
        }

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.d(getClass().getSimpleName(), "onRequestFailure");
        }

        @Override
        public void onRequestSuccess(String result) {

            try {
                JSONObject jObject = new JSONObject(result);
                if (ResultHandler.checkLogStatus(MainActivity.this, jObject)) {

                    //songs object
                    JSONArray jArray = jObject.getJSONArray("data");

                    station_songs = new String[jArray.length()];

                    MainActivity.this.progress.dismiss();

                    GlobalContext.setStationIndex(position);

                    //go to activity
                    Intent intent = new Intent(getApplicationContext(), StreamingMp3Player.class);
                    intent.putExtra("station_songs_json", jArray.toString());
                    intent.putExtra("station_id", station_id);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            MainActivity.this.progress.dismiss();

            if (GlobalContext.getSessionExpired()) {
                clearSessionInfo();
            }

        }

    }

    private class GetStationsRequestListener implements RequestListener<String> {

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.d(getClass().getSimpleName(), "onRequestFailure");
        }

        @Override
        public void onRequestSuccess(String result) {

            try {
                JSONObject jObject = new JSONObject(result);
                if (ResultHandler.checkLogStatus(MainActivity.this, jObject)) {

                    JSONArray jArray = jObject.getJSONArray("data");

                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject j = jArray.getJSONObject(i);
                        Station station = new Station();
                        station.setId(j.getString("id"));
                        station.setTitle(j.getString("name"));
                        station.setFavorite(j.getString("is_favorite"));
                        station.setImage(j.getString("image"));
                        stations.add(station);
                    }

                    renderStations(stations);
                    MainActivity.this.progress.dismiss();
                }
                MainActivity.this.progress.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (GlobalContext.getSessionExpired()) {
                clearSessionInfo();
            }
        }

    }
}
