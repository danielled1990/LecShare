package com.busywww.myliveevent.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.TextView;

import com.busywww.myliveevent.R;
import com.busywww.myliveevent.classes.YouTubeEventData;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by BusyWeb on 12/6/2015.
 */
public class Helper {

    public static String GetSavedUserAccountName(Context context) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getString(AppShared.ACCOUNT_KEY, null);
    }

    public static void SaveUserAccountName(Context context, String accountName) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        sp.edit().putString(AppShared.ACCOUNT_KEY, accountName).apply();
    }

    public static Bitmap GetCircleImage(Context context, Bitmap source, int backgroundColor) {
        Bitmap bitmap = null;
        CircleImage circleImage = new CircleImage(context);
        try {
            bitmap = circleImage.transform(source, backgroundColor);
        } catch (Exception e) {
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_user_acount);
            bitmap = circleImage.transform(icon, backgroundColor);
        }
        return bitmap;
    }

    public static RoundedBitmapDrawable GetRoundBitmapDrawable(Resources resources, Bitmap source) {
        try {
            //Bitmap src = BitmapFactory.decodeResource(res, iconResource);
            RoundedBitmapDrawable dr =
                    RoundedBitmapDrawableFactory.create(resources, source);
            //dr.setCornerRadius(Math.max(source.getWidth(), source.getHeight()) / 2.0f);
            dr.setCircular(true);
            return  dr;
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }

//    public static void ActionShareLiveEvent(Context context, YouTubeEventData eventData) {
//        try {
////            File shareFile = new File(AppShared.SelectedFilePath);
////            Uri uri = Uri.fromFile(shareFile);
////            //String mime = Util.GetFileMimeType(mContext, uri);
////            String mime = Util.GetFileMimeType2(uri.toString());
////            Intent intent = new Intent(Intent.ACTION_SEND);
////            intent.putExtra(Intent.EXTRA_SUBJECT, "Share Live Event");
////            intent.putExtra(Intent.EXTRA_STREAM, uri);
////            if (mime != null && mime.length() > 0) {
////                intent.setType(mime);
////                context.startActivity(Intent.createChooser(intent, "Share"));
////            }
//
//            String eventUri = eventData.GetWatchUri();
//
//            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt");
//            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Live Event Share");
//            intent.putExtra(Intent.EXTRA_TEXT, eventUri);
//            intent.setType(mimeType);
//
//            context.startActivity(Intent.createChooser(intent, "Send Live Event Link"));
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }



    public static void LoadDisplayWidthHeight2(Activity activity) {
        try {
            View decorView = null;
            if (activity != null) {
                decorView = activity.getWindow().getDecorView();
            }
            int w, h;
            if (decorView != null && decorView.getWidth() > 0 && decorView.getHeight() > 0) {
                w = decorView.getWidth(); // - Util.GetDisplayPixel(activity, 20);
                h = decorView.getHeight(); // = display.getHeight() - 150;
            } else {
                w = AppShared.display.getWidth(); // - Util.GetDisplayPixel(activity, 20);
                h = AppShared.display.getHeight(); // = display.getHeight() - 150;
            }
            AppShared.DisplayWidth = w;
            AppShared.DisplayHeight = h;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void LoadDeviceRotation(Activity activity) {
        try {
            AppShared.gDegrees = 0;
            AppShared.gRotation = 1;
            Configuration c = activity.getResources().getConfiguration();
            //AppShared.gOrientation = c.orientation;
            if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
                if (c.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    AppShared.gRotation = 1;
                    AppShared.gDegrees = 90;
                    AppShared.gDeviceUpsideDown = false;
                } else {
                    AppShared.gDegrees = 0;
                    AppShared.gRotation = 0;
                    AppShared.gDeviceUpsideDown = false;
                }
                return;
            }

            if (c.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                AppShared.gOrientation = c.orientation;
            } else {
                AppShared.gOrientation = c.orientation;
            }
            AppShared.gRotation = activity.getWindowManager().getDefaultDisplay().getRotation();

            switch (AppShared.gRotation) {
                case Surface.ROTATION_0:
                    AppShared.gDegrees = 0;
                    AppShared.gDeviceUpsideDown = false;
                    break;
                case Surface.ROTATION_90:
                    AppShared.gDegrees = 90;
                    AppShared.gDeviceUpsideDown = false;
                    break;
                case Surface.ROTATION_180:
                    AppShared.gDegrees = 180;
                    AppShared.gDeviceUpsideDown = false;
                    break;
                case Surface.ROTATION_270:
                    AppShared.gDegrees = 270;
                    AppShared.gDeviceUpsideDown = true;
                    break;
            }
            //AppShared.gOrientation = AppShared.gDegrees;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //System.gc();
        }
    }
    public static int GetImageRotation(Camera.CameraInfo info, int orientation) {
        int rotation = 0;
        try {
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return 0;
//             android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
//             android.hardware.Camera.getCameraInfo(cameraId, info);
            orientation = (orientation + 45) / 90 * 90;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360;
            } else {  // back-facing camera
                rotation = (info.orientation + orientation) % 360;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //mParameters.setRotation(rotation);
        return rotation;
    }

    public static boolean CheckTcpPortOpen(final String ip, final int port, final int timeout) {
        // 80
        // 443
        // 1935
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            return true;
        }

        catch(ConnectException ce){
            ce.printStackTrace();
            return false;
        }

        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
    public static boolean CheckTcpPortOpen2(final String hostName, final int port, final int timeout) {
        // 80
        // 443
        // 1935
        try {
            String ip = GetHostIpAddress(hostName);
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            return true;
        }

        catch(ConnectException ce){
            ce.printStackTrace();
            return false;
        }

        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
    public static String GetHostIpAddress(String hostName) {
        String ip = "";
        try {
            InetAddress address = InetAddress.getByName(hostName);
            ip = address.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }

    private void temp() {
        boolean isAvailable = false;
        try {
            InetAddress address = InetAddress.getByName("www.example.com");
            //System.out.println(address.getHostAddress());
            isAvailable = InetAddress.getByName("11.11.11.11").isReachable(2000);
            if (isAvailable == true) {
                //host is reachable
                //doSomething();
            }
        } catch (Exception e) {

        }
    }

    public static int GetEventStatusIdByValue(String status) {
        int id = 0;
        try {

            if (status.equals("all")) {
                id = 0;
            } else if (status.equals("active")) {
                id = 0;
            } else if (status.equals("completed")) {
                id = 1;
            } else if (status.equals("upcoming")) {
                id = 2;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }
    public static String GetEventStatusValueById(int id) {
        String value = "";
        try {

//            if (id == 0) {
//                value = "all";
//            } else
//
            if (id == 0) {
                value = "active";
            } else if (id == 1) {
                value = "completed";
            } else if (id == 2) {
                value = "upcoming";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


    public static void InitMyAdView(final Context context, View adContainer) {
        TextView txtvwLink = (TextView) adContainer.findViewById(R.id.textViewLinkToApps);
        txtvwLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse("market://search?q=pub:\"Busy WWW\"")));
            }
        });
        ImageButton btnLink = (ImageButton) adContainer.findViewById(R.id.imgbtnLinkToApps);
        btnLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse("market://search?q=pub:\"Busy WWW\"")));
            }
        });
    }

}
