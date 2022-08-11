package com.sushko.hippy;

import com.sushko.recorder.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class OpenglSurfaceView extends GLSurfaceView {

    private final OpenglRenderer renderer;
    private final Context context;
    public OpenglSurfaceView(Context context) {
        this(context, null);
    }

    // Конструкторы для работы через XML-разметку
    public OpenglSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OpenglSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        this.context = context;
        renderer = new OpenglRenderer(context);
        // Создание контекста OpenGL ES 3.0
        setEGLContextClientVersion(3);
        setRenderer(renderer);
        // Реакция на жесты пользователя
        setOnTouchListener(new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeRight() {
                queueEvent(renderer::previousShader);
            }
            @Override
            public void onSwipeLeft() {
                queueEvent(renderer::nextShader);
            }
            @Override
            public void onZoomIn(float scaleFactor) {
                renderer.setScale(scaleFactor);
            }
            @Override
            public void onZoomOut(float scaleFactor) {
                renderer.setScale(-scaleFactor);
            }
        });
    }

    public void setHue(int hue) {
        renderer.setHue(hue);
    }

    public void setSpeed(float speed) {
        renderer.setSpeed(speed);
    }

    public void setMagicNumber(int magicNumber) { renderer.setMagicNumber(magicNumber); }

    public void startRecording() { renderer.startRecording(); }

    public void stopRecording() {
        String directory  = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + Environment.DIRECTORY_MOVIES + "/"
                + UUID.randomUUID().toString() + ".mp4";
        File videoFile = new File(directory);
        ArrayList<Bitmap> bitmaps = renderer.stopRecording();
        Muxer muxer = new Muxer(context, videoFile);
        try {
            muxer.mux(bitmaps, null);
        } catch (Exception ignore) { }
    }

    public void makeScreenshot() {
        // Метод будет вызван в потоке OpenglRenderer
        queueEvent(() -> {
            Bitmap image = renderer.getCurrentFrame();
            MediaStore.Images.Media.insertImage(context.getContentResolver(), image,
                    UUID.randomUUID().toString(), "hippy app image");
        });
    }

}
