package com.yongyida.robot.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.layout.utils.PercentRelativeLayout;
import com.yongyida.robot.R;

import java.util.List;

/**
 * Created by Administrator on 2016/3/30 0030.
 */
public class MeetingActivity extends BaseActivity implements View.OnClickListener{

    private FrameLayout mFrameLayout;
    private Button mSwitchCameraButton;
    private Button mMuteButton;
    private Button mSwitchVoiceButton;
    private PercentRelativeLayout mControlPRL;
    private Button mInviteButton;
    private Button mTrancribeButton;
    private Button mScreenshotButton;
    private Button mHangUpButton;
    private PercentRelativeLayout mInvitePRL;
    private RecyclerView mRecycleView;
    private Button mCancelButton;
    private Button mInviteConfirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        initView();
    }

    private void initView() {
        mFrameLayout = (FrameLayout) findViewById(R.id.view_container);

        mSwitchCameraButton = (Button) findViewById(R.id.bt_switch_camera);
        mSwitchCameraButton.setOnClickListener(this);
        mMuteButton = (Button) findViewById(R.id.bt_mute);
        mMuteButton.setOnClickListener(this);
        mSwitchVoiceButton = (Button) findViewById(R.id.bt_switch_voice);
        mSwitchVoiceButton.setOnClickListener(this);

        mControlPRL = (PercentRelativeLayout) findViewById(R.id.rl_control);
        mInviteButton = (Button) findViewById(R.id.bt_invite);
        mInviteButton.setOnClickListener(this);
        mTrancribeButton = (Button) findViewById(R.id.bt_transcribe);
        mTrancribeButton.setOnClickListener(this);
        mScreenshotButton = (Button) findViewById(R.id.bt_screenshot);
        mScreenshotButton.setOnClickListener(this);
        mHangUpButton = (Button) findViewById(R.id.bt_hang_up);
        mHangUpButton.setOnClickListener(this);

        mInvitePRL = (PercentRelativeLayout) findViewById(R.id.rl_invite);
        mRecycleView = (RecyclerView) findViewById(R.id.recycler_view);
        mCancelButton = (Button) findViewById(R.id.bt_cancel);
        mCancelButton.setOnClickListener(this);
        mInviteConfirmButton = (Button) findViewById(R.id.bt_invite_confirm);
        mInviteConfirmButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

    }

    class MyRecycleViewAdapter extends RecyclerView.Adapter<MyRecycleViewAdapter.MyViewHolder>{
        private List data;
        public MyRecycleViewAdapter(List data){
            this.data = data;
        }

        @Override
        public MyRecycleViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(MeetingActivity.this).inflate(R.layout.item_rv_video, viewGroup , false));
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyRecycleViewAdapter.MyViewHolder viewHolder, int i) {
           // viewHolder.tv.setText();
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{
            TextView tv;
            ImageView iv;
            LinearLayout ll;
            CheckBox cb;
            public MyViewHolder(View itemView) {
                super(itemView);
                tv = ((TextView) itemView.findViewById(R.id.tv));
                iv = ((ImageView) itemView.findViewById(R.id.iv));
                ll = ((LinearLayout) itemView.findViewById(R.id.ll));
                cb = ((CheckBox) itemView.findViewById(R.id.cb));
            }
        }
    }

    @Override
    public void initlayout(SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
    }
}
