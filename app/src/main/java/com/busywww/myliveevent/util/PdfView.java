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
import android.widget.TextView;
import android.widget.Toast;


import com.busywww.myliveevent.R;
import com.busywww.myliveevent.classes.MyCameraPreview;
import com.busywww.myliveevent.util.PdfPlayer;
import com.busywww.myliveevent.util.AppShared;
import com.busywww.myliveevent.util.W_ImgFilePathUtil;
import com.busywww.myliveevent.util.WebSocketsUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

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
    public ArrayList<String> CapturedImageURL;
    private boolean photoTaken = false;

  //  private Uri mImagePdfUri;
    private String mImageUrl;

    private MyImgurUploadTask mImgurUploadTask;
    private String mCapturedImageURL;
    private Bitmap mBitmap;


    public void SetPathToPdf(String path)
    {
        mSrcPath = path;
    }

    @Override
    public void onCapturePhoto(ArrayList<Bitmap> capturedPhotos)
    {    int i;
        photoTaken = true;

        for (i =0 ;i<capturedPhotos.size();i++)
        {
            new MyImgurUploadTask(capturedPhotos.get(i)).execute();

        }

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pdf_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CapturedImageURL = new ArrayList<>();
        pdfImagePages = new ArrayList<>();
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
                            onPdfPageChangeListener.onPageChanged(currentPage,mImageUrl);


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
                    onPdfPageChangeListener.onPageChanged(currentPage,mImageUrl);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

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

        mBitmap = Bitmap.createBitmap(REQ_WIDTH, REQ_HEIGHT, Bitmap.Config.ARGB_4444);
        new MyImgurUploadTask(mBitmap).execute();
        if(mImageUrl!=null)
        Toast.makeText(getActivity(), "Upload Successfully! The Link :"+ mImageUrl, Toast.LENGTH_LONG).show();



        pdfImagePages.add(currentPage,mBitmap);

        Matrix m = slideView.getImageMatrix();
        Rect rect = new Rect(0, 0, REQ_WIDTH, REQ_HEIGHT);
        PdfRenderer.Page page = renderer.openPage(currentPage);
        page.render(mBitmap, rect, m, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        slideView.setImageMatrix(m);
        slideView.setImageBitmap(mBitmap);
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

    public class MyImgurUploadTask extends ImgurUploadTask {
        private String mImgurUrl;
        public MyImgurUploadTask(Bitmap bitmap) {
            super(mBitmap, getActivity());

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
            mImgurUrl = null;
            //getView().findViewById(R.id.choose_image_button).setEnabled(false);
           // setImgurUploadStatus(R.string.choose_image_upload_status_uploading);
        }
        @Override
        protected void onPostExecute(String imageId) {
            super.onPostExecute(imageId);
            mImgurUploadTask = null;
            if (imageId != null) {
                mImgurUrl = "http://imgur.com/" + imageId+".png";
               // setImgurUploadStatus(R.string.choose_image_upload_status_success);
               // if (isResumed()) {
                    //getView().findViewById(R.id.imgur_link_layout).setVisibility(View.VISIBLE);
                    //((TextView) getView().findViewById(R.id.link_url)).setText(mImgurUrl);
                if(photoTaken)
                {
                    PdfView.this.CapturedImageURL.add(mImgurUrl);

                }
                else
                {
                    PdfView.this.mImageUrl = mImgurUrl;
                }

               // Toast.makeText(getActivity(), "Upload Successfully! The Link :"+ mImgurUrl, Toast.LENGTH_LONG).show();
             //   }
            } else {
                mImgurUrl = null;
                        Toast.makeText(getActivity(), R.string.imgur_upload_error, Toast.LENGTH_LONG).show();

                //setImgurUploadStatus(R.string.choose_image_upload_status_failure);
               /* if (isResumed()) {
                    getView().findViewById(R.id.imgur_link_layout).setVisibility(View.GONE);
                    if (isVisible()) {
                        ((ImageView) getView().findViewById(R.id.choose_image_preview)).setImageBitmap(null);
                        Toast.makeText(getActivity(), R.string.imgur_upload_error, Toast.LENGTH_LONG).show();
                    }
                }*/
            }

        }
    }




}

