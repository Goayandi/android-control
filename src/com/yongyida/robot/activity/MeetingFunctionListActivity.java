package com.yongyida.robot.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.ronglianyun.SDKCoreHelper;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.StartUtil;
import com.yongyida.robot.utils.ToastUtil;
import com.yuntongxun.ecsdk.ECDevice;

/**
 * Created by Administrator on 2016/6/16 0016.
 */
public class MeetingFunctionListActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MeetingFunctionListA";
    private RelativeLayout mFunction1RL;
    private RelativeLayout mFunction2RL;
    private RelativeLayout mFunction3RL;
    private RelativeLayout mFunction4RL;
    private RelativeLayout mFunction5RL;
    private RelativeLayout mFunction6RL;
    private TextView power_title;
    private Handler mHandler = new Handler();
    private TextView mBattery;
    private String mVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_function_list);
        init();
    }

    private void init() {
        power_title = (TextView) findViewById(R.id.power_title);
        power_title.setOnClickListener(this);

        mFunction1RL = (RelativeLayout) findViewById(R.id.prl1);
        mFunction1RL.setOnClickListener(this);
        mFunction1RL.setOnTouchListener(ontouch);
        mFunction2RL = (RelativeLayout) findViewById(R.id.prl2);
        mFunction2RL.setOnClickListener(this);
        mFunction2RL.setOnTouchListener(ontouch);
        mFunction3RL = (RelativeLayout) findViewById(R.id.prl3);
        mFunction3RL.setOnClickListener(this);
        mFunction3RL.setOnTouchListener(ontouch);
        mFunction4RL = (RelativeLayout) findViewById(R.id.prl4);
        mFunction4RL.setOnClickListener(this);
        mFunction4RL.setOnTouchListener(ontouch);
        mFunction5RL = (RelativeLayout) findViewById(R.id.prl5);
        mFunction5RL.setOnClickListener(this);
        mFunction5RL.setOnTouchListener(ontouch);
        mFunction6RL = (RelativeLayout) findViewById(R.id.prl6);
        mFunction6RL.setOnClickListener(this);
        mFunction6RL.setOnTouchListener(ontouch);
        mBattery = ((TextView) findViewById(R.id.tv_battery));
        int battery = getIntent().getExtras().getInt("battery");
        mVersion = getIntent().getExtras().getString("version");
        setBattery(battery);
        BroadcastReceiverRegister.reg(this, new String[]{Constants.BATTERY}, mBatteryBR);
    }

    private BroadcastReceiver mBatteryBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int ret = intent.getIntExtra("ret", -1);
            int battery = intent.getIntExtra("battery", -1);
            if (ret == 0) {
                setBattery(battery);
            }
        }
    };

    /**
     * 设置电池的电量
     * @param battery
     */
    private void setBattery(int battery) {
        if (battery < 10) {
            mBattery.setTextColor(getResources().getColor(R.color.red));
        } else {
            mBattery.setTextColor(getResources().getColor(R.color.white));
        }
        mBattery.setText(getString(R.string.battery) + battery + "%");
    }

    private View.OnTouchListener ontouch = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setBackgroundColor(Color.DKGRAY);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.setBackgroundColor(Color.TRANSPARENT);
            }
            return false;
        }
    };

    /**
     * 初始化容联云
     */
    private void initRonglianyun() {
        SDKCoreHelper.init(this);
    }

    private String mUnreleaseMeetingNo;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == TestMeetingActivity.RELEASE_MEETING_FAIL_CODE) {
            mUnreleaseMeetingNo = data.getStringExtra(Constants.UNRELEASE_MEETING_NO);
        } else if (resultCode == TestMeetingActivity.REQUEST_SERVER_FAIL_CODE) {
            initRonglianyun();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prl1:
                if (SDKCoreHelper.getConnectState() == ECDevice.ECConnectState.CONNECT_SUCCESS) {
                    Intent intent = new Intent(MeetingFunctionListActivity.this, TestMeetingActivity.class);
                    if (!TextUtils.isEmpty(mUnreleaseMeetingNo)) {
                        intent.putExtra(Constants.TO_MEETING_NO, mUnreleaseMeetingNo);
                        mUnreleaseMeetingNo = "";
                    }
                    startActivity(intent);
                } else {
                    initRonglianyun();
                    ToastUtil.showtomain(this, "网络连接异常");
                }
                break;
            case R.id.prl2:
                StartUtil.startintent(MeetingFunctionListActivity.this, MonitoringActivity.class, "no");
                break;
            case R.id.prl3:
                StartUtil.startintent(MeetingFunctionListActivity.this, TaskRemindActivity.class, "no");
                break;
            case R.id.prl4:
                StartUtil.startintent(MeetingFunctionListActivity.this, PhotoActivity.class, "no");
                break;
            case R.id.prl5:
                break;
            case R.id.prl6:
                Bundle params = new Bundle();
                params.putString("flag", "main");
                params.putString("version", mVersion);
                StartUtil.startintent(MeetingFunctionListActivity.this, SettingActivity.class, "no", params);
                break;
            case R.id.power_title:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
