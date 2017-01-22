package com.yongyida.robot.utils;

import com.yongyida.robot.bean.Alarm;
import com.yongyida.robot.bean.Task;

public class Constants {


	// 请求地址
	public static String address_cn = "http://server.yydrobot.com";
	public static String address_test = "http://120.24.242.163:81";
	public static String address_test1 = "http://120.24.213.239:81";
	public static String address_hk = "http://hk.server.yydrobot.com:180";
	public static String address_tw = "http://hk.server.yydrobot.com:180";
	public static String address_us = "http://us.server.yydrobot.com:81";

	// socket ip地址
	public static String ip_cn = "server.yydrobot.com";
	public static String ip_test = "120.24.242.163";
	public static String ip_test1 = "120.24.213.239";
	public static String ip_hk = "hk.server.yydrobot.com";
	public static String ip_tw = "hk.server.yydrobot.com";
	public static String ip_us = "us.server.yydrobot.com";

	// socket 端口号
	public static String port_cn = "8002";
	public static String port_test = "8002";
	public static String port_test1 = "8002";
	public static String port_hk = "18002";
	public static String port_tw = "18002";
	public static String port_us = "8002";

	// 下载地址
//	public static String download_address_cn = "http://resource.yydrobot.com/app/yyd/cn/app.version";
//    public static String download_address_test = "http://120.24.242.163/app/yyd/cn/app.version";
//    public static String download_address_test1 = "http://120.24.213.239/app/yyd/cn/app.version";

//	public static String download_address_cn = "http://resource.yydrobot.com/app/youjiaoxiaotong/cn/app.youjiaoxiaotong.version";
// 	public static String download_address_test = "http://120.24.242.163/app/youjiaoxiaotong/cn/app.youjiaoxiaotong.version";
// 	public static String download_address_test1 = "http://120.24.213.239/app/youjiaoxiaotong/cn/app.youjiaoxiaotong.version";

//	public static String download_address_cn= "http://resource.yydrobot.com/app/gaojunkeji/cn/app.gaojunkeji.version";
// 	public static String download_address_test = "http://120.24.242.163/app/gaojunkeji/cn/app.gaojunkeji.version";
// 	public static String download_address_test1 = "http://120.24.213.239/app/gaojunkeji/cn/app.gaojunkeji.version";

//	public static String download_address_cn= "http://resource.yydrobot.com/app/liuchaokeji/cn/app.liuchaokeji.version";
//	public static String download_address_test = "http://120.24.242.163/app/liuchaokeji/cn/app.liuchaokeji.version";
//	public static String download_address_test1 = "http://120.24.213.239/app/liuchaokeji/cn/app.liuchaokeji.version";

	public static String download_address_cn= "http://resource.yydrobot.com/app/cmcc/cn/app.cmcc.version";
	public static String download_address_test = "http://120.24.242.163/app/cmcc/cn/app.cmcc.version";
	public static String download_address_test1 = "http://120.24.213.239/app/cmcc/cn/app.cmcc.version";

//	public static String download_address_cn= "http://resource.yydrobot.com/app/caihongwoniu/cn/app.caihongwoniu.version";
//	public static String download_address_test = "http://120.24.242.163/app/caihongwoniu/cn/app.caihongwoniu.version";
//	public static String download_address_test1 = "http://120.24.213.239/app/caihongwoniu/cn/app.caihongwoniu.version";

//	public static String download_address_cn= "http://resource.yydrobot.com/app/xiaoyujiqiren/cn/app.xiaoyujiqiren.version";
//	public static String download_address_test = "http://120.24.242.163/app/xiaoyujiqiren/cn/app.xiaoyujiqiren.version";
//	public static String download_address_test1 = "http://120.24.213.239/app/xiaoyujiqiren/cn/app.xiaoyujiqiren.version";


	public static String download_address_hk = "http://resource.yydrobot.com/app/hk/cn/app.hk.version";
	public static String download_address_tw = "http://resource.yydrobot.com/app/taiwan/cn/app.taiwan.version";
	public static String download_address_us = "http://resource.yydrobot.com/app/yyd/cn/app.version";

	//fota下载地址
	public static String download_fota_address_cn = "http://resource.yydrobot.com/robot/robot.version";
	public static String download_fota_address_test = "http://resource.yydrobot.com/robot/robot.version";
	public static String download_fota_address_test1 = "http://resource.yydrobot.com/robot/robot.version";
	public static String download_fota_address_hk = "http://resource.yydrobot.com/robot/robot.version";
	public static String download_fota_address_tw = "http://resource.yydrobot.com/robot/robot.version";
	public static String download_fota_address_us = "http://resource.yydrobot.com/robot/robot.version";

	public static String address;
	public static String ip;
	public static String port;
	public static String download_address;
	public static String download_fota_address;


	// 任务添加操作请求码
	public static int add_RequestCode = 888;

	// 修改操作请求码
	public static int update_RequestCode = 999;

	// 绑定机器人请求码
	public static int bindrobot_RequestCode = 555;

	// 请求成功
	public static int IS_OK = 200;

	// 请求失败
	public static int Error = 500;

	// 修改任务标识
	public static String Update = "update_task";

	// 添加任务标识
	public static String Add = "add_task";

	// 控制命令机器人id
	public static String rid = "";

	// 行走方向
	public static String execode;

	//行走速度
	public static float speed = 0.25f;  // 0.10 - 0.40

	// 命令内容
	public static String text;

	// 公用的task类
	public static Task task;
	
	public static Alarm alarm;

	// 任务的四种action
	public static String Task_Add = "task_add";

	public static String Task_Remove = "task_remove";

	public static String Task_Query = "task_query";

	public static String Task_Updata = "task_updata";

	// 语音action
	public static String Speech_action = "speak";

	// 语音复读
	public static String Talk = "talk";

	// 移动action
	public static String Move_aciton = "move";

	// 任务结果
	public static String Result = "result";

	// 超时时间
	public static int timeout = 30000;

	// 照片查询
	public static String Photo_Query = "photo_query";

	//障碍提示
	public static String BARRIER_NOTIFY = "barrier_notify";

	public static String BARRIER_NOTIFY_RESULT = "barrier_notify_result";

	public static String Photo_Query_Name = "photo_query_name";
	// 照片删除
	public static String Photo_Delete = "photo_delete";

	// 照片接收
	public static String Photo_Reply = "photo_reply";

	// 照片名字接收
	public static String Photo_Reply_Names = "photo_reply_names";

	// 照片下载
	public static String Photo_Download = "photo_download";

	// 停止服务
	public static String Stop = "stop";

	// 断网情况socket处理
	public static String Socket_Error = "socket_error";

	// 机器人信息修改
	public static String Robot_Info_Update = "robot_info_update";

	// 连接机器人
	public static String Robot_Connection = "Robot_Connection";

	//socket退出登录
	public static String Socket_Logout = "Socket_Logout";

	// 视频模式
	public static String Video_Mode = "yongyida.robot.video.videomode";

	public static boolean flag = false;

	public static boolean isUserClose = false;  //是不是用户自己要断开socket的 如果是 就断开  不是就需要重连
	
	public static String connect_robot="connect_robot";

	public final static String TYPE = "type";

	public final static String Y50 = "Y50";

	public final static String Y20 = "Y20";

	public final static String HK_CODE = "+852";

	public final static String CN_CODE = "+86";

	public final static String ADD_ROBOT_FRIEND = "/friends/user/addRobot";

	public final static String DELETE_ROBOT_FRIEND = "/friends/user/delRobot";

	public final static String FIND_ROBOT_FRIEND = "/friends/user/findRobot";

	public static final String RESPONSE_VIDEO_REQUEST = "response_video_request";

	public static final String CMD_MEDIA_INVITE = "/media/invite/response";

	public static final String CMD_MEDIA_CANCEL = "/media/cancel/response";

	public static final String CMD_MEDIA_REPLY = "/media/reply/response";

	public static final String CMD_MEDIA_REPLY_NEW = "/media/reply";

	public static final String CMD_MEDIA_LOGIN = "/media/room/login/response";

	public static final String CMD_MEDIA_JOIN_ROOM = "/media/room/join";

	public static final String CMD_MEDIA_LOGOUT = "/media/room/logout/response";

	public static final String CMD_MEDIA_JOIN = "/media/room/join";

	public static final String CMD_MEDIA_CALLBACK = "/media/callback";

	public static final String CMD_MEDIA_IVT = "/media/invite";

	public static final String LOGIN = "login";

	public static final String VIDEO_REQUEST = "video_request";

	public static final String CONNECTION_REQUEST = "connection_request";

	public static final String RET = "ret";

	public static final String RoomID = "roomId";

	public static final String ID = "id";

	public static final String REPLY = "reply";

	public static final String CONNECTION_RESPONSE = "connection_response";

	public static final String Replay_Response = "replay_response";

	public static final String VIDEO_REQUEST_FROM_OTHERS = "video_request_from_others";

	public static final String BR_REPLY = "br_reply";

	public static final String BR_CANCEL_DIAL = "br_cancel_dial";

	public static final String MEDIA_REPLY = "media_reply";

	public static final String MEDIA_INVITE_CANCEL = "media_invite_cancel";

	public static final String LOGIN_VIDEO_ROOM = "login_video_room";

	public static final String LOGOUT_VIDEO_ROOM = "logout_video_room";

	public static final String LOGIN_VIDEO_ROOM_RESPONSE = "login_video_room_response";

	public static final String LOGIN_VIDEO_ROOM_LOGOUT_RESPONSE = "login_video_room_logout_response";

	public static final String QUESTIONNAIRE = "questionnaire";

	public static final String EMERGENCY = "emergency";

	public static final String AGORA_VIDEO_MEETING_INVITE = "agora_video_meeting_invite";

	public static final String AGORA_VIDEO_MEETING_INVITE_CANCEL = "agora_video_meeting_invite_cancel";

	public static final String AGORA_VIDEO_MEETING_INVITE_REPLY = "agora_video_meeting_invite_reply";

	public static final String AGORA_VIDEO_MEETING_TIME = "agora_video_meeting_time";

	public static final String SEND_VOICE = "send_voice";

	public static final String MEDIA_JOIN_ROOM = "join_room";

	public static final String INVITE_ID = "invite_id";

	public static final String BATTERY = "battery";

	public static final String NAVIGATION_NOTIFY = "navigation_notify";

	public static final String NAVIGATION_NOTIFY_RESULT = "navigation_notify_result";

	public static final int SMS_LOGIN = 1;

	public static final int ACCOUNT_LOGIN = 2;

	public static final String LOGIN_METHOD = "method";

	public static final String UPLOAD = "/upload/applog";

	public static final String FOTA_UPDATE = "fota_update";

	public static final String SESSION_ERROR = "session_error";

	public static final String MediaTcpIp = "media_tcp_ip";

	public static final String MediaTcpPort = "media_tcp_port";

	public static final String TypeRole = "role";

	public static final String LOG_ERROR = "yyderror";

	public static final String HOST = "host";

	public static final String CONFERENCE_ID = "conference_id";

	public static final String NET_SUCCESS = "net_success";

	public static final String INVITE_REJECT = "reject";

	public static final String INVITE_ACCEPT = "accept";

	public static final String MEETING_INVITE_REPLY = "meetingInviteReply";

	public static final String CALL_ID = "call_id";

	public static final String CALL_NAME = "call_name";

	public static final String UNRELEASE_MEETING_NO = "unrelease_meeting_no";

	public static final String TO_MEETING_NO = "to_meeting_no";

	public static final String INVITE_MESSAGE = "meetingInvite";

	public static final String CALL_NO = "call_no";

	public static final String MESSAGE_PREFIX = "msgType://";

	public static final String MEETING_CANCEL_INVITE = "meetingCancelInvite";

	public static final String CHOOSED_WEEK = "choosed_week";

	public static final String CHOOSED_WEEK_RESULT = "choosed_week_result";

	public static final String IS_VIDEO = "is_video";

	public static final String RLY_KICK_OFF = "rly_kick_off";

	public static boolean NetBrInterrupt = true;

    public static final String NET_OFF = "net_off";

    public static final String CONNECT_SUCCESS = "connect_success";

	public static final String AGORA_INVITE_CANCEL = "agora_invite_cancel";

	public static final String AGORA_INVITE_REPONSE = "agora_invite_reponse";

	public static final String AGORA_INVITE_REPLY = "agora_invite_reply";

	public static final String AGORA_MEETING_TIME_REPONSE = "agora_meeting_time_reponse";

	public static final String LOCAL_ADDRESS = "/sdcard";
	public static final String RECORD_PCM = "/record.pcm";
	public static final String RECORD_WAV = "/record.wav";
	public static final String CHANGED_WAV = "/changed.wav";

	public static final String AGORA_ID = "id";
	public static final String AGORA_NUMBER = "number";
	public static final String AGORA_ROLE = "role";
	public static final String AGORA_NICKNAME = "nickname";
	public static final String AGORA_CHANNEL_ID = "channel_id";
	public static final String AGORA_TYPE = "type";
	public static final String AGORA_MODE = "mode";
	public static final String AGORA_INVITE_TYPE = "invite_type";
	public static final String AGORA_INVITE_ID = "invite_id";
	public static final String AGORA_REPLY = "reply";
	public static final String AGORA_BEGINTIME = "begintime";
	public static final String AGORA_ENDTIME = "endtime";
	public static final String AGORA_TOTALTIME = "totaltime";
	public static final String AGORA_TOTALSIZE = "totalsize";
	public static final String AGORA_RET = "agora_ret";
	public static final String MEETING_ID = "meeting_id";


	public static final String MAPPING_ACTION = "mapping_action";
	public static final String MAPPING_CMD = "mapping_cmd";
	public static final String MAPPING_TAKE_PICTURES = "take_pictures";
	public static final String MAPPING_OPEN_CAMERA = "camera_open";
	public static final String MAPPING_CLOSE_CAMERA = "camera_close";
	public static final String MAPPING_STOP = "move_stop";
	public static final String MAPPING_STEP_FORWARD = "going";
	public static final String MAPPING_ROTATE_90 = "mode_1";
	public static final String MAPPING_ROTATE_360 = "mode_2";


	public static final String AGORA_CMD_INVITE_REPONSE = "/avm/invite/response";

	public static final String AGORA_CMD_INVITE = "/avm/invite";

	public static final String AGORA_CMD_INVITE_CANCEL = "/avm/invite/cancel";

	public static final String AGORA_CMD_INVITE_REPLY = "/avm/invite/reply";

	public static final String AGORA_CMD_MEETING_TIME_REPONSE = "/media/meeting/report/response";

	public static final String PHOTO_PATH = "photo_path";

	public static final String FROM_PUSH = "from_push";

	public static final String DOWNLOAD_ADDRESS = "download_address";

	public static final String BARRIER = "barrier";

	public static final String BARRIER_SWITCH = "barrier_switch";

	public static final String BARRIER_FLAG = "barrier_flag";

	public static final String WHETHER_NAVIGATION = "whether_navigation";

	public static boolean USER_BACE = false;

	public final static int MSG_SPEECH_RESTART=2;
	public final static int MSG_SEND_A_REQUEST=3;
	public final static int TOTALBYTE = 6*1024;
	public interface Role{
		String Robot = "Robot";
		String User = "User";
		String Phone = "Phone";
	}

}
