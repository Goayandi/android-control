package com.yongyida.robot.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.yongyida.robot.service.SocketService;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
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
}