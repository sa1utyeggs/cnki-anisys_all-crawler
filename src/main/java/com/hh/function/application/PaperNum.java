package com.hh.function.application;

import com.alibaba.fastjson.JSONObject;
import com.hh.function.system.Const;
import com.hh.function.system.ContextSingletonFactory;
import com.hh.utils.DataBaseUtils;
import com.hh.function.system.HttpConnectionPool;
import com.hh.utils.JsonUtils;
import com.hh.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.ApplicationContext;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 86183
 */
public class PaperNum {
    private static final ApplicationContext CONTEXT = ContextSingletonFactory.getInstance();
    private static final DataBaseUtils DATA_BASE_UTILS = CONTEXT.getBean("dataBaseUtils", DataBaseUtils.class);
    private static final HttpConnectionPool HTTP_CONNECTION_POOL = CONTEXT.getBean("httpConnectionPool", HttpConnectionPool.class);
    private static final Map<String, String> EXCESS_HEADERS = new HashMap<>(8);
    private static final Logger logger = LogManager.getLogger(PaperNum.class);

    static {
        EXCESS_HEADERS.put("referer", "https://kns.cnki.net/kns8/defaultresult/index");
    }

    /**
     * 获得代谢物-疾病的论文数量，并插入数据库
     * 支持更新操作（对于已经存在的 disease-metabolite 行，则进行更新操作）；
     *
     * @param disease        疾病名字
     * @param searchType     搜索的类型
     * @param test           如果是测试，不插入数据库
     * @param zeroNumberSave 对于查找结果为 0 的行，是否插入数据库
     */
    public static void getAndInsertMetabolitesDiseasePaperNum(String disease, String searchType, boolean test, int metaboliteLimit, boolean zeroNumberSave) {
        ArrayList<String> errorName = new ArrayList<>(10);
        try {
            // 获得所有的代谢物
            List<String> metabolites = DATA_BASE_UTILS.getMetabolites(metaboliteLimit);
            String key = null;
            int index = 0;
            int sum = 0;
            int error = 0;
            int number = 0;
            int flag = 0;
            for (String metabolite : metabolites) {
                index++;
                try {
                    key = metabolite + " " + disease;
                    // 将读取的字符串经过处理，转为数字（相关文献数量）
                    number = getPaperNum(metabolite, disease, searchType);

                    // 若不是测试，则插入数据库
                    if (!test) {
                        // 若 查询到的结果为 0，且不允许插入 number 为 0 的行
                        if (!(number == 0 && !zeroNumberSave)) {
                            // 存在 则 更新
                            if (!DATA_BASE_UTILS.isMetaboliteDiseaseExist(metabolite, disease)) {
                                flag = DATA_BASE_UTILS.insertMetaboliteDiseaseNumber(metabolite, disease, number);
                            } else {
                                flag = DATA_BASE_UTILS.updateMetaboliteDiseaseNumber(metabolite, disease, number);
                            }
                            if (flag == 1) {
                                sum++;
                            } else {
                                throw new Exception("数据库插入错误");
                            }
                        }
                    }

                } catch (Exception throwable) {
                    throwable.printStackTrace();
                    error++;
                    errorName.add(metabolite + " || " + disease);
                }
                // 实时输出结果
                System.out.printf("查找：%s || 查找结果：%s || 插入结果： %s || 目前插入行数：%s / 已扫描： %s || 错误数：%s\n", key, number, flag, sum, index, error);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 显示失败的插入
            logger.warn("本次查询失败的有：");
            errorName.forEach(System.out::println);
        }
    }

    /**
     * 获得相关文献的数量
     *
     * @param metabolite 代谢物
     * @param disease    疾病
     * @param type       搜索类型 Const
     * @return 文献数量
     * @throws Exception 异常
     */
    public static int getPaperNum(String metabolite, String disease, String type) throws Exception {
        Document document = PaperDetail.getPaperGridDocument(metabolite, disease, 1, "", 0, type);
        String pagerTitleCell = document.getElementsByClass("pagerTitleCell").text();
        String noContent = document.getElementsByClass("no-content").text();
        logger.info("pagerTitleCell: " + pagerTitleCell);
        logger.info("noContent: " + noContent);
        if (!noContent.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(pagerTitleCell.replaceAll("[^0-9]", ""));
    }


    /**
     * 获得部分的知网统计数据
     *
     * @param key 搜索词
     * @return 返回html
     * @throws Exception 异常
     */
    @Deprecated
    private static String getVisualData(String key) throws Exception {
        // 获得基础的visual数据，必须要有sqlVal
        String jsonString = JsonUtils.getJsonFromFile("template.json");
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        String sqlVal = getSqlVal(key);
        // 换 value
        JSONObject json = jsonObject.getJSONObject("json");
        json.getJSONObject("QNode").getJSONArray("QGroup").getJSONObject(0).getJSONArray("Items").getJSONObject(0).fluentPut("Value", key);

        // 换 sqlVal
        jsonObject.fluentPut("searchSql", sqlVal);
        jsonObject.fluentPut("json", json.toJSONString());


//        Connection connection = CONNECTION_FACTORY.getCnkiConnection(Const.VISUAL_URL);
//        CONNECTION_FACTORY.insertPostData(jsonObject, connection);
//        Document doc = connection.post();
        Document doc = HTTP_CONNECTION_POOL.post(Const.VISUAL_URL, jsonObject, null);
        Element anaDesc = doc.getElementsByClass("anaDesc").get(0);
        return anaDesc.select(">span").get(0).text();
    }

    @Deprecated
    private static String getSqlVal(String key) throws Exception {
        JSONObject argModel = JsonUtils.getJsonObjectFromFile("argModel.json");
        // 改变 QueryJson->Items->Value
        JSONObject queryJson = JSONObject.parseObject(argModel.getString("QueryJson"));
        queryJson.getJSONObject("QNode").getJSONArray("QGroup").getJSONObject(0).getJSONArray("Items").getJSONObject(0).fluentPut("Value", key);
        argModel.fluentPut("queryJson", queryJson.toJSONString());
//        Connection connection = CONNECTION_FACTORY.getCnkiConnection(Const.SQL_VAL_URL);
//        // 下面不添加不能返回数据
//        connection.header("referer", "https://kns.cnki.net/kns8/defaultresult/index");
//        CONNECTION_FACTORY.insertPostData(argModel, connection);
//        Document document = connection.post();
        Document document = HTTP_CONNECTION_POOL.post(Const.SQL_VAL_URL, argModel, EXCESS_HEADERS);
        Element sqlValInput = document.getElementById("sqlVal");
        // 断言不为 null
        assert sqlValInput != null;
        String sqlVal = sqlValInput.val();
        if (StringUtils.isEmpty(sqlVal)) {
            // 如果sqlVal为空，则抛出异常
            throw new Exception("sqlVal为空");
        }
        return sqlValInput.val();
    }

    @Deprecated
    private static Document search(String key) throws Exception {
        // Connection connection = CONNECTION_FACTORY.getCnkiConnection(Const.SEARCH_URL);
        HashMap<String, Object> data = new HashMap<>(32);
        // 数据初始化
        data.put("txt_1_sel", "SU$%=|");
        data.put("kw", URLEncoder.encode(key, "utf-8"));
        data.put("txt_1_value1", key);
        data.put("txt_1_special1", "%");
        data.put("txt_extension", "");
        data.put("currentid", "txt_1_value1");
        data.put("dbJson", "coreJson");
        // var o = "SCDB";
        data.put("dbPrefix", "SCDB");
        // f()
        // CJFQ,CDMD,CIPD,CCND,CISD,SNAD,BDZK,CCJD,CCVD,CJFN
        data.put("db_opt", "CJFQ,CDMD,CIPD,CCND,CISD,SNAD,BDZK,CCJD,CCVD,CJFN");
        data.put("singleDB", "SCDB");
        data.put("db_codes", "CJFQ,CDMD,CIPD,CCND,CISD,SNAD,BDZK,CCJD,CCVD,CJFN");
        data.put("singleDBName", "");
        data.put("againConfigJson", "false");
        data.put("action", "scdbsearch");
        data.put("ua", "1.11");
        data.put("t", System.currentTimeMillis());
        // 放入数据
        // CONNECTION_FACTORY.insertPostData(data, connection);

        return HTTP_CONNECTION_POOL.post(Const.SEARCH_URL, data, EXCESS_HEADERS);
    }
}