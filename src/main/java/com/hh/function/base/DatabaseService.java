package com.hh.function.base;

import lombok.Data;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author ab875
 */
@Data
public class DatabaseService {
    private DataSource dataSource;

    /**
     * 获得连接
     *
     * @return 连接
     * @throws SQLException sql
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 关闭连接并删除 ThreadLocal
     */
    public void closeConnection() {
        dataSource.closeConnection();
    }
}
