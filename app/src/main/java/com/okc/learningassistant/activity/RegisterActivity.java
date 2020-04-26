package com.okc.learningassistant.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.okc.learningassistant.R;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

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

public class RegisterActivity extends AppCompatActivity {
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.btn_register)
    Button btnRegister;
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.et_confirm_pwd)
    EditText etConfirmPwd;
    @BindView(R.id.et_phonenumber)
    EditText etPhoneNumber;
    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.tvtest)
    TextView tvtest;

    Drawable drawable_OK;
    Drawable drawable_NO;

    int retCode = 0;
    String body;
    String Url = "http://47.102.195.6:8082/Register.php";
    boolean status_name = false;
    boolean status_num = false;
    boolean status_mail = false;
    boolean status_pwd = false;
    boolean status_confirm_pwn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        QMUIStatusBarHelper.translucent(this);
        drawable_OK = ContextCompat.getDrawable(RegisterActivity.this, R.drawable.round_check_24);
        drawable_NO = ContextCompat.getDrawable(RegisterActivity.this, R.drawable.round_close_24);
        drawable_OK.setTint(ContextCompat.getColor(RegisterActivity.this, R.color.green));
        drawable_NO.setTint(ContextCompat.getColor(RegisterActivity.this, R.color.qmui_config_color_red));
        getIntent();
        initTopBar();



        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status_name&&status_num&&status_mail&&status_pwd&&status_confirm_pwn) {
                    start(Url);
                }
                else {
                    Toast.makeText(RegisterActivity.this,"请输入完整的信息!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //监测用户名
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if((start-before+1)<3) {
                    etName.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable_NO, null);
                    status_name = false;
                }
                else {
                    etName.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable_OK, null);
                    status_name = true;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //监测手机号
        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if((start-before+1)<11) {
                    etPhoneNumber.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable_NO, null);
                    status_num = false;
                }
                else {
                    etPhoneNumber.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable_OK, null);
                    status_num = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //监测邮箱
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if((start-before+1)<3) {
                    etEmail.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable_NO, null);
                    status_mail = false;
                }
                else {
                    etEmail.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable_OK, null);
                    status_mail = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //监测密码
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if((start-before+1)<6) {
                    etPassword.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable_NO, null);
                    status_pwd = false;
                }
                else {
                    etPassword.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable_OK, null);
                    status_pwd = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //监测密码确认
        etConfirmPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if((start-before+1)<6 ||
                    !etConfirmPwd.getText().toString().equals(etPassword.getText().toString())) {
                    etConfirmPwd.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable_NO, null);
                    status_confirm_pwn = false;
                }
                else {
                    etConfirmPwd.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable_OK, null);
                    status_confirm_pwn = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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
        mTopBar.setTitle("注册").setTextColor(getResources().getColor(R.color.white));
    }

    private void start(String url) {
        //初始化okhttp客户端
        OkHttpClient client = new OkHttpClient.Builder().build();
        //创建post表单，获取username和password（没有做非空判断）
        RequestBody post = new FormBody.Builder()
                .add("username", etName.getText().toString())
                .add("password", etPassword.getText().toString())
                .add("mail", etEmail.getText().toString())
                .add("phonenumber", etPhoneNumber.getText().toString())
                .build();
        //开始请求，填入url，和表单
        final Request request = new Request.Builder()
                .url(url)
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
                            RegisterActivity.this.body = response.body().string();
                            tvtest.setText(body);
                            //解析出后端返回的数据来
                            JSONObject jsonObject = new JSONObject(String.valueOf(RegisterActivity.this.body));
                            retCode = jsonObject.getInt("success");
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }

                        //客户端自己判断是否成功。
                        if (retCode == 1) {
                            Toast.makeText(RegisterActivity.this,"成功!",Toast.LENGTH_SHORT).show();
                            setResult(1);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this,"错误:",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
