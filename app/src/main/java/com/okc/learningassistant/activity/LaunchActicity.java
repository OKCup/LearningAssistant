package com.okc.learningassistant.activity;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.okc.learningassistant.R;
import com.okc.learningassistant.fragment.HomeFragment;
import com.okc.learningassistant.fragment.MeFragment;
import com.okc.learningassistant.fragment.TextFragment;
import com.okc.learningassistant.helper.AppPermissionUtil;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.tab.QMUITab;
import com.qmuiteam.qmui.widget.tab.QMUITabBuilder;
import com.qmuiteam.qmui.widget.tab.QMUITabSegment;

public class LaunchActicity extends AppCompatActivity{

    private HomeFragment homeFragment;
    private TextFragment textFragment;
    private MeFragment meFragment;
    private QMUITabSegment mTabSegment;
    Context mContext = this;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        getPermission();
        mTabSegment = (QMUITabSegment)findViewById(R.id.Tab);
        initTabs();
        mTabSegment.selectTab(0);
        fragmentManager = getSupportFragmentManager();
        //默认选中第一个tab
        showFragment(1);
        mTabSegment.addOnTabSelectedListener(new QMUITabSegment.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int index) {
                showFragment(index+1);
            }

            @Override
            public void onTabUnselected(int index) {

            }

            @Override
            public void onTabReselected(int index) {

            }

            @Override
            public void onDoubleTap(int index) {

            }
        });
    }

    private void initTabs() {

        QMUITabBuilder builder = mTabSegment.tabBuilder();
        builder.setSelectedIconScale(1.2f)
                .setTextSize(QMUIDisplayHelper.sp2px(getApplicationContext(), 13), QMUIDisplayHelper.sp2px(getApplicationContext(), 15))
                .setDynamicChangeIconColor(true);
        QMUITab component = builder
                .setNormalDrawable(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.outline_home_white_48))
                .setSelectedDrawable(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.outline_home_white_48))
                .setText("首页")
                .build(getApplicationContext());
        QMUITab util = builder
                .setNormalDrawable(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.outline_home_white_48))
                .setSelectedDrawable(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.outline_home_white_48))
                .setText("文本")
                .build(getApplicationContext());
        QMUITab lab = builder
                .setNormalDrawable(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.outline_home_white_48))
                .setSelectedDrawable(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.outline_home_white_48))
                .setText("我的")
                .build(getApplicationContext());

        mTabSegment.addTab(component)
                .addTab(util)
                .addTab(lab);
    }

    private void showFragment(int page) {
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // 想要显示一个fragment,先隐藏所有fragment，防止重叠
        hideFragments(ft);
        switch (page) {
            case 1:
                // 如果fragment1已经存在则将其显示出来
                if (homeFragment != null)
                    ft.show(homeFragment);
                    // 否则添加fragment1，注意添加后是会显示出来的，replace方法也是先remove后add
                else {
                    homeFragment = new HomeFragment();
                    ft.add(R.id.fl_content_launch, homeFragment);
                }
                break;
            case 2:
                if (textFragment != null)
                    ft.show(textFragment);
                else {
                    textFragment = new TextFragment();
                    ft.add(R.id.fl_content_launch, textFragment);
                }
                break;
            case 3:
                if (meFragment != null) {
                    ft.show(meFragment);
                }
                else {
                    meFragment = new MeFragment();
                    ft.add(R.id.fl_content_launch, meFragment);
                }
                break;
        }
        ft.commit();
    }
    // 当fragment已被实例化，相当于发生过切换，就隐藏起来
    public void hideFragments(FragmentTransaction ft) {
        if (homeFragment != null)
            ft.hide(homeFragment);
        if (textFragment != null)
            ft.hide(textFragment);
        if (meFragment != null)
            ft.hide(meFragment);
    }


    private void getPermission(){
        AppPermissionUtil.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO}, new AppPermissionUtil.OnPermissionListener() {
            @Override
            public void onPermissionGranted() {
                Log.i("Permission:","Granted");
                //授权
            }

            @Override
            public void onPermissionDenied() {
                Log.i("Permission:","Denied");
                //没有授权，或者有一个权限没有授权
            }
        });
    }

}
