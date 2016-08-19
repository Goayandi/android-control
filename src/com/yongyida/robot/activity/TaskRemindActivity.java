package com.yongyida.robot.activity;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.yongyida.robot.R;
import com.yongyida.robot.bean.Alarm;
import com.yongyida.robot.bean.Remind;
import com.yongyida.robot.biz.Biz;
import com.yongyida.robot.fragment.TaskFragment;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.StartUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class TaskRemindActivity extends BaseActivity implements OnClickListener {

	public static final int REMIND = 1;
	public static final int ALARM = 2;
	
	private TaskFragment remind;
	private OnFragmentRefresh onFragmentRefresh;
	private RelativeLayout reminditem;
	private RelativeLayout alarmitem;
	private FragmentManager manager;
	private int flag = REMIND;

	ImageView iv_remind;
	ImageView iv_clock;

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub			
			switch(msg.what){
			case REMIND:							
				iv_remind.setBackgroundResource(R.drawable.tx60);
				iv_clock.setBackgroundResource(R.drawable.nz);
				break;
			case ALARM:			
				iv_clock.setBackgroundResource(R.drawable.nz60);
				iv_remind.setBackgroundResource(R.drawable.tx);
				break;
			}
			super.handleMessage(msg);			
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private void initView() {
		// TODO Auto-generated method stub
		iv_remind = (ImageView) findViewById(R.id.reminditem_img);
		iv_clock = (ImageView) findViewById(R.id.alarmitem_img);	
		iv_remind.setBackgroundResource(R.drawable.tx60);
	}

	public void back(View view) {
		finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.reminditem:
			flag = REMIND;			
			mHandler.sendEmptyMessage(REMIND);
			onFragmentRefresh.OnRefresh(list_task);
			break;
		case R.id.alarmitem:
			flag = ALARM;		
			mHandler.sendEmptyMessage(ALARM);
			onFragmentRefresh.OnRefresh(alarms);
			break;
		default:
			break;
		}

	}

	public Timer time = new Timer();
	BroadcastReceiver refreshUI = new BroadcastReceiver() {
		public void onReceive(android.content.Context arg0, Intent intent) {
			if (intent.getAction().equals(Constants.Result)) {
				list_task.clear();
				alarms.clear();
				String result = intent.getStringExtra("result");
				Log.i("Result", result);
				try {
					Biz.adapter_task(result, list_task, alarms);
					if (flag == REMIND) {
						onFragmentRefresh.OnRefresh(list_task);
					} else if (flag == ALARM) {
						onFragmentRefresh.OnRefresh(alarms);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	};

	public void onAttachFragment(android.app.Fragment fragment) {
		onFragmentRefresh = (OnFragmentRefresh) fragment;
	};

	@Override
	protected void onResume() {
		if (list_task != null) {
			list_task.clear();
		}
		if (alarms != null) {
			alarms.clear();
		}
		sendBroadcast(new Intent(Constants.Task_Query));
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	private List<Remind> list_task = new ArrayList<Remind>();
	private List<Alarm> alarms = new ArrayList<Alarm>();

	public void add(View view) {
		Bundle params = new Bundle();
		params.putString("state", Constants.Add);
		if (flag == ALARM) {
			params.putString("mode", "alarm");
		} else if (flag == REMIND) {
			params.putString("mode", "remind");
		}
		params.putInt("flag", flag);
		StartUtil.startintentforresult(this, AddTaskActivity.class, params,
				Constants.add_RequestCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.update_RequestCode
				&& resultCode == Constants.IS_OK) {
			;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public int returnflag() {
		return flag;
	}

	@Override
	protected void onDestroy() {
		if (refreshUI != null) {
			unregisterReceiver(refreshUI);
		}
		super.onDestroy();
	}

	@Override
	public void initlayout(OnRefreshListener onRefreshListener) {
		setContentView(R.layout.activity_task_remind);
		initView();
		remind = new TaskFragment();
		reminditem = (RelativeLayout) findViewById(R.id.reminditem);
		reminditem.setOnClickListener(this);
		alarmitem = (RelativeLayout) findViewById(R.id.alarmitem);
		alarmitem.setOnClickListener(this);
		manager = getFragmentManager();
		manager.beginTransaction().replace(R.id.task_content, remind).commit();
		BroadcastReceiverRegister.reg(this, new String[] { Constants.Result },
				refreshUI);
	}

	public interface OnFragmentRefresh {
		public void OnRefresh(List<?> tasks);
	}
}
