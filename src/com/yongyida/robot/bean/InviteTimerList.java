package com.yongyida.robot.bean;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/29 0029.
 */
public class InviteTimerList {
    private List<InviteTimeoutTimer> mList;
    public InviteTimerList(){
        mList = new ArrayList<InviteTimeoutTimer>();
    }
    public void addInviter(String inviter, InviteTimeoutTimer.OnInviteResponseListener listener){
        if (!contain(inviter)) {
            addTimer(inviter, listener);

        }
    }

    public void removeInviter(String inviter) {
        if (contain(inviter)) {
            removeTimer(inviter);
        }
    }

    private boolean contain(String inviter){
        if (mList == null || mList.size() == 0 || TextUtils.isEmpty(inviter)) {
            return false;
        }
        for(InviteTimeoutTimer inviteTimeoutTimer : mList) {
            if (inviter.equals(inviteTimeoutTimer.getInviter())) {
                return true;
            }
        }
        return false;
    }

    private void addTimer(String inviter, InviteTimeoutTimer.OnInviteResponseListener listener) {
        if (!contain(inviter)) {
            InviteTimeoutTimer inviteTimeoutTimer = new InviteTimeoutTimer(inviter, listener);
            inviteTimeoutTimer.startTimer();
            mList.add(inviteTimeoutTimer);
        }
    }

    private void removeTimer(String inviter){
        if (mList == null || mList.size() == 0 || TextUtils.isEmpty(inviter)) {
            return;
        }
        for(InviteTimeoutTimer inviteTimeoutTimer : mList) {
            if (inviter.equals(inviteTimeoutTimer.getInviter())) {
                inviteTimeoutTimer.cancelTimer();
                mList.remove(inviteTimeoutTimer);
            }
        }
    }
}
