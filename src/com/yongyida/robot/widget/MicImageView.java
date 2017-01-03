package com.yongyida.robot.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.yongyida.robot.R;

/**
 * Created by Administrator on 2016/12/26 0026.
 */

public class MicImageView extends ImageView {
    private int[] mMicImagesArr = new int[] {
                R.drawable.record_animate_01,
                R.drawable.record_animate_02,
                R.drawable.record_animate_03,
                R.drawable.record_animate_04,
                R.drawable.record_animate_05,
                R.drawable.record_animate_06,
                R.drawable.record_animate_07,
                R.drawable.record_animate_08,
                R.drawable.record_animate_09,
                R.drawable.record_animate_10,
                R.drawable.record_animate_11,
                R.drawable.record_animate_12,
                R.drawable.record_animate_13,
                R.drawable.record_animate_14
    };

    public MicImageView(Context context) {
        super(context);
    }

    public MicImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MicImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置音量大小  0-13
     * @param i
     */
    public void setVolume(int i){
        if (i < 0) {
            i = 0;
        } else if (i > 13) {
            i = 13;
        }
        setImageResource(mMicImagesArr[i]);
    }

}
