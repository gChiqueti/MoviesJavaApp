package com.example.testetokenlab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

        // GET STRING ID AND POSTER BITMAP FROM THE INTENT
        Intent intent = getIntent();
        String url = intent.getStringExtra(MainActivity.EXTRA_TEXT);
        byte[] byteArray = getIntent().getByteArrayExtra(MainActivity.EXTRA_BMP);
        Bitmap imgPoster = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

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
            Request request = new Request.Builder().url(urlBackdrop).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Log.i("ERRO:", "Captura de string da URL falhou");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final Bitmap bmp;
                    if (response.isSuccessful()) {
                        Log.i("SUCESSO:", "Captura de string da URL foi bem sucedida");
                        InputStream is = response.body().byteStream();
                        bmp = BitmapFactory.decodeStream(is);
                    } else {
                        bmp = null;
                    }
                    FilmeDetalhes.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            backdrop.setImageBitmap(bmp);
                        }
                    });
                }
            });
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
        JSONArray strProductedBy = null;
        String producers = "";
        try {
            strProductedBy = jsonObject.getJSONArray("production_companies");
            for (int i = 0; i < strProductedBy.length(); i++)
            {
                String companyName = strProductedBy.getJSONObject(i).getString("name");
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
