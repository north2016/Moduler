package com.easy.moduler.lib.router;

import android.app.Activity;

import java.util.Map;

public interface IRouterRulesCreator {
    void initRule(Map<String, Class<? extends Activity>> rules);
}
