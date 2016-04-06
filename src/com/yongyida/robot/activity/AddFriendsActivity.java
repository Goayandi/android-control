package com.yongyida.robot.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.yongyida.robot.bean.NewRobot;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.NetUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/2 0002.
 */
public class AddFriendsActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initlayout(SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
    }

    public void addRobotFriend(Long id, String robotId, String robotSerial){
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id + "");
        params.put("robot_id", robotId);
        params.put("Robot_serial", robotSerial);
        try {
            NetUtil.getinstance().http(Constants.ADD_ROBOT_FRIEND, params, new NetUtil.callback() {
                @Override
                public void success(JSONObject json) {
                    try {
                        int ret = json.getInt("ret");
                        switch (ret) {
                            case -1:
                                Log.i("AddFriendsActivity", "传入用户信息不存在");
                                break;
                            case 0:
                                Log.i("AddFriendsActivity", "成功");
                                break;
                            case 1:
                                Log.i("AddFriendsActivity", "用户信息为空");
                                break;
                            case 2:
                                Log.i("AddFriendsActivity", "机器人信息为空");
                                break;
                            case 3:
                                Log.i("AddFriendsActivity", "机器人id或序列号不存在");
                                break;
                            case 4:
                                Log.i("AddFriendsActivity", "超过最大好友数(默认1000个)");
                                break;
                            case 5:
                                Log.i("AddFriendsActivity", "手机用户已经添加该机器人为好友");
                                break;
                            default:
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void error(String errorresult) {

                }
            }, AddFriendsActivity.this);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        }
    }
    public void deleteRobotFriend(Long id, String robotId, String robotSerial){
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id + "");
        params.put("robot_id", robotId);
        params.put("Robot_serial", robotSerial);
        try {
            NetUtil.getinstance().http(Constants.DELETE_ROBOT_FRIEND, params, new NetUtil.callback() {
                @Override
                public void success(JSONObject json) {
                    try {
                        int ret = json.getInt("ret");
                        switch (ret) {
                            case -1:
                                Log.i("AddFriendsActivity", "缺少参数");
                                break;
                            case 0:
                                Log.i("AddFriendsActivity", "成功");
                                break;
                            case 1:
                                Log.i("AddFriendsActivity", "用户信息为空");
                                break;
                            case 2:
                                Log.i("AddFriendsActivity", "机器人信息为空");
                                break;
                            case 3:
                                Log.i("AddFriendsActivity", "机器人id或序列号不存在");
                                break;
                            case 4:
                                Log.i("AddFriendsActivity", "机器人不是手机用户的朋友");
                                break;
                            default:
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


                @Override
                public void error(String errorresult) {

                }
            }, AddFriendsActivity.this);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        }
    }
    public void findRobotFriend(Long id){
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id + "");
        try {
            NetUtil.getinstance().http(Constants.FIND_ROBOT_FRIEND, params, new NetUtil.callback() {
                @Override
                public void success(JSONObject json) {
                    try {
                        int ret = json.getInt("ret");
                        List<NewRobot> list = (List<NewRobot>) json.getJSONArray("Robots");
                        switch (ret) {
                            case -1:
                                Log.i("AddFriendsActivity", "缺少参数或参数未校验");
                                break;
                            case 0:
                                Log.i("AddFriendsActivity", "成功");
                                break;
                            case 1:
                                Log.i("AddFriendsActivity", "用户信息为空");
                                break;
                            case 2:
                                Log.i("AddFriendsActivity", "手机用户没有朋友");
                                break;
                            default:
                                break;

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void error(String errorresult) {

                }
            }, AddFriendsActivity.this);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        }
    }

}
