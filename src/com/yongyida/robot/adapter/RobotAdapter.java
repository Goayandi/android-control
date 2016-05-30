package com.yongyida.robot.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.bean.Robot;

import java.util.List;

public class RobotAdapter extends BaseAdapter {

	private Context context;
	private static List<Robot> robots;

	public RobotAdapter(Context context, List<Robot> robots) {
		super();
		this.context = context;
		this.robots = robots;

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return robots.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
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
	public View getView(int index, View v, ViewGroup arg2) {
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
				} else {
					robotholder.online.setTextColor(context.getResources().getColor(R.color.offline));
					robotholder.online.setText(R.string.not_online);
				}
				if (robots.get(index).getAir().equals(Robot.air.bind)) {

				} else {

				}
				v.setTag(robotholder);
			}
		} else if (v != null) {
			robotholder = (RobotHolder) v.getTag();
		}

		return v;
	}

	class RobotHolder {
		private TextView robotname;
		private TextView rid;
		private TextView online;
		private TextView control;
	}

}
