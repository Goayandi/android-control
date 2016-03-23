package com.yongyida.robot.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.HandlerUtil;
import com.yongyida.robot.utils.NetUtil;
import com.yongyida.robot.utils.NetUtil.callback;
import com.yongyida.robot.utils.ThreadPool;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.XmlUtil;

public class UpdateService extends Service {

	private int versionCode = 0;
	private String versionName = null;
	private NotificationManager updatemanager = null;
	private Notification updatenotfication = null;
	private Intent updateintent = null;
	private PendingIntent updatependingintent = null;
	private File newapk = null;

	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		updatemanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		updatenotfication = new Notification(R.drawable.app_icon, "下载中",
				System.currentTimeMillis());
		updatenotfication.flags = Notification.FLAG_ONGOING_EVENT;

		// RemoteViews contentview = new RemoteViews(getPackageName(),
		// R.layout.downloadnotification);
		// contentview.setTextViewText(R.id.notifictiontitle, "正在下载");
		updateintent = new Intent();
		updatependingintent = PendingIntent.getActivity(this, 0, updateintent,
				0);

		updatenotfication.setLatestEventInfo(this, "下载", "下载中",
				updatependingintent);
		// updatenotfication.contentView = contentview;
		updatemanager.notify(0, updatenotfication);
		ThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				final String fileName = "RobotControl.apk";
				newapk = new File(getExternalFilesDir(null).getAbsolutePath()
						+ "/" + fileName);
				try {
					String url = XmlUtil.xml(
							NetUtil.getinstance().downloadfile(
									UpdateService.this,
									getString(R.string.version_url),
									new callback() {

										@Override
										public void success(JSONObject json) {
										}

										@Override
										public void error(String errorresult) {
											HandlerUtil.sendmsg(handler,
													errorresult, 1);
										}
									}), "download");
					InputStream i = NetUtil.getinstance().downloadfile(
							UpdateService.this, url, new callback() {

								@Override
								public void success(JSONObject json) {

								}

								@Override
								public void error(String errorresult) {
									HandlerUtil
											.sendmsg(handler, errorresult, 1);
								}
							});
					FileOutputStream fos = new FileOutputStream(newapk);
					byte[] bs = new byte[1024];
					int len;
					while ((len = i.read(bs)) != -1) {
						fos.write(bs, 0, len);
					}
					fos.close();
					i.close();
					
					Intent install = new Intent();
					install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					install.setAction(android.content.Intent.ACTION_VIEW);
					install.setDataAndType(Uri.fromFile(newapk),
							"application/vnd.android.package-archive");
					startActivity(install);
					updatemanager.cancel(0);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});
		return super.onStartCommand(intent, flags, startId);
	}

	Handler handler = new Handler() {
		public void dispatchMessage(android.os.Message msg) {
			if (msg.what == 1) {
				ToastUtil.showtomain(UpdateService.this, msg.getData()
						.getString("result"));
			}
		};
	};

	@Override
	public void onDestroy() {
		updatemanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		updatemanager.cancel(0);
		super.onDestroy();
	}

}
