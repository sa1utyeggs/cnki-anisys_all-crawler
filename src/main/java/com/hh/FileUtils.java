package com.hh;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ab875
 */
public class FileUtils {

    public static List<String> readCsvColumn(String path, int column, String delimiter) throws IOException {
        String line = null;
        // 注意设置编码
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
        ArrayList<String> returns = new ArrayList<>(500);
        // 跳过首行
        reader.readLine();
        while ((line = reader.readLine()) != null) {
            // 分割csv
            String[] strings = line.split(",");
            // 获得需要的 string
            String target = strings[column - 1];
            // 将得到的String以某种方式进行分割，并获得我们需要的那个最简单的 字符串
            if (!StringUtils.isEmpty(target)) {
                String searchWord = null;
                // 使用最短的搜索词
                searchWord = getShortestSearchWord(target,delimiter);
                // 将那一列的字符串分割后，通过特定方法得到最终添加的字符串
                // 格式化词语
                returns.add(StringUtils.format(searchWord));
            }
        }
        return returns;
    }


    public static String getFirstSearchWord(String string, String delimiter) {
        // 使用第一个词作为搜索词
        String[] split = string.split(delimiter);
        return split[0];
    }

    public static String getShortestSearchWord(String string, String delimiter) {
        String[] split = string.split(delimiter);
        int index = -1;
        int minLength = Integer.MAX_VALUE;
        for (int i = 0; i < split.length; i++) {
            if (split[i].length() < minLength) {
                minLength = split[i].length();
                index = i;
            }
        }
        return split[index];
    }

}

