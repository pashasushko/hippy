package com.sushko.hippy;

import static android.opengl.GLES20.glGetUniformLocation;

import android.util.Log;

import java.util.HashMap;

public class Uniforms {
    private static final String TAG = "Uniform";
    private final HashMap<String, Integer> uniforms;

    public Uniforms() {
        uniforms = new HashMap<>();
    }

    public int get(String uniformName) {
        if (!uniforms.containsKey(uniformName)) return 0;
        Integer uniformId = uniforms.get(uniformName);
        return uniformId != null ? uniformId : 0;
    }

    public void add(String uniformName, int programId) {
        int uniformId = glGetUniformLocation(programId, uniformName);
        if (uniformId == -1) {
            Log.e(TAG,"Couldn't find uniform: " + uniformName);
            return;
        }
        uniforms.put(uniformName, uniformId);
    }

}
