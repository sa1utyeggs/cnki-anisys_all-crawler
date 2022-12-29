package com.hh.function.system;

import cn.hutool.json.JSON;
import lombok.Data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author 86183
 */
@Data
public class DataSource {
    private String databaseUrl;
    private String databaseUsername;
    private String databasePassword;
    ThreadLocal<Connection> connectionThreadLocal;

    public DataSource() {
        connectionThreadLocal = new ThreadLocal<>();
    }

    public Connection getConnection() throws SQLException {
        Connection connection = connectionThreadLocal.get();
        if (connection == null || connection.isClosed()) {
            connection = initConnection();
        }
        return connection;
    }

    public Connection initConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
        connection.setAutoCommit(false);
        return connection;
    }

    public void remove() {
        Connection connection = connectionThreadLocal.get();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            connectionThreadLocal.remove();
        }
    }
}
