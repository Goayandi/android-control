package com.yongyida.robot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.yongyida.robot.R;
import com.yongyida.robot.video.command.User;
import com.yongyida.robot.widget.CircleSelectImageView;

import java.util.List;

public class FriendAdapter extends BaseAdapter {
	private List<User> mUserList;
	private Context mContext;
	private LayoutInflater mInflater;

	public FriendAdapter(Context context, List<User> data) {
		mContext = context;
		mUserList = data;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/*
	 * 列表项监听接口
	 */
	public interface OnListItemClickListener {
		public void onClick(int position, User user);
	}

	/*
	 * 列表项监听器
	 */
	private OnListItemClickListener onListItemClickListener;

	/*
	 * 设置列表项监听器
	 */
	public void setSelectFileListener(OnListItemClickListener listener) {
		this.onListItemClickListener = listener;
	}

	// 获取ListView的项个数
	public int getCount() {
		return mUserList.size();
	}

	// 获取项
	public Object getItem(int position) {
		return mUserList.get(position);
	}

	// 获取项的ID
	public long getItemId(int position) {
		return position;
	}

	// 获取项的类型
	public int getItemViewType(int position) {
		return 0;
	}

	// 获取项的类型数
	public int getViewTypeCount() {
		return 1;
	}

	// 获取View
	public View getView(final int position, View convertView, ViewGroup parent) {
		final User user = mUserList.get(position);
		Holder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listitem_friend, parent, false);

			holder = new Holder();
			holder.imageView = (CircleSelectImageView) convertView.findViewById(R.id.friend_photo);
			convertView.setTag(holder);
		}
		else {
			holder = (Holder) convertView.getTag();
		}

		// Populate the text
		// holder.imageView.setImageResource(resId);

		return convertView;
	}

	private static class Holder {
		public CircleSelectImageView imageView;
	}
}
