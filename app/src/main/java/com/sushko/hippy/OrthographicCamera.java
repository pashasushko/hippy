package com.sushko.hippy;

import android.opengl.Matrix;

public class OrthographicCamera {
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] position = new float[3];

    public OrthographicCamera(float left, float right, float bottom, float top) {
        Matrix.orthoM(projectionMatrix, 0, left, right, bottom, top, -1.0f, 1.0f);
        Matrix.setIdentityM(viewMatrix, 0);
        // Порядок умножения матриц нельзя менять
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
    }

    public void updateViewMatrix() {
        final float[] transform = new float[16];
        Matrix.setIdentityM(transform, 0);
        Matrix.translateM(transform, 0, position[0], position[1], position[2]);
        Matrix.invertM(viewMatrix, 0, transform, 0);
        // Порядок умножения матриц нельзя менять
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
    }

    public float[] getProjectionMatrix() {
        return projectionMatrix;
    }

    public float[] getViewMatrix() {
        return viewMatrix;
    }

    public float[] getViewProjectionMatrix() {
        return viewProjectionMatrix;
    }

    public void setPosition(float x, float y, float z) {
        position[0] = x;
        position[1] = y;
        position[2] = z;
        updateViewMatrix();
    }

    public float[] getPosition() {
        return position;
    }

}
