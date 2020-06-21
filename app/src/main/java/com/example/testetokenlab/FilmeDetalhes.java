package com.example.testetokenlab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class FilmeDetalhes extends AppCompatActivity {

    TextView overview;
    TextView genre;
    TextView title;
    TextView tagLine;
    ImageView poster;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filme_detalhes);

        overview = (TextView) findViewById(R.id.textOverview);
        title = (TextView) findViewById(R.id.textTitle);
        genre = (TextView) findViewById(R.id.textGenre);
        tagLine = (TextView) findViewById(R.id.textTagline);
        poster = (ImageView) findViewById(R.id.imgPoster);

        Intent intent = getIntent();
        String url = intent.getStringExtra(MainActivity.EXTRA_TEXT);

        // get json array
        String jsonString;
        try {
            jsonString = new GetStringFromEndpoint().execute(url).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return;
        }

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String strTitle = null;
        try {
            strTitle = jsonObject.getString("title");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String strOverview = null;
        try {
            strOverview = jsonObject.getString("overview");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String strTagLine = null;
        try {
            strTagLine = jsonObject.getString("tagline");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String strPoster = null;
        Bitmap imgPoster = null;
        try {
            strPoster = jsonObject.getString("poster_url");
            imgPoster = new GetBitmapFromURL().execute(strPoster).get();
            if (imgPoster != null){
                poster.setImageBitmap(imgPoster);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException | ExecutionException e) {
            Log.i("Erro", "Erro");
            e.printStackTrace();
        }


        title.setText(strTitle);
        overview.setText(strOverview);
        tagLine.setText(strTagLine);

    }
}
