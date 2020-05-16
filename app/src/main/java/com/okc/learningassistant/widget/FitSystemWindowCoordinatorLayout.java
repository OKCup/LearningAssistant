package com.okc.learningassistant.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;


public class FitSystemWindowCoordinatorLayout extends CoordinatorLayout {

    public FitSystemWindowCoordinatorLayout(Context context) {
        this(context, null);
    }

    public FitSystemWindowCoordinatorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FitSystemWindowCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
        setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                requestApplyInsets();
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {

            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        int childCount = getChildCount();
        for (int index = 0; index < childCount; index++)
            getChildAt(index).dispatchApplyWindowInsets(insets);
        return insets;
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        WindowInsets result = super.dispatchApplyWindowInsets(insets);
        if (!insets.isConsumed()) {
            final int count = getChildCount();
            for (int i = 0; i < count; i++)
                result = getChildAt(i).dispatchApplyWindowInsets(insets);
        }
        return result;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        ViewCompat.requestApplyInsets(child);
    }
}

