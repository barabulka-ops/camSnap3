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
	Camera camera;
	MediaRecorder mediaRecorder;

  private  PowerManager mPowerManager ;
   private  PowerManager.WakeLock mWakeLock;
   private int mWakeLockState = -1;

SurfaceHolder holder;  //moving from create

File photoFile;

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


       File pictures = Environment
           .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
       photoFile = new File(pictures, "debag.jpg");

        macro_surfaceStart();

////// остановка гашения экрана - режим затемнения................ 
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


////////////////////////////////////////////запуск камеры и превью
   void macro_surfaceStart()  {  
     	//	SurfaceHolder
        holder = surfaceView.getHolder();
              
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  //(настройка типа нужна только в Android версии ниже 3.0).  		
		holder.addCallback(new SurfaceHolder.Callback() {
				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					try {
				 		// mm.
                           camera.setPreviewDisplay(holder);

				 		//mm.
//		эксперименты с поворотом изображения				
                           camera.setDisplayOrientation(90); //0, 90, 180, and 270. тест

				 		//mm.
                           camera.startPreview();
    					text0.setText("Превью пошло");
					} catch (Exception e) {
						e.printStackTrace();
						text0.setText("Ой бляяя!!! КакаятоОшипка");
					}
				}	
                                @Override
				public void surfaceChanged(SurfaceHolder holder, int format,
										   int width, int height) {
				}

				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {

                      if (/* mm. */camera != null) {
                          // mm.camera.setPreviewCallback(null);
                      /* mm. */ camera.stopPreview();
				}
             }
			});

  }


   public int setPictureSize(int width, int height) { //установка siZe
     int ret=0;  
     
     if (camera == null) ret = -1; //камера не активирована
       else {
       Parameters params = camera.getParameters();
       params.setPictureSize(width,height ); 
       camera.setParameters(params);
       }
    return ret;   
   }// setpictsize


  public void getPicture() {
    camera.takePicture(null, null, new PictureCallback() {
      @Override
      public void onPictureTaken(byte[] data, Camera camera) {
        try {
          FileOutputStream fos = new FileOutputStream(photoFile);
          fos.write(data);
          fos.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
     camera.startPreview();
      }
    });

  }


int cameraPrevStat=1;


	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
 
			case R.id.btnSnap:

   //      гасим превью принудительно
  //  просто перекрываем другим окном
      
        if (camera != null)
        {
       Intent intent = new Intent(this, nilActivity.class);
      startActivity(intent);

				break;	
			case R.id.btnProccess:
 
 // забираем картинку 
       setPictureSize(640,480);
       getPicture();
				break;	
           }               
      }


 	@Override
	protected void onResume() {  //проверить, возможно организовать продолжение видеозаписи
		super.onResume();
		//mm.
           camera = Camera.open();		
	}

	@Override
	protected void onPause() {  //проверить, возможно организовать сохранение видеозаписи
		super.onPause();
//		mm.
               releaseMediaRecorder();
		if (/*mm.*/camera != null) 
    			/*mm.*/camera.release();
//		mm.
           camera = null;  

	}

	protected void onDestroy() {  // возобновить гашение экрана по закрытию программы
 
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}

    super.onDestroy();
  }

}//all
