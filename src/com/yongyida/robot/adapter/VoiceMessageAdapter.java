package com.yongyida.robot.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.FileMessageBody;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.LocationMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VideoMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.util.DensityUtil;
import com.easemob.util.EMLog;
import com.easemob.util.LatLng;
import com.easemob.util.TextFormater;
import com.yongyida.robot.R;
import com.yongyida.robot.activity.VoiceChatActivity;
import com.yongyida.robot.activity.ContextMenu;
import com.yongyida.robot.activity.ShowBigImage;
import com.yongyida.robot.activity.ShowVideoActivity;
import com.yongyida.robot.huanxin.Constant;
import com.yongyida.robot.huanxin.DemoHXSDKHelper;
import com.yongyida.robot.huanxin.HXSDKHelper;
import com.yongyida.robot.huanxin.ImageCache;
import com.yongyida.robot.huanxin.LoadImageTask;
import com.yongyida.robot.huanxin.LoadVideoImageTask;
import com.yongyida.robot.huanxin.UserUtils;
import com.yongyida.robot.utils.DateUtils;
import com.yongyida.robot.utils.ImageUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/7/11 0011.
 */
public class VoiceMessageAdapter extends BaseAdapter {
    private final static String TAG = "msg";

    private static final int MESSAGE_TYPE_RECV_TXT = 0;
    private static final int MESSAGE_TYPE_SENT_TXT = 1;
    private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
    private static final int MESSAGE_TYPE_SENT_LOCATION = 3;
    private static final int MESSAGE_TYPE_RECV_LOCATION = 4;
    private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
    private static final int MESSAGE_TYPE_SENT_VOICE = 6;
    private static final int MESSAGE_TYPE_RECV_VOICE = 7;
    private static final int MESSAGE_TYPE_SENT_VIDEO = 8;
    private static final int MESSAGE_TYPE_RECV_VIDEO = 9;
    private static final int MESSAGE_TYPE_SENT_FILE = 10;
    private static final int MESSAGE_TYPE_RECV_FILE = 11;
    private static final int MESSAGE_TYPE_SENT_VOICE_CALL = 12;
    private static final int MESSAGE_TYPE_RECV_VOICE_CALL = 13;
    private static final int MESSAGE_TYPE_SENT_VIDEO_CALL = 14;
    private static final int MESSAGE_TYPE_RECV_VIDEO_CALL = 15;
    private static final int MESSAGE_TYPE_SENT_ROBOT_MENU = 16;
    private static final int MESSAGE_TYPE_RECV_ROBOT_MENU = 17;

    public static final String IMAGE_DIR = "chat/image/";
    public static final String VOICE_DIR = "chat/audio/";
    public static final String VIDEO_DIR = "chat/video";

    private String username;
    private LayoutInflater inflater;
    private Activity activity;

    private static final int HANDLER_MESSAGE_REFRESH_LIST = 0;
    private static final int HANDLER_MESSAGE_SELECT_LAST = 1;
    private static final int HANDLER_MESSAGE_SEEK_TO = 2;

    // reference to conversation object in chatsdk
    private EMConversation conversation;
    EMMessage[] messages = null;

    private Context context;

    private Map<String, Timer> timers = new Hashtable<String, Timer>();

    public VoiceMessageAdapter(Context context, String username, int chatType) {
        this.username = username;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (Activity) context;
        this.conversation = EMChatManager.getInstance().getConversation(username);
    }

    Handler handler = new Handler() {
        private void refreshList() {
            // UI线程不能直接使用conversation.getAllMessages()
            // 否则在UI刷新过程中，如果收到新的消息，会导致并发问题
        //    EMMessage[] msgs = (EMMessage[]) conversation.getAllMessages().toArray(new EMMessage[conversation.getAllMessages().size()]);
            List<EMMessage> msgs = conversation.getAllMessages();
            List<EMMessage> tmp = new ArrayList<EMMessage>();
            for (int i = 0; i < msgs.size(); i++) {
                conversation.getMessage(i);
                if(msgs.get(i).getType() == EMMessage.Type.VOICE) {
                    tmp.add(msgs.get(i));
                }
            }
            messages = tmp.toArray(new EMMessage[tmp.size()]);
//            for (int i = 0; i < messages.length; i++) {
//                    // getMessage will set message as read status
//            //
//            }
            notifyDataSetChanged();
        }

        @Override
        public void handleMessage(android.os.Message message) {
            switch (message.what) {
                case HANDLER_MESSAGE_REFRESH_LIST:
                    refreshList();
                    break;
                case HANDLER_MESSAGE_SELECT_LAST:
                    if (activity instanceof VoiceChatActivity) {
                        ListView listView = ((VoiceChatActivity)activity).getListView();
                        if (messages.length > 0) {
                            listView.setSelection(messages.length - 1);
                        }
                    }
                    break;
                case HANDLER_MESSAGE_SEEK_TO:
                    int position = message.arg1;
                    if (activity instanceof VoiceChatActivity) {
                        ListView listView = ((VoiceChatActivity)activity).getListView();
                        listView.setSelection(position);
                    }
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 获取item数
     */
    public int getCount() {
        return messages == null ? 0 : messages.length;
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        if (handler.hasMessages(HANDLER_MESSAGE_REFRESH_LIST)) {
            return;
        }
        android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST);
        handler.sendMessage(msg);
    }

    /**
     * 刷新页面, 选择最后一个
     */
    public void refreshSelectLast() {
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_SELECT_LAST));
    }

    /**
     * 刷新页面, 选择Position
     */
    public void refreshSeekTo(int position) {
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
        android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_SEEK_TO);
        msg.arg1 = position;
        handler.sendMessage(msg);
    }

    public EMMessage getItem(int position) {
        if (messages != null && position < messages.length) {
            return messages[position];
        }
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * 获取item类型数
     */
    public int getViewTypeCount() {
        return 18;
    }

    /**
     * 获取item类型
     */
    public int getItemViewType(int position) {
        EMMessage message = getItem(position);
        if (message == null) {
            return -1;
        }
        if (message.getType() == EMMessage.Type.TXT) {
            if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false))
                return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE_CALL : MESSAGE_TYPE_SENT_VOICE_CALL;
            else if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VIDEO_CALL, false))
                return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO_CALL : MESSAGE_TYPE_SENT_VIDEO_CALL;
            else if(((DemoHXSDKHelper) HXSDKHelper.getInstance()).isRobotMenuMessage(message))
                return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_ROBOT_MENU : MESSAGE_TYPE_SENT_ROBOT_MENU;
            else
                return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_TXT : MESSAGE_TYPE_SENT_TXT;
        }
        if (message.getType() == EMMessage.Type.IMAGE) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_IMAGE : MESSAGE_TYPE_SENT_IMAGE;

        }
        if (message.getType() == EMMessage.Type.LOCATION) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_LOCATION : MESSAGE_TYPE_SENT_LOCATION;
        }
        if (message.getType() == EMMessage.Type.VOICE) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE : MESSAGE_TYPE_SENT_VOICE;
        }
        if (message.getType() == EMMessage.Type.VIDEO) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO : MESSAGE_TYPE_SENT_VIDEO;
        }
        if (message.getType() == EMMessage.Type.FILE) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_FILE : MESSAGE_TYPE_SENT_FILE;
        }

        return -1;// invalid
    }


    private View createViewByMessage(EMMessage message, int position) {
        switch (message.getType()) {
            case LOCATION:
                return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_location, null) : inflater.inflate(
                        R.layout.row_sent_location, null);
            case IMAGE:
                return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_picture, null) : inflater.inflate(
                        R.layout.row_sent_picture, null);

            case VOICE:
                return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_voice, null) : inflater.inflate(
                        R.layout.row_sent_voice, null);
            case VIDEO:
                return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_video, null) : inflater.inflate(
                        R.layout.row_sent_video, null);
            case FILE:
                return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_file, null) : inflater.inflate(
                        R.layout.row_sent_file, null);
            default:
                // 语音通话
                if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false))
                    return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_voice_call, null) : inflater
                            .inflate(R.layout.row_sent_voice_call, null);
                    // 视频通话
                else if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VIDEO_CALL, false))
                    return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_video_call,
                            null) : inflater.inflate(R.layout.row_sent_video_call, null);
                    // 含有菜单的消息	
                else if (((DemoHXSDKHelper)HXSDKHelper.getInstance()).isRobotMenuMessage(message))
                    return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_menu, null)
                            : inflater.inflate(R.layout.row_sent_message, null);
                else
                    return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_message,
                            null) : inflater.inflate(R.layout.row_sent_message, null);
        }
    }

    @SuppressLint("NewApi")
    public View getView(final int position, View convertView, ViewGroup parent) {
        final EMMessage message = getItem(position);
        EMMessage.ChatType chatType = message.getChatType();
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = createViewByMessage(message, position);
            if (message.getType() == EMMessage.Type.IMAGE) {
                try {
                    holder.iv = ((ImageView) convertView.findViewById(R.id.iv_sendPicture));
                    holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.percentage);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.progressBar);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_usernick = (TextView) convertView.findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }

            } else if (message.getType() == EMMessage.Type.TXT) {
                try {
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_userhead);
                    // 这里是文字内容
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                    holder.tv_usernick = (TextView) convertView.findViewById(R.id.tv_userid);

                    holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                    holder.tvList = (LinearLayout) convertView.findViewById(R.id.ll_layout);
                } catch (Exception e) {
                }

                // 语音通话及视频通话
                if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)
                        || message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VIDEO_CALL, false)) {
                    holder.iv = (ImageView) convertView.findViewById(R.id.iv_call_icon);
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                }

            } else if (message.getType() == EMMessage.Type.VOICE) {
                try {
                    holder.iv = ((ImageView) convertView.findViewById(R.id.iv_voice));
                    holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_length);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_usernick = (TextView) convertView.findViewById(R.id.tv_userid);
                    holder.iv_read_status = (ImageView) convertView.findViewById(R.id.iv_unread_voice);
                } catch (Exception e) {
                }
            } else if (message.getType() == EMMessage.Type.LOCATION) {
                try {
                    holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_location);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_usernick = (TextView) convertView.findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }
            } else if (message.getType() == EMMessage.Type.VIDEO) {
                try {
                    holder.iv = ((ImageView) convertView.findViewById(R.id.chatting_content_iv));
                    holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.percentage);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.progressBar);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.size = (TextView) convertView.findViewById(R.id.chatting_size_iv);
                    holder.timeLength = (TextView) convertView.findViewById(R.id.chatting_length_iv);
                    holder.playBtn = (ImageView) convertView.findViewById(R.id.chatting_status_btn);
                    holder.container_status_btn = (LinearLayout) convertView.findViewById(R.id.container_status_btn);
                    holder.tv_usernick = (TextView) convertView.findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }
            } else if (message.getType() == EMMessage.Type.FILE) {
                try {
                    holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv_file_name = (TextView) convertView.findViewById(R.id.tv_file_name);
                    holder.tv_file_size = (TextView) convertView.findViewById(R.id.tv_file_size);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_file_download_state = (TextView) convertView.findViewById(R.id.tv_file_state);
                    holder.ll_container = (LinearLayout) convertView.findViewById(R.id.ll_file_container);
                    // 这里是进度值
                    holder.tv = (TextView) convertView.findViewById(R.id.percentage);
                } catch (Exception e) {
                }
                try {
                    holder.tv_usernick = (TextView) convertView.findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }

            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

       /* // 群聊时，显示接收的消息的发送人的名称
        if ((chatType == EMMessage.ChatType.GroupChat || chatType == EMMessage.ChatType.ChatRoom) && message.direct == EMMessage.Direct.RECEIVE){
            //demo里使用username代码nick
            UserUtils.setUserNick(message.getFrom(), holder.tv_usernick);
        }*/
        if(message.direct == EMMessage.Direct.SEND){
            UserUtils.setCurrentUserNick(holder.tv_usernick);
        }
        // 如果是发送的消息并且不是群聊消息，显示已读textview
        if (!(chatType == EMMessage.ChatType.GroupChat || chatType == EMMessage.ChatType.ChatRoom) && message.direct == EMMessage.Direct.SEND) {
            holder.tv_ack = (TextView) convertView.findViewById(R.id.tv_ack);
            holder.tv_delivered = (TextView) convertView.findViewById(R.id.tv_delivered);
            if (holder.tv_ack != null) {
                if (message.isAcked) {
                    if (holder.tv_delivered != null) {
                        holder.tv_delivered.setVisibility(View.INVISIBLE);
                    }
                    holder.tv_ack.setVisibility(View.VISIBLE);
                } else {
                    holder.tv_ack.setVisibility(View.INVISIBLE);

                    // check and display msg delivered ack status
                    if (holder.tv_delivered != null) {
                        if (message.isDelivered) {
                            holder.tv_delivered.setVisibility(View.VISIBLE);
                        } else {
                            holder.tv_delivered.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        } else {
            // 如果是文本或者地图消息并且不是group messgae,chatroom message，显示的时候给对方发送已读回执
            if ((message.getType() == EMMessage.Type.TXT || message.getType() == EMMessage.Type.LOCATION) && !message.isAcked && chatType != EMMessage.ChatType.GroupChat && chatType != EMMessage.ChatType.ChatRoom) {
                // 不是语音通话记录
                if (!message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                    try {
                        EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                        // 发送已读回执
                        message.isAcked = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //设置用户头像
        setUserAvatar(message, holder.iv_avatar);

        switch (message.getType()) {
            case VOICE: // 语音
                handleVoiceMessage(message, holder, position, convertView);
                break;
        }

        if (message.direct == EMMessage.Direct.SEND) {
            View statusView = convertView.findViewById(R.id.msg_status);
            // 重发按钮点击事件
            statusView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // 显示重发消息的自定义alertdialog
                    Intent intent = new Intent(activity, AlertDialog.class);
                    intent.putExtra("msg", activity.getString(R.string.confirm_resend));
                    intent.putExtra("title", activity.getString(R.string.resend));
                    intent.putExtra("cancel", true);
                    intent.putExtra("position", position);
                    if (message.getType() == EMMessage.Type.VOICE)
                        activity.startActivityForResult(intent, VoiceChatActivity.REQUEST_CODE_VOICE);
                }
            });

        } else {
            final String st = context.getResources().getString(R.string.Into_the_blacklist);
            if(!((VoiceChatActivity)activity).isRobot && chatType != EMMessage.ChatType.ChatRoom){
                // 长按头像，移入黑名单
                holder.iv_avatar.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        Intent intent = new Intent(activity, AlertDialog.class);
                        intent.putExtra("msg", st);
                        intent.putExtra("cancel", true);
                        intent.putExtra("position", position);
                        activity.startActivityForResult(intent, VoiceChatActivity.REQUEST_CODE_ADD_TO_BLACKLIST);
                        return true;
                    }
                });
            }
        }

        TextView timestamp = (TextView) convertView.findViewById(R.id.timestamp);

        if (position == 0) {
            timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
            timestamp.setVisibility(View.VISIBLE);
        } else {
            // 两条消息时间离得如果稍长，显示时间
            EMMessage prevMessage = getItem(position - 1);
            if (prevMessage != null && DateUtils.isCloseEnough(message.getMsgTime(), prevMessage.getMsgTime())) {
                timestamp.setVisibility(View.GONE);
            } else {
                timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
                timestamp.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }


    /**
     * 显示用户头像
     * @param message
     * @param imageView
     */
    private void setUserAvatar(final EMMessage message, ImageView imageView){
        if (imageView != null) {
            if (message.direct == EMMessage.Direct.SEND) {
                //显示自己头像
                UserUtils.setCurrentUserAvatar(context, imageView);
            } else {
                UserUtils.setUserAvatar(context, message.getFrom(), imageView);
            }
        }
    }


    /**
     * 语音消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleVoiceMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView) {
        VoiceMessageBody voiceBody = (VoiceMessageBody) message.getBody();
        int len = voiceBody.getLength();
        if(len>0){
            holder.tv.setText(voiceBody.getLength() + "\"");
            holder.tv.setVisibility(View.VISIBLE);
        }else{
            holder.tv.setVisibility(View.INVISIBLE);
        }
        holder.iv.setOnClickListener(new VoicePlayClickListener(message, holder.iv, holder.iv_read_status, this, activity, username));
        holder.iv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.startActivityForResult(
                        (new Intent(activity, ContextMenu.class)).putExtra("position", position).putExtra("type",
                                EMMessage.Type.VOICE.ordinal()), VoiceChatActivity.REQUEST_CODE_CONTEXT_MENU);
                return true;
            }
        });
        if (((VoiceChatActivity)activity).playMsgId != null
                && ((VoiceChatActivity)activity).playMsgId.equals(message
                .getMsgId())&&VoicePlayClickListener.isPlaying) {
            AnimationDrawable voiceAnimation;
            if (message.direct == EMMessage.Direct.RECEIVE) {
                holder.iv.setImageResource(R.drawable.voice_from_icon);
            } else {
                holder.iv.setImageResource(R.drawable.voice_to_icon);
            }
            voiceAnimation = (AnimationDrawable) holder.iv.getDrawable();
            voiceAnimation.start();
        } else {
            if (message.direct == EMMessage.Direct.RECEIVE) {
                holder.iv.setImageResource(R.drawable.chatfrom_voice_playing);
            } else {
                holder.iv.setImageResource(R.drawable.chatto_voice_playing);
            }
        }


        if (message.direct == EMMessage.Direct.RECEIVE) {
            if (message.isListened()) {
                // 隐藏语音未听标志
                holder.iv_read_status.setVisibility(View.INVISIBLE);
            } else {
                holder.iv_read_status.setVisibility(View.VISIBLE);
            }
            EMLog.d(TAG, "it is receive msg");
            if (message.status == EMMessage.Status.INPROGRESS) {
                holder.pb.setVisibility(View.VISIBLE);
                EMLog.d(TAG, "!!!! back receive");
                ((FileMessageBody) message.getBody()).setDownloadCallback(new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                holder.pb.setVisibility(View.INVISIBLE);
                                notifyDataSetChanged();
                            }
                        });

                    }

                    @Override
                    public void onProgress(int progress, String status) {
                    }

                    @Override
                    public void onError(int code, String message) {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                holder.pb.setVisibility(View.INVISIBLE);
                            }
                        });

                    }
                });

            } else {
                holder.pb.setVisibility(View.INVISIBLE);

            }
            return;
        }

        // until here, deal with send voice msg
        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.GONE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                holder.pb.setVisibility(View.VISIBLE);
                holder.staus_iv.setVisibility(View.GONE);
                break;
            default:
                sendMsgInBackground(message, holder);
        }
    }



    /**
     * 发送消息
     *
     * @param message
     * @param holder
     * @param position
     */
    public void sendMsgInBackground(final EMMessage message, final ViewHolder holder) {
        holder.staus_iv.setVisibility(View.GONE);
        holder.pb.setVisibility(View.VISIBLE);

        final long start = System.currentTimeMillis();
        // 调用sdk发送异步发送方法
        EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

            @Override
            public void onSuccess() {

                updateSendedView(message, holder);
            }

            @Override
            public void onError(int code, String error) {

                updateSendedView(message, holder);
            }

            @Override
            public void onProgress(int progress, String status) {
            }

        });

    }

    /*
     * chat sdk will automatic download thumbnail image for the image message we
     * need to register callback show the download progress
     */
    private void showDownloadImageProgress(final EMMessage message, final ViewHolder holder) {
        EMLog.d(TAG, "!!! show download image progress");
        // final ImageMessageBody msgbody = (ImageMessageBody)
        // message.getBody();
        final FileMessageBody msgbody = (FileMessageBody) message.getBody();
        if(holder.pb!=null)
            holder.pb.setVisibility(View.VISIBLE);
        if(holder.tv!=null)
            holder.tv.setVisibility(View.VISIBLE);

        msgbody.setDownloadCallback(new EMCallBack() {

            @Override
            public void onSuccess() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // message.setBackReceive(false);
                        if (message.getType() == EMMessage.Type.IMAGE) {
                            holder.pb.setVisibility(View.GONE);
                            holder.tv.setVisibility(View.GONE);
                        }
                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onProgress(final int progress, String status) {
                if (message.getType() == EMMessage.Type.IMAGE) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            holder.tv.setText(progress + "%");

                        }
                    });
                }

            }

        });
    }


    /**
     * 更新ui上消息发送状态
     *
     * @param message
     * @param holder
     */
    private void updateSendedView(final EMMessage message, final ViewHolder holder) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // send success
                if (message.getType() == EMMessage.Type.VIDEO) {
                    holder.tv.setVisibility(View.GONE);
                }
                EMLog.d(TAG, "message status : " + message.status);
                if (message.status == EMMessage.Status.SUCCESS) {
                    // if (message.getType() == EMMessage.Type.FILE) {
                    // holder.pb.setVisibility(View.INVISIBLE);
                    // holder.staus_iv.setVisibility(View.INVISIBLE);
                    // } else {
                    // holder.pb.setVisibility(View.GONE);
                    // holder.staus_iv.setVisibility(View.GONE);
                    // }

                } else if (message.status == EMMessage.Status.FAIL) {
                    // if (message.getType() == EMMessage.Type.FILE) {
                    // holder.pb.setVisibility(View.INVISIBLE);
                    // } else {
                    // holder.pb.setVisibility(View.GONE);
                    // }
                    // holder.staus_iv.setVisibility(View.VISIBLE);

                    if(message.getError() == EMError.MESSAGE_SEND_INVALID_CONTENT){
                        Toast.makeText(activity, activity.getString(R.string.send_fail) + activity.getString(R.string.error_send_invalid_content), 0)
                                .show();
                    }else if(message.getError() == EMError.MESSAGE_SEND_NOT_IN_THE_GROUP){
                        Toast.makeText(activity, activity.getString(R.string.send_fail) + activity.getString(R.string.error_send_not_in_the_group), 0)
                                .show();
                    }else{
                        Toast.makeText(activity, activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), 0)
                                .show();
                    }
                }

                notifyDataSetChanged();
            }
        });
    }



    public static class ViewHolder {
        ImageView iv;
        TextView tv;
        ProgressBar pb;
        ImageView staus_iv;
        ImageView iv_avatar;
        TextView tv_usernick;
        ImageView playBtn;
        TextView timeLength;
        TextView size;
        LinearLayout container_status_btn;
        LinearLayout ll_container;
        ImageView iv_read_status;
        // 显示已读回执状态
        TextView tv_ack;
        // 显示送达回执状态
        TextView tv_delivered;

        TextView tv_file_name;
        TextView tv_file_size;
        TextView tv_file_download_state;

        TextView tvTitle;
        LinearLayout tvList;
    }



}
