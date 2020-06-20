package com.example.testetokenlab;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

    class RetrieveFeedTask extends AsyncTask<String, Void, String>
    {

        private Exception exception;

        private String readStream(InputStream in) throws IOException
        {
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(in),10000);
            for (String line = r.readLine(); line != null; line =r.readLine()){
                sb.append(line);
            }
            in.close();
            return sb.toString();
        }

        public String getRequest(String context)
        {
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
            } catch (IOException e)
            {
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

        protected String doInBackground(String... url)
        {
            String example;
            try {
                example = getRequest("https://desafio-mobile.nyc3.digitaloceanspaces.com/movies");
            } catch (Exception e) {
                this.exception = e;
                example = "";
            }
            return example;

        }
    }


    class Movie()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button botao_principal;
        botao_principal = (Button) findViewById(R.id.buttonTitulo);

        Log.d("MyApp","I am here");
        Log.i("MyApp","I am here2");
        Log.i("MyApp","I am here3");
        String result;
        try {
            result = new RetrieveFeedTask().execute("").get();
            Log.i("OI", result);
        } catch (ExecutionException e) {
            e.printStackTrace();
            Log.i("MyApp","I am here6");
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i("MyApp","I am here5");
            return;
        }
        Log.i("MyApp","I am here4");

        try {
            JSONArray jsonArray = new JSONArray(result);
            String json0 = jsonArray.get(0).toString();
            Log.i("MyApp",json0);
            String title = jsonArray.getJSONObject(0).getString("title");
            botao_principal.setText(title);
            Log.i("Title",title);

        } catch (JSONException e) {
            Log.i("Erro JSON","Inicio erro json");
            e.printStackTrace();
            Log.i("Erro JSON","Fim erro json");
        }


    }
}
