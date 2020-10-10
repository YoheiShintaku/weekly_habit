package com.example.weekly_habit;

import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.TextView;
import android.support.constraint.Guideline;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

// Fragmentクラスを継承
public class DoFragment extends Fragment {
    private SimpleDatabaseHelper helper = null;
    int MILLIS_OF_DAY = 1000 * 60 * 60 * 24;
    View view;
    int viewWidth;
    int viewHeight;
    String stringWeekStart;
    Calendar calendarWeekStart;
    ConstraintLayout constraintLayoutDo;
    Guideline guidelineV;
    Guideline guidelineH;

    String[] itemidAll;// all record
    String[] nameAll;
    Integer[] isvalidAll;
    String[] dowAll;
    String[] dowSplited;// splited by comma
    Integer[] intervalAll;
    String[] starttimeAll;
    Integer[] timewidthAll;
    String[] startdateAll;
    Integer[] versionAll;

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

    Integer getPlans(){
        // 有効プラン情報をdbから取得し配列に格納
        int n = 100;
        itemidAll = new String[n];// all record
        nameAll = new String[n];
        isvalidAll = new Integer[n];
        dowAll = new String[n];
        intervalAll = new Integer[n];
        starttimeAll = new String[n];
        timewidthAll = new Integer[n];
        startdateAll = new String[n];
        versionAll = new Integer[n];
        Cursor cs;
        Integer i;
        String sql;
        int recordCount=-1;

        // planの有効レコード数確認
        sql = String.format("select count(*) from plan where isvalid = 1");
        recordCount = getRecordCount(sql);

        // 存在しなければ-1を返す
        if (recordCount==0){
            return -1;
        }

        // プラン内容取得し、配列に格納
        sql = String.format("select * from plan where isvalid = 1;");
        try(SQLiteDatabase db = helper.getReadableDatabase()) {
            cs = db.rawQuery(sql, null);
            for (i=0;i<n; i++) {
                if (cs.moveToNext()) {
                    // get and set value
                    itemidAll[i] = cs.getString(cs.getColumnIndex("itemid"));
                    nameAll[i] = cs.getString(cs.getColumnIndex("name"));
                    isvalidAll[i] = cs.getInt(cs.getColumnIndex("isvalid"));
                    dowAll[i] = cs.getString(cs.getColumnIndex("dow"));
                    intervalAll[i] = cs.getInt(cs.getColumnIndex("interval"));
                    starttimeAll[i] = cs.getString(cs.getColumnIndex("starttime"));
                    timewidthAll[i] = cs.getInt(cs.getColumnIndex("timewidth"));
                    startdateAll[i] = cs.getString(cs.getColumnIndex("startdate"));
                    versionAll[i] = cs.getInt(cs.getColumnIndex("version"));
                } else{
                    break;
                }
            }
        }
        // プラン数を返す
        Integer planCount = i;
        return planCount;
    }


    Integer getNewRecordId(){
        Integer recordId = -1;
        Cursor cs;
        String sql;

        // do tableのレコード数とrecordid確認
        sql = String.format("select count(*), max(recordid) from do");

        // recordidのmax取得
        Integer recordCountDo=0;
        try(SQLiteDatabase db = helper.getReadableDatabase()) {
            cs = db.rawQuery(sql, null);
            try {
                if (cs.moveToNext()) {
                    recordCountDo = cs.getInt(0);
                    recordId = cs.getInt(1);
                }
            } finally {
                cs.close();
            }
        }
        // 新しいrecordid
        if (recordCountDo > 0){
            recordId += 1;
        } else {
            recordId = 0;
        }
        return recordId;
    }

    Calendar string2Calendar(String str){
        Date DateStartDate=null;
        Calendar calendar;
        try{
            DateStartDate = new SimpleDateFormat("yyyyMMdd").parse(str);
        } catch (ParseException e){ }
        calendar = Calendar.getInstance();
        calendar.setTime(DateStartDate);
        return calendar;
    }

    // 週間隔にマッチする週か判定
    boolean judgeWeek(Integer i){
        Calendar calendar;
        Integer dowNumber;

        // 開始日（登録日）の週の月曜日のcalendarを取得
        calendar = string2Calendar(startdateAll[i]);
        dowNumber = calendar.get(Calendar.DAY_OF_WEEK);//曜日//Sun:1, Sat:7
        calendar.add(Calendar.DAY_OF_MONTH, (dowNumber - 2)*-1);//その週の月曜日の日付に

        // 処理対象週の月曜日との週間隔が、設定した週間隔で割り切れるか
        long diffInMillis = Math.abs(calendar.getTimeInMillis() - calendarWeekStart.getTimeInMillis());
        Integer diffInDays = (int)(diffInMillis / MILLIS_OF_DAY);
        Integer diffInWeeks = diffInDays / 7;
        return ((diffInWeeks%intervalAll[i])>0);
    }

    void makeDoRecords(Integer planCount){
        Integer recordid = -1;
        Integer recordCount;
        Calendar calendar;
        String sql;
        Cursor cs;
        String date;
        Integer isdone = 0;

        // maxの次のrecordidとなる整数を取得
        recordid = getNewRecordId();

        // 現在週の月曜日をCalendar型で取得
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Integer dowNumber = calendar.get(Calendar.DAY_OF_WEEK);//Sun:1, Sat:7
        calendar.add(Calendar.DAY_OF_MONTH, (dowNumber - 2)*-1);
        stringWeekStart = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());//0時にするためにわざわざやってる？
        calendarWeekStart = string2Calendar(stringWeekStart);

        // プランのループ
        for (int i=0; i<planCount; i++){
            // 2回目だったらすでにあるよね。そこのチェックと分岐は？
            // 対象週に該当itemidのレコードが一つ以上あるかどうか。編集したらその週以降のレコードは消すこととすれば、それでok

            // 対象週でなければskip
            if (judgeWeek(i)){continue;}

            // すでにレコードが存在すればskip
            sql = String.format("select count(*) from do where itemid = '%s' and week = '%s'",
                    itemidAll[i], stringWeekStart);
            recordCount = getRecordCount(sql);
            if (recordCount > 0){continue;}

            // 曜日のループ処理でレコードを作成していく
            dowSplited = dowAll[i].split(",");
            for (int k=0; k<dowSplited.length; k++){
                // 曜日を日付の文字列に
                calendar = (Calendar) calendarWeekStart.clone();//月曜日
                dowNumber = Integer.parseInt(dowSplited[k]);
                calendar.add(Calendar.DAY_OF_MONTH, dowNumber - 2);//曜日分ずらす
                date = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());

                // tableにinsert
                sql = String.format(
                        "insert into do (recordid, itemid, version, name, starttime, timewidth, date, week, isdone) values (%d, '%s', %d, '%s', '%s', %d, '%s', '%s', %d);",
                        recordid,
                        itemidAll[i],
                        versionAll[i],
                        nameAll[i],
                        starttimeAll[i],
                        timewidthAll[i],
                        date,
                        stringWeekStart,
                        isdone
                );
                try (SQLiteDatabase db = helper.getWritableDatabase()) {
                    db.execSQL(sql);
                }
                recordid += 1;
            }
        }
    }

    void showTimeSchedule(){
        TextView textView;
        ConstraintLayout.LayoutParams layoutParams;
        float widthPercent=(float)1/7;
        float startx;
        int minuteToDp = 3;
        int textsize;
        Calendar calendar;
        String string;
        String dowString=null;
        textsize = 10;

        // ヘッダ部: 日（横軸）
        for (int j=0; j<7; j++){
            // 日付文字列を生成
            calendar = (Calendar) calendarWeekStart.clone();
            calendar.add(Calendar.DAY_OF_MONTH, j);
            string = new SimpleDateFormat("M/d").format(calendar.getTime());
            int dow = calendar.get(Calendar.DAY_OF_WEEK);
            switch (calendar.get(Calendar.DAY_OF_WEEK)){
                case 1:
                    dowString = "(日)";
                    break;
                case 2:
                    dowString = "(月)";
                    break;
                case 3:
                    dowString = "(火)";
                    break;
                case 4:
                    dowString = "(水)";
                    break;
                case 5:
                    dowString = "(木)";
                    break;
                case 6:
                    dowString = "(金)";
                    break;
                case 7:
                    dowString = "(土)";
                    break;
            }
            string += dowString;

            // 表示位置とサイズ
            layoutParams = new ConstraintLayout.LayoutParams(0, 0);
            layoutParams.startToStart = R.id.guidelineV;
            layoutParams.endToEnd = R.id.constraintLayoutDo;
            layoutParams.topToTop = R.id.constraintLayoutDo;
            layoutParams.bottomToTop = R.id.guidelineH;
            layoutParams.matchConstraintPercentWidth = widthPercent;
            startx = (float)1/7*j;
            layoutParams.horizontalBias = startx / (1 - widthPercent);
            layoutParams.setMargins(0, 0, 0, 0);

            textView = new TextView(getContext());
            textView.setTextSize(textsize);
            textView.setText(string);
            textView.setLayoutParams(layoutParams);

            constraintLayoutDo.addView(textView);
        }

        // ヘッダ部：時間（縦軸）
        for (int j=0; j<24; j++){
            layoutParams = new ConstraintLayout.LayoutParams(0, 60 * minuteToDp);
            layoutParams.startToStart = R.id.constraintLayoutDo;
            layoutParams.endToStart = R.id.guidelineV;
            layoutParams.topToTop = R.id.guidelineH;
            layoutParams.matchConstraintPercentWidth = (float)1.0;
            layoutParams.setMargins(0, 0, 0, 0);;
            layoutParams.topMargin = 60 * minuteToDp * j;

            textView = new TextView(getContext());
            textView.setTextSize(textsize);
            //textView.setPadding(0,0,0,0);
            textView.setText(String.valueOf(j)+":00");
            textView.setLayoutParams(layoutParams);

            constraintLayoutDo.addView(textView);
        }

        // テーブルから対象週のレコードを取得 1レコードずつ処理
        int cnt;
        String sql;
        Cursor cs=null;
        long recordid;
        String name;
        String starttime;
        Integer timewidth;
        String date;
        Integer isdone;
        String[] hhmm;
        Integer starttimeMinute;
        long diffInMillis;
        Integer diffInDays;
        Integer margin_top;
        Integer margin_left;
        Integer margin_right;
        Integer margin_bottom;
        Integer height;

        cnt=0;
        sql = String.format("select * from do where week = '%s'", stringWeekStart);
        try(SQLiteDatabase db = helper.getReadableDatabase()) {
            cs = db.rawQuery(sql, null);
            while(cs.moveToNext()){
                // get and set value
                recordid = cs.getInt(cs.getColumnIndex("recordid"));
                name = cs.getString(cs.getColumnIndex("name"));
                starttime = cs.getString(cs.getColumnIndex("starttime"));
                timewidth = cs.getInt(cs.getColumnIndex("timewidth"));
                date = cs.getString(cs.getColumnIndex("date"));
                isdone = cs.getInt(cs.getColumnIndex("isdone"));

                // 開始時刻を分に変換
                try {
                    hhmm = starttime.split(":");
                    starttimeMinute = Integer.parseInt(hhmm[0]) * 60 + Integer.parseInt(hhmm[1]) / 60;
                } catch (Exception e){ continue; }

                // 月曜日との日数差から横位置を決める
                calendar = string2Calendar(date);
                diffInMillis = calendar.getTimeInMillis() - calendarWeekStart.getTimeInMillis();
                diffInDays = (int)(diffInMillis / MILLIS_OF_DAY);
                startx = (float)1/7*diffInDays;

                layoutParams = new ConstraintLayout.LayoutParams(0, (int)(timewidth * minuteToDp));
                layoutParams.endToEnd = R.id.constraintLayoutDo;
                layoutParams.topToTop = R.id.guidelineH;
                layoutParams.startToStart = R.id.guidelineV;
                //widthの残りが移動できる範囲で、それをbiasで決める
                //widthが決まっていて、ある開始点startxに置きたい場合、
                layoutParams.topMargin = starttimeMinute * minuteToDp;
                layoutParams.matchConstraintPercentWidth = widthPercent;
                layoutParams.horizontalBias = startx / (1 - widthPercent);

                textView = new TextView(getContext());
                textView.setLayoutParams(layoutParams);
                textView.setTextSize(textsize);
                textView.setText(name);// plan name
                //textView.setId(i);
                textView.setHint(String.valueOf(recordid));  // クリックイベントで使う
                if (isdone==0){
                    textView.setBackgroundColor(Color.LTGRAY);
                } else if (isdone==1){
                    textView.setBackgroundColor(Color.MAGENTA);
                }
                textView.setOnClickListener(doOnClickListener);// リスナーを登録

                constraintLayoutDo.addView(textView);

                // to next record
                cnt++;
            }
        } finally {
            cs.close();
        }
    }

    // Fragmentで表示するViewを作成するメソッド
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Integer planCount = -1;

        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.fragment_do, null);
        constraintLayoutDo = view.findViewById(R.id.constraintLayoutDo);
        guidelineV = view.findViewById(R.id.guidelineV);
        guidelineH = view.findViewById(R.id.guidelineH);

        // db接続用
        helper = new SimpleDatabaseHelper(getContext());

        // 有効プラン読み取り
        planCount = getPlans();
        if (planCount==1){
            return view;//ここで処理終了
        }

        // 今週のdoレコード生成
        makeDoRecords(planCount);

        // 表示
        showTimeSchedule();

        // 先ほどのレイアウトをここでViewとして作成します
        return view;
    }

    View.OnClickListener doOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            TextView textView = (TextView)v;
            CharSequence hint = textView.getHint();
            Integer id = Integer.parseInt(hint.toString());
            Integer isdone = 0;
            String sql = String.format("select * from do where recordid = %d", id);

            // 現在のisdoneを取得する
            try(SQLiteDatabase db = helper.getReadableDatabase()) {
                Cursor cursor = db.rawQuery(sql, null);
                if(cursor.moveToNext()){
                    isdone = cursor.getInt(cursor.getColumnIndex("isdone"));
                }
            }
            // 反転させる
            if (isdone==0){
                isdone = 1;
            } else if (isdone==1){
                isdone = 0;
            }

            // dbをupdate
            sql = String.format("update do set isdone = %d where recordid = %d", isdone, id);
            try(SQLiteDatabase db = helper.getWritableDatabase()){ db.execSQL(sql); }

            // isdoneに応じて色を変える
            if (isdone==0){
                v.setBackgroundColor(Color.LTGRAY);
            } else if (isdone==1){
                v.setBackgroundColor(Color.MAGENTA);
            }
        }
    };

    // Viewが生成し終わった時に呼ばれるメソッド
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}