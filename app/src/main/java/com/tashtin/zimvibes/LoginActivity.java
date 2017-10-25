package com.tashtin.zimvibes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.UncachedSpiceService;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import com.tashtin.zimvibes.networking.ResultHandler;
import com.tashtin.zimvibes.networking.tasks.User_SignInTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class LoginActivity extends Activity{

	Button _loginBtnSubmit;
	String username;
	String password;
	EditText usernametext;
	EditText passwordtext;

    private CallbackManager callbackManager;
    private LoginButton loginButton;
	
	ProgressDialog progress;

    String facebookEmail;
    String facebookId;
	
	private final SpiceManager spiceManager = new SpiceManager(UncachedSpiceService.class);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.login);

        loginButton = (LoginButton)findViewById(R.id.login_button);

        loginButton.setReadPermissions(Arrays.asList("email"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                // Application code
                                Log.v("LoginActivity", response.toString());
                                Toast m = null;
                                try {
                                    facebookId = object.getString("id");
                                    facebookEmail = object.getString("email");


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                triggerLogin(facebookEmail,facebookId,"1");
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Toast y = Toast.makeText(getBaseContext(), "cancel", Toast.LENGTH_LONG);
                y.show();
            }

            @Override
            public void onError(FacebookException e) {

            }
        });
	
		TextView sign_up = (TextView)findViewById(R.id.sign_up_link);
		TextView forgot = (TextView)findViewById(R.id.forgot_password);
		
		usernametext = (EditText)findViewById(R.id.editText1);
		passwordtext = (EditText)findViewById(R.id.editText2);
		
        _loginBtnSubmit = (Button) findViewById(R.id.button1);
        
        _loginBtnSubmit.setOnClickListener(new View.OnClickListener() {
			
			  @Override
			  public void onClick(View v) {
			        
					triggerLogin(usernametext.getText().toString(), 
								 passwordtext.getText().toString(),
                                 "0"
								 );	
			  }
        });
		
		sign_up.setOnClickListener(new View.OnClickListener() {
						
			  @Override
			  public void onClick(View v) {
					Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
					startActivity(intent);
			  }
        });
		
		forgot.setOnClickListener(new View.OnClickListener() {
			
			  @Override
			  public void onClick(View v) {
					Intent intent = new Intent(getApplicationContext(), ForgotActivity.class);
					startActivity(intent);
			  }
      });
	}
	
	private void triggerLogin(String username, String password, String is_facebook) {
		
		progress = new ProgressDialog(this);
		progress.setMessage("loading");
		progress.show();
		
		User_SignInTask request = new User_SignInTask(username, password, is_facebook);
		
		getSpiceManager().execute(request, new SignInRequestListener());
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
	
	private class SignInRequestListener implements RequestListener<String> {
		
		@Override
		public void onRequestFailure(SpiceException e) {
			//Log.d(getClass().getSimpleName(), "onRequestFailure");
		}	
		
		@Override
		public void onRequestSuccess(String result) {

			try {
				JSONObject jObject = new JSONObject(result);
				if (ResultHandler.checkLogStatus(LoginActivity.this, jObject)) {
					
					JSONObject j = jObject.getJSONObject("data");
					
					SharedPreferences prefs = getSharedPreferences("zimvibes_prefs", MODE_PRIVATE);
					Editor editor = prefs.edit();
					editor.putString("username", usernametext.getText().toString());
					editor.putString("access_token", j.getString("access_token"));
					editor.putString("user_id", j.getString("id"));
					editor.apply();

					JSONArray likesArray = j.getJSONArray("likes");
					GlobalContext.setLikesArray(likesArray);
                    GlobalContext.setSession_expired(false);
					
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					progress.dismiss();
					finish();
				}
				progress.dismiss();
			} catch (JSONException e) {
			e.printStackTrace();
			}
		}
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
	
}
