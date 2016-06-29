package com.yongyida.robot.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.bean.Robot;

import java.util.List;

public class RobotAdapter extends BaseAdapter {

	private static final String TAG = "RobotAdapter";
	private Context context;
	private static List<Robot> robots;
	private OnImageButtonClickListener mListener;

	public RobotAdapter(Context context, List<Robot> robots, OnImageButtonClickListener listener) {
		super();
		this.context = context;
		this.robots = robots;
		mListener = listener;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return robots.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	LayoutInflater layout = null;
	RobotHolder robotholder = null;

	@Override
	public View getView(final int index, View v, ViewGroup arg2) {
		if (layout == null) {
			layout = LayoutInflater.from(context);
		}
		if (v == null) {
			v = layout.inflate(R.layout.robotitem, null);
			robotholder = new RobotHolder();
			robotholder.robotname = (TextView) v.findViewById(R.id.robot_item);
			robotholder.rid = (TextView) v.findViewById(R.id.rid);
			robotholder.online = (TextView) v.findViewById(R.id.online);
			robotholder.control = (TextView) v.findViewById(R.id.robot_control);
			robotholder.imageButton = (ImageButton) v.findViewById(R.id.ib);
			robotholder.rl = (RelativeLayout) v.findViewById(R.id.rl);
			v.setTag(robotholder);
		} else if (v != null) {
			robotholder = (RobotHolder) v.getTag();
		}

		if (robots.size() != 0) {
			robotholder.robotname
					.setText(robots.get(index).getRname() + "");
			robotholder.rid.setText(robots.get(index).getId() + "");
			if (robots.get(index).getController() == 0) {
				robotholder.control.setText(R.string.dont_have);
				robotholder.control.setTextColor(Color.GREEN);
			} else {
				robotholder.control.setText(robots.get(index).getController() + "");
				robotholder.control.setTextColor(Color.RED);
			}
			if (robots.get(index).isOnline()) {
				robotholder.online.setTextColor(context.getResources().getColor(R.color.online));
				robotholder.online.setText(R.string.online);
				robotholder.rl.setBackground(context.getResources().getDrawable(R.drawable.item_connection_bg_online));
				robotholder.imageButton.setBackground(context.getResources().getDrawable(R.drawable.button_connect_ok));
			} else {
				robotholder.online.setTextColor(context.getResources().getColor(R.color.offline));
				robotholder.online.setText(R.string.not_online);
				robotholder.rl.setBackground(context.getResources().getDrawable(R.drawable.item_connection_bg));
				robotholder.imageButton.setBackground(context.getResources().getDrawable(R.drawable.button_connect));
			}
			if (robots.get(index).getAir().equals(Robot.air.bind)) {

			} else {

			}
			robotholder.imageButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mListener != null) {
						mListener.myClick(index);
					}
				}
			});
		}
		return v;
	}

	public interface OnImageButtonClickListener{
		void myClick(int position);
	}

	class RobotHolder {
		private TextView robotname;
		private TextView rid;
		private TextView online;
		private TextView control;
		private ImageButton imageButton;
		private RelativeLayout rl;
	}

}
