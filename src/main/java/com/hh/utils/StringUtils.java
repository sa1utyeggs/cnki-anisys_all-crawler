package com.hh.utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

/**
 * @author ab875
 */
public class StringUtils {
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }

    public static String formatComma(String string) {
        return string.replace(",", "，");
    }

    public static boolean startWith(String sentence, Set<String> exclusions) {
        for (String exclusion : exclusions) {
            if (sentence.startsWith(exclusion)) {
                return true;
            }
        }
        return false;
    }

    private static final String nums = "零一二三四五六七八九";
    private static final String units = "十百千万亿";
    private static final int[] unitsScales = {10, 100, 1000, 10000, 100000000};

    private static long parseChineseNumberWithoutUnit(String str) {
        long result = 0L;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            int num = nums.indexOf(ch);
            assert num >= 0;
            result = result * 10L + num;
        }
        return result;
    }

    private static double parseChineseNumberWithUnit(String str) {
        if (containsUnit(str)) {
            return parseChineseNumberWithoutUnit(str);
        }

        long num = 0L;

        int lastUnit = units.length();
        boolean lastIsNum = false;

        Deque<Long> stack = new ArrayDeque<>();

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            int unit = units.indexOf(ch);
            if (unit >= 0) {
                // 遇到单位
                if (lastIsNum) {
                    if (unit < lastUnit) {
                        num *= unitsScales[unit];
                    }
                    stack.addLast(num);
                    if (unit >= lastUnit) {
                        if (compactStack(stack, unit)) {
                            // 单位顺序不符合预期（非法）
                            return Double.NaN;
                        }
                    }
                } else {
                    assert !lastIsNum;
                    if (unit < lastUnit) {
                        // 单位后跟着更小的单位（非法）：二百十
                        return Double.NaN;
                    }
                    if (unit >= lastUnit) {
                        if (compactStack(stack, unit)) {
                            // 单位顺序不符合预期（非法）
                            return Double.NaN;
                        }
                    }
                }

                lastUnit = unit;
                lastIsNum = false;
                num = 0L;
            } else {
                // 遇到数字
                if (lastIsNum && num != 0) {
                    if (containsUnit(str)) {
                        // 遇到连续数字（开头已处理，理论上不会走到这里）
                        return parseChineseNumberWithoutUnit(str);
                    } else {
                        // 遇到连续数字，并且带着单位（非法）
                        return Double.NaN;
                    }
                }
                num = nums.indexOf(ch);
                lastIsNum = true;
            }
        }

        // 边界条件：末尾的个位数字入栈
        if (lastIsNum) {
            stack.addLast(num);
        }

        // 弹栈，并计算栈中使用数字之和，同时检查数字单位从小到大顺序
        long total = 0L;
        while (!stack.isEmpty()) {
            num = stack.pollLast();
            if (num <= total) {
                return Double.NaN;
            }
            total += num;
        }

        return total;
    }

    private static boolean compactStack(Deque<Long> stack, int unit) {
        long unitNum = unitsScales[unit];
        long total = 0L;
        boolean pop = false;
        while (!stack.isEmpty() && stack.peekLast() <= unitNum) {
            total += stack.pollLast();
            pop = true;
        }
        if (pop) {
            total *= unitsScales[unit];
            stack.addLast(total);
            return false;
        }
        return true;
    }

    private static boolean containsUnit(String str) {
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            int unit = units.indexOf(ch);
            if (unit >= 0) {
                return false;
            }
        }
        return true;
    }

    public static double parseChineseNumber(String str) {
        // 可能出现的汉字：点 廿卅 零一二三四五六七八九 十百千万亿
        str = str.replace("廿", "二十").replace("卅", "三十");
        int pointOffset = str.indexOf('点');
        String intPart = str;
        String decimalPart = "";
        if (pointOffset >= 0) {
            intPart = str.substring(0, pointOffset);
            decimalPart = str.substring(pointOffset + 1);
        }

        double intNumber = parseChineseNumberWithUnit(intPart);
        long decimalNumber = parseChineseNumberWithoutUnit(decimalPart);

        return intNumber + Double.parseDouble("." + decimalNumber);
    }

    public static void testParseChineseNumber(String str, double expected) {
        double actual = parseChineseNumber(str);
        System.out.println(str + " => " + actual +" " + (expected == actual));

        if (Double.isNaN(expected)) {
            // NOTE: Double.NaN != Double.NaN
            assert Double.isNaN(actual) : str + " != " + expected;
        } else {
            assert expected == actual : str + " != " + expected;
        }
    }

    public static void main(String[] args) {
        // 测试代码
        testParseChineseNumber("一二三四五", 123456);
        testParseChineseNumber("二零一二", 2012);
        testParseChineseNumber("一亿二千万零三万四千五百六十七", 120034567);
        testParseChineseNumber("一百二十万三千零四十亿五千万零六十万七千八百九十", 120304050607890L);

        testParseChineseNumber("一万万", 100000000);
        testParseChineseNumber("一三五七九点二四六八零", 13579.24680);

        testParseChineseNumber("一万千", Double.NaN);
        testParseChineseNumber("二二十", Double.NaN);
        testParseChineseNumber("二十三十", Double.NaN);
    }


}
