package com.yongyida.robot.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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

	private static final String TAG = "GuideActivity";
	private ViewPager viewPager;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		Log.i(TAG, "onCreate");
		if (getSharedPreferences("guide", MODE_PRIVATE).getInt("guide", 0) != 0) {
			StartUtil.startintent(this, NewLoginActivity.class, "finish");
			return;
		}
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setOffscreenPageLimit(3);
		viewPager.setAdapter(new PagerAdapter() {

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}

			@Override
			public int getCount() {
				return 3;
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				ImageView img1 = new ImageView(GuideActivity.this);
				setparams(img1);
				/*img1.setImageResource(R.drawable.img1);*/
				img1.setImageResource(R.drawable.img5);
				container.addView(img1);
				ImageView img2 = new ImageView(GuideActivity.this);
				img2.setImageResource(R.drawable.img2);
				setparams(img2);
				container.addView(img2);
				View v = LayoutInflater.from(GuideActivity.this).inflate(
						R.layout.img3, null);
				v.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						StartUtil.startintent(GuideActivity.this,
								NewLoginActivity.class, "finish");
						getSharedPreferences("guide", MODE_PRIVATE).edit()
								.putInt("guide", 1).commit();
					}
				});
				container.addView(v);
				return container.getChildAt(position);
			}

			@Override
			public void destroyItem(ViewGroup container, int position,
					Object object) {
				container.removeViewAt(position);
			}
		});
		viewPager.setPageMargin(0);

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
