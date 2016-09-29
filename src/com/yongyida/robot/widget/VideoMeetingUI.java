package com.yongyida.robot.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/1 0001.
 */
public class VideoMeetingUI extends RelativeLayout {
    private static final String TAG = "VideoMeetingUI";
    private Context mContext;
    private FrameLayout mFrameLayout;
    private List<MeetingSurfaceView> mSurfaceViewList;  //用于装小窗口显示的surfaceView
    private boolean isFullScreenOccupy = false;

    public VideoMeetingUI(Context context) {
        this(context, null, 0);
    }

    public VideoMeetingUI(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoMeetingUI(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    private void init() {
        Log.i(TAG, "init");
        mSurfaceViewList = new ArrayList<MeetingSurfaceView>();

        /*初始化全屏的surfaceView布局*/
        mFrameLayout = new FrameLayout(mContext);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                (int)(Utils.getScreenHeight((Activity)mContext) * MeetingSurfaceView.WIDTH_HEIGHT_RATIO),
                LayoutParams.MATCH_PARENT);
        mFrameLayout.setLayoutParams(params);
        addView(mFrameLayout);
    }

    /**
     * 初始化房间
     * @param surfaceView 本地视频
     * @param account     本地账号
     */
    public void initRoom(SurfaceView surfaceView, String account){
        MeetingSurfaceView meetingSurfaceView = new MeetingSurfaceView(surfaceView, account, 0, mContext);
        meetingSurfaceView.getSurfaceView().setZOrderOnTop(true);
        meetingSurfaceView.getSurfaceView().setZOrderMediaOverlay(true);
        mSurfaceViewList.add(meetingSurfaceView);
        addView(surfaceView);
    }

    /**
     * 添加房间用户
     * @param surfaceView
     * @param account
     */
    public synchronized void addRoomUser(SurfaceView surfaceView, String account) {
        if (mSurfaceViewList.size() == MeetingSurfaceView.TOTAL_SMALL_WINDOW) {
            ToastUtil.showtomain(mContext, "房间人数达到上限");
            return;
        }
        if (isFullScreenOccupy) {
            addSmallWindowUser(surfaceView, account);
        } else {
            addFullScreenUser(surfaceView, account);
        }
        surfaceView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToFullScreen((SurfaceView) v);
            }
        });
        surfaceView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ToastUtil.showtomain(mContext, ((MeetingSurfaceView) v.getTag()).getAccount());
                return true;
            }
        });
    }

    /**
     * 小屏幕显示用户视频
     * @param surfaceView
     * @param account
     */
    private void addSmallWindowUser(SurfaceView surfaceView, String account){
        MeetingSurfaceView meetingSurfaceView = new MeetingSurfaceView(surfaceView, account,
                mSurfaceViewList.size(), mContext);
        surfaceView.setZOrderOnTop(true);
        surfaceView.setZOrderMediaOverlay(true);
        mSurfaceViewList.add(meetingSurfaceView);
        addView(surfaceView);
    }

    /**
     * 全屏显示用户视频
     * @param surfaceView
     * @param account
     */
    private void addFullScreenUser(SurfaceView surfaceView, String account){
        mFrameLayout.removeAllViews();
        MeetingSurfaceView meetingSurfaceView = new MeetingSurfaceView(surfaceView, account, mContext);
        surfaceView.setZOrderOnTop(false);
        surfaceView.setZOrderMediaOverlay(false);
        mFrameLayout.addView(meetingSurfaceView.getSurfaceView());
        isFullScreenOccupy = true;
    }

    public void smallWindowOnTop(){
        if (mSurfaceViewList != null && mSurfaceViewList.size() != 0) {
            for (MeetingSurfaceView meetingSurfaceView : mSurfaceViewList) {
                meetingSurfaceView.getSurfaceView().bringToFront();
            }
        }
    }

    public synchronized void removeRoomUser(String account){
        MeetingSurfaceView meetingSurfaceView = getViewByAccout(account);
        if (meetingSurfaceView == null) {
            ToastUtil.showtomain(mContext, "房间没有此用户");
            return;
        }
        if(meetingSurfaceView.getFullScreenState()){
            mFrameLayout.removeAllViews();
            if (mSurfaceViewList.size() > 1) {
                MeetingSurfaceView meetingSurfaceView1 = getViewByLocation(mSurfaceViewList.size() - 1);
                meetingSurfaceView1.setSurfaceViewFullScreen();
                mSurfaceViewList.remove(meetingSurfaceView1);
                removeView(meetingSurfaceView1.getSurfaceView());
                mFrameLayout.addView(meetingSurfaceView1.getSurfaceView());
            } else {
                isFullScreenOccupy = false;
            }

        } else {
            int location = meetingSurfaceView.getLocation();
            removeView(meetingSurfaceView.getSurfaceView());
            mSurfaceViewList.remove(meetingSurfaceView);
            if (mSurfaceViewList.size() > 1) {
                for (MeetingSurfaceView meetingSurfaceView1 : mSurfaceViewList) {
                    if (meetingSurfaceView1.getLocation() > location) {
                        meetingSurfaceView1.moveFoward();
                    }
                }
            }

        }
    }

    public synchronized void switchToFullScreen(SurfaceView surfaceView){
        MeetingSurfaceView meetingSurfaceView = (MeetingSurfaceView) surfaceView.getTag();
        if (!meetingSurfaceView.getFullScreenState() && mFrameLayout.getChildCount() != 0) {
            int location = meetingSurfaceView.setSurfaceViewFullScreen();
            MeetingSurfaceView fullScreenView = (MeetingSurfaceView)(mFrameLayout.getChildAt(0).getTag());
            fullScreenView.setSurfaceViewSmallWindow(location);
            removeView(meetingSurfaceView.getSurfaceView());
            mFrameLayout.removeView(fullScreenView.getSurfaceView());
            mFrameLayout.addView(meetingSurfaceView.getSurfaceView());
            addView(fullScreenView.getSurfaceView());
            mSurfaceViewList.remove(meetingSurfaceView);
            mSurfaceViewList.add(fullScreenView);
            fullScreenView.getSurfaceView().setZOrderOnTop(true);
            fullScreenView.getSurfaceView().setZOrderMediaOverlay(true);
            surfaceView.setZOrderOnTop(false);
            surfaceView.setZOrderMediaOverlay(false);
        }
    }

    public boolean getAccoutExistState(String account) {
        if (getViewByAccout(account) != null) {
            return true;
        }
        return false;
    }

    private MeetingSurfaceView getViewByAccout(String account){
        if (isFullScreenOccupy) {
            MeetingSurfaceView meetingSurfaceView = (MeetingSurfaceView)(mFrameLayout.getChildAt(0).getTag());
            if (account.equals(meetingSurfaceView.getAccount())) {
                return meetingSurfaceView;
            }
        }
        if (mSurfaceViewList != null && mSurfaceViewList.size() != 0) {
            for (MeetingSurfaceView meetingSurfaceView : mSurfaceViewList) {
                if (account.equals(meetingSurfaceView.getAccount())) {
                    return meetingSurfaceView;
                }
            }
        }
        return null;
    }

    private MeetingSurfaceView getViewByLocation(int location) {
        if (mSurfaceViewList != null && mSurfaceViewList.size() != 0) {
            for (MeetingSurfaceView meetingSurfaceView : mSurfaceViewList) {
                if (location == meetingSurfaceView.getLocation()) {
                    return meetingSurfaceView;
                }
            }
        }
        return null;
    }

    public int getTotalUserNum(){
        return mSurfaceViewList.size() + mFrameLayout.getChildCount();
    }

}
