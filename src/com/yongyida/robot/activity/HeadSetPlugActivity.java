package com.yongyida.robot.activity;
//package com.robotcontrol.activity;
//
//import android.app.Activity;
//import android.content.IntentFilter;
//import android.os.Bundle;
//
//import com.robotcontrol.service.HeadsetPlugReceiver;
//
//public class HeadSetPlugActivity extends Activity {  
//    
//    private HeadsetPlugReceiver headsetPlugReceiver;  
//    /** Called when the activity is first created. */  
//    @Override  
//    public void onCreate(Bundle savedInstanceState) {  
//        super.onCreate(savedInstanceState);  
//        setContentView(R.layout.headplug);  
//        
//          
//        /* register receiver */  
//        registerHeadsetPlugReceiver();           
//    }  
//  
//    private void registerHeadsetPlugReceiver() {  
//        headsetPlugReceiver = new HeadsetPlugReceiver();   
//        IntentFilter intentFilter = new IntentFilter();  
//        intentFilter.addAction("android.intent.action.HEADSET_PLUG");  
//        registerReceiver(headsetPlugReceiver, intentFilter);  
//    }  
//      
//    @Override  
//    public void onDestroy() {  
//        unregisterReceiver(headsetPlugReceiver);  
//        super.onDestroy();  
//    }       
//} 
