package com.okc.learningassistant.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorDBHelper {
    private SQLiteDatabase database;
    private Context context;
    //数据库文件路径
    private static String DB_PATH = null;
    //表名
    private static final String TABLE_NAME = "data";
    public ErrorDBHelper(Context context, String PATH) {
        this.context = context;
        DB_PATH = PATH;
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    public SQLiteDatabase openDatabase() {
        this.database = this.openDatabase(DB_PATH);
        return database;
    }

    private SQLiteDatabase openDatabase(String PATH) {

        if (!(new File(PATH).exists())) {
            Log.i("111DBError","File not found");
            return null;
        }
        else {
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(PATH, null);
            return db;
        }
    }

    public void closeDatabase() {
        this.database.close();
    }

    public Map<String,String> queryCode(String code){
        String sql = "select * from " + TABLE_NAME + " where code = '" + code + "' COLLATE NOCASE";
        Log.i("111SQL",sql);
        Map<String,String> result = new HashMap<String,String>();
        Cursor cursor = this.database.rawQuery(sql,null);
        Log.i("111cursor",cursor.toString());
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            result.put("code",cursor.getString(0));
            result.put("content",cursor.getString(1));
            result.put("solution",cursor.getString(2));
            return result;
        }
        else return null;
    }

}
