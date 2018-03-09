package com.easy.moduler.lib.okbus;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;

import com.easy.moduler.annotation.Bus;
import com.easy.moduler.lib.Constants;
import com.easy.moduler.lib.router.Router;
import com.easy.moduler.lib.utils.LogUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by baixiaokang on 18/3/6.
 * 模块基类
 */

public abstract class BaseModule implements IModule {

    public WorkThread mWorkThread;

    public AtomicBoolean isConnected = new AtomicBoolean(false);// 是否连接上服务器

    private BaseModule mBaseModule;

    private CountDownLatch latch;
    private AtomicReference<Messenger> resultRef;

    public void init(CountDownLatch latch, AtomicReference<Messenger> resultRef) {
        this.latch = latch;
        this.resultRef = resultRef;
        init();
    }

    @Override
    public void init() {
        mBaseModule = this;
        mWorkThread = new WorkThread();
        mWorkThread.start();

        OkBus.getInstance().register(Constants.ROUTER_OPEN_URL, new Event() {
            @Override
            public void call(Message msg) {
                String url = (String) msg.obj;
                Router.openLocalUrl(BaseAppModuleApp.getBaseApplication(), url);
            }
        }, Bus.UI);
    }

    public class WorkThread extends Thread {
        Handler mHandler;
        public Messenger clientHandler;

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new ClientHandler();
            clientHandler = new Messenger(mHandler);
            if(resultRef!=null){
                try {
                    resultRef.set(clientHandler);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }
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
            OkBus.getInstance().initModule(mBaseModule, mServiceMessenger, getModuleId(), mWorkThread.clientHandler);
            afterConnected();
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConnected.set(false);
            LogUtils.logOnUI(Constants.TAG, "ServiceConnection-->onServiceDisconnected  服务器断开");
            //BaseAppModuleApp.getBaseApplication().connectService();断开后自动重连
        }
    };
}
