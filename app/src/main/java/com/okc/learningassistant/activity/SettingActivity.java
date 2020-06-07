package com.okc.learningassistant.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.okc.learningassistant.R;
import com.okc.learningassistant.helper.ToolBox;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.btn_logout)
    QMUIRoundButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        QMUIStatusBarHelper.translucent(this);
        Intent intent = getIntent();
        initTopBar();
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean deleteName = ToolBox.deleteFile(LaunchActicity.getUserNamePath());
                boolean deletePWD = ToolBox.deleteFile(LaunchActicity.getUserPwdPath());
                if(deleteName && deletePWD){
                    LaunchActicity.setLoginStatus(false);
                    finish();
                }
            }
        });
    }

    private void initTopBar(){
        mTopBar.setTitle("设置").setTextColor(getResources().getColor(R.color.white));
    }
}
