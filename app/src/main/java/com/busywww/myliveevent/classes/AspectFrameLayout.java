/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.busywww.myliveevent.classes;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

/**
 * Layout that adjusts to maintain a specific aspect ratio.
 */
public class AspectFrameLayout extends FrameLayout {
    private static final String TAG = "AspectFrameLayout";

    private double mTargetAspect = -1.0;        // initially use default window size

    public AspectFrameLayout(Context context) {
        super(context);
    }

    public AspectFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Sets the desired aspect ratio.  The value is <code>width / height</code>.
     */
    public void setAspectRatio(double aspectRatio) {
        if (aspectRatio < 0) {
            throw new IllegalArgumentException();
        }
        Log.d(TAG, "Setting aspect ratio to " + aspectRatio + " (was " + mTargetAspect + ")");
        if (mTargetAspect != aspectRatio) {
            mTargetAspect = aspectRatio;
            requestLayout();
        }
//        mTargetAspect = aspectRatio;
//        requestLayout();
        initialMeasureDone = false;
    }

    public static boolean initialMeasureDone = false;
    public static int width = 0, height = 0;

//    @Override
//    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
//        if (width == oldWidth && height == oldHeight) {
//            return;
//        }
//        int initialWidth = MeasureSpec.getSize(width);
//        int initialHeight = MeasureSpec.getSize(height);
//
//        if (initialWidth == width && initialHeight == height) {
//            //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//            //return;
//        } else {
//            width = initialWidth;
//            height = initialHeight;
//        }
//
//        if (mTargetAspect > 0) {
//            //int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
//            //int initialHeight = MeasureSpec.getSize(heightMeasureSpec);
//
//            // factor the padding out
//            int horizPadding = getPaddingLeft() + getPaddingRight();
//            int vertPadding = getPaddingTop() + getPaddingBottom();
//            initialWidth -= horizPadding;
//            initialHeight -= vertPadding;
//
//            double viewAspectRatio = (double) initialWidth / initialHeight;
//            double aspectDiff = mTargetAspect / viewAspectRatio - 1;
//
//            if (Math.abs(aspectDiff) < 0.1) {
//                //0.01
//                // We're very close already.  We don't want to risk switching from e.g. non-scaled
//                // 1280x720 to scaled 1280x719 because of some floating-point round-off error,
//                // so if we're really close just leave it alone.
//                Log.d(TAG, "aspect ratio is good (target=" + mTargetAspect +
//                        ", view=" + initialWidth + "x" + initialHeight + ")");
//            } else {
//                if (aspectDiff > 0) {
//                    // limited by narrow width; restrict height
//                    initialHeight = (int) (initialWidth / mTargetAspect);
//                } else {
//                    // limited by short height; restrict width
//                    initialWidth = (int) (initialHeight * mTargetAspect);
//                }
//                Log.d(TAG, "new size=" + initialWidth + "x" + initialHeight + " + padding " +
//                        horizPadding + "x" + vertPadding);
//                initialWidth += horizPadding;
//                initialHeight += vertPadding;
//                int widthMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
//                int heightMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);
//                measure(widthMeasureSpec, heightMeasureSpec);
//            }
//        }
//        super.onSizeChanged(width, height, oldWidth, oldHeight);
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
        int initialHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (initialWidth == width && initialHeight == height) {
            //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            //return;
        } else {
            width = initialWidth;
            height = initialHeight;
        }

        Log.d(TAG, "onMeasure target=" + mTargetAspect +
                " width=[" + MeasureSpec.toString(widthMeasureSpec) +
                "] height=[" + MeasureSpec.toString(heightMeasureSpec) + "]");
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // Target aspect ratio will be < 0 if it hasn't been set yet.  In that case,
        // we just use whatever we've been handed.
        if (mTargetAspect > 0) {
            //int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
            //int initialHeight = MeasureSpec.getSize(heightMeasureSpec);

            // factor the padding out
            int horizPadding = getPaddingLeft() + getPaddingRight();
            int vertPadding = getPaddingTop() + getPaddingBottom();
            initialWidth -= horizPadding;
            initialHeight -= vertPadding;

            double viewAspectRatio = (double) initialWidth / initialHeight;
            double aspectDiff = mTargetAspect / viewAspectRatio - 1;

            if (Math.abs(aspectDiff) < 0.1) {
                //0.01
                // We're very close already.  We don't want to risk switching from e.g. non-scaled
                // 1280x720 to scaled 1280x719 because of some floating-point round-off error,
                // so if we're really close just leave it alone.
                Log.d(TAG, "aspect ratio is good (target=" + mTargetAspect +
                        ", view=" + initialWidth + "x" + initialHeight + ")");
            } else {
                if (aspectDiff > 0) {
                    // limited by narrow width; restrict height
                    initialHeight = (int) (initialWidth / mTargetAspect);
                } else {
                    // limited by short height; restrict width
                    initialWidth = (int) (initialHeight * mTargetAspect);
                }
                Log.d(TAG, "new size=" + initialWidth + "x" + initialHeight + " + padding " +
                        horizPadding + "x" + vertPadding);
                initialWidth += horizPadding;
                initialHeight += vertPadding;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);
            }
        }

        //Log.d(TAG, "set width=[" + MeasureSpec.toString(widthMeasureSpec) +
        //        "] height=[" + View.MeasureSpec.toString(heightMeasureSpec) + "]");
        if (!initialMeasureDone) {
//            setMeasuredDimension(initialWidth, initialHeight);
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//            initialMeasureDone = true;
        }
        setMeasuredDimension(initialWidth, initialHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initialMeasureDone = true;

    }

//    @SuppressLint("NewApi")
//    @Override
    protected void onMeasure2(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

        if (mTargetAspect < 0) {
            //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // General goal: Adjust dimensions to maintain the requested aspect ratio as much
        // as possible. Depending on the measure specs handed down, this may not be possible

        // Only set one of these to true
        boolean scaleWidth = false;
        boolean scaleHeight = false;

        // Sort out which dimension to scale, if either can be. There are 9 combinations of
        // possible measure specs; a few cases below handle multiple combinations
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            // Can't adjust sizes at all, do nothing
        } else if (widthMode == MeasureSpec.EXACTLY) {
            // Width is fixed, heightMode either AT_MOST or UNSPECIFIED, so adjust height
            scaleHeight = true;
        } else if (heightMode == MeasureSpec.EXACTLY) {
            // Height is fixed, widthMode either AT_MOST or UNSPECIFIED, so adjust width
            scaleWidth = true;
        } else if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            // Need to fit into box <= [width, height] in size.
            // Maximize the View's area while maintaining aspect ratio
            // This means keeping one dimension as large as possible and shrinking the other
            float boxAspectRatio = width / (float) height;
            if (boxAspectRatio > mTargetAspect) {
                // Box is wider than requested aspect; pillarbox
                scaleWidth = true;
            } else {
                // Box is narrower than requested aspect; letterbox
                scaleHeight = true;
            }
        } else if (widthMode == MeasureSpec.AT_MOST) {
            // Maximize width, heightSpec is UNSPECIFIED
            scaleHeight = true;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            // Maximize height, widthSpec is UNSPECIFIED
            scaleWidth = true;
        } else {
            // Both MeasureSpecs are UNSPECIFIED. This is probably a pathological layout,
            // with width == height == 0
            // but arbitrarily scale height anyway
            scaleHeight = true;
        }

        // Do the scaling
        if (scaleWidth) {
            width = (int) (height * mTargetAspect);
        } else if (scaleHeight) {
            height = (int) (width / mTargetAspect);
        }

        // Override width/height if needed for EXACTLY and AT_MOST specs
//        width = View.resolveSizeAndState(width, widthMeasureSpec, 0);
//        height = View.resolveSizeAndState(height, heightMeasureSpec, 0);

        // Finally set the calculated dimensions
        setMeasuredDimension(width, height);
    }
}
