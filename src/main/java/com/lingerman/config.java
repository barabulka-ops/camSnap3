// http://startandroid.ru/ru/uroki/vse-uroki-spiskom/73-urok-33-hranenie-dannyh-preferences.html

package com.lingerman;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;

import android.content.Intent;
import android.content.Context;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.widget.*;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.Toast;  //хуйня вроде алерт
 
public class config extends Activity implements OnClickListener {

 String baseconfig="baseconfig";

   TextView text0; 
   EditText etText;
   Button btnSave, btnLoad, btn3;
    SharedPreferences sPref;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.conf);
    //Log.d(TAG, "ActivityTwo: onCreate()");

        text0 = ( TextView)  findViewById(R.id.text0);

        etText = (EditText) findViewById(R.id.etText);
         
        btnSave = (Button) findViewById(R.id.btnSave);
           btnSave.setOnClickListener(this);
        btnLoad = (Button) findViewById(R.id.btnLoad);
           btnLoad.setOnClickListener(this);
        btn3 = (Button) findViewById(R.id.btn3);
           btn3.setOnClickListener(this);	
    }


  int rep=0;
 
  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.btnSave:
      if (rep==0) { rep++;
       text0.setText("tup save again for change path");           
         }
      else
       {  
       saveText("mysetup",baseconfig, etText.getText().toString() );
       rep=0;
       text0.setText("Now reload camSnap"); 
       }
      break;
    case R.id.btnLoad:
      loadText("mysetup",baseconfig, "");
      break;
      case R.id.btn3:
          Intent intent = new Intent();
          intent.putExtra("sndON", "ch");
 //    if (quitt.equals("qu") )
          setResult(RESULT_OK, intent);
          finish();     
      
      break;
   default:
      break;
    }
  }

  void saveText(String filename, String key, String name ) {
    if (filename==null)
       sPref = getPreferences(MODE_PRIVATE);
     else 
       sPref = getSharedPreferences(filename,MODE_PRIVATE);     
    Editor ed = sPref.edit();
    ed.putString(key,name );
    ed.commit();
 //   Toast.makeText(this, "Text saved", Toast.LENGTH_SHORT).show();
  }

  void loadText(String filename, String key, String ret ) { 
    if (filename==null)
       sPref = getPreferences(MODE_PRIVATE);
     else 
       sPref = getSharedPreferences(filename,MODE_PRIVATE);       
      
    String savedText = sPref.getString(key, "");
    etText.setText(savedText);
//    Toast.makeText(this, "Text loaded", Toast.LENGTH_SHORT).show();
  }

}//all
