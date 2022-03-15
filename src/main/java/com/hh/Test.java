package com.hh;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Test {
    public static void main(String[] args) throws IOException {
        List<String> list = FileUtils.readCsvColumn("C:\\Users\\ab875\\Desktop\\FoodDiet\\metabolites_in_human.csv", 6, ";");
        for (String s :
                list) {
            System.out.println(s);
        }

    }
}

