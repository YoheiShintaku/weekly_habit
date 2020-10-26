package com.example.weekly_habit;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.constraint.Guideline;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

// Fragmentクラスを継承
public class DoFragment extends Fragment {
    private SimpleDatabaseHelper helper = null;
    Common common;
    int MILLIS_OF_DAY = 1000 * 60 * 60 * 24;
    View view;
    String stringWeekStart;
    Calendar calendarWeekStart;
    ConstraintLayout constraintLayoutDo;
    ConstraintLayout doTop;
    Guideline guidelineV;
    Guideline guidelineH;
    ScrollView scrollViewDo;

    Integer[] planrecordidAll;//
    Integer[] itemidAll;//
    Integer[] isvalidAll;
    Integer[] versionAll;
    String[] nameAll;
    String[] dowAll;
    String[] dowSplited;// splited by comma
    Integer[] intervalAll;
    String[] starttimeAll;
    Integer[] timewidthAll;
    String[] startdateAll;
    GestureDetector mDetector;
    Integer add_day_by_swipe;
    ConstraintLayout constraintLayoutHead;
    // ずらず日を持つインスタンス
    public static DoFragment newInstance(Integer i) {
        DoFragment fragment = new DoFragment();
        Bundle b = new Bundle();
        b.putInt("add_day_by_swipe", i);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        add_day_by_swipe = getArguments().getInt("add_day_by_swipe");
        Integer planCount = -1;

        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.fragment_do, null);
        doTop = view.findViewById(R.id.doTop);
        constraintLayoutDo = view.findViewById(R.id.constraintLayoutDo);
        guidelineV = view.findViewById(R.id.guidelineV);
        guidelineH = view.findViewById(R.id.guidelineH);
        constraintLayoutHead = view.findViewById(R.id.constraintLayoutHead);
        scrollViewDo = view.findViewById(R.id.ScrollViewDo);

        // db接続用
        helper = new SimpleDatabaseHelper(getContext());

        // 共通関数クラス
        common = Common.newInstance();
        common.setHelper(helper);

        // swipe検知用
        mDetector = new GestureDetector(getContext(), new MyGestureListener());
        constraintLayoutDo.setOnTouchListener(touchListener);

        // 有効プラン読み取り
        planCount = getPlans();
        if (planCount==0){
            Toast.makeText(getContext(),"planを作成してください",Toast.LENGTH_SHORT).show();
            return view;//ここで処理終了
        }

        // 今週のdoレコード生成
        makeDoRecords(planCount);

        // 表示
        showTimeSchedule();

        // 先ほどのレイアウトをここでViewとして作成します
        return view;
    }

    Integer getPlans(){
        // 有効プラン情報をdbから取得し配列に格納
        int n = 100;
        planrecordidAll = new Integer[n];
        itemidAll = new Integer[n];
        isvalidAll = new Integer[n];
        versionAll = new Integer[n];
        nameAll = new String[n];
        dowAll = new String[n];
        intervalAll = new Integer[n];
        starttimeAll = new String[n];
        timewidthAll = new Integer[n];
        startdateAll = new String[n];
        Cursor cs;
        String sql;
        Integer planCount=0;

        // planの有効レコード数確認
        sql = String.format("select count(*) from plan where isvalid = 1");
        planCount = common.getRecordCount(sql);

        // 存在しなければここで終わり
        if (planCount==0){
            return planCount;
        }

        // プラン内容取得し、配列に格納
        sql = String.format("select * from plan where isvalid = 1;");
        try(SQLiteDatabase db = helper.getReadableDatabase()) {
            cs = db.rawQuery(sql, null);
            for (int i=0;i<n; i++) {
                if (cs.moveToNext()) {
                    // get and set value
                    planrecordidAll[i] = cs.getInt(cs.getColumnIndex("planrecordid"));
                    itemidAll[i] = cs.getInt(cs.getColumnIndex("itemid"));
                    isvalidAll[i] = cs.getInt(cs.getColumnIndex("isvalid"));
                    versionAll[i] = cs.getInt(cs.getColumnIndex("version"));
                    nameAll[i] = cs.getString(cs.getColumnIndex("name"));
                    dowAll[i] = cs.getString(cs.getColumnIndex("dow"));
                    intervalAll[i] = cs.getInt(cs.getColumnIndex("interval"));
                    starttimeAll[i] = cs.getString(cs.getColumnIndex("starttime"));
                    timewidthAll[i] = cs.getInt(cs.getColumnIndex("timewidth"));
                    startdateAll[i] = cs.getString(cs.getColumnIndex("startdate"));
                } else{
                    break;
                }
            }
        }
        // プラン数を返す
        return planCount;
    }
    String stringWeekNow;
    Calendar calendarWeekNow;
    void makeDoRecords(Integer planCount){
        Integer dorecordid = -1;
        Integer recordCount;
        Calendar calendar;
        String sql;
        Cursor cs;
        String date;
        Integer isdone;
        Integer isvalid;

        // maxの次のrecordidとなる整数を取得
        dorecordid = getNewRecordId();

        // 直近の土曜日をCalendar型で取得
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Integer dowNumber = calendar.get(Calendar.DAY_OF_WEEK);//Sun:1, Sat:7
        if (dowNumber==7){dowNumber=0;}//0:Sat
        calendar.add(Calendar.DAY_OF_MONTH, dowNumber*-1);

        // 現在週
        stringWeekNow = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
        calendarWeekNow = string2Calendar(stringWeekNow);

        // 表示対象　swipe検知によりずらす
        calendar.add(Calendar.DAY_OF_MONTH, add_day_by_swipe);

        stringWeekStart = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
        calendarWeekStart = string2Calendar(stringWeekStart);

        // プランのループ
        for (int i=0; i<planCount; i++){
            // 過去ならskip
            float diffMillis = calendarWeekStart.getTimeInMillis() - calendarWeekNow.getTimeInMillis();
            if (diffMillis<0){continue;}

            // すでにレコード作成済（初回表示でない）ならskip
            sql = String.format("select count(*) from do where " +
                            "isvalid=1 and " +  // 有効
                            "week='%s' and " +  // この週
                            "planrecordid='%s'",// 該当プランレコード
                    stringWeekStart,
                    planrecordidAll[i]);
            recordCount = common.getRecordCount(sql);
            if (recordCount > 0){continue;}

            // 対象週でなければskip
            if (judgeWeek(startdateAll[i],intervalAll[i])){continue;}

            // 曜日のループ処理でレコードを追加していく
            dowSplited = dowAll[i].split(",");
            for (int k=0; k<dowSplited.length; k++){
                // 該当曜日を日付に（土曜始まり）
                calendar = (Calendar) calendarWeekStart.clone();
                dowNumber = Integer.parseInt(dowSplited[k]);
                if (dowNumber==7){dowNumber=0;}//0:Sat
                calendar.add(Calendar.DAY_OF_MONTH, dowNumber);
                date = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
                isvalid = 1;
                isdone = 0;

                // tableにinsert
                sql = String.format(
                        "insert into do (" +
                                "dorecordid, isvalid, isdone, date, week, " +
                                "planrecordid, itemid, name, starttime, timewidth) values (" +
                                "%d, %d, %d, '%s', '%s'," +
                                "%d, %d, '%s', '%s', %d);",
                        dorecordid, isvalid, isdone, date, stringWeekStart,
                        planrecordidAll[i], itemidAll[i], nameAll[i], starttimeAll[i], timewidthAll[i]);
                try (SQLiteDatabase db = helper.getWritableDatabase()) {
                    db.execSQL(sql);
                }
                dorecordid += 1;
            }
        }
    }

    Integer getNewRecordId(){
        Integer dorecordid = -1;
        Cursor cs;
        String sql;

        // do tableのレコード数とrecordid確認
        sql = String.format("select count(*), max(dorecordid) from do");

        // dorecordidのmax取得
        Integer recordCountDo=0;
        try(SQLiteDatabase db = helper.getReadableDatabase()) {
            cs = db.rawQuery(sql, null);
            try {
                if (cs.moveToNext()) {
                    recordCountDo = cs.getInt(0);
                    dorecordid = cs.getInt(1);
                }
            } finally {
                cs.close();
            }
        }
        // 新しいdorecordid
        if (recordCountDo > 0){
            dorecordid += 1;
        } else {
            dorecordid = 0;
        }
        return dorecordid;
    }

    // 週間隔にマッチする週か判定
    boolean judgeWeek(String startdate, Integer interval){
        Calendar calendar;
        Integer dowNumber;

        // 開始日（登録日）の直近土曜日のcalendarを取得
        calendar = string2Calendar(startdate);
        dowNumber = calendar.get(Calendar.DAY_OF_WEEK);//曜日//Sun:1, Sat:7
        if (dowNumber==7){dowNumber=0;}//0:Sat
        calendar.add(Calendar.DAY_OF_MONTH, (dowNumber)*-1);

        // 処理対象週の月曜日との週間隔が、設定した週間隔で割り切れるか
        long diffInMillis = Math.abs(calendar.getTimeInMillis() - calendarWeekStart.getTimeInMillis());
        Integer diffInDays = (int)(diffInMillis / MILLIS_OF_DAY);
        Integer diffInWeeks = diffInDays / 7;
        return ((diffInWeeks%interval)>0);
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



    void showTimeSchedule(){
        showDate();

        showTime();

        showItem();
    }

    void showDate(){
        TextView textView;
        ConstraintLayout.LayoutParams layoutParams;
        float guidelineWidthPercent = (float)0.1;
        float widthPercent=(float)(1-guidelineWidthPercent)/(float)7.5;
        Calendar calendar;
        float startx;
        int textsize=10;
        String string;

        // ヘッダ部: 日（横軸）
        String[] dayOfWeek = new String[]{"土","日","月","火","水","木","金"};
        for (int j=0; j<7; j++){
            // 日付文字列を生成
            calendar = (Calendar) calendarWeekStart.clone();
            calendar.add(Calendar.DAY_OF_MONTH, j);
            string = new SimpleDateFormat("M/d").format(calendar.getTime());
            string += "\n" + dayOfWeek[j];

            // 表示位置とサイズ
            layoutParams = new ConstraintLayout.LayoutParams(0, 0);
            layoutParams.startToStart = R.id.constraintLayoutHead;
            layoutParams.endToEnd = R.id.constraintLayoutHead;
            layoutParams.topToTop = R.id.constraintLayoutHead;
            layoutParams.bottomToBottom = R.id.constraintLayoutHead;
            layoutParams.matchConstraintPercentWidth = widthPercent;
            startx = (float)1/7*j;
            layoutParams.horizontalBias = startx / (1 - widthPercent);
            layoutParams.setMargins(0, 0, 0, 0);

            textView = new TextView(getContext());
            textView.setTextSize(textsize);
            textView.setText(string);
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(layoutParams);

            constraintLayoutHead.addView(textView);
        }
    }

    void showTime(){
        TextView textView;
        ConstraintLayout.LayoutParams layoutParams;
        int textsize=10;
        int minuteToDp = 3;

        // 時間（縦軸）
        for (int j=0; j<24; j++){
            layoutParams = new ConstraintLayout.LayoutParams(0, 60 * minuteToDp);
            layoutParams.startToStart = R.id.constraintLayoutDo;
            layoutParams.endToStart = R.id.guidelineVdo;
            layoutParams.topToTop = R.id.constraintLayoutDo;
            //layoutParams.matchConstraintPercentWidth = (float)0.5;
            layoutParams.setMargins(0, 60 * minuteToDp * j, 0, 0);;

            textView = new TextView(getContext());
            textView.setTextSize(textsize);
            //textView.setPadding(0,0,0,0);
            textView.setText(String.valueOf(j)+":00");
            textView.setLayoutParams(layoutParams);

            constraintLayoutDo.addView(textView);
        }
    }

    void showItem(){
        float guidelineWidthPercent = (float)0.1;
        float widthPercent=(float)(1-guidelineWidthPercent)/(float)7.5;
        TextView textView;
        ConstraintLayout.LayoutParams layoutParams;
        float startx;
        int minuteToDp = 3;
        int textsize;
        textsize = 10;
        Calendar calendar;

        int cnt;
        String sql;
        Cursor cs=null;
        long dorecordid;
        String name;
        String starttime;
        Integer timewidth;
        String date;
        Integer isdone;
        String[] hhmm;
        Integer starttimeMinute;
        Integer dowNumber;
        // テーブルから対象週のレコードを取得 1レコードずつ処理
        cnt=0;
        sql = String.format("select * from do where week='%s' and isvalid=1", stringWeekStart);
        try(SQLiteDatabase db = helper.getReadableDatabase()) {
            cs = db.rawQuery(sql, null);
            while(cs.moveToNext()){
                // get and set value
                dorecordid = cs.getInt(cs.getColumnIndex("dorecordid"));
                isdone = cs.getInt(cs.getColumnIndex("isdone"));
                date = cs.getString(cs.getColumnIndex("date"));
                name = cs.getString(cs.getColumnIndex("name"));//以下はプラン情報
                starttime = cs.getString(cs.getColumnIndex("starttime"));
                timewidth = cs.getInt(cs.getColumnIndex("timewidth"));

                // 開始時刻を分に変換
                try {
                    hhmm = starttime.split(":");
                    starttimeMinute = Integer.parseInt(hhmm[0]) * 60 + Integer.parseInt(hhmm[1]);
                } catch (Exception e){ continue; }

                // 曜日で横位置を決める
                calendar = string2Calendar(date);
                dowNumber = calendar.get(Calendar.DAY_OF_WEEK);//Sun:1, Sat:7
                if (dowNumber==7){dowNumber=0;}//0:Sat
                startx = (float)1/7*dowNumber;

                layoutParams = new ConstraintLayout.LayoutParams(0, (int)(timewidth * minuteToDp));
                layoutParams.startToStart = R.id.guidelineVdo;
                layoutParams.endToEnd = R.id.constraintLayoutDo;
                layoutParams.topToTop = R.id.constraintLayoutDo;
                //widthの残りが移動できる範囲で、それをbiasで決める
                //widthが決まっていて、ある開始点startxに置きたい場合、
                layoutParams.matchConstraintPercentWidth = widthPercent;
                layoutParams.horizontalBias = startx / (1 - widthPercent);
                layoutParams.topMargin = (int)(starttimeMinute * minuteToDp);

                textView = new TextView(getContext());
                textView.setLayoutParams(layoutParams);
                textView.setTextSize(textsize);
                textView.setGravity(Gravity.CENTER);
                textView.setText(name);// plan name
                setBackGround(textView, isdone);
                //textView.setId(i);
                textView.setHint(String.valueOf(dorecordid));  // クリックイベントで使う
                textView.setOnClickListener(doOnClickListener);// リスナーを登録

                constraintLayoutDo.addView(textView);

                // to next record
                cnt++;
            }
        } finally {
            cs.close();
        }
    }

    void setBackGround(TextView textView, int isdone){
        if (isdone==0){
            textView.setBackgroundColor(getResources().getColor(R.color.colorBackground));
            textView.setAlpha((float)1);
        } else if (isdone==1){
            textView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            textView.setAlpha((float)0.6);
        }
    }

    // Viewが生成し終わった時に呼ばれるメソッド
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // クリックするとisdoneを反転させる
    View.OnClickListener doOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            TextView textView = (TextView)v;
            CharSequence hint = textView.getHint();
            Integer dorecordid = Integer.parseInt(hint.toString());
            Integer isdone = -1;
            String sql = String.format("select * from do where dorecordid = %d", dorecordid);

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
            sql = String.format("update do set isdone = %d where dorecordid = %d", isdone, dorecordid);
            try(SQLiteDatabase db = helper.getWritableDatabase()){ db.execSQL(sql); }

            // isdoneに応じて色を変える
            setBackGround((TextView)v, isdone);
        }
    };


    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // pass the events to the gesture detector
            // a return value of true means the detector is handling it
            // a return value of false means the detector didn't
            // recognize the event
            return mDetector.onTouchEvent(event);
        }
    };

    // In the SimpleOnGestureListener subclass you should override
    // onDown and any other gesture that you want to detect.
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Log.d("TAG", "onFling: ");
            // X軸最低スワイプ距離
            int SWIPE_MIN_DISTANCE = 50;

            // X軸最低スワイプスピード
            int SWIPE_THRESHOLD_VELOCITY = 200;

            // Y軸の移動距離　これ以上なら横移動を判定しない
            int SWIPE_MAX_OFF_PATH = 250;
            int dateToMove = 0;

            try {

                // 移動距離・スピード
                float distance_x = Math.abs((event1.getX() - event2.getX()));
                float velocity_x = Math.abs(velocityX);

                // flickを検知
                if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH) {
                    // Y軸の移動距離が大きすぎる場合
                    Log.d("TAG", "縦の移動距離が大きすぎ: ");
                }
                else if  (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    // 開始位置から終了位置の移動距離が指定値より大きい
                    // X軸の移動速度が指定値より大きい
                    Log.d("TAG", "右から左: ");
                    dateToMove += 7;
                }
                else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    // 終了位置から開始位置の移動距離が指定値より大きい
                    // X軸の移動速度が指定値より大きい
                    Log.d("TAG", "左から右: ");
                    dateToMove = -7;
                }
            } catch (Exception e) {
                // TODO
            }

            if (Math.abs(dateToMove)>0){
                // flickに応じて週をずらす
                String string = "move to ";
                if (dateToMove>0){
                    string += "next week";
                } else {
                    string += "previous week";
                }
                Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();

                // アクティビティーにずらす日を与える
                ((MainActivity) getActivity()).changeStartDay(dateToMove);

                // 再構成
                ((MainActivity) getActivity()).replaceDoFramgent();
            }

            return true;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d("TAG","onDown: ");

            // don't return false here or else none of the other
            // gestures will work
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.i("TAG", "onSingleTapConfirmed: ");
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i("TAG", "onLongPress: ");
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.i("TAG", "onDoubleTap: ");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            Log.i("TAG", "onScroll: ");
            return true;
        }

    }
}