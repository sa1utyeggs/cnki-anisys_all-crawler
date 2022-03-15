package com.hh;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ab875
 */
public class Main {
    /**
     * URL
     */
    public static final String SEARCH_URL = "https://kns.cnki.net/kns8/defaultresult/index";
    public static final String VISUAL_URL = "https://kns.cnki.net/kns8/Visual/Center";
    public static final String SQL_VAL_URL = "https://kns.cnki.net/kns8/Brief/GetGridTableHtml";
    public static final String BASE_URL = "https://kns.cnki.net";

    public static final long EXCEPTION_TIME = 10000;
    public static final long INTERVAL_BASE_TIME = 10000;

    public static void main(String[] args) {
        insertPaperInfo("磷酸", "结肠癌");
    }

    public static void insertPaperInfo(String metabolite, String disease) {
        int error = 0;
        try {
            List<String> keys = getPaperDetailKey(disease + " " + metabolite);
            List<String> distinctKeys = keys.stream().distinct().collect(Collectors.toList());
            Random random = new Random();
            for (String key : distinctKeys) {
                try {
                    // 打印进度、url
                    System.out.println(distinctKeys.indexOf(key) + "/" + distinctKeys.size());
                    System.out.println(BASE_URL + "/kcms/detail/detail.aspx?" + key);
                    // 获得论文信息
                    Map<String, Object> paperDetail = getPaperDetail(key);
                    // 插入数据库
                    DataBaseUtils.insertPaperInfo(metabolite, disease, paperDetail);
                    Thread.sleep(100 + random.nextInt(100));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("error: " + ++error);
                    // 如果出错 休息 10~15s
                    try {
                        Thread.sleep(10000 + random.nextInt(5000));
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
            System.out.println("本次错误数：" + error +"\n" + "共：" + distinctKeys.size());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("获得url-key失败");
        }
    }

    /**
     * 获取文章的具体信息
     *
     * @param key 关键词
     * @return 信息map
     * @throws IOException
     */
    public static Map<String, Object> getPaperDetail(String key) throws Exception {
        // 返回值
        HashMap<String, Object> map = new HashMap<>();
        String url = BASE_URL + "/kcms/detail/detail.aspx?" + key;
        Connection connection = getConnection(url);
        // 下面不添加不能返回数据
        connection.header("referer", "https://kns.cnki.net/kns8/defaultresult/index");
        Document document = connection.get();
        // 获取文章的头部信息（包括文章名，作者，分类号，摘要）
        Element docTop = document.getElementsByClass("doc-top").get(0);
        Element title;
        Element abstractText;
        if (docTop != null) {
            abstractText = docTop.getElementsByClass("abstract-text").get(0);
            title = docTop.getElementsByClass("wx-tit").get(0).getElementsByTag("h1").get(0);
            AssertUtils.sysIsError(abstractText == null, "此文章无摘要");
            AssertUtils.sysIsError(title == null, "此文章无标题");
        } else {
            throw new Exception("无法获取文章头部信息");
        }
        assert abstractText != null;
        assert title != null;
        map.put("abstractText", abstractText.text());
        map.put("title", title.text());
        map.put("url", url);
        return map;
    }

    /**
     * 获得代谢物-疾病的论文数量，并插入数据库
     *
     * @param disease 疾病名字
     * @throws IOException 异常
     */
    public static void getMetabolitesDiseasePaperNum(String disease) throws IOException {
        // 获得所有的代谢物
        List<String> metabolites = com.hh.FileUtils.readCsvColumn("C:\\Users\\ab875\\Desktop\\FoodDiet\\metabolites_in_human.csv", 6, ";");
        String key = null;
        int sum = 0;
        int error = 0;
        ArrayList<String> errorName = new ArrayList<>(10);
        int number = 0;
        int flag = 0;
        for (String metabolite :
                metabolites) {
            try {
                key = metabolite + " " + disease;
                // 将读取的字符串经过处理，转为数字（相关文献数量）
                number = Integer.parseInt(getVisualData(key).substring(5).split(" ")[0]);

                // 插入操作
                flag = DataBaseUtils.insertMetaboliteDiseaseNumber(metabolite, disease, number);
                if (flag == 1) {
                    sum++;
                } else {
                    throw new Exception("数据库插入错误");
                }
            } catch (Exception throwable) {
                throwable.printStackTrace();
                error++;
                errorName.add(metabolite + " || " + disease);
            }
            // 实时输出结果
            System.out.println("查找：" + key + " || 查找结果：" + number + " || 插入结果： " + flag + " || 目前插入数据数：" + sum + " || 错误数：" + error);
        }
        // 显示失败的插入
        System.out.println("本次查询失败的有：");
        errorName.forEach(System.out::println);
    }

    /**
     * 获得部分的知网统计数据
     *
     * @param key 搜索词
     * @return 返回html
     * @throws Exception 异常
     */
    public static String getVisualData(String key) throws Exception {
        // 获得基础的visual数据，必须要有sqlVal
        String jsonString = JsonUtils.getJsonFromFile("template.json");
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        String sqlVal = getSqlVal(key);
        // 换 value
        JSONObject json = jsonObject.getJSONObject("json");
        json.getJSONObject("QNode")
                .getJSONArray("QGroup")
                .getJSONObject(0)
                .getJSONArray("Items")
                .getJSONObject(0)
                .fluentPut("Value", key);

        // 换 sqlVal
        jsonObject.fluentPut("searchSql", sqlVal);
        jsonObject.fluentPut("json", json.toJSONString());


        Connection connection = getConnection(VISUAL_URL);
        insertPostData(jsonObject, connection);
        Document doc = connection.post();
        Element anaDesc = doc.getElementsByClass("anaDesc").get(0);
        return anaDesc.select(">span").get(0).text();
    }

    public static String getSqlVal(String key) throws Exception {
        JSONObject argModel = JsonUtils.getJsonObjectFromFile("argModel.json");
        // 改变 QueryJson->Items->value
        JSONObject queryJson = JSONObject.parseObject(argModel.getString("QueryJson"));
        queryJson.getJSONObject("QNode")
                .getJSONArray("QGroup")
                .getJSONObject(0)
                .getJSONArray("Items")
                .getJSONObject(0)
                .fluentPut("Value", key);
        argModel.fluentPut("queryJson", queryJson.toJSONString());
        Connection connection = getConnection(SQL_VAL_URL);
        // 下面不添加不能返回数据
        connection.header("referer", "https://kns.cnki.net/kns8/defaultresult/index");
        insertPostData(argModel, connection);
        Document document = connection.post();
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

    public static Document search(String key) throws IOException {
        Connection connection = getConnection(SEARCH_URL);
        HashMap<String, Object> data = new HashMap<>(10);
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
        insertPostData(data, connection);
        return connection.post();

    }

    /**
     * 根据key（搜索词），获得结果文章的关键词（用于获得文章具体信息）
     *
     * @param key 搜索词
     * @return
     * @throws IOException
     */
    public static List<String> getPaperDetailKey(String key) throws IOException {
        int currentPage = 1;
        int pages;
        String sqlVal = "";
        int handlerId = 0;
        ArrayList<String> keys = new ArrayList<>(200);
        do {
            Connection connection = getPaperGridConnection(key, currentPage, sqlVal, handlerId);
            Document document = connection.post();
            // 获得总页数
            String countPageMark = document.getElementsByClass("countPageMark").get(0).text();
            pages = Integer.parseInt(countPageMark.substring(countPageMark.indexOf("/") + 1));
            // 获得 sqlVal 和 handlerId
            Element sqlValInput = document.getElementById("sqlVal");
            if (sqlValInput != null) {
                sqlVal = sqlValInput.text();
            }
            Element handlerIdHid = document.getElementById("HandlerIdHid");
            if (handlerIdHid != null && !handlerIdHid.text().isEmpty()) {
                handlerId = Integer.parseInt(handlerIdHid.text());
            }
            // 提取 FileName、DbName、DbCode
            Elements trs = document.getElementsByTag("tbody").select("tr");
            for (Element tr : trs) {
                Element a = tr.getElementsByClass("name").get(0).getElementsByTag("a").get(0);
                String href = a.attr("href");
                // 添加到 keys中
                keys.add(href.substring(href.indexOf("FileName")));
            }
            // 将 currentPage ++
            System.out.println("当前完成页数：" + currentPage++ + "/" + pages);
        } while (currentPage <= pages);
        return keys;
    }

    public static Connection getPaperGridConnection(String key, Integer curPage, String searchSql, Integer handlerId) {
        // 读出 json 字符串
        String jsonString = null;
        try {
            jsonString = JsonUtils.getJsonFromFile("groupSearchPayload.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 准换为 jsonObject
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        // 修改 Value 为 key
        jsonObject.getJSONObject("QueryJson")
                .getJSONObject("QNode")
                .getJSONArray("QGroup")
                .getJSONObject(0)
                .getJSONArray("Items")
                .getJSONObject(0)
                .fluentPut("Value", key);
        // 修改请求的页数
        jsonObject.fluentPut("CurPage", curPage);
        if (curPage > 1) {
            jsonObject.fluentPut("IsSearch", false);
            jsonObject.fluentPut("SearchSql", searchSql);
            jsonObject.fluentPut("HandlerId", handlerId);
        }
        // System.out.println(jsonString);

        Connection connection = getConnection(BASE_URL + "/kns8/Brief/GetGridTableHtml");
        // 一定要有 referer 不然不会返回数据
        connection.header("referer", "https://kns.cnki.net/kns8/DefaultResult/");
        // 插入post数据
        insertPostData(jsonObject, connection);
        return connection;
    }

    public static void insertPostData(Map<String, Object> data, Connection connection) {
        // 循环插入数据
        try {
            for (Map.Entry<String, Object> d :
                    data.entrySet()) {
                // 强制转换为字符串
                connection.data(d.getKey(), String.valueOf(d.getValue()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection(String url) {
        Connection con = Jsoup.connect(url);
        con.header("Accept", "text/html, */*; q=0.01");
        con.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36");
        con.header("Host", "kns.cnki.net");
        con.header("Origin", "https://kns.cnki.net");
        String baseURL = JsonUtils.class.getResource("/").getPath();
        File cookieFile = new File(baseURL + "cookie.txt");
        String cookie = "";
        try {
            // 从文件里读 cookie
            cookie = FileUtils.readFileToString(cookieFile, "utf-8");
            con.header("cookie", cookie);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return con;
    }

}
