package com.yongyida.robot.ronglianyun;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.yongyida.robot.R;
import com.yongyida.robot.activity.InviteActivity;
import com.yongyida.robot.huanxin.DemoApplication;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.Utils;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECInitParams;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.ECNotifyOptions;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.OnChatReceiveListener;
import com.yuntongxun.ecsdk.OnMeetingListener;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.im.ECMessageNotify;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;
import com.yuntongxun.ecsdk.im.group.ECGroupNoticeMessage;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneMeetingMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingMsg;
import com.yuntongxun.ecsdk.meeting.voice.ECVoiceMeetingMsg;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/6/6 0006.
 */
public class SDKCoreHelper implements ECDevice.InitListener , ECDevice.OnLogoutListener{
    public static final String TAG = "SDKCoreHelper";
    public static final String ACTION_SDK_CONNECT = "com.yuntongxun.Intent_Action_SDK_CONNECT";
    public static final String ACTION_KICK_OFF = "com.yuntongxun.Intent_ACTION_KICK_OFF";
    private static SDKCoreHelper sInstance;
    private Context mContext;
    private ECDevice.ECConnectState mConnect = ECDevice.ECConnectState.CONNECT_FAILED;
    private ECInitParams mInitParams;
    private ECInitParams.LoginMode mMode = ECInitParams.LoginMode.FORCE_LOGIN;
    private boolean mBusyFlag = false;
    /**初始化错误*/
    public static final int ERROR_CODE_INIT = -3;

    public static final int WHAT_SHOW_PROGRESS = 0x101A;
    public static final int WHAT_CLOSE_PROGRESS = 0x101B;
    private ECNotifyOptions mOptions;
    public static SoftUpdate mSoftUpdate;
    private List<OnMeetingListener> mOnMeetingListenerList;
    private List<OnChatReceiveListener> mOnChatReceiveListenerList;
    private List<ECVoIPCallManager.OnVoIPListener> mOnVoIPListenerList;


    private Handler handler;
    private SDKCoreHelper() {
        initNotifyOptions();
        mOnChatReceiveListenerList = new ArrayList<OnChatReceiveListener>();
        mOnMeetingListenerList = new ArrayList<OnMeetingListener>();
        mOnVoIPListenerList = new ArrayList<ECVoIPCallManager.OnVoIPListener>();
    }

    public static SDKCoreHelper getInstance() {
        if (sInstance == null) {
            sInstance = new SDKCoreHelper();
        }
        return sInstance;
    }

    public synchronized void setHandler(final Handler handler) {
        this.handler = handler;
    }


    public static void init(Context ctx) {
        init(ctx, ECInitParams.LoginMode.AUTO);
    }

    public static void init(Context ctx , ECInitParams.LoginMode mode) {
        Log.d(TAG, "[init] start regist..");
        ctx = DemoApplication.applicationContext;
        getInstance().mMode = mode;
        getInstance().mContext = ctx;
        // 判断SDK是否已经初始化，没有初始化则先初始化SDK
        if(!ECDevice.isInitialized()) {
            getInstance().mConnect = ECDevice.ECConnectState.CONNECTING;
            // ECSDK.setNotifyOptions(getInstance().mOptions);
            ECDevice.initial(ctx, getInstance());

            return ;
        }
        Log.d(TAG, " SDK has inited , then regist..");
        // 已经初始化成功，直接进行注册
        if (!TextUtils.isEmpty(Utils.getAccount(ctx))) {
            getInstance().onInitialized();
        }
    }

    public static void setSoftUpdate(String version , String desc , boolean mode) {
        mSoftUpdate = new SoftUpdate(version ,desc ,mode);
    }

    private void initNotifyOptions() {
        if(mOptions == null) {
            mOptions = new ECNotifyOptions();
        }
        // 设置新消息是否提醒
        mOptions.setNewMsgNotify(true);
        // 设置状态栏通知图标
        mOptions.setIcon(R.drawable.ic_launcher);
        // 设置是否启用勿扰模式（不会声音/震动提醒）
        mOptions.setSilenceEnable(false);
        // 设置勿扰模式时间段（开始小时/开始分钟-结束小时/结束分钟）
        // 小时采用24小时制
        // 如果设置勿扰模式不启用，则设置勿扰时间段无效
        // 当前设置晚上11点到第二天早上8点之间不提醒
        mOptions.setSilenceTime(23, 0, 8, 0);
        // 设置是否震动提醒(如果处于免打扰模式则设置无效，没有震动)
        mOptions.enableShake(true);
        // 设置是否声音提醒(如果处于免打扰模式则设置无效，没有声音)
        mOptions.enableSound(true);
    }

    @Override
    public void onInitialized() {
        Log.d(TAG, "ECSDK is ready");

        // 设置消息提醒
        ECDevice.setNotifyOptions(mOptions);
        // 设置接收VoIP来电事件通知Intent
        // 呼入界面activity、开发者需修改该类
        Intent intent = new Intent(getInstance().mContext, InviteActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity( getInstance().mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ECDevice.setPendingIntent(pendingIntent);

        ECDevice.setOnDeviceConnectListener(new MyOnDeviceConnectListener());
        ECDevice.setOnChatReceiveListener(new MyOnChatReceiveListener());

        // 设置VOIP 自定义铃声路径
        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if(setupManager != null) {
            // 目前支持下面三种路径查找方式
            // 1、如果是assets目录则设置为前缀[assets://]
            setupManager.setInComingRingUrl(true, "assets://phonering.mp3");
            setupManager.setOutGoingRingUrl(true, "assets://phonering.mp3");
            setupManager.setBusyRingTone(true, "assets://playend.mp3");
            // 2、如果是raw目录则设置为前缀[raw://]
            // 3、如果是SDCard目录则设置为前缀[file://]
        }

        if(ECDevice.getECMeetingManager() != null) {
            ECDevice.getECMeetingManager().setOnMeetingListener(new MyOnMeetingListener());
        }

        if(getVoIPCallManager() != null) {
            getVoIPCallManager().setOnVoIPCallListener(new MyOnVoIPListener());
        }

        // 构建注册所需要的参数信息
        //5.0.3的SDK初始参数的方法：ECInitParams params = new ECInitParams();
        //5.1.*以上版本如下：
        ECInitParams params = ECInitParams.createParams();
        //自定义登录方式：
        //测试阶段Userid可以填写手机
        params.setUserid(Utils.getAccount(mContext));
        params.setAppKey("8a216da854e74cfc0154e78c05ce00a9");
        params.setToken("9ffc34b2731a6d2f5aa7f53502863918");
        // 设置登陆验证模式（是否验证密码）NORMAL_AUTH-自定义方式
        params.setAuthType(ECInitParams.LoginAuthType.NORMAL_AUTH);
        // 1代表用户名+密码登陆（可以强制上线，踢掉已经在线的设备）
        // 2代表自动重连注册（如果账号已经在其他设备登录则会提示异地登陆）
        // 3 LoginMode（强制上线：FORCE_LOGIN  默认登录：AUTO）
        params.setMode(ECInitParams.LoginMode.FORCE_LOGIN);
        ECDevice.login(params);
        Log.d(TAG, "login");
    }

    public void registOnVoIPCallListener(ECVoIPCallManager.OnVoIPListener listener) {
        if (mOnVoIPListenerList != null && !mOnVoIPListenerList.contains(listener)) {
            mOnVoIPListenerList.add(listener);
        }
    }

    public void unRegistOnVoIPCallListener(ECVoIPCallManager.OnVoIPListener listener) {
        if (mOnVoIPListenerList != null && mOnVoIPListenerList.contains(listener)) {
            mOnVoIPListenerList.remove(listener);
        }
    }

    public void setmBusyFlag(boolean mBusyFlag) {
        this.mBusyFlag = mBusyFlag;
    }

    private class MyOnVoIPListener implements  ECVoIPCallManager.OnVoIPListener {

        @Override
        public void onDtmfReceived(String s, char c) {

        }

        @Override
        public void onCallEvents(ECVoIPCallManager.VoIPCall voIPCall) {
            if (mOnVoIPListenerList == null || mOnVoIPListenerList.size() == 0) {
                return;
            }
            for (ECVoIPCallManager.OnVoIPListener listener : mOnVoIPListenerList) {
                listener.onCallEvents(voIPCall);
            }
        }

        @Override
        public void onSwitchCallMediaTypeRequest(String s, ECVoIPCallManager.CallType callType) {

        }

        @Override
        public void onSwitchCallMediaTypeResponse(String s, ECVoIPCallManager.CallType callType) {

        }

        @Override
        public void onVideoRatioChanged(VideoRatio videoRatio) {

        }
    }

    private class MyOnChatReceiveListener implements OnChatReceiveListener {

        @Override
        public void OnReceivedMessage(ECMessage ecMessage) {
            if(ecMessage.getType() == ECMessage.Type.TXT) {
                // 在这里处理文本消息
                ECTextMessageBody textMessageBody = (ECTextMessageBody) ecMessage.getBody();
                if (textMessageBody != null) {
                    if (!mBusyFlag) {
                        if (ecMessage.getUserData().equals(Constants.MESSAGE_PREFIX + Constants.INVITE_MESSAGE)) {
                            Intent intent = new Intent(mContext, InviteActivity.class);
                            intent.putExtra(Constants.CALL_NO, ecMessage.getForm());
                            intent.putExtra(Constants.CONFERENCE_ID, textMessageBody.getMessage());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                            return;
                        }
                    }
                }
            }

            if (mOnChatReceiveListenerList != null && mOnChatReceiveListenerList.size() != 0) {
                for (OnChatReceiveListener listener : mOnChatReceiveListenerList) {
                    listener.OnReceivedMessage(ecMessage);
                }
            }
        }

        @Override
        public void onReceiveMessageNotify(ECMessageNotify ecMessageNotify) {
            if (mOnChatReceiveListenerList != null && mOnChatReceiveListenerList.size() != 0) {
                for (OnChatReceiveListener listener : mOnChatReceiveListenerList) {
                    listener.onReceiveMessageNotify(ecMessageNotify);
                }
            }
        }

        @Override
        public void OnReceiveGroupNoticeMessage(ECGroupNoticeMessage ecGroupNoticeMessage) {
            if (mOnChatReceiveListenerList != null && mOnChatReceiveListenerList.size() != 0) {
                for (OnChatReceiveListener listener : mOnChatReceiveListenerList) {
                    listener.OnReceiveGroupNoticeMessage(ecGroupNoticeMessage);
                }
            }
        }

        @Override
        public void onOfflineMessageCount(int i) {
        }

        @Override
        public int onGetOfflineMessage() {
            return 0;
        }

        @Override
        public void onReceiveOfflineMessage(List<ECMessage> list) {

        }

        @Override
        public void onReceiveOfflineMessageCompletion() {

        }

        @Override
        public void onServicePersonVersion(int i) {

        }

        @Override
        public void onReceiveDeskMessage(ECMessage ecMessage) {

        }

        @Override
        public void onSoftVersion(String s, int i) {

        }
    }

    private class MyOnMeetingListener implements OnMeetingListener{

        @Override
        public void onReceiveInterPhoneMeetingMsg(ECInterPhoneMeetingMsg ecInterPhoneMeetingMsg) {
            if (mOnMeetingListenerList != null && mOnMeetingListenerList.size() != 0) {
                for (OnMeetingListener listener : mOnMeetingListenerList) {
                    listener.onReceiveInterPhoneMeetingMsg(ecInterPhoneMeetingMsg);
                }
            }
        }

        @Override
        public void onReceiveVoiceMeetingMsg(ECVoiceMeetingMsg ecVoiceMeetingMsg) {
            if (mOnMeetingListenerList != null && mOnMeetingListenerList.size() != 0) {
                for (OnMeetingListener listener : mOnMeetingListenerList) {
                    listener.onReceiveVoiceMeetingMsg(ecVoiceMeetingMsg);
                }
            }
        }

        @Override
        public void onReceiveVideoMeetingMsg(ECVideoMeetingMsg ecVideoMeetingMsg) {
            if (mOnMeetingListenerList != null && mOnMeetingListenerList.size() != 0) {
                for (OnMeetingListener listener : mOnMeetingListenerList) {
                    listener.onReceiveVideoMeetingMsg(ecVideoMeetingMsg);
                }
            }
        }

        @Override
        public void onVideoRatioChanged(VideoRatio videoRatio) {
            if (mOnMeetingListenerList != null && mOnMeetingListenerList.size() != 0) {
                for (OnMeetingListener listener : mOnMeetingListenerList) {
                    listener.onVideoRatioChanged(videoRatio);
                }
            }
        }
    }

    private class MyOnDeviceConnectListener implements  ECDevice.OnECDeviceConnectListener{

        @Override
        public void onConnect() {

        }

        @Override
        public void onDisconnect(ECError ecError) {

        }

        @Override
        public void onConnectState(ECDevice.ECConnectState ecConnectState, ECError ecError) {
            getInstance().mConnect = ecConnectState;
            if (ecConnectState == ECDevice.ECConnectState.CONNECT_SUCCESS) {
                Log.d(TAG, "CONNECT_SUCCESS");
            } else if (ecConnectState == ECDevice.ECConnectState.CONNECTING) {
                Log.d(TAG, "CONNECTING");
            } else if (ecConnectState == ECDevice.ECConnectState.CONNECT_FAILED) {
                if (ecError.errorCode == SdkErrorCode.SDK_KICKED_OFF) {
                    Log.e(TAG, "SDK_KICKED_OFF");
                } else {
                    Log.e(TAG, "CONNECT_FAILED");
                }
            }
        }
    }

    /**
     * 当前SDK注册状态
     * @return
     */
    public static ECDevice.ECConnectState getConnectState() {
        return getInstance().mConnect;
    }

    @Override
    public void onLogout() {
        Log.e(TAG, "logout success");
        getInstance().mConnect = ECDevice.ECConnectState.CONNECT_FAILED;
        if(mInitParams != null && mInitParams.getInitParams() != null) {
            mInitParams.getInitParams().clear();
        }
        mInitParams = null;
    }

    @Override
    public void onError(Exception exception) {
        Log.e(TAG, "ECSDK couldn't start: " + exception.getLocalizedMessage());
//        Intent intent = new Intent(ACTION_SDK_CONNECT);
//        intent.putExtra("error", ERROR_CODE_INIT);
//        mContext.sendBroadcast(intent);
        ECDevice.unInitial();
    }


    public static void logout(boolean isNotice) {
        ECDevice.NotifyMode notifyMode = (isNotice) ? ECDevice.NotifyMode.IN_NOTIFY : ECDevice.NotifyMode.NOT_NOTIFY;
        ECDevice.logout(notifyMode, getInstance());

    }



    /**
     * VoIP呼叫接口
     * @return
     */
    public static ECVoIPCallManager getVoIPCallManager() {
        return ECDevice.getECVoIPCallManager();
    }

    public static ECVoIPSetupManager getVoIPSetManager() {
        return ECDevice.getECVoIPSetupManager();
    }

    public void registOnMeetingListener(OnMeetingListener listener){
        if (mOnMeetingListenerList != null && !mOnMeetingListenerList.contains(listener)) {
            mOnMeetingListenerList.add(listener);
        }
    }

    public void unRegistOnMeetingListener(OnMeetingListener listener) {
        if (mOnMeetingListenerList != null && mOnMeetingListenerList.contains(listener)) {
            mOnMeetingListenerList.remove(listener);
        }
    }

    public void registOnChatReceiveListener(OnChatReceiveListener listener) {
        if (mOnChatReceiveListenerList != null && !mOnChatReceiveListenerList.contains(listener)) {
            mOnChatReceiveListenerList.add(listener);
        }
    }

    public void unRegistOnChatReceiveListener(OnChatReceiveListener listener) {
        if (mOnChatReceiveListenerList != null && mOnChatReceiveListenerList.contains(listener)) {
            mOnChatReceiveListenerList.remove(listener);
        }
    }

    public static class SoftUpdate  {
        public String version;
        public String desc;
        public boolean force;

        public SoftUpdate(String version ,String desc, boolean force) {
            this.version = version;
            this.force = force;
            this.desc = desc;
        }
    }

    /**
     *
     * @return返回底层so库 是否支持voip及会议功能
     * true 表示支持 false表示不支持
     * 请在sdk初始化完成之后调用
     */
    public boolean isSupportMedia(){

        return ECDevice.isSupportMedia();
    }

    public static boolean hasFullSize(String inStr) {
        if (inStr.getBytes().length != inStr.length()) {
            return true;
        }
        return false;
    }

    /**
     * 判断服务是否自动重启
     * @return 是否自动重启
     */
    public static boolean isUIShowing() {
        return ECDevice.isInitialized();
    }
}
