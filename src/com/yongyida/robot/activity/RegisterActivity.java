package com.yongyida.robot.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;
import com.yongyida.robot.R;
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
 * Created by Administrator on 2016/4/19 0019.
 */
public class RegisterActivity extends Activity implements OnClickListener{

    private static final String TAG = "RegisterActivity";
    private EditText mAccountET;
    private EditText mPasswordET;
    private EditText mPasswordConfirmET;
    private EditText mVerifyCodeET;
    private ImageView mVerifyCodeIV;
    private Button mConfirmBT;
    private Button mCancelBT;
    private String mVerifyCodeKey;
    private ProgressDialog mProgress;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Bitmap bitmap = msg.getData().getParcelable("bitmap");
                    if (bitmap == null) {
                        whetherGetVerifyCode(false);
                    } else {
                        whetherGetVerifyCode(true);
                        mVerifyCodeIV.setImageBitmap(bitmap);
                    }
                    enablleClickToGetVerifyCode(true);
                    mNotGetVerifyTV.setText(R.string.refresh);
                    break;
                case 2:
                    ToastUtil.showtomain(RegisterActivity.this,
                            msg.getData().getString("result"));
                    break;
                case 3:
                    whetherGetVerifyCode(false);
                    enablleClickToGetVerifyCode(true);
                    mNotGetVerifyTV.setText(R.string.refresh);
                    break;
            }
        }
    };
    private TextView mNotGetVerifyTV;

    /**
     *   防止用户在一次请求没有完成的时候多次点击 造成输入的验证码过期等问题
     * @param flag 是否能点击获取验证码  true 是可以
     */
    private void enablleClickToGetVerifyCode(boolean flag){
        if (flag) {
            mVerifyCodeIV.setEnabled(true);
            mNotGetVerifyTV.setEnabled(true);
        } else {
            mVerifyCodeIV.setEnabled(false);
            mNotGetVerifyTV.setEnabled(false);
        }
    }

    /**
     *
     * @param flag  获取验证码是否成功  true 是成功
     */
    private void whetherGetVerifyCode(boolean flag){
        if (flag) {
            mVerifyCodeIV.setVisibility(View.VISIBLE);
            mNotGetVerifyTV.setVisibility(View.GONE);
        } else {
            mVerifyCodeIV.setVisibility(View.GONE);
            mNotGetVerifyTV.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
    }

    private void initView() {
        mAccountET = (EditText) findViewById(R.id.et_account);
        mPasswordET = (EditText) findViewById(R.id.et_pwd);
        mPasswordConfirmET = (EditText) findViewById(R.id.et_pwd_confirm);
        mVerifyCodeET = (EditText) findViewById(R.id.et_verify_code);
        mVerifyCodeIV = (ImageView) findViewById(R.id.iv_verify_code);
        mVerifyCodeIV.setOnClickListener(this);
        mConfirmBT = (Button) findViewById(R.id.bt_confirm);
        mConfirmBT.setOnClickListener(this);
        mCancelBT = (Button) findViewById(R.id.bt_cancel);
        mCancelBT.setOnClickListener(this);
        mNotGetVerifyTV = (TextView) findViewById(R.id.tv_not_get);
        mNotGetVerifyTV.setOnClickListener(this);
        getPicture();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_confirm:
                inputVerifyAndCommit();
                break;
            case R.id.bt_cancel:
                finish();
                break;
            case R.id.iv_verify_code:
                getPicture();
                break;
            case R.id.tv_not_get:
                getPicture();
                break;
        }
    }

    private void getPicture() {
        enablleClickToGetVerifyCode(false);
        mNotGetVerifyTV.setText(R.string.refresh_ing);
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
                    mHandler.sendEmptyMessage(3);
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
            ToastUtil.showtomain(RegisterActivity.this,getString(R.string.network_anomalies));
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

    private void inputVerifyAndCommit(){
        String account = mAccountET.getText().toString();
        String password = mPasswordET.getText().toString();
        String passwordConfirm = mPasswordConfirmET.getText().toString();
        String verifyCode = mVerifyCodeET.getText().toString();
        if (TextUtils.isEmpty(account)){
            ToastUtil.showtomain(RegisterActivity.this, getString(R.string.input_account));
            return;
        }
        if (TextUtils.isEmpty(password)){
            ToastUtil.showtomain(RegisterActivity.this, getString(R.string.input_pwd));
            return;
        }
        if (TextUtils.isEmpty(verifyCode)){
            ToastUtil.showtomain(RegisterActivity.this, getString(R.string.input_verify));
            return;
        }
        if (!password.equals(passwordConfirm)) {
            ToastUtil.showtomain(RegisterActivity.this, getString(R.string.pwd_not_consistent));
            return;
        }
        if (!Utils.isAccount(account)) {
            Toast.makeText(RegisterActivity.this, R.string.account_hint, Toast.LENGTH_LONG).show();
            return;
        }
        if (!Utils.isPassword(password)) {
            Toast.makeText(RegisterActivity.this, R.string.pwd_hint, Toast.LENGTH_LONG).show();
            return;
        }
        register(account, password, verifyCode);
    }

    public void into(JSONObject json) {
        try {
            getSharedPreferences("userinfo", MODE_PRIVATE)
                    .edit()
                    .putInt("id", json.getInt("id"))
                    .putString("session", json.getString("session"))
                    .putString("account_name", mAccountET.getText().toString().trim())
                    .commit();
            getSharedPreferences("login", MODE_PRIVATE)
                    .edit()
                    .putInt(Constants.LOGIN_METHOD, Constants.ACCOUNT_LOGIN)
                    .commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void register(final String account, final String password, final String verifyCode){
        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.register_ing));
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
                    NetUtil.getinstance().http("/account/register", params, new NetUtil.callback() {
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
                                        if (!Utils.isServiceRunning(RegisterActivity.this, SocketService.class.getCanonicalName())) {
                                            Utils.startSocketService(RegisterActivity.this);
                                        }
                                        StartUtil.startintent(RegisterActivity.this, ConnectActivity.class,
                                                "finish");
                                        break;
                                    case -1:
                                        HandlerUtil.sendmsg(mHandler, getString(R.string.argu_null), 2);
                                        break;
                                    case 1:
                                        HandlerUtil.sendmsg(mHandler, getString(R.string.verify_expire), 2);
                                        getPicture();
                                        break;
                                    case 2:
                                        HandlerUtil.sendmsg(mHandler, getString(R.string.account_exist), 2);
                                        break;
                                    case 3:
                                        HandlerUtil.sendmsg(mHandler, getString(R.string.account_format_error), 2);
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
                    }, RegisterActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void huanxinlogin(final String currentUsername,
                              final String currentPassword) {
        if (!CommonUtils.isNetWorkConnected(this)) {
            Toast.makeText(this, R.string.network_isnot_available,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(currentUsername)) {
            Toast.makeText(this, R.string.User_name_cannot_be_empty,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(this, R.string.Password_cannot_be_empty,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // 调用sdk登陆方法登陆聊天服务器
        if (this.getSharedPreferences("huanxin", this.MODE_PRIVATE).getString("username",
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

}
