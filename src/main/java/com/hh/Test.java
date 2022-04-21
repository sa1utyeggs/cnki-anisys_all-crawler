package com.hh;

import com.hh.utils.FileUtils;

import java.io.IOException;
import java.util.List;

public class Test {
    public static void main(String[] args) throws IOException {
        List<String> list = FileUtils.readCsvColumn("C:\\Users\\ab875\\Desktop\\FoodDiet\\metabolites_in_human.csv", 6, ";");
        for (String s :
                list) {
            System.out.println(s);
        }

    }
}

