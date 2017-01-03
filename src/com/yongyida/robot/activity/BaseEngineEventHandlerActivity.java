package com.yongyida.robot.activity;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.yongyida.robot.videohelper.VideoEngine;

/**
 * Created by Administrator on 2016/8/30 0030.
 */
abstract public class BaseEngineEventHandlerActivity extends OriginalActivity {
    protected static final int ACCOUNT_MASK = 1000000000;
    protected VideoEngine mVideoEngine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVideoEngine = VideoEngine.create(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoEngine.leaveChannel();
    }

    protected boolean checknetwork() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (info.isAvailable() && info.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
}
