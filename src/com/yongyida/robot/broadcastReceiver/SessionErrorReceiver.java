package com.yongyida.robot.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.easemob.chat.EMChatManager;
import com.yongyida.robot.activity.NewLoginActivity;
import com.yongyida.robot.service.SocketService;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.Utils;

/**
 * Created by Administrator on 2016/4/28 0028.
 */
public class SessionErrorReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Constants.SESSION_ERROR)) {
            context.sendBroadcast(new Intent(Constants.Stop));
            EMChatManager.getInstance().endCall();
            ToastUtil.showtomain(context, "登录过期");
            context.getSharedPreferences("userinfo", context.MODE_PRIVATE).edit().clear().commit();
            context.getSharedPreferences("huanxin", context.MODE_PRIVATE).edit().clear().commit();
            if (Utils.isServiceRunning(context, SocketService.class.getSimpleName())) {
                Utils.stopSocketService(context);
            }
            context.startActivity(new Intent(context, NewLoginActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}
