package com.yongyida.robot.videohelper;

import android.content.Context;
import android.view.SurfaceView;

import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

/**
 * Created by Administrator on 2016/8/30 0030.
 */
public class VideoEngine {
    public static final String VendorKey = "6bb94525b3ba490b81d38d2555e41419";
 //   public static final String VendorKey = "dbdd7b8761bd4ed99cb41ed936420867";
    private Context mContext;
    private RtcEngine rtcEngine;
    private MessageHandler messageHandler;
    private static VideoEngine mInstance = null;

    private VideoEngine(Context context) {
        mContext = context;
        init();
    }

    public static VideoEngine create(Context context){
        if(mInstance==null){
            synchronized(VideoEngine.class){
                if(mInstance==null){
                    mInstance=new VideoEngine(context);
                }
            }
        }
        return mInstance;
    }

    private void init(){
        if(rtcEngine == null) {
            messageHandler = new MessageHandler();
            rtcEngine = RtcEngine.create(mContext, VendorKey, messageHandler);
        }
    }

    public void setEngineEventHandlerMeetingListener(MeetingListener listener){
        messageHandler.setMeetingListener(listener);
    }


    public SurfaceView getSurfaceView(Context context){
        return rtcEngine.CreateRendererView(context);
    }

    /**
     *加入频道
     * @param key
     *     此为程序动态生成的 Token;
     *     当用户使用静态 Key 也即只使用 vendor key 时, 该参数是可选的,传NULL 即可;
     *     当用户使用动态 Key 时,Agora 为应用程序开发者额外签发一个签名 秘钥 sign key,开发者通过 Agora 提供的算法和秘钥生成此用户 Token,用于服务器端用户验证。
     *     一般来说使用静态 Key 即可,对于安全有极高要求的使用者请联系 Agora 客服或销售人员获得动态 Key 使用支持。
     * @param channelName
     *     频道名称。可以是任何描述性的名称,如“游戏1”或“通话2”。Channel 的最大长度为128 个字符。
     * @param optionalInfo
     *     (非必选项) 开发者需加入的任何附加信息。一般可设置为空字符串,或频道相关信息。
     * @param optionalUid
     *     (非必选项) 用户ID,32 位无符号整数。建议设置范围:1 到 (2^32-1),并保证唯一性。如果不指定(即设为0),
     *     SDK 会自动分配一个,并在 onJoinChannelSuccess 回调方法中返回,App 层必须记住该返回值并维护,SDK 不对该返回值进行维护。
     *     注:uid 在SDK 内部用32 位无符号整数表示,由于Java 不支持无符号整数,uid 被当成32 位有符号整数处理,对于过大的整数,
     *     Java 会表示为负数,如有需要可以用(uid&0xffffffffL)转换成64 位整数。
     * @return
     *     0:方法调用成功
     *     <0:方法调用失败
     *    ERR_INVALID_ARGUMENT (-2):传递的参数无效 ERR_NOT_READY (-3):没有成功初始化
     *    ERR_REFUSED (-5):SDK不能发起通话,可能是因为处于另一个通话中,或者创建频道失败。
     */
    public int joinChannel(String key, String channelName, String optionalInfo, int optionalUid){
        return rtcEngine.joinChannel(key, channelName, optionalInfo, optionalUid);
    }

    /**
     * 离开频道
     * 离开频道,即挂断或退出通话。joinChannel 后,必须调用 leaveChannel 以结束通话,否 则不能进行下一次通话。
     * 不管当前是否在通话中,都可以调用 leaveChannel,没有副作用。 leaveChannel 会把会话相关的所有资源释放掉。
     * @return
     * 0:方法调用成功
     * <0:方法调用失败
     */
    public int leaveChannel(){
        return rtcEngine.leaveChannel();
    }

    /**
     * 开启视频模式
     * 该方法用于开启视频模式。可以在加入频道前或者通话中调用,在加入频道前调用,则自动开启 视频模式,在通话中调用则由音频模式切换为视频模式。
     * @return
     * 0:方法调用成功
     *<0:方法调用失败
     */
    public int enableVideo(){
        return rtcEngine.enableVideo();
    }

    /**
     *开启纯音频模式
     * 该方法用于关闭视频,开启纯音频模式。可以在加入频道前或者通话中调用,在加入频道前调用,
     * 则自动开启纯音频模式,在通话中调用则由视频模式切换为纯音频频模式。
     * @return
     * 0:方法调用成功
     *<0:方法调用失败
     */
    public int disableVideo(){
        return rtcEngine.disableVideo();
    }

    /**
     * 开启视频预览
     * 该方法用于启动本地视频预览。在开启预览前,必须先调用 setupLocalVideo 设置预览窗口 及属性,且必须调用 enableVideo 开启视频功能。
     * @return
     * 0:方法调用成功
     * <0:方法调用失败
     */
    public int startPreview(){
        return rtcEngine.startPreview();
    }

    /**
     * 停止视频预览
     * 该方法用于停止本地视频预览。
     * @return
     * 0:方法调用成功
     *<0:方法调用失败
     */
    public int stopPreview(){
        return rtcEngine.stopPreview();
    }

    /**
     * 监测网络连接状态
     * 监听网络连接状态事件,在加入通话前调用。默认监听。
     * @param monitor
     * True:监测网络连接状态 (默认)
     * False:不监测网络连接状态
     */
    public void monitorConnectionEvent(boolean monitor){
        rtcEngine.monitorConnectionEvent(monitor);
    }

    /**
     * 给通话评分
     * @param callId  通过 getCallId 函数获取的通话 ID
     * @param rating  给通话的评分,最低1 分,最高10 分
     * @param description  给通话的描述,可选,长度应小于800 字节
     * @return
     * 0:方法调用成功
     * <0:方法调用失败
     * ERR_INVALID_ARGUMENT (-2):传入的参数无效,比如 callId 无效。
     * ERR_NOT_READY (-3):SDK不在正确的状态,可能是因为没有成功初始化。
     */
    public int rate(String callId, int rating, String description) {
        return rtcEngine.rate(callId, rating, description);
    }

    /**
     *投诉通话质量
     * @param callId  通过 getCallId 函数获取的通话 ID
     * @param description  给通话的描述,可选,长度应小于800 字节
     * @return  0:方法调用成功
     *<0:方法调用失败
     *ERR_INVALID_ARGUMENT (-2):传入的参数无效,比如 callId 无效。
     *ERR_NOT_READY (-3):SDK不在正确的状态,可能是因为没有成功初始化
     */
    public int complain(String callId, String description) {
        return rtcEngine.complain(callId, description);
    }

    /**
     *设置本地视频显示属性
     * 该方法设置本地视频显示信息。应用程序通过调用此接口绑定本地视频流的显示视窗(view),
     * 并 设置视频显示模式。在应用程序开发中, 通常在初始化后调用该方法进行本地视频设置,然后再 加入频道。
     * @param local
     * 设置视频属性。Class VideoCanvas:
        ·view: 视频显示视窗。
        ·enderMode: 视频显示模式。
        RENDER_MODE_HIDDEN (1):如果视频尺寸与显示视窗 尺寸不一致,则视   频流会按照显示视窗的比例进行周边裁 剪或图像拉伸后填满视窗。
        RENDER_MODE_FIT(2): 如果视频尺寸与显示视窗尺寸 不一致,在保持长宽比的前提下,将视频进行缩放后填满 视窗。
        RENDER_MODE_ADAPTIVE(3):如果自己和对方都是竖 屏,或者如果自己和对方都是横屏,使用 RENDER_MODE_HIDDEN;如果对方和自己一个竖屏一个 横屏,则使用 RENDER_MODE_FIT。
        ·uid: 本地用户ID, 与joinchannel 方法中的uid 保持一致。
     * @return  0:方法调用成功
        <0:方法调用失败
     */
    public int setupLocalVideo(VideoCanvas local) {
        return rtcEngine.setupLocalVideo(local);
    }

    /**
     * 设置远端视频显示属性
     * 该方法绑定远程用户和显示视图,即设定 uid 指定的用户用哪个视图显示。调用该接口时需要指 定远程视频的 uid,一般可以在进频道前提前设置好,如果应用程序不能事先知道对方的 uid,
     * 可以在 APP 收到 onUserJoined 事件时设置。解除某个用户的绑定视图可以把 view 设置为空。
     * @param remote
     * 设置视频属性。Class VideoCanvas:
        ·view: 视频显示视窗。
        ·renderMode: 视频显示模式。
        RENDER_MODE_HIDDEN (1):如果视频尺寸与显示视窗 尺寸不一致,则视频流会按照显示视窗的比例进行周边裁 剪或图像拉伸后填满视窗。
        RENDER_MODE_FIT(2): 如果视频尺寸与显示视窗尺寸 不一致,在保持长宽比的前提下,将视频进行缩放后填满 视窗。
        RENDER_MODE_ADAPTIVE(3):如果自己和对方都是竖 屏,或者如果自己和对方都是横屏,使用 RENDER_MODE_HIDDEN;如果对方和自己一个竖屏一个 横屏,则使用 RENDER_MODE_FIT。
        ·uid: 用户ID,指定远端视频来自哪个用户。
     * @return
     * 0:方法调用成功
        <0:方法调用失败
     */
    public int setupRemoteVideo(VideoCanvas remote) {
        return rtcEngine.setupRemoteVideo(remote);
    }

    /**
     * 设置本地视频显示模式
     * 该方法设置本地视频显示模式。应用程序可以多次调用此方法更改显示模式。
     * @param mode
     * 设置视频显示模式。
        RENDER_MODE_HIDDEN(1):如果视频尺寸与显示视窗 尺寸不一致,则视频流会按照显示视窗的比例进行周边裁 剪或图像拉伸后填满视窗。
        RENDER_MODE_FIT(2): 如果视频尺寸与显示视窗尺寸不一致,在保持长宽比的前提下,将视频进行缩放后填满视窗。
        RENDER_MODE_ADAPTIVE(3):如果自己和对方都是竖屏,或者如果自己和对方都是横屏,使用RENDER_MODE_HIDDEN;如果对方和自己一个竖屏一个横屏,则使用RENDER_MODE_FIT。
     * @return
     * 0:方法调用成功
        <0:方法调用失败
     */
    public int setLocalRenderMode(int mode) {
        return rtcEngine.setLocalRenderMode(mode);
    }

    /**
     * 设置远端视频显示模式
     * 该方法设置远端视频显示模式。应用程序可以多次调用此方法更改显示模式。
     * @param uid  用户 ID
     * @param mode  设置视频显示模式。
        RENDER_MODE_HIDDEN (1):如果视频尺寸与显示视窗尺寸不一致,则视频流会按照显示视窗的比例进行周边裁 剪或图像拉伸后填满视窗。
        RENDER_MODE_FIT(2): 如果视频尺寸与显示视窗尺寸不一致,在保持长宽比的前提下,将视频进行缩放后填满视窗。
        RENDER_MODE_ADAPTIVE(3):如果自己和对方都是竖 屏,或者如果自己和对方都是横屏,使用 RENDER_MODE_HIDDEN;如果对方和自己一个竖屏一个横屏,则使用 RENDER_MODE_FIT。
     * @return  0:方法调用成功
       <0:方法调用失败
     */
    public int setRemoteRenderMode(int uid, int mode) {
        return rtcEngine.setRemoteRenderMode(uid, mode);
    }

    /**
     * 切换前置/后置摄像头
     * @return
     * 0:方法调用成功
      <0:方法调用失败
     */
    public int switchCamera(){
        return rtcEngine.switchCamera();
    }

    /**
     * 设置本地视频属性
     * 该方法设置视频属性(Profile)。每个属性对应一套视频参数,如分辨率、帧率、码率等。
     注:应在调用 joinChannel 进入频道前设置视频属性
     * @param profile
     * 视频属性(Profile)。详见下表的定义。Profile 取值在io.agora.rtc. IRtcEngineEventHandler.VideoProfile 中定义。
     * @return
     * 0:方法调用成功
    <0:方法调用失败
     */
    public int setVideoProfile(int profile) {
        return rtcEngine.setVideoProfile(profile);
    }

    /**
     *暂停发送本地视频流
     * 暂停/恢复发送本地视频流。该方法用于允许/禁止往网络发送本地视频流。 注:该方法不影响本地视频流获取,没有禁用摄像头。
     * @param muted
     * True: 不发送本地视频流
    False: 发送本地视频流
     * @return
     */
    public int muteLocalVideoStream(boolean muted) {
        return rtcEngine.muteLocalVideoStream(muted);
    }

    /**
     * 暂停指定远端视频流
     * 暂停/恢复指定远端视频流。本方法用于允许/禁止播放指定远端视频流。 注:该方法不影响视频数据流的接收,只是不播放视频流。
     * @param uid  指定用户
     * @param muted  True: 停止播放接收到的视频流
    False: 允许播放接收到的视频流
     * @return
     */
    public int muteRemoteVideoStream(int uid, Boolean muted) {
        return rtcEngine.muteRemoteVideoStream(uid, muted);
    }

    /**
     * 暂停所有远端视频流
     * 暂停/恢复所有人视频流。本方法用于允许/禁止播放所有人的视频流。 注:该方法不影响视频数据流的接收,只是不播放视频流。
     * @param muted
     * True: 停止播放接收到的所有视频流
    False: 允许播放接收到的所有视频流
     * @return
     */
    public int muteAllRemoteVideoStreams(boolean muted) {
        return rtcEngine.muteAllRemoteVideoStreams(muted);
    }

    /**
     * 监测耳机插拔事件
     * 监听耳机插拔事件,在加入通话前调用。默认监听。
     * @param monitor
     * True:监听耳机插拔事件(默认)
        False:不监听耳机插拔事件
     */
    public void monitorHeadsetEvent(boolean monitor){
        rtcEngine.monitorHeadsetEvent(monitor);
    }

    /**
     *监测蓝牙耳机事件
     * 监听蓝牙耳机事件,在加入通话前调用。默认监听。
     * @param monitor
     * True:监听蓝牙耳机事件(默认)
    False:不监听蓝牙耳机事件
     */
    public void monitorBluetoothHeadsetEvent(boolean monitor) {
        rtcEngine.monitorBluetoothHeadsetEvent(monitor);
    }

    /**
     *扬声器通话
     * 切换音频输出方式:扬声器或听筒。
     * @param enabled
     * True:音频输出至扬声器
    False:音频输出至听筒
     * @return
     */
    public int setEnableSpeakerphone(boolean enabled) {
        return rtcEngine.setEnableSpeakerphone(enabled);
    }

    /**
     * 是否是扬声器状态
     * @return
     */
    public boolean isSpeakerphoneEnabled() {
        return rtcEngine.isSpeakerphoneEnabled();
    }

    /**
     * 设定扬声器音量
     * 该方法设定扬声器音量。
     * @param volume
     * 设定音量,最小为 0,最大为 255
     * @return
     */
    public int setSpeakerphoneVolume(int volume) {
        return rtcEngine.setSpeakerphoneVolume(volume);
    }

    /**
     *启用说话者音量提示
     * 该方法允许 SDK 定期向应用程序反馈当前谁在说话以及说话者的音量。
     * @param interval
     * 指定音量提示的时间间隔。
        <=0 :禁用音量提示功能
        >0 :提示间隔,单位为毫秒。建议设置到大于200 毫秒。
     * @param smooth
     * 平滑系数。默认可以设置为3。
     * @return
     */
    public int enableAudioVolumeIndication(int interval, int smooth) {
        return rtcEngine.enableAudioVolumeIndication(interval, smooth);
    }

    /**
     * 开始客户端录音
     * 开始录音。SDK 支持在通话中进行录音,录音文件的格式为 wav。应用程序必须保证指定的目录
     * 存在而且可写。该接口需要在 joinChannel 之后调用;在 leaveChannel 时如果还在录音, 会自动停止。
     * @param filePath 存储录音文件的文件路径。
     * @return
     */
    public int startAudioRecording(String filePath) {
        return rtcEngine.startAudioRecording(filePath);
    }

    /**
     *停止客户端录音
     * 停止录音。该接口需要在 leaveChannel 之前调用,如果没有调用,在 leaveChannel 时会 自动停止。
     * @return
     */
    public int stopAudioRecording() {
        return rtcEngine.stopAudioRecording();
    }

    /**
     *设置日志过滤器
     * @param filter
     * 过滤器:
    1: INFO
    2: WARNING
    4: ERROR
    8: FATAL
    0x800: DEBUG
     * @return
     */
    public int setLogFilter(int filter) {
        return rtcEngine.setLogFilter(filter);
    }

    /**
     * 设置日志文件
     * 设置 SDK 的输出 log 文件。SDK 运行时产生的所有 log 将写入该文件。应用程序必须保证指定的 目录存在而且可写。
     * @param filePath  log 文件的全路径名
     * @return
     */
    public int setLogFile(String filePath) {
        return rtcEngine.setLogFile(filePath);
    }

    /**
     * 启动语音通话测试
     * 该方法启动回声测试,目的是测试系统的音频设备(耳麦、扬声器等)和网络连接是否正常。在 测试过程中,用户先说一段话,在 10 秒后,
     * 声音会回放出来。如果 10 秒后用户能正常听到自己 刚才说的话,就表示系统音频设备和网络连接都是正常的。
     注:调用 startEchoTest 后必须调用 stopEchoTest 以结束测试,否则不能进行下一次回声 测试,或者调用 joinChannel 进行通话。
     * @return
     * 0:方法调用成功
    <0:方法调用失败
    ERR_REFUSED (-5):不能启动测试,可能没有成功初始化。
     */
    public int startEchoTest() {
        return rtcEngine.startEchoTest();
    }

    /**
     * 终止回声测试
     * 该方法停止回声测试。
     * @return
     */
    public int stopEchoTest() {
        return rtcEngine.startEchoTest();
    }

    /**
     * 将自己静音
     * 静音/取消静音。该方法用于允许/禁止往网络发送本地音频流。
     注:该方法不影响录音状态,并没有禁用麦克风
     * @param muted  True:麦克风静音
        False:取消静音
     * @return
     */
    public int muteLocalAudioStream(boolean muted) {
        return rtcEngine.muteLocalAudioStream(muted);
    }

    /**
     *静音所有远端音频
     * 静音所有远端用户/对所有远端用户取消静音。本方法用于允许/禁止播放远端用户的音频流。 注:该方法不影响音频数据流的接收,只是不播放音频流。
     * @param muted  True:麦克风静音
    False:取消静音
     * @return
     */
    public int muteAllRemoteAudioStreams(boolean muted) {
        return rtcEngine.muteAllRemoteAudioStreams(muted);
    }

    /**
     * 静音指定用户音频
     * 静音指定远端用户/对指定远端用户取消静音。本方法用于允许/禁止播放远端用户的音频流。 注:该方法不影响音频数据流的接收,只是不播放音频流。
     * @param uid  指定用户
     * @param muted  True:麦克风静音
    False:取消静音
     * @return
     */
    public int muteRemoteAudioStream(int uid, boolean muted) {
        return rtcEngine.muteRemoteAudioStream(uid, muted);
    }

    /**
     * 设置频道通话模式
     * 该方法用于设置频道模式(Profile)。为了更好的优化,Agora RtcEngine 需要知道应用程序的使 用场景
     * (比如群聊模式还是主播模式),从而使用不同的优化手段。目前支持三种 Profile:自由 (free)、主播(broadcaster)和听众(audience)。
     自由模式用于常见的一对一或者群聊,频道中的任何用户都可以自由说话,这是默认模式。主播 和听众模式需要配合使用,主要用于主播模式。
     主播模式的用户可以发送和接收音视频;听众模 式只能接收音视频,不能发送。
     * @param profile
     * 指定频道通话模式(Profile),目前支持三种:
    ·CHANNEL_PROFILE_FREE(默认)
    ·CHANNEL_PROFILE_BROADCASTER
    ·CHANNEL_PROFILE_AUDIENCE
     * @return
     */
    public int setChannelProfile (int profile) {
        return rtcEngine.setChannelProfile(profile);
    }


}
