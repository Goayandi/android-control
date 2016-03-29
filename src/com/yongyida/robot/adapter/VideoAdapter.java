package com.yongyida.robot.adapter;

import com.yongyida.robot.R;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class VideoAdapter extends Adapter<VideoAdapter.MyViewHolder> {
	
	private Context context;
	
	public VideoAdapter(Context context) {
		this.context = context;
	}
	
	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_video, parent, false));
		return holder;
	}
	
	@Override
	public int getItemCount() {
		return 0;
	}

	@Override
	public void onBindViewHolder(MyViewHolder arg0, int arg1) {
		
	}

	class MyViewHolder extends ViewHolder{
		SurfaceView surfaceView;
		RecyclerView recyclerView;
		
		public MyViewHolder(View itemView) {
			super(itemView);
			surfaceView = (SurfaceView) itemView.findViewById(R.id.sv);
			recyclerView = (RecyclerView) itemView.findViewById(R.id.recycler_view);
		}
		
	}
}
