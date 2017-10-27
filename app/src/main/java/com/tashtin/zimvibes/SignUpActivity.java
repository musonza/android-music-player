package com.tashtin.zimvibes;

import org.json.JSONException;
import org.json.JSONObject;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.UncachedSpiceService;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tashtin.zimvibes.networking.ResultHandler;
import com.tashtin.zimvibes.networking.tasks.User_SignUpTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SignUpActivity extends Activity {

    EditText usernametext;
    EditText passwordtext;
    EditText confirm_passwordtext;

    ProgressDialog progress;

    private final SpiceManager spiceManager = new SpiceManager(UncachedSpiceService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        usernametext = (EditText) findViewById(R.id.editText1);
        passwordtext = (EditText) findViewById(R.id.editText2);
        confirm_passwordtext = (EditText) findViewById(R.id.editText3);

        Button _signUpBtnSubmit = (Button) findViewById(R.id.button1);

        _signUpBtnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                triggerSignUp(usernametext.getText().toString(),
                        passwordtext.getText().toString(),
                        confirm_passwordtext.getText().toString()
                );
            }
        });

    }

    public void triggerSignUp(String email, String password, String confirm_password) {

        if (password.equals("") || confirm_password.equals("") || email.equals("")) {
            new AlertDialog.Builder(this)
                    .setTitle("Error!")
                    .setMessage("All fields are required")
                    .setNeutralButton("OK", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                        }
                    }).create().show();
        } else if (!password.equals(confirm_password)) {
            new AlertDialog.Builder(this)
                    .setTitle("Error!")
                    .setMessage("Passwords don't match")
                    .setNeutralButton("OK", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                        }
                    }).create().show();
        } else {
            progress = new ProgressDialog(this);
            progress.setMessage("loading");
            progress.show();

            User_SignUpTask request = new User_SignUpTask(email, password, confirm_password);

            getSpiceManager().execute(request, new SignUpRequestListener());
        }

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

    private class SignUpRequestListener implements RequestListener<String> {

        @Override
        public void onRequestFailure(SpiceException e) {
            //Log.d(getClass().getSimpleName(), "onRequestFailure");
        }

        @Override
        public void onRequestSuccess(String result) {

            try {
                JSONObject jObject = new JSONObject(result);
                if (ResultHandler.checkLogStatus(SignUpActivity.this, jObject)) {

                    JSONObject j = jObject.getJSONObject("data");

                    Log.d("access_token", j.getString("access_token"));

                    SharedPreferences prefs = getSharedPreferences("zimvibes_prefs", MODE_PRIVATE);
                    Editor editor = prefs.edit();
                    editor.putString("username", usernametext.getText().toString());
                    editor.putString("access_token", j.getString("access_token"));
                    editor.putString("user_id", j.getString("id"));
                    editor.commit();

                    progress.dismiss();
                    finish();
                }
                progress.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
