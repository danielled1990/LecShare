package com.busywww.myliveevent.util;

/**
 * Created by Danielle on 8/22/2016.
 */
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.busywww.myliveevent.AppStreaming;
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
        public void onPageChanged(int page,String mImageUrl);

    }

    private ImageView slideView;
    private int currentPage = 0;
    private Button next, previous;
    private String mSrcPath;

    private static final int FILE_SELECT_CODE =32 ;
    private static PdfRenderer renderer;
    private static String mPdfurl ="";
    OnPdfPageChangedListener onPdfPageChangeListener;
    public ArrayList<Bitmap> pdfImagePages;
    private boolean photoTaken = false;
    private Activity activity;
    private String[] mImageUrl = new String[1];
    private MyImgurUploadTask mImgurUploadTask;
    private Bitmap mBitmap;




    @Override
    public void onCapturePhoto(Bitmap capturedPhoto)
    {
        int size;
        photoTaken = true;

            MyImgurUploadTask uploadTask = new MyImgurUploadTask(capturedPhoto,mImageUrl,photoTaken);
            uploadTask.execute();
            photoTaken = false;

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


        next = (Button) view.findViewById(R.id.next);
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

                                            LessonSingelton.getInstanceSingelton().setIsPdf(true);
                                            showImage();
                                            onPdfPageChangeListener.onPageChanged(currentPage, mImageUrl[0]);
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

        previous = (Button) view.findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  LessonSingelton.getInstanceSingelton().setIsPdf(true);
                try {

                    currentPage--;
                   LessonSingelton.getInstanceSingelton().setIsPdf(true);
                    showImage();
                    onPdfPageChangeListener.onPageChanged(currentPage,mImageUrl[0]);

                } catch (IOException e) {
                    e.printStackTrace();
                }

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
                      try {
                          renderer = new PdfRenderer(getSeekableFileDescriptor());
                      }catch (IOException e){e.printStackTrace();}

                    //LessonSingelton.getInstanceSingelton().setIsPdf(true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                showImage();
                                onPdfPageChangeListener.onPageChanged(currentPage, mImageUrl[0]);
                            }
                            catch( IOException e)
                            {
                                e.printStackTrace();
                            }
                        }

//
                    });
                    //   File destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Test/TestTest/" + filename);
                    //   Log.d("Destination is ", destination.toString());

                    // });
                    // }
                }

            }).start();
        }
    }

    //}
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void pdfrender() throws IOException, ExecutionException, InterruptedException {

        int REQ_WIDTH = 1;
        int REQ_HEIGHT = 1;
        REQ_WIDTH = slideView.getWidth();
        REQ_HEIGHT = slideView.getHeight();

        mBitmap = Bitmap.createBitmap(REQ_WIDTH, REQ_HEIGHT, Bitmap.Config.ARGB_8888);
        Log.d("pdfview",Integer.toString(REQ_HEIGHT));
       // new MyImgurUploadTask(mBitmap).execute();
        pdfImagePages.add(currentPage,mBitmap);

        Matrix m = slideView.getImageMatrix();
        //Rect rect = new Rect(0, 0, REQ_WIDTH, REQ_HEIGHT);
        PdfRenderer.Page page = renderer.openPage(currentPage);
        page.render(mBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
        new MyImgurUploadTask(mBitmap,mImageUrl,false).execute();



        slideView.setImageMatrix(m);
        slideView.setImageBitmap(mBitmap);
        slideView.invalidate();
    //    if(mImageUrl!=null)
    //        Toast.makeText(getActivity(), "Upload Successfully! The Link :"+ mImageUrl[0], Toast.LENGTH_LONG).show();
        page.close();

       //new MyImgurUploadTask(mBitmap,mImageUrl,false).execute();

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

    public class MyImgurUploadTask extends ImgurUploadTask {
        private String[] mImgurUrl;


        public MyImgurUploadTask(Bitmap bitmap,String[] mImgurUrl,boolean isPhoto) {

           super(bitmap, getActivity(),mImgurUrl,isPhoto);
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
                mImgurUrl[0] = "http://imgur.com/" + imageId+".png";

//                if( LessonSingelton.getInstanceSingelton().getIsPdf()){
//
//                    LessonSingelton.getInstanceSingelton().addToImageLink(mImgurUrl[0]);
//                    LessonSingelton.getInstanceSingelton().setIsPdf(false);
//                }
//                else{
//
//                    LessonSingelton.getInstanceSingelton().addToPdfImageLinks(mImgurUrl[0]);
//                }
            } else {
                mImgurUrl = null;
                        Toast.makeText(getActivity(), R.string.imgur_upload_error, Toast.LENGTH_LONG).show();

            }

        }
    }


}

