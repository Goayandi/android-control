package com.yongyida.robot.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.yongyida.robot.service.SocketService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final String TAG = "Utils";

    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

    public enum SystemLanguage{
        CHINA,
        ENGLISH
    }
    public static SystemLanguage getLanguage(Context context){
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh")){
            return SystemLanguage.CHINA;
        } else if (language.endsWith("en")) {
            return SystemLanguage.ENGLISH;
        }
        return SystemLanguage.CHINA;
    }


    /**
     * 判断某个界面是否在前台
     *
     * @param context
     * @param className
     *            某个界面名称
     */
    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断服务是否开启
     * @param context
     * @param className
     * @return
     */
    public static boolean isServiceRunning(Context context,String className) {

        boolean isRunning = false;

        ActivityManager activityManager =

                (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> serviceList

                = activityManager.getRunningServices(Integer.MAX_VALUE);

        if (!(serviceList.size() > 0)) {

            return false;

        }

        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;

                break;

            }
        }

        return isRunning;

    }

    /**
     * 判断程序是否在后台运行
     * @param context
     * @return
     */
    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                /*
                BACKGROUND=400 EMPTY=500 FOREGROUND=100
                GONE=1000 PERCEPTIBLE=130 SERVICE=300 ISIBLE=200
                 */
                Log.i(context.getPackageName(), "此appimportace ="
                        + appProcess.importance
                        + ",context.getClass().getName()="
                        + context.getClass().getName());
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Log.i(context.getPackageName(), "处于后台"
                            + appProcess.processName);
                    return true;
                } else {
                    Log.i(context.getPackageName(), "处于前台"
                            + appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 是否是wifi环境
     * @param context
     * @return
     */
    public static boolean checknetwork(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (info.isAvailable() && info.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否符合账号的规则
     * @param account
     * @return
     */
    public static boolean isAccount(String account){
        Pattern p = Pattern.compile("^[A-Z,a-z][A-Z,a-z,0-9]{5,19}$");
        Matcher m = p.matcher(account);
        return m.matches();
    }

    /**
     * 是否符合密码的规则
     * @param account
     * @return
     */
    public static boolean isPassword(String account){
        if (account.length() < 6 || account.length() > 20) {
            return false;
        }
        return true;
    }

    /**
     * 开启socket服务
     * @param context
     */
    public static void startSocketService(Context context) {
        Constants.isUserClose = false;
        context.startService(new Intent(context, SocketService.class));
    }

    /**
     * 关闭服务
     * @param context
     */
    public static void stopSocketService(Context context) {
        Constants.isUserClose = true;
        context.stopService(new Intent(context, SocketService.class));
    }

    /**
     * 获取view的bitmap
     * @param v
     * @return
     */
    public static Bitmap getViewBitmap(View v) {

        v.clearFocus(); //

        v.setPressed(false); //

        // 能画缓存就返回false

        boolean willNotCache = v.willNotCacheDrawing();

        v.setWillNotCacheDrawing(false);

        int color = v.getDrawingCacheBackgroundColor();

        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {

            v.destroyDrawingCache();

        }

        v.buildDrawingCache();

        Bitmap cacheBitmap = v.getDrawingCache();

        if (cacheBitmap == null) {

            return null;

        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view

        v.destroyDrawingCache();

        v.setWillNotCacheDrawing(willNotCache);

        v.setDrawingCacheBackgroundColor(color);

        return bitmap;

    }

    /**
     * 保存bitmap到本地
     * @param bitmap
     * @param filename
     */
    public static void saveFile(Bitmap bitmap, String filename) {

        FileOutputStream fileOutputStream = null;
        File folder = new File(filename.substring(0,filename.lastIndexOf("/")));
        if (!folder.exists()) {
            folder.mkdir();
        }
        try {

            fileOutputStream = new FileOutputStream(filename);

            if (fileOutputStream != null) {

                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);

                fileOutputStream.flush();

                fileOutputStream.close();

            }

        } catch (FileNotFoundException e) {


            e.printStackTrace();

        } catch (IOException e) {


            e.printStackTrace();

        }

    }


    /**
     * 获取容联云账号
     * @param context
     * @return
     */
    public static String getAccount(Context context){
        int method = context.getSharedPreferences("login",
                context.MODE_PRIVATE).getInt(Constants.LOGIN_METHOD, -1);
        int userId = context.getSharedPreferences("userinfo", context.MODE_PRIVATE)
                .getInt("id", -1);
        if (method == Constants.ACCOUNT_LOGIN) {
            String account = context.getSharedPreferences("userinfo", context.MODE_PRIVATE)
                    .getString("account_name", null);
            if (account != null) {
                Log.i(TAG, "getAccount:ACCOUNT_LOGIN:account" + account);
                return account;
            } else {
                if (userId != -1) {
                    Log.i(TAG, "getAccount:ACCOUNT_LOGIN:userId" + userId);
                    return userId + "";
                }
            }
        } else {
            String username = context.getSharedPreferences("userinfo", context.MODE_PRIVATE)
                    .getString("phonenumber", null);
            if (username != null) {
                return username;
            } else {
                if (userId != -1) {
                    Log.i(TAG, "getAccount:userId:" + userId);
                    return userId + "";
                }
            }
        }
        Log.i(TAG, "getAccount:null");
        return "";
    }

    /**
     * 获取手机号
     * @param context
     * @return
     */
    public static String getPhoneNumber(Context context){
        String username = context.getSharedPreferences("userinfo", context.MODE_PRIVATE)
                .getString("phonenumber", null);
        if (username != null) {
            return username;
        }
        return "error";
    }

    /**
     * 获取手机数字id
     * @param context
     * @return
     */
    public static String getUserID(Context context){
        int userId = context.getSharedPreferences("userinfo", context.MODE_PRIVATE)
                .getInt("id", -1);
        if (userId != -1) {
            return userId + "";
        }
        return "error";
    }

    public final static int CN = 0;
    public final static int TEST = 1;
    public final static int HK = 2;
    public final static int TW = 3;

    public static void switchServer(int area){
        switch (area) {
            case CN:
                Constants.address = Constants.address_cn;
                Constants.download_fota_address = Constants.download_fota_address_cn;
                Constants.ip = Constants.ip_cn;
                Constants.port = Constants.port_cn;
                Constants.download_address = Constants.download_address_cn;
                break;
            case TEST:
                Constants.address = Constants.address_test;
                Constants.download_fota_address = Constants.download_fota_address_test;
                Constants.ip = Constants.ip_test;
                Constants.port = Constants.port_test;
                Constants.download_address = Constants.download_address_test;
                break;
            case HK:
                Constants.address = Constants.address_hk;
                Constants.download_fota_address = Constants.download_fota_address_hk;
                Constants.ip = Constants.ip_hk;
                Constants.port = Constants.port_hk;
                Constants.download_address = Constants.download_address_hk;
                break;
            case TW:
                Constants.address = Constants.address_tw;
                Constants.download_fota_address = Constants.download_fota_address_tw;
                Constants.ip = Constants.ip_tw;
                Constants.port = Constants.port_tw;
                Constants.download_address = Constants.download_address_tw;
                break;
        }
    }

    /**
     *
     * @param receiver
     * @param service
     */
    public static void unRegisterReceiver(BroadcastReceiver receiver, Service service) {
        try {
            if (receiver != null) {
                service.unregisterReceiver(receiver);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param receiver
     * @param context
     */
    public static void unRegisterReceiver(BroadcastReceiver receiver, Context context) {
        try {
            if (receiver != null) {
                context.unregisterReceiver(receiver);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public static String showTimeCount(long time) {
        String timeCount = "";
        long hourc = time / 3600000;
        String hour = "0" + hourc;
        hour = hour.substring(hour.length()-2, hour.length());

        long minuec = (time-hourc*3600000)/(60000);
        String minue = "0" + minuec;
        minue = minue.substring(minue.length()-2, minue.length());

        long secc = (time-hourc*3600000-minuec*60000)/1000;
        String sec = "0" + secc;
        sec = sec.substring(sec.length()-2, sec.length());
        timeCount = hour + ":" + minue + ":" + sec;

        return timeCount;
    }

    public static void doStartApplicationWithPackageName(Activity activity, String packagename) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = activity.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = activity.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);

            intent.setComponent(cn);
            activity.startActivity(intent);
        }
    }

    public static boolean isCameraCanUse() {
        boolean canUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            // setParameters 是针对魅族MX5 做的。MX5 通过Camera.open() 拿到的Camera
            // 对象不为null
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            canUse = false;
        }
        if (mCamera != null) {
            mCamera.release();
        }
        return canUse;
    }

    /**
     * 获取屏幕宽度
     * @return
     */
    public static int getScreenWidth(Activity activity){
        WindowManager manager = activity.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获取屏幕高度
     * @return
     */
    public static int getScreenHeight(Activity activity){
        WindowManager manager = activity.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * 是否属于当前系列
     * @param id   机器人id Y20... Y50...
     * @param series  系列  20 50
     * @return
     */
    public static boolean isSeries(String id, String series) {
        if (series.equals(id.substring(1,3))) {
            return true;
        }
        return false;
    }

    public static void fixInputMethodManagerLeak(Context destContext) {
        if (destContext == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) destContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        String [] arr = new String[]{"mCurRootView", "mServedView", "mNextServedView"};
        Field f = null;
        Object obj_get = null;
        for (int i = 0;i < arr.length;i ++) {
            String param = arr[i];
            try{
                f = imm.getClass().getDeclaredField(param);
                if (f.isAccessible() == false) {
                    f.setAccessible(true);
                } // author: sodino mail:sodino@qq.com
                obj_get = f.get(imm);
                if (obj_get != null && obj_get instanceof View) {
                    View v_get = (View) obj_get;
                    if (v_get.getContext() == destContext) { // 被InputMethodManager持有引用的context是想要目标销毁的
                        f.set(imm, null); // 置空，破坏掉path to gc节点
                    } else {
                        // 不是想要目标销毁的，即为又进了另一层界面了，不要处理，避免影响原逻辑,也就不用继续for循环了
                        break;
                    }
                }
            }catch(Throwable t){
                t.printStackTrace();
            }
        }
    }
}