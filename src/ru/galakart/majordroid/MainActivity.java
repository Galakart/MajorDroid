package ru.galakart.majordroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

	WebView mWebView;
	private String localURL, globalURL, login, passw;
	private boolean outAccess = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new MajorDroidWebViewer());

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent i = new Intent(this, Prefs.class);
			startActivity(i);
			return true;

		case R.id.action_controls:
			Intent j = new Intent(this, ControsActivity.class);
			startActivity(j);
			return true;
		}
		return super.onOptionsItemSelected(item);
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

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		// читаем установленное значение из CheckBoxPreference
		// if (prefs.getBoolean(getString(R.string.pref_openmode), false))
		// {
		// openFile(FILENAME);
		// }

		localURL = prefs.getString(getString(R.string.localUrl), "");
		globalURL = prefs.getString(getString(R.string.globalUrl), "");
		login = prefs.getString(getString(R.string.login), "");
		passw = prefs.getString(getString(R.string.passw), "");
		String dostup = prefs.getString(getString(R.string.dostup), "");

		if (dostup.contains("Локальный"))
			outAccess = false;
		else if (dostup.contains("Глобальный"))
			outAccess = true;

		if (outAccess)
			homepage(globalURL);
		else
			homepage(localURL);

	}

	private void homepage(String serverURL) {
		if (serverURL != null)
			mWebView.loadUrl("http://" + serverURL + "/menu.html");
	}
}
