package com.yongyida.robot.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 2016/6/27 0027.
 */
public class WeekChooseLinearlayout extends LinearLayout {
    private Context mContext;
    public WeekChooseLinearlayout(Context context) {
        this(context, null, 0);
    }

    public WeekChooseLinearlayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekChooseLinearlayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    private void init() {
        this.setGravity(Gravity.CENTER);
        this.setOrientation(HORIZONTAL);
    }

    public void addWeekTextView(String[] arr, boolean repeat) {
        if (arr != null && arr.length != 0 && arr[0] != "") {
            for (String i : arr) {
                WeekChooseTextView tv = new WeekChooseTextView(mContext);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(3, 3, 3, 3);
                tv.setLayoutParams(lp);
                tv.setWeek(Integer.parseInt(i), repeat);
                this.addView(tv);
            }
        }
    }

    public void setEveryDay() {
        WeekChooseTextView tv = new WeekChooseTextView(mContext);
        tv.setEveryday();
        this.addView(tv);
    }

}
