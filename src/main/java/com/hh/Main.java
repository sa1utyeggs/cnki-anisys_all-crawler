package com.hh;

import com.alibaba.fastjson.JSONObject;
import com.hh.function.Base;
import com.hh.function.Const;
import com.hh.function.PaperDetail;
import com.hh.function.PaperNum;
import com.hh.utils.DataBaseUtils;
import com.hh.utils.JsonUtils;
import com.hh.utils.StringUtils;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author ab875
 */
public class Main {

    public static void main(String[] args) {
        // 参数：代谢物，疾病
        // type：SU按照主题搜索，KY安装关键词搜索
        // test：true不记录到数据库并输出detail信息，false记录到数据库不输出detail信息
        // PaperDetail.insertPaperInfo("皂苷", "结肠癌", Const.SEARCH_KY, true);
        PaperNum.getMetabolitesDiseasePaperNum("结肠癌", Const.SEARCH_KY, true);
    }


}
