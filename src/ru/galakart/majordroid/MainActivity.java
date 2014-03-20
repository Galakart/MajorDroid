package ru.galakart.majordroid;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity {

	private WebView mWebView;
	private WebView webPost;
	private ProgressBar Pbar;
	private String localURL = "", globalURL = "", serverURL = "", login = "",
			passw = "", wifiHomeNet = "";
	private String tmpDostupAccess = "";
	private String tmpAdressAccess = "";
	private boolean outAccess = false;
	private boolean firstLoad = false;
	private static final int REQUEST_CODE = 1234;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Pbar = (ProgressBar) findViewById(R.id.pB1);
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		mWebView.setWebViewClient(new MajorDroidWebViewer());
		webPost = (WebView) findViewById(R.id.webPost);

		mWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				if (progress < 100
						&& Pbar.getVisibility() == ProgressBar.INVISIBLE) {
					Pbar.setVisibility(ProgressBar.VISIBLE);
				}
				Pbar.setProgress(progress);
				if (progress == 100) {
					Pbar.setVisibility(ProgressBar.INVISIBLE);
				}
			}
		});

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

		case R.id.action_about:
			Intent ab = new Intent(this, AboutActivity.class);
			startActivity(ab);
			return true;

		case R.id.action_quit:
			finish();
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			voiceCommand(matches.get(0));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public class MajorDroidWebViewer extends WebViewClient {
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

		// @Override
		// public void onPageStarted(WebView view, String url, Bitmap favicon) {
		// super.onPageStarted(view, url, favicon);
		//
		// }
	}

	@Override
	public void onResume() {
		super.onResume();
		loadHomePage(0);
	}

	private void loadHomePage(int immediateLoad) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		localURL = prefs.getString(getString(R.string.localUrl), "");
		globalURL = prefs.getString(getString(R.string.globalUrl), "");
		login = prefs.getString(getString(R.string.login), "");
		passw = prefs.getString(getString(R.string.passw), "");
		String dostup = prefs.getString(getString(R.string.dostup), "");
		String vid = prefs.getString(getString(R.string.vid), "");
		String wifiHomeNet = prefs.getString("wifihomenet", "");
		String wifiToast = "";

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

		if (!dostup.equals(tmpDostupAccess))
			firstLoad = false;

		if (dostup.contains("Локальный")) {
			outAccess = false;
			serverURL = localURL;
			wifiToast = "";
			tmpDostupAccess = dostup;

		} else if (dostup.contains("Глобальный")) {
			outAccess = true;
			serverURL = globalURL;
			wifiToast = "";
			tmpDostupAccess = dostup;

		} else if (dostup.contains("Автоматический")) {
			if (wifiHomeNet != "") {
				if (isConnectedToSSID(wifiHomeNet)) {
					outAccess = false;
					serverURL = localURL;
					wifiToast = " (SSID: " + wifiHomeNet + ")";
				} else {
					outAccess = true;
					serverURL = globalURL;
					wifiToast = " (не в домашней сети)";
				}
			} else {
				outAccess = false;
				serverURL = localURL;
				wifiToast = " (не задана домашняя wifi-сеть)";
			}
			tmpDostupAccess = dostup;
		}
		if (!serverURL.equals(tmpAdressAccess))
			firstLoad = false;

		if ((!firstLoad) || (immediateLoad == 1)) {
			Toast toast = Toast.makeText(getApplicationContext(), "",
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			if (outAccess)
				toast.setText("Глобальный доступ" + wifiToast);
			else
				toast.setText("Локальный доступ" + wifiToast);
			if (serverURL == "") {
				toast.setText("Не задан адрес сервера в настройках");
				toast.show();
			} else {
				mWebView.loadUrl("http://" + serverURL + "/menu.html");
				// потом использовать reload();
				firstLoad = true;
				if (!serverURL.equals(tmpAdressAccess))
					toast.show();
				tmpAdressAccess = serverURL;
			}
		}

	}

	private void voiceCommand(String command) {
		Toast toast = Toast.makeText(getApplicationContext(), command,
				Toast.LENGTH_LONG);
		toast.setGravity(Gravity.BOTTOM, 0, 0);
		toast.show();
		webPost.loadUrl("http://" + serverURL + "/command.php?qry=" + command);
	}

	public void imgb_home_click(View v) {
		loadHomePage(1);
	}

	public void imgb_voice_click(View v) {
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Голосовой движок не установлен", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			toast.show();
		} else {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите...");
			startActivityForResult(intent, REQUEST_CODE);
		}
	}

	public void imgb_pult_click(View v) {
		Intent j = new Intent(this, ControsActivity.class);
		startActivity(j);
	}

	public void imgb_settings_click(View v) {
		Intent i = new Intent(this, Prefs.class);
		startActivity(i);
	}

	boolean isConnectedToSSID(String t) {
		try {
			WifiManager wifiMgr = (WifiManager) this
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
			if (wifiInfo.getSSID().equals(t))
				return true;
		} catch (Exception a) {
		}
		return false;
	}

}

/*
 * На будущее: 1. Использовать reload(); при обновлении браузера 2. Использовать
 * окно браузера для вывода возможных ошибок, вот так String summary =
 * "<html><body>You scored <b>192</b> points.</body></html>";
 * webview.loadData(summary, "text/html", null);
 */