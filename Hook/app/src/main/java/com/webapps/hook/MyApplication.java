package com.webapps.hook;

import android.app.Application;
import android.util.Log;

/**
 * Created by leon on 16/12/26.
 */

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        HookAmsUtil hookAmsUtil = new HookAmsUtil(this,ProxyActivity.class);
        try {
            hookAmsUtil.hookAms();
            hookAmsUtil.hookSystemHandler();
        } catch (Exception e) {
            Log.i("INFO",e.getMessage());
            Log.i("INFO",e.toString());
            e.printStackTrace();
        }

    }
}
