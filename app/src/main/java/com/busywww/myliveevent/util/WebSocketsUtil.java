package com.busywww.myliveevent.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * Created by Danielle on 8/13/2016.
 */
public class WebSocketsUtil {
    private WebSocketConnection mConnectionOnline = new WebSocketConnection();
    private WebSocketConnection mConnectionsendImage = new WebSocketConnection();
    private ArrayList<Observer> observers = new ArrayList<>();
    private URI uri;
    private int pageIndex;
    public void connectToWebSocketServer(){
        String url = "ws://192.168.1.15:8080/firstStreamingTry/online";
        final String TAG1 = "connectToWebSocketServer";
        try {
            this.uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        try {

            mConnectionOnline.connect(url, new WebSocketHandler() {

                @Override
                public void onOpen() {
                    Log.d("onopen", "Status: Connected to ");
                    JSONObject json = new JSONObject();
                    jsonPut(json,"type","streamer");
                    jsonPut(json,"name","client2");
                   // jsonPut(json,"success","false");
                    mConnectionOnline.sendTextMessage(json.toString());
                    Log.d(TAG1, "trying to send message");
                    // mConnection.sendBinaryMessage(byte2);
                }

                @Override
                public void onTextMessage(String payload) {
                    Log.d("ontext", "Got echo: " + payload);
                    JSONObject json = new JSONObject();
                    String type ="";
                    try {
                        json = new JSONObject(payload);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        type= json.getString("type");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(type.equals("connect")){
                        String name = "";
                        try {
                            name= json.getString("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(!name.equals("")){
                            notifyAllObservers(name);
                        }
                    }


                }


                });
            } catch (WebSocketException e) {
            e.printStackTrace();
        }


    }
    public void sendURL(String answer,String id,String name){
        JSONObject json = new JSONObject();
        jsonPut(json,"type","yes");
        jsonPut(json,"nameToconectTo",name);
        jsonPut(json,"name","client2");
        jsonPut(json,"videoId",id);
        // jsonPut(json,"success","false");
        mConnectionOnline.sendTextMessage(json.toString());
    }
    private static void jsonPut(JSONObject json, String key, Object value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public void attach(Observer observer){
        observers.add(observer);
    }
    private void notifyAllObservers(String name){
        for (Observer observer : observers) {
            observer.update(name);
        }
    }
    public void connectToImageWebSocketServer(){
        String url = "ws://192.168.1.15:8080/firstStreamingTry/image";
        final String TAG1 = "connectToImageWebSocketServer";
        try {
            this.uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        try {

            mConnectionsendImage.connect(url, new WebSocketHandler() {

                @Override
                public void onOpen() {
                    Log.d("onopen", "Status: Connected to ");
                    JSONObject json = new JSONObject();
                    jsonPut(json,"type","streamer");
                    jsonPut(json,"name","client2");
                    // jsonPut(json,"success","false");
                    mConnectionsendImage.sendTextMessage(json.toString());
                    Log.d(TAG1, "trying to send message");
                    // mConnection.sendBinaryMessage(byte2);
                }

                @Override
                public void onTextMessage(String payload) {
                 /*   Log.d("ontext", "Got echo: " + payload);
                    JSONObject json = new JSONObject();
                    String type ="";
                    try {
                        json = new JSONObject(payload);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        type= json.getString("type");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(type.equals("connect")){
                        String name = "";
                        try {
                            name= json.getString("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(!name.equals("")){
                            notifyAllObservers(name);
                        }
                    }
                    //    if(type.)*/

                }






            });
        } catch (WebSocketException e) {
            e.printStackTrace();
        }


    }
    public void sendNextImagePage(int page){
        JSONObject json = new JSONObject();
        jsonPut(json,"type","streamer");
        jsonPut(json,"name","client2");
        jsonPut(json,"page",page);
        mConnectionsendImage.sendTextMessage(json.toString());
    }
    public void setPageIndex(int page){
        this.pageIndex = page;
    }


}

