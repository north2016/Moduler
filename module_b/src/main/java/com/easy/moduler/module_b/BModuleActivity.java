package com.easy.moduler.module_b;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.easy.moduler.annotation.Bus;
import com.easy.moduler.annotation.RouterRule;
import com.easy.moduler.lib.Constants;
import com.easy.moduler.lib.okbus.Event;
import com.easy.moduler.lib.okbus.OkBus;
import com.easy.moduler.lib.okbus.ServiceBus;
import com.easy.moduler.lib.utils.LogUtils;

@RouterRule("moduleHost://com.module.moduleB")
public class BModuleActivity extends AppCompatActivity {

    private static final String TAG = "Message ：BModuleActivity";
    private Event mLogEvent;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmodule);

        TextView tvLog = findViewById(R.id.tv_log);
        OkBus.getInstance().register(Constants.MODULE_PRINT_LOG, mLogEvent = msg -> {
            String log = tvLog.getText().toString();
            tvLog.setText(msg.obj + "\n" + log);
        }, Bus.UI);

        LogUtils.logOnUI(Constants.TAG, "b 进程发送消息");
        OkBus.getInstance().onEvent(Constants.MODULE_B_EVENT001, "b-->Message:恭喜发财！BBB");

        /**
         * 异步调用远端服务
         */
        findViewById(R.id.bt_1).setOnClickListener(v -> {

            ServiceBus.getInstance().fetchService(Constants.SERVICE_A_UID, msg -> {
                LogUtils.logOnUI(Constants.TAG, "b 进程收到[异步服务返回]消息:  获取到的UID-->" + msg.obj);
                Toast.makeText(BModuleActivity.this,
                        "b 进程收到[异步服务返回]消息:  获取到的UID-->" + msg.obj,
                        Toast.LENGTH_SHORT).show();
            });
        });

        /**
         * 同步调用远端服务
         */
        findViewById(R.id.bt_2).setOnClickListener(v -> {
            try {
                String uid = ServiceBus.getInstance().fetchService(Constants.SERVICE_A_UID);
                LogUtils.logOnUI(Constants.TAG, "b 进程收到[同步服务返回]消息:  获取到的UID-->" + uid);
                Toast.makeText(BModuleActivity.this, "b 进程收到[同步服务返回]消息:  获取到的UID-->" + uid, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkBus.getInstance().unRegister(mLogEvent);
    }
}
