package test;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.hh.Main;
import com.hh.function.application.CnkiDatabaseService;
import com.hh.function.base.ContextSingletonFactory;
import com.hh.function.base.DataSource;
import org.springframework.context.ApplicationContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ab875
 */
public class Application {
    public static ApplicationContext context = ContextSingletonFactory.getInstance();
    public static CnkiDatabaseService dataBaseUtils = context.getBean("dataBaseUtils", CnkiDatabaseService.class);

    public static DataSource dataSource = context.getBean("dataSource", DataSource.class);
    ;

    public static void main(String[] args1) throws SQLException {
        Main.searchAndInsert("鼻窦炎", false, true);



    }

    public static void generateMatrix() throws SQLException {
        // 获取所有疾病名
        ArrayList<String> allDisease = new ArrayList<>();
        allDisease.add("");
        allDisease.addAll(dataBaseUtils.getAllDisease());
        int size = allDisease.size();
        // 使用 Map 存储 下标
        HashMap<String, Integer> diseaseIndex = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            diseaseIndex.put(allDisease.get(i), i);
        }

        // 获取所有饮食名
        List<String> allMetabolite = dataBaseUtils.getAllMetabolite();
        // 使用 Map 存储 下标
        HashMap<String, Integer> metaboliteIndex = new HashMap<>(allMetabolite.size());
        for (int i = 0; i < allMetabolite.size(); i++) {
            metaboliteIndex.put(allMetabolite.get(i), i);
        }


        // 获取相应的数据
        Connection connection = dataSource.getConnection();


        ExcelWriter writer = ExcelUtil.getWriter("C:/Users/ab875/Desktop/分析结果.xlsx", "表1");

        PreparedStatement ps1 = connection.prepareStatement("SELECT metabolite, disease, COUNT(1) FROM paper_info where relation != 0 GROUP BY metabolite,disease;");
        generate(writer, allDisease, allMetabolite, diseaseIndex, metaboliteIndex, ps1);


        for (int i = 1; i < 4; i++) {
            writer.setSheet("表" + (i + 1));
            PreparedStatement ps = connection.prepareStatement("SELECT metabolite, disease, COUNT(1) FROM paper_info WHERE relation = ? GROUP BY metabolite,disease;");
            ps.setInt(1, i);
            generate(writer, allDisease, allMetabolite, diseaseIndex, metaboliteIndex, ps);
        }

        writer.close();
    }

    public static void generate(ExcelWriter writer, List<String> allDisease,
                                List<String> allMetabolite,
                                Map<String, Integer> diseaseIndex,
                                Map<String, Integer> metaboliteIndex,
                                PreparedStatement ps) throws SQLException {

        // 建立：以疾病名为列、饮食名为行的 Excel表

        List<List<String>> res = new ArrayList<>(allMetabolite.size() + 1);
        for (String s : allMetabolite) {
            ArrayList<String> tmp = new ArrayList<>(allDisease.size());
            // 初始化行
            tmp.add(s);
            for (int j = 1; j < allDisease.size(); j++) {
                tmp.add("0");
            }
            res.add(tmp);
        }

        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            while (rs.next()) {
                // 获得坐标
                Integer row = metaboliteIndex.get(rs.getString(1));
                Integer col = diseaseIndex.get(rs.getString(2));
                // 文章数量
                int num = rs.getInt(3);

                // 填入数量
                if (row != null && col != null) {
                    List<String> list = res.get(row);
                    list.set(col, String.valueOf(num));
                }
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }

        // Excel 操作
        writer.writeHeadRow(allDisease);
        writer.write(res, true);


    }
}
