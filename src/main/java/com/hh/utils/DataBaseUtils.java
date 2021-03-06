package com.hh.utils;

import com.hh.utils.AssertUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static void main(String[] args) throws Exception {
        System.out.println(getMetabolites(10));
    }

    public static void test() {
        try {
            Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from paper_info limit 1");
            int i = 1;
            while (resultSet.next()) {
                System.out.println(resultSet.getObject(i));
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
    }

    public static int insertMetaboliteDiseaseNumber(String metabolite, String disease, int number) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("insert into metabolite_disease_number(metabolite, disease, number) values (?,?,?)");
        preparedStatement.setString(1, metabolite);
        preparedStatement.setString(2, disease);
        preparedStatement.setInt(3, number);
        int flag = preparedStatement.executeUpdate();
        preparedStatement.close();
        connection.close();
        return flag;
    }

    public static void insertPaperInfo(String metabolite, String disease, String title, String url, String abstractText, String mainSentence) throws Exception {
        long textId = -1L;
        Connection connection = getConnection();
        // ??????????????????
        connection.setAutoCommit(false);
        // ?????????text??????????????????
        PreparedStatement psText = connection.prepareStatement("insert into text(abstract, main_sentence) values(?,?);", Statement.RETURN_GENERATED_KEYS);
        // ?????????paper_info ???????????? textId
        PreparedStatement psPaper = connection.prepareStatement("insert into paper_info(metabolite,disease,title,url,text_id) values (?,?,?,?,?);");
        ResultSet rs = null;
        try {
            psText.setString(1, abstractText);
            psText.setString(2, mainSentence);
            int i1 = psText.executeUpdate();
            AssertUtils.sysIsError(i1 == 0, "??????text?????????");
            rs = psText.getGeneratedKeys();
            // ?????? text_id
            if (rs.next()) {
                textId = rs.getLong(1);
            }

            // ??????paper_info ???
            psPaper.setString(1, metabolite);
            psPaper.setString(2, disease);
            psPaper.setString(3, title);
            psPaper.setString(4, url);
            psPaper.setLong(5, textId);
            int i2 = psPaper.executeUpdate();
            AssertUtils.sysIsError(i2 == 0, "??????paper_info?????????");

            // ????????????
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("?????????????????????");
        } finally {
            if (rs != null) {
                rs.close();
            }
            psText.close();
            psPaper.close();
            connection.close();
        }
    }

    public static void insertPaperInfo(String metabolite, String disease, Map<String, Object> map) throws Exception {
        String title = (String) map.get("title");
        String url = (String) map.get("url");
        String abstractText = (String) map.get("abstractText");
        String mainSentence = (String) map.get("mainSentence");
        insertPaperInfo(metabolite, disease, title, url, abstractText, mainSentence);
    }

    public static List<String> getMetabolites(int limit) throws Exception {
        Connection connection = getConnection();

        ArrayList<String> metaboliteNames = new ArrayList<>();
        // ?????????text??????????????????
        PreparedStatement ps = connection.prepareStatement("select name from metabolite limit ?");
        ResultSet rs = null;
        try {
            ps.setInt(1,limit);
            rs = ps.executeQuery();
            while (rs.next()){
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
}
