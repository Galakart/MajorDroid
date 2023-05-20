package ru.galakart.majordroid

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.WindowManager

class AboutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setScreen()
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

    fun View.rateExec() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=ru.galakart.majordroid")
        startActivity(intent)
    }
}
