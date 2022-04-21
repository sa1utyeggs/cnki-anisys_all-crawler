package com.hh.function;

import com.alibaba.fastjson.JSONObject;
import com.hh.utils.AssertUtils;
import com.hh.utils.DataBaseUtils;
import com.hh.utils.JsonUtils;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author 86183
 */
public class PaperDetail {

    /**
     * 根据 两个参数 来查询论文数据，并插入到数据库中
     * 考虑增加对别名的支持（包括饮食和疾病）
     *
     * @param metabolite 代谢物
     * @param disease    疾病
     */
    public static void insertPaperInfo(String metabolite, String disease, String type, boolean test) {
        int error = 0;
        try {
            // 根据查找词，获得文章的key（key是组成文章url的参数）
            List<String> keys = getPaperDetailKey(metabolite, disease, type);
            List<String> distinctKeys = keys.stream().distinct().collect(Collectors.toList());
            Random random = new Random();
            for (String key : distinctKeys) {
                try {
                    // 打印进度、url
                    System.out.println(distinctKeys.indexOf(key) + "/" + distinctKeys.size());
                    System.out.println(Const.BASE_URL + "/kcms/detail/detail.aspx?" + key);
                    // 获得论文信息
                    Map<String, Object> paperDetail = getPaperDetail(key);
                    // 生成主要语句
                    paperDetail.put("mainSentence", getMainSentence((String) paperDetail.get("abstractText"), metabolite, disease));
                    if (test) {
                        // 如果作为测试则输出
                        System.out.println(paperDetail);
                    } else {
                        // 插入数据库
                        DataBaseUtils.insertPaperInfo(metabolite, disease, paperDetail);
                    }
                    // 访问间隔，防止访问过快
                    Thread.sleep(Const.INTERVAL_BASE_TIME + random.nextInt(100));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("error: " + ++error);
                    try {
                        // 如果出错 休息 10~15s
                        Thread.sleep(Const.EXCEPTION_TIME + random.nextInt(5000));
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
            System.out.println("本次错误数：" + error + "\n" + "共：" + distinctKeys.size());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获得url-key失败");
        }
    }

    /**
     * 获取文章的具体信息
     *
     * @param key 关键词
     * @return 信息map
     */
    private static Map<String, Object> getPaperDetail(String key) throws Exception {
        // 返回值
        HashMap<String, Object> map = new HashMap<>(100);
        String url = Const.BASE_URL + "/kcms/detail/detail.aspx?" + key;
        Connection connection = Base.getCnkiConnection(url);
        // 下面不添加不能返回数据
        connection.header("referer", "https://kns.cnki.net/kns8/defaultresult/index");
        Document document = connection.get();
        // 获取文章的头部信息（包括文章名，作者，分类号，摘要）
        Element docTop = document.getElementsByClass("doc-top").get(0);
        Element title;
        Element abstractText;

        // 获得文章的摘要和标题
        abstractText = docTop.getElementsByClass("abstract-text").get(0);
        title = docTop.getElementsByClass("wx-tit").get(0).getElementsByTag("h1").get(0);
        AssertUtils.sysIsError(abstractText == null, "此文章无摘要");
        AssertUtils.sysIsError(title == null, "此文章无标题");

        assert abstractText != null;
        assert title != null;
        map.put("abstractText", abstractText.text());
        map.put("title", title.text());
        map.put("url", url);
        // 返回 摘要、标题，url的信息
        return map;
    }

    private static String getMainSentence(String text, String metabolite, String disease) {
        String[] sentences = text.split("。");
        StringBuilder mainSentence = new StringBuilder();
        // 遍历
        for (String sentence : sentences) {
            if (sentence.contains(metabolite) && sentence.contains(disease)) {
                // 查找代谢物、疾病都在的句子
                mainSentence.append(sentence).append("。");
            } else if (sentence.contains("结果") || sentence.contains("结论")) {
                // 或者含有结果，结论的语句
                mainSentence.append(sentence).append("。");
            }
        }
        // 结果可能有多个句子
        return mainSentence.toString();
    }


    /**
     * 根据代谢物与疾病，
     * 条件为：模糊搜索，同义词拓展，中英文拓展
     *
     * @param metabolite 代谢物
     * @param disease    疾病
     * @return 文献的key
     * @throws Exception 异常
     */
    private static List<String> getPaperDetailKey(String metabolite, String disease, String type) throws Exception {
        int currentPage = 1;
        int pages = 1;
        String sqlVal = "";
        int handlerId = 0;
        ArrayList<String> keys = new ArrayList<>(200);
        do {
            // 获得 主页面的 connection
            Connection connection = getPaperGridConnection(metabolite, disease, currentPage, sqlVal, handlerId, type);
            Document document = connection.post();

            // 获得总页数
            // 有可能查不到数据，那么页数就找不到东西
            String countPageMark = "countPageMark";
            if (!document.getElementsByClass(countPageMark).isEmpty()) {
                String countPageText;
                countPageText = document.getElementsByClass("countPageMark").get(0).text();
                pages = Integer.parseInt(countPageText.substring(countPageText.indexOf("/") + 1));
            } else {
                // 有可能中间出了问题，那就继续
                System.out.println("当前页：" + currentPage + "/" + pages + " 处理失败！！！");
                currentPage++;
                continue;
            }
            // 获得 sqlVal 和 handlerId
            Element sqlValInput = document.getElementById("sqlVal");
            if (sqlValInput != null) {
                // 注意是 value属性，不是 text
                sqlVal = sqlValInput.val();
            }
            Element handlerIdHid = document.getElementById("HandlerIdHid");
            if (handlerIdHid != null && !handlerIdHid.val().isEmpty()) {
                handlerId = Integer.parseInt(handlerIdHid.val());
            }
            // 提取 FileName、DbName、DbCode
            Elements trs = document.getElementsByTag("tbody").select("tr");
            for (Element tr : trs) {
                Element a = tr.getElementsByClass("name").get(0).getElementsByTag("a").get(0);
                String href = a.attr("href");
                // 将 key 添加到 keys 中
                keys.add(href.substring(href.indexOf("FileName")));
            }
            // 将 currentPage ++
            System.out.println("当前页：" + currentPage++ + "/" + pages + " 处理成功");
        } while (currentPage <= pages);
        return keys;
    }

    public static Connection getPaperGridConnection(String metabolite, String disease, Integer curPage, String searchSql, Integer handlerId, String type) throws Exception {
        // 读出 json 字符串
        String jsonString;

        // 根据关键字，使用不同的搜索方式
        // SU按照主题搜索
        // KY安装关键词搜索
        switch (type) {
            case "KY":
                jsonString = JsonUtils.getJsonFromFile("groupSearchPayload-KY.json");
                break;
            case "SU":
                jsonString = JsonUtils.getJsonFromFile("groupSearchPayload-SU.json");
                break;
            default:
                throw new Exception("没有这种type的搜索方式");
        }

        // 转换为 jsonObject
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        // 修改 关键词1 为 metabolite
        jsonObject.getJSONObject("QueryJson").getJSONObject("QNode").getJSONArray("QGroup").getJSONObject(0).getJSONArray("ChildItems").getJSONObject(0).getJSONArray("Items").getJSONObject(0).fluentPut("Value", metabolite);
        jsonObject.getJSONObject("QueryJson").getJSONObject("QNode").getJSONArray("QGroup").getJSONObject(0).getJSONArray("ChildItems").getJSONObject(0).getJSONArray("Items").getJSONObject(0).fluentPut("Title", metabolite);
        // 修改关键词2 为 disease
        jsonObject.getJSONObject("QueryJson").getJSONObject("QNode").getJSONArray("QGroup").getJSONObject(0).getJSONArray("ChildItems").getJSONObject(1).getJSONArray("Items").getJSONObject(0).fluentPut("Value", disease);
        jsonObject.getJSONObject("QueryJson").getJSONObject("QNode").getJSONArray("QGroup").getJSONObject(0).getJSONArray("ChildItems").getJSONObject(1).getJSONArray("Items").getJSONObject(0).fluentPut("Title", disease);
        // 修改请求的页数
        jsonObject.fluentPut("CurPage", curPage);
        if (curPage > 1) {
            jsonObject.fluentPut("IsSearch", false);
            jsonObject.fluentPut("SearchSql", searchSql);
            jsonObject.fluentPut("HandlerId", handlerId);
        }

        Connection connection = Base.getCnkiConnection(Const.BASE_URL + "/kns8/Brief/GetGridTableHtml");

        // 一定要有 referer 不然不会返回数据
        connection.header("referer", "https://kns.cnki.net/kns8/DefaultResult/");
        // 插入post数据
        Base.insertPostData(jsonObject, connection);

        return connection;
    }

}
