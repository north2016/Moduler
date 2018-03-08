package com.easy.moduler.lib.okbus;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.easy.moduler.lib.Constants;
import com.easy.moduler.lib.utils.LogUtils;


/**
 * Created by baixiaokang on 18/3/1.
 * 客户端消息处理器
 * <p>
 * 1、普通消息，转发给自己的OkBus
 * 2、模块注册消息，打印
 */

public class ClientHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Bundle bundle = msg.getData();
        boolean noticeFlag = bundle.getBoolean(Constants.NOTICE_MSG, false);
        if (noticeFlag) {//唤醒通知，自动注册
            BaseModule mBaseModule = BaseAppModuleApp.getBaseApplication().mBaseModule;
            OkBus.getInstance().initModule(mBaseModule, msg.replyTo, mBaseModule.getModuleId(), mBaseModule.mWorkThread.clientHandler);
            return;
        }

        int resCode = bundle.getInt(Constants.REGISTER_RES, -1);
        if (resCode < 0) {//收到普通消息
            String hex = Integer.toHexString(Math.abs(msg.what));
            LogUtils.i(Constants.TAG + " ClientHandler", "handleMessage: msg = [hello:收到" + (msg.what > 0 ? "普通" : "服务") + "事件类型的消息:" + hex + "]-->[转发给自己的OkBus]: " + msg);
            OkBus.getInstance().onLocalEvent(msg.what, bundle.getSerializable(Constants.MESSAGE_DATA));
        } else {//收到模块注册结果消息
            boolean isRegisterSec = resCode == Constants.REGISTER_SEC;
            if (isRegisterSec) {
                LogUtils.logOnUI(Constants.TAG, "handleMessage() : reply = [注册成功]");
            }
        }
    }
}