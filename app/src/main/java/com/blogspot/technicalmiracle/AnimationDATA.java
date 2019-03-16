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

import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * The Animation Data class hold all necessary data of the application.
 *
 * The initial values will be created during the start.
 * And after the start variable data of every fragment will be saved into
 * the animation data class for future use.
 * This class uses the ViewModel for simple and clear load/save operations.
 * Data synchronization provide to support the application's LifeCycle additionally.
 */
class AnimationDATA extends ViewModel {

    private static final String TAG = "AnimationDATA";

    /**
     * The values to save a state of last the scene animation condition.
     */
    private boolean firstStartCompleted = false;

    /**
     * The variable to observe a current screen orientation.
     */
    private boolean screenOrientation;

    /**
     * Set the initial values for stiffness and damp ratio SeekBars.
     */
    private int st = 1500, damp = 500;

    /**
     * The minimal layout side. A base to compute graphical figures sizes.
     */
    private float scaledSizeValue;

    /**
     * @return the base to compute the size of any shape.
     */
    float getScaledSizeValue() {
        return scaledSizeValue;
    }

    /**
     * A Fragment layout Width and height.
     */
    private float width, height;

    /**
     * Get a Fragment layout width.
     */
    float getLayoutWidth() {
        return width;
    }

    /**
     * Get a Fragment layout height.
     */
    float getLayoutHeight() {
        return height;
    }

    /**
     * The system soft Navigation Bar height, The system status bar height,
     * The application Action Bar height.
     */
    private int statusBarHeight, actionBarHeight, navigationBarHeight;

    /**
     * This device pixel size.
     */
    private float pxSize;

    /**
     * Switch down after the first start completed for this
     * type of Fragment into this application session.
     * The another Fragments start will begin without Scene Animation.
     */
    void setFirstStartCompleted() {
        this.firstStartCompleted = true;
    }

    /**
     * @return Is in this app session a Scene fragment was already open?
     */
    boolean isFirstStartCompleted() {
        return firstStartCompleted;
    }

    /**
     * A constructor of this class, get the actual device pixel size.
     */
    AnimationDATA() {
        this.pxSize = Resources.getSystem().getDisplayMetrics().density;
    }

    /**
     * Getting the px size of the dp unit, accordingly :
     * http://developer.android.com/training/multiscreen/screendensities#dips-pels
     * developer.android.com - 'Support different pixel densities'.
     */
    int getPixelSize(int dp) {
        return (int) (dp * pxSize);
    }

    /**
     * @return the system StatusBar height.
     */
    int getStatusBarHeight() {
        return statusBarHeight;
    }

    /**
     * @return the ActionBar height.
     */
    int getActionBarHeight() {
        return actionBarHeight;
    }

    /**
     * @param st set the Stiffness value for the Spring Animation Fragment.
     */
    void setSt(int st) {
        if (st != 1500) this.st = st;
    }

    /**
     * @param damp set the Damp Ratio value for the Spring Animation Fragment.
     */
    void setDamp(int damp) {
        if (damp != 500) this.damp = damp;
    }

    /**
     * @return the Stiffness value for the Spring Animation Fragment.
     */
    int getSt() {
        return st;
    }

    /**
     * @return the Damp Ratio value for the Spring Animation Fragment.
     */
    int getDamp() {
        return damp;
    }

    /**
     * @return 'true' if this device on the Portrait mode.
     */
    boolean isAPortraitMode() {
        return screenOrientation;
    }

    /**
     * @param screenOrientation
     * Set up a current screen orientation mode.
     */
    void setScreenOrientation(boolean screenOrientation) {
        this.screenOrientation = screenOrientation;
    }

    /**
     * @param context apps context
     *
     * Set the value of the app ActionBar height during the start.
     */
    void setActionBarHeight(@Nullable Context context) {

        if (context == null) return;

        actionBarHeight= (int) context.getResources().getDimension(R.dimen.AcBarHeight);
    }

    /**
     * @param context apps context
     *
     * Set a StatusBar height value.
     */
    void setStatusBarHeight(@Nullable Context context) {

        if (context == null) return;

        int identifier = context.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (identifier > 0) { // Status bar height (px)
            statusBarHeight = context.getResources().getDimensionPixelSize(identifier);
        }
//        Log.d(TAG, "setStatusBarHeight: "+statusBarHeight);
    }


    /**
     * @param context apps context
     * @param windowManager this device window manager
     *
     * When a soft navigation bar the value will set equal it's height.
     * When an ordinary navigation set when have only buttons the value will set to zero.
     * See : http://stackoverflow.com/questions/28983621/
     * for more information.
     */
    void setNavigationBarHeight(@Nullable Context context,
                                @Nullable WindowManager windowManager) {

        if (context == null || windowManager == null) return;
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        //does this device have a soft Navigation bar?
        if ((realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0) {
            int id = context.getResources()
                    .getIdentifier("navigation_bar_height", "dimen", "android");
            if (id > 0) {  // a soft Navigation Bar height (px)
                navigationBarHeight = context.getResources().getDimensionPixelSize(id);
            }
        } else {
            navigationBarHeight=0;
        }
    }

    /**
     * Layout dimensions setup.
     *
     * @param context application context
     * @param takeAccountOfaNavigationBar Do cut the NavigationBar on LandScape Mode?
     * @return 'true' if values did set
     *
     * When this device has a soft NavigationBar this NavigationBar will be disabled
     * on the Arbitrary Animation screen with the Landscape mode a layout width
     * will change at this occasion.
     */
    boolean setLayoutDimensions(@Nullable Context context, boolean takeAccountOfaNavigationBar) {

        if (context != null) {

            DisplayMetrics displayMetrics =
                    context.getResources().getDisplayMetrics();

            if (isAPortraitMode() || !takeAccountOfaNavigationBar) {
                width = (float) displayMetrics.widthPixels;
            } else {
                width = (float) (displayMetrics.widthPixels + navigationBarHeight);
            }

            height = (float) (displayMetrics.heightPixels - getActionBarHeight()
                    - getStatusBarHeight());

            scaledSizeValue = Math.min(height, width);

            return (scaledSizeValue > 0);

        } else return false;
    }

    /**
     * Layout dimensions setup.
     *
     * @param width  A layout width
     * @param height A layout height
     */
    void setLayoutDimensions(float width, float height) {

        this.height = height;
        this.width = width;

        scaledSizeValue = Math.min(height, width);
    }

}
