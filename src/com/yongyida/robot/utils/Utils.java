package com.yongyida.robot.utils;

import android.app.ActivityManager;
import android.content.Context;

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
     * 是否符合账号的规则
     * @param account
     * @return
     */
    public static boolean isAccount(String account){
        Pattern p = Pattern.compile("^[A-Za-z0-9]{6,20}+$");
        Matcher m = p.matcher(account);
        return m.matches();
    }

    /**
     * 是否符合密码的规则
     * @param account
     * @return
     */
    public static boolean isPassword(String account){
        Pattern p = Pattern.compile("^[A-Za-z0-9]{6,20}+$");
        Matcher m = p.matcher(account);
        return m.matches();
    }
}