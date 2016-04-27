package com.yongyida.robot.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.layout.utils.PercentRelativeLayout;
import com.yongyida.robot.R;

/**
 * Created by Administrator on 2016/4/25 0025.
 */
public class ShantuEntranceActivity extends Activity implements OnClickListener {
	private ImageView mDoorIV;
	private LinearLayout mLL;
	private PercentRelativeLayout mLightSwitchPRL;
	private PercentRelativeLayout mDoorSwitchPRL;
	private PercentRelativeLayout mLightManagerPRL;
	private PercentRelativeLayout mWindowPRL;
	private PercentRelativeLayout mSmokePRL;
	private PercentRelativeLayout mWasiPRL;
	private AnimationDrawable mAnimationDrawable;
	private Timer timer;
	private TextView mTitleTV;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shantu_entrance);
        mDoorIV = (ImageView) findViewById(R.id.iv_door);
        mLL = (LinearLayout) findViewById(R.id.ll_tmp);
        mLightSwitchPRL = (PercentRelativeLayout) findViewById(R.id.video_chat);
        mLightSwitchPRL.setOnClickListener(this);
        mDoorSwitchPRL = (PercentRelativeLayout) findViewById(R.id.power_photo);
        mDoorSwitchPRL.setOnClickListener(this);
        mLightManagerPRL = (PercentRelativeLayout) findViewById(R.id.video_monitor);
        mLightManagerPRL.setOnClickListener(this);
        mWindowPRL = (PercentRelativeLayout) findViewById(R.id.power_task);
        mWindowPRL.setOnClickListener(this);
        mSmokePRL = (PercentRelativeLayout) findViewById(R.id.power_setting);
        mSmokePRL.setOnClickListener(this);
        mWasiPRL = (PercentRelativeLayout) findViewById(R.id.more);
        mWasiPRL.setOnClickListener(this);
        mTitleTV = (TextView) findViewById(R.id.power_title);
        mTitleTV.setOnClickListener(this);
        
        mDoorIV.setImageResource(R.drawable.door_animation);
		mAnimationDrawable = (AnimationDrawable) mDoorIV.getDrawable(); 
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.power_title:
			finish();
			break;

		default:
			if (timer != null) {
				timer.cancel();
			}
			timer = new Timer();
			mDoorIV.setVisibility(View.VISIBLE);
			mLL.setVisibility(View.GONE);
			mAnimationDrawable.stop();
			mAnimationDrawable.start();
	        timer.schedule(new MyTimerTask(), 3000);
			break;
		}
		
	}
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				mDoorIV.setVisibility(View.GONE);
				mLL.setVisibility(View.VISIBLE);
				mAnimationDrawable.stop();
			}
		};
	};
	
	private class MyTimerTask extends TimerTask{

		@Override
		public void run() {
			mHandler.sendEmptyMessage(1);
		}
		
	}
}
