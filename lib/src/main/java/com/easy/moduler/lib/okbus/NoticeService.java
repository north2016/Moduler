package com.easy.moduler.lib.okbus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.Nullable;

import com.easy.moduler.lib.Constants;
import com.easy.moduler.lib.utils.LogUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by baixiaokang on 18/3/7.
 * 客户端的唤醒服务
 */

public class NoticeService extends Service {


    private CountDownLatch latch = new CountDownLatch(1);
    private AtomicReference<Messenger> resultRef = new AtomicReference<>();


    /**
     * 收到唤醒通知之后，初始化模块，并自动去服务器注册
     *
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.logOnUI(Constants.TAG, getPackageName() + " 收到唤醒通知");
        BaseModule mBaseModule = BaseAppModuleApp.getBaseApplication().mBaseModule;
        if (!mBaseModule.isConnected.get()) {
            LogUtils.logOnUI(Constants.TAG, getPackageName() + " 我被唤醒啦");
            mBaseModule.init(latch, resultRef);
            mBaseModule.afterConnected();
            try {
                latch.await(2000, TimeUnit.SECONDS);
            } catch (Exception e) { //等待中断
                e.printStackTrace();
            }
        }
        return mBaseModule.mWorkThread.clientHandler.getBinder();
    }
}