package ru.galakart.majordroid

import android.app.AlertDialog
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast

class PrefsActivity : PreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreen()
        setWifiHomeNetButton()
    }

    private fun setScreen() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val vid = prefs.getString("view", "")
        when (vid) {
            "1" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
            "2" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            }
        }
    }

    private fun setWifiHomeNetButton() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        addPreferencesFromResource(R.xml.settings)
        val wifiMgr = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val currentSSID = wifiMgr.connectionInfo.ssid
        val prefSSID = prefs.getString("wifihomenet", "")

        val button = findPreference("buttonWifiHomeNet") as Preference
        button.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val ad = AlertDialog.Builder(this@PrefsActivity)
            ad.setTitle(getString(R.string.wifititle))
            if (prefSSID.isNullOrBlank())
                ad.setMessage(getString(R.string.wifisetto) + currentSSID + " ?")
            else
                ad.setMessage(getString(R.string.wificurrentnet) + prefSSID
                        + "\n" + getString(R.string.wifichoseto) + currentSSID + " ?")
            ad.setPositiveButton(getString(R.string.save)) { dialog, arg1 ->
                val toast = Toast.makeText(applicationContext,
                        getString(R.string.wifisaved) + "($currentSSID)",
                        Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.BOTTOM, 0, 0)
                val editor = prefs.edit()
                editor.putString("wifihomenet", currentSSID)
                if (editor.commit())
                    toast.show()
            }
            ad.setNegativeButton(getString(R.string.cancel)) { dialog, arg1 ->
                // none
            }
            ad.setCancelable(true)

            if (!currentSSID.isNullOrEmpty()) {
                if (prefSSID == currentSSID) {

                    val albuilder = AlertDialog.Builder(
                            this@PrefsActivity)
                    albuilder
                            .setTitle(getString(R.string.message))
                            .setMessage(getString(R.string.wifialreadyset))
                            .setCancelable(false)
                            .setNegativeButton(getString(R.string.back)
                            ) { dialog, _ -> dialog.cancel() }
                    val alert = albuilder.create()
                    alert.show()

                } else
                    ad.show()
            } else {
                val albuilder = AlertDialog.Builder(
                        this@PrefsActivity)
                albuilder
                        .setTitle(getString(R.string.message))
                        .setMessage(getString(R.string.wifiisoff))
                        .setCancelable(false)
                        .setNegativeButton(getString(R.string.back)
                        ) { dialog, _ -> dialog.cancel() }
                val alert = albuilder.create()
                alert.show()
            }
            true
        }
    }
}
