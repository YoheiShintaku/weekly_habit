package com.example.weekly_habit;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;

// Fragmentクラスを継承
public class PlanFragment extends Fragment {
    private SimpleDatabaseHelper helper = null;
    private LinearLayout planLinearLayout;

    // Fragmentで表示するViewを作成するメソッド
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        // db接続用
        helper = new SimpleDatabaseHelper(getContext());

        //ページ番号
        //int ii = getArguments().getInt("intValue");

        View view = inflater.inflate(R.layout.fragment_plan, null);

        // date
        setDate(view);  // calendarArray生成

        return view;
        // 先ほどのレイアウトをここでViewとして作成します
        //return inflater.inflate(R.layout.fragment_plan, container, false);
    }

    void setDate(View view) {
        TextView textView;
        int textsize = 30;
        int gravity = 1;
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        planLinearLayout = view.findViewById(R.id.planLinearLayout);
        //Log.d("debug","setDate");
        for (int i = 0; i < 7; i++) {
            textView = new TextView(getContext());

            textView.setText(String.valueOf(i));
            textView.setTextSize(textsize);
            textView.setGravity(gravity);  // center
            //textView.setBackground(getResources().getDrawable( R.drawable.view_frame ));
            textView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
            planLinearLayout.addView(textView);
        }
    }

    /*
        @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("aa","plan 2");
        int textsize = 20;
        int wc = ViewGroup.LayoutParams.WRAP_CONTENT;
        //TextView textViewArray[];
        //textViewArray = new TextView[7];
        TextView textView;
        String str = "aaaaaaaaaa";
        LinearLayout planLinearLayout = (LinearLayout)view.findViewById(R.id.planLinearLayout);
        for(int i=0; i<7 ;i++) {
            textView = new TextView(getContext());

            textView.setText("adfawefawera");
            textView.setTextSize(textsize);
            textView.setGravity(1);
            textView.setBackground(getResources().getDrawable( R.drawable.view_frame));
            textView.setLayoutParams(new ViewGroup.LayoutParams(wc, wc));

            // LinearLayoutに追加
            planLinearLayout.addView(textView, new LinearLayout.LayoutParams(0, wc,1));
            //textViewArray[i] = textView;
        }
    }
     */
}