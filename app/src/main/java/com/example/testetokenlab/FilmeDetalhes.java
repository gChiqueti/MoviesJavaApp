package com.example.testetokenlab;

import com.example.testetokenlab.GetJsonFromEndpoint;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.concurrent.ExecutionException;

public class FilmeDetalhes extends AppCompatActivity {

    TextView texto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filme_detalhes);

        texto = (TextView) findViewById(R.id.textView3);

        Intent intent = getIntent();
        String url = intent.getStringExtra(MainActivity.EXTRA_TEXT);



        String jsonString;
        // get json array
        try {
            jsonString = new GetJsonFromEndpoint().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
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

        String titulo = null;
        try {
            titulo = jsonObject.getString("title");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        texto.setText(titulo);


    }
}
