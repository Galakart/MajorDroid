package ru.galakart.majordroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.io.File;

import java.util.LinkedList;
import java.util.Queue;


import java.net.ServerSocket;
import java.net.Socket;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.Toast;
import android.util.Log;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;

import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


import android.media.AudioManager;
import android.media.MediaPlayer;

import com.example.recognizer.DataFiles;
import com.example.recognizer.Grammar;
import com.example.recognizer.PhonMapper;

import org.apache.commons.io.FileUtils;


import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;


public class MainActivity extends Activity implements RecognitionListener, SensorEventListener {
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private WebView mWebView;
	private WebView webPost;
	private ProgressBar Pbar;
	private String localURL = "", globalURL = "", serverURL = "", login = "",
			passw = "", wifiHomeNet = "", pathHomepage = "", pathVoice = "", pathQR="", pathGps = "";
	private String tmpDostupAccess = "";
	private String tmpAdressAccess = "";
	private String VoiceHotWord = "";
	private String uploadURL = "";
	private String faceURL = "";
	private boolean outAccess = false;
	private boolean firstLoad = false;
	private static final int REQUEST_CODE_VOICE = 1234;
	private static final int REQUEST_CODE_MESSAGE = 1236;	
	private static final int REQUEST_CODE_QR = 1237;	
	private static final int VOICE_INPUT_TIMIOUT_MILLIS = 10000;
	private String gpsTimeOut;
	private Timer timer;
	private TimerTask doAsynchronousTask;
	private Handler delayHandler;
	private boolean timerOn = false;
	private boolean faceDetectionEnable = false;
	private boolean faceDetectionWorking = false;
	
	private boolean voiceProximityEnable = false;
	private boolean voiceKeywordEnable = false;
	private boolean voiceKeywordWorking = false;
	private boolean voiceGoogleInProgress = false;
	private boolean disableFacePost = false;
	private final int TCP_SERVER_PORT = 7999; //Define the server port
	private MediaPlayer mediaPlayer;
    private String qrCameraSet;	
    private Camera mCamera;
    private SurfaceView cameraSurface;
    
    private int NumberOfFacesDetected=0;

    private static final String COMMAND_SEARCH = "command";
    private static final String KWS_SEARCH = "hotword";

    private final Handler mHandler = new Handler();
    private SpeechRecognizer mRecognizer;
    
    private SensorManager mSensorManager;
    private float mSensorMaximum;
    private float mSensorValue;
    ProgressDialog dialog;
    private int serverResponseCode=0;
    private String activityResultString="";
    

    private FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {
        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {
        	if (faces.length!=NumberOfFacesDetected) {
             Log.d("onFaceDetection", "Number of Faces:" + faces.length);
             NumberOfFacesDetected=faces.length;
        	 String resURL=faceURL + "&faces=" + Integer.toString(NumberOfFacesDetected);
        	 //Log.d("onFaceDetection", "Loading face URL:" + resURL);
        	 if (!disableFacePost) {
              webPost.loadUrl(resURL);
        	 }

             if (NumberOfFacesDetected>0) {
			  Toast toast = Toast.makeText(getApplicationContext(),
					"Faces detected: "+Integer.toString(NumberOfFacesDetected), Toast.LENGTH_SHORT);
			  toast.setGravity(Gravity.BOTTOM, 0, 0);
			  toast.show();
             }
             
        	}
        }
    };    
    
	
    private final Runnable mStopRecognitionCallback = new Runnable() {
        @Override
        public void run() {
            stopRecognition();
        }
    };

    
    @Override
    protected void onPause() {   	

    	Log.d(TAG, "Activity pause");
    	super.onPause();    	
		if (voiceKeywordWorking) {
	        mRecognizer.cancel();
	        mRecognizer.stop();
	        voiceKeywordWorking=false;
		}
		turnOffFaceDetectionCamera();
		
    }
    
    
    
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
		
		final Handler handler = new Handler();
        timer = new Timer();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        try {
                            gpsSend();
                        } catch (Exception e) {
                        }
                    }
                });
            }
        };
        
        
        
        SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		gpsTimeOut = prefs.getString(getString(R.string.gps_period), "5");
		if ((prefs.getString(getString(R.string.gps_switch), "Выкл").equals("Вкл")) && (!timerOn)) {
			timer.schedule(doAsynchronousTask, 0,
					Long.parseLong(gpsTimeOut) * 60 * 1000);
			timerOn = true;
		}
		
		
		
		  //New thread to listen to incoming connections
		  new Thread(new Runnable() {
		 
		   @Override
		   public void run() {
		    try {
		     //Create a server socket object and bind it to a port
		     ServerSocket socServer = new ServerSocket(TCP_SERVER_PORT);
		     //Create server side client socket reference
		     Socket socClient = null;
		     //Infinite loop will listen for client requests to connect
		     while (true) {
		      //Accept the client connection and hand over communication to server side client socket
		      socClient = socServer.accept();
		      //For each client new instance of AsyncTask will be created
		      ServerAsyncTask serverAsyncTask = new ServerAsyncTask();
		      //Start the AsyncTask execution
		      //Accepted client socket object will pass as the parameter
		      serverAsyncTask.execute(new Socket[] {socClient});
		     }
		    } catch (IOException e) {
		     e.printStackTrace();
		    }
		   }
		  }).start();

		  
			if ((prefs.getString(getString(R.string.voice_proximity), "Выкл").equals("Вкл"))) {
				voiceProximityEnable=true;	
			} else {
				voiceProximityEnable=false;				
			}
		  

			if ((prefs.getString(getString(R.string.voice_switch), "Выкл").equals("Вкл"))) {
				voiceKeywordEnable=true;	
			} else {
				voiceKeywordEnable=false;				
			}
			VoiceHotWord=prefs.getString(getString(R.string.voice_phrase), "проснись");
			if (voiceKeywordEnable) {
			 setupRecognizer();
			}
			
	        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
	        if (sensor != null) {
	            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
	            mSensorMaximum = sensor.getMaximumRange();
	        }
			delayHandler=new android.os.Handler();

		    mediaPlayer = new MediaPlayer();
		    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);			
			
	}
	
    @Override
    protected void onDestroy() {
        if (mRecognizer != null) mRecognizer.cancel();
        mSensorManager.unregisterListener(this);
		if (voiceKeywordWorking) {
	        mRecognizer.cancel();
	        mRecognizer.stop();
	        voiceKeywordWorking=false;
		}        
        turnOffFaceDetectionCamera();
        if (mCamera!=null) mCamera=null;
        
        super.onDestroy();
    }
    
    private void setupRecognizer() {

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                	/*
                    List<Device> devices = mController.getDevices();
                    final String[] names = new String[devices.size()];
                    for (int i = 0; i < names.length; i++) {
                        names[i] = devices.get(i).name;
                    }
                    */

            	
                	
                	final String[] names = new String[1];               	
                	names[0]=VoiceHotWord;
                    PhonMapper phonMapper = new PhonMapper(getAssets().open("dict/ru/hotwords"));
                    Grammar grammar = new Grammar(names, phonMapper);
                    grammar.addWords(VoiceHotWord);
                    
                    DataFiles dataFiles = new DataFiles(getPackageName(), "ru");
                    File hmmDir = new File(dataFiles.getHmm());
                    File dict = new File(dataFiles.getDict());
                    File jsgf = new File(dataFiles.getJsgf());
           		    copyAssets(hmmDir);                       
                    saveFile(jsgf, grammar.getJsgf());
                    saveFile(dict, grammar.getDict());
                   
           		    Log.d(TAG, "Recognizer initiate");            		                     
                    mRecognizer = SpeechRecognizerSetup.defaultSetup()
                            .setAcousticModel(hmmDir)
                            .setDictionary(dict)
                            .setBoolean("-remove_noise", false)
                            .setKeywordThreshold(1e-7f)
                            .getRecognizer();

                     Log.d(TAG, "Add keyphrase search");                    
                     mRecognizer.addKeyphraseSearch(KWS_SEARCH, VoiceHotWord);   		
                            
                  
                    //Log.d(TAG, "Add grammar search");                    
                    //mRecognizer.addGrammarSearch(COMMAND_SEARCH, jsgf);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception ex) {
                if (ex != null) {
                    onRecognizerSetupError(ex);
                } else {
                    onRecognizerSetupComplete();
                }
            }
        }.execute();
    }
    
    private void onRecognizerSetupComplete() {
         voiceKeywordWorking=true;    	
         Toast.makeText(this, "Activation: \""+VoiceHotWord+"\"", Toast.LENGTH_SHORT).show();
         mRecognizer.addListener(this);         
         mRecognizer.startListening(KWS_SEARCH);
    }

    private void onRecognizerSetupError(Exception ex) {
        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        voiceKeywordWorking=false;
    }

    private void copyAssets(File baseDir) throws IOException {
        String[] files = getAssets().list("hmm/ru");

        for (String fromFile : files) {
            File toFile = new File(baseDir.getAbsolutePath() + "/" + fromFile);
            InputStream in = getAssets().open("hmm/ru/" + fromFile);
            FileUtils.copyInputStreamToFile(in, toFile);
        }
    }

    private void saveFile(File f, String content) throws IOException {
        File dir = f.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create directory: " + dir);
        }
        FileUtils.writeStringToFile(f, content, "UTF8");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
        if (mRecognizer.getSearchName().equals(COMMAND_SEARCH)) {
            mRecognizer.stop();
        }
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) return;
        String text = hypothesis.getHypstr();
        if (KWS_SEARCH.equals(mRecognizer.getSearchName())) {
            startRecognition();
        } else {
            Log.d(TAG, text);
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        mHandler.removeCallbacks(mStopRecognitionCallback);
        String text = hypothesis != null ? hypothesis.getHypstr() : null;
        Log.d(TAG, "onResult " + text);
        if (text != null) {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            process(text);
        }
        if (COMMAND_SEARCH.equals(mRecognizer.getSearchName())) {
            mRecognizer.startListening(KWS_SEARCH);
        }
    }

    private void startStopRecognition() {
        if (mRecognizer == null) return;
        if (KWS_SEARCH.equals(mRecognizer.getSearchName())) {
            startRecognition();
        } else {
            stopRecognition();
        }
    }

    private synchronized void startRecognition() {
        if (mRecognizer == null || COMMAND_SEARCH.equals(mRecognizer.getSearchName())) return;
        imgb_voice_click(null);
        /*
        new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME).startTone(ToneGenerator.TONE_CDMA_PIP, 200);
        post(400, new Runnable() {
            @Override
            public void run() {
                //mRecognizer.startListening(COMMAND_SEARCH, 3000);
                Log.d(TAG, "Listen commands");
                extCommand("hi");
                //post(4000, mStopRecognitionCallback);
            }
        });
        */
    }

    private synchronized void stopRecognition() {
        if (mRecognizer == null || KWS_SEARCH.equals(mRecognizer.getSearchName())) return;
        mRecognizer.stop();
        voiceKeywordWorking=false;
    }    

    @Override
    public void onSensorChanged(SensorEvent event) {
    	if (!voiceProximityEnable) return;
        mSensorValue = event.values[0];
        if (mSensorValue < mSensorMaximum) {
            post(500, new Runnable() {
                @Override
                public void run() {
                    if ((mSensorValue < mSensorMaximum)) {
                        //startRecognition();
                    	imgb_voice_click(null);
                    }
                }
            });
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

    private void post(long delay, Runnable task) {
        mHandler.postDelayed(task, delay);
    }

    private void process(final String text) {
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
			
		case R.id.action_videorecord:
			extCommand("videomessage");
			return true;			

		case R.id.action_quit:
		    timer.cancel();
		    if (mRecognizer != null) {
		    	mRecognizer.stop();
		    	mRecognizer.cancel();
		    }
		    		    
			finish();
			return true;
			
		case R.id.action_settings:
			Intent st = new Intent(this, Prefs.class);
			startActivity(st);
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		int goToHome=0;
		
		delayHandler.removeCallbacksAndMessages(null);

		if (requestCode == REQUEST_CODE_VOICE) {
			voiceGoogleInProgress=false;
		}
		
		if (requestCode == REQUEST_CODE_VOICE && resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			voiceCommand(matches.get(0));
		}

		
		if (requestCode == REQUEST_CODE_MESSAGE && resultCode == RESULT_OK) {
			Log.d(TAG, "Got REQUEST_CODE_MESSAGE");

			activityResultString = data.getStringExtra("filename");
			
			/*
			Toast toast = Toast.makeText(getApplicationContext(), "Got video message!\n"+activityResultString,
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			toast.show();
			*/
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 			 if (prefs.getString(getString(R.string.homeaftervideo), "off").equals("on")) {
			   goToHome=1;
			 }			
			
            dialog = ProgressDialog.show(MainActivity.this, "", "Uploading file...", true);
            
            new Thread(new Runnable() {
                    public void run() {

                        uploadFile(activityResultString);

                                                  
                    }
                  }).start();   			
			
		}
		
		if (requestCode == REQUEST_CODE_QR) {
			IntentResult scanResult = IntentIntegrator.parseActivityResult(
                    requestCode, resultCode, data);

			if (scanResult != null) {
                // handle scan result
                String contantsString = scanResult.getContents() == null ? "0"
                                : scanResult.getContents();
                
                if (contantsString.equalsIgnoreCase("0")) {
                        Toast.makeText(this, "No code scanned",
                                        Toast.LENGTH_LONG).show();

                } else {
                        Toast.makeText(this, contantsString, Toast.LENGTH_LONG).show();
                        if (contantsString.startsWith("http")) {
                        	mWebView.loadUrl(contantsString);
                        } else {
                        	mWebView.loadUrl("http://" + serverURL + pathQR + contantsString);
                        }
                }

        } else {
                Toast.makeText(this, "Problem to secan the barcode.",
                                Toast.LENGTH_LONG).show();
        }

		}		
		
		
		if (resultCode != RESULT_OK) {
			Log.d(TAG, "Activity finished (not OK)");	
		}
		
		if (voiceKeywordEnable && !voiceKeywordWorking) {
			voiceGoogleInProgress=false;
	        mRecognizer.cancel();
	        mRecognizer.startListening(KWS_SEARCH);
	        voiceKeywordWorking=true;
		}
		initiateFaceDetectionCamera();
		
		if (goToHome==1) {
	     imgb_home_click(null);
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * AsyncTask which handles the commiunication with clients
	 */
	 public class ServerAsyncTask extends AsyncTask<Socket, Void, String> {
	  //Background task which serve for the client
	  @Override
	  protected String doInBackground(Socket... params) {
	   String result = null;
	   //Get the accepted socket object
	   Socket mySocket = params[0];
	   try {
	    //Get the data input stream comming from the client
	    InputStream is = mySocket.getInputStream();
	    //Get the output stream to the client
	    PrintWriter out = new PrintWriter(mySocket.getOutputStream(), true);
	    //Write data to the data output stream
	    //out.println("Hello from server");
	    //Buffer the data input stream
	    BufferedReader br = new BufferedReader(
	      new InputStreamReader(is));
	    //Read the contents of the data buffer
	    result = br.readLine();
	    //Close the client connection
	    mySocket.close();
	   } catch (IOException e) {
	    e.printStackTrace();
	   }
	   return result;
	  }
	 
	  @Override
	  protected void onPostExecute(String s) {
	   //After finishing the execution of background task data will be write the text view
	   //tvClientMsg.setText(s);
		extCommand(s);	
	  }
	 }
	 
 
	
	public class MajorDroidWebViewer extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			

			
			if (url.startsWith("app://")) {
				
				Toast toast = Toast.makeText(getApplicationContext(),
						url, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.BOTTOM, 0, 0);
				toast.show();
				String cmd=url;
				cmd=cmd.replace("app://", "");
				extCommand(cmd);
			} else {
				view.loadUrl(url);
			}
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
    	Log.d(TAG, "Activity resume");    	
		
		super.onResume();
		loadHomePage(0);
		
		}	

	private void loadHomePage(int immediateLoad) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		localURL = prefs.getString(getString(R.string.localUrl), "");
		globalURL = prefs.getString(getString(R.string.globalUrl), "");
		pathHomepage = prefs.getString(getString(R.string.path_homepage), "");
		pathVoice = prefs.getString(getString(R.string.path_voice), "");
		pathQR = prefs.getString(getString(R.string.path_qr), "");
		pathGps = prefs.getString(getString(R.string.path_tracker), "");
		login = prefs.getString(getString(R.string.login), "");
		passw = prefs.getString(getString(R.string.passw), "");
		String dostup = prefs.getString(getString(R.string.dostup), "");
		String vid = prefs.getString(getString(R.string.vid), "");
		String wifiHomeNet = prefs.getString("wifihomenet", "");
		String wifiToast = "";
		TableLayout tl = (TableLayout)findViewById(R.id.homeTableLay);
		

		if (vid.contains("Обычный")) {
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			tl.setVisibility(View.VISIBLE);
		}		
		
		if (vid.contains("Полноэкранный")) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			tl.setVisibility(View.VISIBLE);
		}
		
		if (vid.contains("Полноэкранный (без панели кнопок)")) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            tl.setVisibility(View.GONE);
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
		
		uploadURL="http://" + serverURL + prefs.getString(getString(R.string.path_video), "");
		faceURL="http://" + serverURL + prefs.getString(getString(R.string.path_face), "");
		
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
				mWebView.loadUrl("http://" + serverURL + pathHomepage);
				
				// потом использовать reload();

				
				firstLoad = true;
				if (!serverURL.equals(tmpAdressAccess))
					toast.show();
				tmpAdressAccess = serverURL;
			}
		}
		gpsTimeOut = prefs.getString(getString(R.string.gps_period), "5");
		if ((prefs.getString(getString(R.string.gps_switch), "Выкл").equals("Вкл")) && (!timerOn)) {
			timer.schedule(doAsynchronousTask, 0,
					Long.parseLong(gpsTimeOut) * 60 * 1000);
			timerOn = true;
		} else if ((prefs.getString(getString(R.string.gps_switch), "Выкл").equals("Выкл")) && (timerOn)) {
			timer.cancel();
			timerOn = false;
		}
	
		if ((prefs.getString(getString(R.string.voice_proximity), "Выкл").equals("Вкл"))) {
			voiceProximityEnable=true;	
		} else {
			voiceProximityEnable=false;				
		}
		
		initiateFaceDetectionCamera();
		
    }
	
	private void initiateFaceDetectionCamera() {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (prefs.getString(getString(R.string.facedetection), "off").equals("on")) {
			faceDetectionEnable=true;
			if (!faceDetectionWorking) {
			 int cameraId=0;
			 if (prefs.getString(getString(R.string.facecamera_switch), "0").equals("1")) {
			  cameraId=getFrontFacingCameraId();
			 } else {
			  cameraId=getBackFacingCameraId();
			 }
			 Log.d(TAG,"Initiating camera "+Integer.toString(cameraId));
			 mCamera = Camera.open(cameraId);
			 cameraSurface = new SurfaceView(this);
			 if (mCamera.getParameters().getMaxNumDetectedFaces()>0) {
				 try {
					mCamera.setPreviewDisplay(cameraSurface.getHolder());
				} catch (IOException e) {
					e.printStackTrace();
				}
				 mCamera.setFaceDetectionListener(faceDetectionListener);
				 mCamera.startPreview();
				 mCamera.startFaceDetection();
				 Log.d(TAG,"Face detection started for camera "+Integer.toString(cameraId));
 				 faceDetectionWorking=true;
			 } else {
				Toast.makeText(this, "Face detection is not supported for camera "+Integer.toString(cameraId), Toast.LENGTH_SHORT).show();				 
				Log.d(TAG,"Face detection is not supported for camera "+Integer.toString(cameraId));
				faceDetectionWorking=false;
				faceDetectionEnable=false;
				mCamera.release();
			 }
			}
		} else {
			Log.d(TAG,"Facedetection disabled"); 
			faceDetectionEnable=false;
		    turnOffFaceDetectionCamera();
		}		
		
	}
	
	private void turnOffFaceDetectionCamera() {
		if (faceDetectionWorking) { 
		 mCamera.setFaceDetectionListener(null);
		 mCamera.setErrorCallback(null);
		 mCamera.stopPreview();
		 mCamera.release();
		 faceDetectionWorking=false;
		 Log.d(TAG,"Facedetection stopped");
		}
	}

	private void extCommand(String command) {

		
		if (command.equals("hi") || command.equals("voice")) {
			imgb_voice_click(null);	
		}
		if (command.equals("settings")) {
			imgb_settings_click(null);	
		}		
		if (command.equals("home")) {
			imgb_home_click(null);	
		}		
		if (command.equals("pult")) {
			imgb_pult_click(null);	
		}
		if (command.equals("videomessage")) {
			if (voiceKeywordWorking) {
		        mRecognizer.cancel();
		        mRecognizer.stop();
		        voiceKeywordWorking=false;
			}
			turnOffFaceDetectionCamera();
			Intent vr = new Intent(this, VideoRecordActivity.class);
			startActivityForResult(vr, REQUEST_CODE_MESSAGE);
		}
		if (command.equals("qrscan")) {
			imgb_qr_click(null);
		}		

		if (command.startsWith("url:")) {
			String url=command;
			url=url.replace("url:", "");
			mWebView.loadUrl(url);
		}
		
		if (command.equals("pause")) {
			if (mediaPlayer.isPlaying())
                mediaPlayer.pause();
		}
		
		if (command.startsWith("play:")) {
			String url=command;
			url=url.replace("play:", "");
			try {
				mediaPlayer.setDataSource(url);
				mediaPlayer.prepare();
				mediaPlayer.start();				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}			
		
		Toast toast = Toast.makeText(getApplicationContext(),
				command, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM, 0, 0);
		toast.show();		
	}
	
	private void voiceCommand(String command) {

		disableFacePost=true;
		webPost.loadUrl("http://" + serverURL + pathVoice + command);


   		Toast toast = Toast.makeText(getApplicationContext(), command,
				Toast.LENGTH_LONG);
		toast.setGravity(Gravity.BOTTOM, 0, 0);
		toast.show();
		
		new android.os.Handler().postDelayed(
			    new Runnable() {
			        public void run() {
			        	disableFacePost=false;
			        }
			    }, 
			3000);		

		
/*		
	    try{
	           HttpClient httpclient = new DefaultHttpClient();
	           HttpGet request = new HttpGet();
	           String authorizationString = "Basic " + Base64.encodeToString((login + ":" + passw).getBytes(),Base64.NO_WRAP);	           
	           request.setHeader("Authorization", authorizationString);
	           URI website = new URI("http://" + serverURL + pathVoice + URLEncoder.encode(command,"UTF-8"));                     
	           request.setURI(website);	                     	           
	           httpclient.execute(request);
	       }catch(Exception e){
	           Log.e(TAG, "Error in http connection "+e.toString());
	       }
	*/	
		
	}

	public void imgb_home_click(View v) {
		loadHomePage(1);
	}

	 private int getFrontFacingCameraId() {
		    int cameraCount = 0;
		    int cam = 0;
		    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		    cameraCount = Camera.getNumberOfCameras();
		    for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
		        Camera.getCameraInfo(camIdx, cameraInfo);
		        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
		            try {
		                cam = camIdx;
		            } catch (RuntimeException e) {
		                Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
		            }
		        }
		    }
		    return cam;
		}
	 
	 private int getBackFacingCameraId() {
		    int cameraCount = 0;
		    int cam = 0;
		    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		    cameraCount = Camera.getNumberOfCameras();
		    for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
		        Camera.getCameraInfo(camIdx, cameraInfo);
		        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
		            try {
		                cam = camIdx;
		            } catch (RuntimeException e) {
		                Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
		            }
		        }
		    }
		    return cam;
		}		 
	
	public void imgb_qr_click(View v) {
		   if (voiceKeywordWorking) {
		        mRecognizer.cancel();
		        mRecognizer.stop();
		        voiceKeywordWorking=false;
		   }
		   turnOffFaceDetectionCamera();
		
		   IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
		
		   SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);		   
		   qrCameraSet=prefs.getString(getString(R.string.qrcamera_switch), "0");			   
		   if (!qrCameraSet.equals("0")) {
			 Log.d(TAG,"Searching front qr camera "+qrCameraSet);
			 int camID=getFrontFacingCameraId();
			 Log.d(TAG,"Camera qr found "+Integer.toString(camID));
        	 integrator.setCameraId(Integer.toString(camID));
		   } else {
 		     Log.d(TAG,"Searching back qr camera "+qrCameraSet);
			 int camID=getBackFacingCameraId();
			 Log.d(TAG,"Camera qr found "+Integer.toString(camID));
	         integrator.setCameraId(Integer.toString(camID));
		   }
		   
           integrator.initiateScan();
           		   
		   
	}
	
	public void imgb_voice_click(View v) {
		
		if (voiceGoogleInProgress) return;
		
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Голосовой движок не установлен", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			toast.show();
		} else {

			if (voiceKeywordWorking) {
		        mRecognizer.cancel();
		        mRecognizer.stop();
		        voiceKeywordWorking=false;
			}
			turnOffFaceDetectionCamera();

			voiceGoogleInProgress=true;
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите...");
			intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());			
			startActivityForResult(intent, REQUEST_CODE_VOICE);
			

			delayHandler.postDelayed(
				    new Runnable() {
				        public void run() {
				        	if (voiceGoogleInProgress) {
				             finishActivity(REQUEST_CODE_VOICE);
				             voiceGoogleInProgress=false;
				             if (voiceKeywordEnable) {
			                  mRecognizer.cancel();
			                  mRecognizer.startListening(KWS_SEARCH);
			            	  voiceKeywordWorking=true;			                  
				             }
				             initiateFaceDetectionCamera();
				        	}
				        }
				    }, 
			    VOICE_INPUT_TIMIOUT_MILLIS);			
			
		}
	}

	public void imgb_pult_click(View v) {
		Intent j = new Intent(this, ControsActivity.class);
		startActivity(j);
		gpsSend();
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

	private void gpsSend() {
		Intent batteryIntent = registerReceiver(null, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		double latitude = 0, longitude = 0, altitude = 0, speed = 0, accuracy = 0;
		String provider = "";
		LocationManager mlocManager = null;
		LocationListener mlocListener;
		mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mlocListener = new MyLocationListener();
		Criteria criteria = new Criteria();
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
		mlocManager.requestLocationUpdates(mlocManager.getBestProvider(criteria, true), 10000, 0,
				mlocListener);

//		if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			if (MyLocationListener.latitude > 0) {
				latitude = MyLocationListener.latitude;
				longitude = MyLocationListener.longitude;
				altitude = MyLocationListener.altitude;
				speed = MyLocationListener.speed;
				accuracy = MyLocationListener.accuracy;				
				provider = MyLocationListener.provider;
			}
//		} 
//		else {
//			if (mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//				if (MyLocationListener.latitude > 0) {
//					latitude = MyLocationListener.latitude;
//					longitude = MyLocationListener.longitude;
//					altitude = MyLocationListener.altitude;
//					speed = MyLocationListener.speed;
//					accuracy = MyLocationListener.accuracy;				
//					provider = MyLocationListener.provider;
//				}
//			}
//		}

		String deviceid = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);
		String battlevel = Integer.toString(batteryIntent.getIntExtra(
				BatteryManager.EXTRA_LEVEL, -1));
		String gpsUrl = "http://" + serverURL + pathGps + "?";

		if (latitude != 0)
			gpsUrl += "latitude=" + latitude + "&";
		if (longitude != 0)
			gpsUrl += "longitude=" + longitude + "&";
		if (altitude != 0)
			gpsUrl += "altitude=" + altitude + "&";
		if (provider != "")
			gpsUrl += "provider=" + provider + "&";
		if (speed != 0)
			gpsUrl += "speed=" + speed + "&";
		if (battlevel != "")
			gpsUrl += "battlevel=" + battlevel + "&";
		if (deviceid != "")
			gpsUrl += "deviceid=" + deviceid + "&";
		if (accuracy != 0)
			gpsUrl += "accuracy=" + accuracy + "&";
		
		if (serverURL!="")
			webPost.loadUrl(gpsUrl);
	}

	public static class MyLocationListener implements LocationListener {

		public static double latitude = 0;
		public static double longitude = 0;
		public static double altitude = 0;
		public static double speed = 0;
		public static double accuracy = 0;
		public static String provider = "";

		@Override
		public void onLocationChanged(Location loc) {
			loc.getLatitude();
			loc.getLongitude();
			latitude = loc.getLatitude();
			longitude = loc.getLongitude();
			altitude = loc.getAltitude();
			speed = loc.getSpeed();
			accuracy = loc.getAccuracy();
			provider = loc.getProvider();
		}

		@Override
		public void onProviderDisabled(String provider) {
			// print "Currently GPS is Disabled";
		}

		@Override
		public void onProviderEnabled(String provider) {
			// print "GPS got Enabled";
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}
	}
	
	public int uploadFile(String sourceFileUri) {
        
        
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null; 
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
         
        if (!sourceFile.isFile()) {
             
             dialog.dismiss();
              
             Log.e("uploadFile", "Source File not exist :"
                                 +sourceFileUri);
             
              
             return 0;
          
        }
        else
        {
             try {
                  
                   // open a URL connection to the Servlet
                 FileInputStream fileInputStream = new FileInputStream(sourceFile);
                 URL url = new URL(uploadURL);
                  
                 // Open a HTTP  connection to  the URL
                 conn = (HttpURLConnection) url.openConnection();
                 conn.setDoInput(true); // Allow Inputs
                 conn.setDoOutput(true); // Allow Outputs
                 conn.setUseCaches(false); // Don't use a Cached Copy
                 conn.setRequestMethod("POST");
                 conn.setRequestProperty("Connection", "Keep-Alive");
                 conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                 conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                 conn.setRequestProperty("uploaded_file", fileName);
                  
                 dos = new DataOutputStream(conn.getOutputStream());
        
                 dos.writeBytes(twoHyphens + boundary + lineEnd);
                 dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                                           + fileName + "\"" + lineEnd);
                  
                 dos.writeBytes(lineEnd);
        
                 // create a buffer of  maximum size
                 bytesAvailable = fileInputStream.available();
        
                 bufferSize = Math.min(bytesAvailable, maxBufferSize);
                 buffer = new byte[bufferSize];
        
                 // read file and write it into form...
                 bytesRead = fileInputStream.read(buffer, 0, bufferSize); 
                    
                 while (bytesRead > 0) {
                      
                   dos.write(buffer, 0, bufferSize);
                   bytesAvailable = fileInputStream.available();
                   bufferSize = Math.min(bytesAvailable, maxBufferSize);
                   bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
                    
                  }
        
                 // send multipart form data necesssary after file data...
                 dos.writeBytes(lineEnd);
                 dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
        
                 // Responses from the server (code and message)
                 serverResponseCode = conn.getResponseCode();
                 String serverResponseMessage = conn.getResponseMessage();
                   
                 Log.i(TAG, "HTTP Response is : "
                         + serverResponseMessage + ": " + serverResponseCode);
                  
                 if(serverResponseCode == 200){
                      
                     runOnUiThread(new Runnable() {
                          public void run() {
                               
                              String msg = "File Upload Completed.";
                               
                              Toast.makeText(MainActivity.this, "File Upload Complete.",
                                           Toast.LENGTH_SHORT).show();
                          }
                      });               
                 }   
                  
                 //close the streams //
                 fileInputStream.close();
                 dos.flush();
                 dos.close();
                 
                 File file = new File(sourceFileUri);
                 boolean deleted = file.delete();
                   
            } catch (MalformedURLException ex) {
                 
                dialog.dismiss(); 
                ex.printStackTrace();
                 
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "MalformedURLException",
                                                            Toast.LENGTH_SHORT).show();
                    }
                });
                 
                Log.e(TAG, "error: " + ex.getMessage(), ex); 
            } catch (Exception e) {
                 
                dialog.dismiss(); 
                e.printStackTrace();
                 
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e(TAG, "Exception : "
                                                 + e.getMessage(), e); 
            }
            dialog.dismiss();      
            return serverResponseCode;
             
         } // End else block
       } 	
	
}
/*
 * На будущее: 1. Использовать reload(); при обновлении браузера 2. Использовать
 * окно браузера для вывода возможных ошибок, вот так String summary =
 * "<html><body>You scored <b>192</b> points.</body></html>";
 * webview.loadData(summary, "text/html", null);
 */