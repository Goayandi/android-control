package com.yongyida.robot.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.StartUtil;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.widget.ModifyRobotNameDialog;
import com.yongyida.robot.widget.SwitchButton;

public class SettingActivity<AndroidLearn> extends BaseActivity implements
		View.OnClickListener {

	private TextView exit;	
	private TextView userid;
	private TextView about;
	private SwitchButton wifi;
	private SwitchButton barrier;
	private Button back;
	private TextView edit;
	private TextView versionname;
	private RelativeLayout contact;
	private TextView upgrade;
	private String versionRobot;
	private Dialog mModifyNameDialog;
	private String mNewName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	protected void onStart() {
		BroadcastReceiverRegister.reg(this, new String[] { "flush" }, flush);
		super.onStart();
	}

	private OnCheckedChangeListener changeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton button, boolean flag) {
			getSharedPreferences("setting", MODE_PRIVATE).edit()
					.putBoolean("wificheck", flag).commit();
		}
	};

	private AlertDialog alert;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_exit:
			AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
			builder.setMessage(R.string.whether_exit);
			builder.setPositiveButton(getString(R.string.yes),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							fulllyExit();
						}
					});
			builder.setNegativeButton(getString(R.string.no),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							alert.dismiss();
						}
					});
			alert = builder.create();
			alert.show();

			break;
		case R.id.setting_back:
			this.onBackPressed();
//			Intent intent = new Intent( this,SettingActivity.class); //***为你想要转到的界面名
//			startActivity(intent);
			break;
		case R.id.about:
			StartUtil.startintent(this, AboutActivity.class, "no");
			break;
		case R.id.upgrade:
			Intent intent = new Intent(Constants.FOTA_UPDATE);
			intent.putExtra("robotVersion", versionRobot);
			intent.putExtra("newVersion", getSharedPreferences("Receipt", MODE_PRIVATE).getString("fota", ""));
			sendBroadcast(intent);
			break;
		case R.id.contact:
			final AlertDialog.Builder dialog = new AlertDialog.Builder(SettingActivity.this);
//          dialog.setIcon(R.drawable.ic_launcher);//窗口头图标  
            dialog.setTitle(R.string.reminder);//窗口名
            dialog.setMessage(getString(R.string.service_number) + getString(R.string.contact_number) + "  ");
            dialog.setPositiveButton(R.string.confirm,new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {  
                    // TODO Auto-generated method stub  
                	Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+ getString(R.string.contact_number)));
                    startActivity(intent); 
                }  
            });  
            dialog.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {  
                    // TODO Auto-generated method stub  
                      
                }  
            });
            dialog.show();
			break;
		case R.id.editname:
			mModifyNameDialog = new ModifyRobotNameDialog(SettingActivity.this, new ModifyRobotNameDialog.OnSaveListener() {
				@Override
				public void save(String name) {
					mNewName = name;
					getSharedPreferences("robotname", MODE_PRIVATE).edit().putString("name", name).commit();
					sendBroadcast(new Intent(Constants.Robot_Info_Update).putExtra("name", name));
					mModifyNameDialog.dismiss();
				}
			}, edit.getText().toString());
			mModifyNameDialog.show();
			break;
		}
	}

	BroadcastReceiver flush = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			int ret = intent.getIntExtra("ret", -1);
			if (ret == 0) {
				if (!TextUtils.isEmpty(mNewName)) {
					edit.setText(mNewName);
				}
				ToastUtil.showtomain(SettingActivity.this, getString(R.string.modify_success));
			} else {
				ToastUtil.showtomain(SettingActivity.this, getString(R.string.modify_fail));
			}
		}
	};

	@Override
	public void onBackPressed() {

		super.onBackPressed();
	}

	protected void onStop() {
		if (flush != null)
			unregisterReceiver(flush);
		super.onStop();
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void initlayout(OnRefreshListener onRefreshListener) {
		setContentView(R.layout.activity_setting);
		exit = (TextView) findViewById(R.id.setting_exit);
		exit.setOnClickListener(this);
		versionname = (TextView) findViewById(R.id.versionname);
		try {
			PackageManager packageManager = getPackageManager();
			PackageInfo info = packageManager.getPackageInfo(getPackageName(),
					0);
			versionname.setText(info.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		upgrade=(TextView)findViewById(R.id.upgrade);
		upgrade.setOnClickListener(this);
		contact=(RelativeLayout)findViewById(R.id.contact);
		contact.setOnClickListener(this);
		about = (TextView) findViewById(R.id.about);
		about.setOnClickListener(this);
		wifi = (SwitchButton) findViewById(R.id.wifisetting);
		wifi.setOnCheckedChangeListener(changeListener);
		barrier = (SwitchButton) findViewById(R.id.sb_barrier);
		barrier.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				getSharedPreferences("setting", MODE_PRIVATE).edit()
						.putBoolean(Constants.BARRIER, isChecked).commit();
				Intent intent = new Intent(Constants.BARRIER_SWITCH);
				intent.putExtra(Constants.BARRIER_FLAG, isChecked);
				sendBroadcast(intent);
			}
		});
		userid = (TextView) findViewById(R.id.userid);
		back = (Button) findViewById(R.id.setting_back);
		back.setOnClickListener(this);
		edit = (TextView) findViewById(R.id.editname);
		edit.setOnClickListener(this);
		int method = getSharedPreferences("login",
				MODE_PRIVATE).getInt(Constants.LOGIN_METHOD, -1);
		int userId = getSharedPreferences("userinfo", MODE_PRIVATE)
				.getInt("id", -1);
		if (method == Constants.ACCOUNT_LOGIN) {
			String account = getSharedPreferences("userinfo", MODE_PRIVATE)
					.getString("account_name", null);
			if (account != null) {
				userid.setText(account);
			} else {
				if (userId != -1) {
					userid.setText(userId + "");
				}
			}
		} else {
			String username = getSharedPreferences("userinfo", MODE_PRIVATE)
					.getString("phonenumber", null);
			if (username != null) {
				userid.setText(username);
			} else {
				if (userId != -1) {
					userid.setText(userId + "");
				}
			}
		}

		SharedPreferences sharedPreferences = getSharedPreferences("setting", 0);

		if (sharedPreferences != null) {
			wifi.setChecked(sharedPreferences.getBoolean("wificheck", true));
		} else {
			wifi.setChecked(true);
		}
		if (getIntent().getExtras().getString("flag").equals("main")) {
			(findViewById(R.id.robot_name)).setVisibility(View.VISIBLE);
			edit.setText(getSharedPreferences("robotname", MODE_PRIVATE)
					.getString("name", null));
			versionRobot = getIntent().getStringExtra("version");
			String versionNew = getSharedPreferences("Receipt", MODE_PRIVATE).getString("fota", "");
			if (!"".equals(versionNew)) {
				if (!versionRobot.equals(versionNew)) {
					upgrade.setTextColor(getResources().getColor(R.color.red));
					upgrade.setOnClickListener(this);
				}
			}
			if ("yidong".equals(getIntent().getExtras().getString("from"))) {
				findViewById(R.id.rl_barrier).setVisibility(View.VISIBLE);
			}
			boolean barrierState = getSharedPreferences("setting", MODE_PRIVATE).getBoolean(Constants.BARRIER, true);
			barrier.setChecked(barrierState);
		} else {
			(findViewById(R.id.robot_name)).setVisibility(View.GONE);
			findViewById(R.id.rl_barrier).setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
        super.onDestroy();
        if (mModifyNameDialog != null) {
            mModifyNameDialog.dismiss();
        }
	}
}
