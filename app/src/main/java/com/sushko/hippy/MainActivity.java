package com.sushko.hippy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.internal.TextWatcherAdapter;

import java.util.Random;

import codes.side.andcolorpicker.hsl.HSLColorPickerSeekBar;

public class MainActivity extends AppCompatActivity {

    private OpenglSurfaceView glSurfaceView;
    private Animation fullButtonAnimation;
    private boolean rendererSet = false;
    private boolean longClick = false;

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Проверка поддержки OpenGL ES 3.0
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsGL30 = configurationInfo.reqGlEsVersion >= 0x30000;
        if (supportsGL30) {
            rendererSet = true;
        } else {
            Toast.makeText(this, R.string.opengl_unsupported, Toast.LENGTH_LONG).show();
            return;
        }
        requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
        glSurfaceView = findViewById(R.id.drawingSurfaceView);

        //Инициализация анимаций кнопки
        fullButtonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_full);
        Animation holdButtonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_hold);
        holdButtonAnimation.setFillEnabled(true);
        holdButtonAnimation.setFillAfter(true);
        Animation releasedButtonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_released);
        releasedButtonAnimation.setFillEnabled(true);
        releasedButtonAnimation.setFillAfter(true);

        Button shutterButton = findViewById(R.id.shutterButton);
        shutterButton.setOnClickListener(view -> {
            if (longClick) {
                longClick = false;
                view.startAnimation(releasedButtonAnimation);
                glSurfaceView.stopRecording();
                return;
            }
            System.out.println("SHORT");
            view.startAnimation(fullButtonAnimation);
            glSurfaceView.makeScreenshot();
        });
        shutterButton.setOnLongClickListener(view -> {
            longClick = true;
            view.startAnimation(holdButtonAnimation);
            glSurfaceView.startRecording();
            System.out.println("LONG");
            return false;
        });

        //Инициализация ползунка оттенков
        HSLColorPickerSeekBar colorPicker = findViewById(R.id.colorPicker);
        colorPicker.addListener(new ColorPickerListener(glSurfaceView));

        // Инициализация списка скоростей
        Spinner spinner = findViewById(R.id.speedSpinner);
        ArrayAdapter<?> adapter =
                ArrayAdapter.createFromResource(this, R.array.speed_options_array,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String item = ((String) adapter.getItem(pos)).substring(1);
                glSurfaceView.setSpeed(Float.parseFloat(item));
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        // Магическое число
        EditText editTextMagicNumber = findViewById(R.id.editTextMagicNumber);
        editTextMagicNumber.addTextChangedListener(new TextWatcherAdapter(){
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                super.afterTextChanged(s);
                int magicNumber = 10;
                try {
                    magicNumber = Integer.parseInt(s.toString());
                } catch (NumberFormatException ignore) { }
                glSurfaceView.setMagicNumber(magicNumber);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rendererSet) {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rendererSet) {
            glSurfaceView.onResume();
        }
    }

    public void randomizeOnClick(View view) {
        view.startAnimation(fullButtonAnimation);
        Random random = new Random(System.currentTimeMillis());
        glSurfaceView.setSpeed(0.1f + random.nextFloat() * (3f - 0.1f));
        glSurfaceView.setHue(random.nextInt(360));
        glSurfaceView.setMagicNumber(random.nextInt(30) + 1);
    }

}