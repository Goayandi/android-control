package com.yongyida.robot.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.bean.Remind;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Administrator on 2016/6/27 0027.
 */
public class EventRemindAdapter extends BaseAdapter {
    private List tasks;
    private Context context;

    public EventRemindAdapter(Context context, List tasks) {
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
        TaskHolder holder = null;
        if (layout == null) {
            layout = LayoutInflater.from(context);
        }

        Calendar finishtime = Calendar.getInstance();
        if (tasks.get(index) instanceof Remind) {
            if (v == null){
                v = layout.inflate(R.layout.event_remind_adapter_item, null);
                holder = new TaskHolder();
            } else {
                holder = (TaskHolder) v.getTag();
            }
            holder.textview_settime = (TextView) v.findViewById(R.id.time);
            holder.textView_content = (TextView) v.findViewById(R.id.contenttask);
            holder.textview_title = (TextView) v.findViewById(R.id.tv_title);
            String date = ((Remind) tasks.get(index)).getSettime();
            finishtime.setTimeInMillis(Long.parseLong(date));
            String year = "";
            String month = "";
            String day = "";
            String hour = "";
            String min = "";

            year = "" + finishtime.get(Calendar.YEAR);

            if (finishtime.get(Calendar.MONTH) < 9) {
                month = "0" + (finishtime.get(Calendar.MONTH) + 1);
            } else {
                month = "" + (finishtime.get(Calendar.MONTH) + 1);
            }
            if (finishtime.get(Calendar.DAY_OF_MONTH) < 10) {
                day = "0" + finishtime.get(Calendar.DAY_OF_MONTH);
            } else {
                day = "" + finishtime.get(Calendar.DAY_OF_MONTH);
            }
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
            if(!TextUtils.isEmpty(((Remind) tasks.get(index)).getTitle())) {
                holder.textview_title.setText(((Remind) tasks.get(index)).getTitle());
            }
            holder.textview_settime.setText(year + "." + month + "." + day
                    + " " + hour + ":" + min);
            String titleContent = ((Remind) tasks.get(index)).getContent();
            if(titleContent.length() > 15){
                holder.textView_content.setText(titleContent.substring(0,14) + "...");
            } else {
                holder.textView_content.setText(titleContent);
            }
        }
        if (holder != null) {
            v.setTag(holder);
        }
        return v;
    }


    class TaskHolder {
        private TextView textview_title;
        private TextView textview_settime;
        private TextView textView_content;
    }
}
