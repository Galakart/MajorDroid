package ru.galakart.majordroid;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ControsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contros);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contros, menu);
		return true;
	}

}
