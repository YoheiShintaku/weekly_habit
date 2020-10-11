package com.example.weekly_habit;

import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// Fragmentクラスを継承
public class PlanFragment extends Fragment {
    private SimpleDatabaseHelper helper = null;
    private LinearLayout planLinearLayout;
    View view;
    Integer[] planrecordidAll;
    String[] itemidAll;
    Integer[] versionAll;
    Integer[] isvalidAll;
    String[] startdateAll;
    String[] nameAll;
    String[] dowAll;
    Integer[] intervalAll;
    String[] starttimeAll;
    Integer[] timewidthAll;

    // Fragmentで表示するViewを作成するメソッド
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Integer planCount;

        super.onCreateView(inflater, container, savedInstanceState);

        // db接続用
        helper = new SimpleDatabaseHelper(getContext());

        //ページ番号
        //int ii = getArguments().getInt("intValue");

        view = inflater.inflate(R.layout.fragment_plan, null);
        planLinearLayout = view.findViewById(R.id.planLinearLayout);

        // プラン取得
        planCount = getPlan();

        // プラン表示
        if (planCount>0){
            placePlan(view, planCount);
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

    Integer getRecordCount(String sql){
        Integer recordCount = -1;
        Cursor cs;
        try(SQLiteDatabase db = helper.getReadableDatabase()) {
            cs = db.rawQuery(sql, null);
            try {
                if (cs.moveToNext()) {
                    recordCount = cs.getInt(0);
                }
            } finally {
                cs.close();
            }
        }
        return recordCount;
    }

    // 登録済プラン取得
    Integer getPlan() {
        Integer planCount = 0;
        String sql;
        Cursor cs;
        Integer recordCount;

        // 有効プランがなければ進まない
        sql = String.format("select count(*) from plan where isvalid = 1");
        planCount = getRecordCount(sql);
        if (planCount == 0){ return 0;}

        // 有効プラン読み取り
        Integer n = 100;
        planrecordidAll = new Integer[n];// all record
        itemidAll = new String[n];// all record
        versionAll = new Integer[n];
        isvalidAll = new Integer[n];
        startdateAll = new String[n];
        nameAll = new String[n];
        dowAll = new String[n];
        intervalAll = new Integer[n];
        starttimeAll = new String[n];
        timewidthAll = new Integer[n];
        int cnt = 0;

        // 有効プラン情報をdbから取得し配列に格納
        sql = String.format("select * from plan where isvalid = 1;");
        try(SQLiteDatabase db = helper.getReadableDatabase()) {
            cs = db.rawQuery(sql, null);
            while(cs.moveToNext()){
                // get and set value
                planrecordidAll[cnt] = cs.getInt(cs.getColumnIndex("planrecordid"));
                itemidAll[cnt] = cs.getString(cs.getColumnIndex("itemid"));
                versionAll[cnt] = cs.getInt(cs.getColumnIndex("version"));
                isvalidAll[cnt] = cs.getInt(cs.getColumnIndex("isvalid"));
                startdateAll[cnt] = cs.getString(cs.getColumnIndex("startdate"));
                nameAll[cnt] = cs.getString(cs.getColumnIndex("name"));
                dowAll[cnt] = cs.getString(cs.getColumnIndex("dow"));
                intervalAll[cnt] = cs.getInt(cs.getColumnIndex("interval"));
                starttimeAll[cnt] = cs.getString(cs.getColumnIndex("starttime"));
                timewidthAll[cnt] = cs.getInt(cs.getColumnIndex("timewidth"));
                cnt++;
            }
        }
        return planCount;
    }

    void placePlan(View view, Integer planCount) {
        planLinearLayout.setGravity(1);
        LinearLayout.LayoutParams textLayoutParams;
        TextView textView;
        int textsize = 30;
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;

        for (int i = 0; i < planCount; i++) {
            textLayoutParams = new LinearLayout.LayoutParams(width, height);
            textLayoutParams.setMargins(30, 20, 30, 0);

            textView = new TextView(getContext());
            textView.setLayoutParams(textLayoutParams);
            textView.setText(nameAll[i]);// plan name
            textView.setTextSize(textsize);
            textView.setBackgroundColor(Color.LTGRAY);
            textView.setHint(String.valueOf(planrecordidAll[i]));  // クリックイベントで使う
            textView.setOnLongClickListener(planOnLongClickListener);// リスナーを登録

            planLinearLayout.addView(textView);
        }
    }

    // 追加ボタンクリックリスナー
    View.OnClickListener addPlanOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            // 新規登録画面ダイアログを表示する
            DialogFragment fragment = PlanAddDialogFragment.newInstance(-1);
            fragment.show(getFragmentManager(), "test");

            // プランリスト表示を更新
            //planLinearLayout.removeAllViews();
        }
    };

    // planテキスト長押しリスナー（編集）
    View.OnLongClickListener planOnLongClickListener = new View.OnLongClickListener(){
        @Override
        public boolean onLongClick(View v) {
            // 対応するplanrecordid取得
            TextView textView = (TextView)v;
            CharSequence hint = textView.getHint();
            Integer planrecordid = Integer.parseInt(hint.toString());

            // 編集ダイアログ（基本的には登録画面と同じ）を表示する
            DialogFragment fragment = PlanAddDialogFragment.newInstance(planrecordid);
            fragment.show(getFragmentManager(), "test");

            return true;
        }
    };
}