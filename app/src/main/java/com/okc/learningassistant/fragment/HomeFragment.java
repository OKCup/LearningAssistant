package com.okc.learningassistant.fragment;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.okc.learningassistant.R;
import com.okc.learningassistant.adaptor.QDRecyclerViewAdapter;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUICollapsingTopBarLayout;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class HomeFragment extends Fragment {

    @BindView(R.id.camera)
    ImageView mCamera;
    @BindView(R.id.camera_bg)
    QMUIRadiusImageView mCameraBackground;
    @BindView(R.id.camera_text)
    TextView mCameraText;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.collapsing_topbar_layout)
    QMUICollapsingTopBarLayout mCollapsingTopBarLayout;
    QDRecyclerViewAdapter mRecyclerViewAdapter;

    LinearLayoutManager mPagerLayoutManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_collapse, container, false);
        ButterKnife.bind(this, view);
        Log.i("location:","homefragment");
        Init();
        InitCollapseTopBar();
        InitRecyclerView();

        //设置折叠状态栏不同状态下的标题
        mCollapsingTopBarLayout.setScrimUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.i(TAG, "scrim: " + animation.getAnimatedValue());
                if(Integer.parseInt(animation.getAnimatedValue().toString())==255){
                    mCollapsingTopBarLayout.setTitleEnabled(true);
                    Log.i(TAG,"111enter:true");
                }
                else {
                    mCollapsingTopBarLayout.setTitleEnabled(false);
                    Log.i(TAG,"111enter:flase");
                }
            }
        });
        return view;
    }


    /**
     *  为控件添加尺寸渐变动画
     */
    public static void setScaleAni(View V, float fromScale, float toScale, long ANITIME)
    {
        AnimationSet aniSet = new AnimationSet(true);
        // final int ANITIME = 500;

        // 尺寸变化动画，设置尺寸变化
        ScaleAnimation scaleAni = new ScaleAnimation(fromScale, toScale, fromScale, toScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAni.setDuration(ANITIME);	// 设置动画效果时间
        scaleAni.setRepeatCount(-1);
        scaleAni.setRepeatMode(2);
        aniSet.addAnimation(scaleAni);	// 将动画效果添加到动画集中

        V.startAnimation(aniSet);		// 添加光效动画到控件
    }


    /*
    * 初始化图标动画效果
    * */
    public void Init(){
        mCameraBackground.setCircle(true);
        mCameraBackground.setBorderWidth(0);
        setScaleAni(mCameraBackground, 1, Float.parseFloat("1.1"), 2000);
        setScaleAni(mCamera, 1, Float.parseFloat("1.1"), 2000);
        setScaleAni(mCameraText, 1, Float.parseFloat("1.1"), 2000);
    }


    /*
    * 初始化可折叠的状态栏
    * */
    public void InitCollapseTopBar(){
        mCollapsingTopBarLayout.setTitleEnabled(false);
        mCollapsingTopBarLayout.setTitle("历史记录");
        mCollapsingTopBarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.qmui_config_color_black));
        mCollapsingTopBarLayout.setContentScrimColor(getResources().getColor(R.color.qmui_config_color_white));
        mCollapsingTopBarLayout.setScrimAnimationDuration(1);
    }

    /*
    *初始化滚动界面
    * */
    public void InitRecyclerView(){
        mPagerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mPagerLayoutManager);
        mRecyclerViewAdapter = new QDRecyclerViewAdapter();
        mRecyclerViewAdapter.setItemCount(10);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

}
