package com.okc.learningassistant.fragment;

import android.content.Intent;
import android.icu.text.CaseMap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.okc.learningassistant.R;
import com.okc.learningassistant.activity.CollectActivity;
import com.okc.learningassistant.activity.LaunchActicity;
import com.okc.learningassistant.activity.LoginActivity;
import com.okc.learningassistant.activity.SettingActivity;
import com.okc.learningassistant.helper.RequestCode;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PersonFragment extends Fragment {
    @BindView(R.id.grouplistview)
    QMUIGroupListView mGroupListView;
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    QMUICommonListItemView itemMe;


    private static String Title = "点击登录";
    private static String Detail = "登录后可同步数据";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_person, container, false);
        ButterKnife.bind(this, view);
        initGroupListView();
        if(LaunchActicity.getLoginStatus()){
            itemMe.setText(LaunchActicity.getUserName());
            itemMe.setDetailText(LaunchActicity.getEmail());
        }
        initTopBar();
        return view;
    }

    private void initTopBar(){
        QMUIStatusBarHelper.translucent(getActivity());
        mTopBar.setTitle("我").setTextColor(getResources().getColor(R.color.white));
        mTopBar.addRightImageButton(R.drawable.outline_settings_24,R.id.topbar_right_setting_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivityForResult(intent,RequestCode.REQUEST_SETTING);
            }
        });
        //mTopBar.setBorderColor(getResources().getColor(R.color.white));
    }

    private void initGroupListView(){
        //我的信息控件样式
        int List_size = QMUIDisplayHelper.dp2px(getContext(), 150);
        int Text_size = QMUIDisplayHelper.dp2px(getContext(), 8);
        int Detail_size = QMUIDisplayHelper.dp2px(getContext(), 6);
        int padding = QMUIDisplayHelper.dp2px(getContext(), 20);
        itemMe = mGroupListView.createItemView(
                ContextCompat.getDrawable(getContext(), R.drawable.twotone_account_circle_24),
                LaunchActicity.getUserName(),
                LaunchActicity.getEmail(),
                QMUICommonListItemView.VERTICAL,
                QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
                List_size);
        itemMe.getTextView().setTextSize(Text_size);
        itemMe.getTextView().setPadding(padding,0,0,0);
        itemMe.getDetailTextView().setTextSize(Detail_size);
        itemMe.getDetailTextView().setPadding(padding,0,0,0);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof QMUICommonListItemView) {
                    CharSequence text = ((QMUICommonListItemView) v).getText();
                    Toast.makeText(getActivity(), text + " is Clicked", Toast.LENGTH_SHORT).show();
                    if(text == itemMe.getText()){
                        if(!LaunchActicity.getLoginStatus()) {
                            Intent intent = new Intent(getContext(), LoginActivity.class);
                            startActivityForResult(intent, RequestCode.REQUEST_LOGIN);
                        }
                    }
                    else if(text.toString() == "收藏"){
                        Intent intent = new Intent(getContext(), CollectActivity.class);
                        startActivityForResult(intent, RequestCode.REQUEST_COLLECT);
                    }
                }
            }
        };
        int size = QMUIDisplayHelper.dp2px(getContext(), 120);
        QMUIGroupListView.newSection(getContext())
                .setUseTitleViewForSectionSpace(false)
                .setLeftIconSize(size, size)
                .addItemView(itemMe, onClickListener)
                .addTo(mGroupListView);

        QMUICommonListItemView itemCollect = mGroupListView.createItemView("收藏");
        itemCollect.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        QMUICommonListItemView itemHistory = mGroupListView.createItemView("历史记录");
        itemHistory.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        QMUICommonListItemView itemUpdate = mGroupListView.createItemView("版本更新");
        itemUpdate.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        QMUICommonListItemView itemAbout = mGroupListView.createItemView("关于");
        itemAbout.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        QMUIGroupListView.newSection(getContext())
                .addItemView(itemCollect,onClickListener)
                .addItemView(itemHistory,onClickListener)
                .addItemView(itemUpdate,onClickListener)
                .addItemView(itemAbout,onClickListener)
                .addTo(mGroupListView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RequestCode.LOGIN_COMPLETE && LaunchActicity.getUserName() != null) {
            itemMe.setText(LaunchActicity.getUserName());
            itemMe.setDetailText(LaunchActicity.getEmail());
        }
        else if(!LaunchActicity.getLoginStatus()){
            itemMe.setText(LaunchActicity.getUserName());
            itemMe.setDetailText(LaunchActicity.getEmail());
        }
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        View view = getView();
        if (view != null) {
            if (!hidden) {
                if(LaunchActicity.getLoginStatus()){
                    itemMe.setText(LaunchActicity.getUserName());
                    itemMe.setDetailText(LaunchActicity.getEmail());
                }
                else {
                    itemMe.setText(Title);
                    itemMe.setDetailText(Detail);
                }
            }
            view.requestApplyInsets();
        }
        super.onHiddenChanged(hidden);
    }
}
