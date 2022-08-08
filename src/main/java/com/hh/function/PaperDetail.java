package com.hh.function;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSONObject;
import com.hh.entity.MainSentence;
import com.hh.utils.AssertUtils;
import com.hh.utils.DataBaseUtils;
import com.hh.utils.JsonUtils;
import com.hh.utils.StringUtils;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author 86183
 */
public class PaperDetail {
    private static final ApplicationContext CONTEXT = new ClassPathXmlApplicationContext("applicationContext.xml");
    private static final DataBaseUtils DATA_BASE_UTILS = CONTEXT.getBean("dataBaseUtils", DataBaseUtils.class);

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
                        String url = (String) paperDetail.get("url");
                        String source = Const.SOURCE_ZHIWANG;
                        // 根据 url 以及 source 生成 唯一值
                        String uniqueKey = DigestUtil.md5Hex(metabolite + disease + url + source);
                        DATA_BASE_UTILS.insertPaperInfo(metabolite, disease, paperDetail, source, uniqueKey);
                    }
                    // 访问间隔，防止访问过快
                    Thread.sleep(Const.BASE_INTERVAL_TIME + random.nextInt(100));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("error: " + ++error);
                    try {
                        // 如果出错 休息
                        Thread.sleep(Const.BASE_EXCEPTION_TIME + random.nextInt(500));
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
        // 注意字符串的标准化，免得出现nlp上的困难；
        map.put("abstractText", StringUtils.formatComma(abstractText.text()));
        map.put("title", title.text());
        map.put("url", url);
        // 返回 摘要、标题，url的信息
        return map;
    }

    private static List<MainSentence> getMainSentence(String text, String metabolite, String disease) throws Exception {
        String[] sentences = text.split("。");
        // 获得代谢物的 alias （暂时没做）
        // 获得疾病的 alias，并寻找可能的别名
        List<String> diseaseAliases = DATA_BASE_UTILS.getDiseaseAlias(disease);
        diseaseAliases.addAll(getAliasFromPaperAbstract(sentences, disease));
        // 获得饮食的 alias，并寻找可能的别名
        List<String> dietAliases = DATA_BASE_UTILS.getDietAlias(metabolite);
        dietAliases.addAll(getAliasFromPaperAbstract(sentences, metabolite));
        // 返回的数组
        ArrayList<MainSentence> mainSentences = new ArrayList<>(sentences.length);
        boolean flag = true;
        // 遍历
        for (String sentence : sentences) {
            for (String dietA : dietAliases) {
                if (!flag) {
                    flag = true;
                    break;
                }
                for (String diseaseA : diseaseAliases) {
                    int headOffset = sentence.indexOf(dietA);
                    int tailOffset = sentence.indexOf(diseaseA);

                    if (headOffset > -1 && tailOffset > -1 && headOffset != tailOffset) {
                        // 查找代谢物、疾病（别称）都在的句子
                        mainSentences.add(new MainSentence(sentence, null, dietA, headOffset, diseaseA, tailOffset));
                        // 发现匹配后，低优先级的关键词就不需要添加了
                        // 但是对于 癌、肿瘤、细胞这种词汇，若已经到使用这些词的地步时，还是要三句都选上的；
                        if (!diseaseA.equals(Const.CANCER)
                                && !diseaseA.equals(Const.TUMOR)
                                && !diseaseA.equals(Const.CEIL)) {
                            flag = false;
                            break;
                        }
                        // 由于 disease_alias 表，被认为是包含一些 词根（例如：结肠癌的 癌，肿瘤，细胞，肠癌），
                        // 所以不需要使用 若包含“结果”等词，来作为判别 mainSentence 的依据
                    }
                }
            }
        }
        // 结果可能有多个句子
        return mainSentences;
    }

    /**
     * 根据 target 和 括号 找到可能的别名
     *
     * @param sentences 句子
     * @param target    目标
     * @return 别名List
     */
    private static List<String> getAliasFromPaperAbstract(String[] sentences, String target) {
        ArrayList<String> aliases = new ArrayList<>();
        for (String sentence : sentences) {
            int targetIndex = sentence.indexOf(target);

            if (targetIndex > -1) {
                // 统计后发现：使用中文括号作为别名的句子占绝大多数
                List<Integer> lBracketIndexes = getAllIndex(sentence, "（");
                List<Integer> rBracketIndexes = getAllIndex(sentence, "）");
                int I = -1;
                int min = Integer.MAX_VALUE;
                for (int i = 0; i < lBracketIndexes.size(); i++) {
                    // 论文中极少出现括号不匹配，括号中又有括号的情况，就不需要使用复杂的括号匹配算法；
                    // 所以可以简单的认为：只要下标相同就是可匹配的括号；
                    // 那么，在匹配的时候，可以通过左括号下标与目标字符串下标的差值最小值，来识别括号中的字符是不是目标字符串的别名
                    int tmp = Math.abs(lBracketIndexes.get(i) - targetIndex);
                    if (tmp < min) {
                        I = i;
                        min = tmp;
                    }
                }
                if (I > -1) {
                    String substring = sentence.substring(lBracketIndexes.get(I) + 1, rBracketIndexes.get(I));
                    int comma1Index = substring.indexOf(",");
                    int comma2Index = substring.indexOf("，");
                    // 可能出现逗号分割别名的情况，这种情况都得添加到里面；
                    if (comma1Index > -1) {
                        String[] split = substring.split(",");
                        for (String s : split) {
                            // 记住去掉前后 空格
                            aliases.add(s.trim());
                        }
                    } else if (comma2Index > -1) {
                        String[] split = substring.split("，");
                        for (String s : split) {
                            // 记住去掉前后 空格
                            aliases.add(s.trim());
                        }
                    } else {
                        aliases.add(substring);
                    }
                }
            }
        }
        return aliases;
    }

    /**
     * 获得sentence中所有target的下标
     *
     * @param sentence 句子
     * @param target   目标字符串
     * @return 下标list
     */
    private static List<Integer> getAllIndex(String sentence, String target) {
        int index = sentence.indexOf(target);
        ArrayList<Integer> ans = new ArrayList<>();
        while (index > -1) {
            ans.add(index);
            index = sentence.indexOf(target, index + 1);
        }
        return ans;
    }


    public static void main(String[] args) {
        String[] split = "目的:探讨清燥救肺汤对荷CT26小鼠结肠癌增殖及侵袭转移相关蛋白核转录因子-κB（nuclear transcription factor kappa B，NF-κB），血管内皮生长因子（vascular endothelial growth factor，VEGF），血管内皮细胞生长因子受体-1（vascular endothelial growth factor receptor-1，VEGFR-1），基质金属蛋白酶-9（matrix metalloprotein-9，MMP-9）表达的影响。方法:将50只雄性BALB/c小鼠，随机分为模型组，化疗[50 mg·kg-1·（2 d）-1]组，清燥救肺汤高、中、低剂量(15.2，7.6，3.8 g·kg-1·d-1)组，每组10只。小鼠右腋下注射CT26细胞建立结肠癌小鼠模型，清燥救肺汤组以相应剂量造模前2周开始灌胃给药，造模后化疗组以5-氟尿嘧啶[5-FU，50 mg·kg-1·（2 d）-1]腹腔注射给药，模型组以等体积生理盐水灌胃给药，造模后2周后处死各组小鼠并取瘤，称重计算抑瘤率，蛋白免疫印迹法（Western blot）检测NF-κB，VEGF，VEGFR-1及MMP-9蛋白表达。结果:与模型组比较，清燥救肺汤高、中剂量组瘤重显著减小（P<0.01）。化疗组及清燥救肺汤高、中、低剂量组抑瘤率分别为83.90%，60.98%，44.39%，21.46%。与模型组比较，清燥救肺汤高、中剂量组NF-κB及VEGF蛋白表达明显降低（P<0.05，P<0.01）。与模型组比较，清燥救肺汤高、中、低剂量组VEGFR-1及MMP-9蛋白表达明显降低（P<0.05，P<0.01）。结论:清燥救肺汤可能通过降低NF-κB，VEGF，VEGFR-1，MMP-9蛋白表达，发挥抑制荷CT26小鼠结肠癌细胞增殖及侵袭转移的功效。".split("。");
        System.out.println(getAliasFromPaperAbstract(split, "受体"));
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
        connection.header("referer", "https://kns.cnki.net/kns8/defaultresult/index");
        // 插入post数据
        Base.insertPostData(jsonObject, connection);

        return connection;
    }

}
