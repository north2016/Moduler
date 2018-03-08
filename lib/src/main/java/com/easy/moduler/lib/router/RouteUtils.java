package com.easy.moduler.lib.router;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

public class RouteUtils {

    /**
     * 判断intent来源是否和指定的rule匹配
     *
     * @param intent
     * @param rule
     * @return
     */
    public static boolean matchRule(Intent intent, String rule) {
        String stringUrl = getRouteUrl(intent);
        if (stringUrl == null || rule == null) {
            return false;
        }

        Uri parse = Uri.parse(stringUrl);
        Uri ruleParse = Uri.parse(rule);

        return TextUtils.equals(parse.getScheme(), ruleParse.getScheme())
                && TextUtils.equals(parse.getHost(), ruleParse.getHost())
                && TextUtils.equals(parse.getPath(), ruleParse.getPath());
    }

    /**
     * 获取传递过来的url
     *
     * @param intent
     * @return
     */
    public static String getRouteUrl(Intent intent) {
        String url = "";
        if (intent != null) {
            url = intent.getStringExtra(Router.INTENT_KEY_ROUTE_URL);
        }
        return url;
    }

    /**
     * 从intent中获取指定key对应的值
     *
     * @param intent
     * @param key
     * @param t      返回值类型
     * @param <T>
     * @return
     */
    public static <T> T getQueryParameter(Intent intent, String key, Class<T> t) {
        String url = getRouteUrl(intent);
        return getQueryParameter(url, key, t);
    }

    /**
     * 获取指定key对应的值
     *
     * @param url
     * @param key
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T getQueryParameter(String url, String key, Class<T> t) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(key)) return getDefaultValue(t);

        Uri parse = Uri.parse(url);
        String str = parse.getQueryParameter(key);
        if (t == String.class) { // string
            return (T) str;

        } else if (t == Integer.class || t == int.class) { // int
            int result = -1;
            try {
                result = Integer.parseInt(str);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return (T) Integer.valueOf(result);

        } else if (t == Long.class) { // long
            long result = -1;
            try {
                result = Long.parseLong(str);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return (T) Long.valueOf(result);

        } else {
            throw new IllegalArgumentException(" unsupported parameter ! ");
        }
    }

    private static <T> T getDefaultValue(Class<T> t) {
        if (t == String.class) {
            return (T) "";
        } else if (t == Integer.class || t == int.class) {
            return (T) Integer.valueOf(-1);
        } else if (t == Long.class) {
            return (T) Long.valueOf(-1);
        }
        return (T) "";
    }
}

