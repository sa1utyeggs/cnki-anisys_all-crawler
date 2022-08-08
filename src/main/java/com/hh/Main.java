package com.hh;

import com.alibaba.fastjson.JSONObject;
import com.hh.entity.MainSentence;
import com.hh.function.Base;
import com.hh.function.Const;
import com.hh.function.PaperDetail;
import com.hh.function.PaperNum;
import com.hh.utils.DataBaseUtils;
import com.hh.utils.FileUtils;
import com.hh.utils.JsonUtils;
import com.hh.utils.StringUtils;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.awt.print.Paper;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ab875
 */
public class Main {

    public static ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
    public static DataBaseUtils dataBaseUtils = context.getBean("dataBaseUtils", DataBaseUtils.class);

    public static void main(String[] args) throws Exception {
        // searchAndInsert();
        PaperDetail.insertPaperInfo("姜黄素", "结肠癌", Const.SEARCH_KY, true);
    }

    /**
     * 在知网中查询信息，并插入数据库
     */
    public static void searchAndInsert() {
        // 参数：代谢物，疾病
        // type：SU按照主题搜索，KY按照关键词搜索
        // test：true不记录到数据库并输出detail信息，false记录到数据库不输出detail信息
        // PaperDetail.insertPaperInfo("姜黄素", "结肠癌", Const.SEARCH_KY, false);
        // PaperNum.getMetabolitesDiseasePaperNum("结肠癌", Const.SEARCH_KY, false, Integer.MAX_VALUE,false);
        String disease = "结肠癌";
        try {
            List<String> metabolites = dataBaseUtils.getMetaboliteByMaxPaperNumber(disease, Integer.MAX_VALUE);
            for (String metabolite : metabolites) {
                PaperDetail.insertPaperInfo(metabolite, disease, Const.SEARCH_KY, false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读csv中的 metabolite，并插入别名
     */
    public static void readCsvAndInsert() throws Exception {
        List<List<String>> lists = FileUtils.readCsvColumns("C:\\Users\\86183\\Desktop\\DaChuang\\杂项\\中药（已处理）.csv", 1, 2);
        dataBaseUtils.insertMetabolite(lists.get(0));
        List<String> alias = lists.get(1);
        ArrayList<List<String>> lists1 = new ArrayList<>();
        for (String s : alias) {
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(s);
            lists1.add(tmp);
        }
        System.out.println(lists1);
        try {
            dataBaseUtils.insertMetaboliteAlias(lists.get(0), lists1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void getSentencesForNLP(){
        try {
            List<String> strings = FileUtils.readCsvColumn("C:\\Users\\86183\\Desktop\\DaChuang\\杂项\\nlp\\nlp不相关增强.csv", 1, null);
            List<Long> collect = strings.stream().map(Long::valueOf).collect(Collectors.toList());
            List<MainSentence> mainSentences = dataBaseUtils.getMainSentences(collect);
            System.out.println(collect);
            FileUtils.objectListToCsv(null, mainSentences, MainSentence.class, "C:\\Users\\86183\\Desktop\\test1.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
