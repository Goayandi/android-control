package com.yongyida.robot.activity;

/**
 * Created by Administrator on 2016/12/22 0022.
 */

public class TestActivity extends OriginalActivity {

//    private Thread runner=null;
//    private AudioRecord mRecorder=null;
//    private int bufferSize = 0;
//
//    private  byte[] buffer =null;
//    private static boolean isStartRec=false;
//
//    private int curByte=0;
//    private byte[] frameByte=new byte[Contants.totalByte];
//    private byte[] frameByte2=new byte[Contants.totalByte];
//    private static boolean isDataChange=false;
//    private Thread mThread=null;
//    private final static String Tag="MainActivity";
//    private GoogleSpeechImpl mSpeechImpl=null;
//    private FileWriter mFileStorager;
//
//    Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.arg1){
////                case Contants.MSG_GET_SPEECH_SUCCESS:{
////                    String content=(String)msg.obj;
////                    Toast.makeText(MainActivity.this, "接收到的消息是:" + content, Toast.LENGTH_SHORT).show();
////                   isStartRec=false;
////                    mHandler.postDelayed(mRestartRunnable,800);
////                }break;
////                case Contants.MSG_SPEECH_RECYLE:{
////                    isStartRec=false;
////                }break;
////                case Contants.MSG_SPEECH_RESTART:{
////                    startRec();
////
////                }break;
//                case Contants.MSG_SEND_A_REQUEST:{
//                    frameByte2= (byte[]) msg.obj;
//                    mSpeechImpl.writeData(frameByte2,bufferSize);
//                }break;
//            }
//        }
//    };
//
//    Runnable mRestartRunnable=new Runnable() {
//        @Override
//        public void run() {
//            startRec();
//            mSpeechImpl.start();
//        }
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_test);
//        initView();
//        initGoogle();
//    }
//
//    private void initView() {
//        findViewById(R.id.bt_1).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startRec();
//                mSpeechImpl.start();
//                mFileStorager.open();
//            }
//        });
//        findViewById(R.id.bt_2).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isStartRec=false;
//                mSpeechImpl.stop();
//                mFileStorager.close();
//            }
//        });
//    }
//
//    private void initGoogle() {
//        initAccount();
//        initRec();
//        mFileStorager = new FileWriter();
//        mFileStorager.setFileName("/sdcard/test1.pcm");
//    }
//
//    private void startRec(){
//        if(runner==null || !runner.isAlive()){
//            runner = new Thread() {
//                public void run() {
//                    isStartRec=true;
//                    mRecorder.startRecording();
//                    curByte=0;
//                    while(isStartRec){
//                        if(mRecorder==null){
//                            isStartRec=false;
//                            break;
//                        }
//                        int recState=mRecorder.read(buffer,0,buffer.length);
//                        if(recState<=0){
//                            continue;
//                        }
////                      curByte+=bufferSize;
////                      if(curByte<Contants.totalByte){
////                          System.arraycopy(buffer,0,frameByte,curByte,bufferSize);
////                      }else {
//                        sendMsg(buffer);
////                          curByte=0;
////                          System.arraycopy(buffer,0,frameByte,curByte,bufferSize);
////                          }
//                    }
//                    mRecorder.stop();
//                }
//            };
//            runner.start();
//        }
//    }
//    public void sendMsg(byte[] data){
//        mFileStorager.write(data, 0, data.length);
//        Message msg=Message.obtain();
//        msg.arg1= Contants.MSG_SEND_A_REQUEST;
//        msg.obj=data;
//        mHandler.sendMessage(msg);
//
//    }
//
//    private void initAccount(){
//        mSpeechImpl=GoogleSpeechImpl.getInstance(TestActivity.this, new ISpeechCallBack() {
//            @Override
//            public void onError(Throwable error) {
//
//            }
//
//            @Override
//            public void onSuccess(String result) {
//                Log.e(Tag,"result:"+result);
//            }
//
//            @Override
//            public void onInit(boolean init) {
//                Log.e(Tag,"init success");
//            }
//
//        });
//    }
//
//    private void initRec(){
//        this.bufferSize = AudioRecord.getMinBufferSize(16000,
//                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
//        buffer= new byte[bufferSize];
//        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
//                16000,
//                AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT,
//                bufferSize);
//    }

}
