package com.yongyida.robot.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.yongyida.robot.R;
import com.yongyida.robot.activity.AgoraInviteActivity;
import com.yongyida.robot.activity.VideoMeetingActivity;
import com.yongyida.robot.broadcastReceiver.NetStateBroadcastReceiver;
import com.yongyida.robot.broadcastReceiver.SocketErrorReceiver;
import com.yongyida.robot.net.helper.Decoder;
import com.yongyida.robot.net.helper.MeetingVideoDecoder;
import com.yongyida.robot.net.helper.SocketConnect;
import com.yongyida.robot.net.helper.SocketConnect.SocketListener;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Config;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.FileUtil;
import com.yongyida.robot.utils.NetUtil;
import com.yongyida.robot.utils.ThreadPool;
import com.yongyida.robot.utils.Utils;
import com.yongyida.robot.video.av.BitrateType;
import com.yongyida.robot.video.av.EncoderType;
import com.yongyida.robot.video.av.FrameRateType;
import com.yongyida.robot.video.av.TransferDataType;
import com.yongyida.robot.video.av.TransferType;
import com.yongyida.robot.video.av.VideoSizeType;
import com.yongyida.robot.video.command.RoomUser;
import com.yongyida.robot.video.sdk.MeetingInfo;
import com.yongyida.robot.video.sdk.Role;
import com.yongyida.robot.video.sdk.YYDSDKHelper;
import com.yongyida.robot.video.sdk.YYDVideoServer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SocketService extends Service {

    private static final String TAG = "SocketService";
    private Timer time;
    boolean the = true;
    private ChannelHandlerContext ctx;          //用于发送一般的socket请求
    private ChannelHandlerContext mMediaCtx;    //用于发送视频部分 进入房间之后的请求
    private NetStateBroadcastReceiver netstate = new NetStateBroadcastReceiver();
    private final String size = "small";
    private SocketErrorReceiver mSocketErrorReceiver = new SocketErrorReceiver();

    @Override
    public void onCreate() {
    }

    @Override
    public IBinder onBind(Intent arg0) {

        return null;
    }


    /**
     * y20接收到的socket消息
     * @param ctx
     * @param e
     */
    private void socketY20Receive(ChannelHandlerContext ctx, MessageEvent e) {
        Object o = e.getMessage();
        Object obj;
        JSONObject Result;

        //接收到的数据可能是arrayList型的
        if (o instanceof ArrayList<?>) {
            ArrayList arr = (ArrayList) o;
            for (int n = 0; n < arr.size(); n++) {
                obj = arr.get(n);
                if (obj instanceof JSONObject) {
                    o = obj;
                } else if (obj instanceof Decoder.Result2) {
                    o = obj;
                }
            }
        }

        try {
            if (o instanceof JSONObject) {
                Result = (JSONObject) o;
                String cmd = Result.getString("cmd");
                if (Constants.CMD_MEDIA_INVITE.equals(cmd)) {      //视频请求响应
                    int ret = Result.getInt(Constants.RET);
                    Intent intent = new Intent(Constants.CONNECTION_REQUEST);
                    intent.putExtra(Constants.RET, ret);
                    if (ret == 0) {
                        String mediaTcpIp = Result.getString("media_tcp_ip");
                        int mediaTcpPort = Result.getInt("media_tcp_port");
                        int roomId = Result.getInt("room_id");
                        intent.putExtra(Constants.MediaTcpIp, mediaTcpIp);
                        intent.putExtra(Constants.MediaTcpPort, mediaTcpPort);
                        intent.putExtra(Constants.RoomID, roomId);
                    }
                    sendBroadcast(intent);
                } else if (Constants.CMD_MEDIA_CANCEL.equals(cmd)) {
                    int ret = Result.getInt("ret");
                    Intent intent = new Intent(Constants.MEDIA_INVITE_CANCEL);
                    intent.putExtra(Constants.RET, ret);
                    sendBroadcast(intent);
                } else if (Constants.CMD_MEDIA_REPLY.equals(cmd)) {
                    int ret = Result.getInt("ret");
                    String mediaTcpIp = Result.getString("media_tcp_ip");
                    int mediaTcpPort = Result.getInt("media_tcp_port");
                    int roomId = Result.getInt("room_id");
                    Intent intent = new Intent(Constants.Replay_Response);
                    intent.putExtra(Constants.MediaTcpIp, mediaTcpIp);
                    intent.putExtra(Constants.MediaTcpPort, mediaTcpPort);
                    intent.putExtra(Constants.RoomID, roomId);
                    sendBroadcast(intent);
                }  else if (Constants.CMD_MEDIA_CALLBACK.equals(cmd)) {
                    String command = Result.getString("command");

                } else if (Constants.CMD_MEDIA_IVT.equals(cmd)) {
                    Long id = Result.getLong("id");
                    Intent intent = new Intent(Constants.VIDEO_REQUEST_FROM_OTHERS);
                    sendBroadcast(intent);
                } else if (Constants.CMD_MEDIA_REPLY_NEW.equals(cmd)) {
                    int reply = Result.getInt("reply");
                    Intent intent = new Intent(Constants.MEDIA_REPLY);
                    if (reply != 0) {
                        String invite_type = Result.getString("invite_type");
                        int invite_id = Result.getInt("invite_id");
                        String role = Result.getString("role");
                        int id = Result.getInt("id");
                        intent.putExtra("invite_type", invite_type);
                        intent.putExtra("role", role);
                        intent.putExtra("invite_id", invite_id);
                        intent.putExtra("id", id);
                    }
                    intent.putExtra("reply", reply);
                    sendBroadcast(intent);
                } else if (Constants.AGORA_CMD_INVITE_REPONSE.equals(cmd)) {  //AGORA invite
                    int ret = Result.getInt("ret");
                    Intent intent = new Intent(Constants.AGORA_INVITE_REPONSE);
                    intent.putExtra(Constants.AGORA_RET, ret);
                    sendBroadcast(intent);
                } else if (Constants.AGORA_CMD_INVITE.equals(cmd)) { //AGORA 被邀请
                    Log.d(TAG, "meeting");
                    long id = Result.getLong(Constants.AGORA_ID);
                    String channel_id = Result.getString(Constants.AGORA_CHANNEL_ID);
                    String mode = Result.getString(Constants.AGORA_MODE);
                    if (Utils.isForeground(this, VideoMeetingActivity.class.getCanonicalName())) {
                        NetUtil.agoraVideoMeetingScoketInviteReply(Long.parseLong(Utils.getUserID(this)),
                                "User",
                                "Robot",
                                id + "",
                                0,
                                getString(R.string.meeting),
                                ctx);
                        return;
                    }
                    if (mode.equals("meeting")) {
                        Intent intent = new Intent(this, AgoraInviteActivity.class);
                        intent.putExtra(Constants.AGORA_ID, id);
                        intent.putExtra(Constants.AGORA_CHANNEL_ID, channel_id);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                } else if (Constants.AGORA_CMD_INVITE_CANCEL.equals(cmd)) {  //对方取消邀请
                    sendBroadcast(new Intent(Constants.AGORA_INVITE_CANCEL));
                } else if (Constants.AGORA_CMD_INVITE_REPLY.equals(cmd)) {
                    long id = Result.getLong(Constants.AGORA_ID);
                    int reply = Result.getInt(Constants.AGORA_REPLY);
                    Intent intent = new Intent(Constants.AGORA_INVITE_REPLY);
                    intent.putExtra(Constants.AGORA_REPLY, reply);
                    intent.putExtra(Constants.AGORA_ID, id);
                    sendBroadcast(intent);
                } else if (Constants.AGORA_CMD_MEETING_TIME_REPONSE.equals(cmd)) {  //计时response
                    int ret = Result.getInt(Constants.AGORA_RET);
                    Intent intent = new Intent(Constants.AGORA_MEETING_TIME_REPONSE);
                    intent.putExtra(Constants.AGORA_RET, ret);
                    sendBroadcast(intent);
                }
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }


    /**
     * y50b socket消息接受处理
     * @param ctx
     * @param e
     */
    public void socketY50Receive(final ChannelHandlerContext ctx,
                                 final MessageEvent e){

        int ret = 0;
        Object o = e.getMessage();
        String callback = "";
        JSONObject Result = null;
        Object obj = null;

        //接收到的数据可能是arrayList型的
        if (o instanceof ArrayList<?>) {
            ArrayList arr = (ArrayList) o;
            for (int n = 0; n < arr.size(); n++) {
                obj = arr.get(n);
                if (obj instanceof  JSONObject) {
                    o = obj;
                }
                if (obj instanceof Decoder.Result2) {
                    o = obj;
                }
            }
        }

        if (o instanceof JSONObject) {
            try {
                Result = (JSONObject) o;
                callback = Result.getString("cmd");
                if (callback.equals("/robot/callback")) {
                    JSONObject queryresult = new JSONObject(
                            Result.getString("command"));
                    Log.e(TAG, "result:" + queryresult.toString());
                    if (queryresult.getString("cmd").equals(
                            "photo_names")) {
                        ArrayList<String> names = new ArrayList<String>();

                        String querynames = queryresult
                                .getString("names");
                        JSONArray array = null;
                        if (querynames.equals("null")) {
                            return;
                        } else {
                            array = new JSONArray(querynames);
                        }
                        for (int i = 0; i < array.length(); i++) {
                            names.add(array.getJSONObject(i)
                                    .getString("name"));
                        }
                        sendBroadcast(new Intent(
                                Constants.Photo_Reply_Names)
                                .putExtra("result", names));
                    } else if (queryresult.getString("cmd").equals(
                            "")) {
                        sendBroadcast(new Intent(
                                Constants.Photo_Query).putExtra(
                                "result", ""));
                    } else if (queryresult.getString("cmd").equals(
                            "barrier_location")) {
                        Intent intent = new Intent(Constants.BARRIER_NOTIFY);
                        intent.putExtra(Constants.BARRIER_NOTIFY_RESULT, queryresult.toString());
                        sendBroadcast(intent);
                    } else if (queryresult.getString("cmd").equals("notify")) {
                        String value = queryresult.getString("value");
                        Intent intent = new Intent(Constants.NAVIGATION_NOTIFY);
                        intent.putExtra(Constants.NAVIGATION_NOTIFY_RESULT, value);
                        sendBroadcast(intent);
                    } else {
                        sendBroadcast(new Intent("result")
                                .putExtra("result", queryresult
                                        .getString("data")));
                    }
                    return;
                } else if (callback.equals("/robot/uncontroll")) {
                    if (!Constants.USER_BACE) {
                        Intent socketErrorIntent = new Intent(Constants.Socket_Error);
                        socketErrorIntent.putExtra("content", getString(R.string.robot_offline));
                        sendBroadcast(socketErrorIntent);
                    }
                    return;
                } else if (callback.equals("/robot/flush")) {
                    String from = Result.getString("from");
                    if (from.equals("Robot")) {
                        JSONObject jsonObject = new JSONObject(
                                Result.getString("Robot"));
                        int battery = jsonObject.getInt("battery");
                        String rname = jsonObject.getString("rname");
                        Intent intent = new Intent(Constants.BATTERY);
                        intent.putExtra("ret", ret);
                        intent.putExtra("battery", battery);
                        intent.putExtra("rname", rname);
                        sendBroadcast(intent);
                    } else {
                        sendBroadcast(new Intent("flush").putExtra("ret", ret));
                    }
                    return;
                } else if (callback.equals("/user/login")) {
                    sendBroadcast(new Intent(Constants.LOGIN).putExtra(
                            "ret", Result.getInt("ret")));
                    return;
                } else if (callback.equals("/robot/controll")) {
                    ret = Result.getInt("ret");
                    Intent in = new Intent("online");
                    switch (ret) {
                        case 0:
                            flag = 1;
                            String robot = Result.getString("Robot");
                            JSONObject jsonObject = new JSONObject(robot);
                            String version = jsonObject.getString("version");
                            String id = jsonObject.getString("id");
                            in.putExtra("id", id);
                            in.putExtra("version", version);
                            in.putExtra("ret", 0);
                            Constants.flag = true;
                            break;
                        case -1:
                            in.putExtra("ret", -1);
                            the = true;
                            break;
                        case 1:
                            in.putExtra("ret", 1);
                            the = true;
                            break;
                        case 2:
                            in.putExtra("ret", 2);
                            the = true;
                            break;
                        case 3:
                            in.putExtra("ret", 3);
                            the = true;
                            break;
                        case 4:
                            in.putExtra("ret", 4);
                            the = true;
                            break;
                        case 5:
                            in.putExtra("ret", 5);
                            the = true;
                            break;
                        case 6:
                            in.putExtra("ret", 6);
                            the = true;
                            break;
                        case 7:
                            in.putExtra("ret", 7);
                            the = true;
                            break;
                        default:
                            the = true;
                            break;

                    }
                    sendBroadcast(in);
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        } else if (o instanceof Decoder.Result2) {
            handlePhoto((Decoder.Result2)o);


        }
    }

    private void handlePhoto(final Decoder.Result2 res) {
        ThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    String fileName = new JSONObject(res.json.getString("command"))
                            .getString("name");
                    String robotName = getSharedPreferences("Receipt", MODE_PRIVATE).getString("username", null);
                    String path = getApplication().getExternalFilesDir(null).getAbsolutePath()
                            + "/"
                            + getSharedPreferences("Receipt", MODE_PRIVATE).getString("username", null)
                            + size;
                    if (!TextUtils.isEmpty(robotName) && !Utils.isSeries(robotName, "20")) {
                        /* 照片先转成bitmap 旋转90度再存起来 */
                        Bitmap b = BitmapFactory.decodeByteArray(res.datas, 0, res.datas.length);
                        Matrix matrix = new Matrix();
                        matrix.setRotate(-90);
                        Bitmap bm = b.createBitmap(b, 0, 0, b.getWidth(),
                                b.getHeight(), matrix, true);
                        b.recycle();
                        File dir = new File(path);
                        if (!dir.exists()) {
                            dir.mkdir();
                        }
                        File picture = new File(path + "/" + fileName);

                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(picture);
                            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        } finally {
                            try {
                                fos.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        Intent reply = new Intent(Constants.Photo_Reply);
                        reply.putExtra(Constants.PHOTO_PATH, path + "/" + fileName);
                        sendBroadcast(reply);
                    } else {
                        File file = new File(path);
                        if (!file.exists()) {
                            file.mkdir();
                        }
                        FileUtil.writefile(
                                getApplication().getExternalFilesDir(
                                        null).getAbsolutePath()
                                        + "/"
                                        + robotName + size,
                                res.datas, fileName);
                        Intent reply = new Intent(Constants.Photo_Reply);
                        reply.putExtra(Constants.PHOTO_PATH, file.getAbsolutePath() + "/" + fileName);
                        sendBroadcast(reply);
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }


            }
        });
    }

    public void connectSocketByLanguage(){
        connectsocket(listener, Constants.ip, Constants.port);
    }

    private BroadcastReceiver speak = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {

            if (intent.getAction().equals(Constants.Speech_action)) {
                if (ctx != null) {
                    JSONObject params = new JSONObject();
                    NetUtil.Scoket(params, 2, ctx);
                } else {
                    handleError();
                }
            }
        }
    };

    private BroadcastReceiver mQuestionnaireBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctx != null) {
                JSONObject params = new JSONObject();
                NetUtil.Scoket(params, 6, ctx);
            } else {
                handleError();
            }
        }
    };

    private BroadcastReceiver mEmergencyBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctx != null) {
                JSONObject params = new JSONObject();
                try {
                    params.put("username",intent.getStringExtra("username"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                NetUtil.Scoket(params, 7, ctx);
            } else {
                handleError();
            }
        }
    };

    private BroadcastReceiver mAgoraVideoMeetingInviteBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctx != null) {
                NetUtil.agoraVideoMeetingScoketInvite(intent.getLongExtra(Constants.AGORA_ID, -1),
                        intent.getLongExtra(Constants.AGORA_NUMBER, -1),
                        intent.getStringExtra(Constants.AGORA_ROLE),
                        intent.getStringExtra(Constants.AGORA_NICKNAME),
                        intent.getStringExtra(Constants.AGORA_CHANNEL_ID),
                        intent.getStringExtra(Constants.AGORA_TYPE),
                        intent.getStringExtra(Constants.AGORA_MODE), ctx);
            } else {
                handleError();
            }
        }
    };

    private BroadcastReceiver mAgoraVideoMeetingInviteCancelBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctx != null) {
                NetUtil.agoraVideoMeetingScoketInviteCancel(intent.getLongExtra(Constants.AGORA_ID, -1),
                        intent.getLongExtra(Constants.AGORA_NUMBER, -1),
                        intent.getStringExtra(Constants.AGORA_ROLE),
                        intent.getStringExtra(Constants.AGORA_TYPE), ctx);
            } else {
                handleError();
            }
        }
    };

    private BroadcastReceiver mAgoraVideoMeetingInviteReplyBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctx != null) {
                NetUtil.agoraVideoMeetingScoketInviteReply(intent.getLongExtra(Constants.AGORA_ID, -1),
                        intent.getStringExtra(Constants.AGORA_ROLE),
                        intent.getStringExtra(Constants.AGORA_INVITE_TYPE),
                        intent.getStringExtra(Constants.AGORA_INVITE_ID),
                        intent.getIntExtra(Constants.AGORA_REPLY, -1),
                        "", ctx);
            } else {
                handleError();
            }
        }
    };

    private BroadcastReceiver mAgoraVideoMeetingTimeBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctx != null) {
                NetUtil.agoraVideoMeetingScoketTime(intent.getLongExtra(Constants.AGORA_ID, -1),
                        intent.getStringExtra(Constants.MEETING_ID),
                        intent.getStringExtra(Constants.AGORA_ROLE),
                        intent.getStringExtra(Constants.AGORA_BEGINTIME),
                        intent.getStringExtra(Constants.AGORA_ENDTIME),
                        intent.getIntExtra(Constants.AGORA_TOTALTIME, 0),
                        intent.getFloatExtra(Constants.AGORA_TOTALSIZE, 0),
                        ctx);
            } else {
                handleError();
            }
        }
    };

    private BroadcastReceiver mMappingBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctx != null) {
                NetUtil.mapping(intent.getStringExtra(Constants.MAPPING_CMD), ctx);
            } else {
                handleError();
            }
        }
    };

    private BroadcastReceiver mWhetherNavigationBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctx != null) {
                NetUtil.whetherNavigation(ctx);
            } else {
                handleError();
            }
        }
    };

    private BroadcastReceiver mBarrierSwitchBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctx != null) {
                boolean barrierFlag = intent.getBooleanExtra(Constants.BARRIER_FLAG, true);
                NetUtil.barrierSwitch(barrierFlag, ctx);
            } else {
                handleError();
            }
        }
    };

    private BroadcastReceiver mVoiceSendBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctx != null) {
                try {
                    byte[] b = FileUtil.getContent(Constants.LOCAL_ADDRESS + Constants.CHANGED_WAV);
                    NetUtil.sendVoice(b, ctx);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                handleError();
            }
        }
    };


    private BroadcastReceiver talk = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {

            if (intent.getAction().equals(Constants.Talk)) {
                if (ctx != null) {
                    JSONObject params = new JSONObject();
                    NetUtil.Scoket(params, 5, ctx);
                } else {
                    handleError();
                }
            }
        }
    };

    private void handleError() {
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

        connectSocketByLanguage();
    }

    private BroadcastReceiver move = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Constants.Move_aciton)) {
                if (ctx != null) {
                    JSONObject params = new JSONObject();
                    NetUtil.Scoket(params, 1, ctx);
                } else {
                    handleError();
                }

            }
        }

    };
    private BroadcastReceiver task = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (ctx != null) {
                NetUtil.socket(ctx, intent);
            } else {
                handleError();
            }
        }
    };
    private BroadcastReceiver stop = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals("stop")) {
                if (ctx != null) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("cmd", "/robot/uncontroll");
                        jsonObject.put("id",
                                getSharedPreferences("userinfo", MODE_PRIVATE)
                                        .getInt("id", 0));
                        jsonObject.put("session",
                                getSharedPreferences("userinfo", MODE_PRIVATE)
                                        .getString("session", null));
                        String rid = getSharedPreferences("Receipt", MODE_PRIVATE)
                                .getString("robotid", null);
                        if (rid != null) {
                            jsonObject.put("rid", rid);
                        } else {
                            return;
                        }
                    } catch (JSONException ee) {
                        ee.printStackTrace();
                    }
                    NetUtil.Scoket(jsonObject, 4, ctx);
                    flag = 0;
                    Constants.flag = false;
                } else {
                    handleError();
                }
            }
        }
    };
    private BroadcastReceiver photo = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (ctx != null) {
                NetUtil.photo_socket(ctx, intent);
            } else {
                handleError();
            }
        }
    };
    private BroadcastReceiver flush = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (ctx != null) {
                NetUtil.robotinfoupdate(ctx, intent);
            } else {
                handleError();
            }
        }
    };

    private BroadcastReceiver connectRobot = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (ctx != null) {
                control(ctx);
            } else {
                handleError();
            }
        }
    };

    private BroadcastReceiver socketLogout = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (ctx != null) {
                NetUtil.logoutSocket(getSharedPreferences("userinfo", MODE_PRIVATE).getInt("id", 0),
                        getSharedPreferences("userinfo", MODE_PRIVATE).getString("session", ""),
                        ctx);
            } else {
                handleError();
            }
        }
    };

    private BroadcastReceiver mFotaUpdateBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctx != null) {
                String robotVersion = intent.getStringExtra("robotVersion");
                String newVersion = intent.getStringExtra("newVersion");
                NetUtil.fotaUpdate(robotVersion, newVersion, ctx);
            } else {
                handleError();
            }
        }
    };

    private BroadcastReceiver mCancelDialBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctx != null) {
                long id = intent.getIntExtra("id", -1);
                String role = intent.getStringExtra("role");
                NetUtil.socketY20MediaCancel(id, role, ctx);
            } else {
                handleError();
            }
        }
    };

    private BroadcastReceiver mVideoReplyBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctx != null) {
                Long invite_id = intent.getLongExtra(Constants.INVITE_ID, -1);
                int id = getSharedPreferences("userinfo", MODE_PRIVATE).getInt("id", 0);
                NetUtil.socketY20MediaReply(id, NetUtil.NumberType.Phone, Role.User, 100069 + "", 1, ctx);
            } else {
                handleError();
            }
        }
    };


    private BroadcastReceiver mLogoutVideoRoomBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctx != null) {
                String id = intent.getStringExtra("id");
                String role = intent.getStringExtra("role");
                int room_id = intent.getIntExtra("room_id", -1);
                NetUtil.socketY20MediaLogout(id, role, room_id, mMediaCtx);
            } else {
                handleError();
            }
        }
    };

    private BroadcastReceiver mLoginVideoRoomBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int id = intent.getIntExtra(Constants.ID, -1);
            final int roomId = intent.getIntExtra(Constants.RoomID, -1);
            final String ip = intent.getStringExtra(Constants.MediaTcpIp);
            final int port = intent.getIntExtra(Constants.MediaTcpPort, -1);
            final String role = intent.getStringExtra(Constants.TypeRole);
            connectVideoSocket(new SocketListener() {
                @Override
                public void connectFail() {

                }

                @Override
                public void connectSuccess(ChannelHandlerContext ctx, ChannelStateEvent e) {
                    MeetingInfo info = YYDVideoServer.getInstance().getMeetingInfo();
                    int videowidth = info.VideoWidth;
                    int videoheight = info.VideoHeight;
                    int framerate = info.FrameRate;
                    int bitrate = info.BitRate;
                    int samplerate = info.SampleRate;
                    int channel = info.Channel;
                    int audioformat = info.AudioFormat;
                    NetUtil.socketY20MediaLogin(id + "", role, roomId, videowidth, videoheight,
                            framerate, bitrate, samplerate, channel, audioformat,
                            ctx);
                    mMediaCtx = ctx;
                }

                @Override
                public void writeData(ChannelHandlerContext ctx, MessageEvent e) {
                    Object message = e.getMessage();
                    ChannelBuffer buffer;
                    if (message instanceof ChannelBuffer) {
                        buffer = ((ChannelBuffer) message);
                        Channels.write(ctx, e.getFuture(), buffer);
                    }
                }

                @Override
                public void receiveSuccess(ChannelHandlerContext ctx, MessageEvent e) {
                    Object o = e.getMessage();
                    Object obj;
                    JSONObject Result;

                    //接收到的数据可能是arrayList型的
                    if (o instanceof ArrayList<?>) {
                        ArrayList arr = (ArrayList) o;
                        for (int n = 0; n < arr.size(); n++) {
                            obj = arr.get(n);
                            if (obj instanceof JSONObject) {
                                o = obj;
                            }
                            if (obj instanceof MeetingVideoDecoder.Result2) {
                                o = obj;
                            }
                        }
                    }

                    try {
                        if (o instanceof JSONObject) {
                            Result = (JSONObject) o;

                        } else if (o instanceof MeetingVideoDecoder.Result2) {
                            MeetingVideoDecoder.Result2 result = ((MeetingVideoDecoder.Result2) o);
                            String cmd = result.json.getString("cmd");
                            if (Constants.CMD_MEDIA_LOGIN.equals(cmd)) {
                                int ret = result.json.getInt("ret");
                                if (ret == 0) {
                                    String userMedia = result.json.getString("UserMedia");
                                    JSONObject jsonObject = new JSONObject(userMedia);
                                    String send_host = jsonObject.getString("send_host");
                                    int send_port = jsonObject.getInt("send_port");
                                    String userMedias = result.json.getString("UserMedias");
                                    JSONArray jsonArray = new JSONArray(userMedias);
                                    YYDVideoServer.getInstance().getMeetingInfo().setVideoServer_Udp(send_host, send_port);
                                    YYDVideoServer.getInstance().getMeetingInfo().setAtRooming(true);
                                    if (jsonArray.length() > 0) {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            long id1 = jsonArray.getJSONObject(i).getLong("id");
                                            String role1 = jsonArray.getJSONObject(i).getString("role");
                                            String nickName1 = jsonArray.getJSONObject(i).getString("nikename");
                                            if (id1 != YYDSDKHelper.getInstance().getUser().getId()
                                                    || !role1.equalsIgnoreCase(YYDSDKHelper.getInstance().getUser().getRole())) {
                                                YYDVideoServer.getInstance().getMeetingInfo().addRoomUser(new RoomUser(role1, id1, nickName1));
                                            }
                                        }
                                    }
                                }
                                Intent intent = new Intent(Constants.LOGIN_VIDEO_ROOM_RESPONSE);
                                intent.putExtra(Constants.RET, ret);
                                sendBroadcast(intent);
                            } else if (Constants.CMD_MEDIA_JOIN_ROOM.equals(cmd)) {
                                int ret = result.json.getInt("ret");
                                if (ret == 0) {
                                    String userMedias = result.json.getString("UserMedias");
                                    JSONArray jsonArray = new JSONArray(userMedias);
                                    if (jsonArray.length() > 0) {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            long id1 = jsonArray.getJSONObject(i).getLong("id");
                                            String role1 = jsonArray.getJSONObject(i).getString("role");
                                            String nickName1 = jsonArray.getJSONObject(i).getString("nikename");
                                            if (id1 != YYDSDKHelper.getInstance().getUser().getId()
                                                    || !role1.equalsIgnoreCase(YYDSDKHelper.getInstance().getUser().getRole())) {
                                                YYDVideoServer.getInstance().getMeetingInfo().addRoomUser(new RoomUser(role1, id1, nickName1));
                                            }
                                        }
                                    }
                                }
                                Intent intent2 = new Intent(Constants.MEDIA_JOIN_ROOM);
                                sendBroadcast(intent2);
                            }else if (Constants.CMD_MEDIA_LOGOUT.equals(cmd)) {
                                sendBroadcast(new Intent(Constants.LOGIN_VIDEO_ROOM_LOGOUT_RESPONSE));
                            }
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                }

                @Override
                public void connectClose(ChannelHandlerContext ctx, ChannelStateEvent e) {

                }
            }, ip, port + "");

        }
    };

    private BroadcastReceiver mVideoRequestBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ctx != null) {
                int id = intent.getIntExtra("id", 0);
                String role = intent.getStringExtra("role");
                String picture = intent.getStringExtra("picture");
                if (picture == null) {
                    picture = "";
                }
                String numberType = intent.getStringExtra("numberType");
                long number = intent.getLongExtra("number", 0);
                String nickname = intent.getStringExtra("nickname");
                MeetingInfo info = YYDVideoServer.getInstance().getMeetingInfo();
                int videowidth = info.VideoWidth;
                int videoheight = info.VideoHeight;
                int framerate = info.FrameRate;
                int bitrate = info.BitRate;
                int samplerate = info.SampleRate;
                int channel = info.Channel;
                int audioformat = info.AudioFormat;
                NetUtil.socketY20MediaInvite(id, role, nickname, picture, numberType, number,
                        videowidth, videoheight, framerate, bitrate, samplerate, channel, audioformat,
                        ctx);
            } else {
                handleError();
            }
        }
    };
    /**
     * 连接机器人
     * @param ctx
     */
    private void control(final ChannelHandlerContext ctx) {

        if (flag == 0) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("cmd", "/robot/controll");
                jsonObject.put(
                        "id",
                        getSharedPreferences("userinfo", MODE_PRIVATE).getInt(
                                "id", 0));
                jsonObject.put("session",
                        getSharedPreferences("userinfo", MODE_PRIVATE)
                                .getString("session", null));
                jsonObject.put("rid",
                        getSharedPreferences("Receipt", MODE_PRIVATE)
                                .getString("robotid", null));
            } catch (JSONException ee) {
                ee.printStackTrace();
            }
            NetUtil.Scoket(jsonObject, 0, ctx);
        }

    }

    static int flag = 0;
    String name = null;
    int pn = 1;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
      //  inbackground();
        /**
         * socket Error 广播
         */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.Socket_Error},
                mSocketErrorReceiver);

        /**
         * 网络状况
         */
        BroadcastReceiverRegister.reg(this,
                new String[]{ConnectivityManager.CONNECTIVITY_ACTION},
                netstate);

        /**
         *  y50b广播
         */
        /* 移动 */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.Move_aciton}, move);

        /* 语音 */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.Speech_action}, speak);

        /* 语音复读 */
        BroadcastReceiverRegister.reg(this, new String[]{Constants.Talk}, talk);

        /* 任务提醒 */
        BroadcastReceiverRegister.reg(this, new String[]{
                Constants.Task_Remove, Constants.Task_Add,
                Constants.Task_Query, Constants.Task_Updata}, task);

        /* 注销机器人连接 */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.Stop}, stop);

        /* 相片 */
        BroadcastReceiverRegister.reg(this, new String[]{
                Constants.Photo_Delete, Constants.Photo_Query,
                Constants.Photo_Query_Name, Constants.Photo_Download}, photo);

        /* 修改机器人名字 */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.Robot_Info_Update}, flush);

        /* 连接机器人 */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.Robot_Connection}, connectRobot);

        /* 退出socket登录 */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.Socket_Logout}, socketLogout);

        /* Fota升级 */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.FOTA_UPDATE}, mFotaUpdateBR);

        /**
         * Y20广播
         */
        /* 建立视频房间 */
        BroadcastReceiverRegister.reg(this, new String[]{Constants.VIDEO_REQUEST}, mVideoRequestBR);

        /* 视频反馈 */
        BroadcastReceiverRegister.reg(this, new String[]{Constants.BR_REPLY}, mVideoReplyBR);

        /* 视频请求取消,取消当前拨号 */
        BroadcastReceiverRegister.reg(this, new String[]{Constants.BR_CANCEL_DIAL}, mCancelDialBR);

        /* 登入视频房间 */
        BroadcastReceiverRegister.reg(this, new String[]{Constants.LOGIN_VIDEO_ROOM}, mLoginVideoRoomBR);

        /* 登出视频房间 */
        BroadcastReceiverRegister.reg(this, new String[]{Constants.LOGOUT_VIDEO_ROOM}, mLogoutVideoRoomBR);

        /*问卷调查*/
        BroadcastReceiverRegister.reg(this, new String[]{Constants.QUESTIONNAIRE}, mQuestionnaireBR);

        /*急救中心*/
        BroadcastReceiverRegister.reg(this, new String[]{Constants.EMERGENCY}, mEmergencyBR);

        /*Agora视频会议邀请*/
        BroadcastReceiverRegister.reg(this, new String[]{Constants.AGORA_VIDEO_MEETING_INVITE}, mAgoraVideoMeetingInviteBR);

        /*Agora视频会议邀请取消*/
        BroadcastReceiverRegister.reg(this, new String[]{Constants.AGORA_VIDEO_MEETING_INVITE_CANCEL}, mAgoraVideoMeetingInviteCancelBR);

        /*Agora视频会议邀请回复*/
        BroadcastReceiverRegister.reg(this, new String[]{Constants.AGORA_VIDEO_MEETING_INVITE_REPLY}, mAgoraVideoMeetingInviteReplyBR);

        /*Agora视频会议计时*/
        BroadcastReceiverRegister.reg(this, new String[]{Constants.AGORA_VIDEO_MEETING_TIME}, mAgoraVideoMeetingTimeBR);

        /*发送变声后的语音*/
        BroadcastReceiverRegister.reg(this, new String[]{Constants.SEND_VOICE}, mVoiceSendBR);

        /*发送建图指令*/
        BroadcastReceiverRegister.reg(this, new String[]{Constants.MAPPING_ACTION}, mMappingBR);

        /*避障开关*/
        BroadcastReceiverRegister.reg(this, new String[]{Constants.BARRIER_SWITCH}, mBarrierSwitchBR);

        /*是否开始导航*/
        BroadcastReceiverRegister.reg(this, new String[]{Constants.WHETHER_NAVIGATION}, mWhetherNavigationBR);
        /**
         *  建立socket连接
         */
        connectSocketByLanguage();

        /**
         * 发送socket心跳
         */
        return super.onStartCommand(intent, flags, startId);
    }

    private void connectsocket(final SocketListener socketListener, final String ip , final String port) {
        ThreadPool.execute(new Runnable() {

            @Override
            public void run() {

                SocketConnect.InitSocket(socketListener, ip,
                        Integer.parseInt(port));
            }
        });
    }

    private void connectVideoSocket(final SocketListener socketListener, final String ip , final String port){
        ThreadPool.execute(new Runnable() {

            @Override
            public void run() {

                SocketConnect.InitVideoSocket(socketListener, ip,
                        Integer.parseInt(port));
            }
        });
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }
    private SocketListener listener = new SocketListener() {

        @Override
        public void writeData(final ChannelHandlerContext handler,
                              final MessageEvent event) {
            Object message = event.getMessage();
            ChannelBuffer buffer;
            if (message instanceof ChannelBuffer) {
                buffer = ((ChannelBuffer) message);
                Channels.write(handler, event.getFuture(), buffer);
            }
        }

        @Override
        public void receiveSuccess(final ChannelHandlerContext ctx,
                                   final MessageEvent e) {
            socketY20Receive(ctx, e);
            socketY50Receive(ctx, e);
        }

        @Override
        public void connectSuccess(final ChannelHandlerContext ctx,
                                   final ChannelStateEvent e) {
            setCtx(ctx);
            NetUtil.loginSocket(getSharedPreferences("userinfo", MODE_PRIVATE).getInt("id", 0),
                    getSharedPreferences("userinfo", MODE_PRIVATE).getString("session", ""),
                    ctx);
            time = new Timer();
            time.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (ctx != null) {
                        NetUtil.Scoket(new JSONObject(), 3, ctx);
                    } else {
                        handleError();
                    }
                }
            }, new Date(), 9000);
            initVideo();
            logUpload();
        }

        @Override
        public void connectFail() {
        }

        @Override
        public void connectClose(ChannelHandlerContext ctx,
                                 ChannelStateEvent e) {
//            Intent connectCloseIntent = new Intent(Constants.Socket_Error);
//            connectCloseIntent.putExtra("content", getString(R.string.connection_off));
//            sendBroadcast(connectCloseIntent);
            if (time != null) {
                time.cancel();
                time = null;
            }
            if (!Constants.isUserClose) {
                connectSocketByLanguage();
            } else {
                Constants.isUserClose = false;
            }
        }
    };


    /**
     * 初始化视频
     */
    private void initVideo(){
        YYDSDKHelper.getInstance().init(this);
        YYDSDKHelper.getInstance().setRole(Role.User);

        Config.init(this);
        Config.setTransferDataType(TransferDataType.AUDIOVIDEO);
        Config.setTransferType(TransferType.RTPOVERUDP);
        Config.setEncoderType(EncoderType.HARD_ENCODER);
        Config.setVideoSizeType(VideoSizeType.SIZE_640X480);
        Config.setFrameRateType(FrameRateType.LOW);
        Config.setBitRateType(BitrateType.LOW);

    }

    private void logUpload(){
		/* 发送错误日志 */
        String PATH_LOGCAT;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            PATH_LOGCAT = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + "miniGPS";
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = getFilesDir().getAbsolutePath()
                    + File.separator + "miniGPS";
        }
        if (!Utils.checknetwork(this)) {
            return;
        }
        final File file = new File(PATH_LOGCAT, "info.log");
        if (!file.exists()) {
            return;
        } else if (file.length() == 0) {
            return;
        } else {
            ThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        int id = getSharedPreferences("userinfo", MODE_PRIVATE).getInt("id", -1);
                        if (id == -1) {
                            return;
                        } else {
                            URL url = new URL(Constants.address + Constants.UPLOAD);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
							/* 允许Input、Output，不使用Cache */
                            conn.setDoInput(true);
                            conn.setDoOutput(true);
                            conn.setUseCaches(false);
							/* 设定传送的method=POST */
                            conn.setRequestMethod("POST");
							/* setRequestProperty */
                            conn.setRequestProperty("id", id + "");
                            conn.setRequestProperty("platform", "yyd");
                            conn.setRequestProperty("os", "android");
                            conn.setRequestProperty("Content-Length", file.length()+"");
                            conn.setRequestProperty("Content-Type", "text/plain;charset=utf-8;");
							/* 设定DataOutputStream */
                            DataOutputStream ds = new DataOutputStream(conn.getOutputStream());
							 /* 取得文件的FileInputStream */
                            FileInputStream fStream = new FileInputStream(file);
							/* 设定每次写入1024bytes */
                            int bufferSize = 1024;
                            byte[] buffer = new byte[bufferSize];
                            int length = -1;
							/* 从文件读取数据到缓冲区 */
                            while ((length = fStream.read(buffer)) != -1) {
								/* 将数据写入DataOutputStream中 */
                                ds.write(buffer, 0, length);
                            }
                            fStream.close();
                            ds.flush();
                            ds.close();
							/* 取得Response内容 */
                            InputStream is = conn.getInputStream();
                            int ch;
                            StringBuffer b = new StringBuffer();
                            while ((ch = is.read()) != -1) {
                                b.append((char)ch);
                            }
                            is.close();
                            JSONObject obj = new JSONObject(b.toString());
                            String ret = obj.getString("ret");
                            if (ret.equals("0")){
                                file.delete();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    @Override
    public void onDestroy() {
        Utils.unRegisterReceiver(netstate, this);
        Utils.unRegisterReceiver(mSocketErrorReceiver, this);
        Utils.unRegisterReceiver(photo, this);
        Utils.unRegisterReceiver(task, this);
        Utils.unRegisterReceiver(move, this);
        Utils.unRegisterReceiver(stop, this);
        Utils.unRegisterReceiver(speak, this);
        Utils.unRegisterReceiver(flush, this);
        Utils.unRegisterReceiver(connectRobot, this);
        Utils.unRegisterReceiver(socketLogout, this);
        Utils.unRegisterReceiver(mVideoRequestBR, this);
        Utils.unRegisterReceiver(mVideoReplyBR, this);
        Utils.unRegisterReceiver(mLoginVideoRoomBR, this);
        Utils.unRegisterReceiver(mFotaUpdateBR, this);
        Utils.unRegisterReceiver(mCancelDialBR, this);
        Utils.unRegisterReceiver(mCancelDialBR, this);
        Utils.unRegisterReceiver(mLogoutVideoRoomBR, this);
        Utils.unRegisterReceiver(talk, this);
        Utils.unRegisterReceiver(mQuestionnaireBR, this);
        Utils.unRegisterReceiver(mEmergencyBR, this);
        Utils.unRegisterReceiver(mAgoraVideoMeetingInviteBR, this);
        Utils.unRegisterReceiver(mAgoraVideoMeetingInviteCancelBR, this);
        Utils.unRegisterReceiver(mAgoraVideoMeetingInviteReplyBR, this);
        Utils.unRegisterReceiver(mAgoraVideoMeetingTimeBR, this);
        Utils.unRegisterReceiver(mVoiceSendBR, this);
        Utils.unRegisterReceiver(mMappingBR, this);
        Utils.unRegisterReceiver(mBarrierSwitchBR, this);
        Utils.unRegisterReceiver(mWhetherNavigationBR, this);

        if (time != null) {
            time.cancel();
            time = null;
        }

        if (ctx != null) {
            ctx.getChannel().close();
        }

        super.onDestroy();
    }


}
