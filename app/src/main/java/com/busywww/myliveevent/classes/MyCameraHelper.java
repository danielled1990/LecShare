package com.busywww.myliveevent.classes;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;

import com.busywww.myliveevent.util.AppShared;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by BusyWeb on 8/25/2014.
 */
public class MyCameraHelper {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    //public static List<Integer> ZoomRatio = null;
    public static boolean ZoomSupported = false;
    //public static Integer ZoomCurrent = 100;
    //public static Integer ZoomMax = 100;
    private static DismissAutofocus myDismissAutofocus = new DismissAutofocus();
    private static final int AutofocusDuration = 3000;
    public static boolean AutofocusInAction = false;


    /**
     * Iterate over supported camera preview sizes to see which one best fits the
     * dimensions of the given view while maintaining the aspect ratio. If none can,
     * be lenient with the aspect ratio.
     *
     * @param sizes Supported camera preview sizes.
     * @param w The width of the view.
     * @param h The height of the view.
     * @return Best match camera preview size to fit in the view.
     */
    public static  Camera.Size GetOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        Camera.Size optimalSize = null;

        // new preview size and quality settings (Oct. 22, 2014)
        String currSize = String.valueOf(MyCameraPreview.PreviewWidth)
                    + "x"
                    + String.valueOf(MyCameraPreview.PreviewHeight);    //320x240, "352x288";

        currSize = "320x240";

        if (currSize != null && currSize.length() > 3) {
            String[] sizeValue = currSize.split("x");
            int cW = Integer.parseInt(sizeValue[0]);
            int cH = Integer.parseInt(sizeValue[1]);
            for (Camera.Size size : sizes) {
                if (size.width == cW && size.height == cH) {
                    optimalSize = size;
                    break;
                }
            }
        }
        if (optimalSize != null) {
            return optimalSize;
        }

        // Use a very small tolerance because we want an exact match.
        //final double ASPECT_TOLERANCE = 0.1;
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        int MAX_WANTED_HEIGHT = 320;    //240;    //480;    //240;    //288;
        int MAX_WANTED_WIDTH = 240;     //640;     //320;     //352
        // Start with max value and refine as we iterate over available preview sizes. This is the
        // minimum difference between view and camera height.
        double minDiff = Double.MAX_VALUE;
        //double minDiff = Double.MIN_VALUE;

        // Target view height
        int targetHeight = h;

        // Try to find a preview size that matches aspect ratio and the target view size.
        // Iterate over all available sizes and pick the largest size that can fit in the view and
        // still maintain the aspect ratio.
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff && size.height <= MAX_WANTED_HEIGHT) {
            //if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find preview size that matches the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff && size.height <= MAX_WANTED_HEIGHT) {
                //if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
            if (optimalSize == null) {
                MAX_WANTED_WIDTH = 240;     //320;     //640;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.width - w) <= minDiff && size.width <= MAX_WANTED_WIDTH) {
                        //if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.width - w);
                    }
                }
            }

            if (optimalSize == null) {
                MAX_WANTED_WIDTH = AppShared.gDisplay.getWidth();
                for (Camera.Size size : sizes) {
                    if (optimalSize == null) {
                        optimalSize = size;
                        continue;
                    }
                    if (size.width <= MAX_WANTED_WIDTH && size.width <= optimalSize.width) {
                        //if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                    }
                }
            }
        }
        return optimalSize;
    }

    /**
     * @return the default camera on the device. Return null if there is no camera on the device.
     */
    public static Camera GetDefaultCameraInstance() {
        return Camera.open();
    }

    /**
     * @return the default rear/back facing camera on the device. Returns null if camera is not
     * available.
     */
    public static Camera GetDefaultBackFacingCameraInstance() {
        return getDefaultCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    /**
     * @return the default front facing camera on the device. Returns null if camera is not
     * available.
     */
    public static Camera GetDefaultFrontFacingCameraInstance() {
        return getDefaultCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    public static Camera GetCameraInstanceById(Context context, int id) {
        if (MyCameraPreview.HasFrontCam && MyCameraPreview.HasRearCam) {
            return Camera.open(id);
        } else {
            if (MyCameraPreview.HasFrontCam && !MyCameraPreview.HasRearCam) {
                return Camera.open(id);
                //return GetDefaultCameraInstance();
            } else {
                return GetDefaultCameraInstance();
            }
        }
    }
    /**
     *
     * @param position Physical position of the camera i.e Camera.CameraInfo.CAMERA_FACING_FRONT
     *                 or Camera.CameraInfo.CAMERA_FACING_BACK.
     * @return the default camera on the device. Returns null if camera is not available.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static Camera getDefaultCamera(int position) {
        // Find the total number of cameras available
        int  mNumberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the back-facing ("default") camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == position) {
                return Camera.open(i);

            }
        }

        return null;
    }

    public static void GetCamerasAvailability(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            // It would be safer to use the constant
            // PackageManager.FEATURE_CAMERA_FRONT
            // but since it is not defined for Android 2.2, I substituted the
            // literal value
            MyCameraPreview.HasFrontCam = pm.hasSystemFeature("android.hardware.camera.front");
            MyCameraPreview.HasRearCam = pm.hasSystemFeature("android.hardware.camera");	//PackageManager.FEATURE_CAMERA);

            int  mNumberOfCameras = Camera.getNumberOfCameras();

            // Find the ID of the back-facing ("default") camera
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            //MyCameraPreview.CameraId= 0;
            for (int i = 0; i < mNumberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    MyCameraPreview.RearCamId = i;
                    if (MyCameraPreview.CameraId == i) {
                        MyCameraPreview.CameraId = i;
                    }
                } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    MyCameraPreview.FrontCamId = i;
                    if (!MyCameraPreview.HasRearCam) {
                        MyCameraPreview.CameraId = i;
                    }
                }
            }
        } catch (Exception e) {
//            String path = AppShared.RootFolder + "err_GetCamerasAvailability_" + String.valueOf(System.currentTimeMillis()) + ".txt";
//
//            Util.SaveExceptionToFile(path, e);
        }
    }

    public static int GetPreviewFrameSize( Camera.Parameters parameters) {
        int frameSize = 0;
        try {
            int format = parameters.getPreviewFormat();
            int bitsperpixel = ImageFormat.getBitsPerPixel(format);
            int bytesperpixel = bitsperpixel / 8;
            Camera.Size previewsize = parameters.getPreviewSize();

            frameSize = ((previewsize.width * previewsize.height) * bitsperpixel) / 8;
        } catch (Exception e) {

        }
        return frameSize;

        // usage
        // byte[] frame = new byte[frameSize];
        // camera.setPreviewCallbackWithBuffer(previewListener);
        // camera.addCallbackBuffer(frame);

    }

    public static int GetPreviewFrameSize(int previewFormat, int width, int height) {
        int frameSize = 0;
        try {
            int bitsPerPixel = ImageFormat.getBitsPerPixel(previewFormat);
            int bytesPerPixel = bitsPerPixel / 8;
            frameSize = ((width * height) * bitsPerPixel) / 8;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return frameSize;
    }

    public static ArrayList<String> BuildExposureCompensationList(int minIndex, int maxIndex, int evOneSteps, float step) {
        ArrayList<String> list = new ArrayList<>();

        int plusIndexes = maxIndex / evOneSteps;
        int minusIndexes = minIndex / evOneSteps;

        list.add("0");
        for (int i = plusIndexes; i > 0 ; i--) {
            list.add(String.valueOf(i));
        }
        for(int i = -1; i >= minusIndexes; i--) {
            list.add(String.valueOf(i));
        }

        return list;
    }

    public static void UpdateExposureCompensation(int compensationIndex) {
        if (MyCameraPreview.AppCamera == null) {
            return;
        }
        try {
            Camera.Parameters params = MyCameraPreview.AppCamera.getParameters();
            params.setExposureCompensation(compensationIndex);
            MyCameraPreview.AppCamera.setParameters(params);
            MyCameraPreview.AppCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void UpdateColorEffect(String value) {
        if (AppShared.ColorEffectList == null) {
            return;
        }
        try {
            Camera.Parameters params = MyCameraPreview.AppCamera.getParameters();
            params.setColorEffect(value);
            MyCameraPreview.AppCamera.setParameters(params);
            MyCameraPreview.AppCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    public static void SetCameraParameter(Context context, Camera camera, SharedPreferences pref, boolean forVideo) {
        // request the preview size, the hardware may not honor it,
        // if we depended on it we would have to query the size again
        if (camera == null) {
            return;
        }

        try {
            camera.cancelAutoFocus();
            Camera.Parameters param = camera.getParameters();
            int sdk = Integer.parseInt(Build.VERSION.SDK);
            if (sdk >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                if (MyCameraPreview.CameraId != MyCameraPreview.FrontCamId) {
                    param.setRecordingHint(true);
                }

            }            // Set picture size parameter.

            String pictureSize = "1024x768";
            List<Camera.Size> pictureSizes = param.getSupportedPictureSizes();

            if ((MyCameraPreview.HasFrontCam && !MyCameraPreview.HasRearCam) || MyCameraPreview.CameraId > 0) {
                //pictureSize = "640x480";
                param.set(AppShared.PARM_PICTURE_SIZE, pictureSize);
            } else {
                param.set(AppShared.PARM_PICTURE_SIZE, pictureSize);
            }

            // Set JPEG quality parameter.
            String jpegQuality = "95";
            param.set(AppShared.PARM_JPEG_QUALITY, jpegQuality);

            // Set Flash mode paremeter
            String flashMode = "auto";
            List<String> flashModes = param.getSupportedFlashModes();
            if ((MyCameraPreview.HasFrontCam && !MyCameraPreview.HasRearCam) || MyCameraPreview.CameraId > 0) {
                flashMode = "off";
            } else {
                if (flashModes.indexOf(flashMode) < 0) {
                    param.setFlashMode(flashModes.get(0));
                } else {
                    param.setFlashMode(flashMode);
                }
            }

            // Set Focuse mode paremeter
            String focusMode = "auto";  //pref.getString(AppShared.KEY_FOCUS_MODE, "auto");

            List<String> focusModes = param.getSupportedFocusModes();
            if (focusModes.size() > 0) {
                if (focusModes.indexOf(focusMode) < 0) {
                    if (Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.GINGERBREAD && forVideo) {
                        param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    } else {
                        param.setFocusMode(focusModes.get(0));
                    }
                } else {
                    param.setFocusMode(focusMode);
                }
            }

            // Set Preview Size
            String previewSize = "640x480";     //"320x240"; //pref.getString(AppShared.KEY_PREVIEW_SIZE,"352x288");

            if (previewSize != null && previewSize.length() > 3) {
                String[] sizeValue = previewSize.split("x");
                int cW = Integer.parseInt(sizeValue[0]);
                int cH = Integer.parseInt(sizeValue[1]);
                param.setPreviewSize(cW, cH);

                MyCameraPreview.PreviewWidth = cW;
                MyCameraPreview.PreviewHeight = cH;
                MyCameraPreview.PreviewDataInt = new int[cW * cH];

            }

            camera.setParameters(param);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Log.i("DBG", "setCameraParameter()");
    }

     public static String GetColorEffectListString() {
        String ret = "";
        try {
            if (AppShared.ColorEffectList == null) {
                AppShared.ColorEffectCurrent = AppShared.EFFECT_NONE;
                ret = "false," + AppShared.ColorEffectCurrent + ",";
            } else {
                ret = "true," + AppShared.ColorEffectCurrent + ",";
                for (int i = 0; i < AppShared.ColorEffectList.size(); i++) {
                    ret += AppShared.ColorEffectList.get(i);
                    if (i < AppShared.ColorEffectList.size() - 1) {
                        ret += "|";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ret.length() < 1) {
            ret = "false, none, 0";
        }
        return  ret;
    }

//    public static boolean SetCameraPictureSize(Context context, Camera camera,
//                                               SharedPreferences pref, String pictureSize) {
//        if (camera == null) {
//            return false;
//        }
//        boolean retValue = false;
//        try {
//            Util.SavePreferenceString(pref, AppCameraSettings.KEY_PICTURE_SIZE,
//                    pictureSize);
//            Camera.Parameters param = camera.getParameters();
//            param.set(AppShared.PARM_PICTURE_SIZE, pictureSize);
//            camera.setParameters(param);
//            camera.startPreview();
//            retValue = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            retValue = false;
//        }
//        return retValue;
//    }

//    public static boolean SetCameraJpegQuality(Context context, Camera camera,
//                                               SharedPreferences pref, String jpegQuality) {
//        if (camera == null) {
//            return false;
//        }
//        boolean retValue = false;
//        try {
//            Util.SavePreferenceString(pref, AppCameraSettings.KEY_JPEG_QUALITY,
//                    jpegQuality);
//            Camera.Parameters param = camera.getParameters();
//            param.set(AppShared.PARM_JPEG_QUALITY, jpegQuality);
//            camera.setParameters(param);
//            camera.startPreview();
//            retValue = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            retValue = false;
//        }
//        return retValue;
//    }

//    public static boolean SetCameraPreviewSize(Context context, Camera camera,
//                                               SharedPreferences pref, String previewSize) {
//        if (camera == null) {
//            return false;
//        }
//        boolean retValue = false;
//        try {
//            Util.SavePreferenceString(pref, AppCameraSettings.KEY_PREVIEW_SIZE,
//                    previewSize);
////            camera.stopPreview();
////            Camera.Parameters param = camera.getParameters();
//////            String size = pref.getString(
//////                            AppCameraSettings.KEY_PREVIEW_SIZE,
//////                            "352x288");
////            if (previewSize != null && previewSize.length() > 3) {
////                String[] sizeValue = previewSize.split("x");
////                int cW = Integer.parseInt(sizeValue[0]);
////                int cH = Integer.parseInt(sizeValue[1]);
////                param.setPreviewSize(cW, cH);
////            }
////            camera.setParameters(param);
////
////            PreviewDataInt = new int[getWidth() * getHeight()];
////
////
////            camera.startPreview();
//            retValue = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            retValue = false;
//        }
//        return retValue;
//    }

//    public static boolean SetCameraPreviewQuality(Context context, Camera camera,
//                                               SharedPreferences pref, String previewQuality) {
//        if (camera == null) {
//            return false;
//        }
//        boolean retValue = false;
//        try {
//            Util.SavePreferenceString(pref, AppCameraSettings.KEY_PREVIEW_QUALITY, previewQuality);
//            AppHandlers.PreviewQualityPref = previewQuality;
//            retValue = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            retValue = false;
//        }
//        return retValue;
//    }

//    public static boolean SetCameraVideoSize(Context context, Camera camera,
//                                             SharedPreferences pref, String value) {
//        if (camera == null) {
//            return false;
//        }
//        boolean retValue = false;
//        try {
//            Util.SavePreferenceString(pref, AppCameraSettings.KEY_VIDEO_SIZE, value);
//            //Camera.Parameters param = camera.getParameters();
//            retValue = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            retValue = false;
//        }
//        return retValue;
//    }

//    public static boolean SetCameraVideoDuration(Context context, Camera camera,
//                                                 SharedPreferences pref, String value) {
//        if (camera == null) {
//            return false;
//        }
//        boolean retValue = false;
//        try {
//            Util.SavePreferenceString(pref, AppCameraSettings.KEY_VIDEO_DURATION, value);
//            retValue = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            retValue = false;
//        }
//        return retValue;
//    }
//    public static boolean SetCameraFlashMode(Context context, Camera camera,
//                                             SharedPreferences pref, String flashMode) {
//        if (camera == null) {
//            return false;
//        }
//        boolean retValue = false;
//        try {
//            Util.SavePreferenceString(pref, AppCameraSettings.KEY_FLASH_MODE, flashMode);
//            Camera.Parameters param = camera.getParameters();
//            param.setFlashMode(flashMode);
//            camera.setParameters(param);
//            camera.startPreview();
//            retValue = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            retValue = false;
//        }
//        return retValue;
//    }

//    public static boolean SetCameraFocusMode(Context context, Camera camera,
//                                             SharedPreferences pref, String focusMode) {
//        if (camera == null) {
//            return false;
//        }
//        boolean retValue = false;
//        try {
//            Util.SavePreferenceString(pref, AppCameraSettings.KEY_FOCUS_MODE, focusMode);
//            Camera.Parameters param = camera.getParameters();
//            param.setFocusMode(focusMode);
//            camera.setParameters(param);
//            camera.startPreview();
//            retValue = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            retValue = false;
//        }
//        return retValue;
//    }

    public static boolean isZooming = false;

//    public static int SetCameraZoom(Context context, Camera camera, String zoomMode) {
//        if (camera == null) {
//            return 0;
//        }
//        try {
//            // isZoomSupported()
//            // getMaxZoom();
//            // setZoom(int)
//            // List<Integer> getZoomRatios ()
//            // the zoom ratios in 1/100 increments.
//            // Ex: a zoom of 3.2x is returned as 320.
//            // The number of elements is getMaxZoom() + 1.
//            // The list is sorted from small to large.
//            // The first element is always 100. The last element is the zoom
//            // ratio of the maximum zoom value.
//
//            // Set zoom to 2x if available. This helps encourage the user to
//            // pull back.
//            // Some devices like the Behold have a zoom parameter
//            // parameters.set("zoom", "2.0");
//            // Most devices, like the Hero, appear to expose this zoom
//            // parameter.
//            // (I think) This means 2.0x
//            // parameters.set("taking-picture-zoom", "20");
//
//            if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
//                return 0;
//            }
//            if (!AppShared.gCameraZoomEnabled) {
//                return 0;
//            }
//
//            if (isZooming) {
//                return 0;
//            }
//            isZooming = true;
//
//            Camera.Parameters param = camera.getParameters();
//            if (AppShared.gVideoStatus != AppShared.VIDEO_STATUS_RECORDING) {
//                //camera.stopPreview();
//            }
//
//            ZoomSupported = param.isZoomSupported();
//            if (ZoomSupported) {
//
//                if (AppShared.gCameraZoomValues == null) {
//                    AppShared.gCameraZoomValues = param.getZoomRatios();
//                }
//                AppShared.gCameraZoomMax = param.getMaxZoom();
//
//                //Toast.makeText(context, "MaxZoom:" + String.valueOf(ZoomMax) + ", " + "ZoomRatio:" + String.valueOf(ZoomRatio.size()), Toast.LENGTH_LONG).show();
//
//
//                //int zoom = AppShared.MESSAGE_ZOOMDEFAULT_REQUEST;
//                try {
//                    AppShared.gCameraZoomValue = Integer.parseInt(zoomMode);
//                } catch (NumberFormatException ne) {
//                    return AppShared.gCameraZoomValue;
//                }
//
//                int ZoomCurrent = param.getZoom();
//                if (ZoomCurrent > AppShared.gCameraZoomMax) ZoomCurrent = AppShared.gCameraZoomMax;
//
////                int prevZoom = 0;
////                int nextZoom = 100;
////                int tempZoom = 100;
////
////                if (zoom == AppShared.MESSAGE_ZOOMIN_REQUEST) {
////                    ZoomCurrent += 1;
////                    if (ZoomCurrent > ZoomMax) ZoomCurrent = ZoomMax;
////                } else if (zoom == AppShared.MESSAGE_ZOOMOUT_REQUEST) {
////                    ZoomCurrent -= 1;
////                    if (ZoomCurrent < 0) ZoomCurrent = 0;
////                }
//                boolean zoomFound = true;
////                for (int zoom : AppShared.gCameraZoomValues) {
////                    if (zoom == AppShared.gCameraZoomValue) {
////                        zoomFound = true;
////                        break;
////                    }
////                }
//                if (AppShared.gCameraZoomValue > AppShared.gCameraZoomValues.size()) {
//                    AppShared.gCameraZoomValue = 0;
//                }
//                boolean smoothZoom = param.isSmoothZoomSupported();
//                if (zoomFound) {
//                    if (smoothZoom) {
//                        //camera.startPreview();
//                        camera.startSmoothZoom(AppShared.gCameraZoomValue);
//                    } else {
//                        param.setZoom(AppShared.gCameraZoomValue);
//                    }
//                    Util.SavePreferenceString(AppShared.gPreferences, AppCameraSettings.KEY_ZOOM_VALUE, String.valueOf(AppShared.gCameraZoomValue));
//                } else {
//                    AppShared.gCameraZoomValue = 0;
//                    if (smoothZoom) {
//                        camera.startPreview();
//                        camera.startSmoothZoom(AppShared.gCameraZoomValue);
//                    } else {
//                        param.setZoom(AppShared.gCameraZoomValue);
//                    }
//                    Util.SavePreferenceString(AppShared.gPreferences, AppCameraSettings.KEY_ZOOM_VALUE, String.valueOf(AppShared.gCameraZoomValue));
//                }
//
//            }
//            // Log.i("DBG", "Zoom Current: " + String.valueOf(ZoomCurrent) +
//            // ", Zoom Max: " + String.valueOf(ZoomMax));
////			String zr = "";
////			for (int i = 0; i < ZoomRatio.size(); i += 1) {
////				zr += String.valueOf(ZoomRatio.get(i)) + ",";
////			}
////			ShowBadValueMessage(context, "DEBUG", "MaxZoom:" + String.valueOf(ZoomMax)
////					+ ", " + "ZoomRatio Size:" + String.valueOf(ZoomRatio.size())
////					+ ", " + "Zoom Current:" + String.valueOf(ZoomCurrent)
////					+ ", " + "ZoomRatios:" + zr);
//
//            camera.setParameters(param);
//            if (AppShared.gVideoStatus != AppShared.VIDEO_STATUS_RECORDING) {
//                //camera.startPreview();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            //return ZoomCurrent;
//            return AppShared.gCameraZoomValue;
//        } finally {
//            isZooming = false;
//        }
//
//        //return ZoomCurrent;
//        return AppShared.gCameraZoomValue;
//    }
//
//    public static boolean SetCameraZoomToggle(Context context, Camera camera, String notUsedValue) {
//        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
//            return false;
//        }
//
//        try {
//
//            boolean currentValue = AppShared.gPreferences.getBoolean(AppCameraSettings.KEY_ZOOM_TOGGLE, true);
//            AppShared.gCameraZoomEnabled = !currentValue;
//            Util.SavePreferenceBoolean(AppShared.gPreferences, AppCameraSettings.KEY_ZOOM_TOGGLE, AppShared.gCameraZoomEnabled);
//
//
//            // try to refresh the camera preview
//            Camera.Parameters param = camera.getParameters();
//            if (AppShared.gVideoStatus != AppShared.VIDEO_STATUS_RECORDING) {
//                //camera.stopPreview();
//            }
//
//            ZoomSupported = param.isZoomSupported();
//            if (ZoomSupported) {
//
//                int ZoomCurrent = param.getZoom();
//                AppShared.gCameraZoomValue = ZoomCurrent;
//                boolean smoothZoom = param.isSmoothZoomSupported();
//
//                if (smoothZoom) {
//                    //camera.startPreview();
//                    camera.startSmoothZoom(AppShared.gCameraZoomValue);
//                } else {
//                    param.setZoom(AppShared.gCameraZoomValue);
//                }
//
//            } else {
//                AppShared.gCameraZoomEnabled = false;
//                Util.SavePreferenceBoolean(AppShared.gPreferences, AppCameraSettings.KEY_ZOOM_TOGGLE, AppShared.gCameraZoomEnabled);
//            }
//
//            camera.setParameters(param);
//            if (AppShared.gVideoStatus != AppShared.VIDEO_STATUS_RECORDING) {
//                //camera.startPreview();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            //return ZoomCurrent;
//            return AppShared.gCameraZoomEnabled;
//        }
//
//        return AppShared.gCameraZoomEnabled;
//    }

    /**
     * Creates a media file in the {@code Environment.DIRECTORY_PICTURES} directory. The directory
     * is persistent and available to other applications like gallery.
     *
     * @param type Media type. Can be video or image.
     * @return A file object pointing to the newly created file.
     */
    public  static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return  null;
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraSample");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()) {
                //Log.d("CameraSample", "failed to create directory");
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

    public static void ReleaseCamera() {
//        if (AppShared.AppPreview == null) {
//            return;
//        }
//        if (AppShared.AppPreview.AppCamera != null) {
//            // mCamera.reconnect();
//            AppShared.AppPreview.AppCamera.setPreviewCallback(null);
//            AppShared.AppPreview.AppCamera.stopPreview();
//            AppShared.AppPreview.AppCamera.release();
//            AppShared.AppPreview.AppCamera = null;
//        }
    }

    private static class DismissAutofocus implements Runnable {
        public void run() {
//            if (AutofocusInAction && !CancelPhotoTake) {
//                AppShared.AppPreview.AppCamera.cancelAutoFocus();
//                AppShared.AppPreview.AppCamera.takePicture(
//                        AppShared.AppPreview.shutterCallback,
//                        AppShared.AppPreview.rawCallback,
//                        AppShared.AppPreview.jpegCallback);
//                AutofocusInAction = false;
//            }
        }
    }

//    public static void SavePhoto() {
//        try {
//            // Log.i("DBG", "Request Photo Take: SavePhoto");
//            // Message errMessage = previewHandler.obtainMessage(previewMessage,
//            // AppShared.MESSAGE_ERROR_PHOTO, -1);
//            if (CancelPhotoTake) {
//                Message errMessage = AppHandlers.CameraHandler.obtainMessage(
//                        AppShared.MESSAGE_PHOTO_TAKE, AppShared.MESSAGE_ERROR_PHOTO, -1);
//                errMessage.sendToTarget();
//                return;
//            }
//            if (!Util.CheckSDCardIsAvailable()) {
//                Message errMessage = AppHandlers.CameraHandler.obtainMessage(
//                        AppShared.MESSAGE_PHOTO_TAKE, AppShared.MESSAGE_ERROR_PHOTO, -1);
//                errMessage.sendToTarget();
//                // previewHandler = null;
//                return;
//            }
//
//            boolean success = false;
//            Date dt = new Date();
//            String todayFolder = DateFormat.format("yyyy-MM-dd", dt).toString();
//
//            success = Util.CheckAndCreateSubFolder(AppShared.gRootFolder, todayFolder);
//
//            if (!success) {
//                Message errMessage = AppHandlers.CameraHandler.obtainMessage(
//                        AppShared.MESSAGE_PHOTO_TAKE, AppShared.MESSAGE_ERROR_PHOTO, -1);
//                errMessage.sendToTarget();
//                // previewHandler = null;
//                return;
//            }
//
//            String filePath = AppShared.gRootFolder + todayFolder + "/"
//                    + String.valueOf(System.currentTimeMillis()) + ".jpg";
//            FileOutputStream out = new FileOutputStream(filePath);
//
//            if (NewPhotoImage != null) {
//                success = NewPhotoImage.compress(Bitmap.CompressFormat.JPEG,
//                        95, out);
//                AppShared.gLastPhotoPath = filePath;
//            }
//            if (NewPhotoImage != null && !NewPhotoImage.isRecycled()) {
//                NewPhotoImage.recycle();
//                NewPhotoImage = null;
//            }
//            out.close();
//            out = null;
//            Uri uri = Uri.fromFile(new File(filePath));
//            AppHandlers.HandlerContext.sendBroadcast(new Intent("com.android.camera.NEW_PICTURE", uri));
//
//            // Save Exif Gps Location (Oct. 26, 2014)
//            if (AppShared.gSaveLocation) {
//                SavePhotoExifLocationData(AppShared.gCurrentLocation, filePath);
//            }
//
//            Message message = AppHandlers.CameraHandler.obtainMessage(
//                    AppShared.MESSAGE_PHOTO_TAKE, AppShared.MESSAGE_PHOTO_SAVED, -1);
//            message.sendToTarget();
//
//            SaveResult = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//
//            if (AppShared.AppPreview.AppCamera != null) {
//                boolean connected = AppHandlers.IsRemoteConnected(AppCameraMode.BluetoothService, AppCameraMode.WifiService);
//                MyCameraPreview.AutoFeed = connected;
//                AppShared.AppPreview.AppCamera.startPreview();
//            }
//
//            System.gc();
//        }
//    }
//
//    public static void SavePhotoExifLocationData(Location location, String filePath) {
//        try {
//            if (location == null) {
//                return;
//            }
//
//            ExifInterface exif = new ExifInterface(filePath);
//
//            double lat = location.getLatitude();
//            double lng = location.getLongitude();
//            double alt = 0;
//            if (location.hasAltitude()) {
//                alt = location.getAltitude();
//
//            }
//
////            int lat1 = (int)Math.floor(lat);
////            int lat2 = (int)Math.floor((lat - lat1) * 60);
////            double lat3 = (lat - ((double)lat1 + ((double)lat2/60))) * 3600000;
////
////            int lng1 = (int)Math.floor(lng);
////            int lng2 = (int)Math.floor((lng - lng1) * 60);
////            double lng3 = (lng - ((double)lng1 + ((double)lng2/60))) * 3600000;
//
//            String latRef = (lat > 0 ? "N" : "S");
//            String lngRef = (lng > 0 ? "E" : "W");
//
////            String latValue = String.valueOf(lat1) + "/1,"
////                    + String.valueOf(lat2) + "/1,"
////                    + String.valueOf(lat3) + "/1000";
////            String lngValue = String.valueOf(lng1) + "/1,"
////                    + String.valueOf(lng2) + "/1,"
////                    + String.valueOf(lng3) + "/1000";
//
//            String latValue = ConvertToDms(lat);
//            String lngValue = ConvertToDms(lng);
//
//            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latValue);
//            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latRef);
//
//            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, lngValue);
//            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, lngRef);
//
//            String time = String.valueOf(location.getTime()/1000);
//            exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, time);
//
//            exif.saveAttributes();
//        } catch (Exception e) {
//        }
//    }

    public static String ConvertToDms(double value) {
        String ret = "";

        try {
            value = Math.abs(value);
            int degree = (int) value;
            value *= 60;
            value -= (degree * 60.0d);
            int minute = (int) value;
            value *= 60;
            value -= (minute * 60.0d);
            int second = (int)(value * 1000.0d);

            ret = String.valueOf(degree) + "/1,";
            ret += String.valueOf(minute) + "/1,";
            ret += String.valueOf(second) + "/1000,";
        } catch (Exception e) {
        }


        return ret;
    }

//    public static void SaveCameraInformation(Camera.Parameters parameters, int cameraOrientation, int jpegRotation) {
//        try {
//            String path = AppShared.RootFolder + "camera_parameters_" + String.valueOf(System.currentTimeMillis()) + ".txt";
//            String contents = "Camera Parameters:\r\n";
//
//            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
//            contents += "Supported Preview Sizes:\r\n";
//            for (Camera.Size size : sizes) {
//                contents += String.valueOf(size.width) + "x" + String.valueOf(size.height) + ",";
//            }
//            contents += "\r\n";
//
//            contents += "Preview Size:\r\n";
//            contents += String.valueOf(MyCameraPreview.PreviewWidth) + "x" + String.valueOf(MyCameraPreview.PreviewHeight);
//            contents += "\r\n";
//
//            List<Integer> formats = parameters.getSupportedPreviewFormats();
//            contents += "Supported Preview Formats:\r\n";
//            for (Integer format : formats) {
//                if (format == ImageFormat.YUV_420_888) { contents += "yuv_420_888,"; }
//                else if (format == ImageFormat.YUV_422_888) { contents += "yuv_422_888,"; }
//                else if (format == ImageFormat.YUV_444_888) { contents += "yuv_444_888,"; }
//                else if (format == ImageFormat.NV16) { contents += "nv16,"; }
//                else if (format == ImageFormat.NV21) { contents += "nv21,"; }
//                else if (format == ImageFormat.YV12) { contents += "yv12,"; }
//                else { contents += String.valueOf(format) + ","; }
//            }
//            contents += "\r\n";
//
//            int formatInt = parameters.getPreviewFormat();
//            contents += "Preview Format:\r\n";
//            if (formatInt == ImageFormat.YUV_420_888) { contents += "yuv_420_888,"; }
//            else if (formatInt == ImageFormat.YUV_422_888) { contents += "yuv_422_888,"; }
//            else if (formatInt == ImageFormat.YUV_444_888) { contents += "yuv_444_888,"; }
//            else if (formatInt == ImageFormat.NV16) { contents += "nv16,"; }
//            else if (formatInt == ImageFormat.NV21) { contents += "nv21,"; }
//            else if (formatInt == ImageFormat.YV12) { contents += "yv12,"; }
//            else { contents += String.valueOf(formatInt) + ","; }
//            contents += "\r\n";
//
//            contents += "Camera Orientation, Jpeg Rotation:\r\n";
//            contents += String.valueOf(cameraOrientation) + ", " + String.valueOf(jpegRotation) + "\r\n";
//            contents += "\r\n";
//
//            Util.SaveToFile(path, contents);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public static StringBuilder previewInformation = new StringBuilder();
//    public static void AddPreviewInformation(String information) {
//        try {
//            if (previewInformation == null) { previewInformation = new StringBuilder(); }
//            previewInformation.append(String.valueOf(System.currentTimeMillis() + ": " + information + "\r\n"));
//        } catch (Exception e) {
//            e.printStackTrace();
//            previewInformation.append(String.valueOf(System.currentTimeMillis() + ": error occurred.\r\n"));
//        }
//    }
//
//    public static void SavePreviewInformation(String mode) {
//        try {
//            String path = AppShared.RootFolder + "preview_information_" + mode + "_" + String.valueOf(System.currentTimeMillis()) + ".txt";
//            Util.SaveToFile(path, previewInformation.toString());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}

