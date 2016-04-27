package com.yongyida.robot.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.yongyida.robot.R;
import com.yongyida.robot.huanxin.CommonUtils;
import com.yongyida.robot.huanxin.Constant;
import com.yongyida.robot.huanxin.DemoApplication;
import com.yongyida.robot.huanxin.DemoHXSDKHelper;
import com.yongyida.robot.huanxin.HXSDKHelper;
import com.yongyida.robot.huanxin.User;
import com.yongyida.robot.huanxin.UserDao;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.HandlerUtil;
import com.yongyida.robot.utils.NetUtil;
import com.yongyida.robot.utils.NetUtil.callback;
import com.yongyida.robot.utils.StartUtil;
import com.yongyida.robot.utils.ThreadPool;
import com.yongyida.robot.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("NewApi")
public class LoginActivity extends BaseActivity implements OnClickListener {

    private static final int REQUEST_STATE_CODE = 1;
    private EditText edit_phonenum;
    private EditText edit_vaildcode;
    private Button login;
    private Button getvaild;
    private LinearLayout ll_state;
    private TextView tv_state;
    private TextView tv_state_num;
    private String stateCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    ProgressDialog progress = null;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                // 手机号验证
                if (edit_phonenum.getText() == null
                        || edit_phonenum.getText().toString().equals("")) {
                    ToastUtil.showtomain(LoginActivity.this, getString(R.string.input_phone_number));
                    return;
                }
                if (edit_vaildcode.getText() == null
                        || edit_vaildcode.getText().toString().equals("")) {
                    ToastUtil.showtomain(LoginActivity.this, getString(R.string.input_verification_code));
                    return;
                }
                progress = new ProgressDialog(this);
                progress.setMessage(getString(R.string.logining));
                progress.show();
                //   handler.sendEmptyMessage(1);
                ThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(edit_vaildcode.getText())
                                || TextUtils.isEmpty(edit_vaildcode.getText())) {
                        }
                        progress.show();

                        // 设置参数
                        Map<String, String> parmas = new HashMap<String, String>();
                        parmas.put("phone", edit_phonenum.getText().toString());
                        parmas.put("verify", edit_vaildcode.getText().toString());
                        // 开始请求
                        try {
                            NetUtil.getinstance().http("/sms/login/verify", parmas,
                                    new callback() {

                                        @Override
                                        public void success(JSONObject json) {

                                            try {
                                                if (json.getInt("ret") == 0) {

                                                    into(json);
                                                    huanxinlogin(edit_phonenum
                                                                    .getText().toString(),
                                                            edit_phonenum.getText()
                                                                    .toString());
                                                    if (timer != null) {
                                                        timer.cancel();
                                                    }
                                                    handler.sendEmptyMessage(1);
                                                } else if (json.getInt("ret") == 1) {
                                                    progress.dismiss();
                                                    HandlerUtil
                                                            .sendmsg(
                                                                    handler,
                                                                    getString(R.string.Captchaoverdue),
                                                                    2);
                                                } else if (json.getInt("ret") == 2) {
                                                    progress.dismiss();
                                                    HandlerUtil
                                                            .sendmsg(
                                                                    handler,
                                                                    getString(R.string.vaild_error),
                                                                    2);
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                        }

                                        @Override
                                        public void error(String errorresult) {
                                            if (progress != null) {
                                                progress.dismiss();
                                            }
                                            if (timer != null) {
                                                timer.cancel();
                                            }
                                            HandlerUtil.sendmsg(handler,
                                                    errorresult, 6);
                                        }
                                    }, LoginActivity.this);
                        } catch (SocketTimeoutException e) {
                            if (progress != null) {
                                progress.dismiss();
                            }
                            HandlerUtil.sendmsg(handler,
                                    getString(R.string.time_out), 6);
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case R.id.getvaild:
                // 账号过滤
                if (TextUtils.isEmpty(edit_phonenum.getText())
                        || edit_phonenum.getText().equals("")) {
                    ToastUtil.showtomain(this, getString(R.string.input_phone_number));
                    return;
                }
                if (edit_phonenum.getText().toString().length() < 11) {
                    if (edit_phonenum.getText().toString().length() != 8) {
                        ToastUtil.showtomain(this, getString(R.string.phone_number_length_error));
                        return;
                    }
                }
              /*  if (!edit_phonenum.getText().toString().substring(0, 1).equals("1")) {
                    ToastUtil.showtomain(this, getString(R.string.input_correct_phone_number_please));
                    return;
                }*/
                getvaild.setEnabled(false);
                getvaild.getBackground().setAlpha(80);
                stateCode = tv_state_num.getText().toString().trim();
                if (stateCode != null) {
                    stateCode = stateCode.substring(1);
                }
                ThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("country",stateCode);
                        params.put("phone", edit_phonenum.getText().toString());
                        try {
                            NetUtil.getinstance().http("/sms/send/verify", params,
                                    new callback() {

                                        // 请求成功
                                        @Override
                                        public void success(JSONObject json) {
                                            try {
                                                int ret = json.getInt("ret");
                                                if (ret == 0) {
                                                    handler.sendEmptyMessage(3);
                                                } else if (ret == 1) {
                                                    handler.sendEmptyMessage(5);
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        // 请求失败回掉
                                        @Override
                                        public void error(String errorresult) {
                                            HandlerUtil.sendmsg(handler,
                                                    errorresult, 6);
                                        }
                                    }, LoginActivity.this);
                        } catch (SocketTimeoutException e) {
                            HandlerUtil.sendmsg(handler, getString(R.string.request_timeout), 6);
                            e.printStackTrace();
                        }
                    }
                });

                break;
            case R.id.ll_state:
                Intent intent = new Intent(this, StateActivity.class);
                startActivityForResult(intent, REQUEST_STATE_CODE);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1) {
            String state = data.getStringExtra("state");
            String state_code = data.getStringExtra("state_code");
            tv_state.setText(state);
            tv_state_num.setText(state_code);
            getSharedPreferences("Receipt", MODE_PRIVATE).edit().putString("state", state).apply();
            getSharedPreferences("Receipt", MODE_PRIVATE).edit().putString("state_code", state_code).apply();
        }
    }

    Timer timer = null;

    private void huanxinlogin(final String currentUsername,
                              final String currentPassword) {
        if (!CommonUtils.isNetWorkConnected(this)) {
            Toast.makeText(this, R.string.network_isnot_available,
                    Toast.LENGTH_SHORT).show();
            progress.dismiss();
            return;
        }
        if (TextUtils.isEmpty(currentUsername)) {
            Toast.makeText(this, R.string.User_name_cannot_be_empty,
                    Toast.LENGTH_SHORT).show();
            progress.dismiss();
            return;
        }
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(this, R.string.Password_cannot_be_empty,
                    Toast.LENGTH_SHORT).show();
            progress.dismiss();
            return;
        }
        // 调用sdk登陆方法登陆聊天服务器
        if (getSharedPreferences("huanxin", MODE_PRIVATE).getString("username",
                null) == null) {
            try {
                EMChatManager.getInstance().createAccountOnServer(
                        currentUsername, currentUsername);
            } catch (EaseMobException e1) {
                e1.printStackTrace();

            }
            getSharedPreferences("huanxin", MODE_PRIVATE).edit()
                    .putString("username", currentUsername)
                    .putString("password", currentPassword).commit();
        }
        Log.i("LoginActivity",  getSharedPreferences("huanxin", MODE_PRIVATE).getString(
                "username", null));
        Log.i("LoginActivity",  getSharedPreferences("huanxin", MODE_PRIVATE).getString(
        		"password", null));
        EMChatManager.getInstance().login(
                getSharedPreferences("huanxin", MODE_PRIVATE).getString(
                        "username", null),
                getSharedPreferences("huanxin", MODE_PRIVATE).getString(
                        "password", null), new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        Log.i("LoginActivity", "onSuccess");
                        // 登陆成功，保存用户名密码
                        DemoApplication.getInstance().setUserName(
                                currentUsername);
                        DemoApplication.getInstance().setPassword(
                                currentPassword);

                        try {
                            // ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
                            // ** manually load all local groups and
                            EMGroupManager.getInstance().loadAllGroups();
                            EMChatManager.getInstance().loadAllConversations();
                            // 处理好友和群组
                            initializeContacts();
                        } catch (Exception e) {
                            e.printStackTrace();
                            // 取好友或者群聊失败，不让进入主页面
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    DemoHXSDKHelper.getInstance().logout(true,
                                            null);
                                    Toast.makeText(getApplicationContext(),
                                            R.string.login_failure_failed,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }
                        // 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
                        boolean updatenick = EMChatManager.getInstance()
                                .updateCurrentUserNick(
                                        DemoApplication.currentUserNick.trim());
                        if (!updatenick) {
                            Log.e("LoginActivity",
                                    "update current user nick fail");
                        }
                        progress.dismiss();
                        if (timer != null) {
                            timer.cancel();
                        }

                        // 进入主页面
                        handler.sendEmptyMessage(4);
                    }

                    @Override
                    public void onProgress(int progress, String status) {
                    }

                    @Override
                    public void onError(final int code, final String message) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progress.dismiss();
                                if (timer != null) {
                                    timer.cancel();
                                }
                                handler.sendEmptyMessage(1);
                                Toast.makeText(
                                        getApplicationContext(),
                                        getString(R.string.Login_failed)
                                                + message, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });

                    }
                });
    }

    private void initializeContacts() {
        Map<String, User> userlist = new HashMap<String, User>();
        // 添加user"申请与通知"
        User newFriends = new User();
        newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
        String strChat = getResources().getString(
                R.string.Application_and_notify);
        newFriends.setNick(strChat);

        userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
        // 添加"群聊"
        User groupUser = new User();
        String strGroup = getResources().getString(R.string.group_chat);
        groupUser.setUsername(Constant.GROUP_USERNAME);
        groupUser.setNick(strGroup);
        groupUser.setHeader("");
        userlist.put(Constant.GROUP_USERNAME, groupUser);

        // 添加"Robot"
        User robotUser = new User();
        String strRobot = getResources().getString(R.string.robot_chat);
        robotUser.setUsername(Constant.CHAT_ROBOT);
        robotUser.setNick(strRobot);
        robotUser.setHeader("");
        userlist.put(Constant.CHAT_ROBOT, robotUser);

        // 存入内存
        ((DemoHXSDKHelper) HXSDKHelper.getInstance()).setContactList(userlist);
        // 存入db
        UserDao dao = new UserDao(LoginActivity.this);
        List<User> users = new ArrayList<User>(userlist.values());
        dao.saveContactList(users);
    }

    // 用户信息存入db
    public void into(JSONObject json) {
        try {
            getSharedPreferences("userinfo", MODE_PRIVATE)
                    .edit()
                    .putInt("id", json.getInt("id"))
                    .putString("session", json.getString("session"))
                    .putString("phonenumber",
                            edit_phonenum.getText().toString().trim()).commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    int flag = 0;

    // 设置按钮内容
    @Override
    public void onHandlerMessage(Message msg) {
        if (msg.what == 0) {
            getvaild.setText(getString(R.string.only) + index + getString(R.string.second));
        } else if (msg.what == 1) {
            getvaild.getBackground().setAlpha(255);
            getvaild.setText(R.string.re_get);
            getvaild.setEnabled(true);
            index = 60;
        } else if (msg.what == 2) {
            ToastUtil.showtomain(LoginActivity.this,
                    msg.getData().getString("result"));
            //	getvaild.getBackground().setAlpha(255);
            //	getvaild.setEnabled(true);
        } else if (msg.what == 3) {
            edit_vaildcode.requestFocus();
            // 开启timer任务
            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    if (index == 0) {
                        handler.sendEmptyMessage(1);
                        if (timer != null) {
                            timer.cancel();
                        }

                        return;
                    }
                    handler.sendEmptyMessage(0);
                    index--;
                }
            }, new Date(), 1000);

        } else if (msg.what == 4) {
            getvaild.setEnabled(true);
            index = 60;
            getvaild.getBackground().setAlpha(255);
            getSharedPreferences("login", MODE_PRIVATE)
                    .edit()
                    .putString("login_phonenumber",
                            edit_phonenum.getText().toString()).commit();
            StartUtil.startintent(LoginActivity.this, ConnectActivity.class,
                    "finish");
        } else if (msg.what == 5) {
            getvaild.getBackground().setAlpha(255);
            getvaild.setEnabled(true);
            ToastUtil.showtomain(LoginActivity.this, getString(R.string.so_fast_wait));
        } else if (msg.what == 6) {
            if (timer != null) {
                timer.cancel();
            }
            index = 60;
            ToastUtil.showtomain(LoginActivity.this,
                    msg.getData().getString("result"));
            getvaild.getBackground().setAlpha(255);
            getvaild.setEnabled(true);
        }
        super.onHandlerMessage(msg);
    }

    // 倒计时时间
    int index = 60;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    String mode = "";

    public void toggle_mode() {

        if (getSharedPreferences("net_state", MODE_PRIVATE).getString("state",
                null).equals("official")) {
            mode = getString(R.string.test_server);
            Constants.address = getString(R.string.test_url);
            Constants.ip = getString(R.string.test_ip);
            getSharedPreferences("net_state", MODE_PRIVATE).edit()
                    .putString("state", "test").commit();
        } else {
            mode = getString(R.string.official_server);
            Constants.address = getString(R.string.url);
            Constants.ip = getString(R.string.ip);
            getSharedPreferences("net_state", MODE_PRIVATE).edit()
                    .putString("state", "official").commit();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.showtomain(LoginActivity.this, getString(R.string.already_switch_to) + mode);
            }
        });
    }

    int index_mode = 0;
    long starttime = 0;

    public void togglemode(View view) {
        if (starttime == 0) {
            starttime = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - starttime < 500) {
            starttime = System.currentTimeMillis();
            index_mode++;
        } else {
            index_mode = 0;
            starttime = 0;
        }
        if (index_mode == 10) {
            toggle_mode();
            index_mode = 0;
            starttime = 0;
        }
    }

    @Override
    public void initlayout(OnRefreshListener onRefreshListener) {
        setContentView(R.layout.activity_login);
        SharedPreferences sharedPreferences = getSharedPreferences("userinfo",
                MODE_PRIVATE);
        int id = sharedPreferences.getInt("id", 0);
        // 如果本地存在记录则自动跳转
        if (id != 0 && DemoHXSDKHelper.getInstance().isLogined()) {
            StartUtil.startintent(this, ConnectActivity.class, "finish");
            return;
        }
        edit_phonenum = (EditText) findViewById(R.id.edit_phonenumber);
        edit_vaildcode = (EditText) findViewById(R.id.edit_vaildcode);
        if (getSharedPreferences("login", MODE_PRIVATE).getString(
                "login_phonenumber", null) != null) {
            edit_phonenum.setText(getSharedPreferences("login", MODE_PRIVATE)
                    .getString("login_phonenumber", ""));
        }
        login = (Button) findViewById(R.id.btn_login);
        login.setOnClickListener(this);
        getvaild = (Button) findViewById(R.id.getvaild);
        getvaild.setOnClickListener(this);
        ll_state = (LinearLayout) findViewById(R.id.ll_state);
        ll_state.setOnClickListener(this);
        tv_state = (TextView) findViewById(R.id.tv_state);
        tv_state_num = (TextView) findViewById(R.id.tv_state_num);
        String state = getSharedPreferences("Receipt", MODE_PRIVATE).getString(
                "state", null);
        String state_code = getSharedPreferences("Receipt", MODE_PRIVATE).getString(
                "state_code", null);
        if (state != null) {
            tv_state.setText(state);
        }
        if (state_code != null) {
            tv_state_num.setText(state_code);
        }

    }
}
