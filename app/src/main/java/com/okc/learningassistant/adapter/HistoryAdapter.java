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

public class HistoryAdapter extends BaseQuickAdapter<Map<String,String>, BaseViewHolder>{
    public HistoryAdapter(List<Map<String,String>> data) {
        super(R.layout.layout_history_item, data);
    }
    @Override
    protected void convert(@NotNull BaseViewHolder helper, Map<String,String> item) {
        helper.setText(R.id.history_title,item.get("title"));
        helper.setText(R.id.history_content,item.get("content"));
        helper.setText(R.id.history_date,item.get("date"));
        helper.setText(R.id.history_time,item.get("time"));
    }
}
