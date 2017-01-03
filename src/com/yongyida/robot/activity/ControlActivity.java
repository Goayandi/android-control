/** 
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yongyida.robot.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;

import com.easemob.EMCallBack;
import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMCallStateChangeListener;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.Type;
import com.easemob.chat.EMVideoCallHelper;
import com.easemob.exceptions.EMNetworkUnconnectedException;
import com.easemob.exceptions.EMNoActiveCallException;
import com.easemob.exceptions.EMServiceNotReadyException;
import com.easemob.exceptions.EaseMobException;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.yongyida.robot.R;
import com.yongyida.robot.huanxin.CallActivity;
import com.yongyida.robot.huanxin.CameraHelper;
import com.yongyida.robot.huanxin.HXSDKHelper;
import com.yongyida.robot.service.HeadsetPlugReceiver;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.HandlerUtil;
import com.yongyida.robot.utils.StartUtil;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ControlActivity extends CallActivity implements OnClickListener,
		OnTouchListener {

	private static final String TAG = "ControlActivity";
	private SurfaceView localSurface;
	private SurfaceHolder localSurfaceHolder;
	private static SurfaceView oppositeSurface;
	private SurfaceHolder oppositeSurfaceHolder;
	private boolean isAnswered;
	private boolean endCallTriggerByMe = false;
	EMVideoCallHelper callHelper;

	private CameraHelper cameraHelper;
	private Button play;
	private ImageView speak;
	private Button back;

	private ImageView up;
	private ImageView left;
	private ImageView down;
	private ImageView right;

	private ImageView head_up;
	private ImageView head_down;
	private ImageView head_left;
	private ImageView head_right;
	private RecognizerDialog mDialog;
	private int key;
	private Timer hide_timer;
	private HeadsetPlugReceiver headsetPlugReceiver; 
	private String runningMode;
	private BroadcastReceiver mRNameBR = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String rname = intent.getStringExtra("rname");
			getSharedPreferences("robotname", MODE_PRIVATE).edit()
					.putString("name", rname).commit();
		}
	};
	private boolean isComingCall;
    private String mRobotName;
    private TableLayout mHeadTableLayout;
    private TableLayout mMoveTableLayout;
	private Timer time = null;

	private long starttime;
	private boolean flag = true;

	private int action = 0;

	private int move = 0;
	private Button mMuteBtn;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			finish();
			return;
		}
		setContentView(R.layout.activity_control);
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		initData();
		initView();
		initVideo();
		initBR();

	}

	private void initBR() {
		BroadcastReceiverRegister.reg(ControlActivity.this, new String[]{Constants.BATTERY}, mRNameBR);
	}

	private void initVideo() {
		// 显示本地图像的surfaceview
		localSurface = (SurfaceView) findViewById(R.id.local_surface);
		localSurface.setZOrderMediaOverlay(true);
		localSurface.setZOrderOnTop(true);
		localSurfaceHolder = localSurface.getHolder();
		// 获取callHelper,cameraHelper
		callHelper = EMVideoCallHelper.getInstance();
		cameraHelper = new CameraHelper(callHelper, localSurfaceHolder);

		// 显示对方图像的surfaceview
		oppositeSurface = (SurfaceView) findViewById(R.id.opposite_surface);
		oppositeSurface.setOnClickListener(this);
		oppositeSurfaceHolder = oppositeSurface.getHolder();
		// 设置显示对方图像的surfaceview
		callHelper.setSurfaceView(oppositeSurface);

		localSurfaceHolder.addCallback(new LocalCallback());
		oppositeSurfaceHolder.addCallback(new OppositeCallback());

		// 设置通话监听
		addCallStateListener();
		// 判断视频模式
		if (runningMode.equals("control")) {
			localSurface.setVisibility(View.INVISIBLE);
		}

		initSound();

		if(isComingCall) {
			if (EMChatManager.getInstance().isConnected()) {
				try {
					oppositeSurface.setVisibility(View.VISIBLE);
					play.setBackgroundResource(R.drawable.zanting);
					if (!runningMode.equals("control")) {
						speak.setVisibility(View.GONE);
					}
					mMuteBtn.setVisibility(View.VISIBLE);
					audioManager.setMicrophoneMute(true);
					mMuteBtn.setBackgroundResource(R.drawable.icon_mute_on);
					// 通知cameraHelper可以写入数据
					EMChatManager.getInstance().answerCall();
					cameraHelper.setStartFlag(true);
				} catch (EMNoActiveCallException e) {
					e.printStackTrace();
				} catch (EMNetworkUnconnectedException e) {
					e.printStackTrace();
				}
			} else {
				finish();
			}
		}
	}

	private void initSound() {
		audioManager.setMicrophoneMute(true);
		//	registerHeadsetPlugReceiver();
		if (audioManager.isWiredHeadsetOn()) {
//			Intent headsetPluggedIntent = new Intent(Intent.ACTION_HEADSET_PLUG);
//			headsetPluggedIntent.putExtra("state", 1);
//			headsetPluggedIntent.putExtra("microphone", 1);
//			headsetPluggedIntent.putExtra("name", "");
			//TODO
			closeSpeakerOn() ;
		} else {
//			Intent headsetUnpluggedIntent = new Intent(
//					Intent.ACTION_HEADSET_PLUG);
//			headsetUnpluggedIntent.putExtra("state", 0);
//			headsetUnpluggedIntent.putExtra("microphone", 0);
//			headsetUnpluggedIntent.putExtra("name", "");
			// 打开扬声器
			openSpeakerOn();
		}

	}

	private void initData() {
		HXSDKHelper.getInstance().isVideoCalling = true;
		// 判断视频模式
		runningMode = getIntent().getExtras().getString("mode");

		isComingCall = getIntent().getBooleanExtra("isComingCall", false);
		if (!isComingCall) {
			username = getSharedPreferences("Receipt", MODE_PRIVATE).getString(
					"username", null);
			username = username.toLowerCase();
		} else {
			username = getIntent().getStringExtra("username");
		}
	}

	private void initView(){
		initpower();
		initcontrol();
		progress = new ProgressDialog(this);
		progress.setCanceledOnTouchOutside(false);
	}

	private void registerHeadsetPlugReceiver() {
		headsetPlugReceiver = new HeadsetPlugReceiver();
        IntentFilter intentFilter = new IntentFilter();  
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");  
        registerReceiver(headsetPlugReceiver, intentFilter);  
		
	}

	private EMCallBack mCallBack = new EMCallBack() {

		@Override
		public void onSuccess() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					oppositeSurface.setVisibility(View.VISIBLE);
					if (runningMode.equals("control")) {
						localSurface.setVisibility(View.INVISIBLE);
					} else {
						localSurface.setVisibility(View.VISIBLE);
					}
					if (index > 0) {
                        if (progress != null) {
                            progress.dismiss();
                        }
						ToastUtil.showtomain(ControlActivity.this, getString(R.string.dont_so_fast2));
						return;
					}
					boolean wificheck = getSharedPreferences("setting",
							MODE_PRIVATE).getBoolean("wificheck", true);
					if (wificheck && !checknetwork()) {
                        if (progress != null) {
                            progress.dismiss();
                        }
						ToastUtil.showtomain(ControlActivity.this, getString(R.string.not_wifi));
						return;
					}
					try {
						// 拨打视频通话
						if (EMChatManager.getInstance().isConnected()) {
							EMChatManager.getInstance().makeVideoCall(username);
							play.setBackgroundResource(R.drawable.zanting);
                            if (!runningMode.equals("control")) {
                                speak.setVisibility(View.GONE);
                            }
							mMuteBtn.setVisibility(View.VISIBLE);
							cameraHelper.setStartFlag(true);
							if (!runningMode.equals("control")) {
								// 通知cameraHelper可以写入数据
								cameraHelper.startCapture();
							}
						} else {
							huanxinLogin();
						}
					} catch (EMServiceNotReadyException e) {
						huanxinLogin();
					}
				}
			});
		}

		@Override
		public void onProgress(int arg0, String arg1) {
		}

		@Override
		public void onError(int arg0, String arg1) {
			Log.e("ControlActivity","error:" + arg1);
			enHandler.post(new Runnable() {

				@Override
				public void run() {
					ToastUtil.showtomain(ControlActivity.this, getString(R.string.initialize_fail));
				}
			});
		}
	};

	public void sendmsg(String mode, String touser) {
		EMMessage msg = EMMessage.createSendMessage(Type.CMD);
		msg.setReceipt(touser);
		if ("control".equals(mode)){
			msg.setAttribute("mode", "controll");
		} else {
			msg.setAttribute("mode", mode);
		}
		CmdMessageBody cmd = new CmdMessageBody(Constants.Video_Mode);
		msg.addBody(cmd);
		if (EMChatManager.getInstance().isConnected()) {
			EMChatManager.getInstance().sendMessage(msg, mCallBack);
		} else {
            if (progress != null) {
                progress.dismiss();
            }
			ToastUtil.showtomain(ControlActivity.this, getString(R.string.initialize_fail));
			if (!TextUtils.isEmpty(getSharedPreferences("huanxin", MODE_PRIVATE)
					.getString("username", null)) && !TextUtils.isEmpty(getSharedPreferences("huanxin", MODE_PRIVATE)
					.getString("password", null))) {
				EMChatManager.getInstance().login(
						getSharedPreferences("huanxin", MODE_PRIVATE)
								.getString("username", null),
						getSharedPreferences("huanxin", MODE_PRIVATE)
								.getString("password", null),
						new EMCallBack() {

							@Override
							public void onSuccess() {
							}

							@Override
							public void onProgress(int arg0, String arg1) {
							}

							@Override
							public void onError(int arg0, String arg1) {
							}
						});
			}
		}
	}

	private void sendmsg() {
		EMMessage msg = EMMessage.createSendMessage(Type.CMD);
		msg.setReceipt(username);
		CmdMessageBody cmd = new CmdMessageBody("yongyida.robot.video.rotate");
		msg.setAttribute("angle", 0);
		msg.addBody(cmd);
		try {
			EMChatManager.getInstance().sendMessage(msg);
		} catch (EaseMobException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送静音透传
	 * @param mute
	 */
	private void sendMuteMsg(boolean mute){
		EMMessage msg = EMMessage.createSendMessage(Type.CMD);
		msg.setReceipt(username);
		CmdMessageBody cmd = new CmdMessageBody("yongyida.robot.video.mute");
		msg.setAttribute("mute", mute);
		msg.addBody(cmd);
		try {
			EMChatManager.getInstance().sendMessage(msg);
		} catch (EaseMobException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStart() {
		BroadcastReceiverRegister.reg(this,
				new String[] { ConnectivityManager.CONNECTIVITY_ACTION },
				neterror);
		super.onStart();
	}


	@Override
	public boolean onTouch(final View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			speak.setEnabled(false);
			sendcmd("start", v);
			starttime = System.currentTimeMillis();
			move++;
			if (time != null) {
				time.cancel();
			}
			time = new Timer();
			time.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					sendcmd("start", v);
				}
			}, 1000, 1000);
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			speak.setEnabled(true);
			sendcmd("stop", v);
			flag = true;
		} else if (event.getAction() == MotionEvent.ACTION_CANCEL){
			speak.setEnabled(true);
			sendcmd("stop", v);
			flag = true;
		} 
		return true;

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			if (time != null)
				time.cancel();
		}
		return super.dispatchTouchEvent(ev);
	}

	private void sendcmd(String flag, final View v) {
		if (flag.equals("start")) {
			switch (v.getId()) {
			case R.id.up:
				execute("forward");
				HandlerUtil.sendmsg(enHandler, "up", 0);
				break;
			case R.id.left:
				execute("turn_left");
				HandlerUtil.sendmsg(enHandler, "left", 0);
				break;
			case R.id.down:
				execute("back");
				HandlerUtil.sendmsg(enHandler, "down", 0);
				break;
			case R.id.right:
				execute("turn_right");
				HandlerUtil.sendmsg(enHandler, "right", 0);
				break;
			case R.id.head_up:
				execute("head_up");
				HandlerUtil.sendmsg(enHandler, "head_up", 0);
				break;
			case R.id.head_left:
				execute("head_left");
				HandlerUtil.sendmsg(enHandler, "head_left", 0);
				break;
			case R.id.head_down:
				execute("head_down");
				HandlerUtil.sendmsg(enHandler, "head_down", 0);
				break;
			case R.id.head_right:
				execute("head_right");
				HandlerUtil.sendmsg(enHandler, "head_right", 0);
				break;
			default:
				break;
			}

		} else {
			switch (v.getId()) {
			case R.id.up:
				execute("stop");
				break;
			case R.id.left:
				execute("stop");
				break;
			case R.id.down:
				execute("stop");
				break;
			case R.id.right:
				execute("stop");
				break;
			default:
				break;
			}
			if (time != null) {
				time.cancel();
			}
			time = null;
			enHandler.sendEmptyMessage(1);
		}

	}

	Handler enHandler = new Handler() {
		public void dispatchMessage(android.os.Message msg) {
			if (msg.what == 0) {
			} else if (msg.what == 2) {
				ToastUtil.showtomain(ControlActivity.this, getString(R.string.request_timeout));
			} else if (msg.what == 3) {
				play.setEnabled(true);
			} else if (msg.what == 4) {
                if (play.getVisibility() == View.GONE) {
                    toggle();
                }
				play.setBackgroundResource(R.drawable.bofang);
                if (!runningMode.equals("control")) {
                    speak.setVisibility(View.VISIBLE);
                }
				mMuteBtn.setVisibility(View.GONE);
			} else if (msg.what == 5) {
				resume();
			} else if (msg.what == 6) {
				toggle();
			} else if (msg.what == 7) {
				oppositeSurface.setVisibility(View.VISIBLE);
				toggle();
			}

		};
	};

	private void initpower() {
		msgid = UUID.randomUUID().toString();
		play = (Button) findViewById(R.id.play);
		play.setOnClickListener(this);
		speak = (ImageView) findViewById(R.id.speak);
		speak.setOnClickListener(this);
		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(this);
		mMuteBtn = (Button) findViewById(R.id.mictoggole);
		mMuteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggle_speak(v);
			}
		});
	}

	private void initcontrol() {
		up = (ImageView) findViewById(R.id.up);
		up.setOnTouchListener(this);
		left = (ImageView) findViewById(R.id.left);
		left.setOnTouchListener(this);
		down = (ImageView) findViewById(R.id.down);
		down.setOnTouchListener(this);
		right = (ImageView) findViewById(R.id.right);
		right.setOnTouchListener(this);
		head_up = (ImageView) findViewById(R.id.head_up);
		head_up.setOnTouchListener(this);
		head_left = (ImageView) findViewById(R.id.head_left);
		head_left.setOnTouchListener(this);
		head_down = (ImageView) findViewById(R.id.head_down);
		head_down.setOnTouchListener(this);
		head_right = (ImageView) findViewById(R.id.head_right);
		head_right.setOnTouchListener(this);
        mHeadTableLayout = (TableLayout) findViewById(R.id.tl_head);
        mMoveTableLayout = (TableLayout) findViewById(R.id.tl_move);
        mRobotName = getSharedPreferences("Receipt", MODE_PRIVATE).getString("username", null);
        if (!TextUtils.isEmpty(mRobotName) && Utils.isSeries(mRobotName, "20")) {
			mHeadTableLayout.setVisibility(View.GONE);
		} else {
			mHeadTableLayout.setVisibility(View.VISIBLE);
		}
	}

	InitListener init = new InitListener() {

		@Override
		public void onInit(int code) {
			if (code != ErrorCode.SUCCESS) {
				Log.i("Error", "错误码为:" + code);
			}
		}
	};

	private void execute(String execode) {
		key = 0;
		Constants.execode = execode;
		Intent intent = new Intent();
		intent.setAction("move");
		sendBroadcast(intent);
	}

	/**
	 * 本地SurfaceHolder callback
	 * 
	 */
	class LocalCallback implements SurfaceHolder.Callback {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// cameraHelper.startCapture();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
            if (isComingCall) {
                cameraHelper.startCapture();
                isComingCall = false;
            }

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			holder.removeCallback(this);
		}
	}

	BroadcastReceiver neterror = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			if (manager != null) {

				// 获取网络连接管理的对象

				NetworkInfo info = manager.getActiveNetworkInfo();

				if (info != null && info.isConnected()) {

					// 判断当前网络是否已经连接

					if (info.getState() == NetworkInfo.State.CONNECTED
							&& !info.isAvailable()) {
						if (cameraHelper.isStarted() && isconnected) {
							cameraHelper.stopCapture(oppositeSurfaceHolder);
							EMChatManager.getInstance().endCall();
							saveCallRecord(1);
						}
						StartUtil.startintent(ControlActivity.this,
								ConnectActivity.class, "finish");
					}

				}

			}
		}
	};

	/**
	 * 对方SurfaceHolder callback
	 */
	class OppositeCallback implements SurfaceHolder.Callback {

		@SuppressWarnings("deprecation")
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
	//		callHelper.setRenderFlag(true);
			holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);

		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			callHelper.onWindowResize(width, height, format);
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
	//		callHelper.setRenderFlag(false);
			holder.removeCallback(this);
		}

	}

	/**
	 * 设置通话状态监听
	 */
	boolean isconnected = false;

	void addCallStateListener() {
		callStateListener = new EMCallStateChangeListener() {

			@Override
			public void onCallStateChanged(CallState callState, CallError error) {
				switch (callState) {
				case IDLE:
                    Log.e(TAG, "IDLE");
					break;
				case CONNECTING: // 正在连接对方
                    Log.e(TAG, "CONNECTING");
					enHandler.sendEmptyMessage(7);
					break;
				case CONNECTED: // 双方已经建立连接
                    Log.e(TAG, "CONNECTED");
					isconnected = true;
					break;
				case ACCEPTED: // 电话接通成功
                    Log.e(TAG, "ACCEPTED");
                    if (mTimeoutTimer != null) {
                        mTimeoutTimer.cancel();
                    }
                    if (progress != null) {
                        progress.dismiss();
                    }
					if (timer != null) {
						timer.cancel();
					}
					hide_timer = new Timer();
					hide_timer.schedule(new TimerTask() {

						@Override
						public void run() {
							if (key == 5) {
								enHandler.sendEmptyMessage(6);
							}
							key++;
						}
					}, new Date(), 1000);
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (EMVideoCallHelper.getInstance().getVideoHeight() == 320
											&& EMVideoCallHelper.getInstance().getVideoWidth() == 240) {
                                        Log.e(TAG, "320*240");
                                        ToastUtil.showtomain(ControlActivity.this, EMVideoCallHelper.getInstance().getVideoHeight() + ":" + EMVideoCallHelper.getInstance().getVideoWidth() );
										EMChatManager.getInstance().endCall();
										saveCallRecord(1);
										ToastUtil.showtomain(ControlActivity.this, getString(R.string.connect_error_retry));
										cameraHelper.stopCapture(oppositeSurfaceHolder);
										toggle();
										play.setBackgroundResource(R.drawable.bofang);
                                        if (!runningMode.equals("control")) {
                                            speak.setVisibility(View.VISIBLE);
                                        }
										mMuteBtn.setVisibility(View.GONE);
									}
								}
							});
						}
					}, 5000);
					break;
				case DISCONNNECTED: // 电话断了
                    Log.e(TAG, "DISCONNNECTED");
					if (mTimeoutTimer != null) {
						mTimeoutTimer.cancel();
					}
					if (progress != null) {
						progress.dismiss();
					}
					if (hide_timer != null) {
						hide_timer.cancel();
						key = 0;
					}
					enHandler.sendEmptyMessage(4);
					final CallError fError = error;
					isconnected = false;
					if (cameraHelper != null && cameraHelper.isStarted()) {
						cameraHelper.stopCapture(oppositeSurfaceHolder);
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (oppositeSurface != null && localSurface != null) {
								oppositeSurface.setVisibility(View.GONE);
								localSurface.setVisibility(View.INVISIBLE);
							}
							if (fError == CallError.REJECTED) {
								callingState = CallingState.BEREFUESD;
							} else if (fError == CallError.ERROR_TRANSPORT) {
							} else if (fError == CallError.ERROR_BUSY) {
								callingState = CallingState.BUSY;
							} else if (fError == CallError.ERROR_NORESPONSE) {
								callingState = CallingState.NORESPONSE;
							} else if (fError == CallError.ERROR_INAVAILABLE) {
								saveCallRecord(1);
								ToastUtil.showtomain(ControlActivity.this,
										getString(R.string.connect_error_restart_robot));
								EMChatManager.getInstance().endCall();
								callingState = CallingState.OFFLINE;
							} else {
								if (isAnswered) {
									callingState = CallingState.NORMAL;
									if (endCallTriggerByMe) {
										// callStateTextView.setText(s6);
									} else {

									}
								} else {
									if (isInComingCall) {
										callingState = CallingState.UNANSWERED;

									} else {
										if (callingState != CallingState.NORMAL) {
											callingState = CallingState.CANCED;
										} else {

										}
									}
								}
							}

						}

					});

					break;
					case PAUSING:
                        Log.e(TAG, "PAUSING");
						break;

					case ANSWERING:
                        Log.e(TAG, "ANSWERING");
						break;
				default:
					break;
				}
				if(!error.equals(CallError.ERROR_NONE)){
					Log.e("EMCall","error:" + error.toString());
				}
			}
		};
		EMChatManager.getInstance().addVoiceCallStateChangeListener(
                callStateListener);
	}

	Timer timer = null;
	int index = 0;

	private boolean checknetwork() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = manager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (info.isAvailable() && info.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	private ProgressDialog progress = null;
	private Timer mTimeoutTimer;
	private TimerTask mTimeoutTimerTask;
	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		key = 0;
		switch (v.getId()) {
		case R.id.play:
			if (!cameraHelper.isStarted()) {
				progress.setMessage(getString(R.string.calling));
				progress.show();
                mTimeoutTimer = new Timer();
                mTimeoutTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (progress != null) {
                                    progress.dismiss();
                                }
                                if (hide_timer != null) {
                                    hide_timer.cancel();
                                    key = 0;
                                }
                                enHandler.sendEmptyMessage(4);
                                isconnected = false;
                                if (cameraHelper != null && cameraHelper.isStarted()) {
                                    cameraHelper.stopCapture(oppositeSurfaceHolder);
                                }
                                if (oppositeSurface != null && localSurface != null) {
                                    oppositeSurface.setVisibility(View.GONE);
                                    localSurface.setVisibility(View.INVISIBLE);
                                }
                                EMChatManager.getInstance().endCall();
                                ToastUtil.showtomain(ControlActivity.this, getString(R.string.connect_fail));
                            }
                        });
                    }
                };
				mTimeoutTimer.schedule(mTimeoutTimerTask, 40000);
				audioManager.setMicrophoneMute(true);
				mMuteBtn.setBackgroundResource(R.drawable.icon_mute_on);
				sendmsg();
				sendmsg(runningMode, username);
			} else {
				progress.setMessage(getString(R.string.hang_uping));
				progress.show();
				play.setBackgroundResource(R.drawable.bofang);
                if (!runningMode.equals("control")) {
                    speak.setVisibility(View.VISIBLE);
                }
				mMuteBtn.setVisibility(View.GONE);
				localSurface.setVisibility(View.INVISIBLE);
				oppositeSurface.setVisibility(View.GONE);
				EMChatManager.getInstance().endCall();

				EMMessage msg = EMMessage.createSendMessage(Type.CMD);
				msg.setReceipt(username);
				CmdMessageBody cmd = new CmdMessageBody(
						"yongyida.robot.video.closevideo");
				msg.addBody(cmd);
				EMChatManager.getInstance().sendMessage(msg, new EMCallBack() {

					@Override
					public void onSuccess() {
						cameraHelper.stopCapture(oppositeSurfaceHolder);
						
						timer = new Timer();
						timer.schedule(new TimerTask() {

							@Override
							public void run() {
								if (index >= 3) {
									index = 0;
									timer.cancel();
								} else {
									index++;
								}

							}
						}, new Date(), 1000);
					}

					@Override
					public void onProgress(int arg0, String arg1) {
					}

					@Override
					public void onError(int arg0, String arg1) {
					}
				});

			}
			break;
		case R.id.speak:
			if (audioManager.isMicrophoneMute()) {
				audioManager.setMicrophoneMute(false);
			}
			speak.setEnabled(false);
			audioManager.setSpeakerphoneOn(false);
			SpeechUtility.createUtility(this, "appid="
					+ getString(R.string.app_id));
			EMChatManager.getInstance().pauseVoiceTransfer();
			if (mDialog == null) {
				mDialog = new RecognizerDialog(ControlActivity.this, init);

				Utils.SystemLanguage language = Utils.getLanguage(ControlActivity.this);
				if (Utils.SystemLanguage.CHINA.equals(language)) {
					mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
				} else if (Utils.SystemLanguage.ENGLISH.equals(language)) {
					mDialog.setParameter(SpeechConstant.LANGUAGE, "en_us");
				}
			//	mDialog.setParameter(SpeechConstant.ACCENT, "vinn");
				mDialog.setParameter("asr_sch", "1");
				mDialog.setParameter("nlp_version", "2.0");
				mDialog.setParameter("dot", "0");
			}
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					if (mDialog != null && mDialog.isShowing()) {
						enHandler.sendEmptyMessage(5);
						mDialog.dismiss();
					}
				}
			}, 15000);
			mDialog.setListener(new RecognizerDialogListener() {

				@Override
				public void onResult(RecognizerResult result, boolean arg1) {
					Log.d("Result", "result:" + result.getResultString());
					try {
						JSONObject jo = new JSONObject(result.getResultString());
						String text = jo.getString("text");
						Constants.text = text;
						Intent intent = new Intent();
						intent.setAction(Constants.Speech_action);
						sendBroadcast(intent);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					enHandler.sendEmptyMessage(5);
				}

				@Override
				public void onError(SpeechError error) {
					Log.d("errorXF", "errorXF:" + error.getErrorDescription());
					enHandler.sendEmptyMessage(5);
				}
			});
			mDialog.show();
			mDialog.setCancelable(false);
			speak.setImageResource(R.drawable.dianjishi);
			break;
		case R.id.back:
			this.onBackPressed();
			break;
		case R.id.opposite_surface:
			if (isconnected) {
				if (mMoveTableLayout.getVisibility() == View.GONE) {
					toggle();
				} else {
					if (play.getVisibility() != View.GONE)
						play.setVisibility(View.GONE);
					else
						play.setVisibility(View.VISIBLE);
				}
			}

			break;
		}

	}

	private void huanxinLogin() {
		EMChatManager.getInstance().login(
				getSharedPreferences("huanxin", MODE_PRIVATE)
						.getString("username", null),
				getSharedPreferences("huanxin", MODE_PRIVATE)
						.getString("password", null),
				new EMCallBack() {

					@Override
					public void onSuccess() {
						ToastUtil.showtomain(ControlActivity.this, getString(R.string.connect_error));
						finish();
					}

					@Override
					public void onProgress(int arg0, String arg1) {
					}

					@Override
					public void onError(int arg0, String arg1) {
						ToastUtil.showtomain(ControlActivity.this, getString(R.string.connect_error));
						finish();
					}
				});
	}

	private void toggle() {
		if (mMoveTableLayout.getVisibility() == View.VISIBLE) {
			mHeadTableLayout.setVisibility(View.GONE);
            mMoveTableLayout.setVisibility(View.GONE);
        //    if (runningMode.equals("control")) {
                speak.setVisibility(View.GONE);
        //    }
		//	if (!getIntent().getExtras().getString("mode").equals("control")) {
			mMuteBtn.setVisibility(View.GONE);
		//	}
			play.setVisibility(View.GONE);
		} else {
            if (!TextUtils.isEmpty(mRobotName) && Utils.isSeries(mRobotName, "20")) {
				mHeadTableLayout.setVisibility(View.GONE);
			} else {
				mHeadTableLayout.setVisibility(View.VISIBLE);
			}
            mMoveTableLayout.setVisibility(View.VISIBLE);
            if (runningMode.equals("control")) {
                speak.setVisibility(View.VISIBLE);
            }
	//		if (!getIntent().getExtras().getString("mode").equals("control")) {
			mMuteBtn.setVisibility(View.VISIBLE);
	//		}
			play.setVisibility(View.VISIBLE);
		}
	}

	public void resume() {
		speak.setEnabled(true);
		speak.setImageResource(R.drawable.speech);
		audioManager.setSpeakerphoneOn(true);
		if (getIntent().getExtras().getString("mode").equals("control")) {
			audioManager.setMicrophoneMute(true);
		} else {
			audioManager.setMicrophoneMute(controlMute);
		}
		mDialog.setCancelable(true);
		EMChatManager.getInstance().resumeVoiceTransfer();
	}

	private boolean controlMute = true; //监控静音标识 true表示静音状态


	/**
	 * 视频状态下 静音按钮控制机器人和手机端麦克风同时静音或者不静音
	 * 监控状态下 手机端除了讲话的时候以外都是静音的  按钮只控制机器人端静音与否
	 * @param view
	 */
	public void toggle_speak(View view) {
		if (runningMode.equals("control")) {
			if (controlMute) {
				sendMuteMsg(false);
				view.setBackgroundResource(R.drawable.icon_mute_normal);
			} else {
				sendMuteMsg(true);
				view.setBackgroundResource(R.drawable.icon_mute_on);
			}
		} else {
			if (controlMute) {
				audioManager.setMicrophoneMute(false);
				sendMuteMsg(false);
				view.setBackgroundResource(R.drawable.icon_mute_normal);
			} else {
				audioManager.setMicrophoneMute(true);
				sendMuteMsg(true);
				view.setBackgroundResource(R.drawable.icon_mute_on);
			}
		}
		controlMute= !controlMute;
	}

	
	
	@Override
	protected void onDestroy() {
		HXSDKHelper.getInstance().isVideoCalling = false;

		if (time != null) {
			time.cancel();
		}
		try {
			callHelper.setSurfaceView(null);
			cameraHelper.stopCapture(oppositeSurfaceHolder);
			oppositeSurface = null;
			cameraHelper = null;
			EMChatManager.getInstance().endCall();
			saveCallRecord(1);
		} catch (Exception e) {
		}
		try {
			if (neterror != null) {
				unregisterReceiver(neterror);
			}
			if (mRNameBR != null) {
				unregisterReceiver(mRNameBR);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

	//	unregisterReceiver(headsetPlugReceiver);  
		super.onDestroy();

	}

	@Override
	protected void onPause() {
		EMChatManager.getInstance().endCall();
		saveCallRecord(1);
		cameraHelper.stopCapture(oppositeSurfaceHolder);
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		if (isconnected) {
			ToastUtil.showtomain(this, getString(R.string.hang_up_video_first));
			return;
		}
		cameraHelper.stopCapture(oppositeSurfaceHolder);
		audioManager.setMicrophoneMute(true);
		finish();
		super.onBackPressed();
	}

}
