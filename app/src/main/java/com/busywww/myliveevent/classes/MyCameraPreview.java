package com.busywww.myliveevent.classes;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageButton;

import com.busywww.myliveevent.AppStreaming;
import com.busywww.myliveevent.util.AppShared;
import com.busywww.myliveevent.util.Helper;
import com.busywww.myliveevent.util.UtilGraphic;
import com.google.common.collect.ArrayTable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.os.Environment.*;


/**
 * Created by BusyWeb on 8/25/2014.
 */
public class MyCameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private String TAG = "MyCameraPreview";

    public interface Events {
        public void NewFrameImage(Bitmap bitmap);
    }

    public interface OnCapturePhotoListener
    {
        public void onCapturePhoto(ArrayList<Bitmap> capturedPhotos);

    }


    public OnCapturePhotoListener onCapturePhotoListener;
    public static ArrayList<Bitmap> CapturedPhotos;
    public static boolean mTakePhoto = false;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static MyCameraPreview mMyCameraPreview = null;
    public static Events PreviewEvents = null;
    //public static Camera.PictureCallback mPicture;
    public static Camera AppCamera = null;
    public static Camera.Parameters CameraParameters = null;
    public static int CameraId = 0;
    public static int PreviewMessage;
    public static byte[] PreviewData = null;
    public static int[] PreviewDataInt = null;
    public static Camera.Size PreviewSize = null;
    public static int PreviewWidth = 240;   //320;   //640;   //320;
    public static int PreviewHeight = 320;  //240;  //480;  //240;
    public static boolean PreviewSizeChanged = false;
    public static int CameraRotation = 0;
    public static boolean AutoFeed = true;
    public static boolean HasFrontCam = false;
    public static boolean HasRearCam = false;
    public static int FrontCamId = 1;
    public static int RearCamId = 0;
    public static SurfaceHolder AppSurfaceHolder = null;

    private static Context mContext = null;

    private static boolean mOneShotPreviewCallback = false;

    //private static Handler mCameraHandler = null;
    //private static Handler mPreviewHandler = null;

    private static int sdk;
    public static boolean FocusAreaSupported = false;
    public static boolean MeteringAreaSupported = false;
    public static boolean ExposureCompensationSupported = true;
    public static int MaxExposureCompensation = 0;
    public static int MinExposureCompensation = 0;
    public static float CompensationStep = 0.0f;
    public static int EvOneStepCount = 0;
    public static ArrayList<String> EvList = null;
    public String mImageFileLocation;



    public interface CameraEvent {
        public void PreviewFrameReady(byte[] data);
        public void PreviewFrameImage(Bitmap bitmap);
        public void PreviewFrameYuvImage(YuvImage yuvImage);
    }


    private static CameraEvent mCameraEvent;
    public static void SetCameraEvent(CameraEvent cameraEvent) {
        mCameraEvent = cameraEvent;
    }

    public static MyCameraPreview GetMyCameraPreview() {
        return mMyCameraPreview;
    }

    public MyCameraPreview(Context context, Camera camera) {
        super(context);

        //StopPreviewReleaseCamera();
        sdk = Integer.parseInt(Build.VERSION.SDK);

        mMyCameraPreview = this;

        mContext = context;
        if (camera != null) {
            AppCamera = camera;
            CameraParameters = AppCamera.getParameters();
        }
        //mCameraHandler = camerahandler;

        CapturedPhotos = new ArrayList<>();
        AppSurfaceHolder = getHolder();
        AppSurfaceHolder.addCallback(this);
        AppSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        AppShared.gDataProcessing = false;
        AutoFeed = true;
        if (Integer.parseInt(Build.VERSION.SDK) <= Build.VERSION_CODES.CUPCAKE) {
            mOneShotPreviewCallback = false;
        } else {
            mOneShotPreviewCallback = true;
        }
    }

    public static void SetCamera(Camera camera) {
        StopPreviewReleaseCamera();

        AppCamera = camera;
        CameraParameters = AppCamera.getParameters();

        AppSurfaceHolder = mMyCameraPreview.getHolder();
        AppSurfaceHolder.addCallback(mMyCameraPreview);
        AppSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public static void ResetSurface() {
        CameraParameters = AppCamera.getParameters();

        AppSurfaceHolder = mMyCameraPreview.getHolder();
        AppSurfaceHolder.addCallback(mMyCameraPreview);
        AppSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (AppCamera == null) {
                MyCameraHelper.GetCamerasAvailability(mContext);
                AppCamera = MyCameraHelper.GetCameraInstanceById(mContext, MyCameraPreview.CameraId);
            }

            AppCamera.setPreviewDisplay(surfaceHolder);

        } catch (Exception e) {
            //Log.d(TAG, "Error: " + e.getMessage());
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        if (AppCamera == null) {
            return;
        }
        try {
            AutoFeed = false;
            AppCamera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        CameraParameters = AppCamera.getParameters();
        sdk = Integer.parseInt(Build.VERSION.SDK);
        if (sdk >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (CameraId != FrontCamId) {
                CameraParameters.setRecordingHint(true);
            }
        }

        PreviewSize = null;
        try {
            List<Camera.Size> sizes = CameraParameters.getSupportedPreviewSizes();

            if (sizes != null) {
                if (w > h) {
                    PreviewSize = MyCameraHelper.GetOptimalPreviewSize(sizes, w, h);
                } else {
                    PreviewSize = MyCameraHelper.GetOptimalPreviewSize(sizes, h, w);
                }
                if (PreviewSize == null) {
                    PreviewSize = sizes.get(0);
                } else {
                }
                CameraParameters.setPreviewSize(PreviewSize.width, PreviewSize.height);
                AppCamera.setParameters(CameraParameters);

                PreviewWidth = PreviewSize.width;
                PreviewHeight = PreviewSize.height;
                PreviewDataInt = new int[PreviewSize.width * PreviewSize.height];

                //PreviewDataInt = new int[PreviewSize.width * PreviewSize.height];
            }
            // load and send camera supported parameters for af, ae, compensation, color effects
            LoadSupportedParameterValues();

//            if (AppShared.ColorEffectList != null) {
//                CameraParameters.setColorEffect(AppShared.ColorEffectCurrent);
//            }

            //AppCamera.setParameters(CameraParameters);

            //MyCameraHelper.SetCameraParameter(getContext(), AppCamera, AppShared.gPreferences, true);
            String focusMode = "auto";  //pref.getString(AppShared.KEY_FOCUS_MODE, "auto");
            List<String> focusModes = CameraParameters.getSupportedFocusModes();
            boolean hasContinuousPicture = false;
            boolean hasContinuousVideo = false;
            for (String focus : focusModes) {
                if (focus.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    hasContinuousPicture = true;
                } else if (focus.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    hasContinuousVideo = true;
                }
            }
            if (hasContinuousPicture) {
                CameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (hasContinuousVideo) {
                CameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else {
                CameraParameters.setFocusMode(focusModes.get(0));
            }

            int jpegRotation = -1;
            int result = -1;
            if (Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.GINGERBREAD) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(CameraId, info);

                //int result;
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    result = (info.orientation + AppShared.gDegrees) % 360;
                    result = (360 - result) % 360; // compensate the mirror
                } else { // back-facing
                    result = (info.orientation - AppShared.gDegrees + 360) % 360;
                }
                CameraRotation = result;
                AppCamera.setDisplayOrientation(result);
                //AppCamera.setDisplayOrientation(0);

                //int rotation = Util.GetCameraRotation(info, (AppShared.gOrientation ==  Configuration.ORIENTATION_LANDSCAPE ? 0 : 90));
                //CameraParameters.setRotation(rotation);
                jpegRotation = Helper.GetImageRotation(info, AppShared.gOrientation);
                CameraParameters.setRotation(jpegRotation);
                //CameraParameters.setRotation(0);


            }
            AppCamera.setParameters(CameraParameters);

            AppCamera.startPreview();
            if (UsePreviewBuffer) {
                SetupPreviewCallbackWithBuffer(CameraParameters);
            }

            if (PreviewDataInt == null) {
                PreviewDataInt = new int[getWidth() * getHeight()];
            }
            PreviewDataInt = new int[getWidth() * getHeight()];


        } catch (Exception e) {
//            String path = AppShared.RootFolder + "err_surfaceChanged_" + String.valueOf(System.currentTimeMillis()) + ".txt";
//
//            Util.SaveExceptionToFile(path, e);
        } finally {
            // reset camera layout ratio
            AppStreaming.mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AppStreaming.ResetCameraViewRatio.sendEmptyMessageDelayed(0, 1000);    //1500
                }
            });

            AutoFeed = true;
        }
    }

    public static Camera.PictureCallback  mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d("MyCameraApp", "Error creating media file, check storage permissions: ");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("MyCameraApp", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("MyCameraApp", "Error accessing file: " + e.getMessage());
            }
        }
    };


    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(getExternalStoragePublicDirectory(
                DIRECTORY_PICTURES), "MyLiveEventApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void LoadSupportedParameterValues() {
        try {
            if (sdk < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                return;
            }

            if (MyCameraPreview.CameraParameters.getMaxNumFocusAreas() < 1) {
                FocusAreaSupported = false;
            } else {
                FocusAreaSupported = true;
            }
            if (MyCameraPreview.CameraParameters.getMaxNumMeteringAreas() < 1) {
                MeteringAreaSupported = false;
            } else {
                MeteringAreaSupported = true;
            }
            MaxExposureCompensation = MyCameraPreview.CameraParameters.getMaxExposureCompensation();
            MinExposureCompensation = MyCameraPreview.CameraParameters.getMinExposureCompensation();
            CompensationStep = MyCameraPreview.CameraParameters.getExposureCompensationStep();
            if (MaxExposureCompensation == 0 && MinExposureCompensation == 0) {
                ExposureCompensationSupported = false;
            } else {
                ExposureCompensationSupported = true;
            }

            if (ExposureCompensationSupported) {

                EvOneStepCount = (int)(1 / CompensationStep);

                EvList = MyCameraHelper.BuildExposureCompensationList(MinExposureCompensation, MaxExposureCompensation, EvOneStepCount, CompensationStep);
            }

            // Color Effects
            AppShared.ColorEffectList = MyCameraPreview.CameraParameters.getSupportedColorEffects();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // testing setPreviewCallbackWithBuffer
    public static boolean UsePreviewBuffer = false;
    public static byte[] PreviewDataBuffer = null;
    private void SetupPreviewCallbackWithBuffer(Camera.Parameters parameters) {
        PreviewDataBuffer = new byte[MyCameraHelper.GetPreviewFrameSize(parameters)];
        AppCamera.addCallbackBuffer(PreviewDataBuffer);
        AppCamera.setPreviewCallbackWithBuffer(CameraPreviewCallbackBuffer);
    }

    public static Camera.PreviewCallback CameraPreviewCallbackBuffer = new Camera.PreviewCallback() {

        public synchronized void onPreviewFrame(byte[] data, Camera camera) {
            // TODO Auto-generated method stub

            if (AppCamera == null) {
                return;
            }

            if (!mOneShotPreviewCallback) {
                AppCamera.setPreviewCallback(null);
            }

            if (AutoFeed) {
//                Message message = AppHandlers.CameraHandler.obtainMessage(
//                        PreviewMessage, PreviewWidth,
//                        PreviewHeight, data);
//                message.sendToTarget();
            }

            AppCamera.addCallbackBuffer(PreviewDataBuffer);

            YuvImage image = new YuvImage(PreviewDataBuffer, ImageFormat.NV21, PreviewWidth, PreviewHeight, null);
        }
    };



    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        StopPreviewReleaseCamera();
    }

    public static void StopPreview() {
        try {
            if (AppCamera != null) {
                if (UsePreviewBuffer) {
                    AppCamera.setPreviewCallbackWithBuffer(null);
                } else {
                    if (!mOneShotPreviewCallback) {
                        AppCamera.setPreviewCallback(null);
                    }
                }
                AppCamera.stopPreview();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void StopPreviewReleaseCamera() {
        try {
            try {
                if (UsePreviewBuffer) {
                    AppCamera.setPreviewCallbackWithBuffer(null);
                } else {
                    if (!mOneShotPreviewCallback) {
                        AppCamera.setPreviewCallback(null);
                    }
                }
                AppCamera.stopPreview();
            } catch (Exception e) {
            }
            // camera = null;
            AppCamera.release();
            AppCamera = null;
        } catch (Exception e) {

        }
    }

    public static void RequestPreviewFrame(int message) {
        PreviewMessage = message;
        PreviewRequestHandler.removeCallbacks(PreviewRequestRunnable);
        PreviewRequestHandler.post(PreviewRequestRunnable);
    }
    public static void RequestPreviewFrame() {
        PreviewRequestHandler.removeCallbacks(PreviewRequestRunnable);
        PreviewRequestHandler.post(PreviewRequestRunnable);
    }
    private static Handler PreviewRequestHandler = new Handler();
    private static Runnable PreviewRequestRunnable = new Runnable() {
        @Override
        public void run() {
            if (AppCamera != null) {
                // CameraHandler
                //mPreviewHandler = handler;
                //Log.i("DBG", "RequestPreviewFrame" + DateFormat.format("yyyy-MM-dd HH:mm:ss", new Date()).toString());

                //PreviewMessage = message;

                if (UsePreviewBuffer) {
                    AppCamera.addCallbackBuffer(PreviewDataBuffer);
                    AppCamera.setPreviewCallbackWithBuffer(CameraPreviewCallbackBuffer);
                } else {
                    if (mOneShotPreviewCallback) {
                        AppCamera.setOneShotPreviewCallback(CameraPreviewCallback);
                    } else {
                        AppCamera.setPreviewCallback(CameraPreviewCallback);
                    }
                }
                // Log.i("DBG", "RequestPreviewFrame");
            } else {
                // Log.i("DBG", "RequestPreviewFrame mCamera == null");
            }
        }
    };

    private static Camera.PreviewCallback CameraPreviewCallback = new Camera.PreviewCallback() {

        public void onPreviewFrame(byte[] data, Camera camera) {
            // TODO Auto-generated method stub

            if (AppCamera == null) {
                return;
            }

            if (!mOneShotPreviewCallback) {
                AppCamera.setPreviewCallback(null);
            }
            if (AutoFeed) {
//                Message message = AppHandlers.CameraHandler.obtainMessage(
//                        PreviewMessage, PreviewWidth,
//                        PreviewHeight, data);
//                message.sendToTarget();

//                mCameraEvent.PreviewFrameReady(data);

                mFrameData = data;
                UtilGraphic.decodeYUV420SP(PreviewDataInt,
                        mFrameData, PreviewWidth,
                        PreviewHeight);
                mFrameBitmap = Bitmap.createBitmap(
                        PreviewDataInt, PreviewWidth,
                        PreviewHeight, Bitmap.Config.RGB_565); //ARGB_8888



                Bitmap mBitmap =  Bitmap.createBitmap(
                        PreviewDataInt, PreviewWidth,
                        PreviewHeight, Bitmap.Config.ARGB_8888); //ARGB_8888

                 CapturedPhotos.add(mBitmap);
                TakePhoto(mFrameBitmap);



                mCameraEvent.PreviewFrameImage(mFrameBitmap);

                // mYuvImage = new YuvImage(mFrameData, ImageFormat.NV21, PreviewWidth, PreviewHeight, null);
//                mCameraEvent.PreviewFrameYuvImage(mYuvImage);

            }

            return;
        }
    };

    public static void TakePhoto(Bitmap mFrameBitmap) {
        if (mTakePhoto) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(createImageFileName().getAbsolutePath());
                mFrameBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                //CapturedPhotos.add(mFrameBitmap);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mTakePhoto = false;
        }
    }


    private static File createImageFileName()throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String name = "IMAGE_" + timestamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(name,".png",storageDirectory);
        // String mImageFileLocation = imageFile.getAbsolutePath();
        return imageFile;
    }


    public static Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean arg0, Camera arg1) {
            // TODO Auto-generated method stub
            // ResultDialog.setMessage("Preparing camera...");
            if (!arg0) {
                AppCamera.cancelAutoFocus();
            }
            try {
                // if (AppShared.gSaveLocation) {
                //      boolean success = MyCameraHelper.SetCameraGpsLocation(mContext, AppCamera, AppShared.gPreferences, AppShared.gCurrentLocation);
                //}
                AppCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
            } catch (Exception e) {
                // ResultDialog.setMessage("ERROR occured, please try again.");
            }
            MyCameraHelper.AutofocusInAction = false;
        }
    };

    static Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            // Log.d(TAG, "onShutter'd");
        }
    };

    /** Handles data for raw picture */
    static Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            // Log.d(TAG, "onPictureTaken - raw");
        }
    };

    /** Handles data for jpeg picture */
    static Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {
            try {
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inDither = false;
//                options.inPurgeable = true;
//                options.inInputShareable = true;
//                options.inTempStorage = new byte[32 * 1024];
//                String psize = AppShared.gPreferences
//                        .getString(
//                                AppCameraSettings.KEY_PICTURE_SIZE,
//                                mContext.getString(R.string.pref_camera_picturesize_default));
//                if (psize.startsWith("3648") || psize.startsWith("4608")
//                        || psize.startsWith("3264")) {
//                    if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.HONEYCOMB) {
//                        options.inSampleSize = 2;
//                    }
//                }
//                MyCameraHelper.NewPhotoImage = BitmapFactory.decodeByteArray(data, 0, data.length, options);
//
//                final Handler saveHandler = new Handler() {
//                    @Override
//                    public void handleMessage(Message msg) {
//                        if (MyCameraHelper.SaveResult) {
//                            // Log.i("DBG", "Request Photo Take: saveHandler");
//                            Message successMessage;
//                            if (AppShared.gConnectionMode == AppShared.CONNECTION_MODE_WEB){
//                                successMessage = AppHandlers.CameraHandlerWeb
//                                        .obtainMessage(AppShared.MESSAGE_PHOTO_TAKE,
//                                                AppShared.MESSAGE_PHOTO_SAVED, -1);
//                                successMessage.sendToTarget();
//                            } else {
//                                successMessage = AppHandlers.CameraHandler
//                                        .obtainMessage(AppShared.MESSAGE_PHOTO_TAKE,
//                                                AppShared.MESSAGE_PHOTO_SAVED, -1);
//                                successMessage.sendToTarget();
//                                // previewHandler = null;
//
//                            }
//                        } else {
//                            Message errMessage;
//                            if (AppShared.gConnectionMode == AppShared.CONNECTION_MODE_WEB) {
//                                errMessage = AppHandlers.CameraHandlerWeb.obtainMessage(
//                                        AppShared.MESSAGE_PHOTO_TAKE,
//                                        AppShared.MESSAGE_ERROR_PHOTO, -1);
//                                errMessage.sendToTarget();
//                                // previewHandler = null;
//                            } else {
//                                errMessage = AppHandlers.CameraHandler.obtainMessage(
//                                        AppShared.MESSAGE_PHOTO_TAKE,
//                                        AppShared.MESSAGE_ERROR_PHOTO, -1);
//                                errMessage.sendToTarget();
//                                // previewHandler = null;
//                            }
//                        }
//                    }
//                };
//
//                Thread saveThread = new Thread() {
//                    @Override
//                    public void run() {
//                        // SavePhoto(NewPhotoImage);
//                        if (AppShared.gConnectionMode == AppShared.CONNECTION_MODE_WEB) {
//                            MyCameraHelper.SavePhotoWeb();
//                        } else {
//                            MyCameraHelper.SavePhoto();
//                        }
//                        saveHandler.sendEmptyMessage(0);
//
//                        if (AppShared.gConnectionMode == AppShared.CONNECTION_MODE_WEB) {
//                            if (AppWebControlMode.AppPreview.AppCamera != null) {
//
//                                AppShared.gWebControllerConnected = true;
//
//                                AppHandlers.CameraHandlerWeb.obtainMessage(AppShared.MESSAGE_PREVIEW_STOP_REQUSET).sendToTarget();
//                                Message m = AppHandlers.CameraHandlerWeb.obtainMessage(AppShared.MESSAGE_PREVIEW_RESTART);
//                                AppHandlers.CameraHandlerWeb.sendMessageDelayed(m, 400);
//                                Message m2 = AppHandlers.CameraHandlerWeb.obtainMessage(AppShared.MESSAGE_PREVIEW_REQUEST);
//                                AppHandlers.CameraHandlerWeb.sendMessageDelayed(m2, 400);
//                            }
//                        } else {
//                            if (AppShared.AppPreview.AppCamera != null) {
//                                AppHandlers.CameraHandler.obtainMessage(AppShared.MESSAGE_PREVIEW_STOP_REQUSET).sendToTarget();
//                                Message m = AppHandlers.CameraHandler.obtainMessage(AppShared.MESSAGE_PREVIEW_RESTART);
//                                AppHandlers.CameraHandler.sendMessageDelayed(m, 400);
//                                Message m2 = AppHandlers.CameraHandler.obtainMessage(AppShared.MESSAGE_PREVIEW_REQUEST);
//                                AppHandlers.CameraHandler.sendMessageDelayed(m2, 400);
//                                //MyCameraPreview.AutoFeed = true;
//
//                                //AppCameraMode.AppPreview.AppCamera.startPreview();
//                            }
//                        }
//
//                    }
//                };
//                saveThread.start();

            } catch (Exception e) {
                //Util.ShowBadValueMessage(mContext, "DEBUG", e.toString());
                e.printStackTrace();

            } finally {

            }
        }
    };


    private static byte[] mFrameData = null;
    private static Bitmap mFrameBitmap = null;
    private static YuvImage mYuvImage = null;
}
