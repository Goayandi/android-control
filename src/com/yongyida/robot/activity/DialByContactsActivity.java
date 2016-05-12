//package com.yongyida.robot.activity;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//
//import com.yongyida.robot.R;
//import com.yongyida.robot.adapter.FriendAdapter;
//import com.yongyida.robot.huanxin.DemoHXSDKHelper;
//import com.yongyida.robot.video.comm.NetType;
//import com.yongyida.robot.video.comm.Utils;
//import com.yongyida.robot.video.comm.log;
//import com.yongyida.robot.video.command.User;
//import com.yongyida.robot.widget.HorizontalListView;
//
//import java.util.List;
//
///**
// * Created by Administrator on 2016/5/11 0011.
// */
//public class DialByContactsActivity extends BaseVideoActivity implements View.OnClickListener{
//    public static final String TAG = "DialByContactsActivity";
//
//    private HorizontalListView mFriendView;
//    private List<User> mFriendList;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        log.d(TAG, "onCreate()");
//
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_dial_by_contacts);
//
//        initView();
//        if (isLogined()) {
//            fillFriendList();
//        }
//
//        NetType netType = Utils.getNetWorkType(this);
//        if (netType == NetType.NETTYPE_NONE) {
//            Utils.showCustomToast(this, "网络未连接，请检查网络！");
//        }
//    }
//
//    @Override
//    protected void initView() {
//        findViewById(R.id.btn_dial).setOnClickListener(this);
//        findViewById(R.id.btn_back).setOnClickListener(this);
//    }
//
//    private void fillFriendList() {
//        //TODO 获取好友列表
//     //   mFriendList = YYDLogicServer.getInstance().getFriendList();
//        mFriendView = (HorizontalListView) findViewById(R.id.friend_listview);
//        mFriendView.setAdapter(new FriendAdapter(this, mFriendList));
//        mFriendView.setOnItemClickListener(new OnItemClickListenerImpl());
//    }
//
//    private class OnItemClickListenerImpl implements OnItemClickListener {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            User user = mFriendList.get(position);
//            meetingInvite(user);
//        }
//    }
//
//    /**
//     * 是否登录过
//     * @return
//     */
//    public boolean isLogined(){
//        SharedPreferences sharedPreferences = getSharedPreferences("userinfo",
//                MODE_PRIVATE);
//        int id = sharedPreferences.getInt("id", 0);
//        // 如果本地存在记录则自动跳转
//        if (id != 0 && DemoHXSDKHelper.getInstance().isLogined()) {
//            return true;
//        }
//        return false;
//    }
//
//    public void meetingInvite(User user) {
//        Intent intent = new Intent(this, InviteDialActivity.class);
//        intent.putExtra("role", user.getRole());
//        intent.putExtra("id", user.getId());
//        intent.putExtra("username", user.getUserName());
//        startActivity(intent);
//    }
//
//    private void openDialPlate() {
//        startActivity(new Intent(this, DialByNumberActivity.class));
//    }
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.btn_dial:
//                openDialPlate();
//                break;
//            case R.id.btn_back:
//                finish();
//                break;
//            default:
//                break;
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    }
//}
