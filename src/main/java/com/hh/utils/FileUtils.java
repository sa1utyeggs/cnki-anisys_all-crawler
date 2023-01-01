package com.hh.utils;

import com.hh.entity.MainSentence;
import com.hh.function.PaperDetail;
import com.opencsv.CSVWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ab875
 */
public class FileUtils {
    private static final Logger logger = LogManager.getLogger(FileUtils.class);


    public static List<String> readCsvColumn(String path, int column, String delimiter) throws IOException {
        boolean delimiterNull = StringUtils.isEmpty(delimiter);
        String line = null;
        // 注意设置编码
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
        ArrayList<String> returns = new ArrayList<>(500);
        // 跳过首行
        reader.readLine();
        while ((line = reader.readLine()) != null) {
            // 分割csv
            String[] fields = getFields(line);
            // 获得需要的 string
            String target = fields[column - 1];
            // 将得到的String以某种方式进行分割，并获得我们需要的那个最简单的 字符串
            if (!delimiterNull && !StringUtils.isEmpty(target)) {
                // 使用最短的搜索词
                target = getShortestSearchWord(target, delimiter);
                // 将那一列的字符串分割后，通过特定方法得到最终添加的字符串
                // 格式化词语
                returns.add(StringUtils.formatComma(target));
            } else {
                returns.add(StringUtils.formatComma(target));
            }
        }
        return returns;
    }


    public static List<List<String>> readCsvColumns(String path, Integer... columns) {
        ArrayList<List<String>> ans = new ArrayList<>(100);
        try {
            for (Integer column : columns) {
                ans.add(readCsvColumn(path, column, null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ans;
    }

    private static String[] getFields(String line) {
        List<String> fields = new LinkedList<>();
        line += " ";
        char[] chars = line.toCharArray();
        int totalLength = chars.length;
        boolean isFieldStart = true;
        // 记录游标index
        int pos = 0;
        // 记录该列的 长度
        int len = 0;
        // double quotation marks：双引号
        boolean dqm = false;
        for (char c : chars) {
            if (isFieldStart) {
                len = 0;
                isFieldStart = false;
            }
            if (c == '\"') {
                dqm = !dqm;
            }
            // pos == totalLength - 1 若最后没有逗号，就需要使用总长度来判断，
            // line += " "也是必须的；
            if (c == ',' && !dqm || pos == totalLength - 1) {
                fields.add(new String(chars, pos - len, len));
                isFieldStart = true;
            }
            pos++;
            len++;
        }
        return fields.toArray(new String[0]);
    }

    public static void stringListToCsv(String[] head, List<String[]> lines, String path) {
        try (CSVWriter writer = new CSVWriter(Files.newBufferedWriter(Paths.get(path)), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            if (head != null) {
                writer.writeNext(head);
            }
            writer.writeAll(lines);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> void objectListToCsv(String[] head, List<T> objs, Class<T> type, String path) throws Exception {
        Field[] fields = Arrays.stream(type.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers())).toArray(Field[]::new);
        int fieldsNum = fields.length;
        int size = objs.size();
        ArrayList<String[]> lines = new ArrayList<>(size);
        for (Object obj : objs) {
            String[] values = new String[fieldsNum];
            for (int i = 0; i < fieldsNum; i++) {
                fields[i].setAccessible(true);
                values[i] = fields[i].get(obj).toString();
            }
            lines.add(values);
            logger.info(Arrays.toString(values));
        }
        logger.info(lines);
        stringListToCsv(head, lines, path);
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

