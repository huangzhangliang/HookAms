package com.webapps.hook;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by leon on 16/12/26.
 */

public class HookAmsUtil {

    private Class<?> mProxyActivity;
    private Context mContext;

    public HookAmsUtil(Context context,Class<?> proxyActivity){
        mProxyActivity = proxyActivity;
        mContext = context;
    }

    public void hookAms() throws Exception {
        Log.i("INFO","start hook");
        Class<?> forName =  Class.forName("android.app.ActivityManagerNative");
        Field defaultField = forName.getDeclaredField("gDefault");
        defaultField.setAccessible(true);
        // gDefault变量值
        Object defaultValue = defaultField.get(null);
        // 反射SingleTon
        Class<?> forName2 = Class.forName("android.util.Singleton");
        Field instanceField = forName2.getDeclaredField("mInstance");
        instanceField.setAccessible(true);
        // 系统的iActivityManagerObject对象
        Object iActivityManagerObject = instanceField.get(defaultValue);
        // 钩子
        Class<?> iActivityManagerIntercept  = Class.forName("android.app.IActivityManager");
        AmsInvocationHandler handler = new AmsInvocationHandler(iActivityManagerObject);
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{iActivityManagerIntercept},handler);
        instanceField.set(defaultValue,proxy);
    }


    class AmsInvocationHandler implements InvocationHandler{

        private Object iActivityManagerObject;

        public AmsInvocationHandler(Object iActivityManagerObject){
            this.iActivityManagerObject = iActivityManagerObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Log.i("INFO","invoke");
            if ("startActivity".contains(method.getName())){
                // 偷天换日
                Intent intent = null;
                int index = 0;
                for (int i = 0;i<args.length;i++){
                    if (args[i] instanceof  Intent){
                        intent = (Intent) args[i]; // 原意图，过不了安检
                        index = i;
                        break;
                    }
                }
                Intent proxyIntent = new Intent();
                ComponentName componentName = new ComponentName(mContext,mProxyActivity);
                proxyIntent.setComponent(componentName);
                proxyIntent.putExtra("oldIntent",intent);

                args[index] = proxyIntent;
                return method.invoke(iActivityManagerObject,args);

            }
            return method.invoke(iActivityManagerObject,args);
        }
    }


    public void hookSystemHandler(){
        try{
            Class<?> forName = Class.forName("android.app.ActivityThread");
            Field currentActivityThreadField = forName.getDeclaredField("sCurrentActivityThread");
            currentActivityThreadField.setAccessible(true);
            Object activityThreadValue =  currentActivityThreadField.get(null);
            Field handlerField = forName.getDeclaredField("mH");
            handlerField.setAccessible(true);
            // mH的变量值
            Handler handleObject = (Handler) handlerField.get(activityThreadValue);
            Field callbackField = Handler.class.getDeclaredField("mCallback");
            callbackField.setAccessible(true);
            callbackField.set(handleObject,new ActivityThreadHandlerCallback(handleObject));
        }catch (Exception e){
            Log.i("INFO",e.toString());
        }
    }


    class ActivityThreadHandlerCallback implements Handler.Callback{

        Handler mHandler;

        public ActivityThreadHandlerCallback(Handler handler) {
            mHandler = handler;
        }

        @Override
        public boolean handleMessage(Message msg) {
            Log.i("INFO","message callback");
            if (msg.what == 100){
                Log.i("INFO","launchActivity");
                handleLaunchActivity(msg);
            }

            mHandler.handleMessage(msg);
            return true;
        }
    }

    private void handleLaunchActivity(Message msg) {
        Object obj = msg.obj; // ActivityClientRecord
        try {
            Field intentField = obj.getClass().getDeclaredField("intent");
            intentField.setAccessible(true);
            intentField.get(obj);
            Intent proxyIntent = (Intent) intentField.get(obj);
            Intent realIntent = proxyIntent.getParcelableExtra("oldIntent");
            if (realIntent != null){
                proxyIntent.setComponent(realIntent.getComponent());
            }
        }catch (Exception e){
            Log.i("INFO",e.toString());
        }

    }


}
