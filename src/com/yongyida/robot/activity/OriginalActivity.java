package com.yongyida.robot.activity;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.easemob.EMCallBack;
import com.tencent.android.tpush.XGPushManager;
import com.yongyida.robot.huanxin.DemoHXSDKHelper;
import com.yongyida.robot.service.SocketService;
import com.yongyida.robot.utils.ThreadPool;
import com.yongyida.robot.utils.Utils;

/**
 * Created by Administrator on 2016/10/27 0027.
 */
public class OriginalActivity extends Activity {

    public void fulllyExit(){
        XGPushManager.unregisterPush(getApplicationContext());
        getSharedPreferences("userinfo", Activity.MODE_PRIVATE).edit().clear().commit();
        getSharedPreferences("huanxin", MODE_PRIVATE).edit().clear().commit();
        if (Utils.isServiceRunning(this, SocketService.class.getCanonicalName())) {
            Utils.stopSocketService(this);
        }
        startActivity(new Intent(
                OriginalActivity.this,
                NewLoginActivity.class)
                .setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TASK));
        ThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                DemoHXSDKHelper.getInstance().logout(false,
                        new EMCallBack() {

                            @Override
                            public void onSuccess() {
                                Log.i("DemoHXSDKHelper", "logout");

                            }

                            @Override
                            public void onProgress(int arg0, String arg1) {

                            }

                            @Override
                            public void onError(int arg0, String arg1) {

                            }
                        });

            }
        });
    }
}
