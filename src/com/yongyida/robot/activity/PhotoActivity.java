package com.yongyida.robot.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.yongyida.robot.R;
import com.yongyida.robot.adapter.PhotoAdapter;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ImageLoader;
import com.yongyida.robot.utils.StartUtil;
import com.yongyida.robot.utils.ThreadPool;
import com.yongyida.robot.utils.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhotoActivity extends Activity implements View.OnClickListener {
    private static final int SET_LOCAL_PHOTO = 1;
    private static final int NO_MORE_PHOTO = 2;
    private static final int DELETE_PHOTO = 3;
    private static final int UPDATE = 0;
    private static final int DELETE = 1;
    private static final int ROBOT_DELETE = 1;
    private static final int ROBOT_ADD = 2;
    private static final int EQUAL = 0;
    private static final int DOWNLOAD_COMPLETE = 4;
    private static final String TAG = "PhotoActivity";
    private static final int ADD_PHOTO = 5;
    private static final int ADD = 2;
    private Button refersh;
    private ImageLoader loader;
    private String[] paths;
    private String[] completePaths;
    private File file;
    private GridView photo_grid;
    private PhotoAdapter simpleAdapter;
    private Button delete;
    private List<String> localPhotoList = new ArrayList<String>();
    private boolean choosestate = false;
    private ArrayList<String> delete_list = new ArrayList<String>();  //存放的是.jpg的名字
    private ProgressDialog progressDialog;
    private int totalPhoto = -1;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NO_MORE_PHOTO:
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    ToastUtil.showtomain(PhotoActivity.this, getString(R.string.no_more));
                    refersh.setEnabled(true);
                    break;
                case SET_LOCAL_PHOTO:
                    completePaths = getLocalPhotosAbsAddr();
                    setadapter();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    refersh.setEnabled(true);
                    break;
                case DELETE_PHOTO:
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    completePaths = getLocalPhotosAbsAddr();
                    delete.setEnabled(true);
                    back();
                    setadapter();
                    break;
                case DOWNLOAD_COMPLETE:
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    refersh.setEnabled(true);
                    break;
                case ADD_PHOTO:
                    setadapter((String)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        file = getfile();
        refersh = (Button) findViewById(R.id.photo_refersh);
        refersh.setOnClickListener(this);
        delete = (Button) findViewById(R.id.photo_delete);
        delete.setOnClickListener(this);
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.Photo_Reply}, photo_setadapter);
        BroadcastReceiverRegister.reg(this,
                new String[] { Constants.Photo_Reply_Names }, names_reply);
        loader = ImageLoader.getInstance(3, ImageLoader.Type.LIFO);
        photo_grid = (GridView) findViewById(R.id.photo);
        photo_grid.setOnItemClickListener(clickListener);
        photo_grid.setOnItemLongClickListener(onlongclick);
        progressDialog = new ProgressDialog(this);
        if (!file.exists()) {
            file.mkdirs();
        }
        progressDialog.setMessage(getString(R.string.get_picture_ing));
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        setLocalPhoto(UPDATE, null);
    }

    private void setLocalPhotoList(String[] localphotos){
        localPhotoList.clear();
        for (String name : localphotos) {
            localPhotoList.add(name);
        }
    }

    public File getfile() {
        return new File(this.getExternalFilesDir(null).getAbsolutePath()
                + "/"
                + getSharedPreferences("Receipt", MODE_PRIVATE).getString(
                "username", null) + "small");
    }

    private String[] getLocalPhotos(){
        return getfile().list();
    }

    private String[] getLocalPhotosAbsAddr(){
        String[] tmp = getfile().list();
        String[] addr = new String[tmp.length];
        for (int i = 0; i < tmp.length ; i ++) {
            addr[i] = getfile().getAbsolutePath() + File.separator + tmp[i];
        }
        return addr;
    }

    /**
     *
     * @param flag
     * @param file 添加照片的路径
     */
    private void setLocalPhoto(final int flag, final String file) {
        ThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                setLocalPhotoList(getLocalPhotos());
                if (flag == DELETE) {
                    mHandler.sendEmptyMessage(DELETE_PHOTO);
                } else if (flag == ADD) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = ADD_PHOTO;
                    msg.obj = file;
                    mHandler.sendMessage(msg);
                } else {
                    mHandler.sendEmptyMessage(SET_LOCAL_PHOTO);
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photo_refersh:
                if (refersh.getText().toString().equals(getString(R.string.refresh))) {
                    progressDialog.setMessage(getString(R.string.get_picture_ing));
                    progressDialog.show();
                    progressDialog.setCanceledOnTouchOutside(false);
                    query_photo_name();
                    refersh.setEnabled(false);
                } else {
                    back();
                }
                break;
            case R.id.photo_delete:
                if (delete_list.size() > 0) {
                    progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage(getString(R.string.delete_ing));
                    progressDialog.show();
                    progressDialog.setCanceledOnTouchOutside(false);
                    sendBroadcast(new Intent(Constants.Photo_Delete).putExtra(
                            "delete_names", delete_list.toArray(new String[] {})));
                    delete.setEnabled(false);
                    ThreadPool.execute(new Runnable() {

                        @Override
                        public void run() {
                            for (int i = 0; i < delete_list.size(); i++) {
                                File f = new File(file.getAbsolutePath() + "/"
                                        + delete_list.get(i));
                                f.delete();
                            }
                            setLocalPhoto(DELETE, null);
                        }
                    });

                } else {
                    ToastUtil.showtomain(this, getString(R.string.choose_picture));
                }
                break;
            default:
                break;
        }
    }

    public void back() {
        choosestate = false;
        delete_list.clear();
        simpleAdapter.setAllUnCheck();
        simpleAdapter.notifyDataSetChanged();
        delete.setVisibility(View.GONE);
        refersh.setText(getString(R.string.refresh));
    }

    BroadcastReceiver names_reply = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            final List<String> robotPhotos = intent.getStringArrayListExtra("result");
            ThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    completePaths = getLocalPhotosAbsAddr();

//                    if (robotPhotos.size() == 0) {
//                        if (localphotos.length != 0) {
//                            //清除所有文件
//                            for (int n = 0; n < localphotos.length; n++){
//                                File f = new File(file.getAbsolutePath() + "/"
//                                        + localphotos[n]);
//                                f.delete();
//                            }
//                            setLocalPhoto(UPDATE);
//                        } else {
//                            mHandler.sendEmptyMessage(NO_MORE_PHOTO);
//                        }
//                    } else {
//                        boolean delete = false;
//                        //清除本地的远程已经删除的文件
//                        for (int n = 0; n < localphotos.length; n++){
//                            if(!robotPhotos.contains(localphotos[n])){
//                                File f = new File(file.getAbsolutePath() + "/"
//                                        + localphotos[n]);
//                                f.delete();
//                                delete = true;
//                            }
//                        }
//                        String[] photos = getLocalPhotos();
//                        int compareResult = compareFile(robotPhotos, photos);
//                        if (compareResult == EQUAL) {
//                            if (!delete) {
//                                mHandler.sendEmptyMessage(NO_MORE_PHOTO);
//                            } else {
//                                setLocalPhoto(UPDATE);
//                            }
//                        } else {
//                            totalPhoto = robotPhotos.size();
//                            Log.i(TAG, "totalPhot" + totalPhoto);
//                            for (String name : robotPhotos) {
//                                if (!Arrays.asList(photos).contains(name)) {
//                                    Log.i(TAG, name);
//                                    query_photo(name);
//                                }
//                            }
//                        }
//                    }
                    if (robotPhotos.size() == 0) {
                        mHandler.sendEmptyMessage(NO_MORE_PHOTO);
                    } else {
                        String[] photos = getLocalPhotos();
                        List<String> unDownloadList = containAll(robotPhotos, photos);
                        if (unDownloadList.size() == 0) {
                            mHandler.sendEmptyMessage(NO_MORE_PHOTO);
                        } else {
                            totalPhoto = photos.length + unDownloadList.size();
                            for (String unDownloadName : unDownloadList) {
                                query_photo(unDownloadName);
                            }
                        }
                    }
                }
            });
        }
    };

    /**
     *
     * @param names
     * @param localphotos
     * @return
     */
    private int compareFile(List<String> names, String[] localphotos) {
        // names.size不会为0
        if (names.size() == localphotos.length) {
            return EQUAL;
        } else if (names.size() < localphotos.length) {
            return ROBOT_DELETE;
        } else {
            return ROBOT_ADD;
        }
    }

    private List<String> containAll(List<String> names, String[] localphotos) {
        if (localphotos.length == 0) {
            return names;
        }
        List<String> list = new ArrayList<String>();
        for (String name : names) {
            if (!Arrays.asList(localphotos).contains(name)) {
                list.add(name);
            }
        }
        return list;
    }

    private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int position,
                                long arg3) {
            if (!choosestate) {
                Bundle params = new Bundle();
                params.putInt("position", position);
                params.putStringArray("location", completePaths);
                Log.i(TAG, "position:" + position);
                StartUtil.startintent(PhotoActivity.this,
                        BigImageActivity.class, "no", params);
            }
        }
    };

    private synchronized void setadapter(String file){
        if (completePaths == null || completePaths.length == 0) {
            completePaths = new String [1];
            completePaths[0] = file;
        } else {
            String[] tmpPaths = new String[completePaths.length + 1];
            System.arraycopy(completePaths, 0, tmpPaths, 0, completePaths.length);
            tmpPaths[completePaths.length] = file;
            completePaths = tmpPaths;
        }
        if (completePaths.length == totalPhoto) {
            Log.i(TAG, "dismiss");
            totalPhoto = -1;
            mHandler.sendEmptyMessage(DOWNLOAD_COMPLETE);
        }
        if (simpleAdapter == null) {
            simpleAdapter = new PhotoAdapter(PhotoActivity.this, completePaths, loader,
                    back);
            photo_grid.setAdapter(simpleAdapter);
        } else {
            simpleAdapter.setData(completePaths);
        }


    }

    private void setadapter() {
        if (localPhotoList.size() == 0) {
            paths = new String[]{};
        } else {
            paths = new String[localPhotoList.size()];
            for (int i = 0; i < localPhotoList.size(); i++) {
                paths[i] = file.getAbsolutePath() + "/" + localPhotoList.get(i);
            }
        }
        if (paths.length == totalPhoto) {
            Log.i(TAG, "dismiss");
            totalPhoto = -1;
            mHandler.sendEmptyMessage(DOWNLOAD_COMPLETE);
        }
        if (simpleAdapter == null) {
            simpleAdapter = new PhotoAdapter(PhotoActivity.this, paths, loader,
                    back);
            photo_grid.setAdapter(simpleAdapter);
        } else {
            simpleAdapter.setData(paths);
        }

    }

    private PhotoAdapter.callback back = new PhotoAdapter.callback() {

        @Override
        public boolean IslongClick() {

            return choosestate;
        }
    };

    BroadcastReceiver photo_setadapter = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Constants.Photo_Reply)) {
                String file = intent.getStringExtra(Constants.PHOTO_PATH);
                setLocalPhoto(ADD , file);
            }
        }
    };

    private AdapterView.OnItemLongClickListener onlongclick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapter, View v,
                                       int position, long arg3) {
            if (choosestate) {
                choosestate = false;
            } else {
                choosestate = true;
            }
            simpleAdapter.notifyDataSetChanged();
            delete.setVisibility(View.VISIBLE);
            refersh.setText(getString(R.string.cancel));
            return false;
        }
    };

    public void checked(String name) {
        delete_list.add(name);
    }

    public void notcheck(String name) {
        delete_list.remove(name);
    }


    public void query_photo(String name) {
        sendBroadcast(new Intent(Constants.Photo_Query).putExtra(
                "name", name));
    }

    private void query_photo_name() {
        sendBroadcast(new Intent(Constants.Photo_Query_Name));
    }

    public void back(View view) {
        finish();
    }

    protected void onDestroy() {
        if (names_reply != null) {
            unregisterReceiver(names_reply);
        }
        if (photo_setadapter != null) {
            unregisterReceiver(photo_setadapter);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

}


/*import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.yongyida.robot.R;
import com.yongyida.robot.adapter.PhotoAdapter;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ImageLoader;
import com.yongyida.robot.utils.StartUtil;
import com.yongyida.robot.utils.ThreadPool;
import com.yongyida.robot.utils.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "PhotoActivity";
    private Button refersh;
    private List<String> names = new ArrayList<String>();
    private File file;
    private ImageLoader loader;
    private String[] paths;
    private GridView photo_grid;
    private PhotoAdapter simpleAdapter;
    private Button delete;
    private String[] localphotos;         //存放的是.robot的名字
    private boolean choosestate = false;
    private ArrayList<String> delete_list = new ArrayList<String>();  //存放的是.jpg的名字
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        getfile();
        getlocalphoto();
        refersh = (Button) findViewById(R.id.photo_refersh);
        refersh.setOnClickListener(this);
        delete = (Button) findViewById(R.id.photo_delete);
        delete.setOnClickListener(this);
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.Photo_Reply}, photo_setadapter);
        BroadcastReceiverRegister.reg(this,
                new String[] { Constants.Photo_Reply_Names }, names_reply);
        loader = ImageLoader.getInstance(3, ImageLoader.Type.LIFO);
        photo_grid = (GridView) findViewById(R.id.photo);
        photo_grid.setOnItemClickListener(clickListener);
        photo_grid.setOnItemLongClickListener(onlongclick);
        progressDialog = new ProgressDialog(this);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

//	public void fileScan(String filePath){
//	       Uri data = Uri.parse("file://"+filePath);
//	       sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
//	   }

    public void getfile() {
        file = new File(this.getExternalFilesDir(null).getAbsolutePath()
                + "/"
                + getSharedPreferences("Receipt", MODE_PRIVATE).getString(
                "username", null) + "small");
    }

    private void getlocalphoto() {
        ThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                localphotos = new String[]{};
                localphotos = file.list();
            }
        });

    }

    @Override
    protected void onStart() {
        Log.e(TAG, "onStart");
        query_photo_name();
        setadapter();
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photo_refersh:
                if (refersh.getText().toString().equals(getString(R.string.refresh))) {
                    if (names.size() != 0) {
                        progressDialog.setMessage(getString(R.string.get_picture_ing));
                        progressDialog.show();
                        progressDialog.setCanceledOnTouchOutside(false);
                        query_photo();
                        refersh.setEnabled(false);
                    } else {
                        ToastUtil.showtomain(this, getString(R.string.no_more));
                    }
                } else {
                    back();
                }
                break;
            case R.id.photo_delete:
                if (delete_list.size() > 0) {
                    progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage(getString(R.string.delete_ing));
                    progressDialog.show();
                    progressDialog.setCanceledOnTouchOutside(false);
                    sendBroadcast(new Intent(Constants.Photo_Delete).putExtra(
                            "delete_names", delete_list.toArray(new String[] {})));
                    delete.setEnabled(false);
                    ThreadPool.execute(new Runnable() {

                        @Override
                        public void run() {
                            for (int i = 0; i < delete_list.size(); i++) {
                                File f = new File(file.getAbsolutePath() + "/"
                                        + delete_list.get(i));
                                f.delete();
                            }
                            handler.sendEmptyMessage(0);
                        }
                    });

                } else {

                    ToastUtil.showtomain(this, getString(R.string.choose_picture));
                }
                break;
            default:
                break;
        }
    }

    Handler handler = new Handler() {
        public void dispatchMessage(android.os.Message msg) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            delete.setEnabled(true);
            back();
            setadapter();
        };
    };

    public void back() {
        choosestate = false;
        delete_list.clear();
        simpleAdapter.setAllUnCheck();
        simpleAdapter.notifyDataSetChanged();
        delete.setVisibility(View.GONE);
        refersh.setText(getString(R.string.refresh));
    }

    BroadcastReceiver names_reply = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            names = intent.getStringArrayListExtra("result");
            ThreadPool.execute(new Runnable() {

                @Override
                public void run() {
                    if (names.size() == 0) {
                        return;
                    }
                    for (int n = 0; n < localphotos.length; n++){
                        if(!names.contains(localphotos[n])){
                            File f = new File(file.getAbsolutePath() + "/"
                                    + localphotos[n]);
                            f.delete();
                        }
                    }
                    localphotos = file.list();
                    for (int i = 0; i < names.size(); i++) {
                        for (int j = 0; j < localphotos.length; j++) {
                            if (names.get(i).equals(localphotos[j])) {
                                names.remove(i);
                            }
                        }
                    }
                    Log.i("nameslength", names.size() + "");
                }
            });
        }
    };

    private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int position,
                                long arg3) {
            if (choosestate) {

            } else {
                Bundle params = new Bundle();
                params.putInt("position", position);
                StartUtil.startintent(PhotoActivity.this,
                        BigImageActivity.class, "no", params);
            }
        }
    };

    private void setadapter() {
        Log.e("aaa", "length:" + file.list().length);
        getfile();
        if (file.list().length == 0) {
            paths = new String[] {};
        } else {
            paths = new String[file.list().length];
            for (int i = 0; i < paths.length; i++) {
                paths[i] = file.getAbsolutePath() + "/" + file.list()[i];
            }
        }
        simpleAdapter = new PhotoAdapter(PhotoActivity.this, paths, loader,
                back);
        photo_grid.setAdapter(simpleAdapter);

    }

    private PhotoAdapter.callback back = new PhotoAdapter.callback() {

        @Override
        public boolean IslongClick() {

            return choosestate;
        }
    };

    BroadcastReceiver photo_setadapter = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Constants.Photo_Reply)) {
                setadapter();
                if (index == names.size()) {
                    index = 0;
                    names.clear();
                    refersh.setEnabled(true);
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                } else {
                    query_photo();
                }
            }
        }
    };

    private AdapterView.OnItemLongClickListener onlongclick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapter, View v,
                                       int position, long arg3) {
            if (choosestate) {
                choosestate = false;
            } else {
                choosestate = true;
            }
            simpleAdapter.notifyDataSetChanged();
            delete.setVisibility(View.VISIBLE);
            refersh.setText(getString(R.string.cancel));
            return false;
        }
    };

    public void checked(String name) {
        delete_list.add(name);
    }

    public void notcheck(String name) {
        delete_list.remove(name);
    }

    private int index = 0;

    public void query_photo() {

        if (index < names.size()) {
            this.sendBroadcast(new Intent(Constants.Photo_Query).putExtra(
                    "name", names.get(index)));
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://"
                            + this.getExternalFilesDir(null).getAbsolutePath())));
        }
        index++;
        Log.i("index", index + "");
    }

    private void query_photo_name() {
        sendBroadcast(new Intent(Constants.Photo_Query_Name));
    }

    public void back(View view) {
        finish();
    }

    protected void onDestroy() {
        if (names_reply != null) {
            unregisterReceiver(names_reply);
        }
        if (photo_setadapter != null) {
            unregisterReceiver(photo_setadapter);
        }
        super.onDestroy();
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

}*/
