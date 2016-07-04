package com.softdesign.devintensive.ui.views.behaviors;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.ConstantManager;

/**
 * Класс-поведение для виджета NestedScrollView.
 * Расширяет поведение {@code AppBarLayout.ScrollingViewBehavior}, добавляя сворачивание
 * заголовка контента профайла пользователя до минимально возможных размеров.
 */
public class ProfileScrollingViewBehavior extends AppBarLayout.ScrollingViewBehavior {

    public static final String TAG = ConstantManager.TAG_PREFIX + "PSV_Behavior";

    /** Задает идентификатор виджета - заголовка профайла пользователя */
    public static final int PROFILE_HEADER_ID = R.id.profile_header_ll;

    private Context mContext;
    private View mHeaderView;

    private boolean initDone = false;

    int childStartPadding = 0;
    float dependencyMaxPosY = 0;
    float dependencyMinPosY = 0;

    public ProfileScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        mHeaderView = child.findViewById(PROFILE_HEADER_ID);
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if (!initDone) {
            childStartPadding = mHeaderView.getPaddingTop();
            dependencyMinPosY = getStatusBarHeight() + getActionBarSize();
            dependencyMaxPosY = dependency.getHeight();
            initDone = true;
        }

        int dependencyCurrentPosY = dependency.getBottom();
        float percentFactor = (dependencyCurrentPosY - dependencyMinPosY) /
                (dependencyMaxPosY - dependencyMinPosY);
        int childCurrentPadding = (int) (childStartPadding * percentFactor);

        mHeaderView.setPadding(0, childCurrentPadding, 0, childCurrentPadding);
        return super.onDependentViewChanged(parent, child, dependency);
    }

    /**
     * Определяет высоту строки состояния
     * @return высоту строки состояния
     */
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = mContext.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Определяет высоту ActionBar
     * @return высоту ActionBar
     */
    private float getActionBarSize() {
        final TypedArray styledAttributes = mContext.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        float actionBarSize = styledAttributes.getDimension(0, 0);

        styledAttributes.recycle();

        return actionBarSize;
    }
}
