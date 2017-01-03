package com.yongyida.robot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/6/30 0030.
 */
public class ChooseWeekActivity extends OriginalActivity implements View.OnClickListener {

    private ListView mListView;
    private List<Boolean> mChooseItems;
    private List<String> mTextViewList;
    public final static int CHOOSE_WEEK_ACTIVITY_RESULT = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_week);
        mListView = (ListView) findViewById(R.id.lv);
        String choosedWeek = getIntent().getStringExtra(Constants.CHOOSED_WEEK);
        setTextViewList();
        setCheckedList(choosedWeek);
        MyAdapter mAdapter = new MyAdapter();
        mListView.setAdapter(mAdapter);
        findViewById(R.id.tv_back).setOnClickListener(this);
    }

    private void setTextViewList() {
        mTextViewList = new ArrayList<String>();
        mTextViewList.add(getString(R.string.monday));
        mTextViewList.add(getString(R.string.tuesday));
        mTextViewList.add(getString(R.string.wednesday));
        mTextViewList.add(getString(R.string.thursday));
        mTextViewList.add(getString(R.string.friday));
        mTextViewList.add(getString(R.string.saturday));
        mTextViewList.add(getString(R.string.sunday));
    }

    public void back() {
        Intent intent = new Intent();
        intent.putExtra(Constants.CHOOSED_WEEK_RESULT, getWeekString());
        setResult(CHOOSE_WEEK_ACTIVITY_RESULT, intent);
        finish();
    }

    private String getWeekString() {
        String weeks = "";
        for (int i = 0; i < 7; i++) {
            if (mChooseItems.get(i)) {
                weeks += i + 1 + ",";
            }
        }
        if (!TextUtils.isEmpty(weeks)) {
            weeks = weeks.substring(0, weeks.length() - 1);
        }
        return weeks;
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private boolean containWeek(String[] weeks, int week) {
        if (weeks == null || weeks.length == 0) {
            return false;
        } else {
            for (int i = 0; i < weeks.length; i++) {
                if (weeks[i].equals(week+"")) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setCheckedList(String checkedList) {
        mChooseItems = new ArrayList<Boolean>();
        if (!TextUtils.isEmpty(checkedList)) {
            String[] weeks = checkedList.split(",");
            if (weeks != null && weeks.length != 0) {
                for (int i = 1; i < 8; i++) {
                    if (containWeek(weeks, i)) {
                        mChooseItems.add(i - 1, true);
                    } else {
                        mChooseItems.add(i - 1, false);
                    }
                }
            } else {
                for (int i = 0; i < 7; i++) {
                    mChooseItems.add(i, false);
                }
            }
        } else {
            for (int i = 0; i < 7; i++) {
                mChooseItems.add(i, false);
            }
        }


    }

    @Override
    public void onClick(View v) {
        back();
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mChooseItems.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            WeekHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(ChooseWeekActivity.this).inflate(R.layout.item_choose_week, null);
                holder = new WeekHolder();
                holder.iv = (ImageView) convertView.findViewById(R.id.iv);
                holder.tv = (TextView) convertView.findViewById(R.id.tv);
                holder.rl = (RelativeLayout) convertView.findViewById(R.id.rl);
                convertView.setTag(holder);
            } else {
                holder = (WeekHolder) convertView.getTag();
            }
            holder.tv.setText(mTextViewList.get(position));
            holder.iv.setBackground(mChooseItems.get(position) ? getResources().getDrawable(R.drawable.g) : getResources().getDrawable(R.drawable.shape_cb_bg));
            holder.rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mChooseItems.set(position, !mChooseItems.get(position));
                    v.findViewById(R.id.iv).setBackground(mChooseItems.get(position) ? getResources().getDrawable(R.drawable.g) : getResources().getDrawable(R.drawable.shape_cb_bg));
                }
            });
            return convertView;
        }
    }
    class WeekHolder {
        private ImageView iv;
        private TextView tv;
        private RelativeLayout rl;
    }
}
