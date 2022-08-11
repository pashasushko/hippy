package com.sushko.hippy;

public class Shaders {
    private final int[] shaders;
    private int current;

    public Shaders() {
        shaders = new int[] { R.raw.first, R.raw.third, R.raw.second };
        current = 0;
    }

    public int getCurrent() {
        return shaders[current];
    }

    public int getNext() {
        if (++current == shaders.length) {
            current = 0;
        }
        return shaders[current];
    }

    public int getPrevious() {
        if (--current < 0) {
            current = shaders.length - 1;
        }
        return shaders[current];
    }

}
