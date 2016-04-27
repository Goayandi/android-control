package com.yongyida.robot.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.yongyida.robot.R;
import com.yongyida.robot.activity.ConnectActivity;
import com.yongyida.robot.huanxin.DemoHXSDKHelper;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ToastUtil;

/**
 * Created by Administrator on 2015/10/12 0012.
 */
public class NetStateBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connect = (ConnectivityManager) context
				.getSystemService(context.CONNECTIVITY_SERVICE);

		NetworkInfo mobile= connect.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wifi=connect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if(mobile != null){
			if (!mobile.isConnected()&&!wifi.isConnected()) {
				back(context);
				ToastUtil.showtomain(context, context.getString(R.string.please_connect_net));
			} else if (!mobile.isAvailable()&&!wifi.isAvailable()) {
				back(context);
				ToastUtil.showtomain(context, context.getString(R.string.please_connect_net));
			}
		} else {
			if(!wifi.isConnected()){
				back(context);
				ToastUtil.showtomain(context, context.getString(R.string.please_connect_net));
			}
		}
	}

	public void back(Context context) {
		if (DemoHXSDKHelper.getInstance().isLogined()) {
			context.sendBroadcast(new Intent(Constants.Stop));
			context.startActivity(new Intent(context, ConnectActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}

	}
}
