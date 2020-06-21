package com.example.testetokenlab;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.URL;

public class GetBitmapFromURL extends AsyncTask<String, Void, Bitmap> {
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
