package com.hh;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataBaseUtils {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost/test?useSSL=true&&characterEncoding=UTF-8&&allowMultiQueries=true&&serverTimezone=UTC", "root", "hh985682");
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
}
