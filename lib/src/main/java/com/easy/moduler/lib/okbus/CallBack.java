package com.easy.moduler.lib.okbus;

import android.os.Message;

/**
 * Created by baixiaokang on 18/3/2.
 */

public interface CallBack<T> {
    T onCall(Message msg);
}
