package com.yongyida.robot.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/30 0030.
 */
public class StateActivity extends OriginalActivity implements AdapterView.OnItemClickListener {

    private static final int RESULT_CODE = 1;
    private ListView mListView;
    private MyAdapter mAdapter;
    public List<String> stateList;
    public List<String> codeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);
        mListView = (ListView) findViewById(R.id.lv);
        setData();
        mAdapter = new MyAdapter(this, stateList, codeList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    private void setData() {
        stateList = new ArrayList<String>();
        codeList = new ArrayList<String>();
        stateList.add(getString(R.string.state_1));
        stateList.add(getString(R.string.state_2));
        codeList.add(Constants.CN_CODE);
        codeList.add(Constants.HK_CODE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent();
        intent.putExtra("state",stateList.get(i));
        intent.putExtra("state_code",codeList.get(i));
        setResult(RESULT_CODE, intent);
        finish();
    }

    private class MyAdapter extends BaseAdapter{
        private List<String> stateList;
        private List<String> codeList;
        private Context context;

        public MyAdapter(Context context, List<String> stateList , List<String> codeList){
            this.context = context;
            this.codeList = codeList;
            this.stateList = stateList;
        }

        @Override
        public int getCount() {
            return stateList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            MyViewHolder holder;
            if(view == null){
                holder = new MyViewHolder();
                view = LayoutInflater.from(context).inflate(R.layout.item_state,null);
                holder.tv_state = ((TextView) view.findViewById(R.id.tv_state));
                holder.tv_state_code = ((TextView) view.findViewById(R.id.tv_state_code));
                view.setTag(holder);
            } else {
                holder = ((MyViewHolder) view.getTag()) ;
            }
            holder.tv_state.setText(stateList.get(i));
            holder.tv_state_code.setText(codeList.get(i));
            return view;
        }
    }

    public static class MyViewHolder{
        TextView tv_state;
        TextView tv_state_code;
    }
}
