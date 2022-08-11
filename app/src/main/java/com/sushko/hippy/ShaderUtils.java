package com.sushko.hippy;

import android.content.Context;
import android.util.Log;

import static android.opengl.GLES30.*;

public class ShaderUtils {

    private static final String TAG = "ShaderUtils";

    public static int createShader(Context context, int type, int shaderRawId) {
        String shaderSrc = FileUtils.loadTextFromRawFile(context, shaderRawId);
        return ShaderUtils.createShader(type, shaderSrc);
    }

    public static int createShader(int type, String shaderSrc) {
        // Компиляция шейдера из строки, содержащей его код
        final int shaderId = glCreateShader(type);
        if (shaderId == 0) return 0;
        glShaderSource(shaderId, shaderSrc);
        glCompileShader(shaderId);
        // Проверка ошибок компиляции
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderId, GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            String log = glGetShaderInfoLog(shaderId);
            Log.e(TAG, "Shader compilation error: ");
            Log.e(TAG, log);
            glDeleteShader(shaderId);
            return 0;
        }
        return shaderId;
    }

    public static int createProgram(int vertexShaderId, int fragmentShaderId) {
        final int programId = glCreateProgram();
        if (programId == 0) return 0;
        // Присоединение шейдеров и создание программы
        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);
        glLinkProgram(programId);
        // Проверка ошибок присоединения
        final int[] linkStatus = new int[1];
        glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            String log = glGetProgramInfoLog(programId);
            Log.e(TAG,"Could not link shader program: ");
            Log.e(TAG, log);
            glDeleteProgram(programId);
            return 0;
        }
        // Валидация программы
        glValidateProgram(programId);
        int error = glGetError();
        if (error != GL_NO_ERROR){
            Log.e(TAG, "Error: ");
            Log.e(TAG, String.valueOf(error));
            return 0;
        }
        return programId;
    }

}
