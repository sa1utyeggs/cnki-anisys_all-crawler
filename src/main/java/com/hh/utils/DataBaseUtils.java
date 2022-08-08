package com.hh.utils;

import com.hh.entity.MainSentence;
import com.hh.utils.AssertUtils;
import com.mysql.cj.MysqlType;

import javax.sql.rowset.serial.SerialArray;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 86183
 */
public class DataBaseUtils {
    public static final String DATABASE_URL = "jdbc:mysql://******/diet_disease?useSSL=true&&characterEncoding=UTF-8&&allowMultiQueries=true&&serverTimezone=UTC";
    public static final String DATABASE_USERNAME = "******";
    public static final String DATABASE_PASSWORD = "******";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static DataBaseUtils newInstance() {
        return new DataBaseUtils();
    }

    public static void main(String[] args) throws Exception {
        test();
    }

    public static void test() throws Exception {
        Connection connection = getConnection();
        List<String> metabolites = newInstance().getMetabolites(100000);
        // ps：获得代谢物的名称
        PreparedStatement ps = connection.prepareStatement("insert into diet_metabolite_alias(`name`,alias,priority) values (?,?,?)");
        try {
            for (String metabolite : metabolites) {
                ps.setString(1, metabolite);
                ps.setString(2, metabolite);
                ps.setInt(3, 1);
                ps.executeUpdate();
            }
        } finally {
            ps.close();
            connection.close();
        }
    }

    /**
     * 获得连接
     *
     * @return 连接
     * @throws SQLException sql
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
    }

    /**
     * 插入代谢物-疾病相关文献的数量
     *
     * @param metabolite 代谢物
     * @param disease    疾病
     * @param number     文献数量
     * @return 是否插入成功
     * @throws SQLException sql
     */
    public int insertMetaboliteDiseaseNumber(String metabolite, String disease, int number) throws Exception {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("insert into metabolite_disease_number(metabolite, disease, number) values (?,?,?)");
        int flag = 0;
        try {
            preparedStatement.setString(1, metabolite);
            preparedStatement.setString(2, disease);
            preparedStatement.setInt(3, number);
            flag = preparedStatement.executeUpdate();
            if (flag == 0) {
                System.out.println("插入错误：" + metabolite + " | " + disease + " | " + number);
            }
            return flag;
        } finally {
            preparedStatement.close();
            connection.close();
            endBanner(1 - flag, 1);
        }
    }

    /**
     * 插入文献信息
     *
     * @param metabolite   代谢物
     * @param disease      疾病
     * @param title        文章标题
     * @param url          文章url
     * @param abstractText 文章摘要
     * @param mainSentence 主要句子
     * @throws Exception ex
     */
    public void insertPaperInfo(String metabolite, String disease, String title, String url, String source, String key, String abstractText, List<MainSentence> mainSentence) throws Exception {
        long paperId = -1L;
        int error = 0;
        Connection connection = getConnection();
        // 取消自动提交
        connection.setAutoCommit(false);
        // 先插入 paper_info，并返回主键
        PreparedStatement psPaper = connection.prepareStatement("insert into paper_info(metabolite, disease, title, url,relation,confidence,source,unique_key) values (?,?,?,?,0,0,?,?);", Statement.RETURN_GENERATED_KEYS);
        // 再插入 paper_abstract ，填入 paper_id
        PreparedStatement psAb = connection.prepareStatement("insert into paper_abstract(paper_id, `text`) values(?,?);");
        // 最后插入 paper_main_sentence 表，填入 paper_id
        PreparedStatement psSen = connection.prepareStatement("insert into paper_main_sentence(paper_id, `text`, head, tail,head_offset,tail_offset,relation,confidence) values(?,?,?,?,?,?,0,0);");
        ResultSet rs = null;
        try {
            // 插入paper_info 表
            psPaper.setString(1, metabolite);
            psPaper.setString(2, disease);
            psPaper.setString(3, title);
            psPaper.setString(4, url);
            psPaper.setString(5, source);
            psPaper.setString(6, key);
            int i2 = psPaper.executeUpdate();
            rs = psPaper.getGeneratedKeys();
            if (rs.next()) {
                paperId = rs.getLong(1);
            }
            AssertUtils.sysIsError(i2 == 0, "插入paper_info表失败");

            // 插入 paper_abstract 表
            psAb.setLong(1, paperId);
            psAb.setString(2, abstractText);
            int i1 = psAb.executeUpdate();
            AssertUtils.sysIsError(i1 == 0, "插入paper_abstract表失败");

            // 插入 paper_main_sentence 表
            psSen.setLong(1, paperId);
            for (MainSentence sentence : mainSentence) {
                try {
                    psSen.setString(2, sentence.getText());
                    psSen.setString(3, sentence.getHead());
                    psSen.setString(4, sentence.getTail());
                    psSen.setInt(5, sentence.getHeadOffset());
                    psSen.setInt(6, sentence.getTailOffset());
                    int i3 = psSen.executeUpdate();
                    if (i3 == 0) {
                        throw new Exception("插入失败：" + sentence);
                    }
                    // 由于这个表 只与 nlp 强关联，所以不适合在此做 异常抛出：AssertUtils.sysIsError(i3 == 0, "插入paper_main_sentence表失败");
                } catch (Exception e) {
                    error++;
                    System.out.println("    " + e.getMessage());
                }
            }

            // 提交事务
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("插入数据库失败");
        } finally {
            if (rs != null) {
                rs.close();
            }
            psAb.close();
            psPaper.close();
            connection.close();
            endBanner(error, mainSentence.size());
        }
    }

    public void insertPaperInfo(String metabolite, String disease, Map<String, Object> map, String source, String key) throws Exception {
        String title = (String) map.get("title");
        String url = (String) map.get("url");
        String abstractText = (String) map.get("abstractText");
        List<MainSentence> mainSentences = (List<MainSentence>) map.get("mainSentence");
        this.insertPaperInfo(metabolite, disease, title, url, source, key, abstractText, mainSentences);
    }

    /**
     * 获得代谢物列表
     *
     * @param limit 限制数量
     * @return list
     * @throws Exception ex
     */
    public List<String> getMetabolites(int limit) throws Exception {
        Connection connection = getConnection();

        ArrayList<String> metaboliteNames = new ArrayList<>();
        // ps：获得代谢物的名称
        PreparedStatement ps = connection.prepareStatement("select name from metabolite limit ?");
        ResultSet rs = null;
        try {
            ps.setInt(1, limit);
            rs = ps.executeQuery();
            while (rs.next()) {
                metaboliteNames.add(rs.getString(1));
            }
            return metaboliteNames;
        } finally {
            if (rs != null) {
                rs.close();
            }
            ps.close();
            connection.close();
        }
    }

    /**
     * 按照优先级从高到低排序，包括 disease 本身
     *
     * @param disease 疾病
     * @return List
     * @throws SQLException sql
     */
    public List<String> getDiseaseAlias(String disease) throws SQLException {
        Connection connection = getConnection();

        ArrayList<String> alias = new ArrayList<>();
        // ps：获得代谢物的名称
        PreparedStatement ps = connection.prepareStatement("select alias from disease_alias where name = ? order by priority;");
        ResultSet rs = null;
        try {
            ps.setString(1, disease);
            rs = ps.executeQuery();
            while (rs.next()) {
                alias.add(rs.getString(1));
            }
            return alias;
        } finally {
            if (rs != null) {
                rs.close();
            }
            ps.close();
            connection.close();
        }
    }

    /**
     * 按照优先级从高到低排序，包括 diet 本身
     *
     * @param diet 饮食
     * @return List
     * @throws SQLException sql
     */
    public List<String> getDietAlias(String diet) throws Exception {
        Connection connection = getConnection();
        ArrayList<String> alias = new ArrayList<>();
        // ps：获得代谢物的名称
        PreparedStatement ps = connection.prepareStatement("select alias from diet_metabolite_alias where name = ? order by priority;");
        ResultSet rs = null;
        try {
            ps.setString(1, diet);
            rs = ps.executeQuery();
            while (rs.next()) {
                alias.add(rs.getString(1));
            }
            return alias;
        } finally {
            if (rs != null) {
                rs.close();
            }
            ps.close();
            connection.close();
        }
    }

    public List<String> getMetaboliteByMaxPaperNumber(String disease, int limit) throws SQLException {
        Connection connection = getConnection();
        ArrayList<String> metaboliteNames = new ArrayList<>();
        // ps：获得代谢物的名称
        PreparedStatement ps = connection.prepareStatement("select metabolite from metabolite_disease_number where disease = ? order by number Desc limit ? ");
        ResultSet rs = null;
        try {
            ps.setString(1, disease);
            ps.setInt(2, limit);
            rs = ps.executeQuery();
            while (rs.next()) {
                metaboliteNames.add(rs.getString(1));
            }
            return metaboliteNames;
        } finally {
            if (rs != null) {
                rs.close();
            }
            ps.close();
            connection.close();
        }
    }


    /**
     * 插入 metabolite 表 和 diet_metabolite_alias 表
     *
     * @param metabolites 代谢物
     * @throws Exception ex
     */
    public void insertMetabolite(List<String> metabolites) throws Exception {
        Connection connection = getConnection();
        // ps：获得代谢物的名称
        PreparedStatement ps1 = connection.prepareStatement("insert into metabolite(`name`) values (?)");
        PreparedStatement ps2 = connection.prepareStatement("insert into diet_metabolite_alias(`name`, alias, priority) values (?,?,?)");
        int size = metabolites.size();
        int i = 0;
        int error = 0;
        try {
            for (String s : metabolites) {
                try {
                    i++;
                    System.out.println("插入：" + s + " " + i + " / " + size);
                    connection.setAutoCommit(false);
                    ps1.setString(1, s);
                    int update1 = ps1.executeUpdate();
                    if (update1 == 0) {
                        throw new Exception("插入 metabolite 失败：" + s);
                    }
                    ps2.setString(1, s);
                    ps2.setString(2, s);
                    ps2.setInt(3, 1);
                    int update2 = ps2.executeUpdate();
                    if (update2 == 0) {
                        throw new Exception("插入 diet_metabolite_alias 失败：" + s);
                    }
                    connection.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    error++;
                    connection.rollback();
                }
            }

        } finally {
            ps1.close();
            connection.close();
            endBanner(error, size);
        }

    }

    /**
     * 插入 metabolite 表 和 diet_metabolite_alias 表
     *
     * @param csvPath csv文件 的路径
     * @param column  第几列为
     * @throws Exception ex
     */
    public void insertMetabolite(String csvPath, Integer column) throws Exception {
        List<String> metabolite = FileUtils.readCsvColumn(csvPath, column, null);
        insertMetabolite(metabolite);
    }

    /**
     * 作为 insertMetabolite方法的补充，添加额外的别名（diet_metabolite_alias）
     *
     * @param metabolites 代谢物
     * @param aliases     别名
     */
    public void insertMetaboliteAlias(List<String> metabolites, List<List<String>> aliases) throws Exception {
        int size = metabolites.size();
        int error = 0;
        if (size != aliases.size()) {
            throw new Exception("metabolites 与 alias 的 size 不匹配");
        }
        Connection connection = getConnection();
        // ps1：获得某个代谢物别名的最大 优先级
        PreparedStatement ps1 = connection.prepareStatement("select MAX(p) from (select priority as p from diet_metabolite_alias dma where name = ?) as maxP");
        // ps2：插入表 diet_metabolite_alias
        PreparedStatement ps2 = connection.prepareStatement("insert into diet_metabolite_alias(`name`, alias, priority) values (?,?,?)");
        ResultSet maxPriority = null;
        try {
            for (int i = 0; i < size; i++) {
                String metabolite = metabolites.get(i);
                int tmp = 1;
                System.out.println(metabolite + "：" + i + " / " + size);
                // ps1
                ps1.setString(1, metabolite);
                maxPriority = ps1.executeQuery();
                // 若该代谢物还没有优先级，就用默认值填入： 1
                maxPriority.next();
                if (maxPriority.getInt(1) != 0) {
                    tmp = maxPriority.getInt(1);
                }


                for (String s : aliases.get(i)) {
                    try {
                        if (!StringUtils.isEmpty(s)) {
                            // ps2
                            ps2.setString(1, metabolite);
                            ps2.setString(2, s);
                            ps2.setInt(3, ++tmp);
                            int update = ps2.executeUpdate();
                            if (update == 0) {
                                tmp--;
                                throw new Exception("插入错误:  " + s + " " + tmp);
                            }
                        }
                        System.out.println(tmp);
                    } catch (Exception e) {
                        System.out.println("    " + e.getMessage());
                        error++;
                    }
                }

            }
        } finally {
            if (maxPriority != null) {
                maxPriority.close();
            }
            ps2.close();
            ps1.close();
            connection.close();
            endBanner(error, size);
        }
    }

    public MainSentence getMainSentence(Long id) throws SQLException {

        Connection connection = getConnection();
        // ps1：获得某个代谢物别名的最大 优先级
        PreparedStatement ps = connection.prepareStatement("select `text`,head,tail,head_offset,tail_offset,relation from paper_main_sentence where id = ?");

        ResultSet rs = null;
        MainSentence mainSentence = null;
        try {
            ps.setLong(1, id);
            rs = ps.executeQuery();
            rs.next();
            mainSentence = new MainSentence(rs.getString(1), MainSentence.RELATION_EXPLAIN.get(rs.getInt(6)), rs.getString(2), rs.getInt(4), rs.getString(3), rs.getInt(5));
        } finally {
            if (rs != null) {
                rs.close();
            }
            ps.close();
            connection.close();

        }
        return mainSentence;
    }

    public static String preparePlaceHolders(int size) {
        return String.join(",", Collections.nCopies(size, "?"));

    }


    public List<MainSentence> getMainSentences(List<Long> ids) throws SQLException {
        Connection connection = getConnection();
        int size = ids.size();
        ArrayList<MainSentence> mainSentences = new ArrayList<>(size);
        // ps1：获得某个代谢物别名的最大 优先级
        String sql = String.format("select `text`,head,tail,head_offset,tail_offset,relation from paper_main_sentence where id in (%s)", preparePlaceHolders(ids.size()));
        PreparedStatement ps = connection.prepareStatement(sql);
        for (int i = 0; i < size; i++) {
            ps.setLong(i + 1, ids.get(i));
        }

        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            while (rs.next()) {
                mainSentences.add(new MainSentence(StringUtils.formatComma(rs.getString(1)), MainSentence.RELATION_EXPLAIN.get(rs.getInt(6)), rs.getString(2), rs.getInt(4), rs.getString(3), rs.getInt(5)));
            }

        } finally {
            if (rs != null) {
                rs.close();
            }
            ps.close();
            connection.close();

        }
        return mainSentences;
    }

    /**
     * 结束输出
     *
     * @param errorNum 错误数
     * @param sum      总数
     */
    public static void endBanner(Integer errorNum, Integer sum) {
        System.out.println("错误数： " + errorNum + " / " + sum);
    }


    public static boolean isPaperExists(String uniqueKey) throws SQLException {
        Connection connection = getConnection();
        // ps：获得代谢物的名称
        PreparedStatement ps1 = connection.prepareStatement("select id from paper_info where  unique_key = ?");
        ResultSet resultSet = null;
        try {
            ps1.setString(1, uniqueKey);
            resultSet = ps1.executeQuery();
            resultSet.next();
            if (resultSet.getLong(1) > 0) {
                return true;
            }
        } finally {
            if (resultSet != null){
                resultSet.close();
            }
            ps1.close();
            connection.close();
        }
        return false;
    }

}
