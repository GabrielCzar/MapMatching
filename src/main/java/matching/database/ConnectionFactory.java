package main.java.matching.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {
    private static Connection connection = null;
    private static final String CONFIG = "config.properties";
    private static final String STR_DRIVER = "driver";
    private static final String STR_HOST = "host";
    private static final String STR_USER = "user";
    private static final String STR_PASS = "pass";

    private ConnectionFactory () {}

    public static Connection getConnection() throws SQLException, IOException, PropertyVetoException {
        if (connection == null || connection.isClosed()) {
            Properties properties = new Properties();
            properties.load(ConnectionFactory.class.getClassLoader().getResourceAsStream(CONFIG));

            ComboPooledDataSource pool = new ComboPooledDataSource();
            pool.setDriverClass(properties.getProperty(STR_DRIVER));
            pool.setJdbcUrl(properties.getProperty(STR_HOST));
            pool.setUser(properties.getProperty(STR_USER));
            pool.setPassword(properties.getProperty(STR_PASS));

            pool.setMinPoolSize(3);
            pool.setAcquireIncrement(5);
            pool.setMaxPoolSize(20);
            pool.setCheckoutTimeout(500);
            pool.setMaxStatements(50);

            return pool.getConnection();
        }
        return connection;
    }
}