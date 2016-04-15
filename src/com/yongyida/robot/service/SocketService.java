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

    Timer time = new Timer();

    private SocketListener listener;
    private SocketListener listenerVideoRequestY20;
    boolean the = true;
    private ChannelHandlerContext ctx;
    private MessageEvent e;
    private NetStateBroadcastReceiver netstate = new NetStateBroadcastReceiver();
    private String size = "";

    @Override
    public void onCreate() {

        BroadcastReceiverRegister.reg(getApplicationContext(),
                new String[]{Constants.Move_aciton}, move);
        BroadcastReceiverRegister.reg(getApplicationContext(),
                new String[]{Constants.Speech_action}, speak);
        BroadcastReceiverRegister.reg(getApplicationContext(), new String[]{
                Constants.Task_Remove, Constants.Task_Add,
                Constants.Task_Query, Constants.Task_Updata}, task);
        BroadcastReceiverRegister.reg(getApplicationContext(),
                new String[]{Constants.Stop}, stop);
        BroadcastReceiverRegister.reg(getApplicationContext(), new String[]{
                Constants.Photo_Delete, Constants.Photo_Query,
                Constants.Photo_Query_Name, Constants.Photo_Download}, photo);
        BroadcastReceiverRegister.reg(this,
                new String[]{ConnectivityManager.CONNECTIVITY_ACTION},
                netstate);
        BroadcastReceiverRegister.reg(getApplicationContext(),
                new String[]{Constants.Socket_Error},
                new SocketErrorReceiver());
        BroadcastReceiverRegister.reg(getApplicationContext(),
                new String[]{Constants.Robot_Info_Update}, flush);
        BroadcastReceiverRegister.reg(getApplicationContext(),
                new String[]{Constants.Start_Socket}, startsocket);

        BroadcastReceiverRegister.reg(getApplicationContext(),
                new String[]{Constants.RESPONSE_VIDEO_REQUEST}, videoRequest);

    }

    @Override
    public IBinder onBind(Intent arg0) {

        return null;
    }

    BroadcastReceiver videoRequest = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            listenerVideoRequestY20 = new SocketListener() {
                @Override
                public void connectFail() {

                }

                @Override
                public void connectSuccess(ChannelHandlerContext ctx, ChannelStateEvent e) {
                }

                @Override
                public void writeData(ChannelHandlerContext ctx, MessageEvent e) {

                }

                @Override
                public void receiveSuccess(ChannelHandlerContext ctx, MessageEvent e) {
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

                @Override
                public void connectClose(ChannelHandlerContext ctx, ChannelStateEvent e) {

                }
            };
            if (ctx != null && e != null) {
                Channels.close(ctx, e.getFuture());
            }
            //connectsocket(listener, ip, port);
        }
    };

    BroadcastReceiver startsocket = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, final Intent type) {
            listener = new SocketListener() {

                @Override
                public void writeData(final ChannelHandlerContext ctx,
                                      final MessageEvent e) {
                    control(ctx, e);
                }

                @Override
                public void receiveSuccess(final ChannelHandlerContext ctx,
                                           final MessageEvent e) {
                    String typeString = type.getStringExtra(Constants.TYPE);
                    int ret = 0;
                    Object o = e.getMessage();
                    String callback = "";
                    JSONObject Result = null;
                    Object obj = null;
                    Intent in = new Intent("online");
                    in.putExtra(Constants.TYPE, typeString);

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
                                time = new Timer();
                                time.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        NetUtil.Scoket(new JSONObject(), 3, e, ctx);
                                    }
                                }, new Date(), 9000);
                                in.putExtra("ret", 0);
                                Constants.flag = true;
                                break;
                            case -1:
                                in.putExtra("ret", -1);
                                the = true;
                                Channels.close(ctx, e.getFuture());
                                break;
                            case 1:
                                in.putExtra("ret", 1);
                                the = true;
                                Channels.close(ctx, e.getFuture());
                                break;
                            case 2:
                                in.putExtra("ret", 2);
                                Channels.close(ctx, e.getFuture());
                                the = true;
                                break;
                            case 3:
                                in.putExtra("ret", 3);
                                Channels.close(ctx, e.getFuture());
                                the = true;
                                break;
                            case 4:
                                in.putExtra("ret", 4);
                                Channels.close(ctx, e.getFuture());
                                the = true;
                                break;
                            case 5:
                                in.putExtra("ret", 5);
                                Channels.close(ctx, e.getFuture());
                                the = true;
                                break;
                            case 6:
                                in.putExtra("ret", 6);
                                Channels.close(ctx, e.getFuture());
                                the = true;
                                break;
                            case 7:
                                in.putExtra("ret", 6);
                                Channels.close(ctx, e.getFuture());
                                the = true;
                                break;
                            default:
                                the = true;
                                Channels.close(ctx, e.getFuture());
                                break;

                        }
                        sendBroadcast(in);
                    } else if (o instanceof Decoder.Result2) {
                        Log.i("Success", "收到图片");
                        final Decoder.Result2 res = (Decoder.Result2) o;
                        Result result = new Result();
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

                @Override
                public void connectSuccess(final ChannelHandlerContext ctx,
                                           final ChannelStateEvent e) {
                    NetUtil.Scoket(new JSONObject(), ctx);
                }

                @Override
                public void connectFail() {
                    Log.i("Connect", "connectFail");
                    connectSocketByLanguage();
                }

                @Override
                public void connectClose(ChannelHandlerContext ctx,
                                         ChannelStateEvent e) {
                    Intent connectCloseIntent = new Intent(Constants.Socket_Error);
                    connectCloseIntent.putExtra("content", getString(R.string.connection_off));
                    sendBroadcast(connectCloseIntent);
                    Log.i("Connect", "connectClose");
                }
            };
            if (ctx != null && e != null) {
                Channels.close(ctx, e.getFuture());
            }
            connectSocketByLanguage();
        }
    };

    public void connectSocketByLanguage(){
        String stateCode = getSharedPreferences("Receipt", MODE_PRIVATE).getString("state_code", null);
        String ip;
        String port;
        ip = Constants.ip;
        port = Constants.port;
        connectsocket(listener, ip, port);
    }

    BroadcastReceiver speak = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {

            if (intent.getAction().equals(Constants.Speech_action)) {
                JSONObject params = new JSONObject();
                NetUtil.Scoket(params, 2, e, ctx);
            }
        }
    };
    BroadcastReceiver move = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Constants.Move_aciton)) {
                JSONObject params = new JSONObject();
                if (e != null) {
                    NetUtil.Scoket(params, 1, e, ctx);
                }

            }
        }

    };
    ;;
    BroadcastReceiver task = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            NetUtil.socket(e, ctx, intent);
        }
    };
    BroadcastReceiver stop = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals("stop")) {
                if (time != null && ctx != null && e != null) {
                    time.cancel();
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
                    NetUtil.Scoket(jsonObject, 4, e, ctx);
                    Channels.close(ctx, e.getFuture());
                    flag = 0;
                    Constants.flag = false;
                    stopSelf();
                }
            }
        }
    };
    BroadcastReceiver photo = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            NetUtil.photo_socket(e, ctx, intent);
        }
    };
    ;

    BroadcastReceiver flush = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            NetUtil.robotinfoupdate(e, ctx, intent);
        }
    };

    private void control(final ChannelHandlerContext ctx, final MessageEvent e) {

        if (flag == 0) {
            this.ctx = ctx;
            this.e = e;
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
            NetUtil.Scoket(jsonObject, 0, e, ctx);
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
        inbackground();

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

    @Override
    public void onDestroy() {
        if (netstate != null) {
            unregisterReceiver(netstate);
        }
        if (time != null) {
            time.cancel();
        }
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
