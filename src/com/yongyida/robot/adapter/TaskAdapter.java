//package com.robotcontrol.adapter;
//
//import java.sql.Timestamp;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//
//import com.robotcontrol.activity.R;
//import com.robotcontrol.bean.Task;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.TextView;
//
//public class TaskAdapter extends BaseAdapter {
//
//	private List<Task> tasks;
//	private Context context;
//
//	public TaskAdapter(Context context, List<Task> tasks) {
//		super();
//		this.context = context;
//		this.tasks = tasks;
//	}
//
//	@Override
//	public int getCount() {
//		return tasks.size();
//	}
//
//	@Override
//	public Object getItem(int arg0) {
//		return null;
//	}
//
//	@Override
//	public long getItemId(int arg0) {
//		return 0;
//	}
//
//	TaskHolder holder;
//	LayoutInflater layout = null;
//
//	@Override
//	public View getView(int index, View v, ViewGroup arg2) {
//		if (layout == null) {
//			layout = LayoutInflater.from(context);
//		}
//		holder = new TaskHolder();
//		v = layout.inflate(R.layout.taskitem, null);
//		holder.textview_settime = (TextView) v.findViewById(R.id.time);
//		holder.textView_content = (TextView) v.findViewById(R.id.contenttask);
//		holder.textView_istoday = (TextView) v.findViewById(R.id.istoday);
//		holder.textView_finishtime = (TextView) v.findViewById(R.id.settime);
//
//		Calendar finishtime = Calendar.getInstance();
//		finishtime.setTimeInMillis(Long
//				.parseLong(tasks.get(index).getSettime()));
//		holder.textView_istoday.setText("当天");
//		holder.textview_settime.setText(finishtime.get(Calendar.YEAR) + "."
//				+ (finishtime.get(Calendar.MONTH)+1) + "."
//				+ finishtime.get(Calendar.DATE) + " "
//				+ finishtime.get(Calendar.HOUR_OF_DAY) + ":"
//				+ finishtime.get(Calendar.MINUTE));
//		holder.textView_content.setText("[" + tasks.get(index).getTitle() + "] "
//				+ tasks.get(index).getContent());
//		v.setTag(holder);
//		return v;
//	}
//
//	class TaskHolder {
//		private TextView textview_settime;
//		private TextView textView_content;
//		private TextView textView_istoday;
//		private TextView textView_finishtime;
//	}
//
//}

package com.yongyida.robot.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.bean.Alarm;
import com.yongyida.robot.bean.Remind;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class TaskAdapter extends BaseAdapter {

	private List tasks;
	private Context context;

	public TaskAdapter(Context context, List tasks) {
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
		AlarmHolder alarmholder = null;
		if (layout == null) {
			layout = LayoutInflater.from(context);
		}

		Calendar finishtime = Calendar.getInstance();
		if (tasks.get(index) instanceof Remind) {
			if (v == null){
				v = layout.inflate(R.layout.taskitem, null);
				holder = new TaskHolder();
			} else {
				holder = (TaskHolder) v.getTag();
			}
			holder.textview_settime = (TextView) v.findViewById(R.id.time);
			holder.textView_content = (TextView) v
					.findViewById(R.id.contenttask);
			holder.textView_istoday = (TextView) v.findViewById(R.id.istoday);
			tasks = (List<Remind>) tasks;
			String date = ((Remind) tasks.get(index)).getSettime();
			finishtime.setTimeInMillis(Long.parseLong(date));
			holder.textView_istoday.setText(R.string.that_day);
			holder.textview_settime.setText(finishtime.get(Calendar.YEAR) + "."
					+ (finishtime.get(Calendar.MONTH) + 1) + "."
					+ finishtime.get(Calendar.DATE) + " "
					+ finishtime.get(Calendar.HOUR_OF_DAY) + ":"
					+ finishtime.get(Calendar.MINUTE));
			String year = "";
			String month = "";
			String day = "";
			String hour = "";
			String min = "";
//			String scn = "";

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
//			if (finishtime.get(Calendar.SECOND) < 10) {
//				scn = "0" + finishtime.get(Calendar.SECOND);
//			} else {
//				scn = "" + finishtime.get(Calendar.SECOND);
//			}
			holder.textview_settime.setText(year + "." + month + "." + day
					+ " " + hour + ":" + min);// + ":" + scn);
			if(!TextUtils.isEmpty(((Remind) tasks.get(index)).getTitle())){
				String titleContent = "["
						+ ((Remind) tasks.get(index)).getTitle() + "] "
						+ ((Remind) tasks.get(index)).getContent();
				if(titleContent.length() > 15){
					holder.textView_content.setText(titleContent.substring(0,13) + "...");
				} else {
					holder.textView_content.setText(titleContent);
				}
			}else{
				String titleContent = ((Remind) tasks.get(index)).getContent();
				if(titleContent.length() > 15){
					holder.textView_content.setText(titleContent.substring(0,13) + "...");
				} else {
					holder.textView_content.setText(titleContent);
				}
			}
		} else if (tasks.get(index) instanceof Alarm) {
			if (v == null){
				v = layout.inflate(R.layout.alarmitem, null);
				alarmholder = new AlarmHolder();
			} else {
				alarmholder = (AlarmHolder) v.getTag();
			}
			alarmholder.textview_settime = (TextView) v
					.findViewById(R.id.alarm_time);
			alarmholder.textView_content = (TextView) v
					.findViewById(R.id.alarm_content);
			alarmholder.textView_istoday = (TextView) v
					.findViewById(R.id.alarm_istoday);
			alarmholder.textView_1 = (TextView) v.findViewById(R.id.alarm_1);
			alarmholder.textView_2 = (TextView) v.findViewById(R.id.alarm_2);
			alarmholder.textView_3 = (TextView) v.findViewById(R.id.alarm_3);
			alarmholder.textView_4 = (TextView) v.findViewById(R.id.alarm_4);
			alarmholder.textView_5 = (TextView) v.findViewById(R.id.alarm_5);
			alarmholder.textView_6 = (TextView) v.findViewById(R.id.alarm_6);
			alarmholder.textView_7 = (TextView) v.findViewById(R.id.alarm_7);
			tasks = (List<Alarm>) tasks;
			Alarm alarm = (Alarm) tasks.get(index);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
			try {
				finishtime.setTime(simpleDateFormat.parse(alarm.getSettime()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (alarm.getIsaways() == 0) {
				alarmholder.textView_istoday.setText(R.string.this_week);
			} else {
				alarmholder.textView_istoday.setText(R.string.every_day);
			}
					
			for(int n = 1; n <= 7; n++){
				if(alarm.getWeek().contains(n + "")){
					switch (n) {
					case 1:
						alarmholder.textView_1.setTextColor(Color.GREEN);
						break;
					case 2:
						alarmholder.textView_2.setTextColor(Color.GREEN);
						break;
					case 3:
						alarmholder.textView_3.setTextColor(Color.GREEN);
						break;
					case 4:
						alarmholder.textView_4.setTextColor(Color.GREEN);
						break;
					case 5:
						alarmholder.textView_5.setTextColor(Color.GREEN);
						break;
					case 6:
						alarmholder.textView_6.setTextColor(Color.GREEN);
						break;
					case 7:
						alarmholder.textView_7.setTextColor(Color.GREEN);
						break;
					default:
						break;
					}
				}else{
					switch (n) {
					case 1:
						alarmholder.textView_1.setTextColor(Color.BLACK);
						break;
					case 2:
						alarmholder.textView_2.setTextColor(Color.BLACK);
						break;
					case 3:
						alarmholder.textView_3.setTextColor(Color.BLACK);
						break;
					case 4:
						alarmholder.textView_4.setTextColor(Color.BLACK);
						break;
					case 5:
						alarmholder.textView_5.setTextColor(Color.BLACK);
						break;
					case 6:
						alarmholder.textView_6.setTextColor(Color.BLACK);
						break;
					case 7:
						alarmholder.textView_7.setTextColor(Color.BLACK);
						break;
					default:
						break;
					}
				}
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
				String titleContent = "[" + alarm.getTitle() + "]"
						+ alarm.getContent();
				if(titleContent.length() > 15){
					alarmholder.textView_content.setText(titleContent.substring(0,13) + "...");
				} else {
					alarmholder.textView_content.setText(titleContent);
				}
			}else{
				String titleContent = alarm.getContent();
				if(titleContent.length() > 15){
					alarmholder.textView_content.setText(titleContent.substring(0,13) + "...");
				} else {
					alarmholder.textView_content.setText(titleContent);
				}
			}

		}
		if (holder != null) {
			v.setTag(holder);
		} else if (alarmholder != null) {
			v.setTag(alarmholder);
		}
		return v;
	}

	
	class TaskHolder {
		private TextView textview_settime;
		private TextView textView_content;
		private TextView textView_istoday;
	}

	class AlarmHolder {
		private TextView textview_settime;
		private TextView textView_content;
		private TextView textView_istoday;
		private TextView textView_1;
		private TextView textView_2;
		private TextView textView_3;
		private TextView textView_4;
		private TextView textView_5;
		private TextView textView_6;
		private TextView textView_7;
	}
}
