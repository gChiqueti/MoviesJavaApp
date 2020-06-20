package com.example.testetokenlab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {


    class RetrieveFeedTask extends AsyncTask<String, Void, String>
    {
        Exception exception;
        private String readStream(InputStream in) throws IOException
        {
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(in),10000);
            for (String line = r.readLine(); line != null; line =r.readLine()){
                sb.append(line);
            }
            in.close();
            return sb.toString();
        }

        public String getRequest(String context)
        {
            URL url;
            String result;

            // Create URL
            try{
                url = new URL(context);
            }catch(MalformedURLException ex){
                return "";
            }

            // Opens the connection
            HttpURLConnection urlConnection;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException e)
            {
                e.printStackTrace();
                return "";
            }

            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                result = readStream(in);
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            } finally {
                urlConnection.disconnect();
            }
            return result;
        }

        protected String doInBackground(String... url)
        {
            String example;
            try {
                example = getRequest("https://desafio-mobile.nyc3.digitaloceanspaces.com/movies");
            } catch (Exception e) {
                this.exception = e;
                example = "";
            }
            return example;

        }
    }

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
        botao_principal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeActivity();
            }
        });


        Log.d("MyApp","I am here");
        Log.i("MyApp","I am here2");
        Log.i("MyApp","I am here3");
        String result;
        try {
            result = new RetrieveFeedTask().execute("").get();
            Log.i("OI", result);
        } catch (ExecutionException e) {
            e.printStackTrace();
            Log.i("MyApp","I am here6");
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i("MyApp","I am here5");
            return;
        }
        Log.i("MyApp","I am here4");

        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(result);
            String json0 = jsonArray.get(2).toString();
            Log.i("MyApp23",json0);
            String title = jsonArray.getJSONObject(2).getString("title");

            // Carrega a imagem no imageView
            String url = jsonArray.getJSONObject(2).getString("poster_url");
            Log.i("MyApp24",url);

            ImageView i = (ImageView)findViewById(R.id.image);
            try {
                Bitmap bitmap = new RetrieveImage().execute(url).get();
                if (bitmap != null) {
                    i.setImageBitmap(bitmap);
                    Log.i("MyApp", "imagem carregada com sucesso");
                } else {
                    Log.i("MyApp", "imagem não foi carregada");
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                Log.i("MyApp", "Deu erro cabuloso");
            }





            //botao_principal.setText(title);

            Log.i("Title",title);

        } catch (JSONException e) {
            Log.i("Erro JSON","Inicio erro json");
            e.printStackTrace();
            Log.i("Erro JSON","Fim erro json");
            return;
        }

        LinearLayout ll = (LinearLayout) findViewById(R.id.layout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < jsonArray.length(); i++)
        {
            try {
                String title = jsonArray.getJSONObject(i).getString("title");
                Button myButton = new Button(this);
                myButton.setText(title);
                ll.addView(myButton, lp);

            }catch (JSONException e)
            {

            }

            try {
                String url = jsonArray.getJSONObject(i).getString("poster_url");
                Log.i("URL", url);
                Bitmap bitmap = new RetrieveImage().execute(url).get();
                if (bitmap != null) {
                    ImageView v = new ImageView(this);
                    v.setImageBitmap(bitmap);
                    ll.addView(v, lp);
                }
            } catch (Exception e)
            {

            }

        }  // endfor

    }

    void changeActivity(){
        Intent intent = new Intent(this, FilmeDetalhes.class);
        startActivity(intent);
    };
}
