package com.lingerman;

import android.app.*;
import android.os.*;

import android.view.*;  // SurfaceHolder; SurfaceView; WindowManager;
import java.util.*;
import java.io.*; //FileOutputStream;File;FileWriter;FileReader;
 
import android.util.*;

import android.media.*;// CamcorderProfile;
import android.hardware.*; // Camera. Sensor;  .SensorEvent; .SensorEventListener; .SensorManager;
import android.hardware.Camera.*; //PictureCallback;




public class extn {

  public int Error = 0;
  public String ErrorString=null;


	Camera camera;
	MediaRecorder mediaRecorder;



    void macro_surfaceStart(SurfaceHolder  holder)  {
     	//	SurfaceHolder
//        holder = surfaceView.getHolder();
              
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  //(настройка типа нужна только в Android версии ниже 3.0).  		
		holder.addCallback(new SurfaceHolder.Callback() {
				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					try {
                           camera.setPreviewDisplay(holder);

                           camera.setDisplayOrientation(90); //0, 90, 180, and 270. тест

                           camera.startPreview();

					} catch (Exception e) {
						e.printStackTrace();
                        Error =1;
						ErrorString="preview error ";
					}
				}	
                                @Override
				public void surfaceChanged(SurfaceHolder holder, int format,
										   int width, int height) {
				}

				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {

                      if ( camera != null) {
                          // mm.camera.setPreviewCallback(null);
                        camera.stopPreview();
				    }
                }
			  });
    }   

   
     public int setPictureSize(int width, int height) {
       int ret=0;  
     
       if (camera == null) ret = -1; //камера не активирована
       else {
         Parameters params = camera.getParameters();
         params.setPictureSize(width,height ); 
         camera.setParameters(params);
       }
      return ret;   
    }// setpictsize   


   public int setVideoSize(int width, int height) {
     int ret=0;  
     
     if (camera == null) ret = -1; //камера не активирована
       else {
           videoWidth=width ;   videoHeight= height;
       }
    return ret;   
   }// setvidsize

  
  public int videoWidth=640,videoHeight=480;
  public int bitRate=0 , 
  VideoFrameRate=15;

   public String videoFormat="480";//null;


  public int  getPicture(boolean autofocus, String pathAndName) {
    int ret=0;
    final File photoFile = new File(pathAndName); 
    camera.takePicture(null, null, new PictureCallback() {
      @Override
      public void onPictureTaken(byte[] data, Camera camera) {
        try {
          FileOutputStream fos = new FileOutputStream(photoFile);
          fos.write(data);
          fos.close();
          } catch (Exception e) {
                e.printStackTrace();
    	        Error=2; //ret=-1;//
		        ErrorString="какаято: ошибка файловой системы";
               }
      camera.startPreview();
      }
    });

    return ret;
  } //endgetpict



//////////////////////////////////////////video


	public boolean prepareVideoRecorder(SurfaceView surfaceView, String filename, String frm) {

		File videoFile = new File(filename);
		camera.unlock();
		mediaRecorder = new MediaRecorder();
		mediaRecorder.setCamera(camera);
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        int quality = CamcorderProfile.QUALITY_CIF; 	
        if ( ! frm.equals("old") ) {
           if ( frm.equals("high") ) quality = CamcorderProfile.QUALITY_HIGH;
           else
          if ( frm.equals("480") ) quality = CamcorderProfile.QUALITY_480P;              
           else            
          if ( frm.equals("720") ) quality = CamcorderProfile.QUALITY_720P;
           else            
          if ( frm.equals("low") ) quality = CamcorderProfile.QUALITY_LOW;

    	  mediaRecorder.setProfile(CamcorderProfile.get(quality) );
           }

////***************************for normal phone
								      //QUALITY_CIF));
	//https://developer.android.com/reference/android/media/CamcorderProfile

       else
      {
      //****************************for oldphone

      mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 20.6.18  prev  DEFAULT);
                                                              //   DEFAULT);
                                                             //THREE_GPP);
                                                             //MPEG_4);  //tmp
							     
    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);//tmp
    mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);//DEFAULT);//tmp  !!!!!!

    if (bitRate!=0)  mediaRecorder.setVideoEncodingBitRate (bitRate );//        2022  onnokia
    mediaRecorder.setVideoFrameRate(VideoFrameRate);//15
     mediaRecorder.setVideoSize(videoWidth,videoHeight);//480, 320);  480 800  brate 2933
   //*************************************************************************  old phone
  }  //end else


///  look https://developer.android.com/guide/topics/media/camera

		mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
		mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());

		try {
			mediaRecorder.prepare();
		} catch (Exception e) {
			e.printStackTrace();
			releaseMediaRecorder();
			return false;
		}
		return true;
	}


   	public void releaseMediaRecorder() {
		if (mediaRecorder != null) {
			mediaRecorder.reset();
			mediaRecorder.release();
			mediaRecorder = null;
			camera.lock();
		}
	}


	public void stopVideo(){
		if (mediaRecorder != null) {
			mediaRecorder.stop();
			releaseMediaRecorder();
		//	setString("vidstoP");
		}
	}

     public boolean startVideo(SurfaceView surfaceView, String filename) {
		 if (prepareVideoRecorder(surfaceView, filename,videoFormat)) {
			 mediaRecorder.start();
		         return true;
		 } else {
			 releaseMediaRecorder();
		         return false;
		 }
	 }
  
///////////////////////////////////////////

}  
