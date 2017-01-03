package com.yongyida.robot.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.android.datetimepicker.time.TimePickerDialog.OnTimeSetListener;
import com.yongyida.robot.R;
import com.yongyida.robot.fragment.NewAddAlarmFragment;
import com.yongyida.robot.fragment.NewAddRemindFragment;
import com.yongyida.robot.widget.RobotDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddTaskActivity extends OriginalActivity{

	private static final String TAG = "AddTaskActivity";
	private RobotDialog alert;
	private onChooseListener onChooseListener;
	private Calendar calendar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_task);
		TextView mTitle = (TextView) findViewById(R.id.add_task_tile);
		calendar = Calendar.getInstance();
		if (getIntent().getStringExtra("mode").equals("remind")) {
			getFragmentManager().beginTransaction()
					.replace(R.id.content_task, new NewAddRemindFragment())
					.commit();
			mTitle.setText(R.string.remind);
		} else if (getIntent().getStringExtra("mode").equals("alarm")) {
			getFragmentManager().beginTransaction()
					.replace(R.id.content_task, new NewAddAlarmFragment())
					.commit();
			mTitle.setText(R.string.alert);
		}
		findViewById(R.id.tv_back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onChooseListener.onOver();
			}
		});
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		if (fragment instanceof onChooseListener) {
			onChooseListener = (onChooseListener) fragment;
		}

		super.onAttachFragment(fragment);

	}

	public void ondate(View view) {
		DatePickerDialog.newInstance(onDateSetListener,
				calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH)).show(getFragmentManager(),
				"datePicker");
	}

	public void ontime(View view) {
		TimePickerDialog.newInstance(onTimeSetListener,
				calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE), true).show(getFragmentManager(),
				"timePicker");
	}

	public void choose(View view) {
		List<String> types = new ArrayList<String>();
		types.add(getString(R.string.medicine));
		types.add(getString(R.string.work));
		types.add(getString(R.string.make_dinner));
		types.add(getString(R.string.have_rest));
		types.add(getString(R.string.watch_tv));
		types.add(getString(R.string.meet_people));
		types.add(getString(R.string.take_exercise));
		types.add(getString(R.string.eat));
		types.add(getString(R.string.get_up));
		alert = new RobotDialog(this);
		View v = LayoutInflater.from(this).inflate(R.layout.tasktype_dialog,
				null);
		ListView type = (ListView) v.findViewById(R.id.typelist);
		type.setVerticalScrollBarEnabled(true);
		type.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View text, int arg2,
					long arg3) {
				onChooseListener.onChoose(((TextView) text
						.findViewById(android.R.id.text1)).getText().toString());
				alert.dismiss();
			}
		});
		type.setAdapter(new ArrayAdapter<String>(this, R.layout.type, types));
		alert.addContentView(v, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		alert.showdialog();
	}

	private OnDateSetListener onDateSetListener = new OnDateSetListener() {

		@Override
		public void onDateSet(DatePickerDialog dialog, int year,
				int monthOfYear, int dayOfMonth) {
			onChooseListener.onDate(year, monthOfYear, dayOfMonth);
		}
	};

	private OnTimeSetListener onTimeSetListener = new OnTimeSetListener() {

		@Override
		public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
			onChooseListener.onTime(hourOfDay, minute);
		}
	};


	public interface onChooseListener {
		public void onChoose(String text);

		public void onDate(int year, int monthOfYear, int dayOfMonth);

		public void onTime(int hourOfDay, int minute);

		public void onOver();
	}

}
