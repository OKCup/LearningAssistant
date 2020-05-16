package com.okc.learningassistant.activity;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.okc.learningassistant.R;
import com.okc.learningassistant.fragment.HomeFragment;
import com.okc.learningassistant.fragment.PersonFragment;
import com.okc.learningassistant.fragment.TextFragment;
import com.okc.learningassistant.helper.AppPermissionUtil;
import com.okc.learningassistant.helper.RequestCode;
import com.okc.learningassistant.helper.ToolBox;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.tab.QMUITab;
import com.qmuiteam.qmui.widget.tab.QMUITabBuilder;
import com.qmuiteam.qmui.widget.tab.QMUITabSegment;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LaunchActicity extends AppCompatActivity{
    public static Context ParentContext;

    private static String DATAPATH;
    private static String tessdata;
    private static String DEFAULT_LANGUAGE;
    private static String DEFAULT_TRAINED_FILE_NAME;
    private static String LANGUAGE_PATH ;
    private static String DATABASE_PATH;
    private static String DATABASE_NAME;
    private static String USER_NAME_PATH;
    private static String USER_PWD_PATH;
    private static Boolean loginStatus = false;

    //标题和详细信息
    private static String UserName = "点击登录";
    private static String Email = "登录后可同步数据";


    @BindView(R.id.Tab)
    QMUITabSegment mTabSegment;
    private HomeFragment homeFragment;
    private TextFragment textFragment;
    private PersonFragment personFragment;
    Context mContext = this;
    private boolean loginFinish = false;

    private FragmentManager fragmentManager;

    public static String getDATAPATH() {
        return DATAPATH;
    }

    public static String getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }

    public static Context getParentContext() {
        return ParentContext;
    }

    public static String getDatabasePath() {
        return DATABASE_PATH;
    }

    public static void setContent(String userName, String email){
        UserName = userName;
        Email = email;
    }

    public static String getUserName() {
        return UserName;
    }

    public static String getEmail() {
        return Email;
    }

    public static void setLoginStatus(Boolean loginStatus) {
        LaunchActicity.loginStatus = loginStatus;
    }

    public static String getUserNamePath() {
        return USER_NAME_PATH;
    }

    public static String getUserPwdPath() {
        return USER_PWD_PATH;
    }

    public static Boolean getLoginStatus() {
        return loginStatus;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ButterKnife.bind(this);
        Log.i("111life","in launch activity");
        ParentContext = this;
        DATABASE_NAME = "errordata.db";
        DATAPATH = getExternalFilesDir("")+"/";
        //tessdata文件夹在系统中的路径
        tessdata = DATAPATH + File.separator + "tessdata";
        //默认语言
        DEFAULT_LANGUAGE = "eng";
        //assets里的文件名
        DEFAULT_TRAINED_FILE_NAME = DEFAULT_LANGUAGE + ".traineddata";
        //数据集在系统中的路径
        LANGUAGE_PATH = tessdata + File.separator + DEFAULT_TRAINED_FILE_NAME;
        //整理的错误信息数据库在系统中的路径
        DATABASE_PATH = DATAPATH + File.separator + "database" + File.separator + DATABASE_NAME;
        //存放已登录用户的用户名的文件路径
        USER_NAME_PATH = DATAPATH + File.separator + "user" + File.separator + "name.txt";
        //存放已登录用户的密码的文件路径
        USER_PWD_PATH = DATAPATH + File.separator + "user" + File.separator + "pwd.txt";

        getPermission();

        initOpenCV();
        //复制数据集到SD卡
        ToolBox.copyToSD(mContext,LANGUAGE_PATH, DEFAULT_TRAINED_FILE_NAME);
        //复制错误代码归纳到SD卡
        ToolBox.copyToSD(mContext,DATABASE_PATH,DATABASE_NAME);

        //如果用户登录过，则自动登录
        File namefile = new File(USER_NAME_PATH);
        File pwdfile = new File(USER_PWD_PATH);

        initTabs();
        //mTabSegment.selectTab(0);
        fragmentManager = getSupportFragmentManager();

        //默认选中第一个tab
        showFragment(1);
        mTabSegment.addOnTabSelectedListener(new QMUITabSegment.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int index) {
                showFragment(index+1);
            }

            @Override
            public void onTabUnselected(int index) {

            }

            @Override
            public void onTabReselected(int index) {

            }

            @Override
            public void onDoubleTap(int index) {

            }
        });

        if(namefile.exists()&&pwdfile.exists()){
            autoLogin(ToolBox.readTXT(namefile),ToolBox.readTXT(pwdfile));
        }
    }

    private void initOpenCV() {
        System.loadLibrary("opencv_java4");
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "Could not load OpenCV Lib.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Load OpenCV Lib Success.", Toast.LENGTH_SHORT).show();
            Logger.getLogger("org.opencv.osgi").log(Level.INFO, "Successfully loaded OpenCV native library.");
        }
    }

    private void initTabs() {

        QMUITabBuilder builder = mTabSegment.tabBuilder();
        builder.setSelectedIconScale(1.2f)
                .setTextSize(QMUIDisplayHelper.sp2px(getApplicationContext(), 13), QMUIDisplayHelper.sp2px(getApplicationContext(), 15))
                .setColor(getResources().getColor(R.color.qmui_config_color_gray_5),getResources().getColor(R.color.app_color_blue))
                .setDynamicChangeIconColor(true);
        QMUITab home = builder
                //.setNormalDrawable(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.outline_home_white_48))
                //.setSelectedDrawable(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.outline_home_white_48))
                .setNormalDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_home_24))
                .setSelectedDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_home_24))
                .setText("首页")
                .build(getApplicationContext());
        QMUITab text = builder
                .setNormalDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_text_24))
                .setSelectedDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_text_24))
                .setText("文本")
                .build(getApplicationContext());
        QMUITab person = builder
                .setNormalDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_person_24))
                .setSelectedDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_person_24))
                .setText("我的")
                .build(getApplicationContext());

        mTabSegment.addTab(home)
                .addTab(text)
                .addTab(person);
        mTabSegment.selectTab(0);
    }

    private void showFragment(int page) {
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // 想要显示一个fragment,先隐藏所有fragment，防止重叠
        hideFragments(ft);
        switch (page) {
            case 1:
                // 如果fragment已经存在则将其显示出来
                if (homeFragment != null) {
                    ft.show(homeFragment);
                }
                    // 否则添加fragment，注意添加后是会显示出来的，replace方法也是先remove后add
                else {
                    homeFragment = new HomeFragment();
                    ft.add(R.id.fl_content_launch, homeFragment);
                }
                break;
            case 2:
                if (textFragment != null) {
                    ft.show(textFragment);
                }
                else {
                    textFragment = new TextFragment();
                    ft.add(R.id.fl_content_launch, textFragment);
                }
                break;
            case 3:
                if (personFragment != null) {
                    ft.show(personFragment);
                }
                else {
                    personFragment = new PersonFragment();
                    ft.add(R.id.fl_content_launch, personFragment);
                }
                break;
        }
        ft.commit();
    }
    // 当fragment已被实例化，相当于发生过切换，就隐藏起来
    public void hideFragments(FragmentTransaction ft) {
        if (homeFragment != null)
            ft.hide(homeFragment);
        if (textFragment != null)
            ft.hide(textFragment);
        if (personFragment != null)
            ft.hide(personFragment);
    }


    private void getPermission(){
        AppPermissionUtil.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO}, new AppPermissionUtil.OnPermissionListener() {
            @Override
            public void onPermissionGranted() {
                Log.i("Permission:","Granted");
                //授权
            }

            @Override
            public void onPermissionDenied() {
                Log.i("Permission:","Denied");
                //没有授权，或者有一个权限没有授权
            }
        });
    }

    public void autoLogin(String name, String pwd) {
        //初始化okhttp客户端
        OkHttpClient client = new OkHttpClient.Builder().build();
        //创建post表单，获取username和password（没有做非空判断）
        RequestBody post = new FormBody.Builder()
                .add("username", name)
                .add("password", pwd)
                .build();
        //开始请求，填入url，和表单
        final Request request = new Request.Builder()
                .url("http://47.102.195.6:8082/Login.php")
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
                        String body;
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
                            Toast.makeText(LaunchActicity.this,"成功!",Toast.LENGTH_SHORT).show();
                            try {
                                LaunchActicity.setContent(name,jsonObject.getString("mail"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            LaunchActicity.setLoginStatus(true);
                        } else {
                            Toast.makeText(LaunchActicity.this,"错误:",Toast.LENGTH_SHORT).show();
                        }
                        showFragment(1);
                    }

                });
            }
        });
    }

}
