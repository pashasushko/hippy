package com.sushko.hippy;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;

public class OnSwipeTouchListener implements OnTouchListener {

    private final GestureDetector swipeGestureDetector;
    private final ScaleGestureDetector scaleGestureDetector;

    public OnSwipeTouchListener(Context ctx) {
        swipeGestureDetector = new GestureDetector(ctx, new SwipeGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(ctx, new ScaleGestureListener());
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        view.performClick();
        swipeGestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private final class SwipeGestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    private final class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            if (scaleFactor > 1) {
                onZoomOut(scaleFactor);
            } else {
                onZoomIn(scaleFactor);
            }
            return true;
        }
    }

    public void onSwipeRight() { }

    public void onSwipeLeft() { }

    public void onZoomIn(float scaleFactor) { }

    public void onZoomOut(float scaleFactor) { }

}