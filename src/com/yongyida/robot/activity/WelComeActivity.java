package com.yongyida.robot.activity;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.Menu;

import com.yongyida.robot.R;
import com.yongyida.robot.service.SocketService;
import com.yongyida.robot.service.UpdateService;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.HandlerUtil;
import com.yongyida.robot.utils.NetUtil;
import com.yongyida.robot.utils.StartUtil;
import com.yongyida.robot.utils.ThreadPool;
import com.yongyida.robot.utils.Utils;
import com.yongyida.robot.utils.XmlUtil;
import com.yongyida.robot.video.comm.log;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class WelComeActivity extends BaseActivity {
	private static final String TAG = "WelComeActivity";
	private String address;
	private String fotaAddress;
	private Timer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private AlertDialog alert;

	@Override
	public void onHandlerMessage(Message msg) {
		if (msg.what == 0) {
			AlertDialog.Builder builder = new Builder(WelComeActivity.this);
			builder.setMessage(R.string.have_new_version_if_update);
			builder.setTitle(R.string.version_update);
			builder.setPositiveButton(getString(R.string.yes),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							startService(new Intent(WelComeActivity.this,
									UpdateService.class));
							StartUtil.startintent(WelComeActivity.this,
									GuideActivity.class, "finish");

						}
					});
			builder.setNegativeButton(getString(R.string.no),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							alert.dismiss();
							StartUtil.startintent(WelComeActivity.this,
									GuideActivity.class, "finish");
						}
					});
			alert = builder.create();
			alert.show();
		} else if (msg.what == 1) {
		//	ToastUtil.showtomain(WelComeActivity.this,msg.getData().getString("result"));
			StartUtil.startintent(WelComeActivity.this, GuideActivity.class,
					"finish");
		} else if (msg.what == 2) {
//			StartUtil.startintent(WelComeActivity.this, GuideActivity.class,
//					"finish");
//			return;
			ThreadPool.execute(new Runnable() {

				@Override
				public void run() {

					try {

						String version = XmlUtil.xml(
								NetUtil.getinstance().downloadfile(
										WelComeActivity.this,
										address,
										new NetUtil.callback() {

											@Override
											public void success(JSONObject json) {

											}

											@Override
											public void error(String errorresult) {
												HandlerUtil.sendmsg(handler,
														errorresult, 1);
											}
										}), "xin");
						PackageManager packageManager = getPackageManager();
						PackageInfo info = packageManager.getPackageInfo(
								getPackageName(), 0);
						if (Double.parseDouble(version) > Double
								.parseDouble(info.versionName)) {
							handler.sendEmptyMessage(0);
						} else {
							startActivity(new Intent(WelComeActivity.this,
									GuideActivity.class));
							overridePendingTransition(android.R.anim.fade_in,
									android.R.anim.fade_out);
							finish();
						}
					} catch (PackageManager.NameNotFoundException e) {
						e.printStackTrace();
						HandlerUtil.sendmsg(handler, e.getMessage(), 1);
					}

				}
			});

//			ThreadPool.execute(new Runnable() {
//
//				@Override
//				public void run() {
//					String version = XmlUtil.xmlFota(
//							NetUtil.getinstance().downloadfile(
//									WelComeActivity.this,
//									fotaAddress,
//									new NetUtil.callback() {
//
//										@Override
//										public void success(JSONObject json) {
//
//										}
//
//										@Override
//										public void error(String errorresult) {
//										}
//									}), XmlUtil.Y50B, XmlUtil.YYD);
//					getSharedPreferences("Receipt", MODE_PRIVATE).edit().putString("fota", version).apply();
//
//				}
//			});
		}
		super.onHandlerMessage(msg);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wel_come, menu);
		return true;
	}

    @Override
	public void initlayout(OnRefreshListener onRefreshListener) {
		setContentView(R.layout.activity_wel_come);
//		String stateCode = getSharedPreferences("Receipt", MODE_PRIVATE).getString("state_code", null);
//		if (Constants.HK_CODE.equals(stateCode))
		if (Utils.isServiceRunning(this, SocketService.class.getCanonicalName())) {
			Log.i(TAG, "service start");
		}
		String serverState = getSharedPreferences("net_state", MODE_PRIVATE).getString("state",null);
		if (serverState != null && !serverState.equals("official")){
			Utils.switchServer(Utils.TEST);
		} else {
			Utils.switchServer(Utils.CN);
		}
        Runtime rt=Runtime.getRuntime();
        long maxMemory=rt.maxMemory();
        log.i("maxMemory:", Long.toString(maxMemory / (1024 * 1024)));
		address = Constants.download_address;
		fotaAddress = Constants.download_fota_address;
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.sendEmptyMessage(2);
			}
		}, 1000);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		handler.removeCallbacksAndMessages(null);
	}
}
