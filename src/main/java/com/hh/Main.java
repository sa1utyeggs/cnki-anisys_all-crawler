package com.hh;

import com.hh.entity.application.cnki.MainSentence;
import com.hh.entity.application.cnki.InsertTask;
import com.hh.function.application.CnkiDatabaseService;
import com.hh.function.application.PaperDetail;
import com.hh.function.application.PaperNum;
import com.hh.function.base.Const;
import com.hh.function.base.ContextSingletonFactory;
import com.hh.function.base.ThreadPoolFactory;
import com.hh.utils.FileUtils;
import org.springframework.context.ApplicationContext;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author ab875
 */
public class Main {

    public static ApplicationContext context = ContextSingletonFactory.getInstance();
    public static CnkiDatabaseService dataBaseUtils = context.getBean("dataBaseUtils", CnkiDatabaseService.class);
    public static ThreadPoolFactory threadPoolFactory = context.getBean("threadPoolFactory", ThreadPoolFactory.class);
    public static HashSet<String> exclusions = new HashSet<>();

    static {
        exclusions.add("方法");
        exclusions.add("目的");
    }

    public static void main(String[] args){
        // test()
        // start();
        // multiThreadStart();
    }

    public static void test() {
        searchAndInsert("鼻窦炎", true, true);
    }

    public static void multiThreadStart() {
        ExecutorService threadPool = threadPoolFactory.getThreadPool(ThreadPoolFactory.WORK_POOL_PREFIX);
        try {
            // 查询所有未完成 疾病名与 id；
            List<String> undoneDiseases = dataBaseUtils.getAllUndoneDisease();
            // 遍历疾病信息，并根据当前疾病的数据挖掘状态做相应的操作；
            for (String undoneDisease : undoneDiseases) {
                // 提交任务
                threadPool.submit(new InsertTask(undoneDisease));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void start() {
        try {
            // 查询所有疾病信息；
            List<String> diseases = dataBaseUtils.getAllDisease();
            // 遍历疾病信息，并根据当前疾病的数据挖掘状态做相应的操作；
            for (String disease : diseases) {
                int status = dataBaseUtils.getDiseaseStatus(disease);
                switch (status) {
                    case Const.NOT_FINISHED:
                        searchAndInsert(disease, true, false);
                        // 结束之后修改疾病的数据挖掘状态；
                        dataBaseUtils.setDiseaseStatus(disease, Const.FINISHED);
                        break;
                    case Const.FINISHED:
                        break;
                    default:
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 在知网中查询信息，并插入数据库
     */
    public static void searchAndInsert(String disease, boolean getAndInsertPaperNum, boolean test) {
        // 参数：代谢物，疾病
        // type：SU按照主题搜索，KY按照关键词搜索
        // test：true不记录到数据库并输出detail信息，false记录到数据库不输出detail信息
        // PaperDetail.insertPaperInfo("姜黄素", "结肠癌", Const.SEARCH_KY, false);
        if (getAndInsertPaperNum) {
            PaperNum.getAndInsertMetabolitesDiseasePaperNum(disease, Const.SEARCH_KY, test, Integer.MAX_VALUE, true);
        }
        int maxPaperNumPerTime = 500;
        try {
            List<String> metabolites = dataBaseUtils.getMetaboliteByMaxPaperNumber(disease, Integer.MAX_VALUE);
            for (String metabolite : metabolites) {
                // 若 没完成 则 继续完成
                if (!dataBaseUtils.isMetaboliteDiseaseChecked(metabolite, disease)) {
                    PaperDetail.insertPaperInfo(metabolite, disease, Const.SEARCH_KY, exclusions, maxPaperNumPerTime, false);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读 csv 文件中的 metabolite，并插入别名
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


    public static void getSentencesForNLP() {
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
