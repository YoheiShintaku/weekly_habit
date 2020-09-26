package com.example.weekly_habit;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
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
        planLinearLayout = view.findViewById(R.id.planLinearLayout);
        planLinearLayout.setGravity(1);
        TextView textView;
        int textsize = 30;
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        //Log.d("debug","setDate");
        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(width, height);
        textLayoutParams.setMargins(30, 20, 30, 0);
        for (int i = 0; i < 7; i++) {
            textView = new TextView(getContext());

            textView.setText(String.valueOf(i));
            textView.setTextSize(textsize);
            textView.setBackgroundColor(Color.LTGRAY);
            //textView.setGravity(gravity);  // center
            //textView.setBackground(getResources().getDrawable( R.drawable.view_frame ));
            textView.setLayoutParams(textLayoutParams);
            planLinearLayout.addView(textView);
        }

        ImageButton imageButton = new ImageButton(getContext());
        imageButton.setImageResource(R.drawable.plus);
        imageButton.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        imageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        planLinearLayout.addView(imageButton);
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