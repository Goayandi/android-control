package com.yongyida.robot.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yongyida.robot.R;

/**
 * Created by Administrator on 2016/4/11 0011.
 */
public class FunctionActivity extends BaseVideoActivity implements View.OnClickListener {

    private RelativeLayout video_chat;
    private RelativeLayout video_monitor;
    private RelativeLayout power_task;
    private RelativeLayout power_photo;
    private RelativeLayout power_setting;
    private RelativeLayout more;
    private TextView power_title;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_list_y20);
        initView();
    }

    @Override
    protected void initView() {
        more = (RelativeLayout) findViewById(R.id.more);
        more.setOnClickListener(this);
        more.setOnTouchListener(ontouch);
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
        power_task = (RelativeLayout) findViewById(R.id.power_task);
        power_task.setOnClickListener(this);
        power_task.setOnTouchListener(ontouch);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_chat:
                startActivity(new Intent(FunctionActivity.this, MeetingActivity.class));
                break;
        }
    }
}
