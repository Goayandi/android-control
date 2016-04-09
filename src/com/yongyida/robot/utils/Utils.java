package com.yongyida.robot.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

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
     * @param mServiceList
     * @param className
     * @return
     */
    public static boolean ServiceIsStart(List<ActivityManager.RunningServiceInfo> mServiceList,String className){

        for(int i = 0; i < mServiceList.size(); i ++)

        {

            if(className.equals(mServiceList.get(i).service.getClassName()))

            {
                return true;
            }

        }

        return false;

    }
}