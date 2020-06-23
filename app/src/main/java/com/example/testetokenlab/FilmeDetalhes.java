package com.example.testetokenlab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FilmeDetalhes extends AppCompatActivity {

    TextView overview;
    TextView genre;
    TextView title;
    TextView tagLine;
    ImageView poster;
    ImageView backdrop;
    TextView producedBy;

    OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filme_detalhes);

        // GET VIEWS IN THE ACTIVITY
        overview = (TextView) findViewById(R.id.textOverview);
        title = (TextView) findViewById(R.id.textTitle);
        genre = (TextView) findViewById(R.id.textGenre);
        tagLine = (TextView) findViewById(R.id.textTagline);
        poster = (ImageView) findViewById(R.id.imgPoster);
        backdrop = (ImageView) findViewById(R.id.imgBackdrop);
        producedBy = (TextView) findViewById(R.id.textProductedBy);

        // GET STRING ID, CREATES COMPLETE URL WITH IT AND GET IMAGE FROM INTENT
        Intent intent = getIntent();
        final String id = intent.getStringExtra(MainActivity.EXTRA_ID);
        final String url = MainActivity.ENDPOINT_URL.concat("/" + id);
        final byte[] byteArray = getIntent().getByteArrayExtra(MainActivity.EXTRA_BMP);
        final Bitmap imgPoster = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        // PREPARE STRING NAME FOR SAVE CONTENT USING SHARED PREFERENCES
        final String JSON_STRING_WITH_ID = MainActivity.JSON_STRING.concat("_" + id);

        // INSERT POSTER IMAGE INTO THE IMAGEVIEW
        if (imgPoster != null){
            poster.setImageBitmap(imgPoster);
        }

        client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.i("ERRO","FALHA AO EXECUTAR NOVO REQUEST");
                SharedPreferences sP = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
                final String jsonString = sP.getString(JSON_STRING_WITH_ID, "");
                if (jsonString != ""){
                    FilmeDetalhes.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            retrieveDataFromString(jsonString);
                        }
                    });

                } else {
                    title.setText("Could not retrieve information from the server. Please check your internet connection and try again");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful())
                {
                    final String jsonString = response.body().string();

                    // SAVE JSON STRING FOR OFFLINE USE
                    SharedPreferences sP = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sP.edit();
                    editor.putString(JSON_STRING_WITH_ID, jsonString);
                    editor.apply();

                    FilmeDetalhes.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            retrieveDataFromString(jsonString);
                        }
                    });

                }
            }
        });
    }

    // VIEW
    public void retrieveDataFromString(String jsonString)
    {
        // CONVERT STRING INTO JSONOBJECT
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // GET TITLE FROM JSONOBJECT
        String strTitle = null;
        try {
            strTitle = jsonObject.getString("title");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // GET BACKDROP IMAGE BASED ON URL GOTTEN IN JSONOBJECT
        String urlBackdrop = null;
        try {
            urlBackdrop = jsonObject.getString("backdrop_url");
            Glide.with(this)
                    .load(urlBackdrop)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.no_image)
                    .into(backdrop);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // GET OVERVIEW STRING IN JSON OBJECT
        String strOverview = null;
        try {
            strOverview = jsonObject.getString("overview");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // GET TAGLINE STRING IN JSON OBJECT
        String strTagLine = null;
        try {
            strTagLine = jsonObject.getString("tagline");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // GET LIST OF PRODUCTION COMPANIES
        JSONArray sirProducedBy = null;
        String producers = "";
        try {
            sirProducedBy = jsonObject.getJSONArray("production_companies");
            for (int i = 0; i < sirProducedBy.length(); i++)
            {
                String companyName = sirProducedBy.getJSONObject(i).getString("name");
                producers = producers.concat(companyName + ", ");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // REMOVE UNWANTED CHARS AT PRODUCTION COMPANY STRING
        String jsonGenre;
        try {
            jsonGenre = jsonObject.getString("genres");
            jsonGenre = jsonGenre.replace("[","");
            jsonGenre = jsonGenre.replace("]","");
            jsonGenre = jsonGenre.replace("\"","");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }


        title.setText(strTitle);
        overview.setText(strOverview);
        tagLine.setText(strTagLine);
        genre.setText(jsonGenre);
        producedBy.setText("Produced by: ".concat(producers));
    }


}
