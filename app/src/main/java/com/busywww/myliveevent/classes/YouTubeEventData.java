package com.busywww.myliveevent.classes;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.busywww.myliveevent.R;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveStream;

/**
 * Created by BusyWeb on 12/6/2015.
 */
public class YouTubeEventData {
    private LiveBroadcast mEvent;
    private LiveStream mStream;
    private String mIngestionAddress;
    private String mIngestionAddressBackup;
    private String mStreamName;

    public LiveBroadcast GetEvent() {
        return mEvent;
    }

    public void SetEvent(LiveBroadcast event) {
        mEvent = event;
    }

    public String GetId() {
        return mEvent.getId();
    }

    public String GetTitle() {
        return mEvent.getSnippet().getTitle();
    }

    public String GetThumbUri() {
        String url = mEvent.getSnippet().getThumbnails().getDefault().getUrl();
        //String url = mEvent.getSnippet().getThumbnails().getMedium().getUrl();
        // if protocol is not defined, pick https
        if (url.startsWith("//")) {
            url = "https:" + url;
        }
        return url;
    }

    public String GetStreamName() {
        return mStreamName;
    }
    public void SetStreamName(String streamName) {
        mStreamName = streamName;
    }

    public String GetIngestionAddress() {
        return mIngestionAddress;
    }

    public void SetIngestionAddress(String ingestionAddress) {
        mIngestionAddress = ingestionAddress;
    }

    public String GetBackupIngestionAddress() {
        return mIngestionAddressBackup;
    }

    public void SetBackupIngestionAddress(String backupAddress) {
        mIngestionAddressBackup = backupAddress;
    }

    public String GetWatchUri() {
        return "http://www.youtube.com/watch?v=" + GetId();
    }

//    public String GetPrivacyStatus() {
//        return mEvent.getStatus().getPrivacyStatus();
//    }

    public void SetPrivacyStatus(String status) {
        mEvent.getStatus().setPrivacyStatus(status);
    }

    public LiveStream GetLiveStream() {
        return mStream;
    }
    public void SetLiveStream(LiveStream stream) {
        mStream = stream;
    }

    public String GetBroadcastRecordingStatus() {
        String ret = "broadcast: unknown";
        try {
            String status = mEvent.getStatus().getRecordingStatus();
            if (status != null) {
                if (status.toLowerCase().startsWith("not")) {
                    return "broadcast: not recording";
                } else {
                    status.toLowerCase();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
    public Drawable GetBroadcastRecordingStatusDrawable(Resources resources) {
        Drawable ret = resources.getDrawable(R.mipmap.ic_broadcast_unknown);
        try {
            String status = mEvent.getStatus().getRecordingStatus();
            if (status != null) {
                if (status.toLowerCase().startsWith("not")) {
                    return resources.getDrawable(R.mipmap.ic_broadcast_off);
                } else {
                    return resources.getDrawable(R.mipmap.ic_broadcast_on);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
    public String GetStreamStatus() {
        if (mStream != null) {
            String status = mStream.getStatus().getStreamStatus();
            if (status != null) {
                return "stream: " + status.toLowerCase();
            } else {
                return "stream: unknown";
            }
        } else {
            return  "stream: unknown";
        }
    }
    public Drawable GetStreamStatusDrawable(Resources resources) {
        Drawable ret = resources.getDrawable(R.mipmap.ic_stream_unknown);
        try {
            if (mStream == null) {
                return ret;
            }
            String status = mStream.getStatus().getStreamStatus();
            if (status != null) {
                if (status.toLowerCase().startsWith("ready")) {
                    return resources.getDrawable(R.mipmap.ic_stream_off);
                } else {
                    return resources.getDrawable(R.mipmap.ic_stream_on);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
    public String GetPrivacyStatus() {
        String ret = "unlisted";
        try {
            String status = mEvent.getStatus().getPrivacyStatus();
            if (status != null) {
                ret = status.toLowerCase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
    public Drawable GetPrivacyDrawable(Resources resources) {
        Drawable ret = resources.getDrawable(R.drawable.ic_text_unlisted);
        try {
            String status = mEvent.getStatus().getPrivacyStatus();
            if (status != null) {
                if (status.toLowerCase().startsWith("private")) {
                    return resources.getDrawable(R.drawable.ic_text_private);
                } else if(status.toLowerCase().startsWith("public")) {
                    return resources.getDrawable(R.drawable.ic_text_public);
                } else {
                    resources.getDrawable(R.drawable.ic_text_unlisted);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
