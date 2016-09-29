package com.yongyida.robot.broadcastReceiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.easemob.chat.EMChatManager;
import com.yongyida.robot.activity.ConnectActivity;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ToastUtil;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SocketErrorReceiver extends BroadcastReceiver {

    private static final String TAG = "SocketErrorReceiver";
    private Timer mTimer;
    @Override
	public void onReceive(final Context context, Intent intent) {
		if (intent.getAction().equals("socket_error")) {
            Log.e(TAG, "socket_error");
			context.sendBroadcast(new Intent(Constants.Stop));
			EMChatManager.getInstance().endCall();
			ToastUtil.showtomain(context, intent.getStringExtra("content"));
			if (!isForeground(context, ConnectActivity.class.getCanonicalName()) && !isExitApp(context)) {
                mTimer = new Timer();
                mTimer.schedule(new TimerTask(){

                    @Override
                    public void run() {
                        if (!isForeground(context, ConnectActivity.class.getCanonicalName()) && !isExitApp(context)) {
                            context.startActivity(new Intent(context, ConnectActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                    }
                }, 1000);

			}
		}
	}

	private boolean isForeground(Context context, String className) {
		if (context == null || TextUtils.isEmpty(className)) {
			return false;
		}

		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
		if (list != null && list.size() > 0) {
			ComponentName cpn = list.get(0).topActivity;
            Log.i(TAG, cpn.getClassName());
            if (className.equals(cpn.getClassName())) {
				return true;
			}
		}

		return false;
	}

    private boolean isExitApp(Context context) {
        if (context == null) {
            return true;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            Log.i(TAG, cpn.getClassName());
            if (cpn.getClassName().contains(context.getPackageName())) {
                return false;
            }
        }
        return true;
    }

}
