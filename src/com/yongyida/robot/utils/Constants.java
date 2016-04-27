package com.yongyida.robot.utils;

import com.yongyida.robot.bean.Alarm;
import com.yongyida.robot.bean.Task;

public class Constants {

	// 请求地址
	public static String address = "http://server.yydrobot.com";
	public static String address_hk = "http://hk.server.yydrobot.com:180";

	// socket ip地址
	public static String ip = "server.yydrobot.com";
	public static String ip_hk = "hk.server.yydrobot.com";

	// socket 端口号
	public static String port = "8002";
	public static String port_hk = "18002";

	// 下载地址
	public static String download_address = "http://resource.yydrobot.com/app/app.version";
	public static String download_address_hk = "http://hk.resource.yydrobot.com/app/app.version";

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

	// 移动action
	public static String Move_aciton = "move";

	// 任务结果
	public static String Result = "result";

	// 超时时间
	public static int timeout = 30000;

	// 照片查询
	public static String Photo_Query = "photo_query";

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

	public static final String CMD_MEDIA_LOGOUT = "/media/room/login/response";

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

	public static final String MEDIA_REPLY = "media_reply";

	public static final String LOGIN_VIDEO_ROOM = "login_video_room";

	public static final String LOGIN_VIDEO_ROOM_RESPONSE = "login_video_room_response";

	public static final String INVITE_ID = "invite_id";

	public static final String BATTERY = "battery";

	public static final int SMS_LOGIN = 1;

	public static final int ACCOUNT_LOGIN = 2;

	public static final String LOGIN_METHOD = "method";

	public static final String UPLOAD = "/upload/applog";

	public interface Role{
		String Robot = "Robot";
		String User = "User";
		String Phone = "Phone";
	}
}
