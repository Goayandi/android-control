package com.yongyida.robot.fragment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;
import com.yongyida.robot.R;
import com.yongyida.robot.activity.ConnectActivity;
import com.yongyida.robot.activity.StateActivity;
import com.yongyida.robot.huanxin.CommonUtils;
import com.yongyida.robot.huanxin.DemoApplication;
import com.yongyida.robot.service.SocketService;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.HandlerUtil;
import com.yongyida.robot.utils.NetUtil;
import com.yongyida.robot.utils.StartUtil;
import com.yongyida.robot.utils.ThreadPool;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/4/18 0018.
 */
public class SMSLoginFragment extends BaseFragment implements View.OnClickListener{
    private static final int REQUEST_STATE_CODE = 1;
    private EditText edit_phonenum;
    private EditText edit_vaildcode;
    private Button login;
    private Button getvaild;
    private LinearLayout ll_state;
    private TextView tv_state;
    private TextView tv_state_num;
    private String stateCode;
    private ProgressDialog progress = null;
    private int index = 60; // 倒计时时间
    private Timer timer = null;
    private Handler handler = new Handler() {
        public void dispatchMessage(Message msg) {
            if (msg.what == 0) {
                getvaild.setText(getString(R.string.only) + index + getString(R.string.second));
            } else if (msg.what == 1) {
                getvaild.getBackground().setAlpha(255);
                getvaild.setText(R.string.re_get);
                getvaild.setEnabled(true);
                index = 60;
            } else if (msg.what == 2) {
                ToastUtil.showtomain(getActivity(),
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
                if (getvaild != null) {
                    getvaild.setEnabled(true);
                    index = 60;
                    getvaild.getBackground().setAlpha(255);
                    getActivity().getSharedPreferences("login", getActivity().MODE_PRIVATE)
                            .edit()
                            .putString("login_phonenumber",
                                    edit_phonenum.getText().toString()).commit();
                }
                StartUtil.startintent(getActivity(), ConnectActivity.class,
                        "finish");
            } else if (msg.what == 5) {
                getvaild.getBackground().setAlpha(255);
                getvaild.setEnabled(true);
                ToastUtil.showtomain(getActivity(), getString(R.string.so_fast_wait));
            } else if (msg.what == 6) {
                if (timer != null) {
                    timer.cancel();
                }
                index = 60;
                ToastUtil.showtomain(getActivity(),
                        msg.getData().getString("result"));
                getvaild.getBackground().setAlpha(255);
                getvaild.setEnabled(true);
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.from(getActivity()).inflate(R.layout.fragment_sms_login, null);
        if (isLogined()){
            return v;
        }
        BroadcastReceiverRegister.reg(getActivity(),
                new String[]{Constants.LOGIN}, mBRLogin);
        edit_phonenum = (EditText) v.findViewById(R.id.edit_phonenumber);
        edit_vaildcode = (EditText) v.findViewById(R.id.edit_vaildcode);
        if (getActivity().getSharedPreferences("login", getActivity().MODE_PRIVATE).getString(
                "login_phonenumber", null) != null) {
            edit_phonenum.setText(getActivity().getSharedPreferences("login", getActivity().MODE_PRIVATE)
                    .getString("login_phonenumber", ""));
        }
        login = (Button) v.findViewById(R.id.btn_login);
        login.setOnClickListener(this);
        getvaild = (Button) v.findViewById(R.id.getvaild);
        getvaild.setOnClickListener(this);
        ll_state = (LinearLayout) v.findViewById(R.id.ll_state);
        ll_state.setOnClickListener(this);
        tv_state = (TextView) v.findViewById(R.id.tv_state);
        tv_state_num = (TextView) v.findViewById(R.id.tv_state_num);
        String state = getActivity().getSharedPreferences("Receipt", getActivity().MODE_PRIVATE).getString(
                "state", null);
        String state_code = getActivity().getSharedPreferences("Receipt", getActivity().MODE_PRIVATE).getString(
                "state_code", null);
        if (state != null) {
            tv_state.setText(state);
        }
        if (state_code != null) {
            tv_state_num.setText(state_code);
        }

        return v;
    }

    private BroadcastReceiver mBRLogin = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int ret = intent.getIntExtra("ret", -1);
            switch (ret) {
                case -1:  //参数错误
                    if (progress != null) {
                        progress.dismiss();
                    }
                    HandlerUtil.sendmsg(handler,
                            getString(R.string.Captchaoverdue),
                            2);
                    break;
                case 0:   //登录成功
                case 1:   //已登录过
                    if (progress != null) {
                        progress.dismiss();
                    }
                    if (timer != null) {
                        timer.cancel();
                    }

                    // 进入主页面
                    handler.sendEmptyMessage(4);
                    break;
            }
        }
    };

    private void huanxinlogin(final String currentUsername,
                              final String currentPassword) {
        if (!CommonUtils.isNetWorkConnected(getActivity())) {
            Toast.makeText(getActivity(), R.string.network_isnot_available,
                    Toast.LENGTH_SHORT).show();
            progress.dismiss();
            return;
        }
        if (TextUtils.isEmpty(currentUsername)) {
            Toast.makeText(getActivity(), R.string.User_name_cannot_be_empty,
                    Toast.LENGTH_SHORT).show();
            progress.dismiss();
            return;
        }
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(getActivity(), R.string.Password_cannot_be_empty,
                    Toast.LENGTH_SHORT).show();
            progress.dismiss();
            return;
        }
        // 调用sdk登陆方法登陆聊天服务器
        if (getActivity().getSharedPreferences("huanxin", getActivity().MODE_PRIVATE).getString("username",
                null) == null) {
            try {
                EMChatManager.getInstance().createAccountOnServer(
                        currentUsername, currentUsername);
            } catch (EaseMobException e1) {
                e1.printStackTrace();

            }
            getActivity().getSharedPreferences("huanxin", getActivity().MODE_PRIVATE).edit()
                    .putString("username", currentUsername)
                    .putString("password", currentPassword).commit();
        }

        EMChatManager.getInstance().login(
                getActivity().getSharedPreferences("huanxin", getActivity().MODE_PRIVATE).getString(
                        "username", null),
                getActivity().getSharedPreferences("huanxin", getActivity().MODE_PRIVATE).getString(
                        "password", null), new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        Log.i("LoginActivity", "onSuccess");
                        // 登陆成功，保存用户名密码
                        DemoApplication.getInstance().setUserName(
                                currentUsername);
                        DemoApplication.getInstance().setPassword(
                                currentPassword);

                        // 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
                        boolean updatenick = EMChatManager.getInstance()
                                .updateCurrentUserNick(
                                        DemoApplication.currentUserNick.trim());
                        if (!updatenick) {
                            Log.e("LoginActivity",
                                    "update current user nick fail");
                        }

                    }

                    @Override
                    public void onProgress(int progress, String status) {
                    }

                    @Override
                    public void onError(final int code, final String message) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                progress.dismiss();
                                if (timer != null) {
                                    timer.cancel();
                                }
                                handler.sendEmptyMessage(1);
                                Toast.makeText(
                                        getActivity().getApplicationContext(),
                                        getString(R.string.Login_failed)
                                                + message, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });

                    }
                });
    }


    public void into(JSONObject json) {
        try {
            getActivity().getSharedPreferences("userinfo", getActivity().MODE_PRIVATE)
                    .edit()
                    .putInt("id", json.getInt("id"))
                    .putString("session", json.getString("session"))
                    .putString("phonenumber",
                            edit_phonenum.getText().toString().trim())
                    .putInt(Constants.LOGIN_METHOD, Constants.SMS_LOGIN)
                    .commit();
            getActivity().getSharedPreferences("login", getActivity().MODE_PRIVATE)
                    .edit()
                    .putInt(Constants.LOGIN_METHOD, Constants.SMS_LOGIN)
                    .commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                // 手机号验证
                if (edit_phonenum.getText() == null
                        || edit_phonenum.getText().toString().equals("")) {
                    ToastUtil.showtomain(getActivity(), getString(R.string.input_phone_number));
                    return;
                }
                if (edit_vaildcode.getText() == null
                        || edit_vaildcode.getText().toString().equals("")) {
                    ToastUtil.showtomain(getActivity(), getString(R.string.input_verification_code));
                    return;
                }
                progress = new ProgressDialog(getActivity());
                progress.setMessage(getString(R.string.logining));
                progress.setCancelable(false);
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
                                    new NetUtil.callback() {

                                        @Override
                                        public void success(JSONObject json) {

                                            try {
                                                if (json.getInt("ret") == 0) {

                                                    into(json);
                                                    huanxinlogin(edit_phonenum
                                                                    .getText().toString(),
                                                            edit_phonenum.getText()
                                                                    .toString());
                                                    Constants.isUserClose = false;
                                                    if (!Utils.isServiceRunning(getActivity(), SocketService.class.getSimpleName())) {
                                                        getActivity().startService(new Intent(getActivity(), SocketService.class));
                                                    }

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
                                    }, getActivity());
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
                    ToastUtil.showtomain(getActivity(), getString(R.string.input_phone_number));
                    return;
                }
                if (edit_phonenum.getText().toString().length() < 11) {
                    if (edit_phonenum.getText().toString().length() != 8) {
                        ToastUtil.showtomain(getActivity(), getString(R.string.phone_number_length_error));
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
                                    new NetUtil.callback() {

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
                                    }, getActivity());
                        } catch (SocketTimeoutException e) {
                            HandlerUtil.sendmsg(handler, getString(R.string.request_timeout), 6);
                            e.printStackTrace();
                        }
                    }
                });

                break;
            case R.id.ll_state:
                Intent intent = new Intent(getActivity(), StateActivity.class);
                startActivityForResult(intent, REQUEST_STATE_CODE);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mBRLogin != null) {
                getActivity().unregisterReceiver(mBRLogin);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }
}
