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

import android.app.Fragment;
import android.app.FragmentManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;

/**
 * The Main Activity class served to load and manage animation
 * fragments during this application work.
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private FrameLayout frameLayout;

    /**
     * The Animation Data class hold all necessary data of the application.
     */
    AnimationDATA animationDATA;

    /**
     * @param fragment the fragment to load
     *
     * The fragment load from the main animation fragment screen by pushing the button.
     */
    public void loadFragment(Fragment fragment) {

        if(frameLayout!=null) { frameLayout.removeAllViews(); }

        if (fragment instanceof MainActivityFragment) {
            //loading the main screen
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .commit();
        } else {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .addToBackStack(null)//this transaction will added to the back stack
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {

        if (frameLayout != null)
            frameLayout.removeAllViews();

        // Fragment stack manager added it's always to point
        // to the MainActivityFragment screen.
        FragmentManager fragmentManager = getFragmentManager();

        if (fragmentManager.getBackStackEntryCount() > 1) {
            fragmentManager.popBackStack();
            return;
        }

        super.onBackPressed();
    }

    /**
     * @param savedInstanceState The action bar create with the bended icon if it's possible.
     *                           The ViewModel init animationDATA of the application.
     *                           A display orientation init into animationDATA class.
     *                           The main animation fragment load.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // The action bar create with the icon if it's possible
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) { //yeah, well let's get started
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.drawable.ic_bender);
            actionBar.setDisplayUseLogoEnabled(true);
            // creating the special bended gradient |->)
            GradientDrawable gradient = new GradientDrawable();
            gradient.setColors(new int[]{
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#000000"),
                    Color.parseColor("#FFFFFF"),
            });
            gradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            gradient.setShape(GradientDrawable.RECTANGLE);
            actionBar.setBackgroundDrawable(gradient);

        } else {
            Log.d(TAG, "The Action Bar hadn't created\n");
            Log.d(TAG, "Sadly but an actionBar has created without the Bender Face\n");
        }

        //Data View Model will used to save application data
        animationDATA = ViewModelProviders.of(this).get(AnimationDATA.class);

        //at the apps start initialize the orientation value
        int initialOrientation = this.getResources().getConfiguration().orientation;
        if (initialOrientation == 1) {
            animationDATA.setScreenOrientation(true);
        }

        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);

        loadFragment(new MainActivityFragment());

    }

    /**
     * Apps parameters measure. These parameters save into animation DATA.
     * ActionBar height (px), the StatusBar & Navigation bars height.
     * Layout dimensions init into animationDATA class.
     */
    @Override
    protected void onStart() {

        Context context = getApplicationContext();
        animationDATA.setActionBarHeight(context);
        animationDATA.setStatusBarHeight(context);
        animationDATA.setNavigationBarHeight(context, getWindowManager());
        boolean resHadLoad=animationDATA.setLayoutDimensions(context,false);
        if(!resHadLoad)Log.d(TAG, "OnStart - a Layout dimensions had not set");

        super.onStart();
    }

    /**
     * @param newConfig
     *
     * The screen orientation value switch into AnimationData class.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            animationDATA.setScreenOrientation(false);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            animationDATA.setScreenOrientation(true);
        }
        super.onConfigurationChanged(newConfig);
    }
}





