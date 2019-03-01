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

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.transition.AutoTransition;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This fragment start with Scene animation, scene change can be done on long click.
 * <p>
 * On short touch the an animated circle will be draw.
 * When a scroll gesture is going the stroke draw on the screen.
 * Then a canvas do draw the gesture path include nodes. As a worm move.
 * A velocity and acceleration parameters of drawing define by this gesture.
 * When the animation has finished the generated figure will remove.
 * From the start point to end point will be add the onFling animation move
 * with parameters defined by the total path / total move time.
 */
public class SceneAnimationFragment extends android.app.Fragment {

    public static final String TAG = "SceneAnimationFragment";

    /**
     * The common apps DATA.
     */
    private AnimationDATA animationDATA;

    /**
     * A Strokes array create on Scroll gesture.
     */
    private AnimatedPath animatedPath;

    /**
     * The Array of animated line's arrays to save an animation state onPause.
     */
    private HashMap<String, AnimatedPath> savedAnimatedPath = new HashMap<>();

    /**
     * The Array of animated circles arrays to save the animation state OnPause.
     */
    private HashMap<String, AnImageView> savedAnimatedCircles = new HashMap<>();

    /**
     * The Array of onFling animations to save the animation state OnPause.
     */
    private HashMap<String, FlingMotionView> flingMotionViews = new HashMap<>();

    /**
     * The original set of scenes.
     */
    private LinkedList<Integer> sceneNr=new LinkedList<>();

    /**
     * Indexes of text cells are showing on the screen.
     */
    private Map<String, Integer> tabPlaces = new HashMap<>();

    /**
     * Text labels are showing on the screen
     */
    private ArrayList<android.support.v7.widget.AppCompatImageView>
            onFlingTexts = new ArrayList<>();

    /**
     * The minimum size of layout intend to compute a graphical figure dimensions.
     */
    private float scaledSizeValue;

    /**
     * Switching on pause.
     */
    private boolean pause;

    /**
     * The id's array of character faces to output on the screen.
     */
    final private int[] faces = {R.mipmap.benderwood, R.mipmap.countess, R.mipmap.calculon,
            R.mipmap.robo, R.mipmap.fat, R.mipmap.clamp, R.mipmap.close, R.mipmap.izac,
            R.mipmap.malfuctioning, R.mipmap.roberto, R.mipmap.robot, R.mipmap.url,
            R.mipmap.amy, R.mipmap.bob, R.mipmap.bot, R.mipmap.brain,
            R.mipmap.cibert, R.mipmap.dennis, R.mipmap.elzar, R.mipmap.farnsworth,
            R.mipmap.fry, R.mipmap.hermes, R.mipmap.hypnotoad, R.mipmap.bubblegum,
            R.mipmap.kiff, R.mipmap.leela, R.mipmap.nibbler, R.mipmap.nichelle,
            R.mipmap.nixon, R.mipmap.pamela, R.mipmap.zapp, R.mipmap.zolberg};

    private ViewGroup container;
    private LayoutInflater inflater;

    Random random=new Random();

    public SceneAnimationFragment() {
    }

    /**
     * @return fragment view.
     * <p>
     * A root scene initialize . OnTouch listener add.
     */
    public View SceneFragmentInit() {

        View view;

        if (container != null) container.removeAllViews();
        // the root layout for this fragment
        view = inflater.inflate(R.layout.scene_animation_screen, container, false);

        view.setOnTouchListener(new AnimatedFragmentTouchListener(getContext()));

        return view;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        this.container = container;
        this.inflater = inflater;

        //The list of scene numbers create.
        sceneNr=random.ints(1, 4)
                .boxed()
                .distinct()
                .limit(3)
                .collect(Collectors.toCollection(LinkedList::new));

        animationDATA = ViewModelProviders.of((MainActivity) getActivity()).get(AnimationDATA.class);

        getActivity().setTitle(R.string.layout_transition);

        return SceneFragmentInit();
    }


    /**
     * A Layout create then doing the enter into a fragment screen.
     */
    @Override
    public void onStart() {

        animateIt(getView(), true);

        animationDATA.setFirstStartCompleted();
        pause = false;

        super.onStart();
    }

    @Override
    public void onPause() {
        pause = true;
        super.onPause();
        onFlingTexts.forEach(container::removeView);
        onFlingTexts.clear();// text labels remove
        tabPlaces.clear();
    }

    @Override
    public void onResume() {

        getActivity().setTitle(R.string.layout_transition);
        pause = false;
        // the animation state restore
        if ((savedAnimatedPath != null) && (!savedAnimatedPath.isEmpty()))
            savedAnimatedPath.forEach((id, ap) -> ap.animationTimer.run());
        if ((savedAnimatedCircles != null) && (!savedAnimatedCircles.isEmpty()))
            savedAnimatedCircles.forEach((id, ac) -> ac.animationTimer.run());

        super.onResume();
    }

    /**
     * @param newConfig
     * A view redraw.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        pause = true; // an animation timer will stop
        savedAnimatedPath.clear(); // clearing animation
        savedAnimatedCircles.clear();
        pause = false;
        animateIt(getView(), false); // at start load new Scene

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onStop() {
        pause = true;
        super.onStop();
    }

    /**
     * @param view          a view
     * @param creatingFrame When has sent a "true" the enter to the scene will begin with the transition
     *                      otherwise scene to scene transition will be done.
     */
    void animateIt(View view, boolean creatingFrame) {

        Scene scene;

        ViewGroup sceneRoot = view.findViewById(R.id.scene_root);

        // moving along the scenes list to load a next scene.
        Integer sceneIndex=sceneNr.removeFirst();
        scene = new Scene(sceneRoot, createSceneView(sceneIndex));
        sceneNr.addLast(sceneIndex);

        // Transition animation create
        Transition transition = new AutoTransition();
        transition.setDuration(1200);

        //When this fragment load again the animation will not run
        if (!creatingFrame) TransitionManager.go(scene, transition);
        else if (animationDATA.isFirstStartCompleted())
            scene.enter();
        else if (!animationDATA.isFirstStartCompleted())
            TransitionManager.go(scene, transition);
    }

    /**
     * @param sceneNr a chosen scene number (1,2,3)
     * @return a constructed scene view
     */
    private View createSceneView(int sceneNr) {

        View view;

        if (sceneNr == 1) view = inflater.inflate(R.layout.first_scene, container, false);
        else if (sceneNr == 2) view = inflater.inflate(R.layout.second_scene, container, false);
        else view = inflater.inflate(R.layout.third_scene, container, false);

        // a layout properties setup
        boolean dimSetApproved = animationDATA.setLayoutDimensions(getContext(), false);
        if (dimSetApproved) {
            scaledSizeValue = animationDATA.getScaledSizeValue();
        } else {
            Log.d(TAG, "Scene animation Fragment - Layout dimensions had not loaded , will use those previous");
        }

        // Image placeholders for this scene.
        final LinkedList<Integer> placeHolders = new LinkedList<>(
                Arrays.asList(R.id.img1, R.id.img2, R.id.img3, R.id.img4, R.id.img5));
        /*
         * A scene fill by random icons.
         * see : https://docs.oracle.com/javase/8/docs/api/java/util/Random.html
         * for more information.
         */
        random.ints(0, faces.length)
                .boxed()
                .distinct()
                .limit(placeHolders.size())
                .collect(Collectors.toList())
                .forEach(imagesIndex -> {
                    ImageView image = view.findViewById(placeHolders.removeFirst());
                    image.setImageResource(faces[imagesIndex]);
                    image.getLayoutParams().width = (int) (scaledSizeValue * 0.22);
                    image.getLayoutParams().height = (int) (scaledSizeValue * 0.22);
                });

        return view;
    }

    /**
     * A Touch listener included a gesture detector.
     */
    class AnimatedFragmentTouchListener
            implements View.OnTouchListener,
            GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

        private GestureDetectorCompat mDetector;
        MotionEvent event;
        float x = -1, y = -1;

        AnimatedFragmentTouchListener(Context context) {
            mDetector = new GestureDetectorCompat(context, this);
            mDetector.setOnDoubleTapListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (this.mDetector.onTouchEvent(event)) {
                return true;
            }
            if (event.getAction()==MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Context context = getContext();
            if(context!=null) {
                FlingMotionView flingMotionView =
                        new FlingMotionView(context, event1, event2, velocityX, velocityY);
                if (animatedPath != null) {//the same ID as the current animated path have
                    //onFling motion always started when onScroll event ended
                    String ID = animatedPath.getID();
                    flingMotionView.setID(ID);
                    flingMotionViews.put(ID, flingMotionView);
                }
                flingMotionView.anFling(); //a translucent message show
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            animateIt(getView(), false);
        }

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX,
                                float distanceY) {
            //               The new onScroll gesture start
            if ((x != event1.getX() && y != event1.getY()) || (event == null)) {
                Context context = getContext();
                if (context != null) {
                    animatedPath = new AnimatedPath(context);
                    event = event1;
                    x = event1.getX();
                    y = event1.getY();
                    container.addView(animatedPath);
                    animatedPath.drawPath(event1, event2);
                    String id = UUID.randomUUID().toString();
                    savedAnimatedPath.put(id, animatedPath);
                    animatedPath.setID(id);
                }
            }
            //       It's the same onScroll gesture
            if (event.equals(event1) && (animatedPath != null)) {
                animatedPath.drawPath(event2);
            }
            return true;
        }

        @Override
        public void onShowPress(MotionEvent event) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            Context context = getContext();
            if (context != null) {
                AnImageView animatedCircle = new AnImageView(context);
                animatedCircle.radius = scaledSizeValue * 0.07f;
                animatedCircle.cX = event.getX();
                animatedCircle.cY = event.getY();
                container.addView(animatedCircle);
                animatedCircle.startAnimation();
                String id = UUID.randomUUID().toString();
                savedAnimatedCircles.put(id, animatedCircle);
                animatedCircle.setID(id);
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            return true;
        }
    }//==== End of Touch Listener class====

    /**
     * The Animated circle draw on Single tap event.
     * <p>
     * This class is a subclass of the generic Android ImageView class.
     * It implement own onDraw function.
     */
    class AnImageView extends android.support.v7.widget.AppCompatImageView {

        public static final String TAG = "SceneAnimationFragment.AnImageView";
        float cX = 0, cY = 0, radius, radStart;
        private boolean anStop = false, screenOrientation;
        private String ID; // the figure ID to manage animation

        Handler handler = new Handler(Looper.getMainLooper());
        AnimationTimer animationTimer = new AnimationTimer();
        Paint paint = new Paint();

        void setID(String ID) {
            this.ID = ID;
        }

        /**
         * @param context app context
         *                the local screen orientation value set up
         */
        public AnImageView(Context context) {
            super(context);
            screenOrientation = animationDATA.isAPortraitMode();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawCircle(cX, cY, radius, paint);
            super.onDraw(canvas);
        }

        public void startAnimation() {
            radStart = radius;
            paint.setColor(Color.rgb((int) (200 * Math.random()),
                    (int) (200 * Math.random()), (int) (200 * Math.random())));
            animationTimer.run();
        }

        private void removeAnView() {
            container.removeView(this);
            savedAnimatedCircles.remove(ID);
        }

        /**
         * The inner timer class of the animated circle class
         */
        private class AnimationTimer implements Runnable {

            @Override
            public void run() {

                Activity a = getActivity();

                if (a != null) {

                    if ((0 >= radius) ||
                            (screenOrientation != animationDATA.isAPortraitMode()))
                        anStop = true;

                    if (!pause) {

                        if (anStop) {
                            handler.removeCallbacks(animationTimer);
                            removeAnView();
                        } else {
                            handler.postDelayed(this, 100);
                            paint.setAlpha((int) ((radius / radStart) * 255));
                            radius--;
                            invalidate();
                        }

                    } else {

                        handler.removeCallbacks(animationTimer);

                    }

                } else {//after app stopped animation stop and remove a current image view

                    handler.removeCallbacks(animationTimer);
                    removeAnView();
                }
            }
        }
    }// ==== End of Draw animated circle on Single tap event ====

    /**
     * The animated path class for an onScroll event.
     * <p>
     * This class is a subclass of the generic Android ImageView class.
     * It implement own onDraw function.
     */
    class AnimatedPath extends android.support.v7.widget.AppCompatImageView {

        public static final String TAG = "SceneAnimationFragment.AnimatedPath";
        private float[] originalLines;
        private float xStart, yStart, xEnd, yEnd;
        private int totalLines, lineAlpha = 255;
        private boolean anStart = false, anStop = false, screenOrientation;
        private Paint paint = new Paint();
        private Handler handler = new Handler(Looper.getMainLooper());
        private AnimationTimer animationTimer = new AnimationTimer();
        private ArrayList<float[]> linesToDraw = new ArrayList<>();
        private ArrayList<float[]> compareLines = new ArrayList<>();
        private int chCounter = 0;
        private String ID;
        private long delay = 120;

        /**
         * @param context a default constructor of an AppCompatImageView class also
         *                contain the initializer for the local screen orientation value
         */
        public AnimatedPath(Context context) {
            super(context);
            screenOrientation = animationDATA.isAPortraitMode();
        }

        void setID(String GeneratedID) {
            ID = GeneratedID;
        }

        public String getID() {
            return ID;
        }

        @Override
        protected void onDraw(Canvas canvas) {

            if (!anStart)
                linesToDraw.forEach(l -> canvas.drawLine(l[0], l[1], l[2], l[3], paint));
            else
                for (int i = 0; i < totalLines; i++) {
                    canvas.drawLine(
                            linesToDraw.get(i)[0],
                            linesToDraw.get(i)[1],
                            linesToDraw.get(i)[2],
                            linesToDraw.get(i)[3],
                            calcPaint(i));
                }

            super.onDraw(canvas);
        }

        private Paint calcPaint(int i) { // a new alpha value for each line
            int computedAlpha = (int) ((255.0 * i / totalLines) + (255.0 * lineAlpha / totalLines));
            if (computedAlpha > 255) computedAlpha = (int) (computedAlpha % 255.0);
            paint.setAlpha(computedAlpha);
            return paint;
        }

        protected void drawPath(MotionEvent event) { // continuation of the movement

            originalLines = new float[4];

            xStart = xEnd;
            originalLines[0] = xStart;
            yStart = yEnd;
            originalLines[1] = yStart;
            xEnd = event.getX();
            originalLines[2] = xEnd;
            yEnd = event.getY();
            originalLines[3] = yEnd;

            linesToDraw.add(originalLines);

            invalidate();
        }

        protected void drawPath(MotionEvent event, MotionEvent event1) { // the first movement

            paint.setColor(Color.rgb((int) (200 * Math.random()),
                    (int) (200 * Math.random()), (int) (200 * Math.random())));
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(scaledSizeValue * 0.05f);
            paint.setStrokeCap(Paint.Cap.ROUND);

            originalLines = new float[4];

            xStart = event.getX();
            originalLines[0] = xStart;
            yStart = event.getY();
            originalLines[1] = yStart;
            xEnd = event1.getX();
            originalLines[2] = xEnd;
            yEnd = event1.getY();
            originalLines[3] = yEnd;

            linesToDraw.add(originalLines);
            animationTimer.run();

            invalidate();
        }

        private void removePath() {
            container.removeView(this);
            //remove this path from the map of paths for path's timers manage
            savedAnimatedPath.remove(ID);
            if (flingMotionViews != null) {
                FlingMotionView fl = flingMotionViews.remove(ID);
                if (fl != null) fl.removeOnFlingView(ID);
            }
            linesToDraw.clear();
        }

        private void setPaintAlpha() { //the common alpha path decrease
            lineAlpha = lineAlpha - 1;
            if (lineAlpha == 0) anStop = true;
        }

        private void checkAnimationState() { //the animation start

            chCounter++;
            //4 sec start delay
            if ((chCounter * delay > 4000) && !linesToDraw.isEmpty()) {
                compareLines.add(linesToDraw.get(linesToDraw.size() - 1));
                chCounter = 0;
            } else return;

            if (compareLines.size() > 2) {
                compareLines.remove(0);
            }

            if (compareLines.size() == 2) {

                if (Arrays.equals(compareLines.get(0), compareLines.get(1))) {
                    totalLines = linesToDraw.size();
                    lineAlpha = totalLines;
                    anStart = true;// Animation start
                }
            }
        }

        // an inner animation timer class
        private class AnimationTimer implements Runnable {

            AnimationTimer() {
            }

            @Override
            public void run() {

                Activity a = getActivity();

                if (a != null) {

                    if (screenOrientation != animationDATA.isAPortraitMode())
                        anStop = true; //stop when a display orientation change

                    if (!pause) {

                        if (anStop) {
                            handler.removeCallbacks(animationTimer);
                            // remove a current path
                            removePath();
                        } else {
                            handler.postDelayed(this, delay);
                        }

                        if (anStart) {
                            setPaintAlpha();
                            invalidate();
                        } else {
                            checkAnimationState();
                        }

                    } else {

                        handler.removeCallbacks(animationTimer);

                    }

                } else {

                    handler.removeCallbacks(animationTimer);
                    removePath();
                }
            }
        }//end of animation timer class
    }//==== end of the animated path class for an onScroll event ====

    /**
     * An Animation class to handle the OnFling motion event.
     * <p>
     * The class uses Android ImageView subclasses to draw multiple
     * graphics objects. Tracking shapes and generated text's labels.
     */
    class FlingMotionView {

        static final String TAG = "SceneAnimationFragment.FlingMotionView";

        private MotionEvent event1, event2;
        private float velocityX, velocityY;
        /**
         * The Text Label to display properties a base motion.
         */
        private android.support.v7.widget.AppCompatImageView onFlingLabel;
        /**
         * A circle to show dynamic properties a base motion.
         */
        private android.support.v7.widget.AppCompatImageView OnFlingShape;
        private String ID; // the linked Path's ID
        private float accelerateInterpolator;
        private ObjectAnimator oa;
        private float radius;
        private Context context;

        FlingMotionView(Context context, MotionEvent event1, MotionEvent event2,
                        float velocityX, float velocityY) {
            this.event1 = event1;
            this.event2 = event2;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.context=context;
            //onFling motion acceleration
            accelerateInterpolator = Math.abs((velocityX + velocityY) * 0.002f);
            //get a shape's radius from the original figure
            radius = animatedPath.paint.getStrokeWidth() / 2;
        }

        // ID defined by Path ID
        void setID(String ID) {
            this.ID = ID;
        }

        void removeOnFlingView(String ID) {
            if (this.ID.equals(ID)) {
                container.removeView(onFlingLabel);
                flingMotionViews.remove(ID);
                tabPlaces.remove(ID);
                oa.cancel();
                container.removeView(OnFlingShape);
            }
        }

        void anFling() { // the onFling gesture's text label create

            int universalPadding = animationDATA.getPixelSize(4);
            Rect maxTextWidth = new Rect();
            List<String> labels = Arrays.asList(
                    String.format(Locale.ROOT, "%s %.2f", getText(R.string.event1X), event1.getX()),
                    String.format(Locale.ROOT,"%s %.2f", getText(R.string.event1Y), event1.getY()),
                    String.format(Locale.ROOT,"%s %.2f", getText(R.string.event2X), event2.getX()),
                    String.format(Locale.ROOT,"%s %.2f", getText(R.string.event2Y), event2.getY()),
                    String.format(Locale.ROOT,"%s %.2f", getText(R.string.xVelocity), velocityX),
                    String.format(Locale.ROOT,"%s %.2f", getText(R.string.yVelocity), velocityY),
                    String.format(Locale.ROOT,"%s %.2f", getText(R.string.acceleration),
                            accelerateInterpolator));
            // set a paint for text label, see : http://stackoverflow.com/questions/19691530/
            // http://developer.android.com/reference/android/graphics/Typeface/
            Paint paint = new Paint();
            paint.setTypeface(Typeface.create("serif-monospace ", Typeface.NORMAL));
            paint.setColor(Color.YELLOW);
            paint.setAntiAlias(true);
            paint.setTextSize(animationDATA.getPixelSize(14));

            //measure the longest text line into this text label
            Optional<String> biggest = labels
                    .stream()
                    .max(Comparator.comparingInt(String::length));
            if (biggest.isPresent()) {
                String maxLabel = biggest.get();
                paint.getTextBounds(maxLabel, 0, maxLabel.length(), maxTextWidth);
            }
            //placing text labels
            float startY = maxTextWidth.height() + universalPadding;
            int layoutWidth = maxTextWidth.width() + 2 * universalPadding;
            int layoutHeight = (int) ((labels.size() * startY) + 2 * universalPadding);
            int placeHoldersY = (int) (animationDATA.getLayoutHeight() / layoutHeight);
            int placeHoldersX = (int) (animationDATA.getLayoutWidth() / layoutWidth);
            int cellHeight = (int) (animationDATA.getLayoutHeight() / placeHoldersY);
            int cellWidth = (int) (animationDATA.getLayoutWidth() / placeHoldersX);

            ArrayList<Point> points = new ArrayList<>(); //label placeholders
            for (int i = 0; i < placeHoldersY; i++) {
                for (int j = 0; j < placeHoldersX; j++) {
                    points.add(new Point(cellWidth * j, cellHeight * i));
                }
            }

            onFlingLabel = new android.support.v7.widget.AppCompatImageView(context) {
                int y = 1;
                @Override
                protected void onDraw(Canvas canvas) { //texts draw
                    labels.forEach(txt ->
                            canvas.drawText(txt, universalPadding, startY * y++, paint));
                    super.onDraw(canvas);
                }
            };

            Paint shapePaint = new Paint();
            // the onScroll animated color invert to animate the onFling motion
            int shapeColor = (0xFFFFFF - animatedPath.paint.getColor()) | 0xFF000000;
            shapePaint.setColor(shapeColor);
            shapePaint.setAntiAlias(true);
            // this onFling shape have the same start/end points as a parent onScroll shape
            OnFlingShape = new android.support.v7.widget.AppCompatImageView(context) {
                @Override
                protected void onDraw(Canvas canvas) {
                    canvas.drawCircle(radius, radius, radius, shapePaint);
                    super.onDraw(canvas);
                }
            };

            OnFlingShape.setX(event1.getX() - radius);
            OnFlingShape.setY(event1.getY() - radius);

            Path path = new Path();
            path.moveTo(event1.getX() - radius, event1.getY() - radius);
            path.lineTo(event2.getX() - radius, event2.getY() - radius);

            //take the animation summary time as the length of the gesture path
            double animationDuration = animatedPath.linesToDraw//current path of original figure
                    .stream()
                    .mapToDouble(e ->
                            Math.sqrt(((e[0] - e[2]) * (e[0] - e[2])) + ((e[1] - e[3]) * (e[1] - e[3]))))
                    .sum();
            oa = ObjectAnimator.ofFloat(OnFlingShape, View.X, View.Y, path);
            //oa.setInterpolator(new AccelerateInterpolator(accelerateInterpolator));
            oa.setInterpolator(new AnticipateInterpolator(accelerateInterpolator));
            oa.setRepeatCount(ObjectAnimator.INFINITE);
            oa.setRepeatMode(ObjectAnimator.REVERSE);
            oa.setDuration((long) animationDuration);
            oa.start();

            onFlingLabel.setBackgroundColor(Color.argb(70, 100, 100, 100));

            int i = setIndex(0);//searching for free place

            if (points.size() > i) {
                onFlingLabel.setX(points.get(i).x);
                onFlingLabel.setY(points.get(i).y);
            } else {
                if (i % 2 >= 0.5) onFlingLabel.setX(universalPadding);
                else onFlingLabel.setX(animationDATA.getLayoutWidth() / 2);
                onFlingLabel.setY(Math.round(i / 2f) * layoutHeight + universalPadding);
            }

            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(layoutWidth, layoutHeight);
            onFlingLabel.setLayoutParams(layoutParams);

            container.addView(OnFlingShape);
            container.addView(onFlingLabel);
            onFlingTexts.add(onFlingLabel);
        }//----end of the onFling gesture's text label create---

        /**
         * @param i a current index
         * @return a free place index for the text label
         * To check a taken index and modify when necessary.
         */
        private int setIndex(int i) {
            if (tabPlaces.containsValue(i)) return setIndex(++i);
            tabPlaces.put(ID, i);
            return i;
        }
    }//==== End of An Animation class to handle the OnFling motion event ====

}
