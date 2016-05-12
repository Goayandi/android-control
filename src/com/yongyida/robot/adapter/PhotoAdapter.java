package com.yongyida.robot.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.activity.PhotoActivity;
import com.yongyida.robot.utils.ImageLoader;

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

	public void setAllUnCheck(){
		for(int i = 0; i < checks.length; i++){
			checks[i] = false;
		}
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
			holder = new ViewHolder();
			holder.iv = (ImageView) convertView.findViewById(R.id.photo_img);
			holder.tv = (TextView) convertView.findViewById(R.id.photo_name);
			holder.cb = (CheckBox) convertView.findViewById(R.id.check);
			convertView.setTag(holder);
		}else {
            holder = (ViewHolder) convertView.getTag();
        } 
		String convertpath = paths[position];

		holder.iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

		final String name = convertpath.substring(
				convertpath.lastIndexOf("/") + 1, convertpath.length());
		final int pos  = position; //pos必须声明为final
		holder.tv.setText(name);


		if (back.IslongClick()) {
			holder.cb.setVisibility(View.VISIBLE);
		} else {
			holder.cb.setVisibility(View.GONE);
		}
		holder.cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean ischecked) {
				checks[pos] = ischecked;
				if (ischecked) {
					activity.checked(name);
				} else {
					activity.notcheck(name);
				}
			}
		});
		holder.cb.setChecked(checks[pos]);
		loader.loadImage(convertpath, holder.iv, false);
		return convertView;
	}

	public static class ViewHolder {
		ImageView iv;
		TextView tv;
		CheckBox cb;
	}

	public interface callback {
		public boolean IslongClick();
	}
}
