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

import java.io.IOException;
import java.util.concurrent.ExecutionException;

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

        OkHttpClient client;
        client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.i("ERRO:", "Captura de string da URL falhou");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful())
                {
                    Log.i("SUCESSO:", "Captura de string da URL foi bem sucedida");
                    final String jsonString = response.body().string();
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

    public void retrieveDataFromString(String jsonString)
    {
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
