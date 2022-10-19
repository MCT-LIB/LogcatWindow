package com.mct.logcatwindow.utils;

import android.view.animation.Interpolator;

import androidx.annotation.NonNull;

public class SpringInterpolator implements Interpolator {
    // curve parameters generated with
    // https://www.desmos.com/calculator/6gbvrm5i0s
    private static final double BOUNCY_AMPLITUDE = 0.13;
    private static final double BOUNCY_FREQUENCY = 13.5;

    private static final double SOFT_AMPLITUDE = 0.2;
    private static final double SOFT_FREQUENCY = 6.8;

    double mAmplitude = BOUNCY_AMPLITUDE;
    double mFrequency = BOUNCY_FREQUENCY;

    public SpringInterpolator() {
    }

    public SpringInterpolator(double amplitude, double frequency) {
        mAmplitude = amplitude;
        mFrequency = frequency;
    }

    @NonNull
    public static SpringInterpolator bouncySpring() {
        return new SpringInterpolator(BOUNCY_AMPLITUDE, BOUNCY_FREQUENCY);
    }

    @NonNull
    public static SpringInterpolator softSpring() {
        return new SpringInterpolator(SOFT_AMPLITUDE, SOFT_FREQUENCY);
    }

    public float getInterpolation(float time) {
        return (float) (-1 * Math.pow(Math.E, -time / mAmplitude) * Math.cos(mFrequency * time) + 1);
    }
}
