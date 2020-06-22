package com.example.testetokenlab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.LinkAddress;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import pl.droidsonroids.gif.GifImageView;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_TEXT = "com.example.testetokenlab.example.EXTRA_TEXT";
    public static final String ENDPOINT_URL = "https://desafio-mobile.nyc3.digitaloceanspaces.com/movies";

    private int nbrOfPosters;
    private int actualPoster;
    LinearLayout ll;
    LinearLayout.LayoutParams lp;

    OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new OkHttpClient();
        ll = (LinearLayout) findViewById(R.id.layout);
        lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);


        retriveStringFromURL();
    }

    void retriveStringFromURL()
    {
        // START GIF
        final GifImageView imgMainLoadingGif;
        imgMainLoadingGif = new GifImageView(this);
        imgMainLoadingGif.setImageResource(R.drawable.loading);
        ll.addView(imgMainLoadingGif, lp);

        Request request = new Request.Builder().url(ENDPOINT_URL).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.i("ERRO:", "Captura de string da URL falhou");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ViewGroup vg = ((ViewGroup) imgMainLoadingGif.getParent());
                        vg.removeView(imgMainLoadingGif);
                        TextView txt = new TextView(MainActivity.this);
                        txt.setText("Unable to connect to server. Please verify your internet connection and try again");
                        ll.addView(txt, lp);
                    }
                });

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
                            ViewGroup vg = ((ViewGroup) imgMainLoadingGif.getParent());
                            vg.removeView(imgMainLoadingGif);
                            retrieveDataFromString(jsonString);

                        }
                    });

                }
            }
        });
    }

    LinearLayout createNewMovieObject(Bitmap poster, String id, String title, String rating)
    {
        // Estilo
        // - Horizontal Layout
        //    - Imagem do poster
        //    - Vertical layout
        //       - Titulo
        //       - Rating
        float scale = getResources().getDisplayMetrics().density;

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins((int) scale*10, (int)scale*10, (int) scale * 10, (int) scale* 10);

        // LAYOUT HORIZONTAL
        LinearLayout parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.HORIZONTAL);
        parent.setLayoutParams(param);
        parent.setPadding(5, 5, 5, 5);
        parent.setClickable(true);
        parent.setTag(id);
        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = (String) v.getTag();
                changeActivity(id);
            }
        });

        // IMAGEM
        ImageView imgPoster = new ImageView(this);
        imgPoster.setPadding(10, 5, 10, 5);
        if (poster != null) {
            imgPoster.setImageBitmap(poster);
        } else {
            imgPoster.setImageResource(R.drawable.no_image);
        }
        imgPoster.setLayoutParams(new LinearLayout.LayoutParams((int)(100*scale), (int)(150*scale)));
        imgPoster.requestLayout();
        parent.addView(imgPoster);

        // LAYOUT VERTICAL
        LinearLayout layout2 = new LinearLayout(this);
        layout2.setOrientation(LinearLayout.VERTICAL);
        layout2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        parent.addView(layout2);

        // TITULO
        TextView tv1 = new TextView(this);
        tv1.setText(title);
        tv1.setTextSize((int)scale*15);
        tv1.setTextColor(Color.parseColor("#000000"));
        tv1.setPadding(5, 10, 10, 0);
        layout2.addView(tv1);

        // RATING
        TextView tv2 = new TextView(this);
        tv2.setText(rating);
        tv2.setPadding(5, 5, 10, 0);
        tv2.setTextSize((int)scale*10);
        layout2.addView(tv2);

        return parent;
    }

    void retrieveDataFromString(String jsonString)
    {

        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        this.nbrOfPosters = jsonArray.length();
        this.actualPoster = 0;

        // funcao chamada em segundo plano
        updateAllPosters(jsonArray, actualPoster);
    }

    void updateAllPosters(final JSONArray jsonArray, final int posterNbr)
    {
        if (posterNbr >= nbrOfPosters) {
            return;
        }

        try {
            final String title = jsonArray.getJSONObject(posterNbr).getString("title");
            final String rating = jsonArray.getJSONObject(posterNbr).getString("vote_average");
            final String id = jsonArray.getJSONObject(posterNbr).getString("id");
            final String posterUrl = jsonArray.getJSONObject(posterNbr).getString("poster_url");

            Request request = new Request.Builder().url(posterUrl).build();
            client.newCall(request).enqueue(new Callback()
            {
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
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout l = createNewMovieObject(bmp, id, title, rating);
                            ll.addView(l, lp);
                            updateAllPosters(jsonArray, posterNbr + 1);
                        }
                    });

                }
            });


            //LinearLayout l = createNewMovieObject(null, id, title, rating);
            //ll.addView(l, lp);
        } catch (Exception e) {
            return;
        }

    }
    void changeActivity(String id) {
        Intent intent = new Intent(this, FilmeDetalhes.class);
        intent.putExtra(EXTRA_TEXT, ENDPOINT_URL.concat("/" + id));
        startActivity(intent);
    };
}
