package com.yongyida.robot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.Constants;

/**
 * Created by Administrator on 2016/11/28 0028.
 */

public class ControlMappingActivity extends OriginalActivity {

    private static final String TAG = "ControlMappingActivity";
    private TextView mCameraTV;
    private ImageView mCameraIV;
    private ImageView mTakePhotoIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_mapping);
        init();
    }

    private void init(){
        initView();
    }



    private void handlCamera() {
        if (getString(R.string.open_camera).equals(mCameraTV.getText().toString())) {
            mCameraTV.setText(getString(R.string.close_camera));
            mCameraIV.setImageResource(R.drawable.mapping_gbpz);
            mTakePhotoIV.setImageResource(R.drawable.mapping_pz);
            mTakePhotoIV.setEnabled(true);
            openCamera();
        } else {
            mCameraTV.setText(getString(R.string.open_camera));
            mCameraIV.setImageResource(R.drawable.mapping_dkxj);
            mTakePhotoIV.setImageResource(R.drawable.mapping_pz_nor);
            mTakePhotoIV.setEnabled(false);
            closeCamera();
        }

    }

    private void exit(){
        if (getString(R.string.close_camera).equals(mCameraTV.getText().toString())) {
            Toast.makeText(this, "请先关闭相机", Toast.LENGTH_LONG).show();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    private void initView() {
        mCameraTV = (TextView) findViewById(R.id.tv_opencamera);
        mCameraIV = (ImageView) findViewById(R.id.iv_opencamera);
        mTakePhotoIV = (ImageView) findViewById(R.id.iv_takephoto);

        findViewById(R.id.bt_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });
        findViewById(R.id.iv_opencamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlCamera();
            }

        });

        mTakePhotoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        mTakePhotoIV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (getString(R.string.close_camera).equals(mCameraTV.getText().toString())) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.setAlpha(0.3f);
                    } else if (event.getAction() == MotionEvent.ACTION_UP){
                        v.setAlpha(1f);
                    }
                }
                return false;
            }
        });

        findViewById(R.id.iv_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
        findViewById(R.id.iv_stop).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.3f);
                } else if (event.getAction() == MotionEvent.ACTION_UP){
                    v.setAlpha(1f);
                }
                return false;
            }
        });

        findViewById(R.id.iv_stepforward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepForward();
            }
        });
        findViewById(R.id.iv_stepforward).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.3f);
                } else if (event.getAction() == MotionEvent.ACTION_UP){
                    v.setAlpha(1f);
                }
                return false;
            }
        });

        findViewById(R.id.iv_rotate90).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotate90();
            }
        });
        findViewById(R.id.iv_rotate90).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.3f);
                } else if (event.getAction() == MotionEvent.ACTION_UP){
                    v.setAlpha(1f);
                }
                return false;
            }
        });

        findViewById(R.id.iv_rotate360).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotate360();
            }
        });
        findViewById(R.id.iv_rotate360).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.3f);
                } else if (event.getAction() == MotionEvent.ACTION_UP){
                    v.setAlpha(1f);
                }
                return false;
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
        Log.d(TAG, "stop");
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
    }
}
