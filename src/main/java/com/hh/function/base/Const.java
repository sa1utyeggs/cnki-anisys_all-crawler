package com.hh.function.base;

import java.util.HashSet;

/**
 * @author 86183
 */
public class Const {

    /**
     * URL
     */
    public static final String SEARCH_URL = "https://kns.cnki.net/kns8/defaultresult/index";
    public static final String VISUAL_URL = "https://kns.cnki.net/kns8/Visual/Center";
    public static final String SQL_VAL_URL = "https://kns.cnki.net/kns8/Brief/GetGridTableHtml";
    public static final String BASE_URL = "https://kns.cnki.net";

    /**
     * 时间常量
     */
    public static final long BASE_EXCEPTION_TIME = 500;
    public static final long BASE_INTERVAL_TIME = 100;

    /**
     * 高级搜索时的类型
     * KY：安装关键词搜索
     * SU：按照主题搜索
     */
    public static final String SEARCH_KY = "KY";
    public static final String SEARCH_SU = "SU";

    /**
     * 基础词汇
     */
    public static final String CANCER = "癌";
    public static final String TUMOR = "肿瘤";
    public static final String CEIL = "细胞";

    /**
     * 疾病数据挖掘状态
     */
    public static final int NOT_FINISHED = 0;
    public static final int FINISHED = 1;

    /**
     * 代理 IP 过期时间余量 (秒)
     */
    public static final int IP_TIME = 50;

    public static HashSet<String> EXCLUSION_WORDS = new HashSet<>();

    public static final String SOURCE_ZHIWANG = "中国知网";


    public static final String HTTP_GET = "get";
    public static final String HTTP_POST = "post";

    static {
        EXCLUSION_WORDS.add("方法");
        EXCLUSION_WORDS.add("目的");
    }
}
