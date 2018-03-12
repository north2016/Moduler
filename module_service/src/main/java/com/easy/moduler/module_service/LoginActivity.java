package com.easy.moduler.module_service;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.easy.moduler.annotation.Bus;
import com.easy.moduler.annotation.RouterRule;
import com.easy.moduler.lib.Constants;
import com.easy.moduler.lib.okbus.Event;
import com.easy.moduler.lib.okbus.OkBus;
import com.easy.moduler.lib.router.Router;
import com.easy.moduler.lib.utils.LogUtils;


@RouterRule("moduleHost://com.module.login")
public class LoginActivity extends AppCompatActivity {
    Event mEvent, mLogEvent;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        OkBus.getInstance().register(Constants.MODULE_B_EVENT001, mEvent = msg -> LogUtils.logOnUI(Constants.TAG, "service进程收到消息:Message-->service: " + msg.obj));

        TextView tvLog = findViewById(R.id.tv_log);
        OkBus.getInstance().register(Constants.MODULE_PRINT_LOG, mLogEvent = msg -> {
            String log = tvLog.getText().toString();
            tvLog.setText(msg.obj + "\n" + log);
        }, Bus.UI);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkBus.getInstance().unRegister(mEvent);
        OkBus.getInstance().unRegister(mLogEvent);
    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.bt_a) {
            Router.open(this, "moduleHost://com.module.moduleA?module=a");//子组件打开url时需要告知模块id，用于自动唤醒，主app则不需要
        } else if (i == R.id.bt_b) {
            Router.open(this, "moduleHost://com.module.moduleB?module=b");
        }
    }
}

