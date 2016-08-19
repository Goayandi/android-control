package com.yongyida.robot.bean;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/7/29 0029.
 */
public class InviteTimeoutTimer {
    private Timer mTimer;
    private String mInviter;
    private TimerTask mTimerTask;
    private int mCount;
    private OnInviteResponseListener mListener;
    private final static int MAX_COUNT = 25;
    public final static int RESPONSE_SUCCESS = 1;
    public final static int RESPONSE_TIMEOUT = 2;
    public InviteTimeoutTimer(String inviter, OnInviteResponseListener listener){
        mInviter = inviter;
        mTimer = new Timer();
        mCount = 0;
        mListener = listener;
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mCount < MAX_COUNT) {
                    mCount++;
                } else {
                    if (mTimer != null) {
                        mListener.inviteTimeout();
                        mTimer.cancel();
                        mCount = 0;
                    }
                }
            }
        };
    }

    public String getInviter(){
        return mInviter;
    }

    public void startTimer(){
        if (mTimer != null) {
            mTimer.schedule(mTimerTask, 1000, 1000);
        }
    }

    public void cancelTimer(){
        if (mTimer != null) {
            mTimer.cancel();
            mCount = 0;
        }
    }

    public interface OnInviteResponseListener{
        void inviteTimeout();
    }
}
