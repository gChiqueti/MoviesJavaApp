package com.example.testetokenlab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.droidsonroids.gif.GifImageView;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "com.example.testetokenlab.example.EXTRA_ID";
    public static final String EXTRA_BMP = "com.example.testetokenlab.example.EXTRA_BMP";
    public static final String ENDPOINT_URL = "https://desafio-mobile.nyc3.digitaloceanspaces.com/movies";

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String JSON_STRING = "jsonMainString";

    LinearLayout ll;
    LinearLayout.LayoutParams lp;
    OkHttpClient client;

    private GifImageView imgMainLoadingGif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new OkHttpClient();
        ll = (LinearLayout) findViewById(R.id.layout);
        lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        retrieveStringFromURL();
    }

    private GifImageView addImageAsGIFToView(int resourceID)
    {
        imgMainLoadingGif = new GifImageView(this);
        imgMainLoadingGif.setImageResource(resourceID);
        ll.addView(imgMainLoadingGif, lp);
        return imgMainLoadingGif;
    }

    private void removeGIFFromView(GifImageView gif)
    {
        ViewGroup vg = ((ViewGroup) imgMainLoadingGif.getParent());
        vg.removeView(imgMainLoadingGif);
    }

    void retrieveStringFromURL()
    {
        // START GIF
        imgMainLoadingGif = addImageAsGIFToView(R.drawable.loading);

        // REQUEST JSON FROM URL
        Request request = new Request.Builder().url(ENDPOINT_URL).build();
        client.newCall(request).enqueue(new Callback() {
            // IF REQUEST FAILED
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        removeGIFFromView(imgMainLoadingGif);

                        // TRY TO GET STRING SAVED IN DEVICE
                        SharedPreferences sP = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                        String jsonString = sP.getString(JSON_STRING, "");

                        if (jsonString == "")
                        {
                            // DISPLAY ERROR IF COULD NOT FIND SAVED DATA
                            TextView txt = new TextView(MainActivity.this);
                            txt.setText("Unable to connect to server. Please verify your internet connection and try again");
                            ll.addView(txt, lp);
                        } else {
                            // SHOW TEXT TELLING THE USER ABOUT THE CONNECTIVITY
                            TextView txt = new TextView(MainActivity.this);
                            txt.setText("Unable to connect to server. Using last saved resources");
                            ll.addView(txt, lp);

                            // PROCESS THE DATA
                            retrieveDataFromString(jsonString);
                        }
                    }

                });
            }

            // IF REQUEST SUCCEEDED
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 404)
                {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            removeGIFFromView(imgMainLoadingGif);
                            // SHOW TEXT TELLING THE USER ABOUT THE CONNECTIVITY
                            TextView txt = new TextView(MainActivity.this);
                            txt.setText("HTTP request failed. Please contact the administrator");
                            ll.addView(txt, lp);
                            return;
                        }
                    });
                }
                if (response.isSuccessful()) {
                    final String jsonString = response.body().string();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            removeGIFFromView(imgMainLoadingGif);

                            // SAVE STRING FOR OFFLINE USE
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



    void retrieveDataFromString(String jsonString)
    {
        // CONVERT STRING INTO JSON ARRAY
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // FOR EVERY JSON OBJECT (POSTER) INTO JSON ARRAY
        for (int i = 0; i < jsonArray.length(); i++)
        {
            try {
                // GET TEXTS FROM URL
                final String title     = jsonArray.getJSONObject(i).getString("title");
                final String rating    = jsonArray.getJSONObject(i).getString("vote_average");
                final String id        = jsonArray.getJSONObject(i).getString("id");
                final String posterUrl = jsonArray.getJSONObject(i).getString("poster_url");

                // CREATE A BASIC VIEW LAYOUT WITH THE TEXTS AND A NULL BITMAP
                ImageView poster = createNewMovieObject(null, id, title, rating);

                // TRY TO RETRIEVE BITMAP FROM THE URL AND INSERT IT INTO THE LAYOUT. IF FAILS,
                // IT INSERTS A DEFAULT IMAGE
                Glide.with(this)
                        .load(posterUrl)
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.no_image)
                        .into(poster);

            } catch (Exception e) {
                continue;
            }
        }


    }


    void changeActivity(String id, Bitmap bmp) {
        // CREATE NEW INTENT FOR SHARING DATA BETWEEN ACTIVITIES
        Intent intent = new Intent(this, FilmeDetalhes.class);

        // INSERT STRING ID INTO INTENT
        intent.putExtra(EXTRA_ID, id);

        // COMPRESS BITMAP INTO BYTE ARRAY BEFORE INSERTING INTO INTENT
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        // INSERT BYTE ARRAY INTO INTENT
        intent.putExtra(EXTRA_BMP, byteArray);

        // LOAD ACTIVITY
        startActivity(intent);
    };

    // CRIA UM OBJETO CONTENTO TEXTVIEWS, LAYOUTS UMA IMAGEVIEW QUE REPRESENTA UM POSTER
    // NA TELA PRINCIPAL. RETORNA A IMAGEM ASSOCIADA AO LAYOUT.
    // O LAYOUT SEGUE O SEGUINTE ESTILO:
    //   - Horizontal Layout
    //     - Imagem do poster
    //     - Vertical layout
    //        - Titulo
    //        - Rating
    ImageView createNewMovieObject(Bitmap poster, String id, String title, String rating)
    {
        float scale = getResources().getDisplayMetrics().density;

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins((int) scale*10, (int)scale*10, (int)scale*10, (int)scale*10);

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
        tv1.setTextSize((int)scale*13);
        tv1.setTextColor(Color.parseColor("#000000"));
        tv1.setPadding(5, 10, 10, 0);
        layout2.addView(tv1);

        // RATING
        TextView tv2 = new TextView(this);
        tv2.setText(rating);
        tv2.setPadding(5, 5, 10, 0);
        tv2.setTextSize((int)scale*9);
        layout2.addView(tv2);

        ll.addView(parent, lp);
        return imgPoster;
    }
}
