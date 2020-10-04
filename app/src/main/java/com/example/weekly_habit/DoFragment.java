package com.example.weekly_habit;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

// Fragmentクラスを継承
public class DoFragment extends Fragment {
    private SimpleDatabaseHelper helper = null;
    View view;
    int viewWidth;
    int viewHeight;
    Integer recordid;

    // Fragmentで表示するViewを作成するメソッド
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String sql;
        Cursor cs;
        Integer recordCount;
        Integer n = 100;
        String[] itemidAll = new String[n];// all record
        String[] nameAll = new String[n];
        Integer[] isvalidAll = new Integer[n];
        String[] dowAll = new String[n];
        String[] dowSplited;// splited by comma
        Integer[] intervalAll = new Integer[n];
        String[] starttimeAll = new String[n];
        Integer[] timewidthAll = new Integer[n];
        String[] startdateAll = new String[n];
        Integer[] versionAll = new Integer[n];
        Integer i;
        Integer planCount = -1;
        Calendar calendar;
        Integer dowNumber;
        String week;
        Date weekDate=null;
        String date=null;
        Integer isdone = 0;
        Date DateStartDate = null;
        Calendar weekCalender = null;
        Date hoged;
        long diffInMillis;
        Integer diffInDays;
        Integer diffInWeeks;
        int MILLIS_OF_DAY = 1000 * 60 * 60 * 24;
        int textsize = 30;
        int width = viewWidth/7;
        int height = 0;
        int minuteToDp = viewHeight/1440*2;
        int minute = 0;
        int margin_left = 0;
        int margin_top = 0;
        int starttimeMinute;
        String hhmm[] = new String[2];
        String name;
        String starttime;
        Integer timewidth;
        RelativeLayout relativeLayout;
        TextView textView;

        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.fragment_do, null);
        relativeLayout = view.findViewById(R.id.relativeLayout);

        // db接続用
        helper = new SimpleDatabaseHelper(getContext());

        // 有効プラン数確認
        recordCount=0;
        sql = String.format("select count(*) from plan where isvalid = 1");
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

        if (recordCount==0){
            return view;
        }

        // 有効プラン読み取り
        // 有効プラン情報をdbから取得し配列に格納
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
        planCount = i;

        // 対象週の月曜日の日付を取得
        //// 現在の日付と曜日を取得
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        dowNumber = calendar.get(Calendar.DAY_OF_WEEK);//Sun:1, Sat:7
        //// 月曜日
        calendar.add(Calendar.DAY_OF_MONTH, (dowNumber - 2)*-1);//月曜日になるようずらす
        week = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());//該当週の月曜日の日付
        try {
            weekDate = new SimpleDateFormat("yyyyMMdd").parse(week);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // プランに関して日付単位のレコードを作成する
        // do tableのレコード数とrecordid確認
        sql = String.format("select count(*), max(recordid) from do");

        // recordidのmax取得
        Integer recordCountDo=0;
        try(SQLiteDatabase db = helper.getReadableDatabase()) {
            cs = db.rawQuery(sql, null);
            try {
                if (cs.moveToNext()) {
                    recordCountDo = cs.getInt(0);
                    recordid = cs.getInt(1);
                }
            } finally {
                cs.close();
            }
        }
        // 新しいrecordid
        if (recordCountDo > 0){
            recordid += 1;
        } else {
            recordid = 0;
        }

        // プランのループ
        for (i=0; i<planCount; i++){
            // 2回目だったらすでにあるよね。そこのチェックと分岐は？
            // 対象週に該当itemidのレコードが一つ以上あるかどうか。編集したらその週以降のレコードは消すこととすれば、それでok
            // doテーブルに対象週のレコードがあるか
            sql = String.format("select count(*) from do where itemid = '%s' and week = '%s'",
                    itemidAll[i], week);
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
            DateStartDate = null;
            weekCalender = Calendar.getInstance();
            weekCalender.setTime(weekDate);
            if (recordCount == 0){  // レコードが存在しないときだけ新たに作成処理
                // 今週は対象週か
                //// 開始日
                try{
                    DateStartDate = new SimpleDateFormat("yyyyMMdd").parse(startdateAll[i]);
                } catch (ParseException e){ }
                calendar = Calendar.getInstance();
                calendar.setTime(DateStartDate);
                //// 開始日の曜日
                dowNumber = calendar.get(Calendar.DAY_OF_WEEK);//Sun:1, Sat:7
                //// 開始日の週の月曜日の日付
                calendar.add(Calendar.DAY_OF_MONTH, (dowNumber - 2)*-1);//月曜日になるようずらす
                //// 対象集の月曜日との日数差
                diffInMillis = Math.abs(calendar.getTimeInMillis() - weekCalender.getTimeInMillis());
                diffInDays = (int)(diffInMillis / MILLIS_OF_DAY);
                diffInWeeks = diffInDays / 7;
                //// 週間隔で割り切れるか
                if ((diffInWeeks%intervalAll[i])==0){//余りがゼロ->対象週
                    // 曜日のループ
                    dowSplited = dowAll[i].split(",");
                    for (int k=0; k<dowSplited.length; k++){
                        dowNumber = Integer.parseInt(dowSplited[k]);
                        // 該当週の月曜日の日付
                        calendar = Calendar.getInstance();
                        calendar = (Calendar) weekCalender.clone();
                        // 対象曜日の日付へずらす
                        calendar.add(Calendar.DAY_OF_MONTH, dowNumber - 2);
                        // tableにinsert
                        date = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
                        sql = String.format(
                                "insert into do (recordid, itemid, version, name, starttime, timewidth, date, week, isdone) values (%d, '%s', %d, '%s', '%s', %d, '%s', '%s', %d);",
                                recordid,
                                itemidAll[i],
                                versionAll[i],
                                nameAll[i],
                                starttimeAll[i],
                                timewidthAll[i],
                                date,
                                week,
                                isdone
                        );
                        try (SQLiteDatabase db = helper.getWritableDatabase()) {
                            db.execSQL(sql);
                        }
                        recordid += 1;
                    }
                }
            }
        }// for (i=0; i<planCount; i++)

        // 日（横軸）と時間（縦軸）を描画

        // テーブルから対象週のレコードを取得 1レコードずつ処理
        sql = String.format("select * from do where week = '%s'", week);
        try(SQLiteDatabase db = helper.getReadableDatabase()) {
            cs = db.rawQuery(sql, null);
            i = 0;
            while(cs.moveToNext()){
                // get and set value
                name = cs.getString(cs.getColumnIndex("name"));
                starttime = cs.getString(cs.getColumnIndex("starttime"));
                timewidth = cs.getInt(cs.getColumnIndex("timewidth"));
                date = cs.getString(cs.getColumnIndex("date"));
                isdone = cs.getInt(cs.getColumnIndex("isdone"));

                // 描画
                // textviewを生成 isdoneにより背景色を変える nameのみ表示
                textView = new TextView(getContext());
                textView.setTextSize(textsize);
                textView.setText(name);// plan name
                textView.setId(i);
                textView.setHint(String.valueOf(isdone));
                if (isdone==0){
                    textView.setBackgroundColor(Color.LTGRAY);
                } else if (isdone==1){
                    textView.setBackgroundColor(Color.MAGENTA);
                }
                height = (int)(timewidth * minuteToDp);
                try {
                    hhmm = starttime.split(":");
                    starttimeMinute = Integer.parseInt(hhmm[0]) * 60 + Integer.parseInt(hhmm[1]) / 60;
                } catch (Exception e){
                    continue;
                } finally { }

                // 横位置を決めるために月曜日との日差分をもとめる
                Date dateDate = null;
                try{
                    dateDate = new SimpleDateFormat("yyyyMMdd").parse(date);
                } catch (ParseException e){ }
                calendar = Calendar.getInstance();
                calendar.setTime(dateDate);
                diffInMillis = calendar.getTimeInMillis() - weekCalender.getTimeInMillis();
                diffInDays = (int)(diffInMillis / MILLIS_OF_DAY);

                // 位置
                margin_top = starttimeMinute * minuteToDp;
                margin_left = diffInDays * 100;

                // クリックリスナー登録 isdoneの0/1を入れ替えてupdate、背景色を変える
                // 配列に格納 //しなくていい？edittextとレコードを紐付けられる？

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
                lp.setMargins(margin_left, margin_top, 0, 0);
                textView.setLayoutParams(lp);

                // リスナーを登録
                textView.setOnClickListener(doOnClickListener);

                relativeLayout.addView(textView);
                //textViewArray[i] = textView;

                // to next record
                i++;
            }
        } finally {
            cs.close();
        }

        // 先ほどのレイアウトをここでViewとして作成します
        return view;
    }

    View.OnClickListener doOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            //TextView textView = v.findViewById(v.getId())
            v.setBackgroundColor(Color.MAGENTA);

        }
    };

    // Viewが生成し終わった時に呼ばれるメソッド
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}