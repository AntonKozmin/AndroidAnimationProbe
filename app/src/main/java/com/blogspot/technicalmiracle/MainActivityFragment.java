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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The main application fragment used to animate graphical and text elements placed on the screen.
 *
 * The screen of the fragment include the buttons to enter to another animation screens.
 */
public class MainActivityFragment extends android.app.Fragment {

    public static final String TAG = "MainActivityFragment";

    /**
     * ViewModel object for access to common apps DATA.
     */
    private AnimationDATA animationDATA;

    /**
     * The copy of the Cinnamon panel weather vector image from Linux "share" folder.
     */
    private ImageView weatherImage;

    /**
     * Load a series of Drawable resources one after another to create an animation.
     */
    private static AnimationDrawable weatherAnimator;

    private ViewGroup container;
    private LayoutInflater inflater;

    /**
     * A text label for the layout width.
     */
    private TextView labelForWidth;

    public MainActivityFragment() {
    }

    /**
     * @return view
     *
     * The view build then inflate it.
     */
    public View initializeUI() {

        View view;

        if (container != null) {
            container.removeAllViews();
        }
        view = inflater.inflate(R.layout.fragment_main, container, false);

        labelForWidth = view.findViewById(R.id.label1);
        labelForWidth.setOnClickListener(l -> firstLabelAnimator());
        TextView labelForHeight = view.findViewById(R.id.label2);
        labelForHeight.setOnClickListener(l -> secondLabelAnimator());

        //on change screen orientation label text set accordingly
        TextView displayModeLabel = view.findViewById(R.id.label3);
        if (animationDATA.isAPortraitMode())
            displayModeLabel.setText(R.string.current_orientation_V);
        else displayModeLabel.setText(R.string.current_orientation_H);
        TextView scaleValueLabel = view.findViewById(R.id.label4);

        //get a vector image
        weatherImage = view.findViewById(R.id.titleImageOnLayout);
        //start|stop animation with click on the shape/text
        weatherImage.setOnClickListener(l -> {
            if (weatherAnimator.isRunning()) weatherAnimator.stop();
            else weatherAnimator.start();
        });

        //adding a button to launch the Spring Animation Fragment
        Button springAnimationButton;
        springAnimationButton = view.findViewById(R.id.spring_button);
        // set up listener on click event for this button
        springAnimationButton.setOnClickListener(l ->
                ((MainActivity) getActivity()).loadFragment(new SpringAnimationFragment()));

        // adding a button to launch the Scene Animation Fragment
        Button sceneFragmentButton;
        sceneFragmentButton = view.findViewById(R.id.scene_button);
        // set up listener on click event for this button
        sceneFragmentButton.setOnClickListener(l ->
                ((MainActivity) getActivity()).loadFragment(new SceneAnimationFragment()));

        //adding a button to launch the Arbitrary Animation Fragment
        Button arbitraryFragmentButton;
        arbitraryFragmentButton = view.findViewById(R.id.arbitrary_animation);
        arbitraryFragmentButton.setOnClickListener(l ->
                ((MainActivity) getActivity()).loadFragment(new ArbitraryAnimationFragment()));
        // changing layout dimensions on layout change
        view.addOnLayoutChangeListener((v,
                                        left,
                                        top,
                                        right,
                                        bottom,
                                        oldLeft,
                                        oldTop,
                                        oldRight,
                                        oldBottom) -> {
            float layoutHeight = Math.abs(top - bottom);
            float layoutWidth = Math.abs(right - left);
            animationDATA.setLayoutDimensions(layoutWidth, layoutHeight);
            labelForWidth.setText(R.string.label_for_width);
            labelForWidth.append(String.valueOf((int) animationDATA.getLayoutWidth()));
            labelForWidth.append("\t");
            labelForHeight.setText(R.string.label_for_height);
            labelForHeight.append(String.valueOf((int) animationDATA.getLayoutHeight()));
            labelForHeight.append("\t");
            int scaledSizeValue = (int) animationDATA.getScaledSizeValue();
            scaleValueLabel.setText(R.string.label_for_scale_value);
            scaleValueLabel.append(String.valueOf(scaledSizeValue));
            scaleValueLabel.append("\t");
            //setting animator for vector drawable resource
            weatherImage.setBackgroundResource(R.drawable.weather_animation);
            weatherAnimator = (AnimationDrawable) weatherImage.getBackground();
            //using the base figure value size to set up sizes
            weatherImage.getLayoutParams().width = scaledSizeValue;
            weatherImage.getLayoutParams().height = scaledSizeValue;
        });

        return view;
    }

    /**
     * The first ObjectAnimator that animates coordinates along a path using two properties.
     */
    private ObjectAnimator objectAnimator;

    /**
     * The ObjectAnimator for move the text label.
     *
     * The final position of the text label accounted the current layout width and screen orientation mode.
     */
    private void firstLabelAnimator() {

        if (objectAnimator != null) objectAnimator.cancel();

        if (animationDATA.isAPortraitMode())
            objectAnimator = ObjectAnimator.ofFloat(labelForWidth,
                    "translationX", animationDATA.getLayoutWidth() * 0.25f);
        else objectAnimator = ObjectAnimator.ofFloat(labelForWidth,
                "translationX", animationDATA.getLayoutWidth() * 0.4f);

        objectAnimator.setDuration(2000);
        objectAnimator.start();
        // adding animation listener
        // reverse the move at the end point
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator a) {
                a.removeListener(this);
                objectAnimator.reverse();
                objectAnimator.end();
                objectAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator a) {
                a.removeListener(this);
                objectAnimator.reverse();
                objectAnimator.end();
                objectAnimator = null;
            }
        });
    }

    /**
     * The second Object Animator rotate the animated shape for 3 times.
     */
    private ObjectAnimator shapeRotate;

    /**
     * The second animator launched by the click on the layout height text label.
     *
     * The animated shape will rotate for 3 times. After finish the new one view will
     * be created to replace this one any others animators will canceled.
     */
    private void secondLabelAnimator() {

        if (shapeRotate != null) shapeRotate.cancel();

        shapeRotate = ObjectAnimator.ofFloat(weatherImage, "rotation", 360f);

        shapeRotate.setInterpolator(new DecelerateInterpolator());
        shapeRotate.setDuration(2400);
        shapeRotate.setRepeatCount(3);
        shapeRotate.start();

        // adding animation listener
        shapeRotate.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator a) {
                a.removeListener(this);
                container.removeAllViews();
                container.addView(initializeUI());
            }

            @Override
            public void onAnimationCancel(Animator a) {
                a.removeListener(this);
                container.removeAllViews();
                container.addView(initializeUI());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        this.container = container;
        this.inflater = inflater;

        getActivity().setTitle(R.string.drawable_graphics_animation);

        animationDATA = ViewModelProviders.of((MainActivity) getActivity()).get(AnimationDATA.class);

        return initializeUI();
    }

    @Override
    public void onStart() {

        super.onStart();
    }

    /**
     * @param newConfig Setting the screen orientation value into animation data class. Reinitialize a view.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        // on change display orientation inflate the other layout and recompute it

        container.addView(initializeUI());

        super.onConfigurationChanged(newConfig);
    }

    /**
     * All animation stop.
     */
    @Override
    public void onPause() {

        if (weatherAnimator != null)
            if (weatherAnimator.isRunning())
                weatherAnimator.stop();

        if (shapeRotate != null)
            if (shapeRotate.isRunning())
                shapeRotate.pause();

        if (objectAnimator != null)
            if (objectAnimator.isRunning())
                objectAnimator.pause();

        super.onPause();
    }

    /**
     * When any object animator previously ran start it again.
     */
    @Override
    public void onResume() {

        if (shapeRotate != null)
            if (shapeRotate.isPaused())
                shapeRotate.start();

        if (objectAnimator != null)
            if (objectAnimator.isPaused())
                objectAnimator.start();

        super.onResume();
    }


}
