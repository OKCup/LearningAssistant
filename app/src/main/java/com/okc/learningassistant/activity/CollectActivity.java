package com.okc.learningassistant.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.okc.learningassistant.R;
import com.okc.learningassistant.adapter.HistoryAdapter;
import com.okc.learningassistant.fragment.HomeFragment;
import com.okc.learningassistant.helper.RequestCode;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CollectActivity extends AppCompatActivity {
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    Intent intent;

    private HistoryAdapter mRecyclerViewAdapter;

    private LinearLayoutManager mPagerLayoutManager;

    private String Url = "http://47.102.195.6:8082/QueryCollect.php";

    private List<Map<String,String>> listCollect = new ArrayList<Map<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        ButterKnife.bind(this);
        intent = getIntent();
        initTopBar();
        queryCollect("queryall");
    }

    protected void initTopBar(){
        QMUIStatusBarHelper.translucent(this);
        //mTopBar.setSubTitle(searchContent).setTextColor(getResources().getColor(R.color.white));
        mTopBar.setTitle("收藏").setTextColor(getResources().getColor(R.color.white));
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RequestCode.COLLECT_COMPLETE,intent);
                finish();
            }
        });
    }

    private void InitRecyclerView(){
        mPagerLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mPagerLayoutManager);
        //mRecyclerViewAdapter = new QDRecyclerViewAdapter();
        mRecyclerViewAdapter = new HistoryAdapter(listCollect);
        mRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                Toast.makeText(CollectActivity.this, "onItemClick:"+position, Toast.LENGTH_SHORT).show();
                HashMap<String,String> clickItem = (HashMap<String, String>) adapter.getItem(position);
                Log.i("111adapterItem",clickItem.get("content"));
                WebActivity.setHisURL(clickItem.get("content"));
                WebActivity.setSearchContent(clickItem.get("title"));
                Intent intent = new Intent(CollectActivity.this,WebActivity.class);
                intent.setAction(RequestCode.ACTION_HISTORY_WEB);
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    public void queryCollect(String command) {
        //初始化okhttp客户端
        OkHttpClient client = new OkHttpClient.Builder().build();
        //创建post表单，获取username和password（没有做非空判断）
        if(command=="queryall"&&LaunchActicity.getLoginStatus()) {
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
                    runOnUiThread(new Runnable() {
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
                                Toast.makeText(CollectActivity.this, "获取收藏记录成功!", Toast.LENGTH_SHORT).show();
                                listCollect.clear();
                                try {
                                    int count = jsonObject.getInt("count");
                                    for (int i = 0; i < count; i++) {
                                        Map<String, String> map = new HashMap<String, String>();
                                        map.put("title", jsonObject.getJSONObject(String.valueOf(i)).getString("title"));
                                        map.put("content", jsonObject.getJSONObject(String.valueOf(i)).getString("content"));
                                        String datetime = jsonObject.getJSONObject(String.valueOf(i)).getString("datetime");
                                        String date = datetime.split(" ")[0];
                                        String time = datetime.split(" ")[1];
                                        map.put("date", date);
                                        map.put("time", time);
                                        listCollect.add(map);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(CollectActivity.this, "获取收藏记录失败!", Toast.LENGTH_SHORT).show();
                                Log.i("111body",body);
                            }
                            InitRecyclerView();
                        }
                    });
                }
            });
        }
    }

}
