package com.tashtin.zimvibes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.UncachedSpiceService;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import com.tashtin.zimvibes.R;
import com.tashtin.zimvibes.helpers.DownloadImageTask;
import com.tashtin.zimvibes.models.StationSong;
import com.tashtin.zimvibes.networking.ResultHandler;
import com.tashtin.zimvibes.networking.tasks.LikeTrackTask;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.tashtin.zimvibes.helpers.PlayerService;

public class StreamingMp3Player extends Activity implements OnClickListener, OnTouchListener, OnCompletionListener, OnBufferingUpdateListener{
	
	private InterstitialAd interstitial;
	private InterstitialAd interstitialZimVibes;
	
	private final SpiceManager spiceManager = new SpiceManager(UncachedSpiceService.class);
	
	private ImageButton buttonPlayPause;
    private ImageButton likeButton;
    private ImageButton dislikeButton;
    private SeekBar seekBarProgress;
	public EditText editTextSongURL;
	
	private MediaPlayer mediaPlayer;
	private int mediaFileLengthInMilliseconds; // this value contains the song duration in milliseconds. Look at getDuration() method in MediaPlayer class
	
	private final Handler handler = new Handler();
	private final String CLIENT_ID = "client-id-here";
	private String stream_url, access_token;
	private int count = 0;
	
	String[] station_songs;
	
	String[] streams;
	String station_id;
	
	public int current_song_index;
	
	SharedPreferences prefs;
	Editor editor;
	
	List<StationSong> songs = new ArrayList<StationSong>();
	
	ProgressDialog progress;
	
	ImageView thumbnail;
	TextView songTitle, uploadedBy, tx1, tx2;
	
	private double startTime = 0;
	private double finalTime = 0;
	private Handler myHandler = new Handler();

    private JSONArray likesArray = null;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		// Prepare the Interstitial Ad
		interstitial = new InterstitialAd(StreamingMp3Player.this);
		interstitial.setAdUnitId("ca-app-pub-xxxxxxxxxxxxxxxxxx");
		
		//Locate the Banner Ad in activity_main.xml
		AdView adView = (AdView) this.findViewById(R.id.adView);
		
		// Request for Ads
		AdRequest adRequest = new AdRequest.Builder().build();

		// Load ads into Banner Ads
		adView.loadAd(adRequest);
		
		// Load ads into Interstitial Ads
		interstitial.loadAd(adRequest);
		
		// Prepare an Interstitial Ad Listener
		interstitial.setAdListener(new AdListener() {
			public void onAdLoaded() {
				// Call displayInterstitial() function
				displayInterstitial();
			}
		});
        
		if(GlobalContext.getHttpClient() == null || GlobalContext.getLocalContext() == null) {
			GlobalContext.configureHttpService();
		}

        likesArray = GlobalContext.getLikesArray();
		
		prefs = getSharedPreferences("zimvibes_prefs", MODE_PRIVATE);
		editor = prefs.edit();
		
		if(Long.valueOf(getIntent().getExtras().getString("station_id")) != prefs.getLong("station_id", -1)){
			setupStreams(getIntent().getExtras().getString("station_songs_json"));
		}
		
        initView();
        		
		editor.putLong("current_song_index", GlobalContext.getCurrentSongIndex());
		
		if(Long.valueOf(getIntent().getExtras().getString("station_id")) != prefs.getLong("station_id", -1)){
			GlobalContext.setSongs(songs);
			editor.putLong("station_id", Long.valueOf(getIntent().getExtras().getString("station_id")));
        	
			//stop any playing track
			if(mediaPlayer.isPlaying()){
				mediaPlayer.stop();
			}
			playSong(stream_url, songs.get(0));
			
		}else{
			//this is the same station
			
			if(GlobalContext.getSongs() == null){
				GlobalContext.setSongs(songs);
			}
			
			songs = GlobalContext.getSongs();
			
			if(songs.size() == 0){
				
				editor.putLong("station_id", 0);
				editor.commit();
				GlobalContext.setCurrentSongIndex(0);
				
				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(intent); 
				finish();
			}
			
			if(!mediaPlayer.isPlaying()){
				playSong(stream_url, songs.get(GlobalContext.getCurrentSongIndex()));
			}else{
	            finalTime = mediaPlayer.getDuration();
	            mediaFileLengthInMilliseconds = mediaPlayer.getDuration();
	            
	            startTime = mediaPlayer.getCurrentPosition();
				myHandler.postDelayed(UpdateSongTime,100);
	            tx2.setText(String.format("%d : %02d ",
	                    TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
	                    TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
	                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
	                    );
	            primarySeekBarProgressUpdater();
			}
			
			if(GlobalContext.getCurrentSongIndex() == 0){
				GlobalContext.setCurrentSongIndex(current_song_index);
			}
			
			StationSong song = songs.get(GlobalContext.getCurrentSongIndex());
			songTitle.setText(song.getTitle());
			uploadedBy.setText("uploader: " + song.getUploadedBy());
			buttonPlayPause.setImageResource(R.drawable.pauseicon);
			setTrackImage(song.getArtworkUrl());
			
		}
		
		editor.commit();
    }
    
    public void setupStreams(String streams_jsonArray){
    	
		//keeps track of the station song playing
		current_song_index = 0;
		
        try {
            JSONArray jArray = new JSONArray(streams_jsonArray);
            
            streams = new String[jArray.length()];
            
    		for (int i = 0 ; i < jArray.length(); i++) {
    			JSONObject j = jArray.getJSONObject(i);
    			StationSong song = new StationSong();
    			song.setTitle(j.getString("title"));
    			song.setId(j.getString("id"));
    			song.setArtworkUrl(j.getString("artwork_url"));
    			song.setLiked(j.getString("liked"));
				song.setDisliked("0");
    			song.setUploadedBy(j.getString("added_by"));
    			song.setUrl(j.getString("stream_url"));
    			songs.add(song);
    			streams[i] = j.getString("stream_url") + "?client_id=" + CLIENT_ID;
    		}
    		
        } catch (JSONException e) {
            e.printStackTrace();
        }
		
        GlobalContext.setSongs(songs);
		stream_url = streams[0];
		GlobalContext.setCurrentSongIndex(0);
    }

    public void displayInterstitial() {
		// If Ads are loaded, show Interstitial else show nothing.
		if (interstitial.isLoaded()) {
			if(GlobalContext.getSkipCount() > 5){
				interstitial.show();
				GlobalContext.setSkipCount(0);
			}
		}
	}

    /** This method initialise all the views in project*/
    private void initView() {
		buttonPlayPause = (ImageButton)findViewById(R.id.ButtonTestPlayPause);
		buttonPlayPause.setOnClickListener(this);

        ImageButton nextButton = (ImageButton) findViewById(R.id.nextButton);
		nextButton.setOnClickListener(this);
		
		likeButton = (ImageButton)findViewById(R.id.likeButton);
		likeButton.setOnClickListener(this);

		dislikeButton = (ImageButton)findViewById(R.id.dislikeButton);
		dislikeButton.setOnClickListener(this);

        ImageButton downloadButton = (ImageButton) findViewById(R.id.DownloadButton);
        downloadButton.setOnClickListener(this);
		
		seekBarProgress = (SeekBar)findViewById(R.id.SeekBarTestPlay);	
		seekBarProgress.setMax(99); // It means 100% .0-99
		seekBarProgress.setOnTouchListener(this);
		
		//thumbnail = (ImageView)findViewById(R.id.imageView1);
		songTitle = (TextView)findViewById(R.id.textSongTitle);
		uploadedBy = (TextView)findViewById(R.id.textUploadedBy);
		
		tx1 = (TextView)findViewById(R.id.duration1);
		tx2 = (TextView)findViewById(R.id.duration2);
		
		
		if(GlobalContext.getMediaPlayerObj() == null){
			GlobalContext.configurePlayer();
		}
		
		mediaPlayer = GlobalContext.getMediaPlayerObj();
		mediaPlayer.setOnBufferingUpdateListener(this);
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setScreenOnWhilePlaying(true);
		
		setButtonFilter(nextButton);
		setButtonFilter(likeButton);
        setButtonFilter(dislikeButton);
		setButtonFilter(buttonPlayPause);
	}
    
    private void setButtonFilter(final ImageView btn){
		btn.setOnTouchListener(new View.OnTouchListener() {
            private Rect rect;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    btn.setColorFilter(Color.argb(50, 0, 0, 0));
                    rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    btn.setColorFilter(Color.argb(0, 0, 0, 0));
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        btn.setColorFilter(Color.argb(0, 0, 0, 0));
                    }
                }
                return false;
            }
        });
    }

	/** Method which updates the SeekBar primary progress by current song playing position*/
    private void primarySeekBarProgressUpdater() {
		
    	seekBarProgress.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100)); // This math construction give a percentage of "was playing"/"song length"
		if (mediaPlayer.isPlaying()) {
			Runnable notification = new Runnable() {
		        public void run() {
		        	primarySeekBarProgressUpdater();
				}
		    };
		    handler.postDelayed(notification,100);
    	}
    }

	private void startForegroundService(){
        Intent i=new Intent(this, PlayerService.class);

        i.putExtra(PlayerService.EXTRA_PLAYLIST, "main");
        i.putExtra(PlayerService.EXTRA_SHUFFLE, true);

        startService(i);
	}
    
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
           startTime = mediaPlayer.getCurrentPosition();

           tx1.setText(String.format("%d : %02d ",
           
           TimeUnit.MILLISECONDS.toMinutes((long) startTime),
           TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
           TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
           toMinutes((long) startTime)))
           );

           myHandler.postDelayed(this, 100);
        }
     };
     
    public void playSong(String stream, StationSong song){

		stream_url = stream;

		try {
			mediaPlayer.setDataSource(stream);
			mediaPlayer.prepare();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			songTitle.setText(song.getTitle());
	        setTrackImage(song.getArtworkUrl());
		} catch (Exception e) {
			// TODO: handle exception
		}

		try {
			uploadedBy.setText("uploader: " + song.getUploadedBy());
		} catch (Exception e) {
			// TODO: handle exception
		}

		
		mediaFileLengthInMilliseconds = mediaPlayer.getDuration(); // gets the song length in milliseconds from URL
		
		if(!mediaPlayer.isPlaying()){
			mediaPlayer.start();
			buttonPlayPause.setImageResource(R.drawable.pauseicon);
            finalTime = mediaPlayer.getDuration();
            startTime = mediaPlayer.getCurrentPosition();
            tx2.setText(String.format("%d : %02d ",
            TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
            TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
            );

            tx1.setText(String.format("%d : %02d ",
                            TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
            );

            myHandler.postDelayed(UpdateSongTime, 100);

            startForegroundService();
            //stopService(new Intent(this, PlayerService.class));
		}else {
			mediaPlayer.pause();
			buttonPlayPause.setImageResource(R.drawable.button_play);
		}
		
		primarySeekBarProgressUpdater();

        //jsonArray.toString().contains("\"username\":\""+usernameToFind+"\"");

		if(song.getLiked().equals("1") || isLiked(song.getId())){
			((ImageButton) likeButton).setImageResource(R.drawable.thumbedup);
            ((ImageButton) dislikeButton).setImageResource(R.drawable.thumbsdown);
		}else{
			((ImageButton) likeButton).setImageResource(R.drawable.thumbsup);
            ((ImageButton) dislikeButton).setImageResource(R.drawable.thumbsdown);
		}

        if(song.getDisliked().equals("1")){
            ((ImageButton) likeButton).setImageResource(R.drawable.thumbsup);
            ((ImageButton) dislikeButton).setImageResource(R.drawable.thumbeddown);
        }else{
            ((ImageButton) likeButton).setImageResource(R.drawable.thumbsup);
            ((ImageButton) dislikeButton).setImageResource(R.drawable.thumbsdown);
        }
    }

    private boolean isLiked(String song_id)
    {
        if(likesArray != null)
            return likesArray.toString().contains(song_id);
        else
            return false;
    }
    
	@Override
	public void onClick(View v) {

        if(v.getId() == R.id.DownloadButton){
            String url = "http://farm1.static.flickr.com/114/298125983_0e4bf66782_b.jpg";
            new DownloadFileAsync().execute(stream_url);
        }

		if(v.getId() == R.id.ButtonTestPlayPause){
			 /* ImageButton onClick event handler. Method which start/pause mediaplayer playing */
			try {
				mediaPlayer.setDataSource(stream_url);
				mediaPlayer.prepare();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				mediaFileLengthInMilliseconds = mediaPlayer.getDuration(); // gets the song length in milliseconds from URL
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			
			if(!mediaPlayer.isPlaying()){
				mediaPlayer.start();
				buttonPlayPause.setImageResource(R.drawable.pauseicon);
			}else {
				mediaPlayer.pause();
				buttonPlayPause.setImageResource(R.drawable.playicon);
			}
			
			primarySeekBarProgressUpdater();
		}
		
		if(v.getId() == R.id.nextButton){

			if(mediaPlayer.isPlaying())
				mediaPlayer.stop();
			
			int k = GlobalContext.getCurrentSongIndex();
		    //k = k + 1;
			
			if(songs.size() > k){
				playSong(stream_url, songs.get(k)); 
				GlobalContext.setCurrentSongIndex(k);
				current_song_index = GlobalContext.getCurrentSongIndex();
				GlobalContext.setSkipCount(GlobalContext.getSkipCount() + 1);
			}else{
				editor.putLong("station_id", 0);
				editor.commit();
				GlobalContext.setCurrentSongIndex(0);
				
				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(intent); 
				finish();
			}
			
			
			buttonPlayPause.setImageResource(R.drawable.playicon);
		}

		if(v.getId() == R.id.dislikeButton){

			if(!isLoggedIn()){
				getLogin();
			}else{
				StationSong s = songs.get(GlobalContext.getCurrentSongIndex());
				LikeTrackTask request;

				if(s.getDisliked().equals("1")){
					((ImageButton) likeButton).setImageResource(R.drawable.thumbsup);
					((ImageButton) dislikeButton).setImageResource(R.drawable.thumbsdown);
					s.setDisliked("0");
					s.setLiked("0");
					request = new LikeTrackTask(s.getId(), access_token, s.getTitle(), false);

				}else{
					((ImageButton) likeButton).setImageResource(R.drawable.thumbsup);
					((ImageButton) dislikeButton).setImageResource(R.drawable.thumbeddown);
					s.setDisliked("1");
					s.setLiked("0");
					request = new LikeTrackTask(s.getId(), access_token, s.getTitle(), false);
				}

                getSpiceManager().execute(request, new LikeTrackRequestListener());
			}
		}
		
		if(v.getId() == R.id.likeButton){

	        if(!isLoggedIn()){
				getLogin();
	        }else{
				StationSong s = songs.get(GlobalContext.getCurrentSongIndex());
				LikeTrackTask request;
				
				if(s.getLiked().equals("1")){
					((ImageButton) likeButton).setImageResource(R.drawable.thumbsup);
					((ImageButton) dislikeButton).setImageResource(R.drawable.thumbsdown);
					s.setLiked("0");
					s.setDisliked("0");
					request = new LikeTrackTask(s.getId(), access_token, s.getTitle(), true);
										
				}else{
					((ImageButton) likeButton).setImageResource(R.drawable.thumbedup);
					((ImageButton) dislikeButton).setImageResource(R.drawable.thumbsdown);
					s.setLiked("1");
					s.setDisliked("0");
					request = new LikeTrackTask(s.getId(), access_token, s.getTitle(), true);
				}
				
				getSpiceManager().execute(request, new LikeTrackRequestListener());
	        }
		}
	}
	
	public void getLogin()
	{
		Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
		startActivityForResult(intent, 1);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(v.getId() == R.id.SeekBarTestPlay){
			/* Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/
			if(mediaPlayer.isPlaying()){
		    	SeekBar sb = (SeekBar)v;
				int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
				mediaPlayer.seekTo(playPositionInMillisecconds);
			}
		}
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		 /* MediaPlayer onCompletion event handler. Method which calls then song playing is complete*/
		count++;
		++current_song_index;
		GlobalContext.setCurrentSongIndex(GlobalContext.getCurrentSongIndex() + 1);
		
		if(GlobalContext.getCurrentSongIndex() >= songs.size()){
			buttonPlayPause.setImageResource(R.drawable.playicon);
			editor.putLong("station_id", 0);
			editor.commit();

			GlobalContext.setCurrentSongIndex(0);
			
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			startActivity(intent); 
			finish();
		}else{
			mp.reset();
			StationSong song = songs.get(GlobalContext.getCurrentSongIndex());
			playSong( song.getUrl() + "?client_id=" + CLIENT_ID, song);
            GlobalContext.setSkipCount(GlobalContext.getSkipCount() + 1);
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		/* Method which updates the SeekBar secondary progress by current song loading from URL position*/
		seekBarProgress.setSecondaryProgress(percent);
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

	private class LikeTrackRequestListener implements RequestListener<String> {
		
		@Override
		public void onRequestFailure(SpiceException e) {
			//Log.d(getClass().getSimpleName(), "onRequestFailure");
		}	
		
		@Override
		public void onRequestSuccess(String result) {

			try {
				JSONObject jObject = new JSONObject(result);
				if (ResultHandler.checkLogStatus(StreamingMp3Player.this, jObject)) {
					
				}

			} catch (JSONException e) {
			e.printStackTrace();
			}

            if(GlobalContext.getSessionExpired()){
                clearSessionInfo();
            }
		}
	}

    public void clearSessionInfo(){
        editor.putString("username", null);
        editor.putString("access_token", null);
        editor.putString("user_id", null);
        editor.commit();

        getLogin();
    }
	 
	 public void setTrackImage(String url){ 
		 if(url != null){
			 new DownloadImageTask((ImageView) findViewById(R.id.imageView1)).execute(url);
		 }
	 }

	public boolean isLoggedIn(){
		SharedPreferences prefs = getSharedPreferences("zimvibes_prefs", MODE_PRIVATE);
		String savedUsername = prefs.getString("username", null);

		access_token = prefs.getString("access_token", null);

        return savedUsername != null;
    }

    @Override
    protected ProgressDialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading file..");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;

    class DownloadFileAsync extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;
            try {
                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                int lenghtOfFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);
                InputStream input = new BufferedInputStream(url.openStream());

                File cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"MUSIC/Zimvibes");

                if(!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }

                StationSong s = songs.get(GlobalContext.getCurrentSongIndex());
                OutputStream output = new FileOutputStream(cacheDir + "/"+s.getTitle()+".mp3");
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {}
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC", progress[0]);
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
        }
    }
}

