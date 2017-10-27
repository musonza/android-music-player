package com.tashtin.zimvibes;

import org.json.JSONException;
import org.json.JSONObject;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.UncachedSpiceService;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tashtin.zimvibes.networking.ResultHandler;
import com.tashtin.zimvibes.networking.tasks.User_ForgotTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ForgotActivity extends Activity {

    EditText email;
    Button btnSubmit;
    ProgressDialog progress;

    private final SpiceManager spiceManager = new SpiceManager(UncachedSpiceService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot);

        email = (EditText) findViewById(R.id.editText1);
        btnSubmit = (Button) findViewById(R.id.button1);

        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                triggerReset(email.getText().toString());
            }
        });
    }

    public void triggerReset(String email) {

        if (email.equals("")) {
            new AlertDialog.Builder(this)
                    .setTitle("Error!")
                    .setMessage("Email is required")
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

            User_ForgotTask request = new User_ForgotTask(email);

            getSpiceManager().execute(request, new ForgotRequestListener());
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

    private class ForgotRequestListener implements RequestListener<String> {

        @Override
        public void onRequestFailure(SpiceException e) {
            //Log.d(getClass().getSimpleName(), "onRequestFailure");
        }

        @Override
        public void onRequestSuccess(String result) {

            try {
                JSONObject jObject = new JSONObject(result);
                if (ResultHandler.checkLogStatus(ForgotActivity.this, jObject)) {
                    new AlertDialog.Builder(ForgotActivity.this)
                            .setTitle("Notice!")
                            .setMessage("Instructions have been sent to your email")
                            .setNeutralButton("OK", new OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int arg1) {
                                    dialog.dismiss();
                                    finish();
                                }
                            }).create().show();

                }
                progress.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
