package com.busywww.myliveevent;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.busywww.myliveevent.ImgurLogin.Authorization;
import com.busywww.myliveevent.classes.AspectFrameLayout;
import com.busywww.myliveevent.classes.MyCameraHelper;
import com.busywww.myliveevent.classes.MyCameraPreview;
import com.busywww.myliveevent.LecShareDB.UploadLessonToSql;
import com.busywww.myliveevent.lecshareClasses.LoginActivity;
import com.busywww.myliveevent.util.LessonSingelton;
import com.busywww.myliveevent.classes.YouTubeApi;
import com.busywww.myliveevent.classes.YouTubeStreamer;
import com.busywww.myliveevent.util.AdService;
import com.busywww.myliveevent.util.AppShared;
import com.busywww.myliveevent.util.Helper;
import com.busywww.myliveevent.util.Observer;
import com.busywww.myliveevent.util.PdfPlayer;
import com.busywww.myliveevent.util.PdfView;
import com.busywww.myliveevent.util.UtilNetwork;
import com.busywww.myliveevent.util.WebSocketsUtil;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.youtube.YouTube;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppStreaming extends AppCompatActivity implements PdfView.OnPdfPageChangedListener , Observer {


    public static Chronometer mChronometer;
    private long timeWhenStopped = 0;
    public ArrayList<LessonSingelton> pdfTimePlayerList;

    private static final int ACTIVITY_START_APP = 0;
    private ImageView mPhotoCapturedImageView;
    private String mImageFileLocation;

    public static Activity mActivity;
    public static Context mContext;
    private static View mRootView;
    private static AdService.BannerFragment adFragment;
    private static InterstitialAd fullscreenAd;

    private static MyCameraPreview AppPreview;
    private static Camera mCamera;
    private static MyCameraPreview.CameraEvent mCameraEvent;

    // wakelock
    private static final String WAKELOCK_KEY = "WEB_CONTROLLER";
    private static PowerManager.WakeLock mWakeLock;
    private static final Object LOCK = AppStreaming.class;

    public final static HttpTransport httpTransport = AppShared.AppHttpTransport;
    public final static JsonFactory jsonFactory = AppShared.AppJsonFactory;
    public static GoogleAccountCredential credential = AppShared.AccountCredential;
    public static String mRtmpUrl = "";

    private static FloatingActionButton fab;
    private static FrameLayout layoutCameraView;
    private static AspectFrameLayout cameraViewAfl;
   // private static TextView textViewTitle;
    private static ImageView imageViewSwitchCamera;
    private static ImageButton imageButtonStart;
    private static ImageView imageViewFlash;
    private static ImageView imageViewPreview;
    private ImageButton imageCaptureButton;
    private ImageView imageView;

    private PdfView pdfView;
    private WebSocketsUtil mWebSocketsUtil = new WebSocketsUtil();
    private ImageButton getPdf;
    private String videoID;
    private boolean mWasStreamed = false;
    private boolean onPause = false;



    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_streaming);


        pdfTimePlayerList = new ArrayList<>();

        mActivity = this;
        mContext = this;
        AppShared.gContext = this;
        mRootView = getWindow().getDecorView();


        prepareApp(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        boolean loggedIn = Authorization.getInstance().isLoggedIn();

        mWebSocketsUtil.connectToWebSocketServer();
        mWebSocketsUtil.connectToImageWebSocketServer();
        mWebSocketsUtil.attach(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void TakePhoto(View view) {
        MyCameraPreview.mTakePhoto = true;

    }

    @Override
    public boolean onPageChanged(int page) {

        boolean pageChanged = false;

        if(mIsStreaming) //chronometer is working
        {
            LessonSingelton.getInstanceSingelton().SetHoursMinutesSeconds(getElapsedTime(),page);

           // SendNextPage upload = new SendNextPage(page,mWebSocketsUtil);
          //  upload.execute();
            Toast.makeText(this, "Elapsed milliseconds: " + getElapsedTime(),
                    Toast.LENGTH_SHORT).show();
            pageChanged = true;


        }
        else{ // chronometer doesn't work - time 00:00

            Toast.makeText(this, "Elapsed milliseconds: " + 0,
                        Toast.LENGTH_SHORT).show();

            if(pdfView.getCurrentPage() == 0 )
            {
                LessonSingelton.getInstanceSingelton().SetHoursMinutesSeconds(0,page);
                Toast.makeText(this, "Elapsed milliseconds: " + 0,
                        Toast.LENGTH_SHORT).show();
                pageChanged = true;
            }

        }

        return pageChanged;
    }

    private void prepareApp(Bundle savedInstanceState) {
        try {

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            pdfView = new PdfView();

            fab = (FloatingActionButton)findViewById(R.id.fab);
            fab.setVisibility(View.GONE);

            mChronometer = (Chronometer)findViewById(R.id.chronometer) ;
            cameraViewAfl = (AspectFrameLayout) findViewById(R.id.cameraView_afl);
            layoutCameraView = (FrameLayout) findViewById(R.id.layoutCameraView);

            //textViewTitle = (TextView) findViewById(R.id.textViewTitle);
            imageButtonStart = (ImageButton) findViewById(R.id.imageButtonStart);
            imageCaptureButton = (ImageButton) findViewById(R.id.imageCaptureButton);

           // imageViewSwitchCamera = (ImageView) findViewById(R.id.imageViewSwitchCamera);
            imageViewFlash = (ImageView) findViewById(R.id.imageViewFlash);

            imageViewPreview = (ImageView) findViewById(R.id.imageViewPreview);
            mPhotoCapturedImageView = (ImageView) findViewById(R.id.imageViewPreview);


//            imageViewSwitchCamera.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    HandleChangeCamera();
//                }
//            });
            imageButtonStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

//                    mActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                         setOnPdf();
//                        }
//                    });

                    if (!mIsStreaming) {

                        new CheckRtmpConnection().execute();
                    } else {

                        HandleStartStreaming();
                    }
                }


            });

            imageCaptureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    TakePhoto(v);
                }
            });

            getPdf =(ImageButton)findViewById(R.id.pdfButton);
            getPdf.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                    new Thread(new Runnable() {
                        public void run() {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    getPdf.setVisibility(View.INVISIBLE);
                                    FragmentManager fm = getFragmentManager();
                                    android.app.FragmentTransaction FT = fm.beginTransaction();
                                    FT.add(R.id.pdfContainer, pdfView);
                                    FT.commit();
                                }
                            });
                       }
                    }).start();


                }
            });



            imageViewFlash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HandleFlashChange();
                }
            });
//            if (AppShared.SelectedEvent != null) {
//                textViewTitle.setText(AppShared.SelectedEvent.GetTitle());
//            }
            imageButtonStart.bringToFront();
            initCamera();

            loadAd();


        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public void startStream() {

       imageButtonStart.performClick();
    }


    public void setOnPdf()
    {

        if(!LessonSingelton.getInstanceSingelton().getIsPdf() && !onPause) //&& !mWasStreamed
        {
            getPdf.setVisibility(View.INVISIBLE);
            showAlertSetPdf();
        }
    }

    public void showAlertSetPdf() {

        AlertDialog.Builder myAlert = new AlertDialog.Builder(this);
        myAlert.setTitle("Streaming must be synchronized with PDF !");
        myAlert.setIcon(R.mipmap.ic_pdf_buttn);
        myAlert.setMessage("Do you have a pdf to upload?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
             // open pdf
                getPdf.performClick();


            }
        });
        myAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                LinearLayout pdfLayout =(LinearLayout)findViewById(R.id.pdfContainer);
                pdfLayout.setBackgroundResource(R.drawable.learn);
                pdfView.loadPicInsteadOfPDF();
                onPageChanged(0);

            }
        });
        myAlert.show();
    }

//    public void showAlertStartStreaming()
//    {
//        AlertDialog.Builder myAlert = new AlertDialog.Builder(this);
//        myAlert.setMessage("You must now start streaming...").setPositiveButton("Start", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//               // imageButtonStart.performClick();
//                startStream();
//
//            }
//        });
//        myAlert.setNegativeButton("Finish", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                mActivity.finish();
//
//            }
//        });
//        myAlert.show();
//
//    }



    public long getElapsedTime() {
        if(mIsStreaming)
        {
            long elapsedMillis = SystemClock.elapsedRealtime() - mChronometer.getBase();
            return elapsedMillis;
        }
        else
        {
            return 0;
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTIVITY_START_APP:
                if (resultCode == RESULT_OK) {
                    Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(mImageFileLocation);
                    mPhotoCapturedImageView.setImageBitmap(photoCapturedBitmap);
                }
                break;
            case AppShared.REQUEST_GMS_ERROR_DIALOG:{
            }
            break;
           /* case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    Uri content_describer = data.getData();
                    // String src = getPath(content_describer);
                    mSrcPath = W_ImgFilePathUtil.getPath(getApplicationContext(), content_describer);

                }
                break;*/
        }
    }

    private File createImageFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String name = "IMAGE_" + timestamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(name, ".jpg", storageDirectory);
        mImageFileLocation = imageFile.getAbsolutePath();
        return imageFile;
    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Helper.LoadDisplayWidthHeight2(mActivity);
    }

    @Override
    public void onResume() {

        super.onResume();
       // mChronometer.
    }

    @Override
    public void onPause() {

        super.onPause();
   //     mChronometer.stop();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        String courseName = LessonSingelton.getInstanceSingelton().getLessonCourse().getCourseName();

        if(mWasStreamed){
            UploadLessonToSql upload = new UploadLessonToSql(AppShared.SelectedEvent.GetId(),courseName);
            upload.execute();
        }

    }

    @Override
    public void finish() {
        try {

            synchronized (LOCK) {

                if (mWakeLock != null) {
                    mWakeLock.release();
                }
            }
            ReleaseOrientationEventListener();

            SetCameraPreviewStatus(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.finish();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setCameraDisplayOrientation(mActivity);
    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult

        setCameraDisplayOrientation(mActivity);

        SetCameraPreviewStatus(true);

        synchronized (LOCK) {
            if (mWakeLock == null) {
                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                //mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_KEY);
                mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, WAKELOCK_KEY);
            }
            mWakeLock.acquire();
        }

        SetOrientationEventListener(mContext);
        //SetConnectionStatus();

//        if (AppShared.gWebControllerConnected) {
//            Handler handler = new Handler();
//            handler.postDelayed(initiateUpload, 2000);
//        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "AppStreaming Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.busywww.myliveevent/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onRestart() {
        if (AppPreview == null) {
            initCamera();
        }
        super.onRestart();
    }


    public static Handler ResetCameraViewRatio = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            if (true) {
//                return;
//            }

            MyCameraPreview.AutoFeed = false;

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (cameraViewAfl == null) {
                        cameraViewAfl = (AspectFrameLayout) mActivity.findViewById(R.id.cameraView_afl);
                    }
                    double ratio = (double) MyCameraPreview.PreviewWidth / MyCameraPreview.PreviewHeight;
                    cameraViewAfl.setAspectRatio(ratio);
                }
            });
//            int centerX = cameraViewAfl.getWidth() / 2;
//            int centerY = cameraViewAfl.getHeight() / 2;
//            layoutFocus.SetCenterPosition(centerX, centerY);
//            layoutFocus.SetFocusPosition(centerX, centerY, false);
//            layoutFocus.SetSupportedModes();
//            layoutFocus.setMirror(MyCameraPreview.CameraId == MyCameraPreview.RearCamId);
//            layoutFocus.setDisplayOrientation(AppShared.gDegrees);

            if (!MyCameraPreview.AutoFeed) {
                MyCameraPreview.AutoFeed = true;
                MyCameraPreview.RequestPreviewFrame();
            }
        }
    };

    public static void initCamera() {
        try {
            prepareCameraEvent();

            // check if device has camera...
            MyCameraHelper.GetCamerasAvailability(mContext);

            if (AppPreview.HasFrontCam == false && AppPreview.HasRearCam == false) {
                // device does not have cameras...
                // skip the preview screen
            } else {
                if (AppPreview != null) {
                    AppPreview = null;
                    if (layoutCameraView.getChildCount() > 0) {
                        layoutCameraView.removeAllViews();
                    }
                }
                //Camera camera = MyCameraHelper.GetDefaultCameraInstance();
                Camera camera = MyCameraHelper.GetCameraInstanceById(mContext, MyCameraPreview.CameraId);

                AppPreview = new MyCameraPreview(mContext, camera);
                AppPreview.SetCameraEvent(mCameraEvent);
                layoutCameraView.addView(AppPreview);

                //AppCameraSettings.getPreferenceValues(mContext);

                //AppShared.gViewFinderWidth = AppPreview.getLayoutParams().width;
                //AppShared.gViewFinderHeight = AppPreview.getLayoutParams().height;
            }
            //AppShared.gPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void prepareCameraEvent() {
        try {

            if (mCameraEvent == null) {
                mCameraEvent = new MyCameraPreview.CameraEvent() {
                    @Override
                    public void PreviewFrameReady(byte[] data) {

                        // check streaming status
                        if (!mIsStreaming) {
                            // request preview frame
                            if (AppPreview != null) {
                                AppPreview.RequestPreviewFrame();
                            }
                            return;
                        }

                        // process streaming
                        if (mStreamer == null) {
                            // request preview frame
                            if (AppPreview != null) {
                                AppPreview.RequestPreviewFrame();
                            }
                            return;
                        } else {

                            mStreamer.FrameReady(data);

                            // request preview frame
                            if (AppPreview != null) {
                                AppPreview.RequestPreviewFrame();
                            }
                        }

                    }

                    @Override
                    public void PreviewFrameImage(Bitmap bitmap) {


                        // preview image
                        //imageViewPreview.setImageBitmap(bitmap);


                        // check streaming status
                        if (!mIsStreaming) {
                            // request preview frame
                            if (AppPreview != null) {
                                AppPreview.RequestPreviewFrame();
                            }
                            return;
                        }

                        // process streaming
                        if (mStreamer == null) {
                            // request preview frame
                            if (AppPreview != null) {
                                AppPreview.RequestPreviewFrame();
                            }
                            return;
                        } else {

                            mStreamer.FrameBitmap(bitmap);

                            // request preview frame
                            if (AppPreview != null) {
                                AppPreview.RequestPreviewFrame();
                            }
                        }
                    }

                    @Override
                    public void PreviewFrameYuvImage(YuvImage yuvImage) {
                        // check streaming status
                        if (!mIsStreaming) {
                            // request preview frame
                            if (AppPreview != null) {
                                AppPreview.RequestPreviewFrame();
                            }
                            return;
                        }

                        // process streaming
                        if (mStreamer == null) {
                            // request preview frame
                            if (AppPreview != null) {
                                AppPreview.RequestPreviewFrame();
                            }
                            return;
                        } else {

                            mStreamer.PreviewFrameYuvImage(yuvImage);

                            // request preview frame
                            if (AppPreview != null) {
                                AppPreview.RequestPreviewFrame();
                            }
                        }
                    }
                };
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setCameraDisplayOrientation(Activity activity) {
        try {

            Helper.LoadDeviceRotation(mActivity);

            if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.GINGERBREAD) {
                return;
            }

            if (AppPreview == null) {
                return;
            } else {
                if (AppPreview.AppCamera == null) {
                    return;
                }
                AppPreview.AppCamera.stopPreview();
            }

            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(AppPreview.CameraId, info);

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + AppShared.gDegrees) % 360;
                result = (360 - result) % 360; // compensate the mirror
            } else { // back-facing
                result = (info.orientation - AppShared.gDegrees + 360) % 360;
            }
            AppPreview.CameraRotation = result;
            AppPreview.AppCamera.setDisplayOrientation(result);
            //AppPreview.AppCamera.setDisplayOrientation(0);

            //Thread.sleep(500);
            int jpegRotation = Helper.GetImageRotation(info, AppShared.gOrientation);
            Camera.Parameters parameters = AppPreview.AppCamera.getParameters();
            parameters.setRotation(jpegRotation);
            AppPreview.AppCamera.setParameters(parameters);

            AppPreview.AppCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public static void SetCameraPreviewStatus(boolean enable) {
        try {
            //LinearLayout cameraView = (LinearLayout) mActivity.findViewById(R.id.layoutCameraView);
            if (enable) {

                if (AppPreview == null) {
                    initCamera();
                }
            } else {

                if (AppPreview == null) {
                    return;
                }
                if (AppPreview != null) {
                    AppPreview.StopPreviewReleaseCamera();
                }
                try {
                    //((LinearLayout) mActivity.findViewById(R.id.layoutCameraView)).removeView(AppShared.gPreview);
                    if (layoutCameraView != null) {
                        layoutCameraView.removeView(AppPreview);
                    }
                } catch (Exception e) {
                }
                AppPreview = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static OrientationEventListener mOrientationEventListener;

    public static void SetOrientationEventListener(Context context) {
        try {
            if (mOrientationEventListener == null) {
                mOrientationEventListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
                    @Override
                    public void onOrientationChanged(int orientation) {
                        AppShared.gOrientation = orientation;
                    }
                };
            }
            if (mOrientationEventListener.canDetectOrientation()) {
                mOrientationEventListener.enable();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ReleaseOrientationEventListener() {
        try {
            if (mOrientationEventListener != null) {
                mOrientationEventListener.disable();
                mOrientationEventListener = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void HandleChangeCamera() {

        if (AppPreview == null) {
            return;
        }

        if (AppPreview.HasFrontCam && AppPreview.HasRearCam) {
            AppPreview.CameraId += 1;
            if (AppPreview.CameraId > 1) {
                AppPreview.CameraId = 0;
            }
        } else {
            return;
        }

        if (AppPreview.AppCamera != null) {
            AppPreview.StopPreviewReleaseCamera();
        }

        final Camera camera = MyCameraHelper.GetCameraInstanceById(mContext, AppPreview.CameraId);

        if (camera == null) {
            return;
        }

        // remove current preview
        if (AppPreview != null) {
            AppPreview = null;
        }
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (layoutCameraView.getChildCount() > 0) {
                    layoutCameraView.removeAllViews();
                }

                // start new preview with new camera
                AppPreview = new MyCameraPreview(mContext, camera);
                layoutCameraView.addView(AppPreview);

                //AppShared.gViewFinderWidth = AppShared.AppPreview.getLayoutParams().width;
                //AppShared.gViewFinderHeight = AppShared.AppPreview.getLayoutParams().height;
            }
        });

        AppPreview.AutoFeed = false;

        //AppCameraSettings.getPreferenceValues(mContext);

//        AppShared.gViewFinderWidth = AppShared.AppPreview.getLayoutParams().width;
//        AppShared.gViewFinderHeight = AppShared.AppPreview.getLayoutParams().height;

        AppShared.gPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        //SetRemoteConnectedStatusImage();
        Camera.Parameters parameters = AppPreview.CameraParameters;
        boolean torch = false;
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes == null) {
            torch = false;
        } else {
            for (String mode : flashModes) {
                if (mode.equals("torch")) {
                    torch = true;
                }
            }
        }
        if (!torch) {
            imageViewFlash.setImageResource(R.drawable.ic_action_flash_off);
        }

        // send preview
        if (!MyCameraPreview.AutoFeed) {
            MyCameraPreview.AutoFeed = true;
            MyCameraPreview.RequestPreviewFrame();
        }
    }

    private static void HandleFlashChange() {
        try {
            if (AppPreview == null) {
                return;
            }
            Camera.Parameters parameters = AppPreview.CameraParameters;
            boolean torch = false;
            List<String> flashModes = parameters.getSupportedFlashModes();
            if (flashModes == null) {
                torch = false;
            } else {
                for (String mode : flashModes) {
                    if (mode.equals("torch")) {
                        torch = true;
                    }
                }
            }
            if (!torch) {
                imageViewFlash.setImageResource(R.drawable.ic_action_flash_off);
                return;
            }
            String flashMode = parameters.getFlashMode();
            if (flashMode.equals("torch")) {
                imageViewFlash.setImageResource(R.drawable.ic_action_flash_off);
                parameters.setFlashMode("off");
            } else {
                imageViewFlash.setImageResource(R.drawable.ic_action_flash_on);
                parameters.setFlashMode("torch");
            }
            AppPreview.AppCamera.setParameters(parameters);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private static void HandleShareEvent() {
//        try {
//            Helper.ActionShareLiveEvent(mContext, AppShared.SelectedEvent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static boolean mIsStreaming = false;
    private static boolean mUseBackup = false;
    private static YouTubeStreamer mStreamer = null;

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "AppStreaming Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.busywww.myliveevent/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public void update(String name) {
        final String name1 = name;
        String Tag ="update";
        Snackbar snackbar = Snackbar
                .make(fab, name+" would like to join your broadcast", Snackbar.LENGTH_LONG)
                .setAction("Allow", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        WebSocketsUtil web =AppStreaming.this.getmWebSocketsUtil();
                        Snackbar snackbar1 = Snackbar.make(fab, "connection establised!" , Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                        web.sendURL("yes",AppShared.SelectedEvent.GetId(),name1);
                    }
                });

        snackbar.show();
        Log.d(Tag,"inside update");
    }

    public WebSocketsUtil getmWebSocketsUtil(){
        return this.mWebSocketsUtil;
    }

    private class CheckRtmpConnection extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

            //
            // setOnPdf();

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                YouTubeApi.rtmpConnectionSuccess = Helper.CheckTcpPortOpen2(YouTubeApi.youtube, 1935, 4000);
                if (!YouTubeApi.rtmpConnectionSuccess) {
                    YouTubeApi.rtmpConnectionSuccess = Helper.CheckTcpPortOpen2(YouTubeApi.youtubeBackup, 1935, 4000);
                    if (YouTubeApi.rtmpConnectionSuccess) {
                        mUseBackup = true;
                    } else {
                        mUseBackup = false;
                    }
                }
            } catch (Exception e) {
                Log.e(AppShared.APP_NAME, "", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {

            if (!YouTubeApi.rtmpConnectionSuccess) {
                Snackbar.make(fab, "RTMP connection failed.", Snackbar.LENGTH_SHORT).show();
                return;
            } else {
                HandleStartStreaming();
            }

        }

    }

    private void HandleStartStreaming() {
        try {
            if (mUseBackup) {
                mRtmpUrl = AppShared.SelectedEvent.GetBackupIngestionAddress();
            } else {
                mRtmpUrl = AppShared.SelectedEvent.GetIngestionAddress();
            }

            if (mRtmpUrl == null || mRtmpUrl.length() < 1) {
                Snackbar.make(fab, "Event does not have streaming address, please use desktop YouTube application.", Snackbar.LENGTH_LONG).show();
                //.setAction("Action", null).show();
                return;
            }


//            if (!rtmpCheck) {
//                Snackbar.make(fab, "RTMP connection failed.", Snackbar.LENGTH_SHORT).show();
//                return;
//            }


            if (!mIsStreaming) {

                //boolean rtmpCheck = YouTubeApi.CheckRtmpConnection();

//                Thread t = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        YouTubeApi.rtmpConnectionSuccess = Helper.CheckTcpPortOpen2(YouTubeApi.youtube, 1935, 4000);
//                    }
//                });
//                t.start();

//                if (!YouTubeApi.rtmpConnectionSuccess) {
//                    Snackbar.make(fab, "RTMP connection failed.", Snackbar.LENGTH_SHORT).show();
//                    return;
//                }
                final ProgressDialog progressDialog = ProgressDialog.show(mActivity, null,
                        "Starting streaming...", true);

                mStreamer = new YouTubeStreamer(mRtmpUrl, AppPreview.PreviewWidth, AppPreview.PreviewHeight);
                mStreamer.StartStreaming();
                mIsStreaming = true;


                //new StartEventTask().execute(AppShared.SelectedEvent.GetEvent().getId());

                final Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        YouTube youtube = new YouTube.Builder(httpTransport, jsonFactory,
                                credential).setApplicationName(AppShared.APP_NAME)
                                .build();
                        try {
                            //YouTubeApi.StartEvent(youtube, AppShared.SelectedEvent.GetEvent().getId());

                            YouTube.LiveBroadcasts.Transition transitionRequest = youtube.liveBroadcasts().transition(
                                    "live", AppShared.SelectedEvent.GetEvent().getId(), "status");
                            transitionRequest.execute();


//                        } catch (UserRecoverableAuthIOException e) {
                            //startActivityForResult(e.getIntent(), AppShared.REQUEST_AUTHORIZATION);
                        } catch (IOException e) {
                            Log.e(AppShared.APP_NAME, "", e);
                        }
                        progressDialog.dismiss();

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mStreamer.RestartStream();
                             /*   Thread thread = new Thread() {
                                    @Override
                                    public void run() {

                                        synchronized (this) {


                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mChronometer.setBase(SystemClock.elapsedRealtime());
                                                    mChronometer.start();
                                                }
                                            });

                                        }

                                    }


                                };*/
                             //   thread.start();

                                mChronometer.setBase(SystemClock.elapsedRealtime());
                                mChronometer.start();
                                imageButtonStart.setImageResource(R.mipmap.ic_action_pause);
//
//

                                mWasStreamed = true;



                            }
                        });

                    }
                });
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mStreamer.PauseStream();
                        timeWhenStopped = getElapsedTime();
                        mChronometer.stop();
                        thread.start();
                    }
                }, 10000);


                mIsStreaming = true;
            } else {
//                mStreamer.StopStreaming();
//                mStreamer = null;
//                mIsStreaming = false;


//
                new EndEventTask().execute(AppShared.SelectedEvent.GetEvent().getId());


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class StartEventTask extends AsyncTask<String, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(mActivity, null,
                    "Starting streaming...", true);
        }

        @Override
        protected Void doInBackground(String... params) {
            YouTube youtube = new YouTube.Builder(httpTransport, jsonFactory,
                    credential).setApplicationName(AppShared.APP_NAME)
                    .build();
            try {
                mStreamer = new YouTubeStreamer(mRtmpUrl, AppPreview.PreviewWidth, AppPreview.PreviewHeight);
                mStreamer.StartStreaming();
                mIsStreaming = true;


                YouTubeApi.StartEvent(youtube, params[0]);

//            } catch (UserRecoverableAuthIOException e) {
//
//                //startActivityForResult(e.getIntent(), AppShared.REQUEST_AUTHORIZATION);


            } catch (IOException e) {
                Log.e(AppShared.APP_NAME, "", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            progressDialog.dismiss();

            imageButtonStart.setImageResource(R.mipmap.ic_action_pause);
            // mChronometer.setVisibility(View.VISIBLE);
            mChronometer.start();


//            mStreamer = new YouTubeStreamer(mRtmpUrl, AppPreview.PreviewWidth, AppPreview.PreviewHeight);
//            mStreamer.StartStreaming();
//            mIsStreaming = true;
        }

    }

    private class EndEventTask extends AsyncTask<String, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(mActivity, null,
                    "Stopping streaming...", true);
        }

        @Override
        protected Void doInBackground(String... params) {
            YouTube youtube = new YouTube.Builder(httpTransport, jsonFactory,
                    credential).setApplicationName(AppShared.APP_NAME)
                    .build();
            try {

                if (params.length >= 1) {
                }

                YouTubeApi.EndEvent(youtube, params[0]);

            } catch (UserRecoverableAuthIOException e) {
                Log.e(AppShared.APP_NAME, "", e);
                //startActivityForResult(e.getIntent(), AppShared.REQUEST_AUTHORIZATION);

            } catch (IOException e) {
                Log.e(AppShared.APP_NAME, "", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            progressDialog.dismiss();

            imageButtonStart.setImageResource(R.mipmap.ic_action_start);
            if (mStreamer != null) {
                mStreamer.StopStreaming();
                mChronometer.stop();
                mChronometer.setVisibility(View.INVISIBLE);

            }
            mStreamer = null;
            mIsStreaming = false;

            //finish();
            AppShared.AdAction = AppShared.AdActionSplash;
            if (fullscreenAd != null && fullscreenAd.isLoaded()) {
                fullscreenAd.show();
            } else {
                ProcessUserAction(mActivity, mContext, AppShared.AdAction);
            }


        }
    }


    private void loadAd() {
        try {
            // debug
            //AppShared.ShowAdView = false;

            AppShared.AdAction = AppShared.AdActionNone;

            RelativeLayout layoutContainer = (RelativeLayout) findViewById(R.id.layoutAdContainer);

            if (AppShared.ShowAdView) {
                //final LinearLayout layoutAd = (LinearLayout) findViewById(R.id.layoutAdSplash);
                //UtilGeneralHelper.InitMyAdView(mActivity, mContext, layoutAd, R.layout.fragment_ad_banner_myad);

                Helper.InitMyAdView(mContext, layoutContainer);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (UtilNetwork.IsOnline(mContext)) {
                            try {
                                adFragment = AdService.BannerFragment.newInstance();
                                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                //  ft.replace(R.id.layoutAdStreaming, adFragment);
                                ft.setTransition(FragmentTransaction.TRANSIT_NONE);
                                ft.commit();
                            } catch (Exception eee) {
                                eee.printStackTrace();
                            }


                            fullscreenAd = new InterstitialAd(mContext);
                            //UtilGeneralHelper.LoadAd(adFragment, getSupportFragmentManager(), R.id.layoutAdSplash, null);
                            fullscreenAd.setAdUnitId(AppShared.FullScreenAdId);
                            fullscreenAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();

                                    // check user action...
                                    ProcessUserAction(mActivity, mContext, AppShared.AdAction);

                                    requestFullscreenAd();
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            });

                            requestFullscreenAd();

                        }
                    }
                }, 50);
            } else {
                layoutContainer.setVisibility(View.GONE);
            }

        } catch (Exception e) {

        }
    }

    private void requestFullscreenAd() {
        try {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                        .addTestDevice("E8C1A939A52F387168C48CC03EAB8BAC")	// nexus 7
//                        .addTestDevice("1EC8F452AD1838A8F2DD2DF92A40C20B")	// nexus s
//                        .addTestDevice("9986B85AAD5AE0FA3DE84B361C5810EA")	// nexus 4
                    .build();

            fullscreenAd.loadAd(adRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ProcessUserAction(Activity activity, Context context, int actionId) {
        try {
            Intent _intent;
            switch (actionId) {
                case AppShared.AdActionNone:
                    break;
                case AppShared.AdActionSplash:
//                    _intent = new Intent(context, AppSplash.class);
//                    _intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    activity.startActivity(_intent);
                    mActivity.finish();
                    break;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SendNextPage extends AsyncTask<Void,Void,Void>{
        int page;
        WebSocketsUtil websocket = new WebSocketsUtil();
       public SendNextPage(int page, WebSocketsUtil websocket){
            this.page= page;
           this.websocket = websocket;
        }

        @Override
        protected Void doInBackground(Void... params) {
            websocket.sendNextImagePage(page);
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(),"Next Image Succeded! YaY",Toast.LENGTH_SHORT).show();
            String s ="indob";
            Log.d(s, "works");
        }
    }




}
