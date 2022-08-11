package com.sushko.hippy;

import androidx.annotation.NonNull;

import codes.side.andcolorpicker.model.IntegerHSLColor;
import codes.side.andcolorpicker.view.picker.ColorSeekBar;

public class ColorPickerListener implements ColorSeekBar.OnColorPickListener<ColorSeekBar<IntegerHSLColor>, IntegerHSLColor> {
    private final OpenglSurfaceView glSurfaceView;

    public ColorPickerListener(OpenglSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
    }

    @Override
    public void onColorPicking(@NonNull ColorSeekBar<IntegerHSLColor> colorSeekBar, IntegerHSLColor intHSLColor, int i, boolean b) {
        glSurfaceView.setHue(intHSLColor.getIntH());
    }

    @Override
    public void onColorChanged(@NonNull ColorSeekBar<IntegerHSLColor> colorSeekBar, @NonNull IntegerHSLColor intHSLColor, int i) { }

    @Override
    public void onColorPicked(@NonNull ColorSeekBar<IntegerHSLColor> colorSeekBar, @NonNull IntegerHSLColor intHSLColor, int i, boolean b) { }

}
