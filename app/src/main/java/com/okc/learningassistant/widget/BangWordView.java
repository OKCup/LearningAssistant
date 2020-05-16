package com.okc.learningassistant.widget;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.okc.learningassistant.R;


/**
 * Created by lizhengxian on 2016/10/23.
 * 显示分割后的词语的控件
 */

public class BangWordView extends CheckBox {
    public BangWordView(Context context,String text) {
        super(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundResource(R.drawable.checkbox_selector);
        setButtonDrawable(null);
        setLayoutParams(params);
        setText(text);
        setTextColor(getResources().getColorStateList(R.color.checkbox_textcolor_selector));
        setTextSize(13);
    }
}