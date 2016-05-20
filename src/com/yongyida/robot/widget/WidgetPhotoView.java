package com.yongyida.robot.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.video.command.User;

/**
 * 用户视图
 * 
 * @author
 * @since 2016-04-10
 * */
public class WidgetPhotoView extends LinearLayout {
	private Context mContext;
	private CircleSelectImageView mImageView;
	private TextView mTvUserName;
	private User mUser;
	
	public WidgetPhotoView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public WidgetPhotoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	private void init() {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.widget_photo_view, (ViewGroup)getParent());
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f));
		mImageView = (CircleSelectImageView) layout.findViewById(R.id.iv_contact);
		mTvUserName = (TextView) layout.findViewById(R.id.tv_contact);
		this.addView(layout);
	}
	
	public void setImageResource(int resId) {
		mImageView.setImageResource(resId);
	}
	
	public void setText(String name) {
		mTvUserName.setText(name);
	}
	
	public User getUser() {
		return mUser;
	}
	
	public void setUser(User user) {
		mUser = user;
	}
	
	public boolean getSelected() {
		return mImageView.getSelected();
	}
	
	public void setSelected(boolean selected) {
		mImageView.setSelected(selected);
	}
}
