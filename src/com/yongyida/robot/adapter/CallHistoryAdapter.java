package com.yongyida.robot.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.video.sdk.CallHistory;

import java.util.Date;
import java.util.List;

public class CallHistoryAdapter extends BaseAdapter {
	public interface OnListItemClickListener {
		public void onClick(int position, CallHistory history);
	}

	private List<CallHistory> mHistoryList;
	private Context mContext;
	private OnListItemClickListener mOnListItemClickListener;

	public CallHistoryAdapter(Context context, List<CallHistory> list) {
		mContext = context;
		mHistoryList = list;
	}

	/*
	 * 设置列表项监听器
	 */
	public void setSelectFileListener(OnListItemClickListener listener) {
		mOnListItemClickListener = listener;
	}

	// 获取ListView的项个数
	public int getCount() {
		return mHistoryList.size();
	}

	// 获取项
	public Object getItem(int position) {
		return mHistoryList.get(position);
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
	@SuppressLint("InflateParams")
	public View getView(final int position, View convertView, ViewGroup parent) {
		final CallHistory history = mHistoryList.get(position);
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.listitem_callhistory, null);
		}

		((TextView) convertView.findViewById(R.id.ch_name)).setText(
				(history.getUser().getUserName() != null) ? history.getUser().getUserName():"小勇");
		((TextView) convertView.findViewById(R.id.ch_id)).setText(Long.toString(history.getUser().getId()));
		Date date = new Date(history.getCallTime());
		if (System.currentTimeMillis() - history.getCallTime() > 24*60*60*1000)
			((TextView) convertView.findViewById(R.id.ch_time)).setText(new java.text.SimpleDateFormat("M月d日").format(date));
		else
			((TextView) convertView.findViewById(R.id.ch_time)).setText(new java.text.SimpleDateFormat("h:mm").format(date));

		convertView.findViewById(R.id.ll).setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnListItemClickListener != null) {
					mOnListItemClickListener.onClick(position, history);
				}
			}
		});
		return convertView;
	}
}
