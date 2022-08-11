package com.sushko.hippy;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {

    private static final String TAG = "FileUtils";

    public static String loadTextFromRawFile(Context context, int resId) {
        StringBuilder shaderSrc = new StringBuilder();
        try {
            BufferedReader reader = null;
            try {
                InputStream inputStream = context.getResources().openRawResource(resId);
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    shaderSrc.append(line).append("\n");
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Couldn't load file");
        }
        return shaderSrc.toString();
    }

}