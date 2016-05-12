package com.yongyida.robot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.adapter.CallHistoryAdapter;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.sdk.CallHistory;
import com.yongyida.robot.video.sdk.YYDSDKHelper;

import java.util.List;

/**
 * Created by Administrator on 2016/5/11 0011.
 */
public class DialByNumberActivity extends BaseVideoActivity implements OnClickListener{
    public static final String TAG = "DialByNumberActivity";

    private ListView mLvHistory;
    private List<CallHistory> mCallHistoryList;
    private String mNumberType;
    private TextView mTvNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dial_number);
        initView();
    }

    @Override
    protected void initView() {
        initHistoryList();
        initDialPlate();
    }

    private void initHistoryList() {
        mCallHistoryList = YYDSDKHelper.getInstance().getCallHistoryList();
        mLvHistory = (ListView) findViewById(R.id.friend_listview);
        mLvHistory.setAdapter(new CallHistoryAdapter(this, mCallHistoryList));
        mLvHistory.setOnItemClickListener(new OnItemClickListenerImpl());
    }

    private void initDialPlate() {
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        mTvNumber = (TextView) findViewById(R.id.tv_number);
        findViewById(R.id.btn_back).setOnClickListener(backClick);
        findViewById(R.id.btn_robotid).setOnClickListener(numberTypeClick);
        findViewById(R.id.btn_robotid).callOnClick();
        findViewById(R.id.btn_roomid).setOnClickListener(numberTypeClick);
        findViewById(R.id.btn_phonenumber).setOnClickListener(numberTypeClick);
        findViewById(R.id.btn_backspace).setOnClickListener(backSpaceClick);
        findViewById(R.id.btn_number_one).setOnClickListener(numberClick);
        findViewById(R.id.btn_number_two).setOnClickListener(numberClick);
        findViewById(R.id.btn_number_three).setOnClickListener(numberClick);
        findViewById(R.id.btn_number_four).setOnClickListener(numberClick);
        findViewById(R.id.btn_number_five).setOnClickListener(numberClick);
        findViewById(R.id.btn_number_six).setOnClickListener(numberClick);
        findViewById(R.id.btn_number_seven).setOnClickListener(numberClick);
        findViewById(R.id.btn_number_eight).setOnClickListener(numberClick);
        findViewById(R.id.btn_number_nine).setOnClickListener(numberClick);
        findViewById(R.id.btn_number_asterisk).setOnClickListener(numberClick);
        findViewById(R.id.btn_number_zero).setOnClickListener(numberClick);
        findViewById(R.id.btn_number_pound).setOnClickListener(numberClick);
        findViewById(R.id.btn_dial).setOnClickListener(dialClick);
    }

    @Override
    public void onClick(View v) {

    }

    private class OnItemClickListenerImpl implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CallHistory history = mCallHistoryList.get(position);
            meetingInvite(history.getUser().getRole(), history.getUser().getId());
        }
    }

    private OnClickListener backClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };

    private OnClickListener numberTypeClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.requestFocusFromTouch();
            mNumberType = view.getTag().toString();
            log.d(TAG, "NumberType: " + mNumberType);
        }
    };

    private OnClickListener backSpaceClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            String numbers = mTvNumber.getText().toString();
            if (numbers.length() > 0) {
                numbers = numbers.substring(0, numbers.length() - 1);
                mTvNumber.setText(numbers);
            }
        }
    };

    private OnClickListener numberClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mTvNumber.getText().toString().length() < 11) {
                mTvNumber.setText(mTvNumber.getText().toString() + view.getTag());
            }
        }
    };

    private OnClickListener dialClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            String strNumber = mTvNumber.getText().toString();
            if (strNumber.length() > 0) {
                try {
                    int number = Integer.valueOf(strNumber);
                    meetingInvite(mNumberType, number);
                }
                catch (Exception e) {
                    log.e(TAG, "Number error, " + e);
                }
            }
        }
    };

    public void meetingInvite(String numberType, long number) {

        Intent intent = new Intent(this, InviteDialActivity.class);
        intent.putExtra("numbertype", numberType);
        intent.putExtra("number", number);
        intent.putExtra("username", numberType + number);
        startActivity(intent);
    }
}
