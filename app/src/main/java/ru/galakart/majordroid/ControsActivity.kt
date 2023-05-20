package ru.galakart.majordroid

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.webkit.HttpAuthHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_contros.*

class ControsActivity : Activity() {
    private var serverURL = ""
    private var login = ""
    private var passw = ""
    private var pathScripts = ""
    private var outAccess = false
    private val scriptnames = arrayOfNulls<String>(9)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contros)
        setScreenAndComponents()
    }

    private inner class MajorDroidWebViewer : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            view!!.loadUrl(request!!.url.toString())
            return true
        }
        override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String) {
            if (outAccess)
                handler.proceed(login, passw)
        }
    }

    public override fun onResume() {
        super.onResume()
        loadSettings()
    }

    private fun setScreenAndComponents() {
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
        webPost!!.webViewClient = MajorDroidWebViewer()
        tvLegend!!.movementMethod = ScrollingMovementMethod()
    }

    private fun loadSettings() {
        var prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val localURL = prefs!!.getString("localaddress", "")
        val globalURL = prefs!!.getString("globaladdress", "")
        val dostup = prefs!!.getString("access", "")
        login = prefs!!.getString("login", "")
        passw = prefs!!.getString("passw", "")
        pathScripts = prefs!!.getString("scriptprocessor", "")
        scriptnames[0] = prefs!!.getString("scriptname1", "")
        scriptnames[1] = prefs!!.getString("scriptname2", "")
        scriptnames[2] = prefs!!.getString("scriptname3", "")
        scriptnames[3] = prefs!!.getString("scriptname4", "")
        scriptnames[4] = prefs!!.getString("scriptname5", "")
        scriptnames[5] = prefs!!.getString("scriptname6", "")
        scriptnames[6] = prefs!!.getString("scriptname7", "")
        scriptnames[7] = prefs!!.getString("scriptname8", "")
        scriptnames[8] = prefs!!.getString("scriptname9", "")
        when (dostup) {
            "1" -> {
                outAccess = false
                serverURL = localURL
            }
            "2" -> {
                outAccess = true
                serverURL = globalURL
            }
        }
        var legend = ""
        for (i in 0..8) {
            legend += if (scriptnames[i] !== "")
                (i + 1).toString() + ". " + scriptnames[i] + ".\n"
            else
                (i + 1).toString() + ". " + getString(R.string.noscript) + ".\n"
        }
        tvLegend!!.text = legend
    }

    fun View.imgbScript1Exec() {
        scriptExec(scriptnames[0])
    }

    fun View.imgbScript2Exec() {
        scriptExec(scriptnames[1])
    }

    fun View.imgbScript3Exec() {
        scriptExec(scriptnames[2])
    }

    fun View.imgbScript4Exec() {
        scriptExec(scriptnames[3])
    }

    fun View.imgbScript5Exec() {
        scriptExec(scriptnames[4])
    }

    fun View.imgbScript6Exec() {
        scriptExec(scriptnames[5])
    }

    fun View.imgbScript7Exec() {
        scriptExec(scriptnames[6])
    }

    fun View.imgbScript8Exec() {
        scriptExec(scriptnames[7])
    }

    fun View.imgbScript9Exec() {
        scriptExec(scriptnames[8])
    }

    private fun scriptExec(script: String?) {
        if (!script.isNullOrEmpty()) {
            webPost!!.loadUrl("http://$serverURL$pathScripts$script")
            val toast = Toast.makeText(
                            applicationContext,
                       getString(R.string.scriptok) + "($script)",
                            Toast.LENGTH_LONG)
            toast.setGravity(Gravity.BOTTOM, 0, 0)
            toast.show()
        } else {
            val toast = Toast.makeText(
                            applicationContext,
                            getString(R.string.scriptnotset),
                            Toast.LENGTH_LONG)
            toast.setGravity(Gravity.BOTTOM, 0, 0)
            toast.show()
        }
    }
}
