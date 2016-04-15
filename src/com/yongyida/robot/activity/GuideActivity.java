package com.yongyida.robot.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.ryanharter.viewpager.PagerAdapter;
import com.ryanharter.viewpager.ViewPager;
import com.yongyida.robot.R;
import com.yongyida.robot.utils.StartUtil;

public class GuideActivity extends Activity {

	private ViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		StartUtil.startintent(this, LoginActivity.class, "finish");
		return;

	}

	private void setparams(ImageView img) {
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		img.setLayoutParams(params);
		img.setScaleType(ScaleType.FIT_XY);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
