/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-08
 * 
 */
package com.yongyida.robot.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.Config;
import com.yongyida.robot.video.av.AVMeeting;
import com.yongyida.robot.video.av.CameraView;
import com.yongyida.robot.video.av.TransferDataType;
import com.yongyida.robot.video.av.UserView;
import com.yongyida.robot.video.av.VideoParam;
import com.yongyida.robot.video.av.VideoSizeType;
import com.yongyida.robot.video.comm.Size;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.command.DataPacket;
import com.yongyida.robot.video.command.ExitRoomRequest;
import com.yongyida.robot.video.command.JoinRoomRequest;
import com.yongyida.robot.video.net.HtoN;
import com.yongyida.robot.video.sdk.ChannelConfig;
import com.yongyida.robot.video.sdk.CmdCallBacker;
import com.yongyida.robot.video.sdk.Event;
import com.yongyida.robot.video.sdk.EventListener;
import com.yongyida.robot.video.sdk.User;
import com.yongyida.robot.video.sdk.YYDSDKHelper;
import com.yongyida.robot.video.sdk.YYDVideoServer;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频页面
 */
public class ActivityMeeting extends BaseVideoActivity implements OnClickListener {
	public static final String TAG = "ActivityMeeting";

	private AudioManager mAudioManager;
	private Button mBtnAddCall;
	private Button mBtnMute;
	private Button mBtnHangup;
	private int mThumbWidth;
	private int mThumbHeight;
	private Size mVideoSize;
	private FrameLayout mFrameLayout;
	private List<View> mViews;
	private AVMeeting mAVMeeting;
	private ChannelConfig mConfig;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meeting2);
		
		mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		mBtnAddCall = ((Button) findViewById(R.id.btn_addcall));
		mBtnMute = ((Button) findViewById(R.id.btn_mute));
		mBtnHangup = ((Button) findViewById(R.id.btn_hangup));
		mBtnAddCall.setOnClickListener(this);
		mBtnMute.setOnClickListener(this);
		mBtnHangup.setOnClickListener(this);
		
		mAVMeeting = createAVMeeting();
		mThumbWidth = getThumbWidth();
		mThumbHeight = getThumbHeight();
		
		mFrameLayout = (FrameLayout) findViewById(R.id.view_container);
		mViews = new ArrayList<View>();
		addCameraView();
		addUserViews();
		User user = new User("User", 100227);
		addUserView(user);
		YYDSDKHelper.getInstance().registerEventListener(mEventListener);
	}


	@Override
	protected void initView() {

	}

	private AVMeeting createAVMeeting() {
		log.d(TAG, "createAVMeeting()");
		
		mConfig = new ChannelConfig();
		mConfig.transferTyp = Config.getTransferType();
		mConfig.encoderType = Config.getEncoderType();
		mConfig.transferDataType = Config.getTransferDataType();
		mConfig.enableAudio = (mConfig.transferDataType == TransferDataType.AUDIO
				|| mConfig.transferDataType == TransferDataType.AUDIOVIDEO);
		mConfig.enableVideo = (mConfig.transferDataType == TransferDataType.VIDEO
				|| mConfig.transferDataType == TransferDataType.AUDIOVIDEO);
		mConfig.enableSend = getIntent().getBooleanExtra("EnableSend", true);
		mConfig.enableRecv = getIntent().getBooleanExtra("EnableRecv", true);
		mVideoSize = VideoSizeType.getVideoSize(Config.getVideoSizeType());
		VideoParam vp = VideoParam.getVideoParam(mVideoSize.width, mVideoSize.height, Config.getFrameRateType(),
				Config.getBitRateType());
		log.d(TAG, vp.toString());
		mVideoSize.width = vp.VideoWidth;
		mVideoSize.height = vp.VideoHeight;
		mConfig.videoWidth = mVideoSize.width;
		mConfig.videoHeight = mVideoSize.height;
		mConfig.frameRate = vp.FrameRate;
		mConfig.bitRate = vp.Bitrate;
		log.d(TAG, mConfig.toString());
		
		return new AVMeeting(mConfig);
	}

	/**
	 * 加入摄像头视图
	 * 
	 */
	private void addCameraView() {
		log.d(TAG, "addCameraView");
		
		CameraView cv = new CameraView(this, mAVMeeting);
		cv.setLeft(getViewInitLeft());
		cv.setTop(getViewInitTop());
		cv.setLayoutParams(new LayoutParams(getViewInitWidth(), getViewInitHeight()));
		cv.setOnClickListener(mThumbClickListener);
		cv.setBorderVisible(getInitBorderVisible());
		cv.setTitleVisible(getInitTitleVisible());
		mViews.add(cv);
		mFrameLayout.addView(cv);
	}

	private void addUserViews() {
		List<User> users = mAVMeeting.getUsers();
		for (User user : users) {
			addUserView(user);
		}
	}

	private void addUserView(User user) {
		log.d(TAG, "addUserView");
		
		UserView uv = new UserView(this, user, mAVMeeting);
		uv.setLeft(getViewInitLeft());
		uv.setTop(getViewInitTop());
		uv.setLayoutParams(new LayoutParams(getViewInitWidth(), getViewInitHeight()));
		uv.setOnClickListener(mThumbClickListener);
		uv.setBorderVisible(getInitBorderVisible());
		uv.setTitleVisible(getInitTitleVisible());
		mViews.add(uv);
		mFrameLayout.addView(uv);
		refreshView();
	}
	
	private void refreshView() {
		int count = mFrameLayout.getChildCount();
		int thumbHeight = getThumbHeight();
		for (int i = 0; i < count; ++i) {
			View view = mFrameLayout.getChildAt(i);
			if (view.getHeight() <= thumbHeight) {
				mFrameLayout.bringChildToFront(view);
			}
		}
	}
	
	private OnClickListener mThumbClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			log.d(TAG, "mThumbClickListener onClick(), click View: " + v);
			
			if (v.getHeight() > getThumbHeight()) {
				return;
			}
			
			View bigView = getBigView();
			if (bigView != null) {
				switchView(v, bigView);
			}
			else {
				log.e(TAG, "Not found big view!");
			}
		}
	};
	
	private void switchView(View smallView, View bigView) {
		log.d(TAG, "switchView(), smallView: " + smallView + ", bigView: " + bigView);
		
		// 大视图变为小视图
		FrameLayout.LayoutParams svp = new FrameLayout.LayoutParams(
				smallView.getWidth(),
				smallView.getHeight());
		bigView.setLayoutParams(svp);
		/*if (bigView instanceof IThumbView) {
			IThumbView thumb = (IThumbView)bigView;
			thumb.setBorderVisible(true);
			thumb.setTitleVisible(true);
		}
		
		// 小视图变为大视图
		FrameLayout.LayoutParams bvp = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		smallView.setLayoutParams(bvp);
		if (smallView instanceof IThumbView) {
			IThumbView thumb = (IThumbView)smallView;
			thumb.setBorderVisible(false);
			thumb.setTitleVisible(false);
		}*/
		
		// 把小视图（原来的大视图）置前
		mFrameLayout.bringChildToFront(bigView);
	}
	
	private View getBigView() {
		for (View v : mViews) {
			if (v.getWidth() > mThumbWidth)
			   return v;
		}
		return null;
	}
	
	private void removeUserView(String role, long id) {
		log.d(TAG, "removeUserView(), role: " + role + ", id: " + id);
		
		UserView uv = getUserView(role, id);
		if (uv != null) {
			uv.close();
			mViews.remove(uv);
			mFrameLayout.removeView(uv);
		}
		else {
			log.e(TAG, "Not found user view, role: " + role + ", id: " + id);
		}
	}

	private UserView getUserView(String role, long id) {
		log.d(TAG, "getUserView(), role: " + role + ", id: " + id);
		
		for (View v : mViews) {
			if (v instanceof UserView) {
				UserView uv = (UserView)v;
				log.d(TAG, "User role: " + uv.getUser().getRole() + ", id: " + uv.getUser().getId());
				if (role.equals(uv.getUser().getRole())
						&& id ==  uv.getUser().getId()) {
					return uv;
				}
			}
		}
		return null;
	}

	private int getScreenHeight() {
		return (Utils.getScreenConfigurationOrientatioin(this) == Configuration.ORIENTATION_LANDSCAPE)
				? Utils.getScreenHeight(this) : Utils.getScreenWidth(this);
	}
	
	private int getThumbWidth() {
		return (int)((((float)mVideoSize.width) / mVideoSize.height) * getThumbHeight());
	}

	private int getThumbHeight() {
		return getScreenHeight() / 4;
	}

	private int getViewInitLeft() {
		if (mViews.size() > 1) 
			return (mViews.size() - 1) * mThumbWidth;
		else 
			return 0;
	}
	
	private int getViewInitTop() {
		return 0;
	}

	private int getViewInitWidth() {
		if (mViews.size() == 1)
			return LayoutParams.MATCH_PARENT;
		else
			return mThumbWidth;
	}

	private int getViewInitHeight() {
		if (mViews.size() == 1)
			return LayoutParams.MATCH_PARENT;
		else
			return mThumbHeight;
	}
	
	private boolean getInitBorderVisible() {
		return (mViews.size() != 1);
	}
	
	private boolean getInitTitleVisible() {
		return (mViews.size() != 1);
	}

	public EventListener mEventListener = new EventListener() {
		public void onEvent(Event event, Object data) {
			log.d(TAG, "onEvent(), envet: " + event + ", data: " + data);

			switch (event) {
			case JoinRoomRequest: {
				//收到用户进入房间
				log.d(TAG, "Received a JoinRoomRequest.");
				JoinRoomRequest req = (JoinRoomRequest) data;
				if (req.getRoomUser() != null) {
					final User user = mAVMeeting.getUser(req.getRoomUser().Role, req.getRoomUser().Id);
					if (user != null) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								addUserView(user);
							}
						});
					}
					else {
						log.e(TAG, "Not found user, role: " + req.getRoomUser().Role + ", id: " + req.getRoomUser().Id);
					}
				}
				else {
					log.e(TAG, "JoinRoomRequest error, RoomUser null!");
				}
				break;
			}
			case ExitRoomRequest: {
				//收到用户退出房间
				log.d(TAG, "Received a ExitRoomRequest.");
				final ExitRoomRequest req = (ExitRoomRequest) data;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//移除用户视图
						removeUserView(req.getRole(), req.getId());
						if (mAVMeeting.getUsers().size() == 0) {
							log.d(TAG, "All user exit, will exit.");
							hangUp();
						}
					}
				});
				break;
			}
			case ForwardRequest: {
				//数据转发
				log.d(TAG, "Received a ForwardRequest.");
				break;
			}
			default: {
				break;
			}
			}
		}
	};

	/** 
	 * 屏幕旋转时调用此方法 
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			log.d(TAG, "ORIENTATION_PORTRAIT");
		}
		else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			log.d(TAG, "ORIENTATION_LANDSCAPE");
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_addcall:
			addCall();
			break;
		case R.id.btn_mute:
			switchMute();
			break;
		case R.id.btn_hangup:
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
			hangUp();
			break;
		default:
			break;
		}
	}
	
	/**
	 * 添加通话
	 * @param
	 * @return
	 *
	 */
	private void addCall() {
		
	}

	/**
	 * 静音切换
	 * @param
	 * @return
	 *
	 */
	private void switchMute() {
		setMute(!isMute());
		mBtnMute.setBackgroundResource(
				isMute() ? R.drawable.ic_toolbar_mute_checked : R.drawable.ic_toolbar_mute);
	}
	
	/**
	 * 挂断
	 * @param
	 * @return
	 *
	 */
	private void hangUp() {
		log.d(TAG, "hangUp()");

		YYDVideoServer.getInstance().exitRoom(new CmdCallBacker() {
			public void onSuccess(Object arg) {
				log.d(TAG, "exitRoom success");
				// 如果返回退出成功，则关闭页面。
				close();
			}

			public void onFailed(int error) {
				log.d(TAG, "exitRoom failed, error: " + error);
			}
		});
	}
	
	public boolean isMute() {
		return mAudioManager.isMicrophoneMute();
	}

	public void setMute(boolean mute) {
		mAudioManager.setMicrophoneMute(mute);
	}

	public boolean isSpeakerOn() {
		return mAudioManager.isSpeakerphoneOn();
	}

	public void setSpeakerOn(boolean speakerOn) {
		mAudioManager.setSpeakerphoneOn(speakerOn);
	}
	
	/**
	 * 转发数据
	 * @param
	 * @return
	 * 
	 */
	public void forward() {
		byte[] bytes = HtoN.getBytes("0123456789");
		DataPacket data = DataPacket.createCopy(bytes, 0, bytes.length);
		YYDVideoServer.getInstance().forward(data, new CmdCallBacker() {
			public void onSuccess(Object arg) {
				log.d(TAG, "forward success");
			}
			
			public void onFailed(int error) {
				log.d(TAG, "forward failed, error: " + error);
			}
		});
	}
	
	private void close() {
		for (View v : mViews) {
			if (v instanceof CameraView) {
				((CameraView) v).close();
			}
			if (v instanceof UserView) {
				((UserView) v).close();
			}
		}
		if (mAVMeeting != null) {
			mAVMeeting.close();
			mAVMeeting = null;
		}
		finish();
	}

	@Override
	protected void onDestroy() {
		YYDSDKHelper.getInstance().unRegisterEventListener(mEventListener);
		super.onDestroy();
	}
}
