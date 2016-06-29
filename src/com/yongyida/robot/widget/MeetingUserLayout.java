package com.yongyida.robot.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yongyida.robot.R;

/**
 * Created by Administrator on 2016/6/13 0013.
 */
public class MeetingUserLayout extends FrameLayout{
    private TextView mTextView;
    private SurfaceView mSurfaceView;
    private Context mContext;
    private OnSurfaceViewClickListener mListener;

    public MeetingUserLayout(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    private void init() {
        mSurfaceView = new SurfaceView(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mSurfaceView.setLayoutParams(params);
        mTextView = new TextView(mContext);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setTextColor(getResources().getColor(R.color.red));
        addView(mSurfaceView);
        addView(mTextView);
        mSurfaceView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onSurfaceViewClick(v);
                }
            }
        });
    }

    public void setTextViewText(String text) {
        mTextView.setText(text);
        mTextView.setVisibility(VISIBLE);
    }

    public void invisiableTextView(){
        mTextView.setText("");
        mTextView.setVisibility(GONE);
    //    mSurfaceView.setZOrderOnTop(true);
    }

    public String getTextViewText(){
        return mTextView.getText().toString();
    }

    public boolean whetherTextViewVisible(){
        if (mTextView.getVisibility() == GONE) {
            return false;
        }
        return true;
    }

    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public void setSurfaceViewClickListener(OnSurfaceViewClickListener listener) {
        mListener = listener;
    }

    public interface OnSurfaceViewClickListener{
        public void onSurfaceViewClick(View v);
    }
}
