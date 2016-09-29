package com.yongyida.robot.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ZoomButtonsController;

import com.yongyida.robot.R;

import java.lang.reflect.Field;

public class AboutActivity extends Activity {

	private WebView about;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		about = (WebView) findViewById(R.id.about);
		WebSettings set = about.getSettings();
		set.setJavaScriptEnabled(true);
		set.setBuiltInZoomControls(true);
		setZoomControlGone(about);	
	//	about.loadUrl("http://www.gaojunkeji.com");  // 020-28142331
	//	about.loadUrl("http://www.njLiuchao.com"); // 400-025-8505
	//	about.loadUrl(getString(R.string.about_url));  //400-9696488
		about.loadUrl("http://www.lxyrobot.com");  //400-1003628
	//	about.loadUrl("http://www.uiqrobot.com");  //400-9028738
	}

	public void setZoomControlGone(View view) {

		Class classType;

		Field field;

		try {

			classType = WebView.class;

			field = classType.getDeclaredField("mZoomButtonsController");

			field.setAccessible(true);

			ZoomButtonsController mZoomButtonsController = new ZoomButtonsController(
					view);

			mZoomButtonsController.getZoomControls().setVisibility(View.GONE);

			try {

				field.set(view, mZoomButtonsController);

			} catch (IllegalArgumentException e) {

				e.printStackTrace();

			} catch (IllegalAccessException e) {

				e.printStackTrace();

			}

		} catch (SecurityException e) {

			e.printStackTrace();

		} catch (NoSuchFieldException e) {

			e.printStackTrace();

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		
		return true;
	}

}
