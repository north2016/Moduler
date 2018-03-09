package com.easy.moduler.lib.router;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.easy.moduler.lib.Constants;
import com.easy.moduler.lib.okbus.OkBus;
import com.easy.moduler.lib.okbus.ServiceBus;
import com.easy.moduler.lib.utils.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 自定义url路由
 * <p>
 * 规则：
 * scheme  host  path  params
 * app://app.com/login?liveid={liveid}&name={name}
 * <p>
 * eg.
 * app://app.com/login?liveid=100&name=aaa
 * <p>
 * usage:
 * 1、注册添加规则 {@link Router#addRouterRule(IRouterRulesCreator)}
 * 2、打开指定的url {@link Router#open(android.content.Context, java.lang.String)}
 * <p>
 * Created by evan on 2017/6/2.
 */

public class Router {
    public static final String INTENT_KEY_ROUTE_URL = "router_route_url";
    /**
     * 存储规则的集合
     * url --- activity clazz 映射
     */
    private static Map<String, Class<? extends Activity>> rules = new HashMap<>();
    // private static Map<String, IRouterInterceptor> interceptors = new HashMap<>(); // 拦截器不需要多个，统一在一个拦截器里实现
    private static IRouterInterceptor routerInterceptor;
    private static Set<String> allRuleKeys = new HashSet<>();

    /**
     * 注册添加规则
     *
     * @param creator
     */
    public static void addRouterRule(IRouterRulesCreator creator) {
        creator.initRule(rules);
        allRuleKeys.addAll(rules.keySet());
    }

    public static void setInterceptor(IRouterInterceptor interceptor) {
        routerInterceptor = interceptor;
    }

    public static Intent getMatchIntent(Context context, String url) {
        String matchRuleKey = getMatchRuleKey(url);
        Class<? extends Activity> activityClazz = rules.get(matchRuleKey);
        if (activityClazz != null) {
            Intent intent = new Intent(context, activityClazz);
            intent.putExtra(INTENT_KEY_ROUTE_URL, url);
            return intent;
        }
        return null;
    }

    /**
     * 打开指定url
     *
     * @param url
     * @return
     */
    public static boolean open(Context context, String url) {
        if (OkBus.getInstance().isModule()) {
            if (!openLocalUrl(context, url)) {
                String module_name = RouteUtils.getQueryParameter(url, Constants.MODULE_NAME, String.class);
                ServiceBus.getInstance().noticeModule(module_name,0,url);
                return true;
            }
            return true;
        } else {
            return openLocalUrl(context, url);
        }
    }


    /**
     * 打开本地的指定url
     *
     * @param url
     * @return
     */
    public static boolean openLocalUrl(Context context, String url) {
        if (context == null || url == null) return false;
        try {
            // 1 是否注册url - class
            String matchRuleKey = getMatchRuleKey(url);
            Class<? extends Activity> activityClazz = rules.get(matchRuleKey);
            if (activityClazz != null) {
                // 2 匹配到已注册的url - class，构造intent
                Intent intent = new Intent(context, activityClazz);
                intent.putExtra(INTENT_KEY_ROUTE_URL, url);
                if (context instanceof Application) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                // 3 拦截器处理
                if (routerInterceptor == null || !routerInterceptor.intercept(context, url, intent)) {
                    // 4 无拦截器，或者拦截器未完全拦截，则启动对应的页面
                    context.startActivity(intent);
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private static String getMatchRuleKey(String url) {
        if (url == null) return "";

        Uri checkUrl = Uri.parse(url);
        String checkScheme = checkUrl.getScheme();
        String checkHost = checkUrl.getHost();
        String checkPath = checkUrl.getPath();
        Set<String> checkParameterNames = checkUrl.getQueryParameterNames();

        Set<String> ruleKeys = allRuleKeys;
        for (String ruleKey : ruleKeys) {
            Uri ruleUri = Uri.parse(ruleKey);
            if (!StringUtils.equalsIgnoreCase(ruleUri.getScheme(), checkScheme)
                    || !StringUtils.equalsIgnoreCase(ruleUri.getHost(), checkHost)
                    || !StringUtils.equalsIgnoreCase(ruleUri.getPath(), checkPath)) {
                continue;
            }


            Set<String> queryParameterNames = ruleUri.getQueryParameterNames();

            Set<String> ruleParameterNames = new HashSet<>(queryParameterNames); // queryParameterNames不可编辑
            Iterator<String> iterator = ruleParameterNames.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                // 如果是可选key，则从规则中移除
                if (key != null && key.startsWith("[") && key.endsWith("]")) {
                    iterator.remove();
                }
            }

            if (checkParameterNames.containsAll(ruleParameterNames)) {
                return ruleKey;
            }
        }

        return "";
    }
}
