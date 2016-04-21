package com.yongyida.robot.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.HandlerUtil;
import com.yongyida.robot.utils.NetUtil;
import com.yongyida.robot.utils.ThreadPool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/14 0014.
 */
public class FriendsActivity extends BaseVideoActivity {
    private static String TAG = "FriendsActivity";
    private RecyclerView mRecycleView;
    private MyRecycleViewAdapter mAdapter;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    try {
                        JSONArray data = new JSONArray(msg.getData().getString("result"));
                        setAdapterData(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        initView();
//        addRobotFriend(getSharedPreferences("userinfo", MODE_PRIVATE).getInt("id", 0),
//                1038L, getSharedPreferences("userinfo", MODE_PRIVATE).getString("session", null));
        findRobotFriend(getSharedPreferences("userinfo", MODE_PRIVATE).getInt("id", 0),
                getSharedPreferences("userinfo", MODE_PRIVATE).getString("session", null));
//        deleteRobotFriend(getSharedPreferences("userinfo", MODE_PRIVATE).getInt("id", 0),
//                1038L, getSharedPreferences("userinfo", MODE_PRIVATE).getString("session", null));
//        findRobotFriend(getSharedPreferences("userinfo", MODE_PRIVATE).getInt("id", 0),
//                getSharedPreferences("userinfo", MODE_PRIVATE).getString("session", null));
    }

    /**
     * 查询好友
     *
     * @param id      手机用户id
     * @param session 登录时获得的session
     */
    public void findRobotFriend(final int id, final String session) {
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id + "");
                params.put("session", session);
                try {
                    NetUtil.getinstance().http(Constants.FIND_ROBOT_FRIEND, params, new NetUtil.callback() {
                        @Override
                        public void success(JSONObject json) {
                            Log.i(TAG, json.toString());
                            try {
                                int ret = json.getInt("ret");
                                switch (ret) {
                                    case -2:
                                        Log.i(TAG, "session过期");
                                        break;
                                    case -1:
                                        Log.i(TAG, "缺少参数或参数未校验");
                                        break;
                                    case 0:
                                        String dataString = json.getString("Robots");
                                        HandlerUtil.sendmsg(mHandler, dataString, 1);
                                        Log.i(TAG, "成功");
                                        break;
                                    case 1:
                                        Log.i(TAG, "用户信息为空");
                                        break;
                                    case 2:
                                        Log.i(TAG, "手机用户没有朋友");
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
                            Log.i(TAG, errorresult);
                        }
                    }, FriendsActivity.this);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 删除好友
     *
     * @param id
     * @param frid
     * @param session
     */
    public void deleteRobotFriend(final int id, final Long frid, final String session) {
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id + "");
                params.put("frid", frid + "");
                params.put("session", session);
                try {
                    NetUtil.getinstance().http(Constants.DELETE_ROBOT_FRIEND, params, new NetUtil.callback() {
                        @Override
                        public void success(JSONObject json) {
                            Log.i(TAG, json.toString());
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
                    }, FriendsActivity.this);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 添加好友
     *
     * @param id
     * @param frid
     * @param session
     */
    public void addRobotFriend(final int id, final Long frid, final String session) {
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id + "");
                params.put("frid", frid + "");
                params.put("session", session);
                try {
                    NetUtil.getinstance().http(Constants.ADD_ROBOT_FRIEND, params, new NetUtil.callback() {
                        @Override
                        public void success(JSONObject json) {
                            Log.i(TAG, json.toString());
                            try {
                                int ret = json.getInt("ret");
                                switch (ret) {
                                    case -1:
                                        Log.i("AddFriendsActivity", "传入用户信息不存在");
                                        break;
                                    case 0:
                                        JSONArray data = json.getJSONArray("Robots");
                                        setAdapterData(data);
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
                    }, FriendsActivity.this);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 添加adapter的数据
     *
     * @param data
     */
    private void setAdapterData(JSONArray data) {
        if (mAdapter == null) {
            mAdapter = new MyRecycleViewAdapter(data);
            mRecycleView.setAdapter(mAdapter);
        } else {
            mAdapter.setAdapterData(data);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void initView() {
        mRecycleView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecycleView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    class MyRecycleViewAdapter extends RecyclerView.Adapter<MyRecycleViewAdapter.MyViewHolder> {
        private JSONArray data;
        private List<Boolean> checkList;  //checkBox的状态

        public MyRecycleViewAdapter(JSONArray data) {
            this.data = data;
            checkList = new ArrayList<Boolean>();
            for (int i = 0; i < data.length(); i++) {
                checkList.add(false);
            }
        }

        public void setAdapterData(JSONArray data) {
            this.data = data;
            for (int i = 0; i < data.length(); i++) {
                checkList.add(false);
            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(FriendsActivity.this).inflate(R.layout.item_rv_video, viewGroup, false));
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, final int i) {

            try {
                String s = data.getJSONObject(i).getString("rname");
                myViewHolder.tv.setText(s);
                myViewHolder.cb.setChecked(checkList.get(i));
                myViewHolder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        checkList.set(i, isChecked);
                    }
                });
            } catch (JSONException e) {
                Log.i(TAG, e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return data.length();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            ImageView iv;
            LinearLayout ll;
            CheckBox cb;

            public MyViewHolder(View itemView) {
                super(itemView);
                tv = ((TextView) itemView.findViewById(R.id.tv));
                iv = ((ImageView) itemView.findViewById(R.id.iv));
                ll = ((LinearLayout) itemView.findViewById(R.id.ll));
                cb = ((CheckBox) itemView.findViewById(R.id.cb));
            }
        }
    }
}
