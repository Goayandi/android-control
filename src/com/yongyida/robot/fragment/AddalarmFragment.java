package com.yongyida.robot.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.yongyida.robot.R;
import com.yongyida.robot.activity.AddTaskActivity.onChooseListener;
import com.yongyida.robot.activity.TaskRemindActivity;
import com.yongyida.robot.bean.Alarm;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.widget.SwitchButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddalarmFragment extends Fragment implements OnClickListener,
		onChooseListener {

	private Calendar calendar;
	private boolean timeflag;
	private Button alarmtime;
	private Button zhou1;
	private Button zhou2;
	private Button zhou3;
	private Button zhou4;
	private Button zhou5;
	private Button zhou6;
	private Button zhou7;
	private boolean[] bs = new boolean[] { false, false, false, false, false,
			false, false };

	private Calendar settime;
	private String state;
	private EditText edit_title;
	private EditText edit_content;
	private Alarm alarm;
	private int index;
	private static final int Sunday = 7;

	private SwitchButton switchbutton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.from(getActivity()).inflate(
				R.layout.add_alarm_fragment, null);
		state = getActivity().getIntent().getExtras().getString("state");
		Log.i("state", state);
		calendar = Calendar.getInstance();
		alarmtime = (Button) v.findViewById(R.id.alarm_time);
		edit_title = (EditText) v.findViewById(R.id.task_title_alarm);
		edit_content = (EditText) v.findViewById(R.id.task_content_alarm);
		switchbutton = (SwitchButton) v.findViewById(R.id.isaways_setting);
		switchbutton.setOnCheckedChangeListener(onCheckedChangeListener);
		zhou1 = (Button) v.findViewById(R.id.zhou1);
		zhou1.setOnClickListener(this);
		zhou2 = (Button) v.findViewById(R.id.zhou2);
		zhou2.setOnClickListener(this);
		zhou3 = (Button) v.findViewById(R.id.zhou3);
		zhou3.setOnClickListener(this);
		zhou4 = (Button) v.findViewById(R.id.zhou4);
		zhou4.setOnClickListener(this);
		zhou5 = (Button) v.findViewById(R.id.zhou5);
		zhou5.setOnClickListener(this);
		zhou6 = (Button) v.findViewById(R.id.zhou6);
		zhou6.setOnClickListener(this);
		zhou7 = (Button) v.findViewById(R.id.zhou7);
		zhou7.setOnClickListener(this);
		if (state.equals(Constants.Update)) {
			alarm = getActivity().getIntent().getParcelableExtra("task");
			edit_title.setText(alarm.getTitle());
			settime = Calendar.getInstance();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
			try {
				settime.setTime(simpleDateFormat.parse(alarm.getSettime()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (settime.get(Calendar.HOUR_OF_DAY) >= 10
					&& settime.get(Calendar.MINUTE) >= 10) {
				alarmtime.setText(settime.get(Calendar.HOUR_OF_DAY) + ":"
						+ settime.get(Calendar.MINUTE));
			} else if (settime.get(Calendar.HOUR_OF_DAY) < 10
					&& settime.get(Calendar.MINUTE) >= 10) {
				alarmtime.setText("0"+settime.get(Calendar.HOUR_OF_DAY) + ":"
						+ settime.get(Calendar.MINUTE));
			}else if(settime.get(Calendar.HOUR_OF_DAY) >= 10
					&& settime.get(Calendar.MINUTE) < 10){
				alarmtime.setText(settime.get(Calendar.HOUR_OF_DAY) + ":"
						+"0"+ settime.get(Calendar.MINUTE));		
			}else{
				alarmtime.setText("0"+settime.get(Calendar.HOUR_OF_DAY) + ":"
						+"0"+ settime.get(Calendar.MINUTE));
			}
			edit_content.setText(alarm.getContent() + "");
			if (alarm.getIsaways() == 0) {
				switchbutton.setChecked(false);
			} else {
				switchbutton.setChecked(true);
			}
//			edit_title.setText("闹钟");
			setweek(alarm.getWeek());
			timeflag = true;
			index = getActivity().getIntent().getExtras().getInt("index");
		} else {
			alarm = new Alarm();
			alarm.setIsaways(1);
			settime = Calendar.getInstance();
		}

		return v;
	}

	public void setweek(String week) {
		String[] weeks = week.split(",");
		for (int i = 0; i < weeks.length; i++) {
			switch (Integer.parseInt(weeks[i])) {
			case 1:
				bs[0] = true;
				zhou1.setBackgroundColor(Color.parseColor("#00C5CD"));
				break;
			case 2:
				bs[1] = true;
				zhou2.setBackgroundColor(Color.parseColor("#00C5CD"));
				break;
			case 3:
				bs[2] = true;
				zhou3.setBackgroundColor(Color.parseColor("#00C5CD"));
				break;
			case 4:
				bs[3] = true;
				zhou4.setBackgroundColor(Color.parseColor("#00C5CD"));
				break;
			case 5:
				bs[4] = true;
				zhou5.setBackgroundColor(Color.parseColor("#00C5CD"));
				break;
			case 6:
				bs[5] = true;
				zhou6.setBackgroundColor(Color.parseColor("#00C5CD"));
				break;
			case 7:
				bs[6] = true;
				zhou7.setBackgroundColor(Color.parseColor("#00C5CD"));
				break;
			default:
				break;
			}
		}
	}

	private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean is) {
			if (is) {
				alarm.setIsaways(1);
			} else {
				alarm.setIsaways(0);
			}
		}
	};

	@Override
	public void onOver() {
		Calendar calendar = Calendar.getInstance();
		StringBuilder stringBuilder = new StringBuilder();
		// {星期日，星期一，星期二，星期三，星期四，星期五，星期六} === {1,2,3,4,5,6,7}
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
				stringBuilder.append((i + 1) + ",");
			}
		}
		
		if(!isChooseWeek){
			ToastUtil.showtomain(getActivity(), getActivity().getString(R.string.choose_week));
			return;
		}

		Log.i("settime", settime.get(Calendar.DATE) + "DATE");
		Log.i("settime", settime.get(Calendar.HOUR_OF_DAY) + "HOUR_OF_DAY");
		Log.i("settime", settime.get(Calendar.MINUTE) + "MINUTE");
		Log.i("settime", settime.get(Calendar.WEEK_OF_MONTH) + "WEEK_OF_MONTH");

		if (!timeflag) {
			ToastUtil.showtomain(getActivity(), getActivity().getString(R.string.choose_time));
			return;
		}
		if(alarm.getIsaways() == 0){
			if(!fg){        //如果星期都是
				ToastUtil.showtomain(getActivity(), getActivity().getString(R.string.exist_time_expire));
				return;
			}
		}

		String zhou = null;
		if (stringBuilder.length() != 0) {
			zhou = stringBuilder.toString();
			zhou = zhou.substring(0, zhou.length() - 1);
		} else {
			return;
		}
		String title = edit_title.getText().toString().trim();
		String content = edit_content.getText().toString().trim();
		// if ("".equals(title)) {
		// ToastUtil.showtomain(getActivity(), "请输入标题");
		// return;
		// }
		// if ("".equals(content)) {
		// ToastUtil.showtomain(getActivity(), "请输入内容");
		// return;
		// }
		alarm.setContent(content);
		alarm.setTitle(title);
		alarm.setWeek(zhou);
		alarm.setSettime(alarmtime.getText().toString());
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

	@Override
	public void onDate(int year, int monthOfYear, int dayOfMonth) {

	}

	@Override
	public void onTime(int hourOfDay, int minute) {
		settime.set(Calendar.HOUR_OF_DAY, hourOfDay);
		settime.set(Calendar.MINUTE, minute);
		timeflag = true;
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
		alarmtime.setText(hour + ":" + min);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.zhou1:
			toggle_click(v, bs[0]);
			if (bs[0]) {
				bs[0] = false;
			} else {
				bs[0] = true;
			}
			break;
		case R.id.zhou2:
			toggle_click(v, bs[1]);
			if (bs[1]) {
				bs[1] = false;
			} else {
				bs[1] = true;
			}
			break;
		case R.id.zhou3:
			toggle_click(v, bs[2]);
			if (bs[2]) {
				bs[2] = false;
			} else {
				bs[2] = true;
			}
			break;
		case R.id.zhou4:
			toggle_click(v, bs[3]);
			if (bs[3]) {
				bs[3] = false;
			} else {
				bs[3] = true;
			}
			break;
		case R.id.zhou5:
			toggle_click(v, bs[4]);
			if (bs[4]) {
				bs[4] = false;
			} else {
				bs[4] = true;
			}
			break;
		case R.id.zhou6:
			toggle_click(v, bs[5]);
			if (bs[5]) {
				bs[5] = false;
			} else {
				bs[5] = true;
			}
			break;
		case R.id.zhou7:
			toggle_click(v, bs[6]);
			if (bs[6]) {
				bs[6] = false;
			} else {
				bs[6] = true;
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onChoose(String text) {
		edit_title.setText(text);

	}

	public void toggle_click(View v, boolean b) {
		if (b) {
			v.setBackgroundColor(Color.parseColor("#40E0D0"));
		} else {
			v.setBackgroundColor(Color.parseColor("#00C5CD"));
		}
	}


}
