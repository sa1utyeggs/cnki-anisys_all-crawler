package com.hh.utils;

public class StringUtils {
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }

    public static String format(String string){
        return string.replace(" ", "")
                .replace("，",",")
                .replace("【","[")
                .replace("】","]");
    }
}
