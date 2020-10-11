package com.example.weekly_habit;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.CheckBox;
import android.widget.EditText;

public class PlanAddDialogFragment extends DialogFragment {
    private SimpleDatabaseHelper helper = null;
    EditText EditTextName;
    CheckBox CheckBoxMon;
    CheckBox CheckBoxTue;
    CheckBox CheckBoxWed;
    CheckBox CheckBoxThu;
    CheckBox CheckBoxFri;
    CheckBox CheckBoxSat;
    CheckBox CheckBoxSun;
    EditText EditTextWeekInterval;
    EditText EditTextTimeStart;
    EditText EditTextTimeLength;
    AlertDialog alertDialogCreated;
    String state=null;
    int recordidEdit;

    // flagmentでは引数つきのコンストラクタは作れないので、こういう関数を作る
    public static PlanAddDialogFragment newInstance(int planrecordid) {
        PlanAddDialogFragment fragment = new PlanAddDialogFragment();
        Bundle b = new Bundle();
        b.putInt("planrecordid", planrecordid);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        helper = new SimpleDatabaseHelper(getContext());

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View content = inflater.inflate(R.layout.fragment_plan_add_dialog, null);

        Button planUpdateButton = content.findViewById(R.id.planUpdateButton);
        EditTextName = content.findViewById(R.id.EditTextName);// plan name
        CheckBoxMon = content.findViewById(R.id.checkBoxMon);
        CheckBoxTue = content.findViewById(R.id.checkBoxTue);
        CheckBoxWed = content.findViewById(R.id.checkBoxWed);
        CheckBoxThu = content.findViewById(R.id.checkBoxThu);
        CheckBoxFri = content.findViewById(R.id.checkBoxFri);
        CheckBoxSat = content.findViewById(R.id.checkBoxSat);
        CheckBoxSun = content.findViewById(R.id.checkBoxSun);
        EditTextWeekInterval = content.findViewById(R.id.weekInterval);
        EditTextTimeStart = content.findViewById(R.id.timeStart);
        EditTextTimeLength = content.findViewById(R.id.timeLength);

        planUpdateButton.setOnClickListener(updateButtonOnClickLister);

        recordidEdit = getArguments().getInt("planrecordid");// 取得
        if (recordidEdit==-1){
            // 新規登録
            state = "new";
        } else{
            state = "edit";
            // 編集
            Cursor cs;
            String sql;
            String name=null;
            String starttime=null;
            String dowAll=null;
            Integer interval=0;
            Integer timewidth=0;

            // 現在のプラン情報取得
            sql = String.format("select * from plan where planrecordid = %d", recordidEdit);
            try(SQLiteDatabase db = helper.getReadableDatabase()) {
                cs = db.rawQuery(sql, null);
                if(cs.moveToNext()){
                    // get value
                    name = cs.getString(cs.getColumnIndex("name"));
                    dowAll = cs.getString(cs.getColumnIndex("dow"));
                    interval = cs.getInt(cs.getColumnIndex("interval"));
                    starttime = cs.getString(cs.getColumnIndex("starttime"));
                    timewidth = cs.getInt(cs.getColumnIndex("timewidth"));
                }
            }

            // 値をセット
            EditTextName.setText(name);
            EditTextWeekInterval.setText(String.valueOf(interval));
            EditTextTimeStart.setText(starttime);
            EditTextTimeLength.setText(String.valueOf(timewidth));
            String[] dowSplited = dowAll.split(",");
            for (int i=0; i<dowSplited.length; i++){
                switch(dowSplited[i]){
                    case "0": CheckBoxSat.setChecked(true);break;
                    case "1": CheckBoxSun.setChecked(true);break;
                    case "2": CheckBoxMon.setChecked(true);break;
                    case "3": CheckBoxTue.setChecked(true);break;
                    case "4": CheckBoxWed.setChecked(true);break;
                    case "5": CheckBoxThu.setChecked(true);break;
                    case "6": CheckBoxFri.setChecked(true);break;
                }
            }
        }

        builder.setView(content);

        builder.setMessage("登録/編集/削除")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        alertDialogCreated = builder.create();
        return alertDialogCreated;
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

    // update/insert button click lister
    View.OnClickListener updateButtonOnClickLister = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String sql;
            int planrecordid=-1;
            int itemid=-1;
            Integer version=-1;// 登録時は1。編集ごとに増やしていく
            Integer isvalid;
            String startdate = new SimpleDateFormat("yyyyMMdd").format(new Date());// 作成日をあてておく

            // 設定内容を取得
            String name = EditTextName.getText().toString();
            Integer interval = Integer.parseInt(EditTextWeekInterval.getText().toString());
            String starttime = EditTextTimeStart.getText().toString();
            Integer timewidth = Integer.parseInt(EditTextTimeLength.getText().toString());
            String dow = "";
            if (CheckBoxSat.isChecked()){ dow += ",0"; }
            if (CheckBoxSun.isChecked()){ dow += ",1"; }
            if (CheckBoxMon.isChecked()){ dow += ",2"; }
            if (CheckBoxTue.isChecked()){ dow += ",3"; }
            if (CheckBoxWed.isChecked()){ dow += ",4"; }
            if (CheckBoxThu.isChecked()){ dow += ",5"; }
            if (CheckBoxFri.isChecked()){ dow += ",6"; }
            dow = dow.substring(1);//初めのカンマを除く

            // 新しいrecordid, itemid
            planrecordid = 0;
            if (getRecordCount("select count(*) from plan")>0){
                planrecordid = getRecordCount("select max(planrecordid) from plan") + 1;// recordidのmax+1
            }

            isvalid = 1;
            if (state=="new"){
                // 新規登録
                if (planrecordid==0){
                    itemid = 0;
                }else{
                    itemid = getRecordCount("select max(itemid) from plan") + 1;// itemidのmax+1
                }
                version = 1;

            } else if (state=="edit"){
                // 編集の場合、itemidは変えない、versionは+1
                itemid = getRecordCount(String.format("select itemid from plan where planrecordid = %d", recordidEdit));
                version = getRecordCount(String.format("select version from plan where planrecordid = %d", recordidEdit))+1;//versionを一つ上げる
            }

            // tableにinsert
            sql = String.format(
                    "insert into plan (" +
                            "planrecordid, itemid, version, isvalid, startdate, " +
                            "name, dow, interval, starttime, timewidth) values (" +
                            "%d, %d,%d,%d,'%s'," +
                            "'%s','%s',%d,'%s',%d);",
                    planrecordid, itemid, version, isvalid, startdate,
                    name, dow, interval, starttime, timewidth);
            try (SQLiteDatabase db = helper.getWritableDatabase()) { db.execSQL(sql); }

            // 編集の場合、古いレコードを無効にする
            if (state=="edit") {
                // plan
                sql = String.format("update plan set isvalid = 0 where planrecordid = %d;", recordidEdit);
                try (SQLiteDatabase db = helper.getWritableDatabase()) { db.execSQL(sql); }
                // do
                sql = String.format("update do set isvalid = 0 where planrecordid = %d;", recordidEdit);
                try (SQLiteDatabase db = helper.getWritableDatabase()) { db.execSQL(sql); }
            }

            // ダイアログを閉じる
            alertDialogCreated.dismiss();
        }
    };
}