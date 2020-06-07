package com.okc.learningassistant.fragment;

import android.content.ClipboardManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.okc.learningassistant.R;
import com.okc.learningassistant.activity.LaunchActicity;
import com.okc.learningassistant.activity.WebActivity;
import com.okc.learningassistant.adapter.HistoryAdapter;
import com.okc.learningassistant.adapter.TextAdapter;
import com.okc.learningassistant.helper.RequestCode;
import com.okc.learningassistant.helper.ToolBox;
import com.okc.learningassistant.widget.AutoExpandLinearLayout;
import com.okc.learningassistant.widget.BangWordView;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.popup.QMUIFullScreenPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopups;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.CLIPBOARD_SERVICE;


public class TextFragment extends Fragment {
    @BindView(R.id.et_content)
    EditText etContent;
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.btn_split)
    QMUIRoundButton btnSplit;
    private TextAdapter mRecyclerViewAdapter;
    private LinearLayoutManager mPagerLayoutManager;
    private List<Map<String,String>> TextHistory = new ArrayList<Map<String, String>>();
    private String Url = "http://47.102.195.6:8082/QueryTextHistory.php";

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
        View view = inflater.inflate(R.layout.fragment_text, container, false);
        ButterKnife.bind(this, view);
        initTopBar();
        queryTextHistory("queryall");
        btnSplit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new splitTask().execute(etContent.getText().toString().replaceAll("\\n",""));
            }
        });
        return view;
    }

    public void initTopBar(){
        mTopBar.setTitle("文本拆分").setTextColor(getResources().getColor(R.color.white));
    }


    private void InitRecyclerView(){
        mPagerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mPagerLayoutManager);
        mRecyclerViewAdapter = new TextAdapter(TextHistory);
        mRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                Toast.makeText(getContext(), "onItemClick:"+position, Toast.LENGTH_SHORT).show();
                HashMap<String,String> clickItem = (HashMap<String, String>) adapter.getItem(position);
                Log.i("111adapterItem",clickItem.get("content"));
                etContent.setText(clickItem.get("content"));
                new splitTask().execute(clickItem.get("content"));

            }
        });
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    //查询历史记录
    public void queryTextHistory(String command) {
        //初始化okhttp客户端
        OkHttpClient client = new OkHttpClient.Builder().build();
        //创建post表单，获取username和password（没有做非空判断）
        if(command=="queryall"&& LaunchActicity.getLoginStatus()) {
            RequestBody post = new FormBody.Builder()
                    .add("command", command)
                    .add("username",LaunchActicity.getUserName())
                    .build();
            //开始请求，填入url，和表单
            final Request request = new Request.Builder()
                    .url(Url)
                    .post(post)
                    .build();

            //客户端回调
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    //失败的情况（一般是网络链接问题，服务器错误等）
                    Log.i("111error", "连接错误");
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    //UI线程运行
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String body = null;
                            JSONObject jsonObject = null;
                            int retCode = 0;
                            try {
                                //临时变量（这是okhttp的一个锅，一次请求的response.body().string()只能用一次，否则就会报错）
                                body = response.body().string();
                                //解析出后端返回的数据来
                                jsonObject = new JSONObject(String.valueOf(body));
                                retCode = jsonObject.getInt("success");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            //客户端自己判断是否成功。
                            if (retCode == 1) {
                                Toast.makeText(getContext(), "获取拆分记录成功!", Toast.LENGTH_SHORT).show();
                                TextHistory.clear();
                                try {
                                    int count = jsonObject.getInt("count");
                                    for (int i = 0; i < count; i++) {
                                        Map<String, String> map = new HashMap<String, String>();
                                        map.put("content", jsonObject.getJSONObject(String.valueOf(i)).getString("content"));
                                        String datetime = jsonObject.getJSONObject(String.valueOf(i)).getString("datetime");
                                        String date = datetime.split(" ")[0];
                                        String time = datetime.split(" ")[1];
                                        map.put("date", date);
                                        map.put("time", time);
                                        TextHistory.add(map);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getContext(), "获取拆分记录失败!", Toast.LENGTH_SHORT).show();
                                Log.i("111body",body);
                            }
                            InitRecyclerView();
                        }
                    });
                }
            });
        }
        else if(command=="insert"&&LaunchActicity.getLoginStatus()){
            RequestBody post = new FormBody.Builder()
                    .add("command", command)
                    .add("content",etContent.getText().toString())
                    .add("username",LaunchActicity.getUserName())
                    .build();
            //开始请求，填入url，和表单
            final Request request = new Request.Builder()
                    .url(Url)
                    .post(post)
                    .build();

            //客户端回调
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    //失败的情况（一般是网络链接问题，服务器错误等）
                    Log.i("111error", "连接错误");
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    //UI线程运行
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String body = null;
                            JSONObject jsonObject = null;
                            int retCode = 0;
                            try {
                                //临时变量（这是okhttp的一个锅，一次请求的response.body().string()只能用一次，否则就会报错）
                                body = response.body().string();
                                Log.i("111insert",body);
                                //解析出后端返回的数据来
                                jsonObject = new JSONObject(String.valueOf(body));
                                retCode = jsonObject.getInt("success");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            //客户端自己判断是否成功。
                            if (retCode == 1) {
                                Toast.makeText(getContext(), "插入拆分记录成功!", Toast.LENGTH_SHORT).show();
                                queryTextHistory("queryall");
                            } else {
                                Toast.makeText(getContext(), "插入拆分记录失败!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }
    }

    /*
     * nlp分词线程
     * */
    class splitTask extends AsyncTask<String,Void,List<Term>> {
        QMUIFullScreenPopup nlpPopups = QMUIPopups.fullScreenPopup(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View layoutNLP = inflater.inflate(R.layout.layout_nlp, null);
        QMUIRoundButton btnCopy = (QMUIRoundButton) layoutNLP.findViewById(R.id.btn_copy);
        QMUIRoundButton btnSearch = (QMUIRoundButton) layoutNLP.findViewById(R.id.btn_search);
        QMUIRoundButton btnCancel = (QMUIRoundButton) layoutNLP.findViewById(R.id.btn_cancel);
        AutoExpandLinearLayout mAutoLayout = (AutoExpandLinearLayout)layoutNLP.findViewById(R.id.auto_layout);
        QMUILinearLayout linearLayout = (QMUILinearLayout)layoutNLP.findViewById(R.id.layout);
        @Override
        protected void onPreExecute(){
            linearLayout.setRadiusAndShadow(QMUIDisplayHelper.dp2px(getContext(), 15),
                    QMUIDisplayHelper.dp2px(getContext(), 50),
                    0.5f * 1f /100);
            //复制功能
            btnCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String copyContent = ToolBox.getSelectContent(mAutoLayout);
                    if(TextUtils.isEmpty(copyContent)){
                        Toast.makeText(getContext(),"没有选中词组",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getContext(),"["+copyContent+"] 已经复制到剪切板",Toast.LENGTH_SHORT).show();
                        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
                        clipboardManager.setText(copyContent);
                    }

                }
            });
            //搜索功能
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String searchContent = ToolBox.getSelectContent(mAutoLayout);
                    //去除空格（如果有）
                    Pattern pattern = Pattern.compile("((c|C)|(l|L)[a-zA-Z]+)((\\d)|(\\s))\\d+");
                    Matcher matcher = pattern.matcher(searchContent);
                    if(matcher.find())
                    {
                        Log.i("111code",matcher.group());
                        Map<String,String> queryResult = ToolBox.querySolution(matcher.group().replaceAll(" ",""),getContext());
                        if(queryResult==null){
                            Log.i("111queryResult","Query Failed");
                        }
                        else {
                            Log.i("111queryResult",queryResult.get("solution"));
                        }
                    }else{
                        System.out.println("nothing");
                        WebActivity.setSearchContent(searchContent);
                        Intent intent = new Intent(getContext(), WebActivity.class);
                        intent.setAction(RequestCode.ACTION_SEARCH_WEB);
                        startActivityForResult(intent, RequestCode.REQUEST_SEARCH);
                    }
                }
            });
            //关闭分词弹窗
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nlpPopups.dismiss();
                }
            });
        }
        @Override
        protected void onPostExecute(List<Term> termList){
            String space = " ";
            for(int i = 0;i<termList.size();i++){
                String word = termList.get(i).word;
                if(!word.equals(space)) {
                    BangWordView bangWordView = new BangWordView(getContext(), word);
                    mAutoLayout.addView(bangWordView);
                    Log.i("111s:", termList.get(i).toString().split("/")[0]);
                }
            }
            nlpPopups.addView(layoutNLP)
                    .closeBtn(false)
                    .animStyle(QMUIPopup.ANIM_GROW_FROM_CENTER)
                    .onBlankClick(new QMUIFullScreenPopup.OnBlankClickListener() {
                        @Override
                        public void onBlankClick(QMUIFullScreenPopup popup) {
                            Toast.makeText(getContext(), "点击到空白区域", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .onDismiss(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            Toast.makeText(getContext(), "onDismiss", Toast.LENGTH_SHORT).show();
                            queryTextHistory("insert");
                        }
                    })
                    .show(getView());
        }
        @Override
        protected List<Term> doInBackground(String... strings) {
            String text = strings[0];
            List<Term> termList = HanLP.segment(text);
            return termList;
        }
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        View view = getView();
        if (view != null) {
            if (!hidden) {
                if(LaunchActicity.getLoginStatus()){
                    queryTextHistory("queryall");
                }
                else {
                    TextHistory.clear();
                    InitRecyclerView();
                }
            }
            view.requestApplyInsets();
        }
        super.onHiddenChanged(hidden);
    }

}
