package com.yongyida.robot.fragment;

import com.yongyida.robot.R;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Fragment1 extends BaseFragment {
	
	private RecyclerView mRecyclerView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment1, null);
		mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		return view;
	}
}
