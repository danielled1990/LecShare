package com.busywww.myliveevent.util;


import com.busywww.myliveevent.ImgurLogin.Authorization;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;


import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public abstract class ImgurUploadTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = ImgurUploadTask.class.getSimpleName();
    private int i;
    private static final String UPLOAD_URL = "https://api.imgur.com/3/image";
    private Activity mActivity;
    private Bitmap mImageBitmap;
    private ByteArrayOutputStream stream;
    private String[] mImgurUrl;

    public ImgurUploadTask(Bitmap bitmap, Activity activity,String[] mImgurUrl) {
      //  this.mImageUri = imageUri;
        mImageBitmap = bitmap;

        this.mActivity = activity;
        this.mImgurUrl = mImgurUrl;


    }

    @Override
    protected String doInBackground(Void... params) {
        InputStream imageIn;

        stream = new ByteArrayOutputStream();
        mImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        mImageBitmap.getWidth();
        Log.d("doInBackground",Integer.toString(mImageBitmap.getHeight()));

        imageIn = new ByteArrayInputStream(stream.toByteArray());
        //imageIn = mActivity.getContentResolver().openInputStream(mImageUri);

        HttpURLConnection conn = null;
        InputStream responseIn = null;

        try {
            conn = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();
            conn.setDoOutput(true);
            Authorization.getInstance().addToHttpURLConnection(conn);


            OutputStream out = conn.getOutputStream();

            copy(imageIn, out);
            out.flush();
            out.close();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                responseIn = conn.getInputStream();
                return onInput(responseIn);
            } else {
                Log.i(TAG, "responseCode=" + conn.getResponseCode());
                responseIn = conn.getErrorStream();
                StringBuilder sb = new StringBuilder();
                Scanner scanner = new Scanner(responseIn);
                while (scanner.hasNext()) {
                    sb.append(scanner.next());
                }
                Log.i(TAG, "error response: " + sb.toString());
                return null;
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error during POST", ex);
            return null;
        } finally {
            try {
                responseIn.close();
            } catch (Exception ignore) {
            }
            try {
                conn.disconnect();
            } catch (Exception ignore) {
            }
            try {
                imageIn.close();
            } catch (Exception ignore) {
            }
        }
    }

    private static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[8192];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    protected String onInput(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(in);
        while (scanner.hasNext()) {
            sb.append(scanner.next());
        }

        JSONObject root = new JSONObject(sb.toString());
        String id = root.getJSONObject("data").getString("id");
        String deletehash = root.getJSONObject("data").getString("deletehash");

        Log.i(TAG, "new imgur url: http://imgur.com/" + id + " (delete hash: " + deletehash + ")");
        mImgurUrl[0] = "http://imgur.com/" + id + ".png";
        if(PdfView.photoTaken)  {

            LessonSingelton.getInstanceSingelton().addToImageLink(mImgurUrl[0]);
            PdfView.photoTaken = false;
        }
        else{

            LessonSingelton.getInstanceSingelton().addToPdfImageLinks(mImgurUrl[0]);
        }



        return id;
    }
}