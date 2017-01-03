package com.yongyida.robot.activity;

import android.os.Bundle;
import android.view.Menu;

import com.yongyida.robot.R;

public class ShowVideoActivity extends OriginalActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_video);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_video, menu);
		return true;
	}

}
