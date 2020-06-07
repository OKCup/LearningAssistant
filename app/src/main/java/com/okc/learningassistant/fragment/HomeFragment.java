package com.okc.learningassistant.fragment;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.okc.learningassistant.R;
import com.okc.learningassistant.activity.LaunchActicity;
import com.okc.learningassistant.activity.LoginActivity;
import com.okc.learningassistant.activity.WebActivity;
import com.okc.learningassistant.adapter.HistoryAdapter;
import com.okc.learningassistant.adapter.QDRecyclerViewAdapter;
import com.okc.learningassistant.camera.MyCamera;
import com.okc.learningassistant.helper.ErrorDBHelper;
import com.okc.learningassistant.helper.Popups;
import com.okc.learningassistant.helper.RequestCode;
import com.okc.learningassistant.helper.ToolBox;
import com.okc.learningassistant.widget.AutoExpandLinearLayout;
import com.okc.learningassistant.widget.BangWordView;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUICollapsingTopBarLayout;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.popup.QMUIFullScreenPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopups;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
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
import static androidx.constraintlayout.widget.Constraints.TAG;


public class HomeFragment extends Fragment {

    private Context mContext;
    private String DATAPATH;
    private String DEFAULT_LANGUAGE;
    private String Url = "http://47.102.195.6:8082/QueryHistory.php";
    private List<Map<String,String>> listHistory = new ArrayList<Map<String, String>>();

    private static String webUrl = null;
    private static String webTitle = null;

    //@BindView(R.id.camera)
    //ImageView mCamera;
    @BindView(R.id.camera_bg)
    QMUIRadiusImageView mCamera;
    //@BindView(R.id.camera_text)
    //TextView mCameraText;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.collapsing_topbar_layout)
    QMUICollapsingTopBarLayout mCollapsingTopBarLayout;
    @BindView(R.id.text)
    TextView textView;
    @BindView(R.id.img_gray)
    ImageView img_gray;
    @BindView(R.id.img_binary)
    ImageView img_binary;
    @BindView(R.id.btn_ide_select)
    QMUIRoundButton ideSelect;
    @BindView(R.id.layout_home_login)
    RelativeLayout layoutHomeLogin;
    @BindView(R.id.btn_home_login)
    QMUIRoundButton btnHomeLogin;

    private Uri GrayUri = null;
    private Uri BinaryUri = null;
    private static Uri CroppedUri = null;
    private String btnLoginText = "登录/注册";

    private int ideCheckIndex = 0;
    final String[] ideItems = new String[]{"VC6", "PyCharm", "其他"};

    private Bitmap srcBitmap;
    private Bitmap grayBitmap;
    private Bitmap binaryBitmap;

    //private QDRecyclerViewAdapter mRecyclerViewAdapter;
    private HistoryAdapter mRecyclerViewAdapter;

    private LinearLayoutManager mPagerLayoutManager;

    public static void setCroppedUri(Uri uri){
        CroppedUri = uri;
    }

    public static void setNewHistory(String url,String title){
        webUrl = url;
        webTitle = title;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(getActivity());
        mContext = getActivity();
        DATAPATH = LaunchActicity.getDATAPATH();
        DEFAULT_LANGUAGE = LaunchActicity.getDefaultLanguage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_collapse, container, false);
        view.setFitsSystemWindows(true);
        ButterKnife.bind(this, view);
        Log.i("location:","homefragment");
        if(LaunchActicity.getLoginStatus()){
            btnHomeLogin.setText(LaunchActicity.getUserName());
        }
        Init();
        InitCollapseTopBar();
        queryHistory("queryall");
        //改为获取历史记录后再初始化
        //InitRecyclerView();

        //设置折叠状态栏不同状态下的标题
        mCollapsingTopBarLayout.setScrimUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.i(TAG, "scrim: " + animation.getAnimatedValue());
                if(Integer.parseInt(animation.getAnimatedValue().toString())==255){
                    mCollapsingTopBarLayout.setTitleEnabled(true);
                    QMUIStatusBarHelper.setStatusBarLightMode(getActivity());
                    mCamera.setClickable(false);
                    Log.i(TAG,"111enter:true");
                }
                else {
                    mCollapsingTopBarLayout.setTitleEnabled(false);
                    QMUIStatusBarHelper.setStatusBarDarkMode(getActivity());
                    mCamera.setClickable(true);
                    Log.i(TAG,"111enter:flase");
                }
            }
        });


        //打开相机
        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MyCamera.class);
                Bundle bundle = new Bundle();
                startActivityForResult(intent, RequestCode.REQUEST_PHOTO);
            }
        });
        //编译器选择
        ideSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int mCurrentDialogStyle = com.qmuiteam.qmui.R.style.QMUI_Dialog;
                new QMUIDialog.CheckableDialogBuilder(getActivity())
                        .setCheckedIndex(ideCheckIndex)
                        .addItems(ideItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ideCheckIndex = which;
                                Toast.makeText(getActivity(), "你选择了 " + ideItems[which], Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                        .create(mCurrentDialogStyle).show();
            }
        });

        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {
                if(!LaunchActicity.getLoginStatus()){
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivityForResult(intent, RequestCode.REQUEST_LOGIN);
                }
            }
        };
        //主页登录框
        layoutHomeLogin.setOnClickListener(listener);
        btnHomeLogin.setOnClickListener(listener);

        return view;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.mContext = (Activity)context;
    }


    /**
     *  为控件添加尺寸渐变动画
     */
    private static void setScaleAni(View V, float fromScale, float toScale, long ANITIME) {
        AnimationSet aniSet = new AnimationSet(true);
        // final int ANITIME = 500;

        // 尺寸变化动画，设置尺寸变化
        ScaleAnimation scaleAni = new ScaleAnimation(fromScale, toScale, fromScale, toScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAni.setDuration(ANITIME);	// 设置动画效果时间
        scaleAni.setRepeatCount(-1);
        scaleAni.setRepeatMode(2);
        aniSet.addAnimation(scaleAni);	// 将动画效果添加到动画集中

        V.startAnimation(aniSet);		// 添加光效动画到控件
    }


    /*
    * 初始化图标动画效果
    * */
    private void Init(){
        mCamera.setCircle(true);
        mCamera.setBorderWidth(0);
        setScaleAni(mCamera, 1, Float.parseFloat("1.1"), 2000);
        //setScaleAni(mCamera, 1, Float.parseFloat("1.1"), 2000);
        //setScaleAni(mCameraText, 1, Float.parseFloat("1.1"), 2000);
    }


    /*
    * 初始化可折叠的状态栏
    * */
    private void InitCollapseTopBar(){
        mCollapsingTopBarLayout.setTitleEnabled(false);
        mCollapsingTopBarLayout.setTitle("历史记录");
        mCollapsingTopBarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.qmui_config_color_black));
        //mCollapsingTopBarLayout.setContentScrimColor(getResources().getColor(R.color.qmui_config_color_white));
        //mCollapsingTopBarLayout.setContentScrim(getResources().getDrawable(R.drawable.camera_gradual_bg));
        //mCollapsingTopBarLayout.setStatusBarScrimColor(getResources().getColor(R.color.qmui_config_color_white));
        mCollapsingTopBarLayout.setScrimAnimationDuration(1);
    }

    /*
    *初始化滚动界面
    * */
    private void InitRecyclerView(){
        mPagerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mPagerLayoutManager);
        //mRecyclerViewAdapter = new QDRecyclerViewAdapter();
        mRecyclerViewAdapter = new HistoryAdapter(listHistory);
        // 设置点击事件
        mRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                Toast.makeText(getContext(), "onItemClick:"+position, Toast.LENGTH_SHORT).show();
                HashMap<String,String> clickItem = (HashMap<String, String>) adapter.getItem(position);
                Log.i("111adapterItem",clickItem.get("content"));
                WebActivity.setHisURL(clickItem.get("content"));
                WebActivity.setSearchContent(clickItem.get("title"));
                Intent intent = new Intent(getContext(),WebActivity.class);
                intent.setAction(RequestCode.ACTION_HISTORY_WEB);
                startActivity(intent);

            }
        });
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.gc();
        if (resultCode == RequestCode.PHOTO_COMPLETE && CroppedUri != null) {
            new imgProcessTask().execute();
        }
        else if(resultCode == RequestCode.LOGIN_COMPLETE){
            btnHomeLogin.setText(LaunchActicity.getUserName());
            queryHistory("queryall");
        }
        else if(resultCode == RequestCode.SEARCH_COMPLETE){
            queryHistory("insert");
        }
    }

    private void Identify(Uri imageuri) throws IOException {
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(DATAPATH, DEFAULT_LANGUAGE);//参数后面有说明。
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), imageuri);
        tessBaseAPI.setImage(bitmap);
        String text = tessBaseAPI.getUTF8Text();
        Log.i("111identify",text);
    }
    /*
    * OCR识别的异步任务
    * */
    class IdentifyTask extends AsyncTask<Void, Integer, String> {

        QMUITipDialog identifyTipDialog;
        @Override
        protected void onPreExecute(){

            identifyTipDialog = new QMUITipDialog.Builder(getContext())
                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                    .setTipWord("正在识别")
                    .create();
            identifyTipDialog.show();
        }

        @Override
        protected void onPostExecute(String text) {//后台任务执行完之后被调用，在ui线程执行
            if(text!=null) {
                identifyTipDialog.cancel();
                //textView.setText(text);
                //splitText(text);
                new splitTask().execute(text.replaceAll("\\n",""));
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(), "识别失败", Toast.LENGTH_LONG).show();
            }
        }


        @Override
        protected String doInBackground(Void... voids) {
            TessBaseAPI tessBaseAPI = new TessBaseAPI();
            Log.i("111path:",DATAPATH);
            tessBaseAPI.init(DATAPATH, DEFAULT_LANGUAGE);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), BinaryUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            tessBaseAPI.setImage(bitmap);
            String text = tessBaseAPI.getUTF8Text();
            Log.i("111identify",text);
            tessBaseAPI.end();
            return text;
        }
    }

    /*
    * 图像处理异步任务
    * */
    class imgProcessTask extends AsyncTask<Void,Void,Void>{
        QMUITipDialog imgTipDialog;
        @Override
        protected void onPreExecute(){

            imgTipDialog = new QMUITipDialog.Builder(getContext())
                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                    .setTipWord("图片处理中")
                    .create();
            imgTipDialog.show();
        }

        @Override
        protected void onPostExecute(Void voids){
            //img_binary.setImageBitmap(binaryBitmap);
            imgTipDialog.cancel();
            new IdentifyTask().execute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            GrayUri = null;
            try {
                srcBitmap = getSrcBitmap(CroppedUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            BinaryUri = null;
            Tuple ret = imgProcess(srcBitmap,"default");
            BinaryUri = (Uri) ret.uri;
            binaryBitmap = (Bitmap) ret.bitmap;
            return null;
        }
    }


    /*
     * 获取原始图片的bitmap
     * */
    private Bitmap getSrcBitmap(Uri uri) throws IOException {
        //mContext.grantUriPermission("com.okc.learningassistant.fragment", uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
    }

    private Tuple imgProcess(Bitmap src, String mod){
        Mat srcMat = new Mat();
        Mat dstMat = new Mat();
        Uri tmpuri;
        Log.i("111bitmapmem",String.valueOf(src.getAllocationByteCount()));
        Bitmap dst = Bitmap.createBitmap(src.getWidth(),src.getHeight(),Bitmap.Config.RGB_565);
        Utils.bitmapToMat(src,srcMat);
        Imgproc.cvtColor(srcMat,srcMat,Imgproc.COLOR_RGBA2RGB);
        switch (mod){
            //灰度化
            case "gray":
                Imgproc.cvtColor(srcMat,dstMat, Imgproc.COLOR_RGB2GRAY);
                break;
                //二值化
            case "binary":
                Imgproc.cvtColor(srcMat,srcMat,Imgproc.COLOR_RGB2GRAY);
                Imgproc.threshold(srcMat, dstMat,0,255,Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
                break;
                //滤波
            case "blur":
                Imgproc.medianBlur(srcMat, dstMat,5);
                //Imgproc.blur(srcMat,dstMat,new Size(5,5));
                break;
                //提高亮度
            case "light":
                Imgproc.cvtColor(srcMat,srcMat,Imgproc.COLOR_RGB2GRAY);
                //dstMat = light(srcMat);
                srcMat.convertTo(dstMat, -1, 1.8, 60);
                break;
                //膨胀
            case "dilate":
                Mat ele_d = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(5,5));
                Imgproc.dilate(srcMat, dstMat, ele_d);
                break;
            case "erode":
                Mat ele_e = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(2,2));
                Imgproc.erode(srcMat, dstMat, ele_e);
                break;
            default:
                //先灰度化
                Mat grayMat = new Mat();
                Imgproc.cvtColor(srcMat,grayMat, Imgproc.COLOR_RGB2GRAY);
                //灰度化后调高亮度
                Mat lightMat = new Mat();
                //lightMat = light(grayMat);
                grayMat.convertTo(lightMat, -1, 1.5, 40);
                //调高亮度后滤波
                Mat blurMat = new Mat();
                Imgproc.medianBlur(lightMat, blurMat,5);
                //Imgproc.blur(lightMat,blurMat,new Size(5,5));
                //Imgproc.medianBlur(grayMat, blurMat,5);
                //滤波后二值化
                Mat binaryMat = new Mat();
                Imgproc.threshold(blurMat, binaryMat,0,255,Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
                dstMat = binaryMat;
                break;
        }
        //矩阵转为bitmap
        Utils.matToBitmap(dstMat,dst);
        File file = null;
        try {
            file = ToolBox.createImageFile(mContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
        tmpuri = ToolBox.createImageUri(mContext,file);
        //保存图片
        ToolBox.saveBitmap(file,dst);
        ToolBox.saveToGallery(mContext,file,tmpuri);
        Tuple ret = new Tuple(dst, tmpuri);
        return ret;
    }
    /*
    * 提高图片亮度
    * */

    private Mat light(Mat srcmat){
        Mat dstmat = new Mat(srcmat.size(), srcmat.type());
        int channels = srcmat.channels();//获取图像通道数
        double[] pixel = new double[3];
        float alpha=1.8f;
        float bate=60f;
        for (int i = 0, rlen = srcmat.rows(); i < rlen; i++) {
            for (int j = 0, clen = srcmat.cols(); j < clen; j++) {
                if (channels == 3) {//1 图片为3通道即平常的(R,G,B)
                    //Log.i("channel","3");
                    pixel = srcmat.get(i, j).clone();
                    pixel[0] = pixel[0]*alpha+bate;//R
                    pixel[1] = pixel[1]*alpha+bate;//G
                    pixel[2] = pixel[2]*alpha+bate;//B
                    dstmat.put(i, j, pixel);
                } else {//2 图片为单通道即灰度图
                    //Log.i("channel","1");
                    pixel=srcmat.get(i, j).clone();
                    dstmat.put(i, j, pixel[0]*alpha+bate);
                }
            }
        }
        return dstmat;
    }

    public static class Tuple<Bitmap, Uri> {
        private final Bitmap bitmap;
        private final Uri uri;

        Tuple(Bitmap b, Uri u) {
            this.bitmap = b;
            this.uri = u;
        }
    }

    /*
     * nlp分词线程
     * */
    class splitTask extends AsyncTask<String,Void,List<Term>>{
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
                    String copyContent = getSelectContent(mAutoLayout);
                    if(TextUtils.isEmpty(copyContent)){
                        Toast.makeText(getContext(),"没有选中词组",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(mContext,"["+copyContent+"] 已经复制到剪切板",Toast.LENGTH_SHORT).show();
                        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
                        clipboardManager.setText(copyContent);
                    }

                }
            });
            //搜索功能
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String searchContent = getSelectContent(mAutoLayout);
                    //去除空格（如果有）
                    Pattern pattern = Pattern.compile("((c|C)|(l|L)[a-zA-Z]+)((\\d)|(\\s))\\d+");
                    Matcher matcher = pattern.matcher(searchContent);
                    if(matcher.find())
                    {
                        Log.i("111code",matcher.group());
                        Map<String,String> queryResult = ToolBox.querySolution(matcher.group().replaceAll(" ",""),mContext);
                        if(queryResult==null){
                            Log.i("111queryResult","Query Failed");
                        }
                        else {
                            Log.i("111queryResult",queryResult.get("solution"));
                            createOfflineSearchResultPopup(mContext,queryResult.get("content"),queryResult.get("solution"));
                        }
                    }else{
                        System.out.println("nothing");
                        WebActivity.setSearchContent(searchContent);
                        Intent intent = new Intent(mContext, WebActivity.class);
                        intent.setAction(RequestCode.ACTION_SEARCH_WEB);
                        startActivityForResult(intent,RequestCode.REQUEST_SEARCH);
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
    /*
    * nlp分词
    * */
    private void splitText(String text){
        LayoutInflater inflater = getLayoutInflater();
        View layoutNLP = inflater.inflate(R.layout.layout_nlp, null);
        QMUIRoundButton btnCopy = (QMUIRoundButton) layoutNLP.findViewById(R.id.btn_copy);
        QMUIRoundButton btnSearch = (QMUIRoundButton) layoutNLP.findViewById(R.id.btn_search);
        QMUIRoundButton btnCancel = (QMUIRoundButton) layoutNLP.findViewById(R.id.btn_cancel);
        AutoExpandLinearLayout mAutoLayout = (AutoExpandLinearLayout)layoutNLP.findViewById(R.id.auto_layout);

        List<Term> termList = HanLP.segment(text);
        for(int i = 0;i<termList.size();i++){
            String word = termList.get(i).toString().split("/")[0];
            BangWordView bangWordView = new BangWordView(getContext(),word);
            mAutoLayout.addView(bangWordView);
            Log.i("111s:",termList.get(i).toString().split("/")[0]);
        }

        //复制所选内容
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = mAutoLayout.getChildCount();
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < count; i++) {
                    CheckBox checkBox = (CheckBox) mAutoLayout.getChildAt(i);
                    if (checkBox.isChecked()){
                        builder.append(checkBox.getText());
                    }
                }
                String str = builder.toString();
                if(TextUtils.isEmpty(str)){
                    Toast.makeText(getContext(),"没有选中词组",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(mContext,"["+str+"] 已经复制到剪切板",Toast.LENGTH_SHORT).show();
                    ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
                    clipboardManager.setText(str);
                }
            }
        });

        //查找所选内容
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        QMUIPopups.fullScreenPopup(getContext())
                .addView(layoutNLP)
                .closeBtn(false)
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
                    }
                })
                .show(getView());
    }

    /*
    private Map<String, String> querySolution(String code){
        ErrorDBHelper helper = new ErrorDBHelper(mContext,LaunchActicity.getDatabasePath());
        SQLiteDatabase errDB = helper.openDatabase();
        Map<String,String> result = helper.queryCode(code);
        helper.closeDatabase();
        return result;
    }*/

    private String getSelectContent(AutoExpandLinearLayout layout){
        int count = layout.getChildCount();
        String result = null;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            CheckBox checkBox = (CheckBox) layout.getChildAt(i);
            if (checkBox.isChecked()){
                builder.append(checkBox.getText() + " ");
            }
        }
        result = builder.toString();
        return result;
    }
    //查询历史记录
    public void queryHistory(String command) {
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
                                Toast.makeText(mContext, "获取历史记录成功!", Toast.LENGTH_SHORT).show();
                                listHistory.clear();
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
                                        listHistory.add(map);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(mContext, "获取历史记录失败!", Toast.LENGTH_SHORT).show();
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
                    .add("url",webUrl)
                    .add("title",webTitle)
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
                                Toast.makeText(mContext, "插入历史记录成功!", Toast.LENGTH_SHORT).show();
                                queryHistory("queryall");
                            } else {
                                Toast.makeText(mContext, "插入历史记录失败!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }
    }



    public void createOfflineSearchResultPopup(Context context,String content,String solution){
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



    @Override
    public void onHiddenChanged(boolean hidden) {
        View view = getView();
        if (view != null) {
            if (!hidden) {
                if(LaunchActicity.getLoginStatus()){
                    btnHomeLogin.setText(LaunchActicity.getUserName());
                    queryHistory("queryall");
                }
                else {
                    btnHomeLogin.setText(btnLoginText);
                    listHistory.clear();
                    InitRecyclerView();
                }
            }
            view.requestApplyInsets();
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.i("111life","onstop");
    }
    @Override
    public void onResume(){
        super.onResume();
        Log.i("111life","onResume");
    }
    @Override
    public void onStart(){
        super.onStart();
        Log.i("111life","onStart");
    }
}
