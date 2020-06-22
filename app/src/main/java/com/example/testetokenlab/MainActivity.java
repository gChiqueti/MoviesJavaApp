package com.example.testetokenlab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
    public static final String EXTRA_BMP = "com.example.testetokenlab.example.EXTRA_BMP";
    public static final String ENDPOINT_URL = "https://desafio-mobile.nyc3.digitaloceanspaces.com/movies";

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String JSON_STRING = "jsonMainString";

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
                Log.i("ERROOOOO:", "Captura de string da URL falhou");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ViewGroup vg = ((ViewGroup) imgMainLoadingGif.getParent());
                        vg.removeView(imgMainLoadingGif);

                        SharedPreferences sP = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                        String jsonString = sP.getString(JSON_STRING, "");
                        if (jsonString == "")
                        {
                            TextView txt = new TextView(MainActivity.this);
                            txt.setText("Unable to connect to server. Please verify your internet connection and try again");
                            ll.addView(txt, lp);
                        } else {
                            retrieveDataFromString(jsonString);
                        }
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

                            // SAVE JSON STRING FOR OFFLINE USE
                            SharedPreferences sP = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sP.edit();
                            editor.putString(JSON_STRING, jsonString);
                            editor.apply();
                            retrieveDataFromString(jsonString);

                        }
                    });

                }
            }
        });
    }

    ImageView createNewMovieObject(Bitmap poster, String id, String title, String rating)
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
                Bitmap bmp = null;
                String id = (String) v.getTag();
                for(int index = 0; index < ((ViewGroup) v).getChildCount(); index++) {
                    View nextChild = ((ViewGroup) v).getChildAt(index);
                    if (nextChild instanceof ImageView)
                    {
                        bmp = ((BitmapDrawable)((ImageView) nextChild).getDrawable()).getBitmap();
                        break;
                    }
                }
                changeActivity(id, bmp);
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

        ll.addView(parent, lp);
        return imgPoster;
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

        // funcao chamada em segundo plano
        updateAllPosters(jsonArray);
    }

    void updateAllPosters(final JSONArray jsonArray)
    {
        for (int i = 0; i < jsonArray.length(); i++)
        {
            try {
                final String title     = jsonArray.getJSONObject(i).getString("title");
                final String rating    = jsonArray.getJSONObject(i).getString("vote_average");
                final String id        = jsonArray.getJSONObject(i).getString("id");
                final String posterUrl = jsonArray.getJSONObject(i).getString("poster_url");

                ImageView poster = createNewMovieObject(null, id, title, rating);
                Glide.with(this)
                        .load(posterUrl)
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.no_image)
                        .into(poster);

            } catch (Exception e) {
                return;
            }
        }


    }

    void changeActivity(String id, Bitmap bmp) {
        // CREATE NEW INTENT FOR SHARING DATA BETWEEN ACTIVITIES
        Intent intent = new Intent(this, FilmeDetalhes.class);

        // INSERT STRING ID INTO INTENT
        intent.putExtra(EXTRA_TEXT, ENDPOINT_URL.concat("/" + id));

        // COMPRESS BITMAP INTO BYTE ARRAY BEFORE INSERTING INTO INTENT
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        // INSERT BYTE ARRAY INTO INTENT
        intent.putExtra(EXTRA_BMP, byteArray);

        // LOAD ACTIVITY
        startActivity(intent);
    };
}
