package com.yongyida.robot.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yongyida.robot.R;

/**
 * Created by Administrator on 2016/6/13 0013.
 */
public class ScreenshotDialog extends Dialog {
    private Window window;
    private Context context;

    public ScreenshotDialog(Context context) {
        super(context);
        this.context = context;
        setContentView(R.layout.dialog_screenshot);
    }

    public void setImage(Bitmap bitmap){
        ((ImageView) findViewById(R.id.iv)).setImageBitmap(bitmap);
    }

    public void showDialog(int x, int y, final OnSavingListener listener){

        findViewById(R.id.bt_save).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                listener.save();
            }
        });

        findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        windowDeploy(x, y);

        //设置触摸对话框意外的地方取消对话框
        setCanceledOnTouchOutside(false);
        show();
    }

    //设置窗口显示
    public void windowDeploy(int x, int y){
        window = getWindow(); //得到对话框
        window.setWindowAnimations(R.style.dialogWindowAnim); //设置窗口弹出动画
        window.setBackgroundDrawableResource(R.color.vifrification); //设置对话框背景为透明
        WindowManager.LayoutParams wl = window.getAttributes();
        //根据x，y坐标设置窗口需要显示的位置
        //       wl.x = x; //x小于0左移，大于0右移
        //       wl.y = y; //y小于0上移，大于0下移
//        wl.alpha = 0.6f; //设置透明度
//        wl.gravity = Gravity.BOTTOM; //设置重力
        wl.width = x;
        wl.height = y;
        window.setAttributes(wl);
    }

    public interface OnSavingListener{
        public void save();
    }
}
