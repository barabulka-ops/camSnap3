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

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


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

  String basepath;

  String initConfig = 
   "photo_dir Lingerman \n" +
   "sleep  true  11000 \n" +
   "pictsize  640 480 \n" +
   "videoFormat  480  \n" +
   "videozize 320 200 \n" + 
   "zoom   0  # -1 not active \n" +

   "picFileNamepfx   mypic \n" +
   "vidFileNamepfx   mypic \n"  
    ; 

   File  saveDir , currentDir; // image catalog

   String picFileNamepfx = "mypic";
   String vidFileNamepfx = "myvid"; 


   int pictWidth=640,pictHeight=480,zoomvalue=-1; 

   int videoWidth=0,videoHeight=0,
          bitRate=0,VideoFrameRate=0;


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
        btn1 = (Button) findViewById(R.id.btn1);

		btnSnap.setOnClickListener(this);
		btnProccess.setOnClickListener(this);		 
		btnVideo.setOnClickListener(this);
		btn1.setOnClickListener(this);

/////// conf
     basepath = loadTextPrf("mysetup","baseconfig","");
     if (basepath==null ||  basepath.equals("") ) {
      //  Toast.makeText(this, "klakla", Toast.LENGTH_SHORT).show();
         File pictures = Environment
             .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); 
         basepath=pictures.getAbsolutePath();
         text1.setText(basepath);
         saveTextPrf("mysetup","baseconfig","thisdir");
          }
          else   text1.setText(basepath+ " bravo");  

      File def_path  , conf_path;     
      def_path = new File(basepath);
      conf_path = new File(def_path,"/baseconfig.txt");

      if ( ! conf_path. exists()) 
          extn.writeText(def_path.getAbsolutePath()+"/baseconfig.txt",initConfig,false);
         else {
           initConfig=null;
           initConfig=extn.readStringText(def_path.getAbsolutePath()+ "/baseconfig.txt"); 
          }

  
        String[] stringconfig=initConfig.split("\n");
 
       String[] params = mm.findParam ("photo_dir", stringconfig , true );  
        String str_saveDir;
        if (params==null) str_saveDir="Longer";
          else
          str_saveDir=params[1];
        saveDir = new File(def_path,str_saveDir);
        if (!saveDir.exists()) 
      	  {saveDir.mkdirs();
	       text0.setText("каталог "+str_saveDir+" создан");
           }	

       Date D = new Date( System.currentTimeMillis() );
       String tm = //D.getYear()+1900+"-"+
                  D.getMonth()+"-" + D.getDate()+"T"+     //D.getDay()+"T"+
                  D.getHours()+"_" + D.getMinutes()+"_"+D.getSeconds();      
       currentDir = new File(saveDir,tm) ;
         if(!currentDir.exists()) 
	 	  {
                currentDir.mkdirs();
	      //  text0.setText("каталог  создан");	
            }       

//       File pictures = Environment
//           .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
       photoFile = currentDir.getAbsolutePath();// new File(pictures, "debag.jpg");


       sleep_conf=false; 
       params=null;
       params = mm.findParam ("sleep", stringconfig , true );
       if (params != null) 
            if (params[1].equals("true") )  
              try {sleep_conf=true;
                   int i = Integer.parseInt(params[2]) ;
                   timeSleep_conf=i;
                   } catch (Exception e) {timeSleep_conf=30000;}


       params=null;
       params = mm.findParam ("pictsize", stringconfig , true );
       if (params != null) 
             try {int w = Integer.parseInt(params[1]) ;
                  int h = Integer.parseInt(params[2]) ;
                  pictWidth=w;pictHeight=h; 
                   } catch (Exception e) {}

       params=null;
       params = mm.findParam ("videoFormat", stringconfig , true );

/*
           params=null;
           params = mm.findParam ("videozize", stringconfig , true );
           if (params != null) 
                 try {int w = Integer.parseInt(params[1]) ;
                      int h = Integer.parseInt(params[2]) ;
                      videoWidth=w;videoHeigh=h; 
                       } catch (Exception e) {}
*/

       params=null;
       params = mm.findParam ("zoom", stringconfig , true );
       if (params != null) 
             try {int z = Integer.parseInt(params[1]) ;
                  if (z!=-1) zoomvalue=z;
                  } catch (Exception e) {}  



/////// end conf

		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        holder = surfaceView.getHolder();
        mm.macro_surfaceStart(holder);
 



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

///////// cont configuring with camera active
     File flatt = new File(saveDir,"flatten.txt");
     if (! flatt.exists() ) {  
        final File f = def_path; 
        Thread t = new Thread(new Runnable() {
            public void run() {
                while ( ! mm.cameraREADY )         //camera ready wait
                   try {
                     Thread.sleep(100);
                     } catch (InterruptedException ex) {}
            String flat =  mm.getAllParam(); 
            extn.writeText(f.getAbsolutePath()+"/flatten.txt" , flat,false); 
        }//run
      });
      t.start();
     }
 //       String flat =  mm.getAllParam();
 //       extn.writeText(def_path.getAbsolutePath()+"/flatten.txt" , flat,false); 


///// end conf

 
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
       mm.setPictureSize(pictWidth,pictHeight);
       if (zoomvalue!=-1) mm.setZoom(zoomvalue);
       mm.getPicture(true,photoFile+"/" + picFileNamepfx + indPhoto+".jpg");
       indPhoto++;
       becomeSleep(timeSleep_conf);
//					 am = (AudioManager) getSystemService(AUDIO_SERVICE); //попытка глушить затвор
//	                              	 am.setStreamMute(AudioManager.STREAM_SYSTEM, true);
   }
				break;	
			case R.id.btnProccess:
      if ( ! proccess  && ! videorecorded) {
       becomeSleep(-1);

       text1.setText("PROCCESSED");
       btnProccess.setText("PRO");
       if (zoomvalue!=-1) mm.setZoom(zoomvalue);
       mm.setPictureSize(pictWidth,pictHeight);
       proccessStart(1000,3800, picFileNamepfx);
       }
       else {
         proccess=false;
       text1.setText("stat");
       btnProccess.setText("start");          
       becomeSleep(timeSleep_conf);
   
 
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
			case R.id.btn1:
//             Toast.makeText(this, bpath, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, config.class);
        startActivity(intent);        
             break;

        }               
  }//onclick


     public boolean startVideo() {
         String filestring = /*currentDir.getAbsolutePath()*/ photoFile+"/" + vidFileNamepfx+indPhoto+".3gp";
		         return mm.startVideo(surfaceView , filestring);
		 
	 }
 

	public void stopVideo(){
           mm.stopVideo();
	}


////////////////////////////////////////                                 proccess timer  
  
  boolean  proccess=false;
  boolean   videorecorded=false;

  void proccessStart(int delay, int period, String filename) {
    final String fn = filename;  
    int dl=delay, per = period;
    proccess=true;
    new java.util.Timer().schedule(
        new TimerTask() {
             public void run() {
                   if (proccess)  {
                     mm.getPicture(true,photoFile+"/"+fn+indPhoto+".jpg");                     
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



  private void releaseMediaRecorder() {
    if (mediaRecorder != null) {
      mediaRecorder.reset();
      mediaRecorder.release();
      mediaRecorder = null;
      mm.camera.lock();
    }
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (data == null) {return;}
    switch (requestCode) {
       case 100 :   
  //          camera.startPreview();
       break;
     }//sw
    }

//////////////////////////////////////voids////////////////////////

    ////  add try   /// context -> extn \\\
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

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


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

  String basepath;

  String initConfig = 
   "photo_dir Lingerman \n" +
   "sleep  true  11000 \n" +
   "pictsize  640 480 \n" +
   "videoFormat  480  \n" +
   "videozize 320 200 \n" + 
   "zoom   0  # -1 not active \n" +

   "picFileNamepfx   mypic \n" +
   "vidFileNamepfx   mypic \n"  
    ; 

   File  saveDir , currentDir; // image catalog

   String picFileNamepfx = "mypic";
   String vidFileNamepfx = "myvid"; 


   int pictWidth=640,pictHeight=480,zoomvalue=-1; 

   int videoWidth=0,videoHeight=0,
          bitRate=0,VideoFrameRate=0;


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
        btn1 = (Button) findViewById(R.id.btn1);

		btnSnap.setOnClickListener(this);
		btnProccess.setOnClickListener(this);		 
		btnVideo.setOnClickListener(this);
		btn1.setOnClickListener(this);

/////// conf
     basepath = loadTextPrf("mysetup","baseconfig","");
     if (basepath==null ||  basepath.equals("") ) {
      //  Toast.makeText(this, "klakla", Toast.LENGTH_SHORT).show();
         File pictures = Environment
             .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); 
         basepath=pictures.getAbsolutePath();
         text1.setText(basepath);
         saveTextPrf("mysetup","baseconfig","thisdir");
          }
          else   text1.setText(basepath+ " bravo");  

      File def_path  , conf_path;     
      def_path = new File(basepath);
      conf_path = new File(def_path,"/baseconfig.txt");

      if ( ! conf_path. exists()) 
          extn.writeText(def_path.getAbsolutePath()+"/baseconfig.txt",initConfig,false);
         else {
           initConfig=null;
           initConfig=extn.readStringText(def_path.getAbsolutePath()+ "/baseconfig.txt"); 
          }

  
        String[] stringconfig=initConfig.split("\n");
 
       String[] params = mm.findParam ("photo_dir", stringconfig , true );  
        String str_saveDir;
        if (params==null) str_saveDir="Longer";
          else
          str_saveDir=params[1];
        saveDir = new File(def_path,str_saveDir);
        if (!saveDir.exists()) 
      	  {saveDir.mkdirs();
	       text0.setText("каталог "+str_saveDir+" создан");
           }	

       Date D = new Date( System.currentTimeMillis() );
       String tm = //D.getYear()+1900+"-"+
                  D.getMonth()+"-" + D.getDate()+"T"+     //D.getDay()+"T"+
                  D.getHours()+"_" + D.getMinutes()+"_"+D.getSeconds();      
       currentDir = new File(saveDir,tm) ;
         if(!currentDir.exists()) 
	 	  {
                currentDir.mkdirs();
	      //  text0.setText("каталог  создан");	
            }       

//       File pictures = Environment
//           .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
       photoFile = currentDir.getAbsolutePath();// new File(pictures, "debag.jpg");


       sleep_conf=false; 
       params=null;
       params = mm.findParam ("sleep", stringconfig , true );
       if (params != null) 
            if (params[1].equals("true") )  
              try {sleep_conf=true;
                   int i = Integer.parseInt(params[2]) ;
                   timeSleep_conf=i;
                   } catch (Exception e) {timeSleep_conf=30000;}


       params=null;
       params = mm.findParam ("pictsize", stringconfig , true );
       if (params != null) 
             try {int w = Integer.parseInt(params[1]) ;
                  int h = Integer.parseInt(params[2]) ;
                  pictWidth=w;pictHeight=h; 
                   } catch (Exception e) {}

       params=null;
       params = mm.findParam ("videoFormat", stringconfig , true );

/*
           params=null;
           params = mm.findParam ("videozize", stringconfig , true );
           if (params != null) 
                 try {int w = Integer.parseInt(params[1]) ;
                      int h = Integer.parseInt(params[2]) ;
                      videoWidth=w;videoHeigh=h; 
                       } catch (Exception e) {}
*/

       params=null;
       params = mm.findParam ("zoom", stringconfig , true );
       if (params != null) 
             try {int z = Integer.parseInt(params[1]) ;
                  if (z!=-1) zoomvalue=z;
                  } catch (Exception e) {}  



/////// end conf

		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        holder = surfaceView.getHolder();
        mm.macro_surfaceStart(holder);
 



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

///////// cont configuring with camera active
     File flatt = new File(saveDir,"flatten.txt");
     if (! flatt.exists() ) {  
        final File f = def_path; 
        Thread t = new Thread(new Runnable() {
            public void run() {
                while ( ! mm.cameraREADY )         //camera ready wait
                   try {
                     Thread.sleep(100);
                     } catch (InterruptedException ex) {}
            String flat =  mm.getAllParam(); 
            extn.writeText(f.getAbsolutePath()+"/flatten.txt" , flat,false); 
        }//run
      });
      t.start();
     }
 //       String flat =  mm.getAllParam();
 //       extn.writeText(def_path.getAbsolutePath()+"/flatten.txt" , flat,false); 


///// end conf

 
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
       mm.setPictureSize(pictWidth,pictHeight);
       if (zoomvalue!=-1) mm.setZoom(zoomvalue);
       mm.getPicture(true,photoFile+"/" + picFileNamepfx + indPhoto+".jpg");
       indPhoto++;
       becomeSleep(timeSleep_conf);
//					 am = (AudioManager) getSystemService(AUDIO_SERVICE); //попытка глушить затвор
//	                              	 am.setStreamMute(AudioManager.STREAM_SYSTEM, true);
   }
				break;	
			case R.id.btnProccess:
      if ( ! proccess  && ! videorecorded) {
       becomeSleep(-1);

       text1.setText("PROCCESSED");
       btnProccess.setText("PRO");
       if (zoomvalue!=-1) mm.setZoom(zoomvalue);
       mm.setPictureSize(pictWidth,pictHeight);
       proccessStart(1000,3800, picFileNamepfx);
       }
       else {
         proccess=false;
       text1.setText("stat");
       btnProccess.setText("start");          
       becomeSleep(timeSleep_conf);
   
 
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
			case R.id.btn1:
//             Toast.makeText(this, bpath, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, config.class);
        startActivity(intent);        
             break;

        }               
  }//onclick


     public boolean startVideo() {
         String filestring = /*currentDir.getAbsolutePath()*/ photoFile+"/" + vidFileNamepfx+indPhoto+".3gp";
		         return mm.startVideo(surfaceView , filestring);
		 
	 }
 

	public void stopVideo(){
           mm.stopVideo();
	}


////////////////////////////////////////                                 proccess timer  
  
  boolean  proccess=false;
  boolean   videorecorded=false;

  void proccessStart(int delay, int period, String filename) {
    final String fn = filename;  
    int dl=delay, per = period;
    proccess=true;
    new java.util.Timer().schedule(
        new TimerTask() {
             public void run() {
                   if (proccess)  {
                     mm.getPicture(true,photoFile+"/"+fn+indPhoto+".jpg");                     
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



  private void releaseMediaRecorder() {
    if (mediaRecorder != null) {
      mediaRecorder.reset();
      mediaRecorder.release();
      mediaRecorder = null;
      mm.camera.lock();
    }
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (data == null) {return;}
    switch (requestCode) {
       case 100 :   
  //          camera.startPreview();
       break;
     }//sw
    }

//////////////////////////////////////voids////////////////////////

    ////  add try   /// context -> extn \\\
   
  void saveTextPrf(String filename, String key, String name ) {
    SharedPreferences sPref; 
    if (filename==null)
       sPref = getPreferences(MODE_PRIVATE);
     else 
       sPref = getSharedPreferences(filename,MODE_PRIVATE);     
    Editor ed = sPref.edit();
    ed.putString(key,name );
    ed.commit();
  }

  String loadTextPrf(String filename, String key, String ret ) { 
    SharedPreferences sPref;
    if (filename==null)
       sPref = getPreferences(MODE_PRIVATE);
       else 
         sPref = getSharedPreferences(filename,MODE_PRIVATE);       
      
   String savedText = sPref.getString(key, ret);
   return savedText;
  }
    ////////

}//all
  void saveTextPrf(String filename, String key, String name ) {
    SharedPreferences sPref; 
    if (filename==null)
       sPref = getPreferences(MODE_PRIVATE);
     else 
       sPref = getSharedPreferences(filename,MODE_PRIVATE);     
    Editor ed = sPref.edit();
    ed.putString(key,name );
    ed.commit();
  }

  String loadTextPrf(String filename, String key, String ret ) { 
    SharedPreferences sPref;
    if (filename==null)
       sPref = getPreferences(MODE_PRIVATE);
       else 
         sPref = getSharedPreferences(filename,MODE_PRIVATE);       
      
   String savedText = sPref.getString(key, ret);
   return savedText;
  }
    ////////

}//all
