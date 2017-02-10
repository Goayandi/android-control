//package com.yongyida.robot.activity;
//
//import android.app.AlertDialog;
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.database.Cursor;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Color;
//import android.graphics.drawable.Drawable;
//import android.media.ThumbnailUtils;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.PowerManager;
//import android.provider.MediaStore;
//import android.support.v4.view.ViewPager;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.text.ClipboardManager;
//import android.text.Editable;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.view.Gravity;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.WindowManager;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.AbsListView;
//import android.widget.AdapterView;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.easemob.EMChatRoomChangeListener;
//import com.easemob.EMError;
//import com.easemob.EMNotifierEvent;
//import com.easemob.EMValueCallBack;
//import com.easemob.chat.EMChatManager;
//import com.easemob.chat.EMChatRoom;
//import com.easemob.chat.EMContactManager;
//import com.easemob.chat.EMConversation;
//import com.easemob.chat.EMGroup;
//import com.easemob.chat.EMGroupManager;
//import com.easemob.chat.EMMessage;
//import com.easemob.chat.ImageMessageBody;
//import com.easemob.chat.LocationMessageBody;
//import com.easemob.chat.NormalFileMessageBody;
//import com.easemob.chat.TextMessageBody;
//import com.easemob.chat.VideoMessageBody;
//import com.easemob.chat.VoiceMessageBody;
//import com.easemob.exceptions.EaseMobException;
//import com.easemob.util.EMLog;
//import com.easemob.util.PathUtil;
//import com.easemob.util.VoiceRecorder;
//import com.yongyida.robot.R;
//import com.yongyida.robot.adapter.ExpressionPagerAdapter;
//import com.yongyida.robot.adapter.MessageAdapter;
//import com.yongyida.robot.adapter.VoicePlayClickListener;
//import com.yongyida.robot.huanxin.CommonUtils;
//import com.yongyida.robot.huanxin.DemoApplication;
//import com.yongyida.robot.huanxin.DemoHXSDKHelper;
//import com.yongyida.robot.huanxin.HXSDKHelper;
//import com.yongyida.robot.huanxin.RobotUser;
//import com.yongyida.robot.huanxin.UserUtils;
//import com.yongyida.robot.utils.ImageUtils;
//import com.yongyida.robot.widget.PasteEditText;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import static com.yongyida.robot.R.id.iv_emoticons_checked;
//import static com.yongyida.robot.R.id.iv_emoticons_normal;
//
///**
// * Created by Administrator on 2017/1/20 0020.
// */
//
//public class HuanxinChatActivity extends OriginalActivity implements View.OnClickListener{
//    private static final String TAG = "HuanxinChatActivity";
//    private static final int REQUEST_CODE_EMPTY_HISTORY = 2;
//    public static final int REQUEST_CODE_CONTEXT_MENU = 3;
//    private static final int REQUEST_CODE_MAP = 4;
//    public static final int REQUEST_CODE_TEXT = 5;
//    public static final int REQUEST_CODE_VOICE = 6;
//    public static final int REQUEST_CODE_PICTURE = 7;
//    public static final int REQUEST_CODE_LOCATION = 8;
//    public static final int REQUEST_CODE_NET_DISK = 9;
//    public static final int REQUEST_CODE_FILE = 10;
//    public static final int REQUEST_CODE_COPY_AND_PASTE = 11;
//    public static final int REQUEST_CODE_PICK_VIDEO = 12;
//    public static final int REQUEST_CODE_DOWNLOAD_VIDEO = 13;
//    public static final int REQUEST_CODE_VIDEO = 14;
//    public static final int REQUEST_CODE_DOWNLOAD_VOICE = 15;
//    public static final int REQUEST_CODE_SELECT_USER_CARD = 16;
//    public static final int REQUEST_CODE_SEND_USER_CARD = 17;
//    public static final int REQUEST_CODE_CAMERA = 18;
//    public static final int REQUEST_CODE_LOCAL = 19;
//    public static final int REQUEST_CODE_CLICK_DESTORY_IMG = 20;
//    public static final int REQUEST_CODE_GROUP_DETAIL = 21;
//    public static final int REQUEST_CODE_SELECT_VIDEO = 23;
//    public static final int REQUEST_CODE_SELECT_FILE = 24;
//    public static final int REQUEST_CODE_ADD_TO_BLACKLIST = 25;
//
//    public static final int RESULT_CODE_COPY = 1;
//    public static final int RESULT_CODE_DELETE = 2;
//    public static final int RESULT_CODE_FORWARD = 3;
//    public static final int RESULT_CODE_OPEN = 4;
//    public static final int RESULT_CODE_DWONLOAD = 5;
//    public static final int RESULT_CODE_TO_CLOUD = 6;
//    public static final int RESULT_CODE_EXIT_GROUP = 7;
//
//    public static final int CHATTYPE_SINGLE = 1;
//    public static final int CHATTYPE_GROUP = 2;
//    public static final int CHATTYPE_CHATROOM = 3;
//
//    public static final String COPY_IMAGE = "EASEMOBIMG";
//    private View recordingContainer;
//    private ImageView micImage;
//    private TextView recordingHint;
//    private ListView listView;
//    private PasteEditText mEditTextContent;
//    private View buttonSetModeKeyboard;
//    private View buttonSetModeVoice;
//    private View buttonSend;
//    private View buttonPressToSpeak;
//    private int position;
//    private ClipboardManager clipboard;
//    private InputMethodManager manager;
//    private Drawable[] micImages;
//    private int chatType;
//    private EMConversation conversation;
//    public static HuanxinChatActivity activityInstance = null;
//    // 给谁发送消息
//    private String toChatUsername;
//    private VoiceRecorder voiceRecorder;
//    private MessageAdapter adapter;
//    private File cameraFile;
//    static int resendPos;
//
//    private GroupListener groupListener;
//
//    private RelativeLayout edittext_layout;
//    private ProgressBar loadmorePB;
//    private boolean isloading;
//    private final int pagesize = 20;
//    private boolean haveMoreData = true;
//    public String playMsgId;
//
//    private SwipeRefreshLayout swipeRefreshLayout;
//
//    private Handler micImageHandler = new Handler() {
//        @Override
//        public void handleMessage(android.os.Message msg) {
//            // 切换msg切换图片
//            micImage.setImageDrawable(micImages[msg.what]);
//        }
//    };
//    public EMGroup group;
//    public EMChatRoom room;
//    public boolean isRobot;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chat);
//        activityInstance = this;
//        initView();
//        setUpView();
//    }
//
//    /**
//     * initView
//     */
//    protected void initView() {
//        recordingContainer = findViewById(R.id.recording_container);
//        micImage = (ImageView) findViewById(R.id.mic_image);
//        recordingHint = (TextView) findViewById(R.id.recording_hint);
//        listView = (ListView) findViewById(R.id.list);
//        mEditTextContent = (PasteEditText) findViewById(R.id.et_sendmessage);
//        buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
//        edittext_layout = (RelativeLayout) findViewById(R.id.edittext_layout);
//        buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
//        buttonSend = findViewById(R.id.btn_send);
//        buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
//        loadmorePB = (ProgressBar) findViewById(R.id.pb_load_more);
//        edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);
//
//        // 动画资源文件,用于录制语音时
//        micImages = new Drawable[] { getResources().getDrawable(R.drawable.record_animate_01),
//                getResources().getDrawable(R.drawable.record_animate_02),
//                getResources().getDrawable(R.drawable.record_animate_03),
//                getResources().getDrawable(R.drawable.record_animate_04),
//                getResources().getDrawable(R.drawable.record_animate_05),
//                getResources().getDrawable(R.drawable.record_animate_06),
//                getResources().getDrawable(R.drawable.record_animate_07),
//                getResources().getDrawable(R.drawable.record_animate_08),
//                getResources().getDrawable(R.drawable.record_animate_09),
//                getResources().getDrawable(R.drawable.record_animate_10),
//                getResources().getDrawable(R.drawable.record_animate_11),
//                getResources().getDrawable(R.drawable.record_animate_12),
//                getResources().getDrawable(R.drawable.record_animate_13),
//                getResources().getDrawable(R.drawable.record_animate_14) };
//
//        edittext_layout.requestFocus();
//        voiceRecorder = new VoiceRecorder(micImageHandler);
//        buttonPressToSpeak.setOnTouchListener(new PressToSpeakListen());
//        mEditTextContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_active);
//                } else {
//                    edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);
//                }
//
//            }
//        });
//        mEditTextContent.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_active);
//            }
//        });
//
//        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.chat_swipe_layout);
//
//        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
//                android.R.color.holo_orange_light, android.R.color.holo_red_light);
//
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//
//            @Override
//            public void onRefresh() {
//                new Handler().postDelayed(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        if (listView.getFirstVisiblePosition() == 0 && !isloading && haveMoreData) {
//                            List<EMMessage> messages;
//                            try {
//                                if (chatType == CHATTYPE_SINGLE){
//                                    messages = conversation.loadMoreMsgFromDB(adapter.getItem(0).getMsgId(), pagesize);
//                                }
//                                else{
//                                    messages = conversation.loadMoreGroupMsgFromDB(adapter.getItem(0).getMsgId(), pagesize);
//                                }
//                            } catch (Exception e1) {
//                                swipeRefreshLayout.setRefreshing(false);
//                                return;
//                            }
//
//                            if (messages.size() > 0) {
//                                adapter.notifyDataSetChanged();
//                                adapter.refreshSeekTo(messages.size() - 1);
//                                if (messages.size() != pagesize){
//                                    haveMoreData = false;
//                                }
//                            } else {
//                                haveMoreData = false;
//                            }
//
//                            isloading = false;
//
//                        }else{
//                            Toast.makeText(HuanxinChatActivity.this, getResources().getString(R.string.no_more_messages), Toast.LENGTH_SHORT).show();
//                        }
//                        swipeRefreshLayout.setRefreshing(false);
//                    }
//                }, 1000);
//            }
//        });
//    }
//
//    private void setUpView() {
//        // position = getIntent().getIntExtra("position", -1);
//        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(
//                PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");
//        // 判断单聊还是群聊
//        chatType = getIntent().getIntExtra("chatType", CHATTYPE_SINGLE);
//
//        if (chatType == CHATTYPE_SINGLE) { // 单聊
//            toChatUsername = getIntent().getStringExtra("userId");
//            Map<String,RobotUser> robotMap=((DemoHXSDKHelper) HXSDKHelper.getInstance()).getRobotList();
//            if(robotMap!=null&&robotMap.containsKey(toChatUsername)){
//                isRobot = true;
//                String nick = robotMap.get(toChatUsername).getNick();
//                if(!TextUtils.isEmpty(nick)){
//                    ((TextView) findViewById(R.id.name)).setText(nick);
//                }else{
//                    ((TextView) findViewById(R.id.name)).setText(toChatUsername);
//                }
//            }else{
//                UserUtils.setUserNick(toChatUsername, (TextView) findViewById(R.id.name));
//            }
//        } else {
//            // 群聊
//            findViewById(R.id.container_to_group).setVisibility(View.VISIBLE);
//            findViewById(R.id.container_remove).setVisibility(View.GONE);
//            toChatUsername = getIntent().getStringExtra("groupId");
//
//            if(chatType == CHATTYPE_GROUP){
//                onGroupViewCreation();
//            }else{
//                onChatRoomViewCreation();
//            }
//        }
//
//        // for chatroom type, we only init conversation and create view adapter on success
//        if(chatType != CHATTYPE_CHATROOM){
//            onConversationInit();
//
//            onListViewCreation();
//
//            // show forward message if the message is not null
//            String forward_msg_id = getIntent().getStringExtra("forward_msg_id");
//            if (forward_msg_id != null) {
//                // 显示发送要转发的消息
//                forwardMessage(forward_msg_id);
//            }
//        }
//    }
//
//    protected void onConversationInit(){
//        if(chatType == CHATTYPE_SINGLE){
//            conversation = EMChatManager.getInstance().getConversationByType(toChatUsername, EMConversation.EMConversationType.Chat);
//        }else if(chatType == CHATTYPE_GROUP){
//            conversation = EMChatManager.getInstance().getConversationByType(toChatUsername, EMConversation.EMConversationType.GroupChat);
//        }else if(chatType == CHATTYPE_CHATROOM){
//            conversation = EMChatManager.getInstance().getConversationByType(toChatUsername, EMConversation.EMConversationType.ChatRoom);
//        }
//
//        // 把此会话的未读数置为0
//        conversation.markAllMessagesAsRead();
//
//        // 初始化db时，每个conversation加载数目是getChatOptions().getNumberOfMessagesLoaded
//        // 这个数目如果比用户期望进入会话界面时显示的个数不一样，就多加载一些
//        final List<EMMessage> msgs = conversation.getAllMessages();
//        int msgCount = msgs != null ? msgs.size() : 0;
//        if (msgCount < conversation.getAllMsgCount() && msgCount < pagesize) {
//            String msgId = null;
//            if (msgs != null && msgs.size() > 0) {
//                msgId = msgs.get(0).getMsgId();
//            }
//            if (chatType == CHATTYPE_SINGLE) {
//                conversation.loadMoreMsgFromDB(msgId, pagesize);
//            } else {
//                conversation.loadMoreGroupMsgFromDB(msgId, pagesize);
//            }
//        }
//
//        EMChatManager.getInstance().addChatRoomChangeListener(new EMChatRoomChangeListener(){
//
//            @Override
//            public void onChatRoomDestroyed(String roomId, String roomName) {
//                if(roomId.equals(toChatUsername)){
//                    finish();
//                }
//            }
//
//            @Override
//            public void onMemberJoined(String roomId, String participant) {
//            }
//
//            @Override
//            public void onMemberExited(String roomId, String roomName,
//                                       String participant) {
//
//            }
//
//            @Override
//            public void onMemberKicked(String roomId, String roomName,
//                                       String participant) {
//                if(roomId.equals(toChatUsername)){
//                    String curUser = EMChatManager.getInstance().getCurrentUser();
//                    if(curUser.equals(participant)){
//                        EMChatManager.getInstance().leaveChatRoom(toChatUsername);
//                        finish();
//                    }
//                }
//            }
//
//        });
//    }
//
//    protected void onListViewCreation(){
//        adapter = new MessageAdapter(HuanxinChatActivity.this, toChatUsername, chatType);
//        // 显示消息
//        listView.setAdapter(adapter);
//
//        listView.setOnScrollListener(new ListScrollListener());
//        adapter.refreshSelectLast();
//
//        listView.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                hideKeyboard();
//                return false;
//            }
//        });
//    }
//
//    protected void onGroupViewCreation(){
//        group = EMGroupManager.getInstance().getGroup(toChatUsername);
//
//        if (group != null){
//            ((TextView) findViewById(R.id.name)).setText(group.getGroupName());
//        }else{
//            ((TextView) findViewById(R.id.name)).setText(toChatUsername);
//        }
//
//        // 监听当前会话的群聊解散被T事件
//        groupListener = new GroupListener();
//        EMGroupManager.getInstance().addGroupChangeListener(groupListener);
//    }
//
//    protected void onChatRoomViewCreation(){
//
//        final ProgressDialog pd = ProgressDialog.show(this, "", "Joining......");
//        EMChatManager.getInstance().joinChatRoom(toChatUsername, new EMValueCallBack<EMChatRoom>() {
//
//            @Override
//            public void onSuccess(EMChatRoom value) {
//                // TODO Auto-generated method stub
//                runOnUiThread(new Runnable(){
//                    @Override
//                    public void run(){
//                        pd.dismiss();
//                        room = EMChatManager.getInstance().getChatRoom(toChatUsername);
//                        if(room !=null){
//                            ((TextView) findViewById(R.id.name)).setText(room.getName());
//                        }else{
//                            ((TextView) findViewById(R.id.name)).setText(toChatUsername);
//                        }
//                        EMLog.d(TAG, "join room success : " + room.getName());
//
//                        onConversationInit();
//
//                        onListViewCreation();
//                    }
//                });
//            }
//
//            @Override
//            public void onError(final int error, String errorMsg) {
//                // TODO Auto-generated method stub
//                EMLog.d(TAG, "join room failure : " + error);
//                runOnUiThread(new Runnable(){
//                    @Override
//                    public void run(){
//                        pd.dismiss();
//                    }
//                });
//                finish();
//            }
//        });
//    }
//
//    /**
//     * onActivityResult
//     */
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_CODE_EXIT_GROUP) {
//            setResult(RESULT_OK);
//            finish();
//            return;
//        }
//        if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
//            switch (resultCode) {
//                case RESULT_CODE_COPY: // 复制消息
//                    EMMessage copyMsg = ((EMMessage) adapter.getItem(data.getIntExtra("position", -1)));
//                    // clipboard.setText(SmileUtils.getSmiledText(HuanxinChatActivity.this,
//                    // ((TextMessageBody) copyMsg.getBody()).getMessage()));
//                    clipboard.setText(((TextMessageBody) copyMsg.getBody()).getMessage());
//                    break;
//                case RESULT_CODE_DELETE: // 删除消息
//                    EMMessage deleteMsg = (EMMessage) adapter.getItem(data.getIntExtra("position", -1));
//                    conversation.removeMessage(deleteMsg.getMsgId());
//                    adapter.refreshSeekTo(data.getIntExtra("position", adapter.getCount()) - 1);
//                    break;
//
//                case RESULT_CODE_FORWARD: // 转发消息
////                    EMMessage forwardMsg = (EMMessage) adapter.getItem(data.getIntExtra("position", 0));
////                    Intent intent = new Intent(this, ForwardMessageActivity.class);
////                    intent.putExtra("forward_msg_id", forwardMsg.getMsgId());
////                    startActivity(intent);
//
//                    break;
//
//                default:
//                    break;
//            }
//        }
//        if (resultCode == RESULT_OK) { // 清空消息
//            if (requestCode == REQUEST_CODE_EMPTY_HISTORY) {
//                // 清空会话
//                EMChatManager.getInstance().clearConversation(toChatUsername);
//                adapter.refresh();
//            }  else if (requestCode == REQUEST_CODE_TEXT || requestCode == REQUEST_CODE_VOICE
//                    || requestCode == REQUEST_CODE_PICTURE || requestCode == REQUEST_CODE_LOCATION
//                    || requestCode == REQUEST_CODE_VIDEO || requestCode == REQUEST_CODE_FILE) {
//                resendMessage();
//            } else if (requestCode == REQUEST_CODE_COPY_AND_PASTE) {
//                // 粘贴
//                if (!TextUtils.isEmpty(clipboard.getText())) {
//                    String pasteText = clipboard.getText().toString();
//                    if (pasteText.startsWith(COPY_IMAGE)) {
//                        // 把图片前缀去掉，还原成正常的path
//                        sendPicture(pasteText.replace(COPY_IMAGE, ""));
//                    }
//
//                }
//            } else if (requestCode == REQUEST_CODE_ADD_TO_BLACKLIST) { // 移入黑名单
//                EMMessage deleteMsg = (EMMessage) adapter.getItem(data.getIntExtra("position", -1));
//                addUserToBlacklist(deleteMsg.getFrom());
//            } else if (conversation.getMsgCount() > 0) {
//                adapter.refresh();
//                setResult(RESULT_OK);
//            } else if (requestCode == REQUEST_CODE_GROUP_DETAIL) {
//                adapter.refresh();
//            }
//        }
//    }
//
//    /**
//     * 消息图标点击事件
//     *
//     * @param view
//     */
//    @Override
//    public void onClick(View view) {
//        String st1 = getResources().getString(R.string.not_connect_to_server);
//        int id = view.getId();
//        if (id == R.id.btn_send) {// 点击发送按钮(发文字和表情)
//            String s = mEditTextContent.getText().toString();
//            sendText(s);
//        }
//    }
//
//    /**
//     * 事件监听
//     *
//     * see {@link EMNotifierEvent}
//     */
//    @Override
//    public void onEvent(EMNotifierEvent event) {
//        switch (event.getEvent()) {
//            case EventNewMessage:
//            {
//                //获取到message
//                EMMessage message = (EMMessage) event.getData();
//
//                String username = null;
//                //群组消息
//                if(message.getChatType() == EMMessage.ChatType.GroupChat || message.getChatType() == EMMessage.ChatType.ChatRoom){
//                    username = message.getTo();
//                }
//                else{
//                    //单聊消息
//                    username = message.getFrom();
//                }
//
//                //如果是当前会话的消息，刷新聊天页面
//                if(username.equals(getToChatUsername())){
//                    refreshUIWithNewMessage();
//                    //声音和震动提示有新消息
//                    HXSDKHelper.getInstance().getNotifier().viberateAndPlayTone(message);
//                }else{
//                    //如果消息不是和当前聊天ID的消息
//                    HXSDKHelper.getInstance().getNotifier().onNewMsg(message);
//                }
//
//                break;
//            }
//            case EventDeliveryAck:
//            {
//                //获取到message
//                EMMessage message = (EMMessage) event.getData();
//                refreshUI();
//                break;
//            }
//            case EventReadAck:
//            {
//                //获取到message
//                EMMessage message = (EMMessage) event.getData();
//                refreshUI();
//                break;
//            }
//            case EventOfflineMessage:
//            {
//                //a list of offline messages
//                //List<EMMessage> offlineMessages = (List<EMMessage>) event.getData();
//                refreshUI();
//                break;
//            }
//            default:
//                break;
//        }
//
//    }
//
//
//    private void refreshUIWithNewMessage(){
//        if(adapter == null){
//            return;
//        }
//
//        runOnUiThread(new Runnable() {
//            public void run() {
//                adapter.refreshSelectLast();
//            }
//        });
//    }
//
//    private void refreshUI() {
//        if(adapter == null){
//            return;
//        }
//
//        runOnUiThread(new Runnable() {
//            public void run() {
//                adapter.refresh();
//            }
//        });
//    }
//
//
//    /**
//     * 发送文本消息
//     *
//     * @param content
//     *            message content
//     */
//    public void sendText(String content) {
//
//        if (content.length() > 0) {
//            EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
//            // 如果是群聊，设置chattype,默认是单聊
//            if (chatType == CHATTYPE_GROUP){
//                message.setChatType(EMMessage.ChatType.GroupChat);
//            }else if(chatType == CHATTYPE_CHATROOM){
//                message.setChatType(EMMessage.ChatType.ChatRoom);
//            }
//            if(isRobot){
//                message.setAttribute("em_robot_message", true);
//            }
//            TextMessageBody txtBody = new TextMessageBody(content);
//            // 设置消息body
//            message.addBody(txtBody);
//            // 设置要发给谁,用户username或者群聊groupid
//            message.setReceipt(toChatUsername);
//            // 把messgage加到conversation中
//            conversation.addMessage(message);
//            // 通知adapter有消息变动，adapter会根据加入的这条message显示消息和调用sdk的发送方法
//            adapter.refreshSelectLast();
//            mEditTextContent.setText("");
//
//            setResult(RESULT_OK);
//
//        }
//    }
//
//    /**
//     * 发送语音
//     *
//     * @param filePath
//     * @param fileName
//     * @param length
//     * @param isResend
//     */
//    private void sendVoice(String filePath, String fileName, String length, boolean isResend) {
//        if (!(new File(filePath).exists())) {
//            return;
//        }
//        try {
//            final EMMessage message = EMMessage.createSendMessage(EMMessage.Type.VOICE);
//            // 如果是群聊，设置chattype,默认是单聊
//            if (chatType == CHATTYPE_GROUP){
//                message.setChatType(EMMessage.ChatType.GroupChat);
//            }else if(chatType == CHATTYPE_CHATROOM){
//                message.setChatType(EMMessage.ChatType.ChatRoom);
//            }
//            message.setReceipt(toChatUsername);
//            int len = Integer.parseInt(length);
//            VoiceMessageBody body = new VoiceMessageBody(new File(filePath), len);
//            message.addBody(body);
//            if(isRobot){
//                message.setAttribute("em_robot_message", true);
//            }
//            conversation.addMessage(message);
//            adapter.refreshSelectLast();
//            setResult(RESULT_OK);
//            // send file
//            // sendVoiceSub(filePath, fileName, message);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 发送图片
//     *
//     * @param filePath
//     */
//    private void sendPicture(final String filePath) {
//        String to = toChatUsername;
//        // create and add image message in view
//        final EMMessage message = EMMessage.createSendMessage(EMMessage.Type.IMAGE);
//        // 如果是群聊，设置chattype,默认是单聊
//        if (chatType == CHATTYPE_GROUP){
//            message.setChatType(EMMessage.ChatType.GroupChat);
//        }else if(chatType == CHATTYPE_CHATROOM){
//            message.setChatType(EMMessage.ChatType.ChatRoom);
//        }
//
//        message.setReceipt(to);
//        ImageMessageBody body = new ImageMessageBody(new File(filePath));
//        // 默认超过100k的图片会压缩后发给对方，可以设置成发送原图
//        // body.setSendOriginalImage(true);
//        message.addBody(body);
//        if(isRobot){
//            message.setAttribute("em_robot_message", true);
//        }
//        conversation.addMessage(message);
//
//        listView.setAdapter(adapter);
//        adapter.refreshSelectLast();
//        setResult(RESULT_OK);
//        // more(more);
//    }
//
//
//    /**
//     * 重发消息
//     */
//    private void resendMessage() {
//        EMMessage msg = null;
//        msg = conversation.getMessage(resendPos);
//        // msg.setBackSend(true);
//        msg.status = EMMessage.Status.CREATE;
//
//        adapter.refreshSeekTo(resendPos);
//    }
//
//    /**
//     * 显示语音图标按钮
//     *
//     * @param view
//     */
//    public void setModeVoice(View view) {
//        hideKeyboard();
//        edittext_layout.setVisibility(View.GONE);
//        view.setVisibility(View.GONE);
//        buttonSetModeKeyboard.setVisibility(View.VISIBLE);
//        buttonSend.setVisibility(View.GONE);
//        buttonPressToSpeak.setVisibility(View.VISIBLE);
//    }
//
//    /**
//     * 显示键盘图标
//     *
//     * @param view
//     */
//    public void setModeKeyboard(View view) {
//        // mEditTextContent.setOnFocusChangeListener(new OnFocusChangeListener()
//        // {
//        // @Override
//        // public void onFocusChange(View v, boolean hasFocus) {
//        // if(hasFocus){
//        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//        // }
//        // }
//        // });
//        edittext_layout.setVisibility(View.VISIBLE);
//        view.setVisibility(View.GONE);
//        buttonSetModeVoice.setVisibility(View.VISIBLE);
//        // mEditTextContent.setVisibility(View.VISIBLE);
//        mEditTextContent.requestFocus();
//        // buttonSend.setVisibility(View.VISIBLE);
//        buttonPressToSpeak.setVisibility(View.GONE);
//        if (TextUtils.isEmpty(mEditTextContent.getText())) {
//            buttonSend.setVisibility(View.GONE);
//        } else {
//            buttonSend.setVisibility(View.VISIBLE);
//        }
//
//    }
//
//    /**
//     * 点击清空聊天记录
//     *
//     * @param view
//     */
//    public void emptyHistory(View view) {
//        String st5 = getResources().getString(R.string.Whether_to_empty_all_chats);
//        startActivityForResult(new Intent(this, AlertDialog.class).putExtra("titleIsCancel", true).putExtra("msg", st5)
//                .putExtra("cancel", true), REQUEST_CODE_EMPTY_HISTORY);
//    }
//
//    /**
//     * 点击进入群组详情
//     *
//     * @param view
//     */
//    public void toGroupDetails(View view) {
//        if (room == null && group == null) {
//            Toast.makeText(getApplicationContext(), R.string.gorup_not_found, 0).show();
//            return;
//        }
//        if(chatType == CHATTYPE_GROUP){
//            startActivityForResult((new Intent(this, GroupDetailsActivity.class).putExtra("groupId", toChatUsername)),
//                    REQUEST_CODE_GROUP_DETAIL);
//        }else{
//            startActivityForResult((new Intent(this, ChatRoomDetailsActivity.class).putExtra("roomId", toChatUsername)),
//                    REQUEST_CODE_GROUP_DETAIL);
//        }
//    }
//
//    /**
//     * 显示或隐藏图标按钮页
//     *
//     * @param view
//     */
//    public void toggleMore(View view) {
//        if (more.getVisibility() == View.GONE) {
//            EMLog.d(TAG, "more gone");
//            hideKeyboard();
//            more.setVisibility(View.VISIBLE);
//            btnContainer.setVisibility(View.VISIBLE);
//            emojiIconContainer.setVisibility(View.GONE);
//        } else {
//            if (emojiIconContainer.getVisibility() == View.VISIBLE) {
//                emojiIconContainer.setVisibility(View.GONE);
//                btnContainer.setVisibility(View.VISIBLE);
//                iv_emoticons_normal.setVisibility(View.VISIBLE);
//                iv_emoticons_checked.setVisibility(View.INVISIBLE);
//            } else {
//                more.setVisibility(View.GONE);
//            }
//
//        }
//
//    }
//
//    /**
//     * 点击文字输入框
//     *
//     * @param v
//     */
//    public void editClick(View v) {
//        listView.setSelection(listView.getCount() - 1);
//        if (more.getVisibility() == View.VISIBLE) {
//            more.setVisibility(View.GONE);
//            iv_emoticons_normal.setVisibility(View.VISIBLE);
//            iv_emoticons_checked.setVisibility(View.INVISIBLE);
//        }
//
//    }
//
//    private PowerManager.WakeLock wakeLock;
//
//    /**
//     * 按住说话listener
//     *
//     */
//    class PressToSpeakListen implements View.OnTouchListener {
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    if (!CommonUtils.isExitsSdcard()) {
//                        String st4 = getResources().getString(R.string.Send_voice_need_sdcard_support);
//                        Toast.makeText(HuanxinChatActivity.this, st4, Toast.LENGTH_SHORT).show();
//                        return false;
//                    }
//                    try {
//                        v.setPressed(true);
//                        wakeLock.acquire();
//                        if (VoicePlayClickListener.isPlaying)
//                            VoicePlayClickListener.currentPlayListener.stopPlayVoice();
//                        recordingContainer.setVisibility(View.VISIBLE);
//                        recordingHint.setText(getString(R.string.move_up_to_cancel));
//                        recordingHint.setBackgroundColor(Color.TRANSPARENT);
//                        voiceRecorder.startRecording(null, toChatUsername, getApplicationContext());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        v.setPressed(false);
//                        if (wakeLock.isHeld())
//                            wakeLock.release();
//                        if (voiceRecorder != null)
//                            voiceRecorder.discardRecording();
//                        recordingContainer.setVisibility(View.INVISIBLE);
//                        Toast.makeText(HuanxinChatActivity.this, R.string.recoding_fail, Toast.LENGTH_SHORT).show();
//                        return false;
//                    }
//
//                    return true;
//                case MotionEvent.ACTION_MOVE: {
//                    if (event.getY() < 0) {
//                        recordingHint.setText(getString(R.string.release_to_cancel));
//                        recordingHint.setBackgroundResource(R.drawable.recording_text_hint_bg);
//                    } else {
//                        recordingHint.setText(getString(R.string.move_up_to_cancel));
//                        recordingHint.setBackgroundColor(Color.TRANSPARENT);
//                    }
//                    return true;
//                }
//                case MotionEvent.ACTION_UP:
//                    v.setPressed(false);
//                    recordingContainer.setVisibility(View.INVISIBLE);
//                    if (wakeLock.isHeld())
//                        wakeLock.release();
//                    if (event.getY() < 0) {
//                        // discard the recorded audio.
//                        voiceRecorder.discardRecording();
//
//                    } else {
//                        // stop recording and send voice file
//                        String st1 = getResources().getString(R.string.Recording_without_permission);
//                        String st2 = getResources().getString(R.string.The_recording_time_is_too_short);
//                        String st3 = getResources().getString(R.string.send_failure_please);
//                        try {
//                            int length = voiceRecorder.stopRecoding();
//                            if (length > 0) {
//                                sendVoice(voiceRecorder.getVoiceFilePath(), voiceRecorder.getVoiceFileName(toChatUsername),
//                                        Integer.toString(length), false);
//                            } else if (length == EMError.INVALID_FILE) {
//                                Toast.makeText(getApplicationContext(), st1, Toast.LENGTH_SHORT).show();
//                            } else {
//                                Toast.makeText(getApplicationContext(), st2, Toast.LENGTH_SHORT).show();
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            Toast.makeText(HuanxinChatActivity.this, st3, Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//                    return true;
//                default:
//                    recordingContainer.setVisibility(View.INVISIBLE);
//                    if (voiceRecorder != null)
//                        voiceRecorder.discardRecording();
//                    return false;
//            }
//        }
//    }
//
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        activityInstance = null;
//        if(groupListener != null){
//            EMGroupManager.getInstance().removeGroupChangeListener(groupListener);
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (group != null)
//            ((TextView) findViewById(R.id.name)).setText(group.getGroupName());
//        voiceCallBtn.setEnabled(true);
//        videoCallBtn.setEnabled(true);
//
//        if(adapter != null){
//            adapter.refresh();
//        }
//
//        DemoHXSDKHelper sdkHelper = (DemoHXSDKHelper) DemoHXSDKHelper.getInstance();
//        sdkHelper.pushActivity(this);
//        // register the event listener when enter the foreground
//        EMChatManager.getInstance().registerEventListener(
//                this,
//                new EMNotifierEvent.Event[] { EMNotifierEvent.Event.EventNewMessage,EMNotifierEvent.Event.EventOfflineMessage,
//                        EMNotifierEvent.Event.EventDeliveryAck, EMNotifierEvent.Event.EventReadAck });
//    }
//
//    @Override
//    protected void onStop() {
//        // unregister this event listener when this activity enters the
//        // background
//        EMChatManager.getInstance().unregisterEventListener(this);
//
//        DemoHXSDKHelper sdkHelper = (DemoHXSDKHelper) DemoHXSDKHelper.getInstance();
//
//        // 把此activity 从foreground activity 列表里移除
//        sdkHelper.popActivity(this);
//
//        super.onStop();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (wakeLock.isHeld())
//            wakeLock.release();
//        if (VoicePlayClickListener.isPlaying && VoicePlayClickListener.currentPlayListener != null) {
//            // 停止语音播放
//            VoicePlayClickListener.currentPlayListener.stopPlayVoice();
//        }
//
//        try {
//            // 停止录音
//            if (voiceRecorder.isRecording()) {
//                voiceRecorder.discardRecording();
//                recordingContainer.setVisibility(View.INVISIBLE);
//            }
//        } catch (Exception e) {
//        }
//    }
//
//    /**
//     * 隐藏软键盘
//     */
//    private void hideKeyboard() {
//        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
//            if (getCurrentFocus() != null)
//                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//        }
//    }
//
//    /**
//     * 加入到黑名单
//     *
//     * @param username
//     */
//    private void addUserToBlacklist(final String username) {
//        final ProgressDialog pd = new ProgressDialog(this);
//        pd.setMessage(getString(R.string.Is_moved_into_blacklist));
//        pd.setCanceledOnTouchOutside(false);
//        pd.show();
//        new Thread(new Runnable() {
//            public void run() {
//                try {
//                    EMContactManager.getInstance().addUserToBlackList(username, false);
//                    runOnUiThread(new Runnable() {
//                        public void run() {
//                            pd.dismiss();
//                            Toast.makeText(getApplicationContext(), R.string.Move_into_blacklist_success, 0).show();
//                        }
//                    });
//                } catch (EaseMobException e) {
//                    e.printStackTrace();
//                    runOnUiThread(new Runnable() {
//                        public void run() {
//                            pd.dismiss();
//                            Toast.makeText(getApplicationContext(), R.string.Move_into_blacklist_failure, 0).show();
//                        }
//                    });
//                }
//            }
//        }).start();
//    }
//
//    /**
//     * 返回
//     *
//     * @param view
//     */
//    public void back(View view) {
//        EMChatManager.getInstance().unregisterEventListener(this);
//        if(chatType == CHATTYPE_CHATROOM){
//            EMChatManager.getInstance().leaveChatRoom(toChatUsername);
//        }
//        finish();
//    }
//
//    /**
//     * 覆盖手机返回键
//     */
//    @Override
//    public void onBackPressed() {
//        if (more.getVisibility() == View.VISIBLE) {
//            more.setVisibility(View.GONE);
//            iv_emoticons_normal.setVisibility(View.VISIBLE);
//            iv_emoticons_checked.setVisibility(View.INVISIBLE);
//        } else {
//            super.onBackPressed();
//            if(chatType == CHATTYPE_CHATROOM){
//                EMChatManager.getInstance().leaveChatRoom(toChatUsername);
//            }
//        }
//    }
//
//    /**
//     * listview滑动监听listener
//     *
//     */
//    private class ListScrollListener implements AbsListView.OnScrollListener {
//
//        @Override
//        public void onScrollStateChanged(AbsListView view, int scrollState) {
//            switch (scrollState) {
//                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
//				/*if (view.getFirstVisiblePosition() == 0 && !isloading && haveMoreData && conversation.getAllMessages().size() != 0) {
//					isloading = true;
//					loadmorePB.setVisibility(View.VISIBLE);
//					// sdk初始化加载的聊天记录为20条，到顶时去db里获取更多
//					List<EMMessage> messages;
//					EMMessage firstMsg = conversation.getAllMessages().get(0);
//					try {
//						// 获取更多messges，调用此方法的时候从db获取的messages
//						// sdk会自动存入到此conversation中
//						if (chatType == CHATTYPE_SINGLE)
//							messages = conversation.loadMoreMsgFromDB(firstMsg.getMsgId(), pagesize);
//						else
//							messages = conversation.loadMoreGroupMsgFromDB(firstMsg.getMsgId(), pagesize);
//					} catch (Exception e1) {
//						loadmorePB.setVisibility(View.GONE);
//						return;
//					}
//					try {
//						Thread.sleep(300);
//					} catch (InterruptedException e) {
//					}
//					if (messages.size() != 0) {
//						// 刷新ui
//						if (messages.size() > 0) {
//							adapter.refreshSeekTo(messages.size() - 1);
//						}
//
//						if (messages.size() != pagesize)
//							haveMoreData = false;
//					} else {
//						haveMoreData = false;
//					}
//					loadmorePB.setVisibility(View.GONE);
//					isloading = false;
//
//				}*/
//                    break;
//            }
//        }
//
//        @Override
//        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//
//        }
//
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        // 点击notification bar进入聊天页面，保证只有一个聊天页面
//        String username = intent.getStringExtra("userId");
//        if (toChatUsername.equals(username))
//            super.onNewIntent(intent);
//        else {
//            finish();
//            startActivity(intent);
//        }
//
//    }
//
//    /**
//     * 转发消息
//     *
//     * @param forward_msg_id
//     */
//    protected void forwardMessage(String forward_msg_id) {
//        final EMMessage forward_msg = EMChatManager.getInstance().getMessage(forward_msg_id);
//        EMMessage.Type type = forward_msg.getType();
//        switch (type) {
//            case TXT:
//                // 获取消息内容，发送消息
//                String content = ((TextMessageBody) forward_msg.getBody()).getMessage();
//                sendText(content);
//                break;
//            default:
//                break;
//        }
//
//        if(forward_msg.getChatType() == EMMessage.ChatType.ChatRoom){
//            EMChatManager.getInstance().leaveChatRoom(forward_msg.getTo());
//        }
//    }
//
//    public String getToChatUsername() {
//        return toChatUsername;
//    }
//
//    public ListView getListView() {
//        return listView;
//    }
//}
