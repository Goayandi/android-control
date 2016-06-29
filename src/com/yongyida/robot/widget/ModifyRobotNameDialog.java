package com.yongyida.robot.widget;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.ToastUtil;

/**
 * Created by Administrator on 2016/6/27 0027.
 */
public class ModifyRobotNameDialog extends Dialog {
    private Context mContext;
    private EditText mEditText;
    public ModifyRobotNameDialog(Context context, final OnSaveListener listener, String hint) {
        super(context, R.style.myDialog);
        mContext = context;
        setContentView(R.layout.dialog_modify_robot_name);
        //设置触摸对话框意外的地方取消对话框
        setCanceledOnTouchOutside(false);
        mEditText = (EditText) findViewById(R.id.et);
        if (!TextUtils.isEmpty(hint)) {
            mEditText.setHint(hint);
        }
        findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        findViewById(R.id.tv_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mEditText.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    ToastUtil.showtomain(mContext, mContext.getString(R.string.name_cant_null));
                    return;
                }
                listener.save(name);
            }
        });
    }


    public interface OnSaveListener{
        void save(String name);
    }

}
