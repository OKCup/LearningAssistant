package com.okc.learningassistant.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.okc.learningassistant.R;
import com.okc.learningassistant.fragment.HomeFragment;
import com.okc.learningassistant.helper.RequestCode;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
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

public class WebActivity extends AppCompatActivity {
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.wv_search)
    WebView searchWebView;
    Intent intent;
    public static String searchContent = null;
    private String postUrl = "http://47.102.195.6:8082/QueryCollect.php";
    String URL = "http://m.baidu.com/s?" +
            "wd=" +
            searchContent +
            "&rsv_bp=0&ch=&tn=baidu&bar=&rsv_spt=3&ie=utf-8&rsv_sug3=3&rsv_sug=0&rsv_sug4=95&rsv_sug1=1&inputT=1001";

    String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 9_2 like Mac OS X) " +
            "AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13C75 Safari/601.1";

    public static void setSearchContent(String searchContent) {
        WebActivity.searchContent = searchContent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        ButterKnife.bind(this);
        intent = getIntent();
        initTopBar();
        searchWebView.getSettings().setDomStorageEnabled(true);
        searchWebView.getSettings().setUserAgentString(userAgent);
        searchWebView.getSettings().setJavaScriptEnabled(true);
        searchWebView.loadUrl(URL);
        searchWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

    }

    protected void initTopBar(){
        QMUIStatusBarHelper.translucent(this);
        //mTopBar.setSubTitle(searchContent).setTextColor(getResources().getColor(R.color.white));
        mTopBar.setTitle(searchContent).setTextColor(getResources().getColor(R.color.white));
        mTopBar.addRightImageButton(R.drawable.round_more_horiz_24,R.id.topbar_right_setting_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheet();
            }
        });
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RequestCode.SEARCH_COMPLETE,intent);
                HomeFragment.setNewHistory(searchWebView.getUrl(),searchWebView.getTitle());
                finish();
            }
        });
    }

    private void showBottomSheet() {
        new QMUIBottomSheet.BottomListSheetBuilder(this)
                .addItem("复制链接")
                .addItem("收藏")
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        switch (position) {
                            case 0:
                                Log.i("111webtitle",searchWebView.getTitle());
                                Log.i("111url",searchWebView.getUrl());
                                Log.i("useragent",searchWebView.getSettings().getUserAgentString());
                                break;
                            case 1:
                                queryHistory("insert");
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .build().show();
    }

    //插入收藏记录
    public void queryHistory(String command) {
        //初始化okhttp客户端
        OkHttpClient client = new OkHttpClient.Builder().build();
        //创建post表单，获取username和password（没有做非空判断）
        if(command=="insert"&&LaunchActicity.getLoginStatus()){
            RequestBody post = new FormBody.Builder()
                    .add("command", command)
                    .add("url",searchWebView.getUrl())
                    .add("title",searchWebView.getTitle())
                    .add("username",LaunchActicity.getUserName())
                    .build();
            //开始请求，填入url，和表单
            final Request request = new Request.Builder()
                    .url(postUrl)
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
                                Toast.makeText(WebActivity.this, "插入收藏记录成功!", Toast.LENGTH_SHORT).show();
                                queryHistory("queryall");
                            } else {
                                Toast.makeText(WebActivity.this, "插入收藏记录失败!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }
    }
}
