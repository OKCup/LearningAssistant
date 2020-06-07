package com.okc.learningassistant.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.okc.learningassistant.R;
import com.okc.learningassistant.widget.AutoExpandLinearLayout;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.popup.QMUIFullScreenPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopups;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

public class Popups {
    /*
    public static void createOfflineSearchResultPopup(Context context,String content,String solution){
        QMUIFullScreenPopup offlineResultPopups = QMUIPopups.fullScreenPopup(context);
        LayoutInflater inflater = getLayoutInflater();
        View layoutOfflineResult = inflater.inflate(R.layout.layout_offline_search_result, null);
        QMUILinearLayout linearLayout = (QMUILinearLayout)layoutOfflineResult.findViewById(R.id.layout);
        TextView tvContent = (TextView)layoutOfflineResult.findViewById(R.id.content);
        TextView tvSolution = (TextView)layoutOfflineResult.findViewById(R.id.solution);
        linearLayout.setRadiusAndShadow(QMUIDisplayHelper.dp2px(context, 15),
                QMUIDisplayHelper.dp2px(context, 50),
                0.5f * 1f /100);
        tvContent.setText("错误内容："+content);
        tvSolution.setText("错误分析："+solution);
        offlineResultPopups.addView(layoutOfflineResult)
                .closeBtn(true)
                .animStyle(QMUIPopup.ANIM_GROW_FROM_CENTER)
                .onBlankClick(new QMUIFullScreenPopup.OnBlankClickListener() {
                    @Override
                    public void onBlankClick(QMUIFullScreenPopup popup) {
                        Toast.makeText(context, "点击到空白区域", Toast.LENGTH_SHORT).show();
                    }
                })
                .onDismiss(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        Toast.makeText(context, "onDismiss", Toast.LENGTH_SHORT).show();
                    }
                })
                .show(getView());
    }
    */
}
