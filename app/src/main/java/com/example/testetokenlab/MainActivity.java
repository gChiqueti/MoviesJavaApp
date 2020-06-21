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

    int idMainLoadingGif;
    LinearLayout ll;
    LinearLayout.LayoutParams lp;

    OkHttpClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        ll = (LinearLayout) findViewById(R.id.layout);
        lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        GifImageView imgMainLoadingGif;
        imgMainLoadingGif = new GifImageView(this);
        imgMainLoadingGif.setImageResource(R.drawable.loading);
        idMainLoadingGif = View.generateViewId();
        imgMainLoadingGif.setId(idMainLoadingGif);
        ll.addView(imgMainLoadingGif, lp);

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

    LinearLayout createNewMovieObject(Bitmap poster, String id, String title, String rating)
    {
        // Estilo
        // - Horizontal Layout
        //    - Imagem do poster
        //    - Vertical layout
        //       - Titulo
        //       - Rating

        LinearLayout parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.HORIZONTAL);
        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                             LinearLayout.LayoutParams.WRAP_CONTENT));

        ImageView imgPoster = new ImageView(this);
        imgPoster.setPadding(10, 5, 10, 5);

        if (poster != null) {
            imgPoster.setImageBitmap(poster);
        } else {
            imgPoster.setImageResource(R.drawable.no_image);
        }
        float scale = getResources().getDisplayMetrics().density;
        imgPoster.setLayoutParams(new LinearLayout.LayoutParams((int) (100*scale), (int) (150*scale)));
        imgPoster.requestLayout();
        imgPoster.setClickable(true);
        imgPoster.setTag(id);
        imgPoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = (String) v.getTag();
                changeActivity(id);
            }
        });
        parent.addView(imgPoster);

        LinearLayout layout2 = new LinearLayout(this);
        layout2.setOrientation(LinearLayout.VERTICAL);
        layout2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                              LinearLayout.LayoutParams.WRAP_CONTENT));
        parent.addView(layout2);

//children of layout2 LinearLayout

        TextView tv1 = new TextView(this);
        tv1.setText(title);
        tv1.setTextSize((int)scale*15);
        tv1.setTextColor(Color.parseColor("#000000"));
        tv1.setPadding(5, 10, 10, 0);
        layout2.addView(tv1);

        TextView tv2 = new TextView(this);
        tv2.setText(rating);
        tv2.setPadding(5, 5, 10, 0);
        layout2.addView(tv2);

        return parent;
    }

    void retrieveDataFromString(String jsonString)
    {


        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String title = jsonArray.getJSONObject(i).getString("title");
                String rating = jsonArray.getJSONObject(i).getString("vote_average");
                String url = jsonArray.getJSONObject(i).getString("poster_url");
                String id = jsonArray.getJSONObject(i).getString("id");
                Bitmap bitmap = new GetBitmapFromURL().execute(url).get();
                LinearLayout l = createNewMovieObject(bitmap, id, title, rating);
                ll.addView(l, lp);
            } catch (Exception e) {}

        }

        View loadingView = this.findViewById(idMainLoadingGif);
        ViewGroup vg = ((ViewGroup) loadingView.getParent());
        vg.removeView(loadingView);

    }

    void changeActivity(String id)
    {
        Intent intent = new Intent(this, FilmeDetalhes.class);
        intent.putExtra(EXTRA_TEXT, ENDPOINT_URL.concat("/" + id));
        startActivity(intent);
    };
}
