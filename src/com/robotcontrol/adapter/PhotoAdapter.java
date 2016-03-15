package com.robotcontrol.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.robotcontrol.activity.PhotoActivity;
import com.robotcontrol.activity.R;
import com.robotcontrol.huanxin.MessageAdapter.ViewHolder;
import com.robotcontrol.utils.ImageLoader;

import android.R.color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoAdapter extends BaseAdapter {

	private PhotoActivity activity;
	private String[] paths;
	private ImageLoader loader;
	private callback back;
	private boolean[] checks; //用于保存checkBox的选择状态
	
	

	public PhotoAdapter(PhotoActivity activity, String[] paths,
			ImageLoader loader, callback back) {
		super();
		this.activity = activity;
		this.paths = paths;
		this.loader = loader;
		this.back = back;
		checks = new boolean[paths.length];
	}

	@Override
	public int getCount() {
		return paths.length;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	LayoutInflater layout = null;

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		ViewHolder holder = null;
		if (layout == null) {
			layout = LayoutInflater.from(activity);
		}
		if (convertView == null) {
			convertView = layout.inflate(R.layout.photo, null);
		}else {  
            holder = (ViewHolder) convertView.getTag();  
        } 
		String convertpath = paths[position];
		ImageView image = (ImageView) convertView.findViewById(R.id.photo_img);
		image.setScaleType(ImageView.ScaleType.CENTER_CROP);
		TextView text = (TextView) convertView.findViewById(R.id.photo_name);
		final String name = convertpath.substring(
				convertpath.lastIndexOf("/") + 1, convertpath.length());
		final int pos  = position; //pos必须声明为final
		text.setText(name);
		CheckBox check = (CheckBox) convertView.findViewById(R.id.check);
		convertView.setTag(holder);
		if (back.IslongClick()) {
			check.setVisibility(View.VISIBLE);
		} else {
			check.setVisibility(View.GONE);
		}
		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean ischecked) {
				if (ischecked) {
					activity.checked(name);
					checks[pos] = ischecked;
					
				} else {
					activity.notcheck(name);
				}
			}
		});
		check.setChecked(checks[pos]);
		loader.loadImage(convertpath, image, false);
		return convertView;
	}

	public interface callback {
		public boolean IslongClick();
	}
}
