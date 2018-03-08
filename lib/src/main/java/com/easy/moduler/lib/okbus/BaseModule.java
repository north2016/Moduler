package com.easy.moduler.lib.okbus;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;

import com.easy.moduler.lib.Constants;
import com.easy.moduler.lib.utils.LogUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by baixiaokang on 18/3/6.
 * 模块基类
 */

public abstract class BaseModule implements IModule {

    public WorkThread mWorkThread;

    public AtomicBoolean isConnected = new AtomicBoolean(false);// 是否连接上服务器

    private BaseModule mBaseModule;

    @Override
    public void init() {
        mBaseModule = this;
        mWorkThread = new WorkThread();
        mWorkThread.start();
    }

    public class WorkThread extends Thread {
        Handler mHandler;
        public Messenger clientHandler;

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new ClientHandler();
            clientHandler = new Messenger(mHandler);
            Looper.loop();
        }

        public void quit() {
            mHandler.getLooper().quit();
        }
    }

    public ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.logOnUI(Constants.TAG, "ServiceConnection-->onServiceConnected 已自动唤醒服务器");

            Messenger mServiceMessenger = new Messenger(service);
            //如果是组建，注册组建信息
            OkBus.getInstance().initModule(mBaseModule, mServiceMessenger, getModuleId(), mWorkThread.clientHandler);
            afterConnected();
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConnected.set(false);
            LogUtils.logOnUI(Constants.TAG, "ServiceConnection-->onServiceDisconnected  服务器断开");
        }
    };
}
