package com.example.weekly_habit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

// Fragmentクラスを継承
public class DoFragment extends Fragment {
    private SimpleDatabaseHelper helper = null;

    // Fragmentで表示するViewを作成するメソッド
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        // db接続用
        helper = new SimpleDatabaseHelper(getContext());


        String sql;
        Cursor cs;
        // 有効プラン数確認
        Integer recordCount=0;
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
        // 有効プラン読み取り
        Integer n = 100;
        String[] itemidAll = new String[n];// all record
        String itemid;// an item
        String[] nameAll = new String[n];
        String name;
        Integer[] isvalidAll = new Integer[n];
        Integer isvalid;
        String[] dowAll = new String[n];
        String dow;
        String[] dowSplited;// splited by comma
        Integer[] intervalAll = new Integer[n];
        Integer interval;
        String[] starttimeAll = new String[n];
        String starttime;
        Integer[] timewidthAll = new Integer[n];
        Integer timewidth;
        String[] startdateAll = new String[n];
        String startdate=null;
        Integer[] versionAll = new Integer[n];
        Integer version;
        Integer i = -1;
        Integer planCount = -1;
        Calendar calendar;
        //

        if (recordCount>0){
            // 有効プラン情報をdbから取得し配列に格納
            sql = String.format("select * from plan where isvalid = 1;");
            try(SQLiteDatabase db = helper.getReadableDatabase()) {
                cs = db.rawQuery(sql, null);
                try {
                    if (cs.moveToNext()) {
                        // get and set value
                        i += 1;
                        itemidAll[i] = cs.getString(cs.getColumnIndex("itemid"));
                        nameAll[i] = cs.getString(cs.getColumnIndex("name"));
                        isvalidAll[i] = cs.getInt(cs.getColumnIndex("isvalid"));
                        dowAll[i] = cs.getString(cs.getColumnIndex("dow"));
                        intervalAll[i] = cs.getInt(cs.getColumnIndex("interval"));
                        starttimeAll[i] = cs.getString(cs.getColumnIndex("starttime"));
                        timewidthAll[i] = cs.getInt(cs.getColumnIndex("timewidth"));
                        startdateAll[i] = cs.getString(cs.getColumnIndex("startdate"));
                        versionAll[i] = cs.getInt(cs.getColumnIndex("version"));
                    }
                } finally {
                    cs.close();
                }
            }
            planCount = i + 1;

            // 対象週の月曜日の日付を取得
            //// 現在の日付と曜日を取得
            calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            Integer dowNumber = calendar.get(Calendar.DAY_OF_WEEK);//Sun:1, Sat:7
            //// 月曜日
            calendar.add(Calendar.DAY_OF_MONTH, (dowNumber - 2)*-1);//月曜日になるようずらす
            String week = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());//該当週の月曜日の日付
            Date weekDate=null;
            String date=null;
            try {
                weekDate = new SimpleDateFormat("yyyyMMdd").parse(week);
            } catch (ParseException e) {
                e.printStackTrace();
            }

                    // プランに関して日付単位のレコードを作成する
            Integer isdone = 0;
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
                Date DateStartDate=null;
                Date hoged;
                Calendar weekCalender = Calendar.getInstance();
                weekCalender.setTime(weekDate);
                long diffInMillis;
                Integer diffInDays;
                Integer diffInWeeks;
                int MILLIS_OF_DAY = 1000 * 60 * 60 * 24;
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
                                    "insert into do (itemid, version, name, starttime, timewidth, date, week, isdone) values ('%s', %d, '%s', '%s', %d, '%s', '%s', %d);",
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
                        }
                    }
                }
            }
        }

        // 先ほどのレイアウトをここでViewとして作成します
        return inflater.inflate(R.layout.fragment_do, container, false);
    }

    // Viewが生成し終わった時に呼ばれるメソッド
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}