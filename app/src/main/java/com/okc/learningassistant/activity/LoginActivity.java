package com.okc.learningassistant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.okc.learningassistant.R;
import com.okc.learningassistant.fragment.PersonFragment;
import com.okc.learningassistant.helper.RequestCode;
import com.okc.learningassistant.helper.ToolBox;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.tv_register)
    TextView tvRegister;
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.btn_login)
    QMUIRoundButton btnLogin;

    @BindView(R.id.tvtest)
    TextView tvtest;

    //int RequestCode = 0x0001;
    String body;
    JSONObject jsonObject;
    int retCode = 0;
    String Url = "http://47.102.195.6:8082/Login.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        QMUIStatusBarHelper.translucent(this);
        getIntent();
        initTopBar();
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, RequestCode.REQUEST_REGISTER);
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogin(etName.getText().toString(), etPassword.getText().toString());
            }
        });
    }

    private void initTopBar(){
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTopBar.setTitle("登录").setTextColor(getResources().getColor(R.color.white));
    }
    public void startLogin(String name, String pwd) {
        //初始化okhttp客户端
        OkHttpClient client = new OkHttpClient.Builder().build();
        //创建post表单，获取username和password（没有做非空判断）
        RequestBody post = new FormBody.Builder()
                .add("username", name)
                .add("password", pwd)
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
                Log.i("111error","连接错误");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                //UI线程运行
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            //临时变量（这是okhttp的一个锅，一次请求的response.body().string()只能用一次，否则就会报错）
                            LoginActivity.this.body = response.body().string();
                            tvtest.setText(body);
                            //解析出后端返回的数据来
                            jsonObject = new JSONObject(String.valueOf(LoginActivity.this.body));
                            retCode = jsonObject.getInt("success");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //客户端自己判断是否成功。
                        if (retCode == 1) {
                            Toast.makeText(LoginActivity.this,"成功!",Toast.LENGTH_SHORT).show();
                            try {
                                LaunchActicity.setContent(etName.getText().toString(),jsonObject.getString("mail"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            setResult(RequestCode.LOGIN_COMPLETE);
                            LaunchActicity.setLoginStatus(true);
                            //登录成功，则保存当前用户的用户名密码，用于自动登录
                            ToolBox.createSDFile(LaunchActicity.getUserNamePath());
                            ToolBox.createSDFile(LaunchActicity.getUserPwdPath());
                            ToolBox.writeTXT(LaunchActicity.getUserNamePath(),etName.getText().toString());
                            ToolBox.writeTXT(LaunchActicity.getUserPwdPath(),etPassword.getText().toString());
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this,"错误:",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}

