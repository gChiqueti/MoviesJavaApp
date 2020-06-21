package com.example.testetokenlab;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetStringFromEndpoint extends AsyncTask<String, Void, String> {

    private String readStream(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in),10000);
        for (String line = r.readLine(); line != null; line =r.readLine()){
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }

    private String getRequest(String context) {
        URL url;
        String result;
        // Create URL
        try{
            url = new URL(context);
        }catch(MalformedURLException ex){
            return "";
        }

        // Opens the connection
        HttpURLConnection urlConnection;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            result = readStream(in);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            urlConnection.disconnect();
        }
        return result;
    }

    protected String doInBackground(String... url) {
        String example;
        try {
            example = getRequest(url[0]);
        } catch (Exception e) {
            example = "";
        }
        return example;
    }
}