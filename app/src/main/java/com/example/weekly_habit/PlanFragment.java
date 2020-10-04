package com.example.weekly_habit;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// Fragmentクラスを継承
public class PlanFragment extends Fragment {
    private SimpleDatabaseHelper helper = null;
    private LinearLayout planLinearLayout;
    View view;

    // Fragmentで表示するViewを作成するメソッド
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        // db接続用
        helper = new SimpleDatabaseHelper(getContext());

        //ページ番号
        //int ii = getArguments().getInt("intValue");

        view = inflater.inflate(R.layout.fragment_plan, null);
        planLinearLayout = view.findViewById(R.id.planLinearLayout);

        // プラン取得
        getPlan();

        // プラン表示
        if (planCount>0){
            showPlan(view);
        }

        // 追加ボタン
        ImageButton imageButton = new ImageButton(getContext());
        imageButton.setImageResource(R.drawable.plus);
        imageButton.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        imageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageButton.setOnClickListener(addPlanOnClickListener);
        planLinearLayout.addView(imageButton);

        return view;
        // 先ほどのレイアウトをここでViewとして作成します
        //return inflater.inflate(R.layout.fragment_plan, container, false);
    }

    TextView[] textViewArray;
    // planテキスト長押しリスナー
    View.OnLongClickListener planOnLongClickListener = new View.OnLongClickListener(){
        @Override
        public boolean onLongClick(View v) {

            // どのプランか判定
            int iBox = -1;
            for (int i = 0; i < textViewArray.length; i++) {
                if (textViewArray[i].hashCode() == v.hashCode()) {
                    //Toast.makeText(getContext(), string, Toast.LENGTH_LONG).show();
                    iBox = i;
                    break;
                }
            }

            // 編集ダイアログを表示する // 引数itemidを渡す必要がある
            DialogFragment newFragment = new PlanAddDialogFragment();
            newFragment.show(getFragmentManager(), "test");

            return false;
        }
    };

    // 追加ボタンクリックリスナー
    View.OnClickListener addPlanOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            // 登録画面ダイアログを表示する
            DialogFragment newFragment = new PlanAddDialogFragment();
            newFragment.show(getFragmentManager(), "test");
            // プランリスト表示を更新

            planLinearLayout.removeAllViews();
            getPlan();
            if (planCount>0){
                showPlan(view);
            }
        }
    };

    Integer[] recordidAll;
    String[] itemidAll;
    String[] nameAll;
    Integer[] isvalidAll;
    String[] dowAll;
    Integer[] intervalAll;
    String[] starttimeAll;
    Integer[] timewidthAll;
    String[] startdateAll;
    Integer[] versionAll;
    Integer planCount;
    Integer recordid;
    // 登録済プラン取得
    void getPlan() {
        planCount = 0;
        String sql;
        Cursor cs;
        // 有効プラン数確認
        Integer recordCount = 0;
        sql = String.format("select count(*) from plan where isvalid = 1");
        try (SQLiteDatabase db = helper.getReadableDatabase()) {
            cs = db.rawQuery(sql, null);
            try {
                if (cs.moveToNext()) {
                    recordCount = cs.getInt(0);
                }
            } finally {
                cs.close();
            }
        }

        if (recordCount == 0){
            return;
        }

        // recordidのmax読み取り
        sql = String.format("select max(recordid) from plan");
        try (SQLiteDatabase db = helper.getReadableDatabase()) {
            cs = db.rawQuery(sql, null);
            try {
                if (cs.moveToNext()) {
                    recordid = cs.getInt(0);
                }
            } finally {
                cs.close();
            }
        }

        // 有効プラン読み取り
        Integer n = 100;
        recordidAll = new Integer[n];// all record
        itemidAll = new String[n];// all record
        nameAll = new String[n];
        isvalidAll = new Integer[n];
        dowAll = new String[n];
        intervalAll = new Integer[n];
        starttimeAll = new String[n];
        timewidthAll = new Integer[n];
        startdateAll = new String[n];
        versionAll = new Integer[n];
        Integer i;

        // 有効プラン情報をdbから取得し配列に格納
        sql = String.format("select * from plan where isvalid = 1;");
        try(SQLiteDatabase db = helper.getReadableDatabase()) {
            cs = db.rawQuery(sql, null);
            for (i=0;i<n; i++) {
                if (cs.moveToNext()) {
                    // get and set value
                    recordidAll[i] = cs.getInt(cs.getColumnIndex("recordid"));
                    itemidAll[i] = cs.getString(cs.getColumnIndex("itemid"));
                    nameAll[i] = cs.getString(cs.getColumnIndex("name"));
                    isvalidAll[i] = cs.getInt(cs.getColumnIndex("isvalid"));
                    dowAll[i] = cs.getString(cs.getColumnIndex("dow"));
                    intervalAll[i] = cs.getInt(cs.getColumnIndex("interval"));
                    starttimeAll[i] = cs.getString(cs.getColumnIndex("starttime"));
                    timewidthAll[i] = cs.getInt(cs.getColumnIndex("timewidth"));
                    startdateAll[i] = cs.getString(cs.getColumnIndex("startdate"));
                    versionAll[i] = cs.getInt(cs.getColumnIndex("version"));
                } else {
                    break;
                }
            }
        }
        planCount = i;
    }

    void showPlan(View view) {
        planLinearLayout.setGravity(1);
        TextView textView;
        int textsize = 30;
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        //Log.d("debug","setDate");
        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(width, height);
        textLayoutParams.setMargins(30, 20, 30, 0);
        textViewArray = new TextView[planCount];
        for (int i = 0; i < planCount; i++) {
            textView = new TextView(getContext());

            textView.setText(nameAll[i]);// plan name
            textView.setTextSize(textsize);
            textView.setBackgroundColor(Color.LTGRAY);

            //textView.setGravity(gravity);  // center
            //textView.setBackground(getResources().getDrawable( R.drawable.view_frame ));
            textView.setLayoutParams(textLayoutParams);

            // リスナーを登録
            textView.setOnLongClickListener(planOnLongClickListener);

            planLinearLayout.addView(textView);
            textViewArray[i] = textView;
        }
    }

}