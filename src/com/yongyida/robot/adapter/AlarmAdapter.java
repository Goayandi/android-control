package com.yongyida.robot.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.bean.Alarm;
import com.yongyida.robot.widget.WeekChooseLinearlayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Administrator on 2016/6/27 0027.
 */
public class AlarmAdapter extends BaseAdapter {

    private List tasks;
    private Context context;

    public AlarmAdapter(Context context, List tasks) {
        super();
        this.context = context;
        this.tasks = tasks;
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    LayoutInflater layout = null;

    @Override
    public View getView(int index, View v, ViewGroup arg2) {
        AlarmHolder alarmholder = null;
        if (layout == null) {
            layout = LayoutInflater.from(context);
        }

        Calendar finishtime = Calendar.getInstance();
        if (tasks.get(index) instanceof Alarm) {
            if (v == null){
                v = layout.inflate(R.layout.alarm_adapter_item, null);
                alarmholder = new AlarmHolder();
            } else {
                alarmholder = (AlarmHolder) v.getTag();
            }
            alarmholder.textview_settime = (TextView) v
                    .findViewById(R.id.tv_time);
            alarmholder.textView_title = (TextView) v
                    .findViewById(R.id.tv_title);
            alarmholder.weekChooseLinearlayout = (WeekChooseLinearlayout) v
                    .findViewById(R.id.wcll);
            Alarm alarm = (Alarm) tasks.get(index);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            try {
                finishtime.setTime(simpleDateFormat.parse(alarm.getSettime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String[] week = alarm.getWeek().split(",");
            if (week != null && week.length == 7 && alarm.getIsaways() != 0) {
                alarmholder.weekChooseLinearlayout.setEveryDay();
            } else {
                alarmholder.weekChooseLinearlayout.addWeekTextView(week, alarm.getIsaways() != 0);
            }

            String hour = "";
            String min = "";
            String scn = "";
            if (finishtime.get(Calendar.HOUR_OF_DAY) < 10) {
                if (finishtime.get(Calendar.HOUR_OF_DAY) == 0) {
                    hour = "00";
                } else {
                    hour = "0" + finishtime.get(Calendar.HOUR_OF_DAY);
                }

            } else {

                hour = finishtime.get(Calendar.HOUR_OF_DAY) + "";
            }
            if (finishtime.get(Calendar.MINUTE) < 10) {
                min = "0" + finishtime.get(Calendar.MINUTE);
            } else {
                min = "" + finishtime.get(Calendar.MINUTE);
            }
            if (finishtime.get(Calendar.SECOND) < 10) {
                scn = "0" + finishtime.get(Calendar.SECOND);
            } else {
                scn = "" + finishtime.get(Calendar.SECOND);
            }

            alarmholder.textview_settime.setText(hour + ":" + min + ":" + scn);
            if(!TextUtils.isEmpty(alarm.getTitle())) {
                String titleContent = alarm.getTitle();
                alarmholder.textView_title.setText(titleContent);
                alarmholder.textView_title.setTextColor(context.getResources().getColor(R.color.task_item_title));
            }else{
                String titleContent = alarm.getContent();
                if(titleContent.length() > 15){
                    alarmholder.textView_title.setText(titleContent.substring(0,14) + "...");
                } else {
                    alarmholder.textView_title.setText(titleContent);
                }
                alarmholder.textView_title.setTextColor(context.getResources().getColor(R.color.event_alarm_text_color));
            }

        }
        if (alarmholder != null) {
            v.setTag(alarmholder);
        }
        return v;
    }


    class AlarmHolder {
        private TextView textview_settime;
        private TextView textView_title;
        private WeekChooseLinearlayout weekChooseLinearlayout;
    }
}
