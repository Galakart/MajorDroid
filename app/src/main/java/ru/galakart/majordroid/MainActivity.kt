package ru.galakart.majordroid


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.content.ActivityNotFoundException
import android.speech.RecognizerIntent
import java.util.*
import android.app.AlertDialog

class MainActivity : Activity() {

    private val REQ_CODE_SPEECH_INPUT = 100
    private var serverURL = ""
    private var login = ""
    private var passw = ""
    private var pathHomepage = ""
    private var pathVoice = ""
    private var tmpDostupAccess = ""
    private var tmpAdressAccess = ""
    private var outAccess = false
    private var firstLoad = true
    private var wifiToast = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setComponents()
        showFirstStartTip()
    }

    public override fun onResume() {
        super.onResume()
        setScreen()
        loadSettings()
        loadHomePage()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView!!.canGoBack()) {
            webView!!.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_about -> {
                val ab = Intent(this, AboutActivity::class.java)
                startActivity(ab)
                return true
            }
            R.id.action_quit -> {
                finish()
                return true
            }
            R.id.action_settings -> {
                val st = Intent(this, PrefsActivity::class.java)
                startActivity(st)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    inner class MajorDroidWebViewer : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            view!!.loadUrl(request!!.url.toString())
            return true
        }
        override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String) {
            if (outAccess)
                handler.proceed(login, passw)
        }
    }

    private fun setComponents() {
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView!!.webViewClient = MajorDroidWebViewer()

        webView!!.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress < 100 && pBar!!.visibility == ProgressBar.INVISIBLE) {
                    pBar!!.visibility = ProgressBar.VISIBLE
                }
                pBar!!.progress = progress
                if (progress == 100) {
                    pBar!!.visibility = ProgressBar.INVISIBLE
                }
            }
        }
    }

    private fun setScreen() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val vid = prefs.getString("view", "")
        when (vid) {
            "1" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                homeTableLay.visibility = View.VISIBLE
            }
            "2" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                homeTableLay.visibility = View.VISIBLE
            }
            "3" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                homeTableLay.visibility = View.GONE
            }
        }
    }

    private fun loadSettings() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val localURL = prefs.getString("localaddress", "")
        val globalURL = prefs.getString("globaladdress", "")
        pathHomepage = prefs.getString("homepage", "")
        pathVoice = prefs.getString("voiceprocessor", "")
        login = prefs.getString("login", "")
        passw = prefs.getString("passw", "")
        val dostup = prefs.getString("access", "")
        val wifiHomeNet = prefs.getString("wifihomenet", "")

        when (dostup) {
            "1" -> {
                outAccess = false
                serverURL = localURL
                wifiToast = ""
            }
            "2" -> {
                outAccess = true
                serverURL = globalURL
                wifiToast = ""
            }
            "3" -> {
                if (wifiHomeNet !== "") {
                    if (isConnectedToSSID(wifiHomeNet)) {
                        outAccess = false
                        serverURL = localURL
                        wifiToast = " (SSID: $wifiHomeNet)"
                    } else {
                        outAccess = true
                        serverURL = globalURL
                        wifiToast = " (не в домашней сети)"
                    }
                } else {
                    outAccess = false
                    serverURL = localURL
                    wifiToast = " (не задана домашняя wifi-сеть)"
                }
            }
        }

        if (dostup != tmpDostupAccess)
            firstLoad = true
        tmpDostupAccess = dostup

        if (serverURL != tmpAdressAccess)
            firstLoad = true
        tmpAdressAccess = serverURL

    }

    private fun loadHomePage() {
        if (firstLoad) {
            val toast = Toast.makeText(applicationContext, "",
                    Toast.LENGTH_LONG)
            toast.setGravity(Gravity.BOTTOM, 0, 0)

            if (outAccess)
                toast.setText("Глобальный доступ$wifiToast")
            else
                toast.setText("Локальный доступ$wifiToast")

            if (serverURL === "")
                toast.setText("Не задан адрес сервера в настройках")
            else
                firstLoad = false

            toast.show()
        }

        if (serverURL !== "") {
            webView!!.loadUrl("http://$serverURL$pathHomepage")
        }
    }

    fun imgbHomeClick(v: View?) {
        loadHomePage()
    }

    fun imgbVoiceClick(v: View?) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speechtip))
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val command = result[0]
                    webPost!!.loadUrl("http://$serverURL$pathVoice$command")
                    val toast = Toast.makeText(applicationContext, command, Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.BOTTOM, 0, 0)
                    toast.show()
                }
            }
        }
    }

    fun imgbPultClick(v: View?) {
        val j = Intent(this, ControsActivity::class.java)
        startActivity(j)
    }

    fun imgbSettingsClick(v: View?) {
        val i = Intent(this, PrefsActivity::class.java)
        startActivity(i)
    }

    private fun isConnectedToSSID(t: String): Boolean {
        try {
            val wifiMgr = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiMgr.connectionInfo
            if (wifiInfo.ssid == t)
                return true
        } catch (a: Exception) {
        }
        return false
    }

    private fun showFirstStartTip() {
//        val versionName = BuildConfig.VERSION_NAME
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val changelogShown = prefs.getString("changelogshown", "")
        if (changelogShown!="1") {
            val alertDialog = AlertDialog.Builder(this@MainActivity).create()
            alertDialog.setTitle("Информация")
            alertDialog.setMessage("Данное приложение содержит только минимальные функции по управлению сервером MajorDomo.\n\n" +
                    "Более новое приложение от официального разработчика со всеми обновлениями можно скачать в Google Play по названию MajorDroid Official.")
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK"
            ) { dialog, which -> dialog.dismiss() }
            alertDialog.show()

            val editor = prefs.edit()
            editor.putString("changelogshown", "1")
            editor.commit()
        }
    }
}
/*
 * На будущее: 1. Использовать reload(); при обновлении браузера 2. Использовать
 * окно браузера для вывода возможных ошибок, вот так String summary =
 * "<html><body>You scored <b>192</b> points.</body></html>";
 * webview.loadData(summary, "text/html", null);
 */