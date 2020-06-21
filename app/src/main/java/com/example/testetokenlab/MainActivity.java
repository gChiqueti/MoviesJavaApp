package com.example.testetokenlab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_TEXT = "com.example.testetokenlab.example.EXTRA_TEXT";
    public static final String ENDPOINT_URL = "https://desafio-mobile.nyc3.digitaloceanspaces.com/movies";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        OkHttpClient client;
        client = new OkHttpClient();
        Request request = new Request.Builder().url(ENDPOINT_URL).build();

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
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            retrieveDataFromString(jsonString);
                        }
                    });

                }
            }
        });
    }

    void retrieveDataFromString(String jsonString)
    {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        LinearLayout ll = (LinearLayout) findViewById(R.id.layout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String title = jsonArray.getJSONObject(i).getString("title");
                Button myButton = new Button(this);
                myButton.setText(title);
                ll.addView(myButton, lp);

            }catch (JSONException e) {}

            try {
                String url = jsonArray.getJSONObject(i).getString("poster_url");
                String id = jsonArray.getJSONObject(i).getString("id");
                Log.i("URL", url);
                Bitmap bitmap = new GetBitmapFromURL().execute(url).get();
                if (bitmap != null) {
                    ImageView v = new ImageView(this);
                    v.setImageBitmap(bitmap);
                    v.setTag(id);
                    v.setClickable(true);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String id = (String) view.getTag();
                            changeActivity(id);
                        }
                    });
                    ll.addView(v, lp);
                }
            } catch (Exception e) {}

        }  // endfor

    }

    void changeActivity(String id)
    {
        Intent intent = new Intent(this, FilmeDetalhes.class);
        intent.putExtra(EXTRA_TEXT, ENDPOINT_URL.concat("/" + id));
        startActivity(intent);
    };
}
