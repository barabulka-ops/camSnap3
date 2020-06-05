package com.lingerman;

import android.app.*;
import android.os.*;

import java.util.*;
import java.io.*; //FileOutputStream;File;FileWriter;FileReader;
 
import android.util.Log;

import android.content.*;//  .Intent;Context;

import android.view.*;  // SurfaceHolder; SurfaceView; WindowManager;
import android.view.View;
import android.view.View.*;
import android.widget.*;
 
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.CamcorderProfile;

import android.hardware.*; //Sensor;  .SensorEvent; .SensorEventListener; .SensorManager;
import android.hardware.Camera;
import android.hardware.Camera.*;
import android.hardware.Camera.PictureCallback;

import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;


public class MainActivity extends Activity implements OnClickListener{
   	static TextView text0;
	static TextView text1;
	 
	Button btnSnap; 
	Button btnProccess;
	Button btnVideo;
	Button btn1;

	SurfaceView surfaceView;

////	Camera camera;
	MediaRecorder mediaRecorder;

  private  PowerManager mPowerManager ;
   private  PowerManager.WakeLock mWakeLock;
   private int mWakeLockState = -1;

 SurfaceHolder holder;  //moving from create

// File
  String  photoFile;

  extn mm = new extn(); 

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
   	text0 = (TextView) findViewById(R.id.text0);
	text1 = (TextView) findViewById(R.id.text1);	
	
	btnSnap = (Button) findViewById(R.id.btnSnap); 
        btnProccess = (Button) findViewById(R.id.btnProccess);
        btnVideo = (Button) findViewById(R.id.btnVideo);

		btnSnap.setOnClickListener(this);
		btnProccess.setOnClickListener(this);		 
		btnVideo.setOnClickListener(this);
//		btn1.setOnClickListener(this);

		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        holder = surfaceView.getHolder();
        mm.macro_surfaceStart(holder);
 
       File pictures = Environment
           .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
       photoFile = pictures.getAbsolutePath();// new File(pictures, "debag.jpg");


    mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    mWakeLockState = PowerManager.SCREEN_DIM_WAKE_LOCK;//getWakeLockType("SCREEN_DIM_WAKE_LOCK"); //strLockState);  
    mWakeLock = mPowerManager.newWakeLock(mWakeLockState,
                        "UMSE PowerTest");
    if (mWakeLock != null) {
        mWakeLock.acquire();
	}

/*
PARTIAL_WAKE_LOCK	Вкл	Выкл	Выкл
SCREEN_DIM_WAKE_LOCK	Вкл	Затемнен	Выкл
SCREEN_BRIGHT_WAKE_LOCK	Вкл	Полная яркость	Выкл
FULL_WAKE_LOCK	Вкл	Полная яркость	Полная яркость
*/


    }//end create

int beforeStandby = 1000*30; //30 сек   
int standby = 0;  //  -1 -- ожидание не работает
boolean timeron=false;


int cameraPrevStat=1;
int indPhoto=0;

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
 
			case R.id.btnSnap:
      if ( ! proccess  && ! videorecorded) {
       mm.setPictureSize(640,480);
       mm.getPicture(true,photoFile+"/debag"+indPhoto+".jpg");
       indPhoto++;
       becomeSleep(timeSleep_conf);
//					 am = (AudioManager) getSystemService(AUDIO_SERVICE); //попытка глушить затвор
//	                              	 am.setStreamMute(AudioManager.STREAM_SYSTEM, true);
   }
				break;	
			case R.id.btnProccess:       //включает или выключает непрепывное фотографирование 
      if ( ! proccess  && ! videorecorded) { // каждые 3.8 sec
       becomeSleep(-1); // выключение режима спячки

       text1.setText("PROCCESSED");
       btnProccess.setText("PRO");

       mm.setPictureSize(640,480);
       proccessStart(1000,3800);
       }
       else {
         proccess=false;
       text1.setText("stat");
       btnProccess.setText("start");          
       becomeSleep(timeSleep_conf); //включение режима спячки
   
 
         }
				break;	
			case R.id.btnVideo:
   	if ( ! videorecorded && ! proccess) {//Если процесс не запущен и не пишется видео
/*
                                   am = (AudioManager) getSystemService(AUDIO_SERVICE); //разрешить звуки
                                   am.setStreamMute(AudioManager.STREAM_SYSTEM, false);
*/                 becomeSleep(-1);
				    indPhoto++;
				   videorecorded = startVideo();
				   text1.setText("RECORDDD");
				   btnVideo.setText("REC");
                  }
                  else
				    if (  videorecorded ) {
				       stopVideo();
				       videorecorded = false;
				       text1.setText("state");
				       btnVideo.setText("call");
                       becomeSleep(timeSleep_conf);    
                     }
             break;



        }               
  }//onclick


     public boolean startVideo() {
         String filestring = /*currentDir.getAbsolutePath()*/ photoFile+"/" + "myvideo"+indPhoto+".3gp";
		         return mm.startVideo(surfaceView , filestring);
		 
	 }
 

	public void stopVideo(){
           mm.stopVideo();
	}


////////////////////////////////////////                                 proccess timer  
  
  boolean  proccess=false;
  boolean   videorecorded=false;

  void proccessStart(int delay, int period) {
//    final 
    int dl=delay, per = period;
    proccess=true;
    new java.util.Timer().schedule(
        new TimerTask() {
             public void run() {
                   if (proccess)  {
                     mm.getPicture(true,photoFile+"/debag"+indPhoto+".jpg");                     
                    indPhoto++;
                    }
                    else cancel();
              }
        }, dl , per );
 }



///////////////////////////////////////////////////proccess



////////////////////////////////////////                                 sleep timer
   boolean sleep_conf=true;   //запрещает разрешает  sleep гашение превью
   int timeSleep_conf=11*1000; 
   
   Timer timerSleep;
   TimerTask taskSleep;  

   boolean sleepTimerHalt=false;
   boolean proccessSleep = false;  
   int countToSleep=0;
   int timerSleepInterval=3000;

  void becomeSleep(int param) {   
   if (sleep_conf) {
       if (param<0)                   // отмена подготовка ко сну
            sleepTimerHalt=true;
         else
       if (proccessSleep) countToSleep=0;  // просто обнуляем счетчик
         else {
          sleepTimerHalt=false;
          proccessSleep = true;
          countToSleep=0;    

          final Intent intent = new Intent(this, nilActivity.class);
          final int par = param/timerSleepInterval;

           timerSleep = new Timer();
           taskSleep = new TimerTask() {
   
              @Override
              public void run() {
                if (! sleepTimerHalt)
                   if ( countToSleep<par) countToSleep++;    //   {
                      else{
                       proccessSleep = false;
                       startActivity(intent);
                        cancel();
                       }
                  else {
                     proccessSleep = false;                
                     cancel();
                    }
         }//endrun
     };
      timerSleep.schedule(taskSleep, 0 , timerSleepInterval );   
     }//else
 }//sleepconf
}
///////////////////////////////////////////////////////////////////////////////

            //  startActivityForResult(intent, 100); //startActivity(intent);

 	@Override
	protected void onResume() {  //проверить, возможно организовать продолжение видеозаписи
		super.onResume();
		mm.camera = Camera.open();		
	}

	@Override
	protected void onPause() {  //проверить, возможно организовать сохранение видеозаписи
		super.onPause();
//		mm.releaseMediaRecorder();
		if (mm.camera != null) 
    			mm.camera.release();
		mm.camera = null;  
	}


	protected void onDestroy() {
 
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}

    super.onDestroy();
  }


}
