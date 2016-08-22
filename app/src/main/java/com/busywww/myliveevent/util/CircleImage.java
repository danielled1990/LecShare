package com.busywww.myliveevent.util;

/**
 * Created by BusyWeb on 12/8/2015.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

import com.busywww.myliveevent.R;

public class CircleImage {
    Context context;
    private static Path mPath = null;
    private static Paint mPaint = null;

    // usage
    //your_imageview.setImageBitmap(new CircleImage(getApplicationContext()).transform(your_image_bitmap));

    public CircleImage(Context context) {
        this.context = context;

    }

    public Bitmap transform(Bitmap source, int backgroundColor) {
        Bitmap bitmap = null;
        int size = Math.min(source.getWidth(), source.getHeight());
        int width = source.getWidth();
        int height = source.getHeight();
        try {

            //Canvas canvas = new Canvas();
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            mPath = new Path();
            mPath.addCircle(width / 2F, height / 2F, width / 2F, Path.Direction.CW);
            canvas.clipPath(mPath);
            canvas.clipPath(mPath, Region.Op.REPLACE);
            canvas.drawColor(backgroundColor);
            canvas.drawBitmap(source, null, new Rect(0, 0, width, height), null);
            //mPaint = new Paint();
            //mPaint.setAntiAlias(true);
            //mPaint.setARGB(0, 0, 0, 0);

//            Bitmap squaredBitmap = Bitmap
//                    .createBitmap(source, x, y, size, size);
//            if (squaredBitmap != source) {
//                // source.recycle();
//            }
//            Bitmap bitmap = Bitmap.createBitmap(size, size,
//                    squaredBitmap.getConfig());
//
//            canvas.drawColor(0);
//            Paint paint = new Paint();
//            BitmapShader shader = new BitmapShader(squaredBitmap,
//                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
//            paint.setShader(shader);
//            paint.setAntiAlias(true);
//
//            float r = size / 2f;
//            canvas.drawCircle(r, r, r, paint);


        } catch (Exception e) {
            // TODO: handle exception
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            mPath = new Path();
            mPath.addCircle(width / 2F, height / 2F, width / 2F, Path.Direction.CW);
            canvas.clipPath(mPath);
            canvas.clipPath(mPath, Region.Op.REPLACE);
            canvas.drawColor(backgroundColor);
            canvas.drawBitmap(icon, new Rect(0, 0, icon.getWidth(), icon.getHeight()), new Rect(0, 0, width, height), null);
        }
        return bitmap;
    }

}