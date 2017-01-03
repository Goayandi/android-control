package com.yongyida.robot.activity;

import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.fragment.BaseFragment;
import com.yongyida.robot.fragment.RegisterLoginFragment;
import com.yongyida.robot.fragment.SMSLoginFragment;
import com.yongyida.robot.service.SocketService;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/4/18 0018.
 */
public class NewLoginActivity extends OriginalActivity implements OnCheckedChangeListener{
    private static final String TAG = "NewLoginActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private List<BaseFragment> mFragments;
    private int index_mode = 0; //用于点击切换测试版本
    private long starttime = 0; //用于点击切换测试版本
    private String mode = "";
    private RadioButton mRegisterRB;
    private RadioButton mSmsRB;
    List<RadioButton> mRBList; //用于存放RadioButton 方便控制RadioButton的颜色
    private RadioGroup mRadioGroup;
    private TextView mLoginTitleTV;
    private Spinner mSpinner;
    private boolean mSpinnerFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_new_login);
        if (Utils.isServiceRunning(this, SocketService.class.getCanonicalName())) {
            Utils.stopSocketService(this);
        }
        initView();
        hideOtherLoginMethod(1);
    }

    /**
     *
     * @param i  显示第几种登录方式  -1是都显示 1是短信  2是注册
     */
    private void hideOtherLoginMethod(int i) {
        if(i != -1) {
            mRadioGroup.setVisibility(View.GONE);
            mLoginTitleTV.setVisibility(View.VISIBLE);
            initFragment(i);
        } else {
            initFragment(-1);
        }
    }

    private void initView() {
        mLoginTitleTV = (TextView) findViewById(R.id.login_title);
        mRadioGroup = (RadioGroup) findViewById(R.id.rg);
        mRadioGroup.setOnCheckedChangeListener(this);
        mSmsRB = (RadioButton) findViewById(R.id.rb_sms);
        mRegisterRB = (RadioButton) findViewById(R.id.rb_register);
        mSpinner = (Spinner) findViewById(R.id.spinner);
        String[] mItems = {
                getString(R.string.server),
                getString(R.string.official_server),
                getString(R.string.test_server),
                getString(R.string.test_server1)
        };
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner .setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mSpinnerFirst) {
                    mSpinnerFirst = false;
                    return;
                }
                if (position == 0) {
                    return;
                }
                if (position == 1) {
                    mode = getString(R.string.official_server);
                    Utils.switchServer(Utils.CN);
                    getSharedPreferences("net_state", MODE_PRIVATE).edit()
                            .putString("state", "official").commit();
                } else if (position == 2) {
                    mode = getString(R.string.test_server);
                    Utils.switchServer(Utils.TEST);
                    getSharedPreferences("net_state", MODE_PRIVATE).edit()
                            .putString("state", "test").commit();
                } else if (position == 3) {
                    mode = getString(R.string.test_server1);
                    Utils.switchServer(Utils.TEST1);
                    getSharedPreferences("net_state", MODE_PRIVATE).edit()
                            .putString("state", "test1").commit();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showtomain(NewLoginActivity.this, getString(R.string.already_switch_to) + mode);
                        mSpinner.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSpinner.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.bt_switch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglemode();
            }
        });
        mRBList = new ArrayList<RadioButton>();
        mRBList.add(mSmsRB);
        mRBList.add(mRegisterRB);
        mFragments = new ArrayList<BaseFragment>();
    }

    public void togglemode() {
        if (starttime == 0) {
            starttime = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - starttime < 500) {
            starttime = System.currentTimeMillis();
            Log.i(TAG, "index_mode:" + index_mode);
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

    public void toggle_mode() {
        mSpinner.setVisibility(View.VISIBLE);
    }

    /**
     *
     * @param i 第一次显示的是第几钟登录方式  1是短信 2是注册 -1是都显示
     */
    private void initFragment(int i) {

        SharedPreferences sharedPreferences = getSharedPreferences("login",
                MODE_PRIVATE);
        int method;
        if (i == -1) {
            method = sharedPreferences.getInt(Constants.LOGIN_METHOD, -1); //登录方式 1是短信，2是账号
        } else {
            method = i;
        }
        if (method == Constants.ACCOUNT_LOGIN) {
            switchFragment(Constants.ACCOUNT_LOGIN);
            setRadioButtonColor(Constants.ACCOUNT_LOGIN);
        } else {
            switchFragment(Constants.SMS_LOGIN);
            setRadioButtonColor(Constants.SMS_LOGIN);
        }
    }

    /**
     * 切换fragment
     * @param position 切换到第几个fragment
     */
    public void switchFragment(int position){

        switch (position) {
            case 1:
                BaseFragment fragment1 = (BaseFragment) getFragmentManager().findFragmentByTag(SMSLoginFragment.class.getSimpleName());
                if(fragment1 != null){
                    showFragment(fragment1);
                }else{
                    fragment1 = new SMSLoginFragment();
                    addFragment(fragment1);
                    showFragment(fragment1);
                }
                break;
            case 2:
                BaseFragment fragment2 = (BaseFragment) getFragmentManager().findFragmentByTag(RegisterLoginFragment.class.getSimpleName());
                if(fragment2 != null){
                    showFragment(fragment2);
                }else{
                    fragment2 = new RegisterLoginFragment();
                    addFragment(fragment2);
                    showFragment(fragment2);
                }
                break;

            default:
                break;
        }
        setRadioButtonColor(position);
    }


    /**
     * 设置选中radioButton的颜色
     * @param position
     */
    private void setRadioButtonColor(int position){
        for (int i = 0; i < mRBList.size(); i++) {
            if (i == position - 1) {
                mRBList.get(i).setTextColor(getResources().getColor(R.color.red));
            } else {
                mRBList.get(i).setTextColor(getResources().getColor(R.color.white));
            }
        }
    }

    /**
     *
     * @param fragment 需要显示的fragment
     */
    public void showFragment(BaseFragment fragment){
        if(mFragments == null){
            return;
        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        for (BaseFragment f : mFragments) {
            if(f == fragment){
                ft.show(f);
            } else {
                ft.hide(f);
            }
        }
        ft.commit();
    }

    /**
     * 添加fragment到mFragment并添加到FragmentManager
     * @param fragment
     */
    public void addFragment(BaseFragment fragment){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.fl, fragment, fragment.getClass().getSimpleName()).commit();
        mFragments.add(fragment);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_sms:
                switchFragment(Constants.SMS_LOGIN);
                break;
            case R.id.rb_register:
                switchFragment(Constants.ACCOUNT_LOGIN);
                break;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
