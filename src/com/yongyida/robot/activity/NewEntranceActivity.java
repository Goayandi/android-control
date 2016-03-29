package com.yongyida.robot.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

import com.yongyida.robot.R;
import com.yongyida.robot.fragment.BaseFragment;
import com.yongyida.robot.fragment.Fragment1;
import com.yongyida.robot.fragment.Fragment2;
import com.yongyida.robot.fragment.Fragment3;


public class NewEntranceActivity extends BaseFragmentActivity {
	List<BaseFragment> mFragments;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_new_entrance);
		init();
	}

	private void init() {
		initView();
		initFragment();
	}

	private void initView() {
		//TODO
	}

	private void initFragment() {
		Fragment1 fragment = new Fragment1();
		getSupportFragmentManager()
		.beginTransaction()
		.add(R.id.fl, fragment,fragment.getClass().getSimpleName())
		.commit();
		mFragments = new ArrayList<BaseFragment>();
		mFragments.add(fragment);
	}
	
	/**
	 * 切换fragment
	 * @param position 切换到第几个fragment
	 */
	public void switchFragment(int position){
		
		switch (position) {
		case 1:
			BaseFragment fragment1 = (BaseFragment) getSupportFragmentManager().findFragmentByTag(Fragment1.class.getSimpleName());
			if(fragment1 != null){
				showFragment(fragment1);
			}else{
				fragment1 = new Fragment1();
				addFragment(fragment1);
				showFragment(fragment1);
			}
			break;
		case 2:
			BaseFragment fragment2 = (BaseFragment) getSupportFragmentManager().findFragmentByTag(Fragment2.class.getSimpleName());
			if(fragment2 != null){
				showFragment(fragment2);
			}else{
				fragment2 = new Fragment2();
				addFragment(fragment2);
				showFragment(fragment2);
			}
			break;
		case 3:
			BaseFragment fragment3 = (BaseFragment) getSupportFragmentManager().findFragmentByTag(Fragment3.class.getSimpleName());
			if(fragment3 != null){
				showFragment(fragment3);
			}else{
				fragment3 = new Fragment3();
				addFragment(fragment3);
				showFragment(fragment3);
			}
			break;

		default:
			break;
		}
	}
	
	/**
	 * 
	 * @param fragment 需要显示的fragment
	 */
	public void showFragment(BaseFragment fragment){
		if(mFragments == null){
			return;
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		for (BaseFragment f : mFragments) {
			if(f == fragment){
				ft.show(f);
			} else {
				ft.hide(f);
			}
		}
		ft.commit();
	}
	
	/**
	 * 添加fragment到mFragment并添加到FragmentManager
	 * @param fragment
	 */
	public void addFragment(BaseFragment fragment){
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.fl, fragment, fragment.getClass().getSimpleName()).commit();
		mFragments.add(fragment);
	}
}
