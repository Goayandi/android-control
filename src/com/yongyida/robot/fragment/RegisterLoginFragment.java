package com.yongyida.robot.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;
import com.yongyida.robot.R;
import com.yongyida.robot.activity.ConnectActivity;
import com.yongyida.robot.activity.RegisterActivity;
import com.yongyida.robot.huanxin.CommonUtils;
import com.yongyida.robot.huanxin.DemoApplication;
import com.yongyida.robot.service.SocketService;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.HandlerUtil;
import com.yongyida.robot.utils.NetUtil;
import com.yongyida.robot.utils.StartUtil;
import com.yongyida.robot.utils.ThreadPool;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/18 0018.
 */
public class RegisterLoginFragment extends BaseFragment implements View.OnClickListener {
    private static final int REQUEST_CODE = 1;
    private static String TAG = "RegisterLoginFragment";
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Bitmap bitmap = msg.getData().getParcelable("bitmap");
                    mImageCode.setImageBitmap(bitmap);
                    break;
                case 2:
                    ToastUtil.showtomain(getActivity(),
                            msg.getData().getString("result"));
                    break;
            }
        }
    };
    private String mVerifyCodeKey;
    private ImageView mImageCode;
    private Button mRegisterBT;
    private EditText mAccountET;
    private EditText mPasswordET;
    private EditText mVerifyCodeET;
    private Button mLoginBT;
    private ProgressDialog mProgress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.from(getActivity()).inflate(R.layout.fragment_register_login, null);
        if (isLogined()) {
            return v;
        }
        mRegisterBT = (Button) v.findViewById(R.id.bt_register);
        mRegisterBT.setOnClickListener(this);
        mLoginBT = (Button) v.findViewById(R.id.bt_login);
        mLoginBT.setOnClickListener(this);
        mAccountET = (EditText) v.findViewById(R.id.et_account);
        mPasswordET = (EditText) v.findViewById(R.id.et_pwd);
        mVerifyCodeET = (EditText) v.findViewById(R.id.et_verify_code);
        mImageCode = (ImageView) v.findViewById(R.id.iv);
        mImageCode.setOnClickListener(this);
        if (getActivity().getSharedPreferences("userinfo", getActivity().MODE_PRIVATE).getString(
                "account_name", null) != null) {
            mAccountET.setText(getActivity().getSharedPreferences("userinfo", getActivity().MODE_PRIVATE)
                    .getString("account_name", ""));
        }
        getPicture();
        return v;
    }

    private void getPicture() {
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = downloadPicture(Constants.address + "/image/code/phone");
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    msg.what = 1;
                    bundle.putParcelable("bitmap", bitmap);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Bitmap downloadPicture(String url) {
        HttpURLConnection connection = null;
        InputStream inputstream = null;
        try {
            URL uri = new URL(url);
            connection = (HttpURLConnection) uri
                        .openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(8000);
            inputstream = connection.getInputStream();
            mVerifyCodeKey = connection.getHeaderField("code_key");
            Bitmap bitmap = BitmapFactory.decodeStream(inputstream);
            return bitmap;
        } catch (Exception e) {
            ToastUtil.showtomain(getActivity(),getString(R.string.network_anomalies));
        } finally {
            if (connection != null) {
                connection.disconnect();
                try {
                    inputstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_register:
                StartUtil.startintentforresult(getActivity(), RegisterActivity.class, REQUEST_CODE);
                break;
            case R.id.iv:
                getPicture();
                break;
            case R.id.bt_login:
                inputVerifyAndCommit();
                break;
        }
    }


    private void inputVerifyAndCommit(){
        String account = mAccountET.getText().toString();
        String password = mPasswordET.getText().toString();
        String verifyCode = mVerifyCodeET.getText().toString();
        if (TextUtils.isEmpty(account)){
            ToastUtil.showtomain(getActivity(), "请输入账户");
            return;
        }
        if (TextUtils.isEmpty(password)){
            ToastUtil.showtomain(getActivity(), "请输入密码");
            return;
        }
        if (TextUtils.isEmpty(verifyCode)){
            ToastUtil.showtomain(getActivity(), "请输入验证码");
            return;
        }
        if (!Utils.isAccount(account)) {
            ToastUtil.showtomain(getActivity(), "账号请输入6-20位数字,字母或下划线(必须以字母开头)");
            return;
        }
        if (!Utils.isPassword(password)) {
            ToastUtil.showtomain(getActivity(), "密码请输入6-20位数字,字母或下划线");
            return;
        }

        login(account, password, verifyCode);
    }

    private void login(final String account, final String password, final String verifyCode) {
        mProgress = new ProgressDialog(getActivity());
        mProgress.setMessage("登录中");
        mProgress.setCancelable(false);
        mProgress.show();
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("account_name", account);
                    params.put("password", password);
                    params.put("code_key", mVerifyCodeKey);
                    params.put("code_value", verifyCode);
                    NetUtil.getinstance().http("/account/login", params, new NetUtil.callback() {
                        @Override
                        public void success(JSONObject json) {
                            if (mProgress != null) {
                                mProgress.dismiss();
                            }
                            try {
                                int ret = json.getInt("ret");
                                switch (ret) {
                                    case 0:
                                        into(json);
                                        huanxinlogin(json.getString("id"), json.getString("id"));
                                        Constants.isUserClose = false;
                                        if (!Utils.isServiceRunning(getActivity(), SocketService.class.getSimpleName())) {
                                            getActivity().startService(new Intent(getActivity(), SocketService.class));
                                        }
                                        StartUtil.startintent(getActivity(), ConnectActivity.class,
                                                "finish");
                                        break;
                                    case -1:
                                        HandlerUtil.sendmsg(mHandler, "传入参数为空", 2);
                                        break;
                                    case 1:
                                        HandlerUtil.sendmsg(mHandler, "验证码数值错误", 2);
                                        break;
                                    case 2:
                                        HandlerUtil.sendmsg(mHandler, "账号信息不存在", 2);
                                        break;
                                    case 3:
                                        HandlerUtil.sendmsg(mHandler, "密码错误", 2);
                                        break;
                                    case 4:
                                        HandlerUtil.sendmsg(mHandler, "密码未设置，请设置密码", 2);
                                        break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void error(String errorresult) {
                            if (mProgress != null) {
                                mProgress.dismiss();
                            }
                        }
                    }, getActivity());
                } catch (Exception e) {

                }
            }
        });
    }



    private void huanxinlogin(final String currentUsername,
                              final String currentPassword) {
        if (!CommonUtils.isNetWorkConnected(getActivity())) {
            Toast.makeText(getActivity(), R.string.network_isnot_available,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(currentUsername)) {
            Toast.makeText(getActivity(), R.string.User_name_cannot_be_empty,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(getActivity(), R.string.Password_cannot_be_empty,
                    Toast.LENGTH_SHORT).show();
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

                    }
                });
    }

    public void into(JSONObject json) {
        try {
            getActivity().getSharedPreferences("userinfo", getActivity().MODE_PRIVATE)
                    .edit()
                    .putInt("id", json.getInt("id"))
                    .putString("session", json.getString("session"))
                    .putString("account_name", mAccountET.getText().toString().trim())
                    .commit();
            getActivity().getSharedPreferences("login", getActivity().MODE_PRIVATE)
                    .edit()
                    .putInt(Constants.LOGIN_METHOD, Constants.ACCOUNT_LOGIN)
                    .commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            getActivity().finish();
        }
    }
}
