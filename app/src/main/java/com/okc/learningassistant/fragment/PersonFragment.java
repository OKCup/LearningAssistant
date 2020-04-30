package com.okc.learningassistant.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.okc.learningassistant.R;
import com.okc.learningassistant.activity.LoginActivity;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
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

    //标题和详细信息
    private static String Title = "点击登录";
    private static String Detail = "登录后可同步数据";

    public static void setContent(String title, String detail){
        Title = title;
        Detail = detail;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        ButterKnife.bind(this, view);
        initGroupListView();
        initTopBar();
        return view;
    }

    private void initTopBar(){
        mTopBar.setBorderColor(getResources().getColor(R.color.white));
    }

    private void initGroupListView(){
        //我的信息控件样式
        int List_size = QMUIDisplayHelper.dp2px(getContext(), 150);
        int Text_size = QMUIDisplayHelper.dp2px(getContext(), 8);
        int Detail_size = QMUIDisplayHelper.dp2px(getContext(), 6);
        int padding = QMUIDisplayHelper.dp2px(getContext(), 20);
        itemMe = mGroupListView.createItemView(
                ContextCompat.getDrawable(getContext(), R.drawable.twotone_account_circle_24),
                Title,
                Detail,
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
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivityForResult(intent,1);
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
        if (resultCode == 1 && Title != null) {
            itemMe.setText(Title);
            itemMe.setDetailText(Detail);
        }
    }
}
