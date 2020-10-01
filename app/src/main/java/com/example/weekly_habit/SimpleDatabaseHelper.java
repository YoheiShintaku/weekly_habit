package com.example.weekly_habit;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SimpleDatabaseHelper extends SQLiteOpenHelper {
    // コンストラクタ
    static String dbname = "sample.sqlite";
    static int version = 1;

    SimpleDatabaseHelper(Context context){
        super(context, dbname, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // db作成時
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

}
