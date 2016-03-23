package com.yongyida.robot.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

import com.yongyida.robot.R;

public class RobotDialog extends Dialog {

	@Override
	public void setContentView(View view, LayoutParams params) {
		// TODO Auto-generated method stub
		super.setContentView(view, params);
	}

	public RobotDialog(Context context) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public void showdialog() {
		
		setCanceledOnTouchOutside(true);
		Window window = getWindow();
		
		window.setWindowAnimations(R.style.dialoganim);
		// window.setBackgroundDrawableResource(R.color.vifrification);
		WindowManager.LayoutParams params = window.getAttributes();
		window.setAttributes(params);
		show();
	}

}
