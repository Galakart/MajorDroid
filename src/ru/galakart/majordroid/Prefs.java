package ru.galakart.majordroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.WindowManager;

public class Prefs extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String vid = prefs.getString(getString(R.string.vid), "");
		if (vid.contains("Обычный")) {
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		if (vid.contains("Полноэкранный")) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}
	    addPreferencesFromResource(R.xml.settings);
	}
}
