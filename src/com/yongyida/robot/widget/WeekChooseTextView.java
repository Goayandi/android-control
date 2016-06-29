package com.yongyida.robot.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.yongyida.robot.R;

/**
 * Created by Administrator on 2016/6/27 0027.
 */
public class WeekChooseTextView extends TextView {
    private final static int EVERYDAY = 8;
    private Context mContext;
    private boolean repeatFlag;

    public WeekChooseTextView(Context context) {
        this(context, null);
    }

    public WeekChooseTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekChooseTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    private void init() {
        this.setTextColor(getResources().getColor(R.color.white));
        this.setGravity(Gravity.CENTER);
    }


    public void setWeek(int week, boolean isRepeat) {
        switch (week) {
            case 1:
                this.setText(mContext.getString(R.string.monday));
                break;
            case 2:
                this.setText(mContext.getString(R.string.tuesday));
                break;
            case 3:
                this.setText(mContext.getString(R.string.wednesday));
                break;
            case 4:
                this.setText(mContext.getString(R.string.thursday));
                break;
            case 5:
                this.setText(mContext.getString(R.string.friday));
                break;
            case 6:
                this.setText(mContext.getString(R.string.saturday));
                break;
            case 7:
                this.setText(mContext.getString(R.string.sunday));
                break;
            default:
                break;
        }
        if (!isRepeat) {
            this.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            this.setBackgroundColor(getResources().getColor(R.color.red));
        }
    }

    public void setEveryday() {
        this.setText(mContext.getString(R.string.everyday));
        this.setBackgroundColor(getResources().getColor(R.color.red));
    }

}
