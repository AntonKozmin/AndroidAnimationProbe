/*
 * Copyright 2019 these are based on the 'technicalmiracle.blogspot.com'
 * blog content. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blogspot.technicalmiracle;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

/**
 * A fragment using simple gestures to run Spring Animation for images with common and specified values.
 */
public class SpringAnimationFragment extends android.app.Fragment implements SeekBar.OnSeekBarChangeListener {

    public static final String TAG = "SpringAnimationFragment";

    private ViewGroup container;
    private LayoutInflater inflater;

    //images to animate
    private ImageView android, colorsCircle, gear;

    /**
     * Damping Ratio and Stiffness SeekBars.
     */
    private SeekBar bouncySeekBar, stiffnessSeekBar;

    /**
     * Check the animation state.
     */
    private boolean animationRunning;

    /**
     * The common apps DATA.
     */
    private AnimationDATA animationDATA;
    private ObjectAnimator gearAnimator;
    ObjectAnimator robotAnimator;

    private float seekBarsHeight;

    public SpringAnimationFragment() {
    }

    /**
     * @return view
     * A fragment init. Layout dimensions setup.
     */
    public View fragmentUInit() {

        View view;

        if (container != null) container.removeAllViews();

        view = inflater.inflate(R.layout.spring_physics_animation_screen, container, false);

        android = view.findViewById(R.id.android);
        colorsCircle = view.findViewById(R.id.colors);
        gear = view.findViewById(R.id.gear);
        view.setOnTouchListener(new TouchHandler());

        boolean resHadLoad=animationDATA.setLayoutDimensions(getContext(), true);
        if(!resHadLoad)Log.d(TAG, "Spring animation Fragment " +
                "- Layout dimensions had not loaded, use old one");

        bouncySeekBar = view.findViewById(R.id.bouncy_value_seekbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bouncySeekBar.setMin(10);
        }
        bouncySeekBar.setProgress(animationDATA.getDamp());

        stiffnessSeekBar = view.findViewById(R.id.stiffness_value_seekbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stiffnessSeekBar.setMin(50);
        }
        stiffnessSeekBar.setProgress(animationDATA.getSt());
        stiffnessSeekBar.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    /**
                     * Get SeekBar stiffness actual value of height after lay out it.
                     */
                    @Override
                    public void onGlobalLayout() {
                        seekBarsHeight = stiffnessSeekBar.getHeight();
                        stiffnessSeekBar
                                .getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                    }
                });
        stiffnessSeekBar.setOnSeekBarChangeListener(this);
        bouncySeekBar.setOnSeekBarChangeListener(this);
        android.getLayoutParams().width = (int) (animationDATA.getScaledSizeValue() * 0.20);
        android.getLayoutParams().height = (int) (animationDATA.getScaledSizeValue() * 0.20);
        robotAnimator = ObjectAnimator.ofObject(android,
                "backgroundColor",
                new ArgbEvaluator(),
                Color.parseColor("#FFFFFF"),  // white
                ContextCompat.getColor(getContext(), R.color.ic_launcher_background));
        robotAnimator.setDuration(10000);
        robotAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        robotAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        robotAnimator.start();
        colorsCircle.getLayoutParams().width = (int) (animationDATA.getScaledSizeValue() * 0.20);
        colorsCircle.getLayoutParams().height = (int) (animationDATA.getScaledSizeValue() * 0.20);
        gear.getLayoutParams().width = (int) (animationDATA.getScaledSizeValue() * 0.20);
        gear.getLayoutParams().height = (int) (animationDATA.getScaledSizeValue() * 0.20);
        gearAnimator = ObjectAnimator.ofFloat(gear,
                "rotation", 0f, 360f);
        gearAnimator.setInterpolator(new LinearInterpolator());
        gearAnimator.setDuration(1000);
        gearAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        gearAnimator.start();

        return view;
    }

    /**
     * @param seekBar a SeekBar which value has change
     * @param progress a new value
     * @param fromUser
     * On change the value of the SeekBar save current value into AnimationDATA.
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (seekBar.getId() == stiffnessSeekBar.getId()) {
                animationDATA.setSt(progress);
            } else {
                animationDATA.setDamp(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    /**
     * @param seekBar a SeekBar which value has change
     * On end of change show to user current Damping Ratio and Stiffness. A Layout invalidate.
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        Toast toast = Toast.makeText(getContext(),
                getText(R.string.stiffness_value) + " : " +
                        String.valueOf((float) stiffnessSeekBar.getProgress())
                        + ",\n" + getText(R.string.damping_ratio) + " : "
                        + String.valueOf(bouncySeekBar.getProgress() / 1000f),
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        container.removeAllViews();
        container.addView(fragmentUInit());
    }

    /**
     * Down to Up swipe performed. Start animator 4.
     * The values of the Stiffness and Damp Ratio SeekBars will be used.
     */
    private void startAnimator4() {
        if (!animationRunning) {
            animationRunning = true;
            SpringAnimation springAnim = new SpringAnimation(colorsCircle,
                    DynamicAnimation.TRANSLATION_Y,
                    animationDATA.getLayoutHeight() - seekBarsHeight -
                            animationDATA.getPixelSize(48) -
                            (animationDATA.getScaledSizeValue() / 5));
            springAnim.getSpring().setDampingRatio(animationDATA.getDamp() / 1000f);
            springAnim.getSpring().setStiffness((float) animationDATA.getSt());
//            Log.d(TAG, "Animator 4 final pos: " + springAnim.getSpring().getFinalPosition());
            stiffnessSeekBar.setEnabled(false);
            bouncySeekBar.setEnabled(false);
            springAnim.addEndListener((d, b, v, v1) -> atEndofAnimation());
            springAnim.start();
        }
    }

    /**
     * Up to Down swipe performed. Start animator 3.
     * The "MEDIUM" value constants from the SpringForce Class will be used.
     */
    private void startAnimator3() {
        if (!animationRunning) {
            animationRunning = true;
            SpringAnimation SpringAnim1 = new SpringAnimation(gear, DynamicAnimation.TRANSLATION_Y,
                    animationDATA.getLayoutHeight() - seekBarsHeight -
                            animationDATA.getPixelSize(48) -
                            (animationDATA.getScaledSizeValue() / 5));
            SpringAnim1.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
            SpringAnim1.getSpring().setStiffness(SpringForce.STIFFNESS_MEDIUM);
            stiffnessSeekBar.setEnabled(false);
            bouncySeekBar.setEnabled(false);
            SpringAnim1.addEndListener((d, b, v, v1) -> {
                gearAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                atEndofAnimation();
            });
            SpringAnim1.start();
        }
    }

    /**
     * Right to Left swipe performed. Start animator 2.
     * "HIGH" value constants from the SpringForce class will be used.
     */
    private void startAnimator2() {
        if (!animationRunning) {
            animationRunning = true;
            SpringAnimation SpringAnim2 = new SpringAnimation(android, DynamicAnimation.TRANSLATION_Y,
                    animationDATA.getLayoutHeight() - seekBarsHeight -
                            animationDATA.getPixelSize(48) -
                            (animationDATA.getScaledSizeValue() / 5));
            SpringAnim2.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
            SpringAnim2.getSpring().setStiffness(SpringForce.STIFFNESS_HIGH);
            stiffnessSeekBar.setEnabled(false);
            bouncySeekBar.setEnabled(false);
            SpringAnim2.addEndListener((d, b, v, v1) -> {
                robotAnimator.pause();
                atEndofAnimation();
            });
            SpringAnim2.start();
        }
    }

    /**
     * Left to Right swipe performed. Start animator 1.
     * "LOW" value constants from the SpringForce class will be used.
     */
    private void startAnimator1() {
        if (!animationRunning) {
            animationRunning = true;
            SpringAnimation SpringAnim3 = new SpringAnimation(colorsCircle, DynamicAnimation.TRANSLATION_Y,
                    animationDATA.getLayoutHeight() - seekBarsHeight -
                            animationDATA.getPixelSize(48) -
                            (animationDATA.getScaledSizeValue() / 5));
            SpringAnim3.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY);
            SpringAnim3.getSpring().setStiffness(SpringForce.STIFFNESS_VERY_LOW);
            stiffnessSeekBar.setEnabled(false);
            bouncySeekBar.setEnabled(false);
            SpringAnim3.addEndListener((d, b, v, v1) -> atEndofAnimation());
            SpringAnim3.start();
        }
    }

    /**
     * The SeekBars enable after all animations end.
     * Boolean value of animation state variable set to 'false'.
     */
    private synchronized void atEndofAnimation() {
        stiffnessSeekBar.setEnabled(true);
        bouncySeekBar.setEnabled(true);
        animationRunning = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        this.container = container;
        this.inflater = inflater;

        getActivity().setTitle(R.string.spring_physics_animation);

        animationDATA = ViewModelProviders.of((MainActivity) getActivity()).get(AnimationDATA.class);

        return fragmentUInit();
    }

    /**
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        // a layout redraw
        container.removeAllViews();
        container.addView(fragmentUInit());

        super.onConfigurationChanged(newConfig);
    }

    /**
     * A simple gesture detector for these moves : to the right, left, up, down.
     * Each gesture will run the linked Spring animation function with individual parameter set.
     */
    class TouchHandler implements View.OnTouchListener {

        float initialX, initialY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int action = event.getActionMasked();

            switch (action) {

                case MotionEvent.ACTION_DOWN:
                    initialX = event.getX();
                    initialY = event.getY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    break;

                case MotionEvent.ACTION_UP:
                    v.performClick();
                    float finalX = event.getX();
                    float finalY = event.getY();
                    if (Math.abs(initialX - finalX) >= Math.abs(initialY - finalY)) {
                        // it was a horizontal move
                        if (initialX < finalX) startAnimator1();
                        else startAnimator2();
                    } else {//It was a vertical move
                        if (initialY < finalY) startAnimator3();
                        else startAnimator4();
                    }
                    break;

                case MotionEvent.ACTION_CANCEL:
                    break;

                case MotionEvent.ACTION_OUTSIDE:
                    break;
            }
            return true;
        }
    } //end of the TouchHandler class
}


