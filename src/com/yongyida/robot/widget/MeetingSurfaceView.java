package com.yongyida.robot.widget;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.yongyida.robot.utils.Utils;

/**
 * Created by Administrator on 2016/9/1 0001.
 */
public class MeetingSurfaceView {
    public static final int TOTAL_SMALL_WINDOW = 4;
    private static final String TAG = "MeetingSurfaceView";
    private String account;  //用户账号
    private int location;    //小窗口的位置 0,1,2,3
    private SurfaceView surfaceView;
    private Context context;
    public final static float WIDTH_HEIGHT_RATIO = 320f / 240;
    private boolean isFullScreen;
    private int mWidth;
    private int mHeight;
    /**
     * 用于创建小窗口SurfaceView
     * @param surfaceView
     * @param account
     * @param location
     * @param context
     */
    public MeetingSurfaceView(SurfaceView surfaceView, String account, int location, Context context) {
        this.surfaceView = surfaceView;
        this.account = account;
        this.location = location;
        this.context = context;
        surfaceView.setTag(this);
        setSmallWindowLayout(location);
    }

    /**
     * 用于创建全屏SurfaceView
     * @param surfaceView
     * @param account
     * @param context
     */
    public MeetingSurfaceView(SurfaceView surfaceView, String account, Context context) {
        this.surfaceView = surfaceView;
        this.account = account;
        this.context = context;
        surfaceView.setTag(this);
        setFullScreenLayout();
    }


    /**
     * 小视图向前移一格  （前面有视图被移除 ）
     */
    public void moveFoward(){
        location = location - 1;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                mWidth,
                mHeight);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.setMargins(mWidth * location, 0, 0, 0);
        surfaceView.setLayoutParams(params);
    }

    public void setFullScreenLayout(){
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        surfaceView.setLayoutParams(params);
        isFullScreen = true;
    }

    public void setSmallWindowLayout(int location){
        int screenWidth = Utils.getScreenWidth((Activity)context);
        mWidth = screenWidth / TOTAL_SMALL_WINDOW;
        mHeight = (int) (mWidth / WIDTH_HEIGHT_RATIO);
        Log.i(TAG, "w:" + mWidth + ",h:" + mHeight);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                mWidth, mHeight);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.setMargins(screenWidth / TOTAL_SMALL_WINDOW * location, 0, 0, 0);
        surfaceView.setLayoutParams(params);
        isFullScreen = false;
    }

    public boolean getFullScreenState(){
        return isFullScreen;
    }

    public void clearSurfaceView(){
    }

    public int setSurfaceViewFullScreen(){
        setFullScreenLayout();
        return location;
    }

    public void setSurfaceViewSmallWindow(int location){
        setSmallWindowLayout(location);
        this.location = location;
    }

    public SurfaceView getSurfaceView(){
        return surfaceView;
    }

    public String getAccount() {
        return account;
    }

    public int getLocation(){
        return location;
    }
}
