package com.okc.learningassistant.camera;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.okc.learningassistant.R;
import com.okc.learningassistant.helper.RequestCode;
import com.okc.learningassistant.helper.ToolBox;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.FileCallback;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Grid;
import com.otaliastudios.cameraview.gesture.Gesture;
import com.otaliastudios.cameraview.gesture.GestureAction;
import com.otaliastudios.cameraview.size.Size;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;

import static com.okc.learningassistant.helper.RequestCode.REQUEST_CROP;


public class MyCamera extends AppCompatActivity {
    CameraView camera;
    ImageView btn_light;
    ImageView btn_album;
    ImageView btn_take;
    ImageView btn_close;
    CropImageView cropImageView;
    private File ImageFile;
    private Uri ImageUri;
    Context mContext = this;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mycamera);

        btn_album = findViewById(R.id.btn_album);
        btn_take = findViewById(R.id.btn_take);
        btn_close = findViewById(R.id.btn_close);
        btn_light = findViewById(R.id.btn_light);

        cropImageView = findViewById(R.id.cropImageView);


        ToolBox.makeStatusBarTransparent(this,false);
        camera = findViewById(R.id.mycamera);
        camera.setPlaySounds(false);
        camera.setLifecycleOwner(this);
        camera.setGrid(Grid.DRAW_3X3);
        camera.mapGesture(Gesture.PINCH, GestureAction.ZOOM);

        intent = getIntent();
        //打开相册
        btn_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlbum();
            }
        });
        //拍照
        btn_take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture();
            }
        });
        //关闭相机
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //开关闪光灯
        btn_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String flash = camera.getFlash().toString();
                switch (flash){
                    case "OFF":
                        Log.i("111flash",flash);
                        camera.setFlash(Flash.TORCH);
                        break;
                    case "TORCH":
                        Log.i("111flash",flash);
                        camera.setFlash(Flash.OFF);
                        break;
                    default:
                        Log.i("111flash","error");
                }
            }
        });
        camera.addCameraListener(new CameraListener() {
            /*
            * 旋转图标
            * */
            @Override
            public void onOrientationChanged(int orientation) {
                Log.i("111data:",String.valueOf(orientation));
                if(orientation==0 || orientation ==180 ) {
                    btn_light.animate().rotation(orientation);
                    btn_album.animate().rotation(orientation);
                    btn_take.animate().rotation(orientation);
                    btn_close.animate().rotation(orientation);
                }
                else {
                    btn_light.animate().rotation(orientation-180);
                    btn_album.animate().rotation(orientation-180);
                    btn_take.animate().rotation(orientation-180);
                    btn_close.animate().rotation(orientation-180);
                }
            }
            /*
            * 获取拍照结果
            * */
            @Override
            public void onPictureTaken(PictureResult result) {
                //获取图片uri
                try {
                    ImageFile = ToolBox.createImageFile(mContext);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ImageUri = ToolBox.createImageUri(mContext,ImageFile);
                //保存图片
                result.toFile(ImageFile, new FileCallback() {
                    @Override
                    public void onFileReady(@Nullable File file) {
                        Log.i("111file",file.toString());
                        ToolBox.saveToGallery(mContext,ImageFile,ImageUri);
                        CropImage();
                    }
                });
            }
        });
    }
    /*
    * 打开裁剪框
    * */
    protected void CropImage(){
        CropImage.setImagePath(ImageUri);
        Intent crop_intent = new Intent(MyCamera.this,CropImage.class);
        startActivityForResult(crop_intent,REQUEST_CROP);
    }
    /*
    * 打开相册选择照片
    * */
    protected void openAlbum(){
        Intent intent = new Intent();
        /* 开启Pictures画面Type设定为image */
        intent.setType("image/*");
        /* 使用Intent.ACTION_GET_CONTENT这个Action */
        intent.setAction(Intent.ACTION_GET_CONTENT);
        /* 取得相片后返回本画面 */
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.open();
        Log.i("111camera","open");
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.destroy();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RequestCode.REQUEST_FINISH) {
            setResult(RequestCode.RESULT_OK,intent);
            finish();
        }
        if(resultCode == RESULT_OK){
            ImageUri = data.getData();
            CropImage();
            //MainActivity.setCroppedUri(ImageUri);
            //finish();
        }
    }
}
