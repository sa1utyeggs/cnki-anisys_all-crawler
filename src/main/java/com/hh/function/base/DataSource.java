package com.hh.function.base;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * @author 86183
 */
@Getter
@Setter
public class DataSource implements javax.sql.DataSource {
    private String databaseUrl;
    private String databaseUsername;
    private String databasePassword;
    private ThreadLocal<Connection> connectionThreadLocal;

    protected PrintWriter logWriter = new PrintWriter(System.out);
    private static volatile int loginTimeout = 0;
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(DataSource.class);

    public DataSource() {
        connectionThreadLocal = new ThreadLocal<>();
    }

    @Override
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

    /**
     * 关闭数据库连接，并移除 threadLocal 中的对象
     */
    public void closeConnection() {
        Connection connection = connectionThreadLocal.get();
        try {
            if (connection != null && !connection.isClosed()) {
                try {
                    // 关闭连接
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                connectionThreadLocal.remove();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // 模仿 DruidDataSource 的写法

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        if (logWriter == null) {
            throw new SQLException("logWriter == null");
        }
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        if (out == null) {
            throw new SQLException("out == null");
        }
        logWriter = out;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        if (seconds < 0) {
            throw new SQLException("seconds < 0");
        }
        loginTimeout = seconds;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        if (loginTimeout < 0) {
            throw new SQLException("loginTimeout < 0");
        }
        return loginTimeout;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        if (iface == null) {
            throw new SQLException("iface == null");
        }
        return iface.isInstance(this);

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface == null) {
            throw new SQLException("iface == null");
        }

        if (iface.isInstance(this)) {
            return (T) this;
        }

        return null;
    }
}
