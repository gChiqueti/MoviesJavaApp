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

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_TEXT = "com.example.testetokenlab.example.EXTRA_TEXT";
    public static final String ENDPOINT_URL = "https://desafio-mobile.nyc3.digitaloceanspaces.com/movies";

    class RetrieveImage extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... url) {
            Bitmap bitmap = null;
            try {
                URL u = new URL(url[0]);
                InputStream c = (InputStream) u.getContent();
                bitmap = BitmapFactory.decodeStream(c);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }

    Button botao_principal;
    ImageView imagem_principal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        botao_principal = (Button) findViewById(R.id.buttonTitulo);

        String jsonString;
        try {
            jsonString = new GetJsonFromEndpoint().execute(ENDPOINT_URL).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String json0 = null;
        try {
            json0 = jsonArray.get(2).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String title = null;
        try {
            title = jsonArray.getJSONObject(2).getString("title");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Carrega a imagem no imageView
        ImageView imageView = (ImageView)findViewById(R.id.image);
        String url = null;
        try {
            url = jsonArray.getJSONObject(2).getString("poster_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try
        {
            Bitmap bitmap = new RetrieveImage().execute(url).get();
            if (bitmap != null)
            {
                imageView.setImageBitmap(bitmap);
                Log.i("MyApp", "imagem carregada com sucesso");
            } else {
                Log.i("MyApp", "imagem não foi carregada");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            Log.i("MyApp", "Deu erro cabuloso");
        }

        LinearLayout ll = (LinearLayout) findViewById(R.id.layout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < jsonArray.length(); i++)
        {
            try {
                title = jsonArray.getJSONObject(i).getString("title");
                Button myButton = new Button(this);
                myButton.setText(title);
                ll.addView(myButton, lp);

            }catch (JSONException e)
            {

            }

            try {
                url = jsonArray.getJSONObject(i).getString("poster_url");
                String id = jsonArray.getJSONObject(i).getString("id");
                Log.i("URL", url);
                Bitmap bitmap = new RetrieveImage().execute(url).get();
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
            } catch (Exception e)
            {

            }

        }  // endfor

    }

    void changeActivity(String id)
    {
        Intent intent = new Intent(this, FilmeDetalhes.class);
        intent.putExtra(EXTRA_TEXT, ENDPOINT_URL.concat("/" + id));
        startActivity(intent);
    };
}
