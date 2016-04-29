package com.yongyida.robot.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.easemob.chat.EMChatManager;
import com.yongyida.robot.activity.ConnectActivity;
import com.yongyida.robot.utils.ToastUtil;

public class SocketErrorReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("socket_error")) {
			//context.sendBroadcast(new Intent(Constants.Stop));
			EMChatManager.getInstance().endCall();
			ToastUtil.showtomain(context, intent.getStringExtra("content"));
			context.startActivity(new Intent(context, ConnectActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
	}

}
