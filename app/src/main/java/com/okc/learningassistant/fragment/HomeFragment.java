package com.okc.learningassistant.fragment;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.okc.learningassistant.R;
import com.okc.learningassistant.activity.LaunchActicity;
import com.okc.learningassistant.adapter.QDRecyclerViewAdapter;
import com.okc.learningassistant.camera.MyCamera;
import com.okc.learningassistant.helper.RequestCode;
import com.okc.learningassistant.helper.ToolBox;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUICollapsingTopBarLayout;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class HomeFragment extends Fragment {

    private Context mContext;
    private String DATAPATH;
    private String DEFAULT_LANGUAGE;


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
    @BindView(R.id.btn_compiler_select)
    QMUIRoundButton compilerSelect;

    private Uri GrayUri = null;
    private Uri BinaryUri = null;
    private static Uri CroppedUri = null;

    private int compilerCheckIndex = 0;
    final String[] compilerItems = new String[]{"VC6", "Pycharm", "其他"};

    private Bitmap srcBitmap;
    private Bitmap grayBitmap;
    private Bitmap binaryBitmap;

    private QDRecyclerViewAdapter mRecyclerViewAdapter;

    private LinearLayoutManager mPagerLayoutManager;

    public static void setCroppedUri(Uri uri){
        CroppedUri = uri;
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
        ButterKnife.bind(this, view);
        Log.i("location:","homefragment");
        Init();
        InitCollapseTopBar();
        InitRecyclerView();

        //设置折叠状态栏不同状态下的标题
        mCollapsingTopBarLayout.setScrimUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.i(TAG, "scrim: " + animation.getAnimatedValue());
                if(Integer.parseInt(animation.getAnimatedValue().toString())==255){
                    mCollapsingTopBarLayout.setTitleEnabled(true);
                    Log.i(TAG,"111enter:true");
                }
                else {
                    mCollapsingTopBarLayout.setTitleEnabled(false);
                    Log.i(TAG,"111enter:flase");
                }
            }
        });

        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MyCamera.class);
                Bundle bundle = new Bundle();
                startActivityForResult(intent, RequestCode.REQUEST_PHOTO);
            }
        });

        compilerSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int mCurrentDialogStyle = com.qmuiteam.qmui.R.style.QMUI_Dialog;
                new QMUIDialog.CheckableDialogBuilder(getActivity())
                        .setCheckedIndex(compilerCheckIndex)
                        .addItems(compilerItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                compilerCheckIndex = which;
                                Toast.makeText(getActivity(), "你选择了 " + compilerItems[which], Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                        .create(mCurrentDialogStyle).show();
            }
        });

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
    private static void setScaleAni(View V, float fromScale, float toScale, long ANITIME)
    {
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
        mCollapsingTopBarLayout.setContentScrimColor(getResources().getColor(R.color.qmui_config_color_white));
        mCollapsingTopBarLayout.setScrimAnimationDuration(1);
    }

    /*
    *初始化滚动界面
    * */
    private void InitRecyclerView(){
        mPagerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mPagerLayoutManager);
        mRecyclerViewAdapter = new QDRecyclerViewAdapter();
        mRecyclerViewAdapter.setItemCount(10);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RequestCode.RESULT_OK && CroppedUri != null) {
            /*
            GrayUri = null;
            try {
                srcBitmap = getSrcBitmap(CroppedUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            BinaryUri = null;
            Tuple ret = imgProcess(srcBitmap,"overall");
            BinaryUri = (Uri) ret.uri;
            binaryBitmap = (Bitmap) ret.bitmap;
            img_binary.setImageBitmap(binaryBitmap);
             */
            new imgProcessTask().execute();
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
                textView.setText(text);
                identifyTipDialog.cancel();
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(), "识别失败", Toast.LENGTH_LONG).show();
            }
        }


        @Override
        protected String doInBackground(Void... voids) {
            TessBaseAPI tessBaseAPI = new TessBaseAPI();
            Log.i("111path:",DATAPATH);
            tessBaseAPI.init(DATAPATH, DEFAULT_LANGUAGE);//参数后面有说明。
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), BinaryUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            tessBaseAPI.setImage(bitmap);
            String text = tessBaseAPI.getUTF8Text();
            Log.i("111identify",text);
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
            img_binary.setImageBitmap(binaryBitmap);
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
            Tuple ret = imgProcess(srcBitmap,"overall");
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
                Imgproc.threshold(srcMat, dstMat,0,255,Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
                break;
                //滤波
            case "blur":
                Imgproc.medianBlur(srcMat, dstMat,5);
                break;
                //提高亮度
            case "light":
                Imgproc.cvtColor(srcMat,srcMat,Imgproc.COLOR_RGB2GRAY);
                dstMat = light(srcMat);
                break;
                //膨胀
            case "dilate":
                Mat ele = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(2,2));
                Imgproc.dilate(srcMat, dstMat, ele);
                break;
            case "overall":
                //先灰度化
                Mat grayMat = new Mat();
                Imgproc.cvtColor(srcMat,grayMat, Imgproc.COLOR_RGB2GRAY);
                //灰度化后调高亮度
                Mat lightMat = new Mat();
                //lightMat = light(grayMat);
                grayMat.convertTo(lightMat, -1, 1.2, 30);
                //调高亮度后滤波
                Mat blurMat = new Mat();
                Imgproc.medianBlur(lightMat, blurMat,5);
                //Imgproc.medianBlur(grayMat, blurMat,5);
                //滤波后二值化
                Mat binaryMat = new Mat();
                Imgproc.threshold(blurMat, binaryMat,0,255,Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
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
        float alpha=1.2f;
        float bate=30f;
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

}
