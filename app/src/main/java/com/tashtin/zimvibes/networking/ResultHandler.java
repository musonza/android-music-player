package com.tashtin.zimvibes.networking;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.tashtin.zimvibes.GlobalContext;

public class ResultHandler {

    public static boolean checkStatus(Context mContext, JSONObject jObject) {
        showErrorMessage(mContext, "test");
        try {
            if (jObject.getString(API_KEY.STATUS).equals("success"))
                return true;
            else if (jObject.getString(API_KEY.STATUS).equals("fail")) {
                showErrorMessage(mContext, jObject.getString(API_KEY.MESSAGE));
                //Log.d(API_KEY.ERROR, jObject.getString(API_KEY.ERROR));
                return false;
            }

            return false;
        } catch (JSONException e) {
            return false;
        }
    }

    public static boolean checkLogStatus(Context mContext, JSONObject jObject) {

        try {
            if (jObject.getString(API_KEY.STATUS).equals("success"))
                return true;
            else if (jObject.getString(API_KEY.STATUS).equals("fail")) {
                try {
                    showErrorMessage(mContext, jObject.getString(API_KEY.MESSAGE));
                    return false;
                } catch (JSONException e) {
                    showErrorMessage(mContext, jObject.getString(API_KEY.MESSAGE));
                    return false;
                }
            }
            return false;
        } catch (JSONException e) {

            try {
                if (jObject.getString("error").equals("invalid_credentials")) {
                    showErrorMessage(mContext, jObject.getString("error_description"));
                }
                if (jObject.getString("error").equals("access_denied")) {
                    showErrorMessage(mContext, "Your session has expired. Please login again");
                    GlobalContext.setSession_expired(true);
                }
            } catch (JSONException ex) {

            }
            return false;
        }
    }

    private static void showErrorMessage(Context mContext, String error) {
        new AlertDialog.Builder(mContext)
                .setTitle("Error!")
                .setMessage(error.replace("[", "").replace("]", "").replace("\"", ""))
                .setNeutralButton("OK", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                }).create().show();

    }
}