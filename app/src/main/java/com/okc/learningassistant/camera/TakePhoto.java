package com.okc.learningassistant.camera;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

import com.okc.learningassistant.R;
import com.okc.learningassistant.helper.RequestCode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TakePhoto extends AppCompatActivity {

    private File ImageFile;
    private Uri ImageUri;
    public ImageView ivPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_photo);

        //ivCamera = findViewById(R.id.ivCamera);
        ivPhoto = findViewById(R.id.ivPhoto);
        Intent intent=getIntent();
        takePhoto();
        Bundle bundle = new Bundle();
        bundle.putString("uri",ImageUri.toString());
        intent.putExtras(bundle);
        setResult(RequestCode.RETURN_PHOTO, intent);
    }



    /**
     * 拍照
     */
    private void takePhoto() {

        try {
            ImageFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ImageFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                ImageUri = FileProvider.getUriForFile(this, "com.okc.learningassistant.fileprovider", ImageFile);
            } else {
                ImageUri = Uri.fromFile(ImageFile);
            }
        }
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (ImageUri != null) {
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUri);
            captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(captureIntent, RequestCode.CAMERA_REQUEST_CODE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //
        if (requestCode == RequestCode.CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    MediaStore.Images.Media.insertImage(this.getContentResolver(),
                            ImageFile.getAbsolutePath(), ImageFile.getName(), null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                // 最后通知图库更新
                Log.i("uri:",ImageUri.toString());
                this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, ImageUri));
                Toast.makeText(this,"保存",Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this,"取消",Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /**
     * 创建保存图片的文件
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date())+".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageName);
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        return tempFile;
    }
}
