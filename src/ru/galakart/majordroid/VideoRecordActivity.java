package ru.galakart.majordroid;

import java.io.File;
import java.io.IOException;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



public class VideoRecordActivity extends Activity implements SurfaceHolder.Callback, MediaRecorder.OnInfoListener {
	private static final String TAG = MainActivity.class.getSimpleName();
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    public MediaRecorder mrec = new MediaRecorder();
    //private Button stopRecording = null;
    private boolean inProgress = false;
    File video;
    private Camera mCamera;
    private CountDownTimer timer1;
	private int cntTimer=0;
	private String filePath;
	private String msgCameraSet;
	private String msgCameraTurn;
	private String msgVideoTurn;
	private String msgVideoQuality;
	private String msgMaxLength;
	private String msgMaxSize;
	private int cameraIndex;
	int serverResponseCode = 0;
	long totalSize = 0;
	boolean faceCameraFound=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        	

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
          filePath=Environment.getExternalStorageDirectory()+"/video_message.mp4";        	
        } else {
          filePath=Environment.getDownloadCacheDirectory()+"/video_message.mp4";
        }

   	
    	
		setContentView(R.layout.activity_video_record);
        
        Log.i(TAG , "Video starting");
        
        final Button btnstartRecording = (Button)findViewById(R.id.buttonstart);
        btnstartRecording.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	if (inProgress) {
            		stopRecording();
            		
                	Intent dataRes = new Intent();
                	dataRes.putExtra("filename", filePath);
                	setResult(RESULT_OK, dataRes);
                	finish();            		
            		
            	} else {
            		startRecording();
            	}
            }
        });
        
        final Button btnCancel = (Button)findViewById(R.id.buttoncancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	if (inProgress) {
            		stopRecording();
            	}
            	Intent dataRes = new Intent();
            	setResult(RESULT_CANCELED, dataRes);
            	finish();
            }
        });        

		final TextView txtTimer= (TextView)findViewById(R.id.txtTimer);


		timer1 = new CountDownTimer( Long.MAX_VALUE , 1000) {

		        @Override
		        public void onTick(long millisUntilFinished) {

		        	cntTimer++;
	            
		               int seconds = cntTimer;
		               int minutes = seconds / 60;
		               seconds     = seconds % 60;

		               txtTimer.setText(String.format("%02d:%02d", minutes, seconds));

		        }

		        @Override
		        public void onFinish() {            }
		    };
		    
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		msgCameraSet=prefs.getString(getString(R.string.msgcamera_switch), "0");	    
		msgCameraTurn=prefs.getString(getString(R.string.turncamera_switch), "0");
		msgVideoTurn=prefs.getString(getString(R.string.turnvideo_switch), "0");
		msgMaxLength=prefs.getString(getString(R.string.maxlenght), "50");		
		msgMaxSize=prefs.getString(getString(R.string.maxsize), "50");
		msgVideoQuality=prefs.getString(getString(R.string.quality_switch), "Low");
		
        Log.v(TAG,"Camera set: "+msgCameraSet);
        
		if (!msgCameraSet.equals("0")) {
 		 cameraIndex=getFrontFacingCameraId();		
        } else {
         cameraIndex=getBackFacingCameraId();	
         faceCameraFound=false;        	
        }
        mCamera = Camera.open(cameraIndex);		
        
        if (!msgCameraTurn.equals("0")) {
            mCamera.setDisplayOrientation(Integer.parseInt(msgCameraTurn));        	
        }
        
        
        surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        //surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	
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
	
	   public void onInfo(MediaRecorder mr, int what, int extra) { 
		      if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
		         Log.v(TAG,"Maximum Duration Reached"); 
		         stopRecording();
        	     Intent dataRes = new Intent();
        	     dataRes.setData(Uri.parse(filePath));
        	     setResult(RESULT_OK, dataRes);
        	     finish();		         
		      }
		   }	
    
	private void resetTimer() {
		final TextView txtTimer= (TextView)findViewById(R.id.txtTimer);
		txtTimer.setText("00:00");
		timer1.cancel();
		cntTimer=0;
	}
	
	private void startTimer() {
		timer1.start();
	}
	   
    private void initRecorder() {

        mrec = new MediaRecorder();  // Works well
        mrec.setOnInfoListener(this);
        mCamera.unlock();
        mrec.setCamera(mCamera);
       
        if (!msgVideoTurn.equals("0")) {
         mrec.setOrientationHint(Integer.parseInt(msgVideoTurn));
        }
        
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC); 

        //
        if (msgVideoQuality.equals("High")) {
            mrec.setProfile(CamcorderProfile.get(cameraIndex,CamcorderProfile.QUALITY_HIGH));
        } else if (msgVideoQuality.equals("480P")) {
         mrec.setProfile(CamcorderProfile.get(cameraIndex,CamcorderProfile.QUALITY_480P));
        } else if (msgVideoQuality.equals("720P")) {
             mrec.setProfile(CamcorderProfile.get(cameraIndex,CamcorderProfile.QUALITY_720P));
        } else {
            mrec.setProfile(CamcorderProfile.get(cameraIndex,CamcorderProfile.QUALITY_LOW));        	
        }

        
        //mrec.setVideoFrameRate(25);
        
        mrec.setOutputFile(filePath);
        mrec.setMaxDuration(Integer.parseInt(msgMaxLength)*1000); // seconds
        mrec.setMaxFileSize(Integer.parseInt(msgMaxSize)*1024*1024); // megabytes
        
    }
    

    protected void startRecording() {
    	inProgress=true;
    	final Button startRecording = (Button)findViewById(R.id.buttonstart);
		startRecording.setText(R.string.btnRecordStopTitle);    	
    	Log.d(TAG, "Init recorder");
    	initRecorder();
    	Log.d(TAG, "Prepare recorder");
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        try {
        	mrec.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    	Log.d(TAG, "Start record");
    	startTimer();
        mrec.start();
    }

    protected void stopRecording() {
    	if (inProgress) {
   		 Log.d(TAG, "Stop recording");
         mrec.stop();
     	 Log.d(TAG, "Release recorder");
      	 mrec.reset();
     	 mrec.release();
         inProgress=false;   	    
    	}
    	final Button startRecording = (Button)findViewById(R.id.buttonstart);
		startRecording.setText(R.string.btnRecordStartTitle);
		resetTimer();
    	
   	 Log.d(TAG, "Finish");
   	 
    
    }
    

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera != null){
            Parameters params = mCamera.getParameters();
            mCamera.setParameters(params);
            try {
				mCamera.setPreviewDisplay(surfaceHolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
            mCamera.startPreview();
        }
        else {
            Toast.makeText(getApplicationContext(), "Camera not available!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
    }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		
	}	
	
   
	
    
}
