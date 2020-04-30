package com.okc.learningassistant.activity;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.okc.learningassistant.R;
import com.okc.learningassistant.fragment.HomeFragment;
import com.okc.learningassistant.fragment.PersonFragment;
import com.okc.learningassistant.fragment.TextFragment;
import com.okc.learningassistant.helper.AppPermissionUtil;
import com.okc.learningassistant.helper.ToolBox;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.tab.QMUITab;
import com.qmuiteam.qmui.widget.tab.QMUITabBuilder;
import com.qmuiteam.qmui.widget.tab.QMUITabSegment;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LaunchActicity extends AppCompatActivity{
    public static Context ParentContext;

    private static String DATAPATH;
    private static String tessdata;
    private static String DEFAULT_LANGUAGE;
    private static String DEFAULT_LANGUAGE_NAME;
    private static String LANGUAGE_PATH ;

    @BindView(R.id.Tab)
    QMUITabSegment mTabSegment;
    private HomeFragment homeFragment;
    private TextFragment textFragment;
    private PersonFragment personFragment;
    Context mContext = this;

    private FragmentManager fragmentManager;

    public static String getDATAPATH() {
        return DATAPATH;
    }

    public static String getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }

    public static Context getParentContext() {
        return ParentContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ButterKnife.bind(this);
        ParentContext = this;
        //tessdata路径
        DATAPATH = getExternalFilesDir("")+"/";
        tessdata = DATAPATH + File.separator + "tessdata";
        DEFAULT_LANGUAGE = "eng";
        DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
        LANGUAGE_PATH = tessdata + File.separator + DEFAULT_LANGUAGE_NAME;

        getPermission();

        initOpenCV();
        ToolBox.copyToSD(mContext,LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);

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

    private void initOpenCV() {
        System.loadLibrary("opencv_java4");
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "Could not load OpenCV Lib.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Load OpenCV Lib Success.", Toast.LENGTH_SHORT).show();
            Logger.getLogger("org.opencv.osgi").log(Level.INFO, "Successfully loaded OpenCV native library.");
        }
    }

    private void initTabs() {

        QMUITabBuilder builder = mTabSegment.tabBuilder();
        builder.setSelectedIconScale(1.2f)
                .setTextSize(QMUIDisplayHelper.sp2px(getApplicationContext(), 13), QMUIDisplayHelper.sp2px(getApplicationContext(), 15))
                .setColor(getResources().getColor(R.color.qmui_config_color_gray_5),getResources().getColor(R.color.app_color_blue))
                .setDynamicChangeIconColor(true);
        QMUITab home = builder
                //.setNormalDrawable(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.outline_home_white_48))
                //.setSelectedDrawable(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.outline_home_white_48))
                .setNormalDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_home_24))
                .setSelectedDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_home_24))
                .setText("首页")
                .build(getApplicationContext());
        QMUITab text = builder
                .setNormalDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_text_24))
                .setSelectedDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_text_24))
                .setText("文本")
                .build(getApplicationContext());
        QMUITab person = builder
                .setNormalDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_person_24))
                .setSelectedDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_person_24))
                .setText("我的")
                .build(getApplicationContext());

        mTabSegment.addTab(home)
                .addTab(text)
                .addTab(person);
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
                if (personFragment != null) {
                    ft.show(personFragment);
                }
                else {
                    personFragment = new PersonFragment();
                    ft.add(R.id.fl_content_launch, personFragment);
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
        if (personFragment != null)
            ft.hide(personFragment);
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
