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
package com.yongyida.robot.huanxin;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.easemob.EMCallBack;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.yongyida.robot.utils.MyCrashHandler;
import com.yongyida.robot.utils.NetUtil;
import com.yongyida.robot.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class DemoApplication extends Application {

    private static final String TAG = "DemoApplication";
    public static Context applicationContext;
	private static DemoApplication instance;
	// login user name
	public final String PREF_USERNAME = "username";
	public static final Logger logger = LoggerFactory
			.getLogger(DemoApplication.class);
	private List<Activity> mActivityList = new ArrayList<>();

	public void addActivity(Activity activity){
		if (mActivityList != null) {
			mActivityList.add(activity);
		}
	}
	public void removeActivity(Activity activity){
		if (mActivityList != null) {
			mActivityList.remove(activity);
		}
	}
	public void finishActivity(){
		if (mActivityList != null) {
			for (Activity activity : mActivityList) {
				if (null != activity) {
					activity.finish();
				}
			}
		}
	}

	public void exitApp(){
		finishActivity();
		//杀死该应用进程
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	/**
	 * 当前用户nickname,为了苹果推送不是userid而是昵称
	 */
	public static String currentUserNick = "";
	public static DemoHXSDKHelper hxSDKHelper = new DemoHXSDKHelper();
	static int flag = 1;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "app create");
		MyCrashHandler myCrashHandler = MyCrashHandler.getInstance();
		myCrashHandler.init(this);
//		if (LeakCanary.isInAnalyzerProcess(this)) {
//			// This process is dedicated to LeakCanary for heap analysis.
//			// You should not init your app in this process.
//			return;
//		}
//		LeakCanary.install(this);
		applicationContext = this;
		instance = this;
		new NetUtil();
	//	LogHelper.getInstance(instance).start();
	//	PropertyConfigurator.getConfigurator(this).configure();
		String net_state = getSharedPreferences("net_state", 0).getString(
				"state", null);
		if (net_state == null) {
			getSharedPreferences("net_state", MODE_PRIVATE).edit()
					.putString("state", "official").commit();
		}

        Utils.SystemLanguage language = Utils.getLanguage(this);
        if (Utils.SystemLanguage.ENGLISH.equals(language)) {
            Utils.switchServer(Utils.US);
        } else {
            String serverState = getSharedPreferences("net_state", MODE_PRIVATE).getString("state",null);
            if (serverState != null && !serverState.equals("official")){
                if (serverState.equals("test")) {
                    Utils.switchServer(Utils.TEST);
                } else if (serverState.equals("test1")){
                    Utils.switchServer(Utils.TEST1);
                }
            } else {
                Utils.switchServer(Utils.CN);
            }
        }

		/**
		 * this function will initialize the HuanXin SDK
		 *
		 * @return boolean true if caller can continue to call HuanXin related
		 *         APIs after calling onInit, otherwise false.
		 *
		 *         环信初始化SDK帮助函数
		 *         返回true如果正确初始化，否则false，如果返回为false，请在后续的调用中不要调用任何和环信相关的代码
		 *
		 *         for example: 例子：
		 *
		 *         public class DemoHXSDKHelper extends HXSDKHelper
		 *
		 *         HXHelper = new DemoHXSDKHelper();
		 *         if(HXHelper.onInit(context)){ // do HuanXin related work }
		 */
		hxSDKHelper.onInit(applicationContext);
	}

	public static DemoApplication getInstance() {
		return instance;
	}

	/**
	 * 获取当前登陆用户名
	 * 
	 * @return
	 */
	public String getUserName() {
		return hxSDKHelper.getHXId();
	}

	/**
	 * 获取密码
	 * 
	 * @return
	 */
	public String getPassword() {
		return hxSDKHelper.getPassword();
	}

	/**
	 * 设置用户名
	 * 
	 * @param username
	 */
	public void setUserName(String username) {
		hxSDKHelper.setHXId(username);
	}

	/**
	 * 设置密码 下面的实例代码 只是demo，实际的应用中需要加password 加密后存入 preference 环信sdk
	 * 内部的自动登录需要的密码，已经加密存储了
	 * 
	 * @param pwd
	 */
	public void setPassword(String pwd) {
		hxSDKHelper.setPassword(pwd);
	}

	/**
	 * 退出登录,清空数据
	 */
	public void logout(final boolean isGCM, final EMCallBack emCallBack) {
		// 先调用sdk logout，在清理app中自己的数据
		hxSDKHelper.logout(isGCM, emCallBack);
	}

    private String agoraChannelId; //视频邀请推送时需要保存的参数
    private long agoraId;          //视频邀请推送时需要保存的参数
    private boolean fromPush;      //是否是点击推送启动

    public String getAgoraChannelId() {
        return agoraChannelId;
    }

    public void setAgoraChannelId(String agoraChannelId) {
        this.agoraChannelId = agoraChannelId;
    }

    public long getAgoraId() {
        return agoraId;
    }

    public void setAgoraId(long agoraId) {
        this.agoraId = agoraId;
    }

    public boolean isFromPush() {
        return fromPush;
    }

    public void setFromPush(boolean fromPush) {
        this.fromPush = fromPush;
    }
}
