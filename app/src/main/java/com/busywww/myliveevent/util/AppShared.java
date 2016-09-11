package com.busywww.myliveevent.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.Display;
import android.view.Surface;

import com.busywww.myliveevent.classes.YouTubeEventData;
import com.google.android.gms.common.Scopes;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTubeScopes;

import java.util.List;

/**
 * Created by BusyWeb on 12/6/2015.
 */
public class AppShared {

    public static final String ACCOUNT_KEY = "AIzaSyAqAIoUaUFGuaBM1HReVw4gt7EYyPC2lTE";
    public static final String APP_NAME = "try2";

    public static final String[] SCOPES = {Scopes.PROFILE, YouTubeScopes.YOUTUBE};
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    public static final int REQUEST_GMS_ERROR_DIALOG = 1;
    public static final int REQUEST_ACCOUNT_PICKER = 2;
    public static final int REQUEST_AUTHORIZATION = 3;
    public static final int REQUEST_STREAMER = 4;


    public static Activity gActivity = null;
    public static Context gContext = null;
    public static Resources gResources = null;
    public static SharedPreferences gPreferences = null;

    public static boolean ShowAdView = true;
    public static final String FullScreenAdId = "ca-app-pub-0882328384926881/6581515690";
    public static final String AdId = "ca-app-pub-0882328384926881/2151316093";

    public static final int AdActionNone = -1;
    public static final int AdActionMain = 1;
    public static final int AdActionSplash = 2;
    public static final int AdActionView = 3;
    public static final int AdActionGoMain = 4;
    public static int AdAction = AdActionNone;


    public final static HttpTransport AppHttpTransport = AndroidHttp.newCompatibleTransport();
    public final static JsonFactory AppJsonFactory = new GsonFactory();
    public static GoogleAccountCredential AccountCredential;
    public static YouTubeEventData SelectedEvent;

    public static Display gDisplay = null;
    public static Display display = null;
    public static boolean gDeviceUpsideDown = false;
    public static int gRotation = Surface.ROTATION_90;
    public static int gDegrees = 0;
    public static int gOrientation = Surface.ROTATION_90;       //Configuration.ORIENTATION_LANDSCAPE;
    public static boolean gDataProcessing = false;
    public static int DisplayWidth;
    public static int DisplayHeight;

    public static String EventStatusForList = "upcoming";

    // continuous focusing mode
    public static final String PREF_CONTIUOUS_FOCUSMODE_KEY = "pref_continuous_focusmode_key";
    public static final int CONTIUOUS_FOCUSMODE_NONE = 1;
    public static final int CONTIUOUS_FOCUSMODE_FAST = 2;
    public static final int CONTIUOUS_FOCUSMODE_SLOW = 3;
    public static int ContinuousFocusing = CONTIUOUS_FOCUSMODE_FAST;

    // The parameter strings to communicate with camera driver.
    public static final String PARM_PICTURE_SIZE = "picture-size";
    public static final String PARM_JPEG_QUALITY = "jpeg-quality";
    public static final String PARM_ROTATION = "rotation";
    public static final String PARM_GPS_LATITUDE = "gps-latitude";
    public static final String PARM_GPS_LONGITUDE = "gps-longitude";
    public static final String PARM_GPS_ALTITUDE = "gps-altitude";
    public static final String PARM_GPS_TIMESTAMP = "gps-timestamp";
    public static final String SUPPORTED_ZOOM = "zoom-values";
    public static final String SUPPORTED_PICTURE_SIZE = "picture-size-values";
    public static final String SUPPORTED_FLASHMODE = "flash-mode";
    public static final String SUPPORTED_FOCUSMODE = "focus-mode";
    public static final String SUPPORTED_VIDEO_SIZE = "video-size-values";
    public static final String SUPPORTED_PREVIEW_SIZE = "preview-size-values";
    public static final String PARM_ZOOM = "zoom-value";
    public static final String PARM_UPLOAD_INTERVAL = "upload-interval";
    public static final String KEY_VIDEO_QUALITY = "pref_camera_videoquality_key";
    public static final String KEY_PICTURE_SIZE = "pref_camera_picturesize_key";
    public static final String KEY_JPEG_QUALITY = "pref_camera_jpegquality_key";
    public static final String KEY_FOCUS_MODE = "pref_camera_focusmode_key";
    //public static final boolean DEFAULT_VIDEO_QUALITY_VALUE = true;
    //public static final int DEFAULT_VIDEO_DURATION_VALUE = 1;  // 1 minute
    public static final String KEY_VIDEO_SIZE = "pref_camera_videosize_key";
    public static final String KEY_VIDEO_DURATION = "pref_camera_video_duration_key";
    public static final String KEY_FLASH_MODE = "pref_camera_flashmode_key";
    public static final String KEY_VIDEOCAMERA_FLASH_MODE = "pref_camera_video_flashmode_key";
    public static final String KEY_COLOR_EFFECT = "pref_camera_coloreffect_key";
    public static final String KEY_WHITE_BALANCE = "pref_camera_whitebalance_key";
    public static final String KEY_SCENE_MODE = "pref_camera_scenemode_key";
    public static final String KEY_QUICK_CAPTURE = "pref_camera_quickcapture_key";
    public static final String KEY_EXPOSURE = "pref_camera_exposure_key";
    public static final String KEY_PREVIEW_QUALITY = "pref_camera_previewquality_key";
    public static final String KEY_PREVIEW_SIZE = "pref_camera_previewsize_key";
    public static final String KEY_ZOOM_VALUE = "pref_camera_zoomvalue_key";
    public static final String KEY_ZOOM_TOGGLE = "pref_camera_zoomtoggle_key";
    public static final String KEY_SAVE_LOCATION = "pref_camera_recordlocaiton_key";

    // Color Effects
    // Values for color effect settings.
    public static final String EFFECT_NONE = "none";
    public static final String EFFECT_MONO = "mono";
    public static final String EFFECT_NEGATIVE = "negative";
    public static final String EFFECT_SOLARIZE = "solarize";
    public static final String EFFECT_SEPIA = "sepia";
    public static final String EFFECT_POSTERIZE = "posterize";
    public static final String EFFECT_WHITEBOARD = "whiteboard";
    public static final String EFFECT_BLACKBOARD = "blackboard";
    public static final String EFFECT_AQUA = "aqua";
    public static final int MESSAGE_COLOR_EFFECT_REQUEST = 2105;
    public static final int MESSAGE_SET_COLOR_EFFECT = 2106;
    public static String ColorEffectCurrent = EFFECT_NONE;
    public static List<String> ColorEffectList = null;

}
