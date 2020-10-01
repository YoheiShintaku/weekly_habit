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

            // itemidは登録日時
            DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String itemid = formatter.format(new Date());

            // 設定内容を取得
            String name = EditTextName.getText().toString();
            Integer isvalid = 1;
            Integer interval = Integer.parseInt(EditTextWeekInterval.getText().toString());
            String starttime = EditTextTimeStart.getText().toString();
            Integer timewidth = Integer.parseInt(EditTextTimeLength.getText().toString());
            String dow = "";
            if (CheckBoxMon.isChecked()){ dow += ",Mon"; }
            if (CheckBoxTue.isChecked()){ dow += ",Tue"; }
            if (CheckBoxWed.isChecked()){ dow += ",Wed"; }
            if (CheckBoxThu.isChecked()){ dow += ",Thu"; }
            if (CheckBoxFri.isChecked()){ dow += ",Fri"; }
            if (CheckBoxSat.isChecked()){ dow += ",Sat"; }
            if (CheckBoxSun.isChecked()){ dow += ",Sun"; }
            dow = dow.substring(1);//初めのカンマを除く

            //カンマ区切りの練習
            //String[] a = dow.split(",");

            // tableにinsert
            sql = String.format(
                    "insert into plan (itemid, isvalid, name, dow, interval, starttime, timewidth) values ('%s', %d, '%s', '%s', %d, '%s', %d);",
                    itemid,
                    isvalid,
                    name,
                    dow,
                    interval,
                    starttime,
                    timewidth
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