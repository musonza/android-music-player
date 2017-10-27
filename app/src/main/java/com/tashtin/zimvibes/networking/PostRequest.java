package com.tashtin.zimvibes.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;

import android.util.Log;

import com.tashtin.zimvibes.GlobalContext;
import com.octo.android.robospice.request.SpiceRequest;

public class PostRequest extends SpiceRequest<String> {

    private String api;
    private UrlEncodedFormEntity entity;

    public PostRequest(Class<String> Class) {
        super(Class);
        // TODO Auto-generated constructor stub
    }

    @Override
    public String loadDataFromNetwork() throws Exception {

        String result = "";
        HttpPost httpPost = new HttpPost(String.format(API.SERVER_URL + "%s", api));
        Log.i(getClass().getSimpleName(), "Request: " + httpPost.getURI());

        try {
            if (entity != null)
                httpPost.setEntity(entity);

            HttpClient httpClient = GlobalContext.getHttpClient();
            HttpResponse response = httpClient.execute(httpPost, GlobalContext.getLocalContext());
            System.out.println(getClass().getSimpleName() + ": " + response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // result = new String(EntityUtils.toString(entity));
                InputStream instream = entity.getContent();
                result = convertStreamToString(instream);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        logInfo(result);
        // httpClient.close();
        return result.trim();
    }

    public void setApi(String api) {
        this.api = api;
    }


    public void setEntity(UrlEncodedFormEntity entity) {
        this.entity = entity;
    }

    private String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append((line + "\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private void logInfo(String str) {
        if (str.length() > 4000) {
            Log.i(getClass().getSimpleName(), "Result: " + str.substring(0, 4000));
            logInfo(str.substring(4000));
        } else
            Log.i(getClass().getSimpleName(), "Result: " + str);
    }

}
