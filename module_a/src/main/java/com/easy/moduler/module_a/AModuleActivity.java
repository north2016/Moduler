package com.easy.moduler.module_a;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import com.easy.moduler.annotation.Bus;
import com.easy.moduler.annotation.RouterRule;
import com.easy.moduler.lib.Constants;
import com.easy.moduler.lib.okbus.Event;
import com.easy.moduler.lib.okbus.OkBus;
import com.easy.moduler.lib.okbus.ServiceBus;
import com.easy.moduler.lib.utils.LogUtils;

@RouterRule("moduleHost://com.module.moduleA")
public class AModuleActivity extends AppCompatActivity {
    Event mBEvent, mLogEvent;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amodule);
        EditText editText = findViewById(R.id.et_uid);

        ServiceBus.getInstance().registerService(Constants.SERVICE_A_UID, msg -> {
            LogUtils.logOnUI(Constants.TAG, "a 进程收到[服务请求]消息:ServiceMessage-->hello:  " + Integer.toHexString(Math.abs(msg.what)));
            return editText.getText().toString();
        });

        TextView tvLog = findViewById(R.id.tv_log);
        OkBus.getInstance().register(Constants.MODULE_B_EVENT001, mBEvent = msg -> LogUtils.logOnUI(Constants.TAG, "a 进程收到消息:Message-->a: " + msg.obj));
        OkBus.getInstance().register(Constants.MODULE_PRINT_LOG, mLogEvent = msg -> {
            String log = tvLog.getText().toString();
            tvLog.setText(msg.obj + "\n" + log);
        }, Bus.UI);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkBus.getInstance().unRegister(mBEvent);
        OkBus.getInstance().unRegister(mLogEvent);
    }
}
