package com.yongyida.robot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ThreadPool;
import com.yongyida.robot.utils.ToastUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2016/11/28 0028.
 */

public class ControlMappingActivity extends OriginalActivity {
    private static final int PORT = 9999;
    private Socket mSocket;
    private EditText mIPEditText;
    private String mIP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_mapping);
        init();
    }

    private void init(){
        initView();
        initSocket();
    }

    private void initSocket() {
    }


    /**
     * 建立服务端连接
     */
    public void conn() {
        mIP = mIPEditText.getText().toString();
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = new Socket(mIP, PORT);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 发送消息
     */
    public void send(final String cmd) {
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    DataOutputStream writer = new DataOutputStream(mSocket.getOutputStream());
                    writer.write(cmd.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initView() {
        mIPEditText = (EditText) findViewById(R.id.et_ip);
        findViewById(R.id.bt_conn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conn();
            }
        });
        findViewById(R.id.bt_is_conn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSocket != null && mSocket.isConnected()) {
                    ToastUtil.showtomain(ControlMappingActivity.this, "yes");
                } else {
                    ToastUtil.showtomain(ControlMappingActivity.this, "no");
                }
            }
        });
        findViewById(R.id.bt_takephoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        findViewById(R.id.bt_opencamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
        findViewById(R.id.bt_closecamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCamera();
            }
        });
        findViewById(R.id.bt_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
        findViewById(R.id.bt_stepforward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepForward();
            }
        });
        findViewById(R.id.bt_rotate90).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotate90();
            }
        });
        findViewById(R.id.bt_rotate360).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotate360();
            }
        });
    }

//    /**
//     *
//     * @param argu  1表示拍照
//     */
//    private String takePhoto(int argu){
//        String cmd = "{\"cmd\":530,\"content\":{\"takephoto\":\"" + argu + "\"}}";
//        return cmd;
//    }

    private void takePhoto(){
        Intent intent = new Intent(Constants.MAPPING_ACTION);
        intent.putExtra(Constants.MAPPING_CMD, Constants.MAPPING_TAKE_PICTURES);
        sendBroadcast(intent);
    }

//    /**
//     *
//     * @param argu  1表示打开相机，0表示关闭相机
//     * @return
//     */
//    private String openCamera(int argu) {
//        String cmd = "{\"cmd\":532,\"content\":{\"opencamera\":\"" + argu + "\"}}";
//        return cmd;
//    }

    private void openCamera(){
        Intent intent = new Intent(Constants.MAPPING_ACTION);
        intent.putExtra(Constants.MAPPING_CMD, Constants.MAPPING_OPEN_CAMERA);
        sendBroadcast(intent);
    }

    private void closeCamera(){
        Intent intent = new Intent(Constants.MAPPING_ACTION);
        intent.putExtra(Constants.MAPPING_CMD, Constants.MAPPING_CLOSE_CAMERA);
        sendBroadcast(intent);
    }

//    /**
//     *
//     * @param argu  1表示停止底盘运动
//     * @return
//     */
//    private String stop(int argu) {
//        String cmd = "{\"cmd\":536,\"content\":{\"stop\":\"" + argu + "\"}}";
//        return cmd;
//    }

    private void stop(){
        Intent intent = new Intent(Constants.MAPPING_ACTION);
        intent.putExtra(Constants.MAPPING_CMD, Constants.MAPPING_STOP);
        sendBroadcast(intent);
    }


//    /**
//     *
//     * @param argu 1表示向前走30
//     * @return
//     */
//    private String stepForward(int argu) {
//        String cmd = "{\"cmd\":540,\"content\":{\"stepforward\":\"" + argu + "\"}}";
//        return cmd;
//    }

    private void stepForward(){
        Intent intent = new Intent(Constants.MAPPING_ACTION);
        intent.putExtra(Constants.MAPPING_CMD, Constants.MAPPING_STEP_FORWARD);
        sendBroadcast(intent);
    }

//    /**
//     *
//     * @param argu  1表示执行向左步进拍照，0表示不执行向左步进拍照
//     * @return
//     */
//    private String sLeftRotate90(int argu){
//        String cmd = "{\"cmd\":550,\"content\":{\"sleftrotate90\":\"" + argu + "\"}}";
//        return cmd;
//    }

    private void rotate90(){
        Intent intent = new Intent(Constants.MAPPING_ACTION);
        intent.putExtra(Constants.MAPPING_CMD, Constants.MAPPING_ROTATE_90);
        sendBroadcast(intent);
    }

//    /**
//     *
//     * @param argu   1表示执行向左步进拍照，0表示不执行向左步进拍照
//     * @return
//     */
//    private String sLeftRotate360(int argu) {
//        String cmd = "{\"cmd\":560,\"content\":{\"sleftrotate360\":\"" + argu + "\"}}";
//        return cmd;
//    }

    private void rotate360(){
        Intent intent = new Intent(Constants.MAPPING_ACTION);
        intent.putExtra(Constants.MAPPING_CMD, Constants.MAPPING_ROTATE_360);
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
