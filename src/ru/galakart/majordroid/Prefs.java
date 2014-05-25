package ru.galakart.majordroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

public class Prefs extends PreferenceActivity {
	private String currentSSID = null, tmpPrefSSID = null;
	AlertDialog.Builder ad;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final SharedPreferences prefs = PreferenceManager
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

		WifiManager wifiMgr = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
		currentSSID = wifiInfo.getSSID();
		tmpPrefSSID = prefs.getString("wifihomenet", "");

		Preference button = (Preference) findPreference("button");
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				ad = new AlertDialog.Builder(Prefs.this);
				ad.setTitle("Выбор домашней Wifi-сети"); // заголовок
				if (tmpPrefSSID == "")
					ad.setMessage("Установить домашнюю сеть на " + currentSSID
							+ " ?");
				else
					ad.setMessage("Текущая домашняя сеть: " + tmpPrefSSID
							+ "\nПоменять её на " + currentSSID + " ?");
				ad.setPositiveButton("Сохранить", new OnClickListener() {
					public void onClick(DialogInterface dialog, int arg1) {
						Toast toast = Toast.makeText(getApplicationContext(),
								"Сеть " + currentSSID + " сохранена",
								Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.BOTTOM, 0, 0);
						Editor editor = prefs.edit();
						editor.putString("wifihomenet", currentSSID);
						if (editor.commit())
							toast.show();
					}
				});
				ad.setNegativeButton("Отмена", new OnClickListener() {
					public void onClick(DialogInterface dialog, int arg1) {
						// none
					}
				});
				ad.setCancelable(true);

				if (currentSSID != null) {
					if (tmpPrefSSID.equals(currentSSID)) {

						AlertDialog.Builder albuilder = new AlertDialog.Builder(
								Prefs.this);
						albuilder
								.setTitle("Сообщение")
								.setMessage(
										"Текущая WiFi-сеть совпадает с занесённой в память. Для записи другой домашней сети, подключитесь к ней.")
								.setCancelable(false)
								.setNegativeButton("Назад",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
											}
										});
						AlertDialog alert = albuilder.create();
						alert.show();

					} else
						ad.show();
				} else {
					AlertDialog.Builder albuilder = new AlertDialog.Builder(
							Prefs.this);
					albuilder
							.setTitle("Важное сообщение!")
							.setMessage("WiFi выключен или нет соединения")
							.setCancelable(false)
							.setNegativeButton("Назад",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
					AlertDialog alert = albuilder.create();
					alert.show();
				}

				return true;
			}
		});

//		Preference button_path_default = (Preference) findPreference("button_path_default");
//		button_path_default.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//			@Override
//			public boolean onPreferenceClick(Preference arg0) {
//				Editor editor = prefs.edit();
//				editor.putString("path_homepage", "/menu.html");
//				editor.putString("path_scripts", "/objects/?script=");
//				editor.putString("path_voice", "/command.php?qry=");
//				editor.putString("path_gps", "/gps.php");
//				Toast toast = Toast.makeText(getApplicationContext(),
//						"Умолчания восстановлены",
//						Toast.LENGTH_SHORT);
//				toast.setGravity(Gravity.BOTTOM, 0, 0);
//				if (editor.commit())
//					toast.show();
//				return true;
//			}
//		});

	}
}
