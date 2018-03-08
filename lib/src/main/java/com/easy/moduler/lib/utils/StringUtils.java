package com.easy.moduler.lib.utils;

/**
 * Created by baixiaokang on 18/3/6.
 */

public class StringUtils {
    /**
     * 比较字符串不记大小写是否相同
     *
     * @param string1
     * @param string2
     * @return
     */
    public static boolean equalsIgnoreCase(String string1, String string2) {
        if (string1 == string2) return true;
        if (string1 == null) return false;
        return string1.equalsIgnoreCase(string2);
    }
}
