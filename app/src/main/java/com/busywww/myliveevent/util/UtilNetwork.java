package com.busywww.myliveevent.util;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.ListView;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by BusyWeb on 8/29/2014.
 */
public class UtilNetwork {

    private static final int MIN_PORT_NUMBER = 8100;
    private static final int MAX_PORT_NUMBER = 65535;



    public static boolean IsOnline(Context ctx) {
        try {
            ConnectivityManager cm = (ConnectivityManager) ctx
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            // return cm.getActiveNetworkInfo().isConnectedOrConnecting();
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null && ni.isConnected()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean IsWifiAvaiable(Context context) {
        boolean retValue = false;
        try {
            ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //mobile
            //State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

            //wifi
            NetworkInfo.State wifi = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
                retValue = true;
            }
            //Boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return retValue;
    }

//    public static String GetWifiIpAddress2(Context context) {
//        String retValue = "127.0.0.1";
//        try {
//            WifiManager myWifiManager = (WifiManager) context
//                    .getSystemService(Context.WIFI_SERVICE);
//
//            WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
//            int myIp = myWifiInfo.getIpAddress();
//            //Log.i("DBG", String.valueOf(myIp));
//
//            retValue = android.text.format.Formatter.formatIpAddress(myIp);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return retValue;
//    }

//    public static String GetWifiIpAddress(Context context) {
//        String retValue = "127.0.0.1";
//        try {
//            WifiManager myWifiManager = (WifiManager) context
//                    .getSystemService(Context.WIFI_SERVICE);
//
//            WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
//            int myIp = myWifiInfo.getIpAddress();
//            //Log.i("DBG", String.valueOf(myIp));
//
//            int intMyIp3 = myIp / 0x1000000;
//            int intMyIp3mod = myIp % 0x1000000;
//
//            int intMyIp2 = intMyIp3mod / 0x10000;
//            int intMyIp2mod = intMyIp3mod % 0x10000;
//
//            int intMyIp1 = intMyIp2mod / 0x100;
//            int intMyIp0 = intMyIp2mod % 0x100;
//
//            retValue = String.valueOf(intMyIp0) + "."
//                    + String.valueOf(intMyIp1) + "." + String.valueOf(intMyIp2)
//                    + "." + String.valueOf(intMyIp3);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return retValue;
//    }

//    public static String GetLocalIpAddressString() {
//        try {
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//                NetworkInterface intf = en.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
//                        return inetAddress.getHostAddress().toString();
//
////                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
////                        if (isIPv4 && intf.getDisplayName().startsWith("wlan")) {
////                            return sAddr;
////                        }
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            //Log.e("IPADDRESS", ex.toString());
//        }
//        return null;
//    }

    private static int mFreePort = 0;
    public static int GetSocketFreePort() {
        //int port = mFreePort;
        try {
            ServerSocket server = new ServerSocket(0);
            if (mFreePort == 0) {
                mFreePort = server.getLocalPort();
            } else {
                boolean p = IsPortAvailable(mFreePort);
                if (!p) {
                    mFreePort = server.getLocalPort();
                }
            }
            server.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mFreePort;
    }

    public static boolean IsPortAvailable(int port) {
        if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
					/* should not be thrown */
                }
            }
        }
        return false;
    }

}
