package com.okc.learningassistant.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.okc.learningassistant.R;
import com.okc.learningassistant.fragment.HomeFragment;
import com.okc.learningassistant.helper.RequestCode;
import com.okc.learningassistant.helper.ToolBox;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;

public class CropImage extends AppCompatActivity {
    private static Uri ImageUri;
    private static File CroppedFile;
    private static Uri CroppedUri;
    private CropImageView cropImageView;
    ImageView btn_confirm;
    ImageView btn_cancel;
    Context mContext = this;
    Intent intent;
    //获取图片uri
    public static void setImagePath(Uri uri){
        ImageUri = uri;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_image);
        ToolBox.makeStatusBarTransparent(this, false);
        //用cameraview辅助旋转
        CameraView rotate_help;
        rotate_help = findViewById(R.id.rotate_help);

        btn_cancel = findViewById(R.id.btn_cancel);
        btn_confirm = findViewById(R.id.btn_confirm);

        cropImageView = findViewById(R.id.cropImageView);
        cropImageView.setImageUriAsync(ImageUri); //设置图片uri


        intent = getIntent();

        /*
        * 裁剪确认
        * */
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                try {
                    CroppedFile = ToolBox.createImageFile(mContext);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                CroppedUri = ToolBox.createImageUri(mContext,CroppedFile);
                */
                //float width = cropImageView.getCropWindowRect().right - cropImageView.getCropWindowRect().left;
                //float height = cropImageView.getCropWindowRect().bottom - cropImageView.getCropWindowRect().top;
                cropImageView.getCroppedImageAsync();


            }

        });
        /*
        * 图像裁剪完毕
        * */
        cropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
            @Override
            public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
                Bitmap cropped_map = result.getBitmap();
                try {
                    CroppedFile = ToolBox.createImageFile(mContext);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                CroppedUri = ToolBox.createImageUri(mContext,CroppedFile);
                //ToolBox.saveBitmap(CroppedFile,cropped_map);
                //保存裁剪后的图片
                new saveTask().execute(cropped_map);
            }
        });
        /*
        * 取消裁剪
        * */
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /*
        * 使用cameraview自带的传感器
        * */
        rotate_help.addCameraListener(new CameraListener() {
            /*
             * 旋转图标
             * */
            @Override
            public void onOrientationChanged(int orientation) {
                Log.i("111data:", String.valueOf(orientation));
                if (orientation == 0 || orientation == 180) {
                    cropImageView.animate().rotation(orientation);
                    btn_cancel.animate().rotation(orientation);
                    btn_confirm.animate().rotation(orientation);
                } else {
                    cropImageView.animate().rotation(orientation - 180);
                    btn_cancel.animate().rotation(orientation - 180);
                    btn_confirm.animate().rotation(orientation - 180);
                }
            }
        });

    }
    class saveTask extends AsyncTask<Bitmap, Integer, String>{
        QMUITipDialog saveTipDialog;
        @Override
        protected void onPreExecute(){

            saveTipDialog = new QMUITipDialog.Builder(mContext)
                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                    .setTipWord("保存中")
                    .create();
            saveTipDialog.show();
        }

        @Override
        protected void onPostExecute(String text) {//后台任务执行完之后被调用，在ui线程执行
            saveTipDialog.cancel();
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            String status = ToolBox.saveBitmap(CroppedFile,params[0]);
            if(status == "saved"){
                ToolBox.saveToGallery(mContext,CroppedFile,CroppedUri);
                HomeFragment.setCroppedUri(CroppedUri);
                setResult(RequestCode.REQUEST_FINISH, intent);
                finish();
            }
            return null;
        }

    }

}
