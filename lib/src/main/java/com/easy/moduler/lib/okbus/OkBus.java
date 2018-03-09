package com.easy.moduler.lib.okbus;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.SparseArray;

import com.easy.moduler.annotation.Bus;
import com.easy.moduler.lib.Constants;
import com.easy.moduler.lib.utils.LogUtils;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by baixiaokang on 16/11/15.
 */
@SuppressWarnings("unchecked")
public class OkBus {
    private ConcurrentHashMap<Integer, CopyOnWriteArrayList<SparseArray<Event>>> mEventList = new ConcurrentHashMap<>();//存储所有事件ID以及其回调
    private ConcurrentHashMap<Integer, Object> mStickyEventList = new ConcurrentHashMap<>();//存储粘连事件ID以及其数据
    private ScheduledExecutorService mPool = Executors.newScheduledThreadPool(5);
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private OkBus() {
    }

    private static class Holder {
        public static OkBus eb = new OkBus();
    }

    public static OkBus getInstance() {
        return Holder.eb;
    }

    public OkBus register(int tag, Event ev) {
        register(tag, ev, Bus.DEFAULT);
        return this;
    }

    public OkBus register(int tag, final Event ev, int thread) {
        SparseArray<Event> mEvent = new SparseArray<>();
        mEvent.put(thread, ev);
        if (mEventList.get(tag) != null) {
            mEventList.get(tag).add(mEvent);
        } else {
            CopyOnWriteArrayList<SparseArray<Event>> mList = new CopyOnWriteArrayList<>();
            mList.add(mEvent);
            mEventList.put(tag, mList);
        }
        LogUtils.i(Constants.TAG, "Bus register   " + tag + " :" + mEventList.get(tag).size());
        if (mStickyEventList.get(tag) != null) {//注册时分发粘连事件
            final Message msg = Message.obtain();
            msg.obj = mStickyEventList.get(tag);
            msg.what = tag;
            callEvent(msg, ev, thread);
            LogUtils.i(Constants.TAG, "mStickyEvent register  and  onEvent " + tag + " :" + mEventList.get(tag).size());
        }
        return this;
    }

    private void callEvent(final Message msg, final Event ev, int thread) {
        switch (thread) {
            case Bus.DEFAULT:
                ev.call(msg);
                break;
            case Bus.UI:
                mHandler.post(() -> ev.call(msg));
                break;
            case Bus.BG:
                mPool.execute(() -> ev.call(msg));
                break;
        }
    }

    /**
     * 一次性注销所有当前事件监听器
     *
     * @param ev
     * @return
     */
    public OkBus unRegister(Event ev) {
        Enumeration keys = mEventList.keys();
        while (keys.hasMoreElements()) {
            int key = (int) keys.nextElement();
            CopyOnWriteArrayList<SparseArray<Event>> list = mEventList.get(key);
            for (SparseArray<Event> item : list) {
                if (item.indexOfValue(ev) >= 0) {
                    list.remove(item);
                    LogUtils.i(Constants.TAG, "remove Event " + "key :" + key + "   keys:" + item.toString());
                }
            }
        }
        return this;
    }

    public OkBus unRegister(int tag) {
        if (mEventList.get(tag) != null)
            mEventList.remove(tag);
        return this;
    }


    public OkBus onEvent(int tag) {
        onEvent(tag, null);
        return this;
    }

    public OkBus onStickyEvent(int tag, Object data) {
        LogUtils.i(Constants.TAG, "Bus onStickyEvent " + tag + " ");
        mStickyEventList.put(tag, (data == null ? tag : data));
        onEvent(tag, data);
        return this;
    }

    public OkBus onStickyEvent(int tag) {
        onStickyEvent(tag, null);
        return this;
    }

    /**
     * @param tag  发送消息的事件ID
     * @param data 发送消息的数据
     * @return
     */
    public OkBus onEvent(int tag, Object data) {

        String hex = Integer.toHexString(Math.abs(tag));
        LogUtils.i("Message OkBus", "onEvent  " + (tag > 0 ? "[普通]" : "[服务]") + "  tag: " + hex);

        //1、本地先处理非服务消息
        if (tag >= 0) onLocalEvent(tag, data);

        //2、如果是组建化，向服务器发消息
        if (isModule.get()) {
            if (!isModuleConnected()) {
                LogUtils.i("Message OkBus", "发消息失败，服务已经断开链接，尝试重新打开服务，进行发消息");
                BaseAppModuleApp.getBaseApplication().connectService();
                return this;
            }
            if (data == null || data instanceof Serializable) {
                Message newMsg = new Message();
                if (data != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.MESSAGE_DATA, (Serializable) data);
                    newMsg.setData(bundle);
                }
                newMsg.arg1 = mModuleId;
                newMsg.what = tag;
                try {
                    mServiceMessenger.send(newMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                assert false : "跨进程时，你传递的对象没有序列化！";
            }
        } else if (tag < 0) {//非组件化时本地处理服务消息
            onLocalEvent(tag, data);
        }
        return this;
    }

    /**
     * 触发本地消息事件
     *
     * @param tag
     * @param data
     */
    public void onLocalEvent(int tag, Object data) {
        Message msg = Message.obtain();
        msg.obj = data;
        msg.what = tag;
        //1、本地先处理消息

        CopyOnWriteArrayList<SparseArray<Event>> mEvents = mEventList.get(tag);
        if (mEvents != null) {
            LogUtils.i(Constants.TAG + " OkBus", "Bus onEvent " + tag + " :" + mEvents.size());
            for (SparseArray<Event> ev : mEvents)
                callEvent(msg, ev.valueAt(0), ev.keyAt(0));
        }
    }


    private AtomicBoolean isModule = new AtomicBoolean(false);// 是否是模块化
    public Messenger mServiceMessenger;
    private BaseModule mBaseModule;
    public int mModuleId;

    public void initModule(BaseModule mBaseModule, Messenger mServiceMessenger, int mModuleId, Messenger mClientMessenger) {
        this.mServiceMessenger = mServiceMessenger;
        this.mModuleId = mModuleId;
        this.mBaseModule = mBaseModule;
        isModule.set(true);
        mBaseModule.isConnected.set(true);

        Message msg = Message.obtain();
        Bundle data = new Bundle();
        data.putInt(Constants.REGISTER_ID, mModuleId);//注册模块
        msg.setData(data);
        msg.replyTo = mClientMessenger;   //将处理消息的Messenger绑定到消息上带到服务端
        try {
            mServiceMessenger.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isModule() {
        return isModule.get();
    }

    public boolean isModuleConnected() {
        return mBaseModule.isConnected.get();
    }
}
