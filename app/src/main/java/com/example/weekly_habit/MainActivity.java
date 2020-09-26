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

/*
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // レイアウト読み込み
        setContentView(R.layout.activity_main);
        //View view = findViewById(R.id.linearLayout);
        //view.setClickable(true);

        // コードからFragmentを追加
        addPlanFramgent();

    }
    public void addPlanFramgent(){
        PlanFragment fragment = new PlanFragment();// Fragmentを作成します
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();// Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
        // 新しく追加を行うのでaddを使用します // 他にも、よく使う操作で、replace removeといったメソッドがあります
        transaction.add(R.id.mainLinearLayout, fragment);// 1つ目の引数は対象のViewGroupのID、2つ目の引数は追加するfragment
        transaction.commit();// 最後にcommitを使用することで変更を反映します
    }

    public void button1_onClick(View view){
        //Log.d("event","tab1_onClick");
        Toast.makeText(this, "plan",Toast.LENGTH_SHORT).show();

        PlanFragment fragment = new PlanFragment();// Fragmentを作成します
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();// Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
        transaction.replace(R.id.mainLinearLayout, fragment);// 1つ目の引数は対象のViewGroupのID、2つ目の引数は追加するfragment
        transaction.commit();// 最後にcommitを使用することで変更を反映します
    }

    public void button2_onClick(View view){
        //Log.d("event","tab1_onClick");
        Toast.makeText(this, "do",Toast.LENGTH_SHORT).show();

        DoFragment fragment = new DoFragment();// Fragmentを作成します
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();// Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
        // 新しく追加を行うのでaddを使用します // 他にも、よく使う操作で、replace removeといったメソッドがあります
        transaction.replace(R.id.mainLinearLayout, fragment);// 1つ目の引数は対象のViewGroupのID、2つ目の引数は追加するfragment
        transaction.commit();// 最後にcommitを使用することで変更を反映します
    }
    public void button3_onClick(View view){
        //Log.d("event","tab1_onClick");
        Toast.makeText(this, "check",Toast.LENGTH_SHORT).show();
    }
}