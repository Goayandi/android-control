package com.yongyida.robot.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.activity.AddTaskActivity;
import com.yongyida.robot.activity.ChooseWeekActivity;
import com.yongyida.robot.activity.TaskRemindActivity;
import com.yongyida.robot.bean.Alarm;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.widget.SwitchButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Administrator on 2016/6/29 0029.
 */
public class NewAddAlarmFragment extends Fragment implements View.OnClickListener,
        AddTaskActivity.onChooseListener{
    private static final int REQUEST_CODE = 1;
    private static final int Sunday = 7;
    private Calendar settime;
    private String state;
    private TextView mTVTitle;
    private TextView mTVTime;
    private TextView mTVWeek;
    private Alarm alarm;
    private int index;
    private String mWeeksString;
    private SwitchButton mSwitchButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.from(getActivity()).inflate(
                R.layout.fragment_add_alarm, null);
        state = getActivity().getIntent().getExtras().getString("state");
        Log.i("state", state);
        mTVTime = (TextView) v.findViewById(R.id.tv_alarm_time);
        mTVWeek = (TextView) v.findViewById(R.id.tv_week);
        mTVTitle = (TextView) v.findViewById(R.id.tv_alarm_title);
        mSwitchButton = (SwitchButton) v.findViewById(R.id.isaways_setting);
        v.findViewById(R.id.rl_week).setOnClickListener(this);
        if (state.equals(Constants.Update)) {
            alarm = getActivity().getIntent().getParcelableExtra("task");
            mTVTitle.setText(alarm.getTitle());
            settime = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            try {
                settime.setTime(simpleDateFormat.parse(alarm.getSettime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (settime.get(Calendar.HOUR_OF_DAY) >= 10
                    && settime.get(Calendar.MINUTE) >= 10) {
                mTVTime.setText(settime.get(Calendar.HOUR_OF_DAY) + ":"
                        + settime.get(Calendar.MINUTE));
            } else if (settime.get(Calendar.HOUR_OF_DAY) < 10
                    && settime.get(Calendar.MINUTE) >= 10) {
                mTVTime.setText("0"+settime.get(Calendar.HOUR_OF_DAY) + ":"
                        + settime.get(Calendar.MINUTE));
            }else if(settime.get(Calendar.HOUR_OF_DAY) >= 10
                    && settime.get(Calendar.MINUTE) < 10){
                mTVTime.setText(settime.get(Calendar.HOUR_OF_DAY) + ":"
                        +"0"+ settime.get(Calendar.MINUTE));
            }else{
                mTVTime.setText("0"+settime.get(Calendar.HOUR_OF_DAY) + ":"
                        +"0"+ settime.get(Calendar.MINUTE));
            }
            if (alarm.getIsaways() == 1) {
                mSwitchButton.setChecked(true);
            } else {
                mSwitchButton.setChecked(false);
            }
            mWeeksString = alarm.getWeek();
            index = getActivity().getIntent().getExtras().getInt("index");
        } else {
            alarm = new Alarm();
            settime = Calendar.getInstance();
        }
        setWeek();

        return v;
    }

    private void setWeekText() {
        String[] weeks = mWeeksString.split(",");
        String text = "";
        for (String s : weeks) {
            text += getWeekText(s) + " ";
        }
        mTVWeek.setText(text);
    }

    private String getWeekText(String i){
        if (i.equals("1")) {
            return getString(R.string.monday);
        } else if (i.equals("2")) {
            return getString(R.string.tuesday);
        } else if (i.equals("3")) {
            return getString(R.string.wednesday);
        } else if (i.equals("4")) {
            return getString(R.string.thursday);
        } else if (i.equals("5")) {
            return getString(R.string.friday);
        } else if (i.equals("6")) {
            return getString(R.string.saturday);
        } else if (i.equals("7")) {
            return getString(R.string.sunday);
        }
        return "";
    }

    @Override
    public void onOver() {
        String title = mTVTitle.getText().toString().trim();
        if (TextUtils.isEmpty(mTVTime.getText().toString())) {
            ToastUtil.showtomain(getActivity(), getActivity().getString(R.string.choose_time));
            return;
        }
        if (TextUtils.isEmpty(mTVTitle.getText().toString())) {
            ToastUtil.showtomain(getActivity(), getActivity().getString(R.string.input_title));
            return;
        }

        boolean[] bs = getChooseWeekArr();
        Calendar calendar = Calendar.getInstance();
        int currnweek = calendar.get(Calendar.DAY_OF_WEEK) - 1 == 0 ? Sunday
                : calendar.get(Calendar.DAY_OF_WEEK) - 1;
        boolean fg = true;         //是否没选小于当前星期的星期值    false选了  true没选
        //	boolean iscurrn = false;
        boolean isChooseWeek = false;   //是否选择了星期几
        for (int i = 0; i < bs.length; i++) {
            isChooseWeek = isChooseWeek || bs[i];
            if (bs[i]) {
                if (i + 1 < currnweek) {
                    fg = fg && false;
                } else if (i + 1 == currnweek) {
                    if (settime.get(Calendar.HOUR_OF_DAY) < calendar
                            .get(Calendar.HOUR_OF_DAY)) {
                        fg = fg && false;
                    } else if (settime.get(Calendar.HOUR_OF_DAY) == calendar
                            .get(Calendar.HOUR_OF_DAY)
                            && settime.get(Calendar.MINUTE) <= calendar
                            .get(Calendar.MINUTE)) {
                        fg = fg && false;
                    } else {
                        fg = fg && true;
                    }
                } else {
                    fg = fg && true;
                }
            }
        }

        if(!isChooseWeek){
            ToastUtil.showtomain(getActivity(), getActivity().getString(R.string.choose_week));
            return;
        }

        if(!mSwitchButton.isChecked()){
            if(!fg){        //如果星期都是
                ToastUtil.showtomain(getActivity(), getActivity().getString(R.string.exist_time_expire));
                return;
            }
        }

        int repeat = mSwitchButton.isChecked() ? 1 : 0;
        alarm.setWeek(mWeeksString);
        alarm.setIsaways(repeat);
        alarm.setTitle(title);
        alarm.setSettime(mTVTime.getText().toString());
        Intent in = new Intent();
        Intent intent = new Intent(getActivity(), TaskRemindActivity.class);
        intent.putExtra("task", alarm);
        Constants.task = alarm;
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

    private boolean[] getChooseWeekArr() {
        boolean[] weekArrBoolean = new boolean[7];
        String[] weekArrString = mWeeksString.split(",");
        for (int i = 0; i < weekArrString.length; i++) {
            weekArrBoolean[Integer.parseInt(weekArrString[i]) - 1] = true;
        }
        return weekArrBoolean;
    }

    @Override
    public void onDate(int year, int monthOfYear, int dayOfMonth) {

    }

    @Override
    public void onTime(int hourOfDay, int minute) {
        settime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        settime.set(Calendar.MINUTE, minute);
        String hour = "";
        String min = "";
        if (hourOfDay < 10) {
            hour = "0" + hourOfDay;
        } else {
            hour = "" + hourOfDay;
        }
        if (minute < 10) {
            min = "0" + minute;
        } else {
            min = "" + minute;
        }
        mTVTime.setText(hour + ":" + min);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_week:
                Intent intent = new Intent(getActivity(), ChooseWeekActivity.class);
                intent.putExtra(Constants.CHOOSED_WEEK, mWeeksString);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ChooseWeekActivity.CHOOSE_WEEK_ACTIVITY_RESULT) {
            String weeks = data.getStringExtra(Constants.CHOOSED_WEEK_RESULT);
            mWeeksString = weeks;
            setWeek();
        }
    }

    private void setWeek(){
        if (TextUtils.isEmpty(mWeeksString)) {
            setDefaultWeek();
        } else {
            setWeekText();
        }
    }

    private void setDefaultWeek(){
        mWeeksString = "1";
        mTVWeek.setText(getString(R.string.monday));
    }

    @Override
    public void onChoose(String text) {
        mTVTitle.setText(text);
    }

}
