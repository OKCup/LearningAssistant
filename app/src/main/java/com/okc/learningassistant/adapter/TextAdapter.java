package com.okc.learningassistant.adapter;

import android.os.AsyncTask;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.gms.common.api.Status;
import com.okc.learningassistant.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class TextAdapter extends BaseQuickAdapter<Map<String,String>, BaseViewHolder>{
    public TextAdapter(List<Map<String,String>> data) {
        super(R.layout.layout_text_item, data);
    }
    @Override
    protected void convert(@NotNull BaseViewHolder helper, Map<String,String> item) {
        helper.setText(R.id.text_content,item.get("content"));
        helper.setText(R.id.text_date,item.get("date"));
        helper.setText(R.id.text_time,item.get("time"));
    }
}