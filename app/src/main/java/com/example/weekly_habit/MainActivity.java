package com.example.weekly_habit;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.SimpleDateFormat;

/*
 */

public class MainActivity extends AppCompatActivity {
    private SimpleDatabaseHelper helper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // レイアウト読み込み
        setContentView(R.layout.activity_main);
        //View view = findViewById(R.id.linearLayout);
        //view.setClickable(true);

        // テーブル作成（存在しない場合）
        helper = new SimpleDatabaseHelper(this);// db接続用
        //dropTables();//応急対応
        createTables();

        // コードからFragmentを追加
        PlanFragment fragment = new PlanFragment();// Fragmentを作成します
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();// Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
        transaction.add(R.id.topFrameLayout, fragment);// 初回はadd
        transaction.commit();
    }


    void createTables(){
        // テーブルがなければ作成
        String sql;

        // create plan table
        sql = String.format(
                "create table if not exists plan ("
                        + "planrecordid INTEGER PRIMARY KEY"
                        + ", itemid INTEGER"
                        + ", version INTEGER"  // ex.: 1
                        + ", isvalid INTEGER"  // ex.: 1  // 有効or無効 編集すると無効化して新しいid発行
                        + ", startdate TEXT"  // 登録日を開始日とする
                        + ", name TEXT"  // ex.: "walking"
                        + ", dow TEXT"  // ex.: "1,3,5"
                        + ", interval INTEGER"  // ex.: 1  // 週間隔
                        + ", starttime TEXT"  // ex.: "10:30"
                        + ", timewidth INTEGER"  // ex.: 15 // minute
                        + ")"
        );
        try(SQLiteDatabase db = helper.getWritableDatabase()) {
            db.execSQL(sql);
        }

        // create do table
        // record id を付与したい
        sql = String.format(
                "create table if not exists do ("
                        + "dorecordid INTEGER PRIMARY KEY"
                        + ", isvalid INTEGER"
                        + ", isdone INTEGER"
                        + ", date TEXT"
                        + ", week TEXT"
                        + ", planrecordid INTEGER"//ここ以下はplanからコピーした情報
                        + ", itemid INTEGER"
                        + ", name TEXT"
                        + ", starttime TEXT"
                        + ", timewidth INTEGER"
                        + ")"
        );
        try(SQLiteDatabase db = helper.getWritableDatabase()) {
            db.execSQL(sql);
        }
    }

    void dropTables(){
        // 余計なテーブルがあって落ちる時の応急対応
        String sql=null;
        sql=String.format("drop table plan");
        try(SQLiteDatabase db = helper.getWritableDatabase()) {
            db.execSQL(sql);
        }
        sql=String.format("drop table do");
        try(SQLiteDatabase db = helper.getWritableDatabase()) {
            db.execSQL(sql);
        }
    }

    public void replacePlanFramgent(){
        PlanFragment fragment = new PlanFragment();// Fragmentを作成します
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();// Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
        transaction.replace(R.id.topFrameLayout, fragment);// add
        transaction.commit();
    }

    // plan
    public void button1_onClick(View view){
        replacePlanFramgent();
    }

    // do
    public void button2_onClick(View view){
        DoFragment fragment = new DoFragment();// Fragmentを作成します
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();// Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
        // 新しく追加を行うのでaddを使用します // 他にも、よく使う操作で、replace removeといったメソッドがあります
        transaction.replace(R.id.topFrameLayout, fragment);// 1つ目の引数は対象のViewGroupのID、2つ目の引数は追加するfragment
        transaction.commit();// 最後にcommitを使用することで変更を反映します
    }

    // check
    public void button3_onClick(View view){
        CheckFragment fragment = new CheckFragment();// Fragmentを作成します
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();// Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
        // 新しく追加を行うのでaddを使用します // 他にも、よく使う操作で、replace removeといったメソッドがあります
        transaction.replace(R.id.topFrameLayout, fragment);// 1つ目の引数は対象のViewGroupのID、2つ目の引数は追加するfragment
        transaction.commit();// 最後にcommitを使用することで変更を反映します
    }
}