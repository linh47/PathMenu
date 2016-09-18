package com.ai.ipu.ipu_pathmenu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PathMenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_float);
		Button btnOpen = (Button) findViewById(R.id.btnOpenFloat);
		Button btnClose = (Button) findViewById(R.id.btnCloseFloat);

		btnOpen.setOnClickListener(mClickListener);
		btnClose.setOnClickListener(mClickListener);

	}

	OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.btnOpenFloat) {
				Intent intent = new Intent(PathMenuActivity.this,
						PathMenuService.class);
				startService(intent);
			} else if (v.getId() == R.id.btnCloseFloat) {
				Intent intent = new Intent(PathMenuActivity.this,
						PathMenuService.class);
				stopService(intent);
			}
		}
	};
}
