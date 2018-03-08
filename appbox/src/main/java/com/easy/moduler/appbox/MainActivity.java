package com.easy.moduler.appbox;

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


@RouterRule("moduleHost://com.module.main")
public class MainActivity extends AppCompatActivity {

    Event mLogEvent;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tvLog = findViewById(R.id.tv_log);
        OkBus.getInstance().register(Constants.MODULE_PRINT_LOG, mLogEvent = msg -> {
            String log = tvLog.getText().toString();
            tvLog.setText(msg.obj + "\n" + log);
        }, Bus.UI);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkBus.getInstance().unRegister(mLogEvent);
    }

    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.bt_a:
                Router.open(this, "moduleHost://com.module.moduleA");
                break;
            case R.id.bt_b:
                Router.open(this, "moduleHost://com.module.moduleB");
                break;
            case R.id.bt_s:
                Router.open(this, "moduleHost://com.module.login");
                break;
        }
    }
}
