package com.sushko.hippy;

import static android.opengl.GLES30.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES30.GL_FLOAT;
import static android.opengl.GLES30.GL_FRAGMENT_SHADER;
import static android.opengl.GLES30.GL_RGBA;
import static android.opengl.GLES30.GL_TRIANGLE_STRIP;
import static android.opengl.GLES30.GL_UNSIGNED_BYTE;
import static android.opengl.GLES30.GL_VERTEX_SHADER;
import static android.opengl.GLES30.glClear;
import static android.opengl.GLES30.glDeleteShader;
import static android.opengl.GLES30.glDrawArrays;
import static android.opengl.GLES30.glEnableVertexAttribArray;
import static android.opengl.GLES30.glFinish;
import static android.opengl.GLES30.glGetAttribLocation;
import static android.opengl.GLES30.glReadPixels;
import static android.opengl.GLES30.glUniform1f;
import static android.opengl.GLES30.glUniform2f;
import static android.opengl.GLES30.glUniformMatrix4fv;
import static android.opengl.GLES30.glUseProgram;
import static android.opengl.GLES30.glVertexAttrib3fv;
import static android.opengl.GLES30.glVertexAttribPointer;
import static android.opengl.GLES30.glViewport;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenglRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "OpenglRenderer";
    private final OrthographicCamera camera;
    private final Uniforms uniforms;
    private final Shaders shaders;
    private final Context context;
    private final FloatBuffer vertices;
    private final ArrayList<Bitmap> bitmaps;
    private long lastTime;
    private float hue, elapsedTime, speed, scale, magicNumber;
    private int aPosition, width, height;
    private boolean isCapturing = false;

    public OpenglRenderer(Context context) {
        this.context = context;
        camera = new OrthographicCamera(-1.0f, 1.0f, -1.0f, 1.0f);
        bitmaps = new ArrayList<>();
        uniforms = new Uniforms();
        shaders = new Shaders();
        vertices = createVertices();
        speed = 1f;
        scale = 1f;
        magicNumber = 10f;
    }

    public void setHue(int hue) {
        this.hue = hue / 360f;
    }

    public void setSpeed(float speed) { this.speed = speed; }

    public void setScale(float scale) { this.scale += scale * 0.1; }

    public void setMagicNumber(int magicNumber) { this.magicNumber = magicNumber; }

    public void setShader(int fragShaderFileId) {
        int vertexShader = ShaderUtils.createShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader);
        int fragmentShader = ShaderUtils.createShader(context, GL_FRAGMENT_SHADER, fragShaderFileId);
        // Присоединение шейдеров и создание программы
        int programId = ShaderUtils.createProgram(vertexShader, fragmentShader);
        // Запуск программы шейдеров
        glUseProgram(programId);
        // Создание поверхности для рисования из двух треугольников
        aPosition = createPlane(programId, vertices);
        // Подключение uniforms
        uniforms.add("u_viewProjectionMatrix", programId);
        uniforms.add("u_resolution", programId);
        uniforms.add("u_time", programId);
        uniforms.add("u_hue", programId);
        uniforms.add("u_speed", programId);
        uniforms.add("u_scale", programId);
        uniforms.add("u_magic_number", programId);
        // Очистка памяти
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        // Запись времени начала работы программы
        lastTime = System.currentTimeMillis();
    }

    public void nextShader() {
        setShader(shaders.getNext());
    }

    public void previousShader() {
        setShader(shaders.getPrevious());
    }

    public void startRecording() {
        bitmaps.clear();
        isCapturing = true;
    }
    public ArrayList<Bitmap> stopRecording() {
        isCapturing = false;
        return bitmaps;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) { setShader(shaders.getCurrent()); }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        this.width = width;
        this.height = height;
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        if (isCapturing) {
            bitmaps.add(getCurrentFrame());
        }
        long time = System.currentTimeMillis();
        float deltaTime = (time - lastTime) / 1000.0f;
        lastTime = time;
        elapsedTime += deltaTime;
        // Обновление in-параметров
        glVertexAttrib3fv(aPosition, vertices);
        // Обновление данных в uniforms
        glUniformMatrix4fv(uniforms.get("u_viewProjectionMatrix"), 1, false, camera.getViewProjectionMatrix(), 0);
        glUniform1f(uniforms.get("u_time"), elapsedTime);
        glUniform1f(uniforms.get("u_hue"), hue);
        glUniform2f(uniforms.get("u_resolution"), width, height);
        glUniform1f(uniforms.get("u_speed"), speed);
        glUniform1f(uniforms.get("u_scale"), scale);
        glUniform1f(uniforms.get("u_magic_number"), magicNumber);
        glFinish();
    }

    public Bitmap getCurrentFrame() {
        return savePixels(width, height);
    }

    private Bitmap savePixels(int w, int h) {
        int[] b = new int[w * (h)];
        int[] bt = new int[w * h];
        IntBuffer ib = IntBuffer.wrap(b);
        ib.position(0);
        glReadPixels(0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, ib);
        for (int i = 0, k = 0; i < h; i++, k++) {
            // bitmap OpenGL несовместим с Android,
            // поэтому нужна коррекция.
            for (int j = 0; j < w; j++) {
                int pix = b[i * w + j];
                int pb = (pix >> 16) & 0xff;
                int pr = (pix << 16) & 0x00ff0000;
                int pix1 = (pix & 0xff00ff00) | pr | pb;
                bt[(h - k - 1) * w + j] = pix1;
            }
        }
        return Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
    }

    private FloatBuffer createVertices() {
        final float[] planeVertices = {
                //первый треугольник
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f,  1.0f,
                //второй треугольник
                1.0f,  1.0f,
        };
        FloatBuffer vertexData = ByteBuffer
                .allocateDirect(planeVertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(planeVertices);
        return vertexData;
    }

    private int createPlane(int programId, FloatBuffer vertexData) {
        // Сохранение вершин в программу
        int aPositionLocation = glGetAttribLocation(programId, "a_Position");
        if (aPositionLocation == -1) {
            Log.e(TAG,"Couldn't find attribute: a_Position");
        }
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 0, vertexData);
        glEnableVertexAttribArray(aPositionLocation);
        return aPositionLocation;
    }

}
