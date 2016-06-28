package com.softdesign.devintensive.ui.views.behaviors;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.softdesign.devintensive.utils.ConstantManager;

public class UserInfoBehavior extends CoordinatorLayout.Behavior<LinearLayout> {

    public static final String TAG = ConstantManager.TAG_PREFIX + "UserInfoBehavior";

    private boolean initDone = false;
    private int padding = 0;
    private int height = 0;


    public UserInfoBehavior(Context context, AttributeSet attrs) {}

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, LinearLayout child, View dependency) {
        if (!initDone) {
            initDone = true;
            padding = child.getPaddingTop();
            Log.d(TAG, "padding = " + padding);
        }
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout child, View dependency) {
        int appbarBottom = dependency.getBottom();
        Log.d(TAG, appbarBottom + "");
        child.setY(appbarBottom);
        if (appbarBottom > 240) {
            child.setPadding(0, padding, 0, padding);
        } else {
            child.setPadding(0, 0, 0, 0);
        }
        return true;
    }

}
