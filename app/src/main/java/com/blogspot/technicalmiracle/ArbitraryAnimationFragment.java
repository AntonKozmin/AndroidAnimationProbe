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
import android.content.ClipData;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;


/**
 * This Fragment include the animated background formed by Canvas and
 * the image arbitrary chosen from the apps resources. The image may be dragged additionally.
 */
public class ArbitraryAnimationFragment extends android.app.Fragment {

    public static final String TAG = "ArbitraryAnimationFragment";

    /**
     * The animated shapes array to generate an arbitrary background.
     */
    ArrayList<DrawView.FigureToDraw> figures = new ArrayList<>();

    /**
     * An array to save figures onPause.
     */
    ArrayList<DrawView.FigureToDraw> figuresOnPause = new ArrayList<>();

    /**
     * Managing the animation timer.
     */
    private boolean stopTimer = false;

    /**
     * The local values set by animationDATA.
     */
    private float scaledSizeValue, width, height;

    /**
     * The common apps DATA.
     */
    private AnimationDATA animationDATA;

    /**
     * The actual pixel size.
     */
    private int pxl;

    private ViewGroup container;

    /**
     * A random image from any available image resource.
     */
    private ImageView rImage;

    /**
     * The index for choose an image resource
     */
    private int chosenImage = -1;

    /**
     * The array of available drawable/mipmap images
     */
    private final int[] imagesIds = {R.drawable.ic_amoebax,
            R.drawable.ic_colors_circle, R.drawable.ic_bender, R.mipmap.android};

    public ArbitraryAnimationFragment() {
    }

    /**
     * @return the view object formed by Canvas
     *
     * This function intended to create the view formed by Android Canvas class.
     * When this device has a software Navigation Bar this bar will be
     * disabled on Arbitrary Animation screen with the Landscape mode.
     * The layout width will change with this occasion
     * to draw correct final position of shape on drag.
     */
    public View initCanvasUI() {

        if (container != null) container.removeAllViews();

        View decorView = this.getActivity().getWindow().getDecorView();

        if (animationDATA.isAPortraitMode()) {
            // View flags clear on change screen orientation
            decorView.setSystemUiVisibility(0);
        } else { // cut out the soft NavigationBar
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        boolean resHadLoad=animationDATA.setLayoutDimensions(getContext(), true);
        if(!resHadLoad)Log.d(TAG, "Arbitrary animation Fragment " +
                "- Layout dimensions had not loaded, use old one");

        scaledSizeValue = animationDATA.getScaledSizeValue();
        width = animationDATA.getLayoutWidth();
        height = animationDATA.getLayoutHeight();

        DrawView drawView = new DrawView(getContext());
        //adding the Drag and Drop listener
        drawView.setOnDragListener(new AnimationViewDragAndDropListener());

        return drawView;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        this.container = container;
        animationDATA = ViewModelProviders.of((MainActivity) getActivity()).get(AnimationDATA.class);
        getActivity().setTitle(R.string.arbitrary_animation);
        stopTimer = false;

        return initCanvasUI();
    }

    @Override
    public void onStart() {

        stopTimer = false;
        chooseAnImage();

        super.onStart();
    }

    /**
     * Animation Timer will stopped.
     * The current graphical figures set (the current screen) will saved into the list.
     */
    @Override
    public void onPause() {

        //Saving the current graphical figures set into the list
        figuresOnPause = new ArrayList<>(figures);
        stopTimer = true;

        super.onPause();
    }

    /**
     * The image load. Restoring a last condition of the screen. Animation timer start.
     */
    @Override
    public void onResume() {

        container.removeAllViews();
        //The graphical figures set restore from the saved list
        figures = new ArrayList<>(figuresOnPause);
        container.addView(initCanvasUI());
        stopTimer = false;
        chooseAnImage();

        super.onResume();
    }

    @Override
    public void onStop() {

        stopTimer = true;

        super.onStop();
    }

    /**
     * @param newConfig
     * The layout will redraw.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        container.removeAllViews();
        stopTimer = false;
        container.addView(initCanvasUI());
        chooseAnImage();

        super.onConfigurationChanged(newConfig);
    }

    /**
     * An image view chose/invalidate
     */
    private void chooseAnImage() {

        if (chosenImage < 0) { //did the image already chose?

            int id = (int) (Math.random() * (imagesIds.length));
            rImage = new ImageView(getContext());
            container.addView(rImage);
            chosenImage = imagesIds[id];

            //adding listener to a chosen image
            rImage.setOnLongClickListener(l -> {
                // start drag coloring the image with lime paint on drag move
                ClipData data = ClipData.newPlainText("", "");
                rImage.setColorFilter(0xff00ff00, PorterDuff.Mode.MULTIPLY);
                View.DragShadowBuilder sb = new View.DragShadowBuilder(rImage);
                rImage.startDragAndDrop(data, sb, rImage, 0);
                rImage.setVisibility(View.INVISIBLE);
                return true;
            });
            prepareAnImage();
        } else {
            container.removeView(rImage);
            container.addView(rImage);
            prepareAnImage();
        }
    }

    /**
     * The chosen image prepare
     */
    private void prepareAnImage(){

        int chosenImageSize = (int) scaledSizeValue / 4;

        //The special image creation procedure will applied when the Face of Bender happened
        if (chosenImage == R.drawable.ic_bender) {
            chosenImageSize = (int) scaledSizeValue / 3;
            rImage.setPadding(0, animationDATA.getPixelSize(10),
                    0, animationDATA.getPixelSize(10)
            );
            //The background bend for more gold
            rImage.setBackgroundColor(Color.rgb(237, 179, 7));
        }
        if (chosenImage == R.mipmap.android) {// An android cookie prepare to turn into animate
            rImage.setPadding(0, animationDATA.getPixelSize(12),
                    0, animationDATA.getPixelSize(12)
            );
            rImage.setBackgroundColor(ContextCompat.getColor(
                    getContext(), R.color.ic_launcher_background));
            ObjectAnimator robotAnimator = ObjectAnimator.ofObject(rImage,
                    "backgroundColor",
                    new ArgbEvaluator(),
                    ContextCompat.getColor(getContext(), R.color.ic_launcher_background),
                    Color.rgb(253,252,63));
            robotAnimator.setDuration(3000);
            robotAnimator.setRepeatMode(ObjectAnimator.REVERSE);
            robotAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            robotAnimator.start();
        }
        rImage.setImageResource(chosenImage);
        rImage.getLayoutParams().height = chosenImageSize;
        rImage.getLayoutParams().width = chosenImageSize;
        rImage.setX((width - chosenImageSize) / 2);
        rImage.setY((height - chosenImageSize) / 2);
    }


    /**
     * The custom animated 'View' class based on Canvas.
     *
     * This class extends the Android View class using it's onDraw function to generate
     * dynamical changing background. The background make up by the random generated shapes.
     * The background refresh every 110 ms.
     * Each frame of background's animation create as the follower of the previous
     * and include the most of the previous shapes with changed geometrical dimensions.
     */
    class DrawView extends View {

   //     public static final String TAG = "ArbitraryAnimationFragment.DrawView";

        /**
         * The internal animation timer.
         */
        BackgroundAnimationTimer animationTimer = new BackgroundAnimationTimer();

        /**
         * The Thread pool to change/replace figures.
         */
        private ForkJoinPool thPool = new ForkJoinPool();

        /**
         * The array of figures indexes to replace figures after an invalidate() function invoke.
         */
        private ArrayList<Integer> indexesOfFiguresToReplace;

        /**
         * A Drawing figures array size.
         */
        private int fSize;

        Handler handler = new Handler(Looper.getMainLooper());
        Paint paint = new Paint();
        Random random = new Random();

        /**
         * The medium radius used to filter shapes.
         */
        final int radiusMed = (int) (scaledSizeValue * 0.32);

        public DrawView(Context context) {
            super(context);
            //get actual pixel size
            pxl = animationDATA.getPixelSize(1);
            //animation start
            animationTimer.run();
        }

        /**
         * The class represent the graphical figure
         * includes the color paint, rectangle and circle objects.
         */
        private class FigureToDraw {
            BackGroundRectangle rectangle;
            BackGroundCircle circle;
            BackGroundPaints pt;

            private FigureToDraw(BackGroundRectangle rectangle,
                                 BackGroundCircle circle,
                                 BackGroundPaints pt) {
                this.rectangle = rectangle;
                this.circle = circle;
                this.pt = pt;
            }
        }

        /**
         * The class represent the circle into a figure object.
         */
        private class BackGroundRectangle {
            float top;
            float bottom;
            float left;
            float right;

            private BackGroundRectangle(float top, float bottom, float left, float right) {
                this.top = top;
                this.bottom = bottom;
                this.left = left;
                this.right = right;
            }
        }

        /**
         * The class represent the rectangle into a figure object.
         */
        private class BackGroundCircle {
            int cx;
            int cy;
            int rd;

            private BackGroundCircle(int cx, int cy, int rd) {
                this.cx = cx;
                this.cy = cy;
                this.rd = rd;
            }
        }

        /**
         * The class defined to represent the figure color paint.
         * The paint of this class is a simple color paint included an alpha value.
         */
        private class BackGroundPaints {
            int red;
            int blue;
            int green;
            int alpha;

            private BackGroundPaints(int red, int blue, int green, int alpha) {
                this.red = red;
                this.blue = blue;
                this.green = green;
                this.alpha = alpha;
            }
        }

        /**
         * @param canvas
         * A Background redraw. The set of circles on Portrait Mode or the set of rectangles on Landscape Mode.
         */
        @Override
        public void onDraw(Canvas canvas) {

            for (int i = 0; i < fSize; i++) {
                paint.setARGB(figures.get(i).pt.alpha,
                        figures.get(i).pt.red,
                        figures.get(i).pt.green,
                        figures.get(i).pt.blue);
                int cx = figures.get(i).circle.cx;
                int cy = figures.get(i).circle.cy;
                int rd = figures.get(i).circle.rd;
                float t = figures.get(i).rectangle.top;
                float b = figures.get(i).rectangle.bottom;
                float r = figures.get(i).rectangle.right;
                float l = figures.get(i).rectangle.left;
                if (animationDATA.isAPortraitMode()) {
                    canvas.drawCircle(cx, cy, rd, paint);
                } else {
                    canvas.drawRect(l, t, r, b, paint);
                }
            }
            changeFigures();
        }

        /**
         * Shapes calculators will execute on thread pool after the application layout redraw.
         */
        private void changeFigures() {
            if (indexesOfFiguresToReplace == null) indexesOfFiguresToReplace = new ArrayList<>();
            // invalidate() then all shapes will recomputed
            figures.forEach(fi -> thPool.execute(new FigureCalculator(fi)));
        }

        /**
         * The FigureCalculator using by a ForkJoin thread pool.
         *
         * Here could happen simultaneous access to the figures/figures indexes array to take changes.
         * The result accumulator is a 'synchronized' function changeFigure().
         */
        private class FigureCalculator implements Runnable {

            int i, rd;
            float t, b;
            float r, l;

            /**
             * @param fi figure parameters to change
             */
            FigureCalculator(FigureToDraw fi) {
                this.i = figures.indexOf(fi);
                this.rd = fi.circle.rd;
                this.t = fi.rectangle.top;
                this.b = fi.rectangle.bottom;
                this.r = fi.rectangle.right;
                this.l = fi.rectangle.left;
            }

            @Override
            public void run() {

                if (fSize > i) { //Index bound check

                    if (animationDATA.isAPortraitMode()) {

                        if (rd > radiusMed && fSize > 0) {
                            changeFigure(i);
                        } else {
                            rd = rd + pxl;
                            changeFigure(i, rd);
                        }

                    } else {// the absolute values of the rectangle
                          // sides compute by normalized operations

                        int tb = (int) (t - b), rl = (int) (r - l);

                        tb = (tb ^ (tb >> 31)) - (tb >> 31);
                        rl = (rl ^ (rl >> 31)) - (rl >> 31);

                        if ((tb >= height || rl >= width) && fSize > 0) {
                            changeFigure(i);
                        } else {
                            t = t - pxl;
                            b = b + pxl;
                            r = r + pxl;
                            l = l - pxl;
                            changeFigure(i, r, l, t, b);
                        }
                    }
                }
            }
        }

        /**
         * @param i index which will add to the array of figure indexes to replace
         *
         * By the the same 'i' index saved both of shapes - the rectangle and the circle.
         * The first signature of the changeFigure function intend to create an array of indexes to replace.
         */
        private synchronized void changeFigure(int i) {
            indexesOfFiguresToReplace.add(i);
        }

        /**
         * @param i an index of 'figures' array rectangle to change
         *
         * The second signature of the changeFigure function intend to change the radius of the circle.
         */
        private synchronized void changeFigure(int i, int rd) {
            figures.get(i).circle.rd = rd;
        }

        /**
         * @param i an index of 'figures' array rectangle to change
         *
         * The third signature of the changeFigure function intend to change the rectangle dimensions.
         */
        private synchronized void changeFigure(int i, float r, float l, float t, float b) {
            figures.get(i).rectangle.right = r;
            figures.get(i).rectangle.left = l;
            figures.get(i).rectangle.top = t;
            figures.get(i).rectangle.bottom = b;
        }

        /**
         * @param index index of shape to replace
         *
         * A shape replace with new one.
         */
        private synchronized void replaceFigure(int index) {

            figures.set(index, new FigureToDraw(
                    new BackGroundRectangle(
                            random.nextInt((int) height),
                            random.nextInt((int) height),
                            random.nextInt((int) width),
                            random.nextInt((int) width)),
                    new BackGroundCircle(
                            random.nextInt((int) width),
                            random.nextInt((int) height),
                            random.nextInt((int) (scaledSizeValue * 0.25))),
                    new BackGroundPaints(
                            random.nextInt(256),
                            random.nextInt(256),
                            random.nextInt(256),
                            random.nextInt(256))));
        }

        /**
         *  The internal animation timer of the DrawView class.
         *
         *  A default delay value set to 6 ms.
         *  - at 10 ms a graphical figure will be added if the
         *  shapes total < 260.
         *  - at 30 ms the graphical figures intended to change will replace by
         *  newly generated figures
         *  - at 80 ms the background will redraw
         *  - at 110 ms a counter will reset
         */
        private class BackgroundAnimationTimer implements Runnable {

            long timerCounter = 0;

            @Override
            public void run() {

                timerCounter++; // 0 ms

                if (!stopTimer) {

                    if (timerCounter == 1)// 10 ms
                        if (fSize < 260) {
                            figures.add(new FigureToDraw(// a one newly shape add
                                    new BackGroundRectangle(
                                            random.nextInt((int) height),
                                            random.nextInt((int) height),
                                            random.nextInt((int) width),
                                            random.nextInt((int) width)),
                                    new BackGroundCircle(
                                            random.nextInt((int) width),
                                            random.nextInt((int) height),
                                            random.nextInt((int) (scaledSizeValue * 0.25))),
                                    new BackGroundPaints(
                                            random.nextInt(256),
                                            random.nextInt(256),
                                            random.nextInt(256),
                                            random.nextInt(256))));
                        }
                    if (timerCounter == 3) { // 30ms
                        if (indexesOfFiguresToReplace != null && !indexesOfFiguresToReplace.isEmpty()) {
                            // shapes replace
                            indexesOfFiguresToReplace.forEach(DrawView.this::replaceFigure);
                            indexesOfFiguresToReplace.clear();
                        }
                        fSize = figures.size();
                    }
                    if (timerCounter == 8) { // 80 ms
                        invalidate(); //will trigger the onDraw
                    }
                    if (timerCounter == 11) timerCounter = 0;
                    //a timer post a message every 6 ms
                    handler.postDelayed(this, 6);

                } else {
                    //a timer stop
                    handler.removeCallbacks(animationTimer);
                }
            }
        } // end of The internal animation timer of the DrawView class.
    } // end of DrawView class

    /**
     * A Specialized Drag Listener to the shape created by the custom animated view class.
     *
     * Class's internal onDrag function use translated values for final points of the drag motion.
     * A arbitrary generated Custom Animated shape could not cross a layout border.
     * When a drag motion hasn't finished into application screen area this listener
     * will use the same method used to a layout cross happen.
     */
    private class AnimationViewDragAndDropListener implements View.OnDragListener {

        /**
         * 'true' if a drag motion hasn't finished into application
         * screen area
         */
        boolean isADragAreaCrossed = false;

        @Override
        public boolean onDrag(View v, DragEvent event) {

            float endY = event.getY();
            float endX = event.getX();
            float fullSizeX = rImage.getWidth();
            float fullSizeY = rImage.getHeight();
            float halfSizeX = fullSizeX * 0.5f;
            float halfSizeY = fullSizeY * 0.5f;
            
            switch (event.getAction()) {

                case DragEvent.ACTION_DRAG_STARTED:
                    break;

                case DragEvent.ACTION_DRAG_ENTERED:
                    break;

                case DragEvent.ACTION_DRAG_EXITED:
                    isADragAreaCrossed = true;
                    break;

                case DragEvent.ACTION_DRAG_LOCATION:
                    break;

                case DragEvent.ACTION_DROP:

                    if (endX <= halfSizeX) {
                        endX = 0;
                    } else if (endX > halfSizeX && endX < width - halfSizeX) {
                        endX = endX - halfSizeX;
                    } else {
                        endX = width - fullSizeX;
                    }

                    if (endY <= halfSizeY) {
                        endY = 0;
                    } else if (endY >= height - halfSizeY) {
                        endY = height - fullSizeY;
                    } else {
                        endY = endY - halfSizeY;
                    }

                    rImage.setY(endY);
                    rImage.setX(endX);
                    rImage.clearColorFilter();
                    rImage.setVisibility(View.VISIBLE);
                    break;

                case DragEvent.ACTION_DRAG_ENDED:

                    if (isADragAreaCrossed) { // the application area was crossed on drag

                        // a translated zero point for Y
                        float lYzero = animationDATA.getStatusBarHeight()
                                + animationDATA.getActionBarHeight();

                        if (animationDATA.isAPortraitMode()) {

                            if (endY < height * 0.5) {
                                endY = 0;
                            } else {
                                endY = height - fullSizeX;
                            }

                            if (endX <= halfSizeX) {
                                endX = 0;
                            } else if (width - halfSizeX > endX) {
                                endX = endX - halfSizeX;
                            } else if (width - halfSizeX <= endX) {
                                endX = width - fullSizeX;
                            }

                        } else {

                            float tY = endY - lYzero;

                            if (tY <= 0) {
                                endY = 0;
                            } else if (0 < tY && tY <= height - halfSizeY) {
                                endY = tY - halfSizeY;
                            } else endY = height - fullSizeX;

                            if (endX <= halfSizeX) {
                                endX = 0;
                            } else if (endX > halfSizeX && endX < width - halfSizeX) {
                                endX = endX - halfSizeX;
                            } else {
                                endX = width - fullSizeX;
                            }
                        }

                        rImage.setX(endX);
                        rImage.setY(endY);
                        rImage.clearColorFilter();
                        rImage.setVisibility(View.VISIBLE);
                        isADragAreaCrossed = false;
                    }
                    break;

                default:
                    break;
            }
            return true;
        }
    }  // end of A Specialized Drag Listener

}