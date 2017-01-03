package com.yongyida.robot.activity;

import android.os.Bundle;

/**
 * Created by Administrator on 2016/4/11 0011.
 */
public  abstract class BaseVideoActivity extends OriginalActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected abstract void initView();
}
