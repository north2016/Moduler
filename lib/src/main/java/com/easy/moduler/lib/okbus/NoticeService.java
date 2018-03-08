package com.easy.moduler.lib.okbus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.easy.moduler.lib.Constants;
import com.easy.moduler.lib.utils.LogUtils;

/**
 * Created by baixiaokang on 18/3/7.
 * 客户端的唤醒服务
 */

public class NoticeService extends Service {

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
        BaseAppModuleApp mBaseAppModuleApp = BaseAppModuleApp.getBaseApplication();
        if (!mBaseAppModuleApp.mBaseModule.isConnected.get()) {
            LogUtils.logOnUI(Constants.TAG, getPackageName() + " 我被唤醒啦");
            mBaseAppModuleApp.mBaseModule.init();
            mBaseAppModuleApp.mBaseModule.afterConnected();
            return BaseAppModuleApp.getBaseApplication().mBaseModule.mWorkThread.clientHandler.getBinder();
        } else {
            LogUtils.logOnUI(Constants.TAG, getPackageName() + " 我本来就是醒的");
            return null;
        }
    }
}