package ru.galakart.majordroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

public class ControsActivity extends Activity {
	private WebView webPost;
	private String serverURL, login, passw, pathScripts = "";
	private boolean outAccess = false;
	private SharedPreferences prefs;
	private TextView textView_legend;
	private String scriptnames[] = new String[9];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String vid = prefs.getString(getString(R.string.vid), "");
		pathScripts = prefs.getString(getString(R.string.path_scripts), "");
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
		setContentView(R.layout.activity_contros);
		webPost = (WebView) findViewById(R.id.webPost);
		webPost.getSettings().setJavaScriptEnabled(true);
		webPost.setWebViewClient(new MajorDroidWebViewer());
		textView_legend = (TextView) findViewById(R.id.textView_legend);
		textView_legend.setMovementMethod(new ScrollingMovementMethod());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contros, menu);
		return true;
	}

	private class MajorDroidWebViewer extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onReceivedHttpAuthRequest(WebView view,
				HttpAuthHandler handler, String host, String realm) {
			if (outAccess)
				handler.proceed(login, passw);
		}
	}

	public void onResume() {
		super.onResume();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		String localURL = prefs.getString(getString(R.string.localUrl), "");
		String globalURL = prefs.getString(getString(R.string.globalUrl), "");
		String dostup = prefs.getString(getString(R.string.dostup), "");
		login = prefs.getString(getString(R.string.login), "");
		passw = prefs.getString(getString(R.string.passw), "");
		scriptnames[0] = prefs.getString(getString(R.string.scriptname1), "");
		scriptnames[1] = prefs.getString(getString(R.string.scriptname2), "");
		scriptnames[2] = prefs.getString(getString(R.string.scriptname3), "");
		scriptnames[3] = prefs.getString(getString(R.string.scriptname4), "");
		scriptnames[4] = prefs.getString(getString(R.string.scriptname5), "");
		scriptnames[5] = prefs.getString(getString(R.string.scriptname6), "");
		scriptnames[6] = prefs.getString(getString(R.string.scriptname7), "");
		scriptnames[7] = prefs.getString(getString(R.string.scriptname8), "");
		scriptnames[8] = prefs.getString(getString(R.string.scriptname9), "");
		if (dostup.contains("Локальный")) {
			outAccess = false;
			serverURL = localURL;
		} else if (dostup.contains("Глобальный")) {
			outAccess = true;
			serverURL = globalURL;
		}
		String legend = "";
		for (int i = 0; i < 9; i++) {
			if (scriptnames[i] != "")
				legend += i + 1 + ". " + scriptnames[i] + ".\n";
			else
				legend += i + 1 + ". Скрипт не задан.\n";
		}
		textView_legend.setText(legend);
	}

	public void imgb_script1_exec(View v) {
		String script = scriptnames[0];
		if (script != "")
			scriptExec(script);
		else
			scriptneni();
	}

	public void imgb_script2_exec(View v) {
		String script = scriptnames[1];
		if (script != "")
			scriptExec(script);
		else
			scriptneni();
	}

	public void imgb_script3_exec(View v) {
		String script = scriptnames[2];
		if (script != "")
			scriptExec(script);
		else
			scriptneni();
	}

	public void imgb_script4_exec(View v) {
		String script = scriptnames[3];
		if (script != "")
			scriptExec(script);
		else
			scriptneni();
	}

	public void imgb_script5_exec(View v) {
		String script = scriptnames[4];
		if (script != "")
			scriptExec(script);
		else
			scriptneni();
	}

	public void imgb_script6_exec(View v) {
		String script = scriptnames[5];
		if (script != "")
			scriptExec(script);
		else
			scriptneni();
	}

	public void imgb_script7_exec(View v) {
		String script = scriptnames[6];
		if (script != "")
			scriptExec(script);
		else
			scriptneni();
	}

	public void imgb_script8_exec(View v) {
		String script = scriptnames[7];
		if (script != "")
			scriptExec(script);
		else
			scriptneni();
	}

	public void imgb_script9_exec(View v) {
		String script = scriptnames[8];
		if (script != "")
			scriptExec(script);
		else
			scriptneni();
	}

	public void scriptExec(String script) {
		webPost.loadUrl("http://" + serverURL + pathScripts + script);
		Toast toast = Toast.makeText(getApplicationContext(), "Скрипт "
				+ script, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.BOTTOM, 0, 0);
		toast.show();
	}

	private void scriptneni() {
		Toast toast = Toast
				.makeText(
						getApplicationContext(),
						"Кнопка не привязана к скрипту.\nЗадайте имя скрипта в настройках",
						Toast.LENGTH_LONG);
		toast.setGravity(Gravity.BOTTOM, 0, 0);
		toast.show();
	}
}
