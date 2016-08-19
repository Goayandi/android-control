package com.yongyida.robot.fragment;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.activity.AddTaskActivity;
import com.yongyida.robot.activity.TaskRemindActivity;
import com.yongyida.robot.bean.Remind;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.widget.ModifyRobotNameDialog;
import com.yongyida.robot.widget.RobotDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Administrator on 2016/6/27 0027.
 */
public class NewAddRemindFragment extends Fragment implements AddTaskActivity.onChooseListener ,View.OnClickListener{
    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";
    private TextView date;
    private TextView time;
    private Remind task;
    private TextView taskcontent;
    private String state;
    private int index;
    private TextView edit_title;
    private Calendar settime;
    boolean dateflag = false;
    boolean timeflag = false;
    private ModifyRobotNameDialog mDialog;

    @SuppressLint("SimpleDateFormat")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.from(getActivity()).inflate(
                R.layout.fragment_add_remind, null);
        state = getActivity().getIntent().getExtras().getString("state");
        date = (TextView) v.findViewById(R.id.tv_remind_date);
        time = (TextView) v.findViewById(R.id.tv_remind_time);
        taskcontent = (TextView) v.findViewById(R.id.tv_remind_content);
        edit_title = (TextView) v.findViewById(R.id.tv_remind_title);
        RelativeLayout contentRL = (RelativeLayout) v.findViewById(R.id.rl_content);
        contentRL.setOnClickListener(this);
        if (state.equals(Constants.Update)) {
            task = getActivity().getIntent().getParcelableExtra("task");
            settime = Calendar.getInstance();
            settime.setTimeInMillis(Long.parseLong(task.getSettime()));
            edit_title.setText(task.getTitle());
            if (settime.get(Calendar.MONTH)>= 9
                    && settime.get(Calendar.DATE) >= 10) {
                date.setText(settime.get(Calendar.YEAR) + getActivity().getString(R.string.year)
                        + (settime.get(Calendar.MONTH) + 1) + getActivity().getString(R.string.month)
                        + settime.get(Calendar.DATE) + getActivity().getString(R.string.day));
            } else if (settime.get(Calendar.MONTH)>= 9
                    && settime.get(Calendar.DATE) < 10) {
                date.setText(settime.get(Calendar.YEAR) + getActivity().getString(R.string.year)
                        + (settime.get(Calendar.MONTH) + 1) + getActivity().getString(R.string.month) + "0"
                        + settime.get(Calendar.DATE) + getActivity().getString(R.string.day));
            } else if (settime.get(Calendar.MONTH) < 9
                    && settime.get(Calendar.DATE) >= 10) {
                date.setText(settime.get(Calendar.YEAR) + getActivity().getString(R.string.year) + "0"
                        + (settime.get(Calendar.MONTH) + 1) + getActivity().getString(R.string.month)
                        + settime.get(Calendar.DATE) + getActivity().getString(R.string.day));
            } else {
                date.setText(settime.get(Calendar.YEAR) + getActivity().getString(R.string.year) + "0"
                        + (settime.get(Calendar.MONTH) + 1) + getActivity().getString(R.string.month) + "0"
                        + settime.get(Calendar.DATE) + getActivity().getString(R.string.day));
            }
            if (settime.get(Calendar.HOUR_OF_DAY) >= 10
                    && settime.get(Calendar.MINUTE) >= 10) {
                time.setText(settime.get(Calendar.HOUR_OF_DAY) + ":"
                        + settime.get(Calendar.MINUTE));
            } else if (settime.get(Calendar.HOUR_OF_DAY) < 10
                    && settime.get(Calendar.MINUTE) >= 10) {
                time.setText("0"+settime.get(Calendar.HOUR_OF_DAY) + ":"
                        + settime.get(Calendar.MINUTE));
            }else if(settime.get(Calendar.HOUR_OF_DAY) >= 10
                    && settime.get(Calendar.MINUTE) < 10){
                time.setText(settime.get(Calendar.HOUR_OF_DAY) + ":"
                        +"0"+ settime.get(Calendar.MINUTE));
            }else{
                time.setText("0"+settime.get(Calendar.HOUR_OF_DAY) + ":"
                        +"0"+ settime.get(Calendar.MINUTE));
            }
            taskcontent.setText(task.getContent() + "");
            index = getActivity().getIntent().getExtras().getInt("index");
            timeflag = true;
            dateflag = true;
        } else {
            task = new Remind();
            settime = Calendar.getInstance();
        }
        return v;
    }

    RobotDialog alert = null;

    @Override
    public void onDate(int year, int monthOfYear, int dayOfMonth) {
        settime.set(year, monthOfYear, dayOfMonth);
        dateflag = true;
        String month = "";
        String day = "";
        if (monthOfYear < 9) {
            month = "0" + (monthOfYear + 1);
        } else {
            month = "" + (monthOfYear + 1);
        }
        if (dayOfMonth < 10) {
            day = "0" + dayOfMonth;
        } else {
            day = "" + dayOfMonth;
        }
        date.setText(year + getActivity().getString(R.string.year) + month + getActivity().getString(R.string.month) + day + getActivity().getString(R.string.day));

    }

    @Override
    public void onTime(int hourOfDay, int minute) {
        settime.set(settime.get(Calendar.YEAR), settime.get(Calendar.MONTH),
                settime.get(Calendar.DATE), hourOfDay, minute, 0);
        String hour = "";
        String min = "";
        if (hourOfDay < 10) {
            if (hourOfDay == 0) {
                hour = "00";
            } else {
                hour = "0" + hourOfDay;
            }

        } else {
            hour = hourOfDay + "";
        }
        if (settime.get(Calendar.MINUTE) < 10) {
            min = "0" + minute;
            time.setText(hourOfDay + ":0" + minute);
        } else {
            min = "" + minute;
            time.setText(hourOfDay + ":" + min);
        }
        time.setText(hour + ":" + min);
        timeflag = true;

    }

    @Override
    public void onOver() {
        String content = taskcontent.getText().toString().trim();
        String title = edit_title.getText().toString().trim();
        if (TextUtils.isEmpty(content) && TextUtils.isEmpty(title)){
            if(TextUtils.isEmpty(content)){
                ToastUtil.showtomain(getActivity(), getActivity().getString(R.string.input_remind_content));
            }else{
                ToastUtil.showtomain(getActivity(), getActivity().getString(R.string.input_title));
            }
            return;
        }
        if (state.equals(Constants.Add)) {
            if (dateflag == false && timeflag == false) {
                ToastUtil.showtomain(getActivity(), getActivity().getString(R.string.choose_time));
                return;
            }
        }
        if (settime.getTimeInMillis() < System.currentTimeMillis()) {
            ToastUtil.showtomain(getActivity(), getActivity().getString(R.string.time_illegal));
            return;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        String datatime = dateFormat.format(settime.getTimeInMillis());
        task.setSettime(datatime);
        task.setTitle(title);
        task.setContent(content);
        Intent in = new Intent();
        Intent intent = new Intent(getActivity(), TaskRemindActivity.class);
        intent.putExtra("task", task);
        Constants.task = task;
        if (state.equals(Constants.Update)) {
            intent.putExtra("index", index);
            in.setAction(Constants.Task_Updata);
        } else {
            in.setAction(Constants.Task_Add);
        }
        getActivity().sendBroadcast(in);
        getActivity().setResult(Constants.IS_OK, intent);
        getActivity().finish();

    }

    @Override
    public void onChoose(String text) {
        edit_title.setText(text);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_content:
                mDialog = new ModifyRobotNameDialog(getActivity(), new ModifyRobotNameDialog.OnSaveListener() {
                    @Override
                    public void save(String name) {
                        if (TextUtils.isEmpty(name)) {
                            taskcontent.setText("");
                        } else {
                            taskcontent.setText(name);
                        }
                        mDialog.dismiss();
                    }
                }, taskcontent.getText().toString());
                mDialog.setTitle(getString(R.string.remind_content));
                mDialog.show();
                break;
        }
    }
}
