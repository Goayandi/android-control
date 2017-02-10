package com.yongyida.robot.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.NetUtil;
import com.yongyida.robot.utils.NetUtil.callback;
import com.yongyida.robot.utils.ThreadPool;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.XmlUtil;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateService extends Service {
    private final static int IMAGE_RESOURCE = R.drawable.app_icon;
	private NotificationManager mNotificationManager = null;
	private Notification mNotification = null;
	private File newapk = null;
    private int mNotificationId = 0;
    private final static int REQUEST_CODE = 0;
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == PROGRESS_UPDATE) {
                mNotification.contentView.setProgressBar(R.id.progress_horizontal, 100, msg.arg1, false);
                mNotificationManager.notify(mNotificationId, mNotification);
            }
        }
    };
    private final static int PROGRESS_UPDATE = 3;

    @Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        if (mNotificationManager != null) {
            return super.onStartCommand(intent, flags, startId);
        }
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotification = new Notification();
        mNotification.icon = IMAGE_RESOURCE;
        mNotification.tickerText = getString(R.string.downloading);
        mNotification.when = System.currentTimeMillis();
        Intent updateintent = new Intent();
        updateintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        updateintent.setClass(getApplicationContext(), UpdateService.class);
        PendingIntent updatependingintent = PendingIntent.getActivity(this, REQUEST_CODE, updateintent,
                PendingIntent.FLAG_UPDATE_CURRENT);
		mNotification.flags = Notification.FLAG_NO_CLEAR;
        mNotification.contentIntent = updatependingintent;
        mNotification.contentView = new RemoteViews(getPackageName(), R.layout.notification_update);
        mNotification.contentView.setProgressBar(R.id.progress_horizontal, 100, 0, false);
        mNotification.contentView.setTextViewText(R.id.text, getString(R.string.downloading));
        mNotification.contentView.setImageViewResource(R.id.image, IMAGE_RESOURCE);
        mNotificationManager.cancel(mNotificationId);
        mNotificationManager.notify(mNotificationId, mNotification);

		ThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				final String fileName = "RobotControl.apk";
				newapk = new File(getExternalFilesDir(null).getAbsolutePath()
						+ "/" + fileName);
				if (TextUtils.isEmpty(Constants.download_address)) {
					stopSelf();
					return;
				}
				try {
					String urlStr = XmlUtil.xml(
							NetUtil.getinstance().downloadfile(
									UpdateService.this,
									Constants.download_address,
									new callback() {

										@Override
										public void success(JSONObject json) {
										}

										@Override
										public void error(String errorresult) {
											handler.post(new ToastRunnable(errorresult));
										}
									}), "download");
                    if (TextUtils.isEmpty(urlStr)) {
                        stopSelf();
                        return;
                    }
                    URL url = new URL(urlStr);
                    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                    urlConn.setConnectTimeout(5000);
                    urlConn.setReadTimeout(8000);
                    urlConn.connect();
                    int file_length = urlConn.getContentLength();
                    InputStream is = urlConn.getInputStream();
                    BufferedInputStream bin = new BufferedInputStream(is);
                    FileOutputStream fos = new FileOutputStream(newapk);
					byte[] bs = new byte[1024];
					int len;
                    int progress = 0;
                    int current_length = 0;
                    int i = 0;
					while ((len = bin.read(bs)) != -1) {
                        current_length += len;
                        fos.write(bs, 0, len);
                        progress = (int)((current_length / (float) file_length) * 100);
                        i++;
                        if (i % 50 == 0) {
                            Message message = handler.obtainMessage();
                            message.arg1 = progress;
                            message.what = PROGRESS_UPDATE;
                            handler.sendMessage(message);
                        }

                    }
					fos.close();
                    bin.close();
					is.close();
					installApk();
					stopSelf();
				} catch (ClientProtocolException e) {
					if (!TextUtils.isEmpty(e.getMessage())) {
						handler.post(new ToastRunnable(e.getMessage()));
					} else {
						handler.post(new ToastRunnable("ClientProtocolException"));
					}
					e.printStackTrace();
				} catch (IOException e) {
					if (!TextUtils.isEmpty(e.getMessage())) {
						handler.post(new ToastRunnable(e.getMessage()));
					} else {
						handler.post(new ToastRunnable("IOException"));
					}
					e.printStackTrace();
				}

			}
		});
		return super.onStartCommand(intent, flags, startId);
	}

	public class ToastRunnable implements Runnable {
		private String mText;
		public ToastRunnable(String text){
			this.mText = text;
		}
		@Override
		public void run() {
			ToastUtil.showtomain(UpdateService.this, mText);
			stopSelf();
		}
	};

    private void installApk(){
        Intent install = new Intent();
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        install.setAction(android.content.Intent.ACTION_VIEW);
        install.setDataAndType(Uri.fromFile(newapk),
                "application/vnd.android.package-archive");
        startActivity(install);
    }

	@Override
	public void onDestroy() {
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationManager.cancel(mNotificationId);
		super.onDestroy();
	}

}
