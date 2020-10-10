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
    Integer recordid;

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

    // update/insert button click lister
    View.OnClickListener updateButtonOnClickLister = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int recordCount=-1;
            String sql="";
            Cursor cursor;

            String itemid = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());// itemidは登録日時
            String startdate = new SimpleDateFormat("yyyyMMdd").format(new Date());// 作成/編集日をあてておく
            Integer version = 1;// 登録時は1。編集ごとに増やしていく

            // レコード数確認
            Integer recordCountAll = 0;
            sql = String.format("select count(*) from plan");
            try (SQLiteDatabase db = helper.getReadableDatabase()) {
                cursor = db.rawQuery(sql, null);
                try {
                    if (cursor.moveToNext()) {
                        recordCountAll = cursor.getInt(0);
                    }
                } finally {
                    cursor.close();
                }
            }

            // 新しいrecordidの決定
            if (recordCountAll==0){
                recordid = 0;
            } else{
                // recordidのmax読み取り
                sql = String.format("select max(recordid) from plan");
                try (SQLiteDatabase db = helper.getReadableDatabase()) {
                    cursor = db.rawQuery(sql, null);
                    try {
                        if (cursor.moveToNext()) {
                            recordid = cursor.getInt(0);
                        }
                    } finally {
                        cursor.close();
                    }
                }
                recordid += 1; // 新しいレコードは1つ足したIDとする
            }


            // 設定内容を取得
            String name = EditTextName.getText().toString();
            Integer isvalid = 1;
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

            // tableにinsert
            sql = String.format(
                    "insert into plan (recordid, itemid, isvalid, name, dow, interval, starttime, timewidth, startdate, version) values (%d,'%s', %d, '%s', '%s', %d, '%s', %d, '%s', %d);",
                    recordid,
                    itemid,
                    isvalid,
                    name,
                    dow,
                    interval,
                    starttime,
                    timewidth,
                    startdate,
                    version
            );
            // update
            // sql = String.format("update diary set diary = '%s' where date = '%s'", diaryString, dateString);
            try (SQLiteDatabase db = helper.getWritableDatabase()) {
                db.execSQL(sql);
            }

            // ダイアログを閉じる
            alertDialogCreated.dismiss();

            /*
            // テーブルのレコード数を取得
            sql = String.format("select count(*) from plan");
            try(SQLiteDatabase db = helper.getReadableDatabase()) {
                cursor = db.rawQuery(sql, null);
                try {
                    if (cursor.moveToNext()) {
                        recordCount = cursor.getInt(0);
                    }
                } finally {
                    cursor.close();
                }
            }
            */
        }
    };
}