package com.lingerman;

import android.app.*;
import android.os.*;

import java.util.*; 
import android.util.Log;

import android.content.*;//  .Intent;Context;

import android.view.*; 

public class nilActivity extends Activity {

      @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.nil);

//          Intent intent = new Intent();
 
 
//          setResult(RESULT_OK, intent);
//          finish();     
 
      }

@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
  boolean ret=false;

    switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_UP :  
        finish();
        break;
        }
        
 return super.onKeyDown(keyCode, event); //  возврат , если не обработан
  }
  
}
