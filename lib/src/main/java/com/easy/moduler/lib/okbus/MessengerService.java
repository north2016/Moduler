package com.easy.moduler.lib.okbus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.support.annotation.Nullable;

import com.easy.moduler.lib.Constants;
import com.easy.moduler.lib.utils.LogUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Created by baixiaokang on 18/2/27.
 * <p>
 * 让客户端和服务端能互相发送和接受消息。
 * <p>
 * 首相我们回想一下：
 * <p>
 * 发送消息必须要得到远端的Binder对象来构造Messenger；
 * 处理消息必须新建一个Handler来构造Messenger；
 * 就以上这两点我们来重新写一下service端和Client端的代码：
 */

public class MessengerService extends Service {
    public WorkThread mWorkThread;
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<Messenger> resultRef = new AtomicReference<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        try {
            latch.await(10, TimeUnit.SECONDS); //最多等待10秒
        } catch (Exception e) { //等待中断
            e.printStackTrace();
        }
        Messenger mMessenger = resultRef.get();
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i(Constants.TAG + " essengerService", "MessengerService -->onCreate");
        mWorkThread = new WorkThread();
        mWorkThread.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i(Constants.TAG + " essengerService", "MessengerService -->onDestroy  quit");
        mWorkThread.quit();
    }


    public class WorkThread extends Thread {
        public ServiceHandler mHandler;

        @Override
        public void run() {
            Looper.prepare();
            LogUtils.i(Constants.TAG + " essengerService", "MessengerService -->new ServiceHandler");
            mHandler = new ServiceHandler();
            Messenger mMessenger = new Messenger(mHandler);
          //  OkBus.getInstance().mServiceMessenger = mMessenger;
            try {
                resultRef.set(mMessenger);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
            Looper.loop();
        }

        public void quit() {
            mHandler.getLooper().quit();
        }
    }
}