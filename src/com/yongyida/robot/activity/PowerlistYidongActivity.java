package com.yongyida.robot.activity;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.StartUtil;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.Utils;

/**
 * Created by Administrator on 2016/10/28 0028.
 */
public class PowerlistYidongActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "PowerlistYidongActivity";
    private RelativeLayout video_chat;
    private RelativeLayout video_monitor;
    private RelativeLayout power_photo;
    private RelativeLayout power_setting;
    private RelativeLayout more;
    private TextView power_title;
    private String mMode;
    private TextView mBattery;
    private String mVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_list_yidong);
        initBase();
        Constants.USER_BACE = false;   //防止在powlist界面按一次返回 2次进入connect界面的一个标志位
    }


    private void initBase() {
        more = (RelativeLayout) findViewById(R.id.more);
        more.setOnClickListener(this);
        power_title = (TextView) findViewById(R.id.power_title);
        power_title.setOnClickListener(this);
        power_setting = (RelativeLayout) findViewById(R.id.power_setting);
        power_setting.setOnClickListener(this);
        power_setting.setOnTouchListener(ontouch);
        power_photo = (RelativeLayout) findViewById(R.id.power_photo);
        power_photo.setOnTouchListener(ontouch);
        power_photo.setOnClickListener(this);
        video_monitor = (RelativeLayout) findViewById(R.id.video_monitor);
        video_monitor.setOnClickListener(this);
        video_monitor.setOnTouchListener(ontouch);
        video_chat = (RelativeLayout) findViewById(R.id.video_chat);
        video_chat.setOnClickListener(this);
        video_chat.setOnTouchListener(ontouch);
        mBattery = ((TextView) findViewById(R.id.tv_battery));


        int battery = getIntent().getExtras().getInt("battery");
        mVersion = getIntent().getExtras().getString("version");
        setBattery(battery);
        BroadcastReceiverRegister.reg(PowerlistYidongActivity.this, new String[]{Constants.BATTERY}, mBatteryBR);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.unRegisterReceiver(mBatteryBR, this);
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

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    public void initlayout(SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
    }

    long now = 0;
    long secc = 0;
    boolean flag = true;

    public void downanimaction(View view) {
        view.setPivotX(0);
        view.setPivotY(0);
        view.invalidate();
        ObjectAnimator.ofFloat(view, "rotationX", 0.0f, -30.0f)
                .setDuration(500).start();
    }

    private View.OnTouchListener ontouch = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Drawable background = v.getBackground();
                v.setBackgroundColor(Color.DKGRAY);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.setBackgroundColor(Color.TRANSPARENT);
            }
            return false;
        }
    };



    @Override
    public void onClick(View v) {
        Bundle params = new Bundle();

        switch (v.getId()) {
            case R.id.more:
                v.setBackgroundColor(getResources().getColor(R.color.transparent));
                ToastUtil.showtomain(this, getString(R.string.waitting));
                break;
            case R.id.power_title:
                onBackPressed();
                break;
            case R.id.video_chat:
                mMode = "chat";
                Bundle bundle1 = new Bundle();
                bundle1.putString("mode", mMode);
                StartUtil.startintent(PowerlistYidongActivity.this, ControlYidongActivity.class, "no", bundle1);
                break;
            case R.id.video_monitor:
                mMode = "control";
                Bundle bundle2 = new Bundle();
                bundle2.putString("mode", mMode);
                StartUtil.startintent(PowerlistYidongActivity.this, ControlYidongActivity.class, "no", bundle2);
                break;
            case R.id.power_photo:
                StartUtil.startintent(this, PhotoActivity.class, "no");
                break;
            case R.id.power_setting:
                params.putString("flag", "main");
                params.putString("version", mVersion);
                StartUtil.startintent(this, SettingActivity.class, "no", params);
                break;
            default:
                break;
        }
    }


    @Override
    public void onBackPressed() {
        sendBroadcast(new Intent(Constants.Stop));
        Constants.USER_BACE = true;
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}
