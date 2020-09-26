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
        createTables();

        // コードからFragmentを追加
        addPlanFramgent();

    }

    void createTables(){
        // テーブルがなければ作成
        String sql;

        // create plan table
        sql = String.format(
                "create table if not exists plan ("
                        + "itemid TEXT PRIMARY KEY"
                        + ", isvalid INTEGER"  // ex.: 1  // 有効or無効 編集すると無効化して新しいid発行
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

        // debug: insert test data.
        // terminalでやる
        /*
        int itemid = 0;
        int isvalid = 1;
        String name = "test";
        sql = String.format("insert into plan (itemid, isvalid, name) values ('%d', '%d', %s');", itemid, isvalid, name);
        try (SQLiteDatabase db = helper.getWritableDatabase()) {
            db.execSQL(sql);
        }
        */
    }

    public void addPlanFramgent(){
        PlanFragment fragment = new PlanFragment();// Fragmentを作成します
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();// Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
        // 新しく追加を行うのでaddを使用します // 他にも、よく使う操作で、replace removeといったメソッドがあります
        transaction.add(R.id.topFrameLayout, fragment);// 1つ目の引数は対象のViewGroupのID、2つ目の引数は追加するfragment
        transaction.commit();// 最後にcommitを使用することで変更を反映します
    }


    public void button1_onClick(View view){
        PlanFragment fragment = new PlanFragment();// Fragmentを作成します
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();// Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
        transaction.replace(R.id.topFrameLayout, fragment);// 1つ目の引数は対象のViewGroupのID、2つ目の引数は追加するfragment
        transaction.commit();// 最後にcommitを使用することで変更を反映します
    }

    public void button2_onClick(View view){
        DoFragment fragment = new DoFragment();// Fragmentを作成します
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();// Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
        // 新しく追加を行うのでaddを使用します // 他にも、よく使う操作で、replace removeといったメソッドがあります
        transaction.replace(R.id.topFrameLayout, fragment);// 1つ目の引数は対象のViewGroupのID、2つ目の引数は追加するfragment
        transaction.commit();// 最後にcommitを使用することで変更を反映します
    }
    public void button3_onClick(View view){
    }
}