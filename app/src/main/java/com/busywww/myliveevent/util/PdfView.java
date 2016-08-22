package com.busywww.myliveevent.util;

/**
 * Created by Danielle on 8/22/2016.
 */
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


import com.busywww.myliveevent.R;
import com.busywww.myliveevent.util.PdfPlayer;
import com.busywww.myliveevent.util.AppShared;
import com.busywww.myliveevent.util.W_ImgFilePathUtil;
import com.busywww.myliveevent.util.WebSocketsUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Alona on 8/16/2016.
 */
public class PdfView extends android.app.Fragment {


    public interface OnPdfPageChangedListener
    {
        public void onPageChanged(int page);

    }

    //  private WebSocketUtil mWebSocketsUtil = new WebSocketUtil();
    private ImageView slideView;
    private int currentPage = 0;
    private Button next, previous;
    private String mSrcPath;

    private static final int FILE_SELECT_CODE =32 ;
    private static PdfRenderer renderer;
    private static String mPdfurl ="";
    OnPdfPageChangedListener onPdfPageChangeListener;


    public void SetPathToPdf(String path)
    {
        mSrcPath = path;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pdf_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // mWebSocketsUtil.connectToImageWebSocketServer();
        // mWebSocketsUtil.attach(this);

        slideView = (ImageView)view.findViewById(R.id.imageSlide);
        startFileExplorer();
        next = (Button) view.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {

                if(renderer!=null){
                    if(renderer.getPageCount()>currentPage){

                        try {

                            currentPage++;
                            showImage();
                            onPdfPageChangeListener.onPageChanged(currentPage);
                            //SendNextPage upload = new SendNextPage(currentPage,mWebSocketsUtil);
                            //upload.execute();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        Toast.makeText(getActivity().getApplicationContext(),"please choose slide first",Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        previous = (Button) view.findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    currentPage--;
                    showImage();
                    onPdfPageChangeListener.onPageChanged(currentPage);
                    // SendNextPage upload = new SendNextPage(currentPage,mWebSocketsUtil);
                    // upload.execute();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                // render();

            }
        });

        // render();
    }
   /* public void render() {
        try {

            int REQ_WIDTH = 1;
            int REQ_HEIGHT = 1;
            REQ_WIDTH = slideView.getWidth();
            REQ_HEIGHT = slideView.getHeight();

            Bitmap bitmap = Bitmap.createBitmap(REQ_WIDTH, REQ_HEIGHT, Bitmap.Config.ARGB_4444);

            File file = new File(mSrcPath);//("/sdcard/Download/algorithms_summary.pdf");
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));

            if (currentPage < 0) {
                currentPage = 0;
            } else if (currentPage > renderer.getPageCount()) {
                currentPage = renderer.getPageCount() - 1;
            }

            Matrix m = slideView.getImageMatrix();
            Rect rect = new Rect(0, 0, REQ_WIDTH, REQ_HEIGHT);
            renderer.openPage(currentPage).render(bitmap, rect, m, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            slideView.setImageMatrix(m);
            slideView.setImageBitmap(bitmap);
            slideView.invalidate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            onPdfPageChangeListener=(OnPdfPageChangedListener)activity;
        }
        catch (Exception e){

        }

    }


    private void startFileExplorer(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setType("*/*");      //all files
        intent.setType("application/pdf");   //PDF file only
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if(renderer!=null){
            closeRenderer();
        }

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"),FILE_SELECT_CODE );
            intent.toUri(32);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(getActivity(), "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==AppShared.REQUEST_GMS_ERROR_DIALOG) {

        }
        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK){
            Uri content_describer = data.getData();
            String src = W_ImgFilePathUtil.getPath(getActivity().getApplicationContext(), content_describer);
            File source = new File(src);
            Log.d("src is ", source.toString());
            mPdfurl = src;
            String filename = content_describer.getLastPathSegment();
            Toast.makeText(getActivity(),source.toString(),Toast.LENGTH_LONG).show();
            Log.d("FileName is ", filename);
            try {
                renderer = new PdfRenderer(getSeekableFileDescriptor());
                showImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //   File destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Test/TestTest/" + filename);
            //   Log.d("Destination is ", destination.toString());
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void pdfrender() throws IOException {

        int REQ_WIDTH = 1;
        int REQ_HEIGHT = 1;
        REQ_WIDTH = slideView.getWidth();
        REQ_HEIGHT = slideView.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(REQ_WIDTH, REQ_HEIGHT, Bitmap.Config.ARGB_4444);

        Matrix m = slideView.getImageMatrix();
        Rect rect = new Rect(0, 0, REQ_WIDTH, REQ_HEIGHT);
        PdfRenderer.Page page = renderer.openPage(currentPage);
        page.render(bitmap, rect, m, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        slideView.setImageMatrix(m);
        slideView.setImageBitmap(bitmap);
        slideView.invalidate();

        page.close();

    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void closeRenderer(){
        renderer.close();

    }

    private ParcelFileDescriptor getSeekableFileDescriptor()
    {
        ParcelFileDescriptor fd = null;
        try
        {
            fd = ParcelFileDescriptor.open(new File(mPdfurl),
                    ParcelFileDescriptor.MODE_READ_ONLY);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return fd;
    }
    private void showImage() throws IOException {
        pdfrender();
    }


//    private class SendNextPage extends AsyncTask<Void,Void,Void> {
//        int page;
//        WebSocketUtil websocket = new WebSocketUtil();
//
//        public SendNextPage(int page,WebSocketUtil websocket){
//            this.page= page;
//            this.websocket = websocket;
//        }
//
//        public void SetPage(int ipage)
//        {
//            page = ipage;
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            websocket.sendNextImagePage(page);
//            return null;
//        }
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            Toast.makeText(getActivity().getApplicationContext(),"Next Image Succeded! YaY",Toast.LENGTH_SHORT).show();
//            String s ="indob";
//            Log.d(s, "works");
//        }
//    }


}

