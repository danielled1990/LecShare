package com.busywww.myliveevent.classes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.busywww.myliveevent.util.UtilGraphic;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
//import com.googlecode.javacv.FFmpegFrameRecorder;
//import com.googlecode.javacv.Frame;
//import com.googlecode.javacv.cpp.opencv_core.IplImage;
//import static com.googlecode.javacv.cpp.opencv_core.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * Created by BusyWeb on 12/11/2015.
 */
public class YouTubeStreamer {

    private static final String TAG = "YouTubeStreamer";

    private static FFmpegFrameRecorder mRecorder;
    private static Frame mFrame;
    //private static opencv_core.IplImage mIplImage;
    private static int mSampleAudioRateInHz = 44100;    //44100;
    private static int mImageWidth = 320;
    private static int mImageHeight = 240;
    private static int mFrameRate = 30;     //30;
    final private static int GOP_LENGTH_IN_FRAMES = 60; // = framerate * 2;
    private static String mStreamLink;
    private static boolean mIsRecording = false;
    private static AudioRecord mAudioRecord;
    private static AudioRecordRunnable mAudioRecordRunnable;
    private static Thread mAudioThread;
    private static boolean mRunAudioThread;
    private static long mStartTime;
    private static int mPreviewScaleDown = 1;

    public YouTubeStreamer(String streamLink, int imageWidth, int imageHeight) {

        mStreamLink = streamLink;
        mImageWidth = imageWidth;
        mImageHeight = imageHeight;

        // youtube 240P
        //mImageWidth = 426;
        //mImageHeight = 240;

    }

    public void StartStreaming() {
        initRecorder();

        try {

            mRecorder.start();
            mStartTime = System.currentTimeMillis();
            mAudioThread.start();
            mIsRecording = true;

        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    public void StopStreaming() {

        mRunAudioThread = false;
        try {
            mAudioThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mAudioRecordRunnable = null;
        mAudioThread = null;

        if (mRecorder != null && mIsRecording) {
            mIsRecording = false;
            Log.v(TAG,"Finishing recording, calling stop and release on recorder");
            try {
                mRecorder.stop();
                mRecorder.release();
            } catch (FFmpegFrameRecorder.Exception e) {
                e.printStackTrace();
            }
            mRecorder = null;
        }
    }

    public void PauseStream() {
        mIsRecording = false;
    }
    public void RestartStream() {
        mIsRecording = true;
    }

    //---------------------------------------
    // initialize ffmpeg_recorder
    //---------------------------------------
    private void initRecorder() {

        Log.w(TAG, "init recorder");
        //mFrame = new Frame(mImageWidth, mImageHeight, Frame.DEPTH_UBYTE, 2);
        mFrame = new Frame(mImageWidth/mPreviewScaleDown, mImageHeight/mPreviewScaleDown, Frame.DEPTH_UBYTE, 2);
        //mIplImage = opencv_core.IplImage.create(mImageWidth, mImageHeight, opencv_core.IPL_DEPTH_8U, 2);

        Log.i(TAG, "stream_url: " + mStreamLink);

//        String root = Environment.getExternalStorageDirectory().toString() + "/" + "myliveevent/";
//        File appFolder = new File(root);
//        if (appFolder.exists() == false){
//            appFolder.mkdir();
//        }
//        String videoFile = root + String.valueOf(System.currentTimeMillis()) + ".mp4";
//        mRecorder = new FFmpegFrameRecorder(videoFile, mImageWidth/mPreviewScaleDown, mImageHeight/mPreviewScaleDown, 1);
//        mRecorder.setFormat("mp4");

        mRecorder = new FFmpegFrameRecorder(mStreamLink, mImageWidth/mPreviewScaleDown, mImageHeight/mPreviewScaleDown, 1);
        mRecorder.setFormat("flv");

        //mRecorder.setFormat("mp4");
        //mRecorder.setFormat("3gp");

        mRecorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        mRecorder.setFrameRate(15);     //15(mFrameRate);
        //mRecorder.setFrameRate(mFrameRate);
        // Key frame interval, in our case every 2 seconds -> 30 (fps) * 2 = 60
        // (gop length)
        mRecorder.setGopSize(30);     // GOP_LENGTH_IN_FRAMES: framerate(15) * 4 = 30
        mRecorder.setVideoOption("profile", "baseline");
        mRecorder.setVideoOption("preset", "veryslow");
        mRecorder.setVideoOption("crf", "28");
        mRecorder.setVideoOption("maxrate", "240k");
        mRecorder.setVideoOption("bufsize", "480k");
        //mRecorder.setVideoQuality(10);
        //mRecorder.setVideoOption("fflags", "nobuffer");
        //mRecorder.setVideoOption("faststart", "1");
        //mRecorder.setVideoQuality(0); // maximum quality, replace recorder.setVideoBitrate(16384);
        //mRecorder.setVideoOption("preset", "veryfast"); // or ultrafast or fast, etc.
        //mRecorder.setVideoOption("preset", "veryfast");
        //mRecorder.setVideoOption("faststart", "1");
        //mRecorder.setVideoOption("tune", "zerolatency");
        //mRecorder.setVideoOption("fflags", "nobuffer");
        //mRecorder.setVideoOption("analyzeduration", "0");

        mRecorder.setSampleRate(mSampleAudioRateInHz);
        // Set in the surface changed method

//        recorder.setCodecID(CODEC_ID_MPEG4);
//        recorder.setFormat("mp4");
//        recorder.setPixelFormat(PIX_FMT_YUV420P);
//        recorder.start();
//// loop ...
//        cvCvtColor(yuvimage, bgrimage, CV_YUV420sp2BGR)
//        recorder.record(bgrimage);

        Log.i(TAG, "recorder initialize success");

        mAudioRecordRunnable = new AudioRecordRunnable();
        mAudioThread = new Thread(mAudioRecordRunnable);
        mRunAudioThread = true;
    }

    //---------------------------------------------
    // audio thread, gets and encodes audio data
    //---------------------------------------------
    class AudioRecordRunnable implements Runnable {

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            // Audio
            int bufferSize;
            ShortBuffer audioData;
            //short[] audioData;
            int bufferReadResult;

            bufferSize = AudioRecord.getMinBufferSize(mSampleAudioRateInHz,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mSampleAudioRateInHz,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            audioData = ShortBuffer.allocate(bufferSize);
            //audioData = new short[bufferSize];

            Log.d(TAG, "audioRecord.startRecording()");
            mAudioRecord.startRecording();

            /* ffmpeg_audio encoding loop */
            while (mRunAudioThread) {
                //Log.v(LOG_TAG,"recording? " + recording);
                bufferReadResult = mAudioRecord.read(audioData.array(), 0, audioData.capacity());
                audioData.limit(bufferReadResult);
                //bufferReadResult = mAudioRecord.read(audioData, 0, audioData.length);

                if (bufferReadResult > 0) {
                    // If "recording" isn't true when start this thread, it never get's set according to this if statement...!!!
                    // Why?  Good question...
                    if (mIsRecording) {
                        try {

                            Log.v(TAG,"bufferReadResult: " + bufferReadResult);
                            mRecorder.recordSamples(audioData);

                            //mRecorder.record(ShortBuffer.wrap(audioData, 0, bufferReadResult));
                            //Log.v(LOG_TAG,"recording " + 1024*i + " to " + 1024*i+1024);
                        } catch (Exception e) {
                            //FFmpegFrameRecorder.Exception
                            Log.v(TAG,e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
            Log.v(TAG,"AudioThread Finished, release audioRecord");

            /* encoding finish, release recorder */
            if (mAudioRecord != null) {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
                Log.v(TAG,"audioRecord released");
            }
        }
    }

    public void FrameReady(byte[] data) {
        try {
            if (!mIsRecording) {
                return;
            }

            if (mAudioRecord == null || mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                mStartTime = System.currentTimeMillis();
                return;
            }

//            /* get video data */
//            if (mFrame != null && mIsRecording) {
//                ((ByteBuffer)mFrame.image[0].position(0)).put(data);
//
//                try {
//                    Log.v(TAG,"Writing Frame");
//
//                    long t = 1000 * (System.currentTimeMillis() - mStartTime);
//                    if (t > mRecorder.getTimestamp()) {
//                        mRecorder.setTimestamp(t);
//                    }
//                    mRecorder.record(mFrame);
//                } catch (FFmpegFrameRecorder.Exception e) {
//                    Log.v(TAG,e.getMessage());
//                    e.printStackTrace();
//                }
//            }
            /* get video data */
            //if (mIplImage != null && mIsRecording) {
                    //mIplImage.getByteBuffer().put(data);
            if (mFrame != null && mIsRecording) {

                ((ByteBuffer)mFrame.image[0].position(0)).put(data);

                // sample for to update image
//                Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ALPHA_8);
//                bitmap.copyPixelsFromBuffer(mIplImage.getByteBuffer());
//                Canvas canvas = new Canvas(bitmap);
//                Paint paint = new Paint();
//                paint.setColor(Color.GREEN);
//                float leftx = 20;
//                float topy = 20;
//                float rightx = 50;
//                float bottomy = 100;
//                RectF rectangle = new RectF(leftx,topy,rightx,bottomy);
//                canvas.drawRect(rectangle, paint);
//                bitmap.copyPixelsToBuffer(mIplImage.getByteBuffer());

                try {
                    Log.v(TAG,"Writing Frame");

                    long t = 1000 * (System.currentTimeMillis() - mStartTime);
                    if (t > mRecorder.getTimestamp()) {
                        mRecorder.setTimestamp(t);
                    }
                    mRecorder.record(mFrame);
                    //mRecorder.record(mIplImage);
                } catch (FFmpegFrameRecorder.Exception e) {
                    Log.v(TAG,e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] mCompressedYuvBytes = null;
    private static int mCompressQuality = 25;
    public void FrameBitmap(Bitmap bitmap) {
        try {
            if (!mIsRecording) {
                return;
            }

            if (mAudioRecord == null || mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                mStartTime = System.currentTimeMillis();
                return;
            }
            if (mFrame != null && mIsRecording) {

                //bitmap.copyPixelsToBuffer(temp2(bitmap));
                mCompressedYuvBytes = UtilGraphic.GetNV21FromBitmap(bitmap, mCompressQuality, mImageWidth/mPreviewScaleDown, mImageHeight/mPreviewScaleDown);
                ((ByteBuffer)mFrame.image[0].position(0)).put(mCompressedYuvBytes);

                 try {
                     Log.v(TAG,"Writing Frame");
                     long t = 1000 * (System.currentTimeMillis() - mStartTime);
                     //mRecorder.setTimestamp(t);

                    if (t > mRecorder.getTimestamp()) {
                        mRecorder.setTimestamp(t);

                        Log.i(TAG, "timestamp: " + String.valueOf(t));
                    }

                    mRecorder.record(mFrame);
                    //mRecorder.record(mIplImage);
                } catch (FFmpegFrameRecorder.Exception e) {
                    Log.v(TAG,e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void PreviewFrameYuvImage(YuvImage yuvImage) {
        try {
            if (!mIsRecording) {
                return;
            }

            if (mAudioRecord == null || mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                mStartTime = System.currentTimeMillis();
                return;
            }
            if (mFrame != null && mIsRecording) {

                //bitmap.copyPixelsToBuffer(temp2(bitmap));
                ((ByteBuffer)mFrame.image[0].position(0)).put(temp(yuvImage));

                try {
                    Log.v(TAG,"Writing Frame");

                    long t = 1000 * (System.currentTimeMillis() - mStartTime);
                    if (t > mRecorder.getTimestamp()) {
                        mRecorder.setTimestamp(t);
                    }
                    mRecorder.record(mFrame);
                    //mRecorder.record(mIplImage);
                } catch (FFmpegFrameRecorder.Exception e) {
                    Log.v(TAG,e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ByteArrayOutputStream NewFrameBos = null;
    private static byte[] bitmapBytes;
    private static byte[] yuvImageBytes;

    private static byte[] temp(YuvImage yuvImage) {
        try {
            NewFrameBos = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, MyCameraPreview.PreviewWidth,
                    MyCameraPreview.PreviewHeight), 40, NewFrameBos);
            yuvImageBytes = NewFrameBos.toByteArray();
            NewFrameBos.close();
            //NewFrameBos = null;
        } catch (IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        return yuvImageBytes;
    }
    private static byte[] temp2(Bitmap bitmap) {
        try {
            NewFrameBos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, NewFrameBos);
            bitmapBytes = NewFrameBos.toByteArray();
            NewFrameBos.close();
            //NewFrameBos = null;
        } catch (IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        return bitmapBytes;
    }
}
