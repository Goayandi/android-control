package com.yongyida.robot.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

import com.yongyida.robot.R;
import com.yongyida.robot.bean.Result;
import com.yongyida.robot.broadcastReceiver.NetStateBroadcastReceiver;
import com.yongyida.robot.broadcastReceiver.SocketErrorReceiver;
import com.yongyida.robot.net.helper.Decoder;
import com.yongyida.robot.net.helper.SocketConnect;
import com.yongyida.robot.net.helper.SocketConnect.SocketListener;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.FileUtil;
import com.yongyida.robot.utils.NetUtil;
import com.yongyida.robot.utils.ThreadPool;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SocketService extends Service {

    private Timer time;
    boolean the = true;
    private ChannelHandlerContext ctx;
    private NetStateBroadcastReceiver netstate = new NetStateBroadcastReceiver();
    private String size = "";

    @Override
    public void onCreate() {
        Log.i("SocketService","onCreate");
        /**
         * socket Error 广播
         */
        BroadcastReceiverRegister.reg(getApplicationContext(),
                new String[]{Constants.Socket_Error},
                new SocketErrorReceiver());

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
        BroadcastReceiverRegister.reg(getApplicationContext(),
                new String[]{Constants.Move_aciton}, move);

        /* 语音 */
        BroadcastReceiverRegister.reg(getApplicationContext(),
                new String[]{Constants.Speech_action}, speak);

        /* 任务提醒 */
        BroadcastReceiverRegister.reg(getApplicationContext(), new String[]{
                Constants.Task_Remove, Constants.Task_Add,
                Constants.Task_Query, Constants.Task_Updata}, task);

        /* 注销机器人连接 */
        BroadcastReceiverRegister.reg(getApplicationContext(),
                new String[]{Constants.Stop}, stop);

        /* 相片 */
        BroadcastReceiverRegister.reg(getApplicationContext(), new String[]{
                Constants.Photo_Delete, Constants.Photo_Query,
                Constants.Photo_Query_Name, Constants.Photo_Download}, photo);

        /* 修改机器人名字 */
        BroadcastReceiverRegister.reg(getApplicationContext(),
                new String[]{Constants.Robot_Info_Update}, flush);

        /* 连接机器人 */
        BroadcastReceiverRegister.reg(getApplicationContext(),
                new String[]{Constants.Robot_Connection}, connectRobot);
        /**
         * 其它产品广播
         */
        //TODO
        /**
         *  建立socket连接
         */
        connectSocketByLanguage();

        /**
         * 发送socket心跳
         */
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
        JSONObject Result;
        try {
            if (o instanceof JSONObject) {
                Result = (JSONObject) o;
                String cmd = Result.getString("cmd");
                if (Constants.CMD_MEDIA_INVITE.equals(cmd)) {
                    int ret = Result.getInt("ret");
                    String mediaTcpIp = Result.getString("media_tcp_ip");
                    int mediaTcpPort = Result.getInt("media_tcp_port");
                    int roomId = Result.getInt("room_id");

                } else if (Constants.CMD_MEDIA_CANCEL.equals(cmd)) {
                    int ret = Result.getInt("ret");
                    //TODO
                } else if (Constants.CMD_MEDIA_REPLY.equals(cmd)) {
                    int ret = Result.getInt("ret");
                    String mediaTcpIp = Result.getString("media_tcp_ip");
                    int mediaTcpPort = Result.getInt("media_tcp_port");
                    int roomId = Result.getInt("room_id");

                } else if (Constants.CMD_MEDIA_LOGIN.equals(cmd)) {
                    String UserMedias = Result.getString("UserMedias");

                } else if (Constants.CMD_MEDIA_LOGOUT.equals(cmd)) {

                } else if (Constants.CMD_MEDIA_JOIN.equals(cmd)) {

                } else if (Constants.CMD_MEDIA_CALLBACK.equals(cmd)) {
                    String command = Result.getString("command");
                } else {

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
        Intent in = new Intent("online");

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
            Log.i("Message", e.getMessage().toString());
            try {
                Result = (JSONObject) o;
                callback = Result.getString("cmd");
                if (callback.equals("/robot/callback")) {
                    JSONObject queryresult = new JSONObject(
                            Result.getString("command"));
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
                    } else {
                        sendBroadcast(new Intent("result")
                                .putExtra("result", queryresult
                                        .getString("data")));
                    }
                    return;
                } else if (callback.equals("/robot/uncontroll")) {
                    Intent socketErrorIntent = new Intent(Constants.Socket_Error);
                    socketErrorIntent.putExtra("content", getString(R.string.robot_offline));
                    sendBroadcast(socketErrorIntent);
                    return;
                } else if (callback.equals("/robot/flush")) {
                    sendBroadcast(new Intent("flush").putExtra(
                            "result", "success"));
                    return;
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            try {
                ret = Result.getInt("ret");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            switch (ret) {
                case 0:
                    flag = 1;
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
                    in.putExtra("ret", 6);
                    the = true;
                    break;
                default:
                    the = true;
                    break;

            }
            sendBroadcast(in);
        } else if (o instanceof Decoder.Result2) {
            Log.i("Success", "收到图片");
            final Decoder.Result2 res = (Decoder.Result2) o;
            com.yongyida.robot.bean.Result result = new Result();
            result.setDw(BitmapFactory.decodeByteArray(res.datas,
                    0, res.datas.length));
            try {
                name = new JSONObject(res.json.getString("command"))
                        .getString("name");

                result.setName(name);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            size = "small";
            ThreadPool.execute(new Runnable() {

                @Override
                public void run() {
                    File file = new File(getApplication()
                            .getExternalFilesDir(null)
                            .getAbsolutePath()
                            + "/"
                            + getSharedPreferences("Receipt",
                            MODE_PRIVATE).getString(
                            "username", null) + size);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    name = FileUtil.confusePhotoName(name);
                    FileUtil.writefile(
                            getApplication().getExternalFilesDir(
                                    null).getAbsolutePath()
                                    + "/"
                                    + getSharedPreferences(
                                    "Receipt", MODE_PRIVATE)
                                    .getString("username",
                                            null) + size,
                            res.datas, name);
                                /* 照片先转成bitmap 旋转90度再存起来 */
                    File picture = new File(file.getAbsolutePath() + "/" + name);
                    Bitmap b = BitmapFactory.decodeFile(picture.getAbsolutePath());
                    Matrix matrix = new Matrix();
                    matrix.setRotate(-90);
                    Bitmap bm = b.createBitmap(b, 0, 0, b.getWidth(),
                            b.getHeight(), matrix, true);
                    b.recycle();
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(picture);
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                    Intent reply = new Intent(Constants.Photo_Reply);
                    sendBroadcast(reply);
                }
            });

        }
    }

    public void connectSocketByLanguage(){
        String stateCode = getSharedPreferences("Receipt", MODE_PRIVATE).getString("state_code", null);
        String ip;
        String port;
        if (Constants.HK_CODE.equals(stateCode)) {
            ip = Constants.ip_hk;
            port = Constants.port_hk;
        } else {
            ip = Constants.ip;
            port = Constants.port;
        }
        connectsocket(listener, ip, port);
    }

    BroadcastReceiver speak = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {

            if (intent.getAction().equals(Constants.Speech_action)) {
                JSONObject params = new JSONObject();
                NetUtil.Scoket(params, 2, ctx);
            }
        }
    };
    BroadcastReceiver move = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Constants.Move_aciton)) {
                JSONObject params = new JSONObject();
                NetUtil.Scoket(params, 1, ctx);

            }
        }

    };
    ;;
    BroadcastReceiver task = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            NetUtil.socket(ctx, intent);
        }
    };
    BroadcastReceiver stop = new BroadcastReceiver() {

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
                        Log.i("id",
                                getSharedPreferences("userinfo", MODE_PRIVATE)
                                        .getInt("id", 0) + "");
                        jsonObject.put("session",
                                getSharedPreferences("userinfo", MODE_PRIVATE)
                                        .getString("session", null));
                        jsonObject.put("rid",
                                getSharedPreferences("Receipt", MODE_PRIVATE)
                                        .getString("robotid", null));
                    } catch (JSONException ee) {
                        ee.printStackTrace();
                    }
                    NetUtil.Scoket(jsonObject, 4, ctx);
                    flag = 0;
                    Constants.flag = false;
                }
            }
        }
    };
    BroadcastReceiver photo = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            NetUtil.photo_socket(ctx, intent);
        }
    };
    BroadcastReceiver flush = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            NetUtil.robotinfoupdate(ctx, intent);
        }
    };

    BroadcastReceiver connectRobot = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            control(ctx);
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
                Log.i("id", getSharedPreferences("userinfo", MODE_PRIVATE)
                        .getInt("id", 0) + "");
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

    private void inbackground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> prs = activityManager
                .getRunningAppProcesses();
        for (int i = 0; i < prs.size(); i++) {
            if (prs.get(i).equals(getPackageName())) {
                pn = 0;
            }
        }
        if (pn == 1) {
            sendBroadcast(new Intent(Constants.Stop));
            stopSelf();
        }
    }

    static int flag = 0;
    String name = null;
    int pn = 1;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
      //  inbackground();

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

            socketY50Receive(ctx, e);
        }

        @Override
        public void connectSuccess(final ChannelHandlerContext ctx,
                                   final ChannelStateEvent e) {
            setCtx(ctx);
            time = new Timer();
            time.schedule(new TimerTask() {
                @Override
                public void run() {
                    NetUtil.Scoket(new JSONObject(), 3, ctx);
                }
            }, new Date(), 9000);

        }

        @Override
        public void connectFail() {
            Log.i("Connect", "connectFail");
            connectSocketByLanguage();
        }

        @Override
        public void connectClose(ChannelHandlerContext ctx,
                                 ChannelStateEvent e) {
            //TODO
            Intent connectCloseIntent = new Intent(Constants.Socket_Error);
            connectCloseIntent.putExtra("content", getString(R.string.connection_off));
            sendBroadcast(connectCloseIntent);
            Log.i("Connect", "connectClose");
        }
    };

    @Override
    public void onDestroy() {
        Log.i("SocketService","onDestroy");
        if (netstate != null) {
            unregisterReceiver(netstate);
        }
        if (time != null) {
            time.cancel();
        }
      //  ctx.getChannel().close();
        // if (photo != null) {
        // unregisterReceiver(photo);
        // }
        // if (task != null) {
        // unregisterReceiver(task);
        // }
        // if (move != null) {
        // unregisterReceiver(move);
        // }
        // if (stop != null) {
        // unregisterReceiver(stop);
        // }
        // if (speak != null) {
        // unregisterReceiver(speak);
        // }
        // if (flush != null) {
        // unregisterReceiver(flush);
        // }
        super.onDestroy();
    }
}
