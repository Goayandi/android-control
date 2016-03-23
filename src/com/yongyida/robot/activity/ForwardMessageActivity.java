package com.yongyida.robot.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.yongyida.robot.R;

public class ForwardMessageActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forward_message);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.forward_message, menu);
		return true;
	}

}
