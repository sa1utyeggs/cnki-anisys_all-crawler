package com.hh;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author ab875
 */
public class JsonUtils {
    public static String getJsonFromFile(String fileName) throws IOException {
        String baseURL = JsonUtils.class.getResource("/").getPath();
        File jsonFile = new File(baseURL + fileName);
        String jsonString = FileUtils.readFileToString(jsonFile, "utf-8");
        return jsonString;
    }
    public static JSONObject getJsonObjectFromFile(String fileName) throws IOException {
        return JSONObject.parseObject(getJsonFromFile(fileName));
    }
    public static void main(String[] args) {
    }
}
