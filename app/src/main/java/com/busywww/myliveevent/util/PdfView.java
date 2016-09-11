package com.busywww.myliveevent.util;

/**
 * Created by Danielle on 8/22/2016.
 */
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


import com.busywww.myliveevent.AppStreaming;
import com.busywww.myliveevent.LecShare;
import com.busywww.myliveevent.R;
import com.busywww.myliveevent.classes.MyCameraPreview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Alona on 8/16/2016.
 */
public class PdfView extends android.app.Fragment implements MyCameraPreview.OnCapturePhotoListener {


    public interface OnPdfPageChangedListener
    {
        public boolean onPageChanged(int page);
        public void startStream();

    }

    private ImageView slideView;
    private int currentPage = 0;
    private ImageButton next, previous;
    private String mSrcPath;

    private static final int FILE_SELECT_CODE =32 ;
    private static PdfRenderer renderer;
    private static String mPdfurl ="";
    OnPdfPageChangedListener onPdfPageChangeListener;
    public ArrayList<Bitmap> pdfImagePages;
    public static boolean photoTaken = false;
    private Activity activity;
   public String[] mImageUrl = new String[1];
    private MyImgurUploadTask mImgurUploadTask;
    private Bitmap mBitmap;
    int[] PreviewDataInt =null;

    public int getCurrentPage()
    {
        return currentPage;
    }



    @Override
    public void onCapturePhoto(Bitmap capturedPhoto)
    {
        photoTaken = true;

            MyImgurUploadTask uploadTask = new MyImgurUploadTask(capturedPhoto,mImageUrl);
            uploadTask.execute();


    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pdf_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImageUrl[0]="";

        pdfImagePages = new ArrayList<>();
        slideView = (ImageView)view.findViewById(R.id.imageSlide);

        PreviewDataInt = new int[slideView.getWidth() * slideView.getHeight()];

        next = (ImageButton) view.findViewById(R.id.next);
        activity = AppStreaming.mActivity;
        next.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if (renderer != null) {
                            if (renderer.getPageCount() > currentPage) {

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        currentPage++;
                                        try {

                                            showImage();

                                          onPdfPageChangeListener.onPageChanged(currentPage);


                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                               }
                                else
                                {
                                    Toast.makeText(getActivity().getApplicationContext(), "please choose slide first", Toast.LENGTH_SHORT).show();
                                }

                        }
                    }
                }).start();

            }
            });

        previous = (ImageButton) view.findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                new Thread(new Runnable() {
                    @Override
                    public void run() {

                    currentPage--;
                    getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            showImage();
                                                        }catch (IOException e)
                                                        {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                    onPdfPageChangeListener.onPageChanged(currentPage);

                    }

                }).start();

            }
        });

        startFileExplorer();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            onPdfPageChangeListener=(OnPdfPageChangedListener)activity;
        }
        catch (Exception e){

        }
    }

    public void startFileExplorer(){

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //intent.setType("*/*");      //all files
                intent.setType("application/pdf");   //PDF file only
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                if (renderer != null) {
                    closeRenderer();
                }

                try {
                    startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
                    intent.toUri(32);
                } catch (android.content.ActivityNotFoundException ex) {
                    // Potentially direct the user to the Market with a Dialog
                    Toast.makeText(getActivity(), "Please install a File Manager.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == AppShared.REQUEST_GMS_ERROR_DIALOG) {
        }
        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {



            Uri content_describer = data.getData();
            String src = W_ImgFilePathUtil.getPath(getActivity().getApplicationContext(), content_describer);
            File source = new File(src);
            Log.d("src is ", source.toString());
            mPdfurl = src;
            String filename = content_describer.getLastPathSegment();
            //  Toast.makeText(getActivity(),source.toString(),Toast.LENGTH_LONG).show();
            //  Log.d("FileName is ", filename);

            new Thread(new Runnable() {
                public void run() {

                    getActivity().runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                            try {
                                try {
                                    renderer = new PdfRenderer(getSeekableFileDescriptor());
                                }catch (IOException e){e.printStackTrace();}

                                LessonSingelton.getInstanceSingelton().setPdfOn();
                                showImage();
                                onPdfPageChangeListener.onPageChanged(currentPage);

                            }
                            catch( IOException e)
                            {
                                e.printStackTrace();
                            }

                        }

                   });

                     }


            }).start();

           if(!AppStreaming.mIsStreaming)
           {
               showAlertStartStreaming();

            }

        }
    }


    public void showAlertStartStreaming()
    {
        AlertDialog.Builder myAlert = new AlertDialog.Builder(activity);
        myAlert.setMessage("You must now start streaming...").setPositiveButton("Start", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                onPdfPageChangeListener.startStream();

            }
        });
        myAlert.setNegativeButton("Finish", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                activity.finish();

            }
        });
        myAlert.show();

    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void pdfrender() throws IOException, ExecutionException, InterruptedException {

//        int REQ_WIDTH = 1;
//        int REQ_HEIGHT = 1;
//        REQ_WIDTH = slideView.getWidth();
//        REQ_HEIGHT = slideView.getHeight();
//
//
//        mBitmap = Bitmap.createBitmap(REQ_WIDTH, REQ_HEIGHT, Bitmap.Config.ARGB_8888);
//        Log.d("pdfview",Integer.toString(REQ_HEIGHT));
//        Matrix m = slideView.getImageMatrix();
//        Rect rect = new Rect(0, 0, REQ_WIDTH, REQ_HEIGHT);
//        PdfRenderer.Page page = renderer.openPage(currentPage);
//        page.render(mBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
//       slideView.setImageMatrix(m);
//        slideView.setImageBitmap(mBitmap);
//        slideView.invalidate();
//        page.close();
//
//       Bitmap bitmap = mBitmap;//Bitmap.createBitmap(400,400,Bitmap.Config.ARGB_8888);
//
//        MyImgurUploadTask uploadTask =  new MyImgurUploadTask(bitmap,mImageUrl);
//        uploadTask.execute();


        MyPdfUploadTask pdfTask =  new MyPdfUploadTask(slideView.getHeight(),slideView.getWidth());
        pdfTask.execute();

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
        try {

            pdfrender();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public class MyPdfUploadTask extends AsyncTask<Void,Void,Void>
    {
        Bitmap mBitmap;
        int REQ_WIDTH = 1;
        int REQ_HEIGHT = 1;
        Matrix m ;
        PdfRenderer.Page page = null;
        MyImgurUploadTask uploadTask;

        public MyPdfUploadTask(int height,int width)
        {
            REQ_HEIGHT = height;
            REQ_WIDTH = width;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mBitmap = Bitmap.createBitmap(REQ_WIDTH, REQ_HEIGHT, Bitmap.Config.ARGB_8888);


        }

        @Override
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            PdfView.this.slideView.setImageMatrix(m);
            PdfView.this.slideView.setImageBitmap(mBitmap);
            PdfView.this.slideView.invalidate();
            page.close();

            uploadTask.execute();

        }


        @Override
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        protected Void doInBackground(Void... params)
        {

            Log.d("pdfview",Integer.toString(REQ_HEIGHT));
            page = renderer.openPage(currentPage);
            page.render(mBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            uploadTask = new MyImgurUploadTask(mBitmap,PdfView.this.mImageUrl);

            return null;
        }

    }


    public class MyImgurUploadTask extends ImgurUploadTask {
        private String[] mImgurUrl;

        public MyImgurUploadTask(Bitmap bitmap,String[] mImgurUrl) {

           super(bitmap, getActivity(),mImgurUrl);
            this.mImgurUrl = mImgurUrl;


        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mImgurUploadTask != null) {
                boolean cancelled = mImgurUploadTask.cancel(false);
                if (!cancelled)
                    this.cancel(true);
            }
            mImgurUploadTask = this;


        }
        @Override
        protected void onPostExecute(String imageId) {
            super.onPostExecute(imageId);
            mImgurUploadTask = null;
            if (imageId != null) {
                mImgurUrl[0] = "http://imgur.com/" + imageId+".jpg";


            } else {
                mImgurUrl = null;
                        Toast.makeText(getActivity(), R.string.imgur_upload_error, Toast.LENGTH_LONG).show();

            }


        }
    }


}

