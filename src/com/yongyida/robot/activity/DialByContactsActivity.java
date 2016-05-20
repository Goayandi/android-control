package com.yongyida.robot.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.huanxin.DemoHXSDKHelper;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.HandlerUtil;
import com.yongyida.robot.utils.NetUtil;
import com.yongyida.robot.utils.ThreadPool;
import com.yongyida.robot.video.comm.NetType;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.command.User;
import com.yongyida.robot.video.sdk.Role;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/5/11 0011.
 */
public class DialByContactsActivity extends BaseVideoActivity implements View.OnClickListener{
    public static final String TAG = "DialByContactsActivity";
    private RecyclerView mRecycleView;
    private List<User> mFriendList;
    private FriendsRecyclerViewAdapter mAdapter;
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
        log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dial_by_contacts);

        initView();
        if (isLogined()) {
            fillFriendList();
        }

        NetType netType = Utils.getNetWorkType(this);
        if (netType == NetType.NETTYPE_NONE) {
            Utils.showCustomToast(this, "网络未连接，请检查网络！");
        }
    }

    @Override
    protected void initView() {
        mRecycleView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecycleView.setLayoutManager(linearLayoutManager);
        findViewById(R.id.btn_dial).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
    }

    /**
     * 添加adapter的数据
     *
     * @param data
     */
    private void setAdapterData(JSONArray data) {
        if (mAdapter == null) {
            mAdapter = new FriendsRecyclerViewAdapter(data, this);
            mRecycleView.setAdapter(mAdapter);
        } else {
            mAdapter.setAdapterData(data);
            mAdapter.notifyDataSetChanged();
        }
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
                    }, DialByContactsActivity.this);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void fillFriendList() {
        findRobotFriend(getSharedPreferences("userinfo", MODE_PRIVATE).getInt("id", 0),
                getSharedPreferences("userinfo", MODE_PRIVATE).getString("session", null));
    }

    /**
     * 是否登录过
     * @return
     */
    public boolean isLogined(){
        SharedPreferences sharedPreferences = getSharedPreferences("userinfo",
                MODE_PRIVATE);
        int id = sharedPreferences.getInt("id", 0);
        // 如果本地存在记录则自动跳转
        if (id != 0 && DemoHXSDKHelper.getInstance().isLogined()) {
            return true;
        }
        return false;
    }

    public void meetingInvite(Long rid, String rRole, String rName) {
        Intent intent = new Intent(this, InviteDialActivity.class);
        intent.putExtra("numbertype", rRole);
        intent.putExtra("number", rid);
        intent.putExtra("username", rName);
        intent.putExtra("role", Role.User);
        intent.putExtra("id", getSharedPreferences("userinfo", MODE_PRIVATE).getInt("id", 0));
        startActivity(intent);
    }

    private void openDialPlate() {
        startActivity(new Intent(this, DialByNumberActivity.class));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_dial:
                openDialPlate();
                break;
            case R.id.btn_back:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class FriendsRecyclerViewAdapter extends RecyclerView.Adapter<FriendsRecyclerViewAdapter.MyViewHolder> {
        private JSONArray data;
        private Context mContext;

        public FriendsRecyclerViewAdapter(JSONArray data, Context context) {
            mContext = context;
            this.data = data;
        }

        public void setAdapterData(JSONArray data) {
            this.data = data;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_rv_friend_dial, viewGroup, false));
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, final int i) {

            try {
                String rName = data.getJSONObject(i).getString("rname");
                final Long rid = data.getJSONObject(i).getLong("rid");
                myViewHolder.tv.setText(rName);
                myViewHolder.ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        meetingInvite(rid, "Robot", "Robot" + rid);
                    }
                });
            } catch (JSONException e) {
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
            public MyViewHolder(View itemView) {
                super(itemView);
                tv = ((TextView) itemView.findViewById(R.id.tv));
                iv = ((ImageView) itemView.findViewById(R.id.iv));
                ll = (LinearLayout) itemView.findViewById(R.id.ll);
            }
        }
    }
}
