package com.hh;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class DataBaseUtils {
    public static final String DATABASE_URL = "jdbc:mysql://******/for_nlp?useSSL=true&&characterEncoding=UTF-8&&allowMultiQueries=true&&serverTimezone=UTC";
    public static final String DATABASE_USERNAME = "****";
    public static final String DATABASE_PASSWORD = "******";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
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

    public static void insertPaperInfo(String metabolite, String disease, String title, String url, String abstractText) throws Exception {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("insert into paper_info(metabolite,disease,title,url,abstractText) values (?,?,?,?,?);");
        preparedStatement.setString(1, metabolite);
        preparedStatement.setString(2, disease);
        preparedStatement.setString(3, title);
        preparedStatement.setString(4, url);
        preparedStatement.setString(5, abstractText);
        int flag = preparedStatement.executeUpdate();
        preparedStatement.close();
        connection.close();
        AssertUtils.sysIsError(flag == 0,"插入失败");
    }

    public static void insertPaperInfo(String metabolite, String disease, Map<String, Object> map) throws Exception {
        String title = (String) map.get("title");
        String url = (String) map.get("url");
        String abstractText = (String) map.get("abstractText");
        insertPaperInfo(metabolite, disease, title, url, abstractText);
    }

}
