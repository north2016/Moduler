package com.easy.moduler.lib.okbus;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

import com.easy.moduler.lib.Constants;
import com.easy.moduler.lib.utils.LogUtils;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by baixiaokang on 18/3/1.
 * 服务消息处理器
 * <p>
 * a）、发送事件类型的消息
 * 1 、转发给自己的OkBus
 * 2.转发给其他模块的OkBus，来源模块除外
 * <p>
 * b）、模块注册消息
 * 1、存储Client端接受处理消息的Messenger来发送Message到Client
 * 2、通知Client模块注册成功
 */

public class ServiceHandler extends Handler {
    /**
     * 存储所有的模块id以及对应的客户端信使
     */
    private ConcurrentHashMap<Integer, Messenger> mClientMessengers = new ConcurrentHashMap<>();

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        try {
            Bundle bundle = msg.getData();
            int registerId = bundle.getInt(Constants.REGISTER_ID, -1);
            if (registerId > 0) {//注册模块类型的消息
                LogUtils.logOnUI(Constants.TAG, "handleMessage: msg = [收到注册模块类型的消息]: registerId: " + Integer.toHexString(registerId));

                Messenger client = msg.replyTo;
                mClientMessengers.put(registerId, client);//存储Client端接受处理消息的Messenger来发送Message到Client

                Message data = Message.obtain();
                Bundle mBundle = new Bundle();
                mBundle.putInt(Constants.REGISTER_RES, Constants.REGISTER_SEC);    //通知Client模块注册成功
                data.setData(mBundle);
                try {
                    client.send(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {//发送事件类型的消息
                String hex = Integer.toHexString(Math.abs(msg.what));
                LogUtils.logOnUI(Constants.TAG, "handleMessage: msg = [Service:收到" + (msg.what > 0 ? "普通" : "服务") + "类型的消息:" + hex + "]-->[转发给自己的OkBus]");

                //1、转发给自己的OkBus:
                OkBus.getInstance().onLocalEvent(msg.what, bundle.getSerializable(Constants.MESSAGE_DATA));

                //2.转发给其他模块的OkBus，来源模块除外
                Enumeration keys = mClientMessengers.keys();
                while (keys.hasMoreElements()) {
                    int moduleId = (int) keys.nextElement();
                    Messenger mMessenger = mClientMessengers.get(moduleId);
                    if (moduleId != msg.arg1) {//不是目标来源模块，进行分发
                        LogUtils.logOnUI(Constants.TAG, "handleMessage:转发给其他模块的OkBus: 消息Id-->: " + (msg.what > 0 ? "普通" : "服务") + hex + "消息  -->模块Id: " + Integer.toHexString(moduleId));
                        Message _msg = Message.obtain(msg);
                        try {
                            mMessenger.send(_msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.logOnUI(Constants.TAG, "handleMessage:Exception");
        }
    }
}