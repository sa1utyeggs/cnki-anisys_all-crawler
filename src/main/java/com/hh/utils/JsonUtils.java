package com.hh.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author ab875
 */
public class JsonUtils {
    public static String getJsonFromFile(String fileName) throws IOException {
        String baseUrl = JsonUtils.class.getResource("/").getPath();
        File jsonFile = new File(baseUrl + fileName);
        return FileUtils.readFileToString(jsonFile, "utf-8");
    }


    public static JSONObject getJsonObjectFromFile(String fileName) throws IOException {
        return JSONObject.parseObject(getJsonFromFile(fileName));
    }

}
